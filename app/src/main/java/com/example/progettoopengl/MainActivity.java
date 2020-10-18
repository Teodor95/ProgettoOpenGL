package com.example.progettoopengl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.example.progettoopengl.openGL.BasicRenderer;
import com.example.progettoopengl.openGL.MainRenderer;
import com.example.progettoopengl.openGL.models.ModelUtilities;
import com.example.progettoopengl.openGL.threads.MangerThreads;

public class MainActivity extends Activity implements View.OnTouchListener {

    // Gesture Variables
    private DisplayMetrics displayM;
    private int numberOfTaps = 0;
    private long lastTapTimeMs = 0;
    private long touchDownMs = 0;
    private Handler handler = new Handler();

    //Threads
    MangerThreads threads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Optional: for full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags
                (WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);


        displayM = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayM);

        //get a reference to the Activity Manager (AM)
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //from the AM we get an object with our mobile device info

        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        int supported = 1;
        if (configurationInfo.reqGlEsVersion >= 0x30000)
            supported = 3;
        else if (configurationInfo.reqGlEsVersion >= 0x20000)
            supported = 2;

        Log.v("TAG", "Opengl ES supported >= " +
                supported + " (" + Integer.toHexString(configurationInfo.reqGlEsVersion) + " " +
                configurationInfo.getGlEsVersion() + ")");


        GLSurfaceView surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(supported);
        surface.setPreserveEGLContextOnPause(true);
        GLSurfaceView.Renderer renderer = new BasicRenderer(0.45f, 0.32f, 0.13f);

        renderer = new MainRenderer();
        setContentView(surface);
        ((MainRenderer) renderer).setContextAndSurface(this, surface);

        surface.setRenderer(renderer);
        threads = new MangerThreads();
        surface.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownMs = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacksAndMessages(null);
                if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                    numberOfTaps = 0;
                    lastTapTimeMs = 0;
                    break;
                }
                if (numberOfTaps > 0
                        && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                    numberOfTaps += 1;
                } else {
                    numberOfTaps = 1;
                }
                lastTapTimeMs = System.currentTimeMillis();

                if (numberOfTaps == 1) {
                    if (ModelUtilities.getInstance().getActualView() == com.example.progettoopengl.openGL.Utilities.View.DEFAULT) {
                        if (event.getY() < (displayM.heightPixels * 2 / 3))
                            ModelUtilities.getInstance().setActualView(com.example.progettoopengl.openGL.Utilities.View.DASH);
                        else
                            ModelUtilities.getInstance().setActualView(com.example.progettoopengl.openGL.Utilities.View.PROXIMITY_SENSORS);
                    }
                } else if (numberOfTaps == 2) {
                    if (ModelUtilities.getInstance().getActualView() == com.example.progettoopengl.openGL.Utilities.View.PROXIMITY_SENSORS ||
                            ModelUtilities.getInstance().getActualView() == com.example.progettoopengl.openGL.Utilities.View.DASH) {
                        ModelUtilities.getInstance().setActualView(com.example.progettoopengl.openGL.Utilities.View.DEFAULT);
                    }
                }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        threads.startThreads();
    }

    @Override
    protected void onPause() {
        super.onPause();
        threads.pauseThreads();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threads.destroyThreads();
    }
}

