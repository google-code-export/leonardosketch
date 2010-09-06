package org.joshy.sketch.actions.io;

import org.joshy.gfx.util.u;
import org.joshy.sketch.actions.SAction;
import org.joshy.sketch.actions.io.SavePNGAction;
import org.joshy.sketch.modes.DocContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * An action to send the current doc as PNG attachment to an email
 */
public class SendMacMail extends SAction {
    private DocContext context;

    public SendMacMail(DocContext context) {
        this.context = context;
    }

    @Override
    public void execute() {
        try {
            File file = File.createTempFile("foo", ".png");
            file.deleteOnExit();
            SavePNGAction.export(file, context.getDocument());
            u.p("wrote image to "+file.getAbsolutePath());


            File script = File.createTempFile("foo",".applescript");
            PrintWriter out = new PrintWriter(new FileOutputStream(script));
            out.println("set theAttachment to \""+file.getAbsolutePath()+"\"");
            out.println("tell application \"Mail\"");
            out.println("    set theMessage to make new outgoing message with properties {visible:true, subject:\""
                    +"Sketchy doc:"+ context.getDocument().getTitle()+" \","
                    +" content:\"Here's the document I've been working on.\n\n\n\n\"}");
            out.println("    tell content of theMessage");
            out.println("        make new attachment with properties {file name:theAttachment} at after last paragraph");
            out.println("    end tell");
            out.println("    activate");
            /*
            out.println("    tell application \"System Events\" to tell process \"Mail\"");
            out.println("        delay 1");
            out.println("        if name of menu item -1 of menu 1 of menu bar item 8 of menu bar 1 contains \"Plain Text\" then");
            out.println("            keystroke \"t\" using {shift down, command down}");
            out.println("        end if");
            out.println("    end tell");
            */
            out.println("end tell");
            out.close();

            u.p("wrote script to: " + script.getAbsolutePath());
            String[] cmd = new String[]{
                    "arch","-i386",
                    "osascript",script.getAbsolutePath() 
            };
            Runtime.getRuntime().exec(cmd);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
