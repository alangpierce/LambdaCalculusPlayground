package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.definition.DefinitionManagerImpl;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGeneratorImpl;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManager;
import com.alangpierce.lambdacalculusplayground.drag.TouchObservableManagerImpl;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManagerImpl;
import com.alangpierce.lambdacalculusplayground.evaluator.ExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.evaluator.OptimizedExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactoryImpl;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverterImpl;
import com.alangpierce.lambdacalculusplayground.pan.PanManager;
import com.alangpierce.lambdacalculusplayground.pan.PanManagerImpl;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressionEvaluatorImpl;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRendererImpl;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PlaygroundModule {
    private final Activity activity;
    private final RelativeLayout canvasRoot;
    private final RelativeLayout abovePaletteRoot;
    private final DrawerLayout drawerRoot;
    private final TopLevelExpressionState expressionState;

    public PlaygroundModule(Activity activity, RelativeLayout canvasRoot,
                            RelativeLayout abovePaletteRoot, DrawerLayout drawerRoot,
                            TopLevelExpressionState expressionState) {
        this.activity = activity;
        this.canvasRoot = canvasRoot;
        this.abovePaletteRoot = abovePaletteRoot;
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

    @Qualifier @interface CanvasRoot {}
    @Provides @CanvasRoot
    RelativeLayout provideCanvasRoot() {
        return canvasRoot;
    }

    @Qualifier @interface AbovePaletteRoot {}
    @Provides @AbovePaletteRoot
    RelativeLayout provideAbovePaletteRoot() {
        return abovePaletteRoot;
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
    PanManager providePanManager(@CanvasRoot RelativeLayout canvasRoot,
            DragObservableGenerator dragObservableGenerator) {
        return new PanManagerImpl(canvasRoot, dragObservableGenerator);
    }

    @Provides @Singleton
    PointConverter providePointConverter(@CanvasRoot RelativeLayout canvasRoot, PanManager panManager) {
        return new PointConverterImpl(canvasRoot, panManager);
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
    DragManager provideDragManager() {
        return new DragManagerImpl();
    }

    @Provides @Singleton
    DefinitionManager provideDefinitionManager() {
        return DefinitionManagerImpl.createWithDefaults();
    }

    @Provides @Singleton
    ExpressionEvaluator provideExpressionEvaluator() {
        return new OptimizedExpressionEvaluator();
    }

    @Provides @Singleton
    UserExpressionEvaluator provideUserExpressionEvaluator(
            DefinitionManager definitionManager, ExpressionEvaluator expressionEvaluator) {
        return new UserExpressionEvaluatorImpl(definitionManager, expressionEvaluator);
    }

    @Provides
    ExpressionViewRenderer provideExpressionViewRenderer(
            Activity activity, LayoutInflater layoutInflater) {
        return new ExpressionViewRendererImpl(activity, layoutInflater);
    }

    @Provides
    TopLevelExpressionManager provideTopLevelExpressionManager(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            DragManager dragManager, PointConverter pointConverter, PanManager panManager,
            @DrawerRoot DrawerLayout drawerRoot, DefinitionManager definitionManager) {
        return new TopLevelExpressionManagerImpl(
                expressionState, controllerFactoryFactory, dragManager, pointConverter, drawerRoot,
                panManager, definitionManager);
    }

    @Provides
    ExpressionControllerFactoryFactory provideExpressionControllerFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, DragManager dragManager,
            UserExpressionEvaluator userExpressionEvaluator, @CanvasRoot RelativeLayout canvasRoot,
            @AbovePaletteRoot RelativeLayout abovePaletteRoot) {
        return ExpressionControllerFactoryImpl.createFactory(viewRenderer, dragObservableGenerator,
                pointConverter, dragManager, userExpressionEvaluator, canvasRoot, abovePaletteRoot);
    }
}
