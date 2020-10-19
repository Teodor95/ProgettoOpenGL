package com.example.progettoopengl.openGL;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES10.GL_VERSION;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class BasicRenderer implements GLSurfaceView.Renderer {

    protected static String TAG; //for debug
    protected float[] clearScreen; //we hold the screen color
    protected Point currentScreen; //we hold the current screen size
    protected Context context; //reference to the app Context
    protected GLSurfaceView surface; //reference to the actual surface

    public BasicRenderer() {
        this(0, 0, 0);
    }

    public BasicRenderer(float r, float g, float b) {
        this(r, g, b, 1);
    }

    public BasicRenderer(float r, float g, float b, float a) {
        TAG = getClass().getSimpleName();
        clearScreen = new float[]{r, g, b, a};
        currentScreen = new Point(0, 0);
    }

    protected void setContextAndSurface(Context context, GLSurfaceView surface) {
        this.context = context;
        this.surface = surface;
    }

    /**
     * Metodi GL.
     */

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(clearScreen[0], clearScreen[1], clearScreen[2], clearScreen[3]);
        Log.v(TAG, "onSurfaceCreated" + Thread.currentThread().getName());
        Log.v(TAG, gl.glGetString(GL_VERSION));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.v(TAG, "onSurfaceChanged" + Thread.currentThread().getName());

        /* glViewport - Sets the parameters to define the transformation of vertex positions
         from NDC (normalized device coordinates) space to window space
         */
        glViewport(0, 0, width, height);
        currentScreen.x = width;
        currentScreen.y = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
