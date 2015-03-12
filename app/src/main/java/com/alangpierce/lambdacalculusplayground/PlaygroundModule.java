package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;

import com.alangpierce.lambdacalculusplayground.ExpressionViewGenerator.ExpressionViewGeneratorFactory;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGeneratorImpl;
import com.alangpierce.lambdacalculusplayground.drag.DragTracker;
import com.alangpierce.lambdacalculusplayground.drag.DragTrackerImpl;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManager;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManagerImpl;

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

    @Provides @Singleton
    DragTracker provideDragTracker(DragObservableGenerator dragObservableGenerator) {
        return new DragTrackerImpl(dragObservableGenerator);
    }

    @Provides @Singleton
    DragObservableGenerator provideDragObservableGenerator(
            TouchObservableManager touchObservableManager) {
        return new DragObservableGeneratorImpl(touchObservableManager);
    }

    @Provides @Singleton
    TouchObservableManager provideTouchObservableManager() {
        return new TouchObservableManagerImpl();
    }

    @Provides ExpressionViewGeneratorFactory provideExpressionViewGeneratorFactory(
            Activity activity, DragTracker dragTracker) {
        return ExpressionViewGeneratorImpl.createFactory(activity, dragTracker);
    }
}
