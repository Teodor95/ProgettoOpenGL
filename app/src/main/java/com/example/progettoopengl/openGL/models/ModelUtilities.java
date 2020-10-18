package com.example.progettoopengl.openGL.models;

import android.content.Context;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.progettoopengl.openGL.Utilities.Models;
import com.example.progettoopengl.openGL.Utilities.View;

import java.util.ArrayList;

/**
 * Classe core che serve per la gesione dei modelli del progetto.
 */
public class ModelUtilities {

    private static ModelUtilities modelUtilInstance;
    private ArrayList<GenericModel> projectModels;
    private View actualView;

    /**
     * Costruttore della classe ModelUtilities
     */
    private ModelUtilities() {
        this.projectModels = new ArrayList<>();
        this.actualView = View.DEFAULT;
    }

    /**
     * Metodo Statico per ritornare il sigleton della classe MainModel
     *
     * @return il singleton della classe MainModel.
     */
    public synchronized static ModelUtilities getInstance() {
        if (modelUtilInstance == null)
            modelUtilInstance = new ModelUtilities();
        return modelUtilInstance;
    }

    /**
     * Metodo che serve a inziare le classi dei modelli presenti nel progetto.
     */
    private void initALLModels() {
        for (Models m : Models.values()) {
            projectModels.add(getDataModel(m));
            setModelTranslations(m);
        }

    }

    /**
     * Metodo che serve a configurare i modelli presenti nel progetto in base alla view.
     */
    private void initModelsByView() {
        for (Models m : Models.values())
            setModelTranslations(m);
    }

    /**
     * Metodo che serve a inizializzare
     *
     * @param shaderHandle la compilazione del programma GL.
     * @param context      il contesto dell'ambiente dell'applicazione android.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void initModelOnSurfCreate(int shaderHandle, Context context) {
        initALLModels();
        for (int i = 0; i < this.projectModels.size(); i++) {
            projectModels.get(i).setModel(shaderHandle, context);
        }
    }

    /**
     * Metodo che serve per il draw dei modelli.
     *
     * @param projM         matrice di projection.
     * @param viewM         matrice view.
     * @param umodelM       glGetUniformLocation di modelMatrix.
     * @param MVPloc        glGetUniformLocation di MVP.
     * @param uInverseModel glGetUniformLocation di inverseModel.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void drawAllModels(float[] projM, float[] viewM, int umodelM, int MVPloc, int uInverseModel) {
        for (int i = 0; i < projectModels.size(); i++) {
            switch (actualView) {
                case DEFAULT: {
                    projectModels.get(i).textureUpdate();
                    Matrix.multiplyMM(projectModels.get(i).getTempM(), 0, projM, 0, viewM, 0);
                    projectModels.get(i).draw(umodelM, MVPloc, uInverseModel);
                    break;
                }
                case DASH: {
                    if (projectModels.get(i).getView() == View.DASH) {
                        projectModels.get(i).textureUpdate();
                        Matrix.multiplyMM(projectModels.get(i).getTempM(), 0, projM, 0, viewM, 0);
                        projectModels.get(i).draw(umodelM, MVPloc, uInverseModel);
                    }
                    break;
                }
                case PROXIMITY_SENSORS: {
                    if (projectModels.get(i).getView() == View.PROXIMITY_SENSORS) {
                        projectModels.get(i).textureUpdate();
                        Matrix.multiplyMM(projectModels.get(i).getTempM(), 0, projM, 0, viewM, 0);
                        projectModels.get(i).draw(umodelM, MVPloc, uInverseModel);
                    }
                    break;
                }

            }

        }
    }

    /**
     * Metodo che serve a ritornare la classe contente un specifico modello.
     *
     * @param m il modello specifico.
     * @return la classe contentente il modello. Se non c'è una classe per il modello, ritorna null.
     */
    private GenericModel getModel(Models m) {
        for (GenericModel k : projectModels) {
            if (k.getModelName().name().equals(m.name())) {
                return k;
            }
        }
        return null;
    }

