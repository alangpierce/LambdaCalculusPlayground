package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGeneratorImpl;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManager;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManagerImpl;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManagerImpl;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactoryImpl;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverterImpl;
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
    private final DrawerLayout drawerRoot;
    private final TopLevelExpressionState expressionState;

    public PlaygroundModule(Activity activity, RelativeLayout rootView, DrawerLayout drawerRoot,
            TopLevelExpressionState expressionState) {
        this.activity = activity;
        this.rootView = rootView;
        this.drawerRoot = drawerRoot;
        this.expressionState = expressionState;
    }

    @Provides Activity provideActivity() {
        return activity;
    }

    @Provides
    LayoutInflater provideLayoutInflater(Activity activity) {
        return activity.getLayoutInflater();
    }

    @Qualifier @interface RootView {}
    @Provides @RootView RelativeLayout provideRootView() {
        return rootView;
    }

    @Qualifier @interface DrawerRoot {}
    @Provides @DrawerRoot
    DrawerLayout provideDrawerRoot() {
        return drawerRoot;
    }

    @Provides @Singleton
    TopLevelExpressionState provideTopLevelExpressionState() {
        return expressionState;
    }

    @Provides @Singleton
    PanManager providePanManager(@RootView RelativeLayout rootView,
            DragObservableGenerator dragObservableGenerator) {
        return new PanManagerImpl(rootView, dragObservableGenerator);
    }

    @Provides @Singleton
    PointConverter providePointConverter(@RootView RelativeLayout rootView, PanManager panManager) {
        return new PointConverterImpl(rootView, panManager);
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
    ExpressionViewRenderer provideExpressionViewRenderer(
            Activity activity, @RootView RelativeLayout rootView, LayoutInflater layoutInflater) {
        return new ExpressionViewRendererImpl(activity, rootView, layoutInflater);
    }

    @Provides
    TopLevelExpressionManager provideTopLevelExpressionManager(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            DragManager dragManager, PointConverter pointConverter, PanManager panManager,
            @DrawerRoot DrawerLayout drawerRoot) {
        return new TopLevelExpressionManagerImpl(
                expressionState, controllerFactoryFactory, dragManager, pointConverter, drawerRoot,
                panManager);
    }

    @Provides
    ExpressionControllerFactoryFactory provideExpressionControllerFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, DragManager dragManager,
            @RootView RelativeLayout rootView) {
        return ExpressionControllerFactoryImpl.createFactory(viewRenderer, dragObservableGenerator,
                pointConverter, dragManager, rootView);
    }
}
