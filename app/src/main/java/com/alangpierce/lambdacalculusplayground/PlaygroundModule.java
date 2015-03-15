package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGeneratorImpl;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManager;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManagerImpl;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSourceManagerImpl;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactoryImpl;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRendererImpl;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PlaygroundModule {
    private final Activity activity;
    private final RelativeLayout rootView;
    private final TopLevelExpressionState expressionState;

    public PlaygroundModule(Activity activity, RelativeLayout rootView,
            TopLevelExpressionState expressionState) {
        this.activity = activity;
        this.rootView = rootView;
        this.expressionState = expressionState;
    }

    @Provides Activity provideActivity() {
        return activity;
    }

    @Qualifier @interface RootView {}
    @Provides @RootView RelativeLayout provideRootView() {
        return rootView;
    }

    @Provides @Singleton
    TopLevelExpressionState provideTopLevelExpressionState() {
        return expressionState;
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
    DragManager provideDragSourceRegistry(@RootView RelativeLayout rootView) {
        return new DragSourceManagerImpl(rootView);
    }

    @Provides
    ExpressionViewRenderer provideExpressionViewRenderer(Activity activity) {
        return new ExpressionViewRendererImpl(activity);
    }

    @Provides
    TopLevelExpressionManager provideTopLevelExpressionManager(
            TopLevelExpressionState expressionState, @RootView RelativeLayout rootView,
            ExpressionControllerFactory controllerFactory) {
        return new TopLevelExpressionManagerImpl(expressionState, rootView, controllerFactory);
    }

    @Provides
    ExpressionControllerFactory provideExpressionControllerFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            DragManager dragManager) {
        return new ExpressionControllerFactoryImpl(viewRenderer, dragObservableGenerator,
                dragManager);
    }
}
