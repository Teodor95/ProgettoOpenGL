package com.example.progettoopengl.openGL.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.progettoopengl.openGL.Utilities.Models;
import com.example.progettoopengl.openGL.Utilities.PlyObject;
import com.example.progettoopengl.openGL.Utilities.View;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static android.content.ContentValues.TAG;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_NEAREST;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES32.GL_CLAMP_TO_BORDER;

/**
 * Classe che serve a definire il modello.
 */
public class GenericModel {

    private Models modelName;
    private View view;
    public int[] VAO, VBO;
    protected float[] translation;
    protected float[] rotation;
    protected float[] scaling;

    private String ply;
    private float[] vertices;
    private int[] indices;

    public final float[] MVP, modelM, tempM, inverseModel;

    //Texture
    private String texture;
    private int[] texUnit;
    private int elements;
    private int[] texObjId;

    private int shaderHandle;

    //Model lights
    private int lightDPos;
    private int usingLightD;
    private int randomDLight;

    //Model sensors
    private int usingSensor;
    private int sensorAreasPos;
    private float[] sensorAreas;

    /**
     * Costruttore della classe per definire un modello.
     *
     * @param view     la view a cui è associato il modello.
     * @param m        il modello che si viene a creare.
     * @param ply      il ply associato al modello.
     * @param rotation i parametri di rotazione.
     * @param scaling  i parametri di scale.
     * @param texture  la texture utilizzata dal modello.
     */
    public GenericModel(View view, Models m, String ply, float[] rotation, float[] scaling, String texture) {
        this.view = view;
        this.modelName = m;
        this.ply = ply;
        this.shaderHandle = 0;
        this.texture = texture;

        this.rotation = rotation;
        this.scaling = scaling;
        this.translation = new float[3];

        VAO = new int[1]; //A VAO is a collection of VBOs and related attributes’ configuration.
        VBO = new int[2]; // color and vertex position.

        MVP = new float[16];
        modelM = new float[16];
        tempM = new float[16];
        inverseModel = new float[16];

        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(MVP, 0);
        Matrix.setIdentityM(inverseModel, 0);

    }

    /**
     * Metodo che serve a ritornare la matrice temporanea.
     *
     * @return la matrice temporanea.
     */
    public float[] getTempM() {
        return tempM;
    }

