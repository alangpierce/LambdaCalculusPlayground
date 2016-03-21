package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGeneratorImpl;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManager;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManagerImpl;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManagerImpl;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactoryImpl;
import com.alangpierce.lambdacalculusplayground.pan.PanManager;
import com.alangpierce.lambdacalculusplayground.pan.PanManagerImpl;
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
    PanManager providePanManager(@RootView  RelativeLayout rootView,
            DragObservableGenerator dragObservableGenerator) {
        return new PanManagerImpl(rootView, dragObservableGenerator);
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
    DragManager provideDragSourceRegistry() {
        return new DragManagerImpl();
    }

    @Provides
    ExpressionViewRenderer provideExpressionViewRenderer(Activity activity) {
        return new ExpressionViewRendererImpl(activity);
    }

    @Provides
    TopLevelExpressionManager provideTopLevelExpressionManager(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            DragManager dragManager, PanManager panManager, Activity activity,
            @RootView RelativeLayout rootView) {
        return new TopLevelExpressionManagerImpl(
                expressionState, controllerFactoryFactory, dragManager, rootView, panManager,
                activity);
    }

    @Provides
    ExpressionControllerFactoryFactory provideExpressionControllerFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            DragManager dragManager, @RootView RelativeLayout rootView) {
        return ExpressionControllerFactoryImpl.createFactory(viewRenderer, dragObservableGenerator,
                dragManager, rootView);
    }
}
