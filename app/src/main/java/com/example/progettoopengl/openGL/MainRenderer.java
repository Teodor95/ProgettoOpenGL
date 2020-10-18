package com.example.progettoopengl.openGL;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.example.progettoopengl.openGL.models.ModelUtilities;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_CCW;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glFrontFace;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUseProgram;
import static com.example.progettoopengl.openGL.Utilities.ShaderCompiler.createProgram;

/**
 * Classe che serve per il renderer principale del progetto.
 */
public class MainRenderer extends BasicRenderer {

    private static final String VS_FILE = "shaders/VS.glsl";
    private static final String FS_FILE = "shaders/FS.glsl";

    private int shaderHandle;
    private int MVPloc, uInverseModel, umodelM;

    // The Wiew Matrix
    private float[] viewM;

    //The Projection Matrix
    private float[] projM;

    //lights
    private float[] eyePos, lightPos;
    private int uEyePos, uLightPos;

    /**
     * Costruttore della classe
     */
    public MainRenderer() {
        super();
        setGL();
        setLights();

    }

    /**
     * Metodo che serve a inizializzare le matrici.
     */
    private void setGL() {
        viewM = new float[16];
        projM = new float[16];
        Matrix.setIdentityM(viewM, 0);
        Matrix.setIdentityM(projM, 0);

    }

    /**
     * Metodo che serve a impostare la luce
     */
    private void setLights() {
        this.lightPos = new float[]{-0.25f, 5f, 10};
        this.eyePos = new float[]{0f, 0f, 10f};
    }

    /**
     * Metodi GL.
     */

    @Override
    public void setContextAndSurface(Context context, GLSurfaceView surface) {
        super.setContextAndSurface(context, surface);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        super.onSurfaceChanged(gl10, w, h);
        float aspect = ((float) w) / ((float) (h == 0 ? 1 : h));

        //coordinate in openGL vanno da -0 a 1
        //The perspectiveM utility method allows to construct the projection matrix.
        Matrix.perspectiveM(projM, 0, 45f, aspect, 0.1f, 100f);
        //The setLookAtM utility method allows to construct the view matrix.
        Matrix.setLookAtM(viewM, 0, 0, 0f, 2f,
                0, 0, 0,
                0, 1, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);

        try {
            InputStream isV = null;
            InputStream isF = null;
            isV = context.getAssets().open(VS_FILE);
            isF = context.getAssets().open(FS_FILE);
            this.shaderHandle = createProgram(isV, isF);
        } catch (IOException e) {
            Log.v("TAG", "ERORR: Exit APP: ", e);
            System.exit(-1);
        }
        if (this.shaderHandle == -1 || this.shaderHandle < 0) {
            Log.v("TAG", "ERORR: Error in shader(s) compile. Check SHADER_COMPILER " +
                    "logcat tag. Exiting ");
            System.exit(-1);
        }

        this.MVPloc = glGetUniformLocation(shaderHandle, "MVP");
        this.uInverseModel = glGetUniformLocation(shaderHandle, "inverseModel");
        this.umodelM = glGetUniformLocation(shaderHandle, "modelMatrix");
        this.uLightPos = glGetUniformLocation(shaderHandle, "lightPos");
        this.uEyePos = glGetUniformLocation(shaderHandle, "eyePos");
        GLES30.glBindVertexArray(0);

        glUseProgram(shaderHandle);

        ///initialize all models
        ModelUtilities.getInstance().initModelOnSurfCreate(shaderHandle, context);
        glUniform3fv(this.uLightPos, 1, this.lightPos, 0);
        glUniform3fv(this.uEyePos, 1, this.eyePos, 0);

        glUseProgram(0);

        glEnable(GL_DEPTH_TEST); //ombre
        glDepthFunc(GL_LEQUAL); //calcolare le ombre

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK); //abilita il rendering delle facce che si trovano dietro all'oggetto
        glFrontFace(GL_CCW);


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDrawFrame(GL10 gl10) {
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(shaderHandle);
        ModelUtilities.getInstance().drawAllModels(projM, viewM, umodelM, MVPloc, uInverseModel);
        glUseProgram(0);
        GLES30.glBindVertexArray(0);
    }
}

