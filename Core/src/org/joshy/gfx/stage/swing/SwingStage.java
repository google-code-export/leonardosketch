package org.joshy.gfx.stage.swing;

import org.joshy.gfx.Core;
import org.joshy.gfx.draw.GFX;
import org.joshy.gfx.node.Node;
import org.joshy.gfx.node.control.Control;
import org.joshy.gfx.node.layout.Container;
import org.joshy.gfx.node.layout.Panel;
import org.joshy.gfx.stage.AWTEventPublisher;
import org.joshy.gfx.stage.Camera;
import org.joshy.gfx.stage.Stage;
import org.joshy.gfx.util.PerformanceTracker;

import javax.swing.*;
import java.awt.*;

public class SwingStage extends Stage {
    private JFrame frame;
    private SceneComponent scene;
    private boolean skinsDirty = true;
    private boolean layoutDirty = true;
    private boolean drawingDirty = true;
    private int minimumHeight = 10;
    private int minimumWidth = 10;
    protected Panel popupLayer;
    protected Container root;
    protected Container contentLayer;
    private Node contentNode;

    @Override
    public void setUndecorated(boolean undecorated) {
        JFrame newFrame = new JFrame("stage");
        newFrame.setUndecorated(undecorated);
        newFrame.setMinimumSize(new Dimension(200,200));
        newFrame.setSize(frame.getSize());
        newFrame.setLocation(frame.getLocation());
        newFrame.add(scene);
        frame.remove(scene);
        frame.setVisible(false);
        newFrame.setJMenuBar(frame.getJMenuBar());
        newFrame.setVisible(true);
        frame = newFrame;
        scene.requestFocus();
    }

    @Override
    public void hide() {
        frame.setVisible(false);
    }

    @Override
    public void raiseToTop() {
        frame.setVisible(true);
        frame.toFront();
    }

    public SwingStage() {
        super();
        frame = new JFrame("stage");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(200,200));
        root = new Container() {
            @Override
            protected void setSkinDirty() {
                super.setSkinDirty();
                SwingStage.this.skinsDirty = true;
            }

            @Override
            protected void setLayoutDirty() {
                super.setLayoutDirty();
                SwingStage.this.layoutDirty = true;
            }

            @Override
            public void doLayout() {
                contentLayer.setWidth(getWidth());
                contentLayer.setHeight(getHeight());
                super.doLayout();
            }
            @Override
            public void setDrawingDirty(Node node) {
                super.setDrawingDirty(node);
                scene.repaint();
                SwingStage.this.drawingDirty = true;
            }

            @Override
            public void setLayoutDirty(Node node) {
                super.setLayoutDirty(node);
                scene.repaint();
                layoutDirty = true;
            }

            public Stage getStage() {
                return SwingStage.this;
            }

            @Override
            public void setSkinDirty(Node node) {
                super.setSkinDirty(node);
                scene.repaint();
                skinsDirty = true;
            }

            @Override
            public void draw(GFX g) {
                for(Node child : children) {
                    g.translate(child.getTranslateX(),child.getTranslateY());
                    child.draw(g);
                    g.translate(-child.getTranslateX(),-child.getTranslateY());
                }
                this.drawingDirty = false;
            }
        };
        root.setId("root");
        scene = new SceneComponent();
        contentLayer = new Container() {
            @Override
            public void doLayout() {
                for(Node n : children()) {
                    if(n instanceof Control) {
                        Control c = (Control) n;
                        c.setWidth(getWidth());
                        c.setHeight(getHeight());

                        c.doLayout();
                    }
                }
            }

            @Override
            public void draw(GFX g) {
                //g.setPaint(FlatColor.RED);
                //g.fillRect(0,0,getWidth(),getHeight());
                for(Node child : children) {
                    g.translate(child.getTranslateX(),child.getTranslateY());
                    child.draw(g);
                    g.translate(-child.getTranslateX(),-child.getTranslateY());
                }
                this.drawingDirty = false;
            }
        };
        root.add(contentLayer);
        popupLayer = new Panel();
        root.add(popupLayer);
        frame.add(scene);
        frame.setSize(500,500);
        frame.setVisible(true);
        scene.requestFocus();
        frame.addWindowListener(scene.publisher);
        ((SwingCore)Core.getShared()).addStage(this);
    }

    @Override
    public void setContent(Node node) {
        this.contentNode = node;
        contentLayer.add(node);
        this.skinsDirty = true;
        this.layoutDirty = true;
        this.drawingDirty = true;
    }

    @Override
    public Node getContent() {
        return contentNode;
    }

    @Override
    public void setCamera(Camera camera) {
        //swing stage doesn't use cameras at all
    }

    @Override
    public void setWidth(double width) {
        this.frame.setSize((int)width,this.frame.getHeight());
    }

    @Override
    public double getWidth() {
        return this.frame.getWidth();
    }

    public void setHeight(double height) {
        this.frame.setSize(this.frame.getWidth(), (int) height);
    }

    @Override
    public double getX() {
        return frame.getX();
    }

    @Override
    public double getY() {
        return frame.getY();
    }

    @Override
    public void setMinimumWidth(double width) {
        this.minimumWidth = (int) width;
        this.frame.setMinimumSize(new Dimension(minimumWidth,minimumHeight));
    }

    @Override
    public void setMinimumHeight(double height) {
        this.minimumHeight = (int) height;
        this.frame.setMinimumSize(new Dimension(minimumWidth,minimumHeight));
    }

    @Override
    public Container getPopupLayer() {
        return popupLayer;
    }

    @Override
    public Object getNativeWindow() {
        return frame;
    }

    @Override
    public void setTitle(String title) {
        frame.setTitle(title);
    }


    @Override
    public double getHeight() {
        return this.frame.getHeight();

    }

    private class SceneComponent extends JComponent {
        private AWTEventPublisher publisher;

        private SceneComponent() {
            publisher = new AWTEventPublisher(root);
            this.addMouseListener(publisher);
            this.addMouseMotionListener(publisher);
            this.addMouseWheelListener(publisher);
            this.addKeyListener(publisher);
            this.setFocusTraversalKeysEnabled(false);
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            layoutDirty = true;
            drawingDirty = true;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if(skinsDirty) {
                doSkins();
                skinsDirty = false;
            }
            if(layoutDirty) {
                doGFXLayout(getWidth(),getHeight());
                layoutDirty = false;
            }
            //if paintcomponent was called, then we must always draw
            //if(drawingDirty) {
                doGFXDrawing(graphics, getWidth(), getHeight());
                drawingDirty = false;
            //}
        }

        private void doSkins() {
            PerformanceTracker.getInstance().skinStart();
            root.doSkins();
            PerformanceTracker.getInstance().skinEnd();
        }

        private void doGFXLayout(int width, int height) {
            PerformanceTracker.getInstance().layoutStart();
            root.setWidth(width);
            root.setHeight(height);
            root.doLayout();
            PerformanceTracker.getInstance().layoutEnd();
        }

        private void doGFXDrawing(Graphics graphics, int width, int height) {
            PerformanceTracker.getInstance().drawStart();
            //graphics.setColor(Color.BLUE);
            //graphics.fillRect(0,0,getWidth(),getHeight());
            GFX gfx = new SwingGFX((Graphics2D) graphics);
            root.draw(gfx);
            gfx.dispose();
            PerformanceTracker.getInstance().drawEnd();
        }
    }



}

