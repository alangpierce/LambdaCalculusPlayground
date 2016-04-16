package com.alangpierce.lambdacalculusplayground;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class PlaygroundApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
