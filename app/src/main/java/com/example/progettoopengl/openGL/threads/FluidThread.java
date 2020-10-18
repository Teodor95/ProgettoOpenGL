package com.example.progettoopengl.openGL.threads;

import android.util.Log;

import com.example.progettoopengl.openGL.models.ModelUtilities;

import java.util.TimerTask;

/**
 * Classe che serve a definire il thread del fluid.
 */
public class FluidThread extends TimerTask {
    private boolean isRunning;
    private Object lock;
    private int fluid;

    /**
     * Costruttore della classe
     */
    public FluidThread() {
        this.isRunning = false;
        this.lock = new Object();
        this.fluid = 140;

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
                if (this.fluid >= 40) {
                    ModelUtilities.getInstance().modifyFluidLevel((float) this.fluid);
                    this.fluid -= 1;
                }
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
