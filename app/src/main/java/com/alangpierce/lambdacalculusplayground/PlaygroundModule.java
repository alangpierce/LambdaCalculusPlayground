package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGeneratorImpl;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManager;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManagerImpl;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTargetRegistry;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTargetRegistryImpl;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactoryImpl;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionViewRendererImpl;

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
    DragObservableGenerator provideDragObservableGenerator(
            TouchObservableManager touchObservableManager) {
        return new DragObservableGeneratorImpl(touchObservableManager);
    }

    @Provides @Singleton
    TouchObservableManager provideTouchObservableManager() {
        return new TouchObservableManagerImpl();
    }

    @Provides @Singleton
    DropTargetRegistry provideDropTargetRegistry() {
        return new DropTargetRegistryImpl();
    }

    @Provides
    ExpressionViewRenderer provideExpressionViewRenderer(Activity activity) {
        return new ExpressionViewRendererImpl(activity);
    }

    @Provides
    ExpressionControllerFactoryFactory provideExpressionControllerFactoryFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            DropTargetRegistry dropTargetRegistry) {
        return ExpressionControllerFactoryImpl.createFactory(
                viewRenderer, dragObservableGenerator, dropTargetRegistry);
    }
}
