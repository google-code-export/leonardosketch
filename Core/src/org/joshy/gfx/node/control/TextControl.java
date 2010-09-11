package org.joshy.gfx.node.control;

import org.joshy.gfx.Core;
import org.joshy.gfx.SkinManager;
import org.joshy.gfx.draw.Font;
import org.joshy.gfx.event.*;
import org.joshy.gfx.util.OSUtil;
import org.joshy.gfx.util.u;

import java.util.Date;

/*
 redesign text control to fix all of the bugs
 first, there should be a text model which holds the actual strings of text
 and converts between the various locations

 convert an x y pixel coordinate to a character point
 convertXYToCharPoint()

 convert a character point to the line or createColumn
 getLine(cp)
 getColumn(cp)

 delete or insert a char by typing:
 cursor == current char point
 model.insertChar(cursor,char);
 model.deleteChar(cursor);

 move cursor
 cursor = current char point
 cursor.advanceColumn()

 cursor.getX()
 cursor.getY()

 the text model simply stores lines of text and allows their editing
 the char point converts between screen coords to row & createColumn
 the textcontrol implements the actual drawing and input handleing 

 */
public abstract class TextControl extends Control implements Focusable {
    protected boolean focused;
    protected String text = "";
    Font font;

    protected boolean allowMultiLine = false;
    private CursorPoint currentCursorPoint = null;
    private Font realFont;
    protected TextSelection selection = new TextSelection(this);

