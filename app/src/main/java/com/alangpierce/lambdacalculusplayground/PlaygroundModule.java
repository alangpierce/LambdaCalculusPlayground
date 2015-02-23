package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;

import com.alangpierce.lambdacalculusplayground.ExpressionViewGenerator.ExpressionViewGeneratorFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PlaygroundModule {
    private final Activity activity;

    public PlaygroundModule(Activity activity) {
        this.activity = activity;
    }

    @Provides Activity provideActivity() {
        return activity;
    }

    @Provides @Singleton DragTracker provideDragTracker() {
        return new DragTrackerImpl();
    }

    @Provides ExpressionViewGeneratorFactory provideExpressionViewGeneratorFactory(
            Activity activity, DragTracker dragTracker) {
        return ExpressionViewGeneratorImpl.createFactory(activity, dragTracker);
    }
}