    /**
     * Metodo che serve ad aggiornare la velocità sul cruscotto.
     *
     * @param speed la velocità da aggiornare.
     */
    public void modifySpeedNeedle(float speed) {
        if (getModel(Models.NEEDLE_SPEED) == null) {
            Log.v("NULL", "Models.NEEDLE_SPEED is NULL, exit");
            System.exit(-1);
        } else {
            float[] rotation = getModel(Models.NEEDLE_SPEED).getRotation();
            getModel(Models.NEEDLE_SPEED).changeRotation(speed, rotation[1], rotation[2]);
        }
    }

    /**
     * Metodo che serve per l'aggiornamento del carburanìte sul cruscotto.
     *
     * @param fluidLevel il livello di carburante da aggiorare.
     */
    public void modifyFluidLevel(float fluidLevel) {
        if (getModel(Models.NEEDLE_FLUID) == null) {
            Log.v("NULL", "Models.NEEDLE_FLUID is NULL, exit");
            System.exit(-1);
        } else {
            float[] rotation = getModel(Models.NEEDLE_FLUID).getRotation();
            getModel(Models.NEEDLE_FLUID).changeRotation(fluidLevel, rotation[1], rotation[2]);
        }
    }

    /**
     * Metodo che serve ad aggiornare le luci dei sensori sul cruscotto.
     *
     * @param light il sensore randomico da accendere.
     */
    public void changeLight(int light) {
        if (getModel(Models.CIRCLE_LIGHTS) == null) {
            Log.v("NULL", "Models.CIRCLE_LIGHTS is NULL, exit");
            System.exit(-1);
        } else
            getModel(Models.CIRCLE_LIGHTS).updateLightDash(light);
    }

    /**
     * Tale metodo serve per l'aggiornamento dei sensori della macchina.
     *
     * @param areas float contentente 4 elementi che servono per impostare i sensori.
     */
    public void changeSensorAreas(float[] areas) {
        if (getModel(Models.SENSOR_CIRCLE) == null) {
            Log.v("NULL", "Models.SENSOR_CIRCLE is NULL, exit");
            System.exit(-1);
        } else
            getModel(Models.SENSOR_CIRCLE).setSensorAreas(areas);
    }

    /**
     * Metodo che ritorna la view attuale.
     *
     * @return la view attuale.
     */
    public View getActualView() {
        return this.actualView;
    }

    /**
     * Metodo che imposta la view attuale e in base alla view imposta i modelli.
     *
     * @param view la view da impostare.
     */
    public void setActualView(View view) {
        this.actualView = view;
        initModelsByView();
    }

    /**
     * Metodo che serve ad impostare le configurazioni dei modelli del progetto.
     *
     * @param m il modello da impostare.
     * @return ritorna la classe del modello con le configurazioni.
     */
    private GenericModel getDataModel(Models m) {
        float[] scaling;
        float[] rotation;
        switch (m) {
            case MAIN_DASHBOARD: {
                rotation = new float[]{180f, 270f, 0f};
                scaling = new float[]{0.8f, 0.8f, 0.8f};
                return new GenericModel(View.DASH, Models.MAIN_DASHBOARD,
                        "dash.ply", rotation, scaling, "purple");
            }
            case CIRCLE_SPEED: {
                rotation = new float[]{210f, 90f, 0f};
                scaling = new float[]{1f, 1f, 1f};
                return new GenericModel(View.DASH, Models.CIRCLE_SPEED,
                        "circle.ply", rotation, scaling, "speed");
            }
            case CIRCLE_FLUID: {
                rotation = new float[]{210f, 90f, 0f};
                scaling = new float[]{1f, 1f, 1f};
                return new GenericModel(View.DASH, Models.CIRCLE_FLUID,
                        "circle.ply", rotation, scaling, "fluidlevel");
            }
            case CIRCLE_LIGHTS: {
                rotation = new float[]{210f, 90f, 0f};
                scaling = new float[]{0.9f, 0.9f, 0.9f};
                return new GenericModel(View.DASH, Models.CIRCLE_LIGHTS,
                        "circle.ply", rotation, scaling, "spie");
            }
            case NEEDLE_SPEED: {
                ;
                rotation = new float[]{-30f, 90f, 0f};
                scaling = new float[]{0.07f, 0.07f, 0.07f};
                return new GenericModel(View.DASH, Models.NEEDLE_SPEED,
                        "needle.ply", rotation, scaling, "red");
            }
            case NEEDLE_FLUID: {
                rotation = new float[]{140f, 90f, 0f};
                scaling = new float[]{0.07f, 0.07f, 0.07f};
                return new GenericModel(View.DASH, Models.NEEDLE_FLUID,
                        "needle.ply", rotation, scaling, "red");
            }
            case SENSOR_CAR: {
                rotation = new float[]{15f, 60f, 40f};
                scaling = new float[]{0.3f, 0.3f, 0.3f};
                return new GenericModel(View.PROXIMITY_SENSORS, Models.SENSOR_CAR,
                        "car.ply", rotation, scaling, "grigio");
            }
            case SENSOR_CIRCLE: {
                rotation = new float[]{45f, 150f, 0f};
                scaling = new float[]{2f, 2f, 2f};
                return new GenericModel(View.PROXIMITY_SENSORS, Models.SENSOR_CIRCLE,
                        "circle.ply", rotation, scaling, "white");
            }
            default:
                return null;

        }
    }