    /**
     * Metodo che serve a inizializzare il modello lato GL.
     *
     * @param shaderHandle la compilazione del programma GL.
     * @param context      il contesto dell'ambiente dell'applicazione android.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setModel(int shaderHandle, Context context) {

        this.shaderHandle = shaderHandle;

        InputStream is;
        try {
            is = context.getAssets().open(this.ply);
            PlyObject po = new PlyObject(is);
            po.parse();
            this.vertices = po.getVertices();
            this.indices = po.getIndices();
        } catch (IOException | NumberFormatException e) {
            Log.v(TAG, "Error:", e);
        }

        Log.v(TAG, "Allocate Data");
        FloatBuffer vertexData = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);

        IntBuffer indexData = ByteBuffer.allocateDirect(indices.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();
        indexData.put(indices);
        indexData.position(0);
        this.elements = this.indices.length;

        //Splitting an array into two FloatBuffers
        glGenBuffers(2, VBO, 0);
        GLES30.glGenVertexArrays(1, VAO, 0);
        GLES30.glBindVertexArray(VAO[0]);

        //Bind buffers
        glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
        //Device Memory Allocation and Transfer
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexData.capacity(), vertexData, GL_STATIC_DRAW);

        if (this.modelName == Models.SENSOR_CAR) {
            /*
            glVertexAttribPointer allows the developer to define the buffer layout
            explain to the GPU how the data is structured.

            Size: Given an attribute buffer as a set of tuples of type T, the element size is the
                    number of element within the tuple: sizeof(T) * (number of elements in tuple)
            Stride: Given an attribute buffer as a set of tuples of type T, the element stride is the
                    number of elements between the last element of a tuple and the first element of
                    the subsequent tuple of the same attribute buffer.
            */
            glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 12, 0); //vpos
            glVertexAttribPointer(2, 3, GL_FLOAT, false, Float.BYTES * 12, 3 * Float.BYTES); //normals
        } else {
            glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES * 8, 0); //vpos
            glVertexAttribPointer(2, 3, GL_FLOAT, false, Float.BYTES * 8, 3 * Float.BYTES); //normals
        }


        glEnableVertexAttribArray(1); //location 1 - vpos
        glEnableVertexAttribArray(2); // location 2 - normals
        glEnableVertexAttribArray(3); // location 3 - texcoord

        glVertexAttribPointer(3, 2, GL_FLOAT, false, Float.BYTES * 8, 6 * Float.BYTES); //texcoord
        initTexture(context, this.shaderHandle);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBO[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexData.capacity(), indexData,
                GL_STATIC_DRAW);
        initByModel();

    }

    /**
     * Tale Metodo serve a inizializzare la parte texture OpenGl.
     *
     * @param context      il contesto dell'ambiente dell'applicazione android.
     * @param shaderHandle la compilazione del programma GL.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initTexture(Context context, int shaderHandle) {

        //texture object creation
        this.texUnit = new int[3];
        // Attribute location index
        this.texUnit[0] = glGetUniformLocation(shaderHandle, "moondiff");
        this.texUnit[1] = glGetUniformLocation(shaderHandle, "moonspec");

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        int id = context.getResources().getIdentifier(this.texture, "drawable", context.getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id, opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        this.texObjId = new int[1];
        glGenTextures(1, texObjId, 0);

        glBindTexture(GL_TEXTURE_2D, texObjId[0]);
        //what happens if we scale-down the texture?
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        //what happens if we scale-up the texture?
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

        //Transfer host data to device-only memory
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId[0]);

        glUniform1i(texUnit[0], 0); //0 because active texture is GL_TEXTURE0.
        glUniform1i(texUnit[1], 1);

        int texScalingLoc = glGetUniformLocation(shaderHandle, "texScaling");
        int texOffsetLoc = glGetUniformLocation(shaderHandle, "texOffset");
        glUniform2f(texScalingLoc, -0.5f, -0.5f);
        glUniform2f(texOffsetLoc, 0.5f, 0.5f);

        glBindTexture(GL_TEXTURE_2D, 0);

        if (bitmap == null) {
            Log.v("NULL", "ERRORE: bitmap è null, exit");
            System.exit(-1);
        } else
            bitmap.recycle();
    }

    /**
     * Metodo private che serve a inizializzare determinate variabili solo nel caso appartengano
     * ad un certo modello.
     */
    private void initByModel() {
        switch (this.modelName) {
            case CIRCLE_LIGHTS: {
                this.randomDLight = 1;
                this.usingLightD = glGetUniformLocation(shaderHandle, "usingLightD");
                this.lightDPos = glGetUniformLocation(shaderHandle, "lightDPos");
                break;
            }
            case SENSOR_CIRCLE: {
                this.usingSensor = glGetUniformLocation(shaderHandle, "usingSensor");
                this.sensorAreasPos = glGetUniformLocation(shaderHandle, "sensorAreasPos");
                this.sensorAreas = new float[4];
                break;
            }
        }
    }

    /**
     * Metodo che serve a inizializzare l'array dei sensori della car in modo randomico.
     *
     * @param areas l'array contenente valori randomici.
     */
    public void setSensorAreas(float[] areas) {
        this.sensorAreas = areas.clone();
    }

    /**
     * Metodo che serve a ritornare l'array ddella rotazione.
     *
     * @return l'array della rotazione.
     */
    public float[] getRotation() {
        return rotation;
    }

    /**
     * Metodo che serve a specificare quale texture unit attivare e bindare.
     */
    public void textureUpdate() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.texObjId[0]);
    }

    /**
     * Metodo che serve a ritornare la view del modello.
     *
     * @return la view del modello.
     */
    public View getView() {
        return this.view;
    }

    /**
     * Metodo che serve a ritornare il nome del modello
     *
     * @return il nome del modello.
     */
    public Models getModelName() {
        return this.modelName;
    }

    /**
     * Metodo che serve a modificare i valori di traslazione.
     *
     * @param x il valore x.
     * @param y il valore y.
     * @param z il valore z.
     */
    public void changeTranslation(float x, float y, float z) {
        // y,x,z
        this.translation[0] = x;
        this.translation[1] = y;
        this.translation[2] = z;
    }

    /**
     * Metodo che serve a modificare i valori di rotazione
     *
     * @param x il valore x.
     * @param y il valore y.
     * @param z il valore z.
     */
    public void changeRotation(float x, float y, float z) {
        this.rotation[0] = x;
        this.rotation[1] = y;
        this.rotation[2] = z;
    }

    /**
     * Metodo OpenGL per il draw dei modelli.
     *
     * @param umodelM       glGetUniformLocation di modelMatrix.
     * @param MVPloc        glGetUniformLocation di MVP.
     * @param uInverseModel glGetUniformLocation di inverseModel.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void draw(int umodelM, int MVPloc, int uInverseModel) {

        GLES30.glBindVertexArray(this.VAO[0]);

        Matrix.setIdentityM(modelM, 0);

        Matrix.translateM(modelM, 0, translation[0], translation[1], translation[2]);

        Matrix.rotateM(modelM, 0, rotation[2], 0, 0, 1);
        Matrix.rotateM(modelM, 0, rotation[1], 0, 1, 0);
        Matrix.rotateM(modelM, 0, rotation[0], 1, 0, 0);

        Matrix.scaleM(modelM, 0, scaling[0], scaling[1], scaling[2]);

        glUniformMatrix4fv(umodelM, 1, false, modelM, 0);
        Matrix.multiplyMM(MVP, 0, tempM, 0, modelM, 0);
        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
        Matrix.invertM(inverseModel, 0, modelM, 0);
        glUniformMatrix4fv(uInverseModel, 1, true, inverseModel, 0);
        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);

        drawByModel();
        glDrawElements(GL_TRIANGLES, this.elements, GL_UNSIGNED_INT, 0);
        resetByModel();
    }

    /**
     * Metodo che serve a modificare quale area deve essere illuminata nel cruscotto.
     *
     * @param random il valore contentetne l'area.
     */
    public void updateLightDash(int random) {
        this.randomDLight = random;
    }

    /**
     * Metodo che serve a fare il draw di certi oggetti in base al modello.
     */
    private void drawByModel() {
        switch (this.modelName) {
            case CIRCLE_LIGHTS: {
                glUniform1i(this.usingLightD, 1);
                glUniform1i(this.lightDPos, this.randomDLight);
                break;
            }
            case SENSOR_CIRCLE: {
                glUniform1i(this.usingSensor, 1);
                glUniform4f(this.sensorAreasPos, this.sensorAreas[0], this.sensorAreas[1], this.sensorAreas[2], this.sensorAreas[3]);
                break;
            }
        }
    }

    /**
     * Metodo che serve a resettare la draw di un oggetto di un specifico modello
     * attivato dallo funzione drawByModel()
     */
    private void resetByModel() {
        switch (this.modelName) {
            case CIRCLE_LIGHTS: {
                glUniform1i(this.usingLightD, 0);
                break;
            }
            case SENSOR_CIRCLE: {
                glUniform1i(this.usingSensor, 0);
                break;
            }
        }
    }
}

