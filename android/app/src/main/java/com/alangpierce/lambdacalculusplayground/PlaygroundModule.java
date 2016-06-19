package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.definition.DefinitionManagerImpl;
import com.alangpierce.lambdacalculusplayground.definition.UserDefinitionManager;
import com.alangpierce.lambdacalculusplayground.definition.UserDefinitionManagerImpl;
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
import com.alangpierce.lambdacalculusplayground.palette.PaletteDrawerManager;
import com.alangpierce.lambdacalculusplayground.palette.PaletteDrawerManagerImpl;
import com.alangpierce.lambdacalculusplayground.palette.PaletteView;
import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManager;
import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManagerImpl;
import com.alangpierce.lambdacalculusplayground.view.ExpressionCreatorImpl;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRendererImpl;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Module;
import dagger.Provides;

@Module
public class PlaygroundModule {
    private final Activity activity;
    private final AppState appState;

    // These are all children of the fragment.
    @Bind(R.id.above_palette_root) RelativeLayout abovePaletteRoot;
    @Bind(R.id.lambda_palette_drawer_root) DrawerLayout lambdaPaletteDrawerRoot;
    @Bind(R.id.definition_palette_drawer_root) DrawerLayout definitionPaletteDrawerRoot;
    @Bind(R.id.lambda_palette_scroll_view) ScrollView lambdaPaletteDrawer;
    @Bind(R.id.definition_palette_scroll_view) ScrollView definitionPaletteDrawer;
    @Bind(R.id.lambda_palette_linear_layout) LinearLayout lambdaPaletteLinearLayout;
    @Bind(R.id.definition_palette_linear_layout) LinearLayout definitionPaletteLinearLayout;
    @Bind(R.id.canvas_root) RelativeLayout canvasRoot;

    private PlaygroundModule(Activity activity, AppState appState) {
        this.activity = activity;
        this.appState = appState;
    }

    public static PlaygroundModule create(Activity activity, AppState appState, View root) {
        PlaygroundModule module = new PlaygroundModule(activity, appState);
        ButterKnife.bind(module, root);
        return module;
    }

    @Provides Activity provideActivity() {
        return activity;
    }

    @Provides Context provideContext(Activity activity) {
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

    @Provides @Singleton
    AppState provideAppState() {
        return appState;
    }

    @Provides @Singleton
    ReactNativeManager provideReactNativeManager() {
        return new ReactNativeManagerImpl(canvasRoot, activity);
    }

    @Provides @Singleton
    PaletteDrawerManager providePaletteDrawerManager(
            @Lambda PaletteView lambdaPaletteView, @Definition PaletteView definitionPaletteView) {
        return new PaletteDrawerManagerImpl(
                lambdaPaletteView, definitionPaletteView, lambdaPaletteDrawerRoot,
                lambdaPaletteDrawer, definitionPaletteDrawerRoot, definitionPaletteDrawer);
    }

    @Qualifier @interface Lambda {}
    @Qualifier @interface Definition {}
    @Provides
    @Lambda
    PaletteView provideLambdaPaletteView() {
        return new PaletteView(lambdaPaletteDrawerRoot, lambdaPaletteDrawer,
                lambdaPaletteLinearLayout);
    }

    @Provides
    @Definition
    PaletteView provideDefinitionPaletteView() {
        return new PaletteView(definitionPaletteDrawerRoot, definitionPaletteDrawer,
                definitionPaletteLinearLayout);
    }

    @Provides
    ExpressionCreator provideExpressionCreator(Context context, LayoutInflater layoutInflater,
            CanvasManager canvasManager, ReactNativeManager reactNativeManager,
            PointConverter pointConverter, AppState appState) {
        return new ExpressionCreatorImpl(context, layoutInflater, canvasManager, reactNativeManager,
                pointConverter, appState);
    }

    @Provides @Singleton
    PointConverter providePointConverter(@CanvasRoot RelativeLayout canvasRoot) {
        return new PointConverterImpl(canvasRoot);
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
    UserDefinitionManager provideUserDefinitionManager(AppState appState) {
        return new UserDefinitionManagerImpl(appState);
    }

    @Provides @Singleton
    DefinitionManager provideDefinitionManager(AppState appState) {
        return new DefinitionManagerImpl(appState);
    }

    @Provides
    ExpressionViewRenderer provideExpressionViewRenderer(
            Context context, LayoutInflater layoutInflater) {
        return new ExpressionViewRendererImpl(context, layoutInflater);
    }

    @Provides @Singleton
    CanvasManager provideTopLevelExpressionManager(
            AppState appState, ExpressionControllerFactoryFactory controllerFactoryFactory,
            PointConverter pointConverter, DefinitionManager definitionManager,
            @Lambda PaletteView lambdaPaletteView, @Definition PaletteView definitionPaletteView,
            PaletteDrawerManager drawerManager, UserDefinitionManager userDefinitionManager) {
        return new CanvasManagerImpl(
                appState, controllerFactoryFactory, pointConverter, definitionManager,
                lambdaPaletteView, definitionPaletteView, drawerManager, userDefinitionManager);
    }

    @Provides
    ExpressionControllerFactoryFactory provideExpressionControllerFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, DragManager dragManager,
            @CanvasRoot RelativeLayout canvasRoot, @AbovePaletteRoot RelativeLayout abovePaletteRoot,
            DefinitionManager definitionManager) {
        return ExpressionControllerFactoryImpl.createFactory(viewRenderer, dragObservableGenerator,
                pointConverter, dragManager, canvasRoot, abovePaletteRoot, definitionManager);
    }
}