    /**
     * Metodo che serve a impostare la traslazione in base al modello e alla view.
     *
     * @param m il modello da traslare.
     */
    private void setModelTranslations(Models m) {
        if (getModel(m) == null) {
            Log.v("NULL", "Models is NULL, exit");
            System.exit(-1);
        }
        switch (actualView) {
            case DEFAULT: {
                switch (m) {
                    case MAIN_DASHBOARD: {
                        getModel(m).changeTranslation(-0.1f, 0.4f, 0f);
                        break;
                    }
                    case CIRCLE_SPEED: {
                        getModel(m).changeTranslation(-0.050f, 0.330f, -0.220f);
                        break;
                    }
                    case CIRCLE_FLUID: {
                        getModel(m).changeTranslation(-0.15f, 0.58f, -0.220f);
                        break;
                    }
                    case CIRCLE_LIGHTS: {
                        getModel(m).changeTranslation(-0.15f, 0.09f, -0.220f);
                        break;
                    }
                    case NEEDLE_SPEED: {
                        getModel(m).changeTranslation(-0.045f, 0.27f, 0.20f);
                        break;
                    }
                    case NEEDLE_FLUID: {
                        getModel(m).changeTranslation(-0.15f, 0.47f, 0.20f);
                        break;
                    }
                    case SENSOR_CAR: {
                        getModel(m).changeTranslation(-0.25f, -0.45f, 0f);
                        break;
                    }
                    case SENSOR_CIRCLE: {
                        getModel(m).changeTranslation(-0.28f, -0.50f, -0.20f);
                        break;
                    }
                }
                break;
            }
            case DASH: {
                switch (m) {
                    case MAIN_DASHBOARD: {
                        getModel(m).changeTranslation(0f, 0.1f, 1f);
                        break;
                    }
                    case CIRCLE_SPEED: {
                        getModel(m).changeTranslation(0.055f, 0.015f, 0.7f);
                        break;
                    }
                    case CIRCLE_FLUID: {
                        getModel(m).changeTranslation(-0.045f, 0.285f, 0.7f);
                        break;
                    }
                    case CIRCLE_LIGHTS: {
                        getModel(m).changeTranslation(-0.045f, -0.227f, 0.8f);
                        break;
                    }
                    case NEEDLE_SPEED: {
                        getModel(m).changeTranslation(0.035f, 0.01f, 0.95f);
                        break;
                    }
                    case NEEDLE_FLUID: {
                        getModel(m).changeTranslation(-0.065f, 0.225f, 0.95f);
                        break;
                    }
                }
            }
            case PROXIMITY_SENSORS: {
                switch (m) {
                    case SENSOR_CAR: {
                        getModel(m).changeTranslation(-0f, 0.015f, 1f);
                        break;
                    }
                    case SENSOR_CIRCLE: {
                        getModel(m).changeTranslation(0f, 0.0f, 0.8f);
                        break;
                    }
                }
                break;
            }
        }

    }

}
