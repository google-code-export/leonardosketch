package org.joshy.gfx.stage;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Apr 25, 2010
 * Time: 10:32:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PerspectiveCamera extends Camera {
    double front = 100;
    double back = 500;

    @Override
    public void configureDisplay(GL2 gl, Stage stage, double w, double h) {
        gl.glClearColor(1f, 1f, 1f, 0);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);
        // this camera should give us a 3d perspective view that is
        // still pixel matching when objects are at z=0
        gl.glFrustum( -w/2, w/2, h/2, -h/2, front, back );
        gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslated(-w,-h,0);
        gl.glScaled(2,2,1);
        gl.glLineWidth(2.0f);

    }

    public void setFront(double front) {
        this.front = front;
    }

    public void setBack(double back) {
        this.back = back;
    }
}
