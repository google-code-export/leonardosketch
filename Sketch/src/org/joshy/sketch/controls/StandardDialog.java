package org.joshy.sketch.controls;

import org.joshy.gfx.util.u;

import javax.swing.*;

/**
* Created by IntelliJ IDEA.
* User: joshmarinacci
* Date: Apr 29, 2010
* Time: 9:07:58 PM
* To change this template use File | Settings | File Templates.
*/
public class StandardDialog {
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null
                , message
                , message
                , JOptionPane.OK_OPTION
        );

    }

    public static enum Result {Yes, No, Cancel};
    public static Result showYesNoCancel(String text, String yesText, String noText, String cancelText) {
        int result = JOptionPane.showOptionDialog(null
                , text
                , "Warning"
                , JOptionPane.YES_NO_CANCEL_OPTION
                , JOptionPane.WARNING_MESSAGE
                , null
                , new String[]{yesText, cancelText, noText}
                , yesText
        );
        u.p("result = " + result);
        switch(result) {
            case 0: return Result.Yes;
            case 1: return Result.Cancel;
            case 2: return Result.No;
            default: return Result.Cancel;
        }
    }
    
    public static String showEditText(String text, String value) {
        String result = JOptionPane.showInputDialog(text,value);
        return result;
    }
}
