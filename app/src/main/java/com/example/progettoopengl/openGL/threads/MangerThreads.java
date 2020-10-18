package com.example.progettoopengl.openGL.threads;

import java.util.Timer;

/**
 * Classe che serve per la gestione dei thread presenti nel progetto.
 */
public class MangerThreads {

    private SpeedThread speedThread;
    private Timer timerSpeedThread;

    private FluidThread fluidThread;
    private Timer timeFluidThread;

    private LightsThread lightsThread;
    private Timer timeLightsThread;

    private SensorThread sensorThread;
    private Timer timeSensorThread;

    /**
     * Costruttore della classe ManagerThreads
     */
    public MangerThreads() {
        //threads
        speedThread = new SpeedThread();
        fluidThread = new FluidThread();
        lightsThread = new LightsThread();
        sensorThread = new SensorThread();


        //timers
        timerSpeedThread = new Timer();
        timeFluidThread = new Timer();
        timeLightsThread = new Timer();
        timeSensorThread = new Timer();

        timerSpeedThread.scheduleAtFixedRate(speedThread, 0, 2 * 1000);
        timeFluidThread.scheduleAtFixedRate(fluidThread, 0, 1000);
        timeLightsThread.scheduleAtFixedRate(lightsThread, 0, 4 * 1000);
        timeSensorThread.scheduleAtFixedRate(sensorThread, 0, 4 * 1000);

    }

    /**
     * Start Threads.
     */
    public void startThreads() {
        speedThread.start();
        fluidThread.start();
        lightsThread.start();
        sensorThread.start();
    }

    /**
     * Pause Threads.
     */
    public void pauseThreads() {
        speedThread.stop();
        fluidThread.stop();
        lightsThread.stop();
        sensorThread.stop();
    }

    /**
     * Destroy Threads
     */
    public void destroyThreads() {
        speedThread.cancel();
        timerSpeedThread.purge();
        timerSpeedThread.cancel();
        fluidThread.cancel();
        timeFluidThread.purge();
        timeFluidThread.cancel();
        lightsThread.cancel();
        timeLightsThread.purge();
        timeLightsThread.cancel();
        sensorThread.cancel();
        timeSensorThread.purge();
        timeSensorThread.cancel();
    }


}
