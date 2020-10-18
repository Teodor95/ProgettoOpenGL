package com.example.progettoopengl.openGL.threads;

import android.util.Log;

import com.example.progettoopengl.openGL.models.ModelUtilities;

import java.util.Random;
import java.util.TimerTask;

/**
 * Classe che serve a definire il thread delle spie.
 */
public class LightsThread extends TimerTask {
    private Object lock;
    private boolean isRunning;
    private int light;

    /**
     * Costruttore della classe.
     */
    public LightsThread() {
        this.isRunning = false;
        this.lock = new Object();
        this.light = 0;
    }

    /**
     * Metodo che serve a generare randomicamente un numero compreso tra 0 e 7
     */
    private void randomLight() {
        this.light = new Random().nextInt(7);
    }

    /**
     * Questo metodo serve per la routine che si verifica ogni volta che si attiva l'evento dell'orologio.
     */
    @Override
    public void run() {
        if (isRunning) {
            try {
                randomLight();
                ModelUtilities.getInstance().changeLight(this.light);
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
}


