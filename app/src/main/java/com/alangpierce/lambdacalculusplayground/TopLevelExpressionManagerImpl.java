package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.definitioncontroller.DefinitionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.palette.PaletteLambdaController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteReferenceController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteView;
import com.alangpierce.lambdacalculusplayground.pan.PanManager;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

public class TopLevelExpressionManagerImpl implements TopLevelExpressionManager {
    private final TopLevelExpressionState expressionState;
    private final ExpressionControllerFactoryFactory controllerFactoryFactory;
    private final PointConverter pointConverter;
    private final PanManager panManager;
    private final DefinitionManager definitionManager;
    private final PaletteView lambdaPaletteView;
    private final PaletteView definitionPaletteView;

    private final Set<TopLevelExpressionController> expressionControllers = new HashSet<>();
    private final Map<String, DefinitionController> definitionControllers = new HashMap<>();

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            PointConverter pointConverter,
            PanManager panManager,
            DefinitionManager definitionManager,
            PaletteView lambdaPaletteView,
            PaletteView definitionPaletteView) {
        this.expressionState = expressionState;
        this.controllerFactoryFactory = controllerFactoryFactory;
        this.pointConverter = pointConverter;
        this.panManager = panManager;
        this.definitionManager = definitionManager;
        this.lambdaPaletteView = lambdaPaletteView;
        this.definitionPaletteView = definitionPaletteView;
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
        renderPalettes();
    }

    private void renderPalettes() {
        for (String varName : ImmutableList.of("x", "y", "t", "f", "b", "s", "z", "n", "m")) {
            PaletteLambdaController lambdaController =
                    controllerFactoryFactory.create(this).createPaletteLambdaController(varName);
            lambdaPaletteView.addChild(lambdaController.getView().getNativeView());
        }

        List<String> definitionNames =
                Ordering.natural().sortedCopy(definitionManager.getDefinitionNames());

        for (String defName : definitionNames) {
            PaletteReferenceController referenceController =
                    controllerFactoryFactory.create(this).createPaletteReferenceController(defName);
            definitionPaletteView.addChild(referenceController.getView().getNativeView());
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
        expressionControllers.add(controller);
        panManager.registerPanListener(controller);
        controller.setOnChangeCallback(newController -> {
            if (newController != null) {
                expressionState.modifyExpression(
                        exprId, newController.getScreenExpression());
            } else {
                expressionState.deleteExpression(exprId);
                panManager.unregisterPanListener(controller);
                expressionControllers.remove(controller);
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
            boolean alreadyExisted = existingDefinition != null;
            if (!alreadyExisted) {
                definitionManager.updateDefinition(defName, null);
                addDefinitionToPalette(defName);
            }
            return alreadyExisted;
        }
    }

    private void addDefinitionToPalette(String defName) {
        List<String> definitionNames =
                Ordering.natural().sortedCopy(definitionManager.getDefinitionNames());
        int defIndex = definitionNames.indexOf(defName);

        PaletteReferenceController referenceController =
                controllerFactoryFactory.create(this).createPaletteReferenceController(defName);
        definitionPaletteView.addChild(referenceController.getView().getNativeView(), defIndex);
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
                definitionManager.updateDefinition(
                        newScreenDefinition.defName(), newScreenDefinition.expr());
                invalidateExecuteButtons();
            } else {
                // Hide the definition (but don't actually delete it from the definition manager).
                expressionState.deleteDefinition(screenDefinition.defName());
                panManager.unregisterPanListener(controller);
                definitionControllers.remove(screenDefinition.defName());
            }
        });
        return controller;
    }

    private void invalidateExecuteButtons() {
        for (TopLevelExpressionController controller : expressionControllers) {
            controller.invalidateExecuteButton();
        }
    }
}
