package com.alangpierce.lambdacalculusplayground;

import android.support.v4.widget.DrawerLayout;

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.definitioncontroller.DefinitionController;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.palette.PaletteController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteLambdaController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteView;
import com.alangpierce.lambdacalculusplayground.pan.PanManager;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

public class TopLevelExpressionManagerImpl implements TopLevelExpressionManager {
    private final TopLevelExpressionState expressionState;
    private final ExpressionControllerFactoryFactory controllerFactoryFactory;
    private final DragManager dragManager;
    private final PointConverter pointConverter;
    private final DrawerLayout drawerRoot;
    private final PanManager panManager;
    private final DefinitionManager definitionManager;

    private final Map<String, DefinitionController> definitionControllers = new HashMap<>();

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            DragManager dragManager, PointConverter pointConverter, DrawerLayout drawerRoot,
            PanManager panManager, DefinitionManager definitionManager) {
        this.expressionState = expressionState;
        this.controllerFactoryFactory = controllerFactoryFactory;
        this.dragManager = dragManager;
        this.pointConverter = pointConverter;
        this.drawerRoot = drawerRoot;
        this.panManager = panManager;
        this.definitionManager = definitionManager;
    }

    @Override
    public void renderInitialData() {
        // Note that we need to initialize the pan manager first so that the rest of the code will
        // correctly get the pan offset.
        panManager.init(expressionState.getPanOffset());
        panManager.registerPanListener(
                () -> expressionState.setPanOffset(panManager.getPanOffset()));

        for (Entry<Integer, ScreenExpression> entry : expressionState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            renderTopLevelExpression(exprId, screenExpression, false /* placeAbovePalette */);
        }
        for (ScreenDefinition definition : expressionState.definitions()) {
            renderDefinition(definition);
        }
        renderPalette();
    }

    private void renderPalette() {
        PaletteView view = PaletteView.render(drawerRoot);
        PaletteController controller = new PaletteController(view);
        controller.registerCallbacks(dragManager);

        for (String varName : ImmutableList.of("x", "y", "t", "f", "b", "s", "z", "n", "m")) {
            PaletteLambdaController lambdaController =
                    controllerFactoryFactory.create(this).createPaletteLambdaController(varName);
            view.addChild(lambdaController.getView());
        }
    }

    @Override
    public TopLevelExpressionController createNewExpression(
            UserExpression expression, ScreenPoint screenPos, boolean placeAbovePalette) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        ScreenExpression screenExpression = ScreenExpression.create(expression, canvasPos);
        int exprId = expressionState.addScreenExpression(screenExpression);
        return renderTopLevelExpression(exprId, screenExpression, placeAbovePalette);
    }

    @Override
    public TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, ScreenPoint screenPos) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        ScreenExpression screenExpression = ScreenExpression.create(
                expression.getExpression(), canvasPos);
        int exprId = expressionState.addScreenExpression(screenExpression);
        TopLevelExpressionController controller = controllerFactoryFactory.create(this)
                .wrapInTopLevelController(
                        expression, screenExpression, false /* placeAbovePalette */);
        registerTopLevelExpression(exprId, controller, canvasPos);
        return controller;
    }

    /**
     * Given a new expression, create a view for it and hook up all necessary callbacks.
     */
    private TopLevelExpressionController renderTopLevelExpression(
            int exprId, ScreenExpression screenExpression, boolean placeAbovePalette) {
        TopLevelExpressionController controller =
                controllerFactoryFactory.create(this).createTopLevelController(
                        screenExpression, placeAbovePalette);
        registerTopLevelExpression(exprId, controller, screenExpression.canvasPos());
        return controller;
    }

    private void registerTopLevelExpression(
            int exprId, TopLevelExpressionController controller, CanvasPoint canvasPos) {
        panManager.registerPanListener(controller);
        controller.setOnChangeCallback(newController -> {
            if (newController != null) {
                expressionState.modifyExpression(
                        exprId, newController.getScreenExpression());
            } else {
                expressionState.deleteExpression(exprId);
                panManager.unregisterPanListener(controller);
            }
        });
        controller.getView().attachToRoot(canvasPos);
    }

    @Override
    public boolean placeDefinition(String defName, DrawableAreaPoint drawableAreaPoint) {
        DefinitionController existingController = definitionControllers.get(defName);
        if (existingController != null) {
            ScreenPoint screenPoint = pointConverter.toScreenPoint(drawableAreaPoint);
            existingController.handlePositionChange(screenPoint);
            return true;
        } else {
            @Nullable UserExpression existingDefinition =
                    definitionManager.getUserDefinition(defName);
            CanvasPoint canvasPoint = pointConverter.toCanvasPoint(drawableAreaPoint);
            // Either make a new blank definition or use the existing one.
            ScreenDefinition definition = ScreenDefinition.create(
                    defName, existingDefinition, canvasPoint);
            expressionState.setDefinition(definition);
            renderDefinition(definition);
            return existingDefinition != null;
        }
    }

    private DefinitionController renderDefinition(ScreenDefinition screenDefinition) {
        DefinitionController controller =
                controllerFactoryFactory.create(this).createDefinitionController(screenDefinition);
        panManager.registerPanListener(controller);
        definitionControllers.put(screenDefinition.defName(), controller);
        controller.setOnChangeCallback(newController -> {
            if (newController != null) {
                ScreenDefinition newScreenDefinition = newController.getScreenDefinition();
                expressionState.setDefinition(newScreenDefinition);
                definitionManager.updateDefinition(newScreenDefinition.defName(), newScreenDefinition.expr());
            } else {
                expressionState.deleteDefinition(screenDefinition.defName());
                panManager.unregisterPanListener(controller);
                definitionControllers.remove(screenDefinition.defName());
            }
        });
        return controller;
    }
}