    protected TextControl() {
        currentCursorPoint = new CursorPoint(0,0,0,0,0,0,0);

        EventBus.getSystem().addListener(this, MouseEvent.MousePressed, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                Core.getShared().getFocusManager().setFocusedNode(TextControl.this);
                if(selection.isActive() && !event.isShiftPressed()) {
                    selection.clear();
                }
                if(text.length() >= 1) {
                    double ex = filterMouseX(event.getX());
                    double ey = filterMouseY(event.getY());
                    currentCursorPoint = mouseXYToCursorPoint(ex,ey,text);
                    u.p("set to : " + currentCursorPoint);
                } else {
                    currentCursorPoint = new CursorPoint(0,0,0,0,0,0,0);
                }
                if(!selection.isActive()) {
                    selection.setStart(currentCursorPoint);
                }
                if(event.isShiftPressed() && selection.isActive()) {
                    selection.setEnd(currentCursorPoint);
                }
            }
        });

        EventBus.getSystem().addListener(this, MouseEvent.MouseDragged, new Callback<MouseEvent>(){
            public void call(MouseEvent event) {
                Core.getShared().getFocusManager().setFocusedNode(TextControl.this);
                if(text.length() >= 1) {
                    currentCursorPoint = mouseXYToCursorPoint(event.getX(),event.getY(),text);
                } else {
                    currentCursorPoint = new CursorPoint(0,0,0,0,0,0,0);
                }
                if(selection.isActive()) {
                    selection.setEnd(currentCursorPoint);
                }
            }
        });

        EventBus.getSystem().addListener(FocusEvent.All, new Callback<FocusEvent>(){
            public void call(FocusEvent event) {
                if(event.getType() == FocusEvent.Lost && event.getSource() == TextControl.this) {
                    focused = false;
                    setDrawingDirty();
                }
                if(event.getType() == FocusEvent.Gained && event.getSource() == TextControl.this) {
                    focused = true;
                    setDrawingDirty();
                }
            }
        });
        
        EventBus.getSystem().addListener(this, KeyEvent.KeyPressed, new Callback<KeyEvent>() {
            public void call(KeyEvent event) {
                processKeyEvent(event);
            }
        });

        EventBus.getSystem().addListener(this, MouseEvent.MouseReleased, new Callback<MouseEvent>() {
            public long lastRelease = 0;
            public int clickCount = 0;

            public void call(MouseEvent event) {
                long now = new Date().getTime();
                if(now-lastRelease < 400) {
                } else {
                    clickCount = 0;
                }
                clickCount++;
                lastRelease = now;
                if(clickCount == 3) {
                    selectAll();
                }
            }
        });
    }

    protected double filterMouseY(double y) {
        return y;
    }

    protected double filterMouseX(double x) {
        return x;
    }

    private void processKeyEvent(KeyEvent event) {
        int cursorCharX = cursorPointToCursorChar(currentCursorPoint);
        
        if(event.getKeyCode().equals(KeyEvent.KeyCode.KEY_V) && event.isSystemPressed()) {
            insertText(OSUtil.getClipboardAsString());
            return;
        }
        
        if(event.isTextKey()) {
            insertText(event.getGeneratedText());
            return;
        }

        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_ENTER && allowMultiLine) {
            if(selection.isActive()) {
                replaceAndClearSelectionWith("\n");
            } else {
                insertAtCursor("\n");
            }
            return;
        }
        
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_BACKSPACE) {
            if(text.length() > 0 && (cursorCharX > 0 || selection.isActive())) {
                if(selection.isActive()) {
                    replaceAndClearSelectionWith("");
                } else {
                    cursorCharX--;
                    String beforeString = text.substring(0, cursorCharX);
                    String afterString = text.substring(cursorCharX+1,text.length());
                    text = beforeString + afterString;
                    currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
                    EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.StringChanged,text,TextControl.this));
                    setDrawingDirty();
                }
                return;
            }
        }
        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_DELETE) {
            if(text.length() > 0 && (cursorCharX < text.length()-1)) {
                if(selection.isActive()) {
                    replaceAndClearSelectionWith("");
                } else {
                    String beforeString = text.substring(0, cursorCharX);
                    String afterString = text.substring(cursorCharX+1,text.length());
                    text = beforeString + afterString;
                    currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
                    EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.StringChanged,text,TextControl.this));
                    setDrawingDirty();
                }
            }
            return;
        }

        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_LEFT_ARROW) {
            if(selection.isActive() && !event.isShiftPressed()) {
                selection.clear();
                return;
            }
            if(event.isShiftPressed() && !selection.isActive()) {
                selection.setStart(currentCursorPoint);
            }
            if(cursorCharX > 0) {
                cursorCharX--;
                currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
                setDrawingDirty();
            }
            if(event.isShiftPressed() && selection.isActive()) {
                selection.setEnd(currentCursorPoint);
            }
        }

        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_RIGHT_ARROW) {
            if(selection.isActive() && !event.isShiftPressed()) {
                selection.clear();
                return;
            }
            if(event.isShiftPressed() && !selection.isActive()) {
                selection.setStart(currentCursorPoint);
            }
            if(cursorCharX < text.length()) {
                cursorCharX++;
                currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
                setDrawingDirty();
            }
            if(event.isShiftPressed() && selection.isActive()) {
                selection.setEnd(currentCursorPoint);
            }
        }

        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_UP_ARROW && allowMultiLine) {
            if(selection.isActive() && !event.isShiftPressed()) {
                selection.clear();
                return;
            }
            if(event.isShiftPressed() && !selection.isActive()) {
                selection.setStart(currentCursorPoint);
            }
            CursorPoint cp = cursorCharToCursorPoint(cursorCharX, getText());
            if(cp.row > 0) {
                cp.row--;
            }
            cursorCharX = cursorPointToCursorChar(cp);
            currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
            setDrawingDirty();
            if(event.isShiftPressed() && selection.isActive()) {
                selection.setEnd(currentCursorPoint);
            }
        }

        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_DOWN_ARROW && allowMultiLine) {
            if(selection.isActive() && !event.isShiftPressed()) {
                selection.clear();
                return;
            }
            if(event.isShiftPressed() && !selection.isActive()) {
                selection.setStart(currentCursorPoint);
            }
            CursorPoint cp = cursorCharToCursorPoint(cursorCharX, getText());
            if(cp.row < cp.rowCount-1) {
                cp.row++;
            }
            cursorCharX = cursorPointToCursorChar(cp);
            currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
            setDrawingDirty();
            if(event.isShiftPressed() && selection.isActive()) {
                selection.setEnd(currentCursorPoint);
            }
        }

        if(event.getKeyCode() == KeyEvent.KeyCode.KEY_TAB) {
            if(event.isShiftPressed()) {
                Core.getShared().getFocusManager().gotoPrevFocusableNode();
            } else {
                Core.getShared().getFocusManager().gotoNextFocusableNode();
            }
        }
    }

    private void insertText(String generatedText) {
        int cursorCharX = cursorPointToCursorChar(currentCursorPoint);
        if(selection.isActive() && text.length() >= 1) {
            replaceAndClearSelectionWith(generatedText);
            return;
        }

        if(text.length() >= 1) {
            text = text.substring(0, cursorCharX) +
                generatedText+
                text.substring(cursorCharX,text.length());
        } else {
            text = generatedText;
        }
        if(selection.isActive()) {
            selection.clear();
        }
        cursorCharX++;
        currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.StringChanged,text,TextControl.this));
        setDrawingDirty();
    }

    private void insertAtCursor(String string) {
        int cursorCharX = cursorPointToCursorChar(currentCursorPoint);
        if(text.length() >= 1) {
            text = text.substring(0, cursorCharX) +
                "\n"+
                text.substring(cursorCharX,text.length());
        } else {
            text = "\n";
        }
        if(selection.isActive()) {
            selection.clear();
        }
        cursorCharX++;
        currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);
        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.StringChanged,text,TextControl.this));
        setDrawingDirty();
    }

    private void replaceAndClearSelectionWith(String replacementText) {
        int cursorCharX = cursorPointToCursorChar(currentCursorPoint);
        if(text.length() < 1) {
            text = replacementText;
        } else {
            String beforeString = text.substring(0, rowColumnToCursorChar(selection.getLeadingRow(),selection.getLeadingColumn()));
            String afterString = text.substring(rowColumnToCursorChar(selection.getTrailingRow(),selection.getTrailingColumn()),text.length());
            cursorCharX = beforeString.length();
            cursorCharX++;
            text = beforeString + replacementText + afterString;
        }
        currentCursorPoint = cursorCharToCursorPoint(cursorCharX,text);

        selection.clear();

        EventBus.getSystem().publish(new ChangedEvent(ChangedEvent.StringChanged,text,TextControl.this));
        setDrawingDirty();
    }

    public void setText(String text) {
        this.text = text;
        setDrawingDirty();
    }

    public void setFont(Font font) {
        this.realFont = font;
    }

    public Font getFont() {
        if(realFont != null) {
            return realFont;
        }
        return this.font;
    }

    public boolean isFocused() {
        return focused;
    }

    @Override
    public void doSkins() {
        cssSkin = SkinManager.getShared().getCSSSkin();
        font = cssSkin.getDefaultFont();
    }

    @Override
    public void doLayout() {
    }

    protected CursorPoint getCurrentCursorPoint() {
        return currentCursorPoint;
    }


    CursorPoint mouseXYToCursorPoint(double x, double y, String text) {
        String[] lines = text.split("\n");
        double cursorW = 1;
        Font font = getFont();
        double cursorH = font.getAscender() + font.getDescender();
        double cursorX = x;
        double cursorY = 0;
        int row = 0;
        int col = 0;
        double lineHeight = font.getAscender()+font.getDescender()+font.getLeading();
        //calculate the row first
        for(String line : lines) {
            if(y > cursorY && y < cursorY + lineHeight) {
                break; 
            }
            cursorY += lineHeight;
            row++;
        }
        if(row >= lines.length) {
            //u.p("too far");
            row = lines.length-1;
            cursorY = row * lineHeight;
        }

        //calculate the createColumn next
        String line = lines[row];
        double prevLen = 0;
        if(font.getWidth(line) < x) {
            col = line.length()-1;
            cursorX = font.getWidth(line);
        } else {
            for(int i=0; i<line.length(); i++) {
                double len = font.getWidth(line.substring(0,i+1));
                //u.p("checking len " + len);
                if(len > x) {
                    //u.p("dist to prev = " + (cursorX-prevLen));
                    //u.p("dist to clen = " + (len-cursorX));
                    //if closer to left edge
                    if(cursorX-prevLen < len-cursorX) {
                        cursorX = prevLen;
                        //col--;
                    } else {
                        cursorX = len;
                        col++;
                    }
                    break;
                }
                col++;
                prevLen = len;
            }
        }

        CursorPoint cp = new CursorPoint(cursorX, cursorY, cursorW, cursorH, row, col, lines.length);
        return cp;
    }
    
    CursorPoint cursorCharToCursorPoint(int cursorCharX, String text) {
        String[] lines = text.split("\n");
        double cursorY=0;
        int cursorLeft = cursorCharX;
        double cursorX = 0;
        double cursorW = 1;
        Font font = getFont();
        double cursorH = font.getAscender() + font.getDescender();
        int row = 0;
        for(String line : lines) {
            if(line.length() < cursorLeft) {
                cursorLeft -= (line.length()+1); //the +1 is to account for the \n
                cursorY += font.getAscender()+font.getDescender()+font.getLeading();
                cursorX = 0;
                row++;
            } else {
                cursorX = font.getWidth(line.substring(0,cursorLeft));
                break;
            }
        }
        int col = cursorLeft;
        return new CursorPoint(cursorX,cursorY, cursorW, cursorH, row, col, lines.length);
    }

    int rowColumnToCursorChar(int row, int col) {
        String[] lines = text.split("\n");
        int cx = 0;
        for(int r = 0; r<row; r++) {
            cx += lines[r].length()+1;
        }
        if(row >= lines.length) {
            cx = 0;
        } else {
            if(col > lines[row].length()) {
                cx += lines[row].length();
            } else {
                cx += col;
            }
        }
        return cx;
    }
    
    int cursorPointToCursorChar(CursorPoint cp) {
        return rowColumnToCursorChar(cp.row,cp.col);
    }

    public String getText() {
        return text;
    }

    protected class CursorPoint {
        protected double cursorX;
        double cursorY;
        double cursorW;
        double cursorH;
        public int row;
        int col;
        public int rowCount;

        public CursorPoint(double cursorX, double cursorY, double cursorW, double cursorH, int row, int col, int rowCount) {
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.cursorW = cursorW;
            this.cursorH = cursorH;
            this.row  = row;
            this.col = col;
            this.rowCount = rowCount;
        }

        @Override
        public String toString() {
            return "CursorPoint{" +
                    "cursorX=" + cursorX +
                    ", cursorY=" + cursorY +
                    ", cursorW=" + cursorW +
                    ", cursorH=" + cursorH +
                    ", row=" + row +
                    ", col=" + col +
                    ", rowCount=" + rowCount +
                    '}';
        }
    }

    public static class TextSelection {
        private TextControl textControl;
        private boolean active;
        public int startRow;
        public int startCol;
        private int endRow;
        public int endCol;

        private TextSelection(TextControl textControl) {
            this.textControl = textControl;
        }

        public void clear() {
            active = false;
            textControl.setDrawingDirty();
        }

        protected boolean isActive() {
            return active;
        }

        public void setStart(CursorPoint cp) {
            active = true;
            startRow = cp.row;
            endRow = cp.row;
            startCol = cp.col;
            endCol = cp.col;
        }

        public void setEnd(CursorPoint cp) {
            endRow = cp.row;
            endCol = cp.col;
        }

        public void selectAll() {
            active = true;
            startRow = 0;
            startCol = 0;
            String[] lines = textControl.text.split("\n");
            endRow = lines.length-1;
            endCol = lines[lines.length-1].length()-1;
        }

        public int getLeadingRow() {
            if(endRow < startRow) {
                return endRow;
            } else {
                return startRow;
            }
        }

        public int getTrailingRow() {
            if(endRow < startRow) {
                return startRow;
            } else {
                return endRow;
            }
        }
        
        public int getLeadingColumn() {
            if(endRow < startRow) return endCol;
            if(endCol < startCol && startRow == endRow) return endCol;
            return startCol;
        }

        public int getTrailingColumn() {
            if(endRow < startRow) return startCol;
            if(endCol < startCol && startRow == endRow) return startCol;
            return endCol;
        }

    }

    public void selectAll() {
        //To change body of created methods use File | Settings | File Templates.
        selection.selectAll();
        setDrawingDirty();
    }

}
