package com.example.progettoopengl.openGL.threads;

import android.util.Log;

import com.example.progettoopengl.openGL.models.ModelUtilities;

import java.util.Random;
import java.util.TimerTask;

/**
 * Classe che serve  definire il thread dei sensori.
 */
public class SensorThread extends TimerTask {

    private boolean isRunning;
    private Object lock;
    private float[] areas;

    /**
     * Costruttore della classe.
     */
    public SensorThread() {
        this.isRunning = false;
        this.lock = new Object();
        this.areas = new float[4];
    }

    /**
     * Metodo che serve a genereare un array con 4 elementi generati randomicamente.
     */
    private void updateAreas() {
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            this.areas[i] = rand.nextFloat();
        }

    }


    /**
     * Metodo che serve a startare il Thread.
     */
    public void start() {
        this.isRunning = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * metodo che serve a mettere stop sul Thread.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * Questo metodo serve per la routine che si verifica ogni volta che si attiva l'evento dell'orologio.
     */
    @Override
    public void run() {
        if (isRunning) {
            try {
                updateAreas();
                ModelUtilities.getInstance().changeSensorAreas(this.areas);
            } catch (Exception e) {
                Log.v("THREAD", "Il Thread non è inizializzato.");
            }
        } else {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Log.v("THREAD", "Error Lock Wait.");
                }
            }
        }

    }
}
