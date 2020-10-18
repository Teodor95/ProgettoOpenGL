package com.example.progettoopengl.openGL.threads;

import android.util.Log;

import com.example.progettoopengl.openGL.models.ModelUtilities;

import java.util.Random;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Classe che serve  definire il thread dello speed.
 */
public class SpeedThread extends TimerTask {

    private boolean isRunning;
    private Object lock;
    private int speed;
    private int offset;
    boolean isSetInitialSpeed;

    /**
     * Costruttore della classe.
     */
    public SpeedThread() {
        this.isRunning = false;
        this.lock = new Object();
        this.speed = getInitialSpeed();
        this.offset = -30;
        this.isSetInitialSpeed = false;
    }


    /**
     * Metodo privato che permette di ritornare la speed random iniziale
     *
     * @return valore iniziale speed
     */
    private int getInitialSpeed() {
        Vector<Integer> k = new Vector();
        for (int i = 10; i < 220; i += 10)
            k.add(i);

        Random rand = new Random();
        int n = rand.nextInt(k.size());
        return k.get(n);
    }

    /**
     * Metodo che serve per aggiornare lo speed casualmente.
     *
     * @return il gap da aggiornare
     */
    private int updateSpeed() {
        int min = -10;
        int max = 10;
        return (int) Math.round((Math.random() * ((max - min) + 1)) + min);
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
                if (!this.isSetInitialSpeed) {
                    ModelUtilities.getInstance().modifySpeedNeedle(this.speed + this.offset);
                    this.isSetInitialSpeed = true;
                } else {
                    ModelUtilities.getInstance().modifySpeedNeedle(this.speed + this.updateSpeed() + this.offset);
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
