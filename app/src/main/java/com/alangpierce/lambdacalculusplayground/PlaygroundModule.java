package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PlaygroundModule {
    private final Activity activity;

    public PlaygroundModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    Activity provideActivity() {
        return activity;
    }


    @Provides @Singleton DragTracker provideDragTracker() {
        return new DragTrackerImpl();
    }

    @Provides ExpressionViewGenerator provideExpressionViewGenerator(
            Activity activity, DragTracker dragTracker) {
        return new ExpressionViewGeneratorImpl(activity, dragTracker);
    }
}
