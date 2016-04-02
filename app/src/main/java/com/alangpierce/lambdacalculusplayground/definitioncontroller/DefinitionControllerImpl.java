package com.alangpierce.lambdacalculusplayground.definitioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenDefinition;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.component.ProducerController;
import com.alangpierce.lambdacalculusplayground.component.ProducerControllerParent;
import com.alangpierce.lambdacalculusplayground.component.SlotController;
import com.alangpierce.lambdacalculusplayground.component.SlotControllerParent;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragData;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserReference;
import com.alangpierce.lambdacalculusplayground.view.DefinitionView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class DefinitionControllerImpl implements DefinitionController {
    private final PointConverter pointConverter;

    private final DefinitionView view;
    private final ProducerController referenceProducerController;
    private final SlotController expressionSlotController;

    private ScreenDefinition screenDefinition;
    private OnDefinitionChangeCallback onChangeCallback;

    private final Subject<Observable<PointerMotionEvent>, Observable<PointerMotionEvent>>
            dragActionSubject = PublishSubject.create();

    public DefinitionControllerImpl(
            PointConverter pointConverter,
            DefinitionView view,
            ProducerController referenceProducerController,
            SlotController expressionSlotController,
            ScreenDefinition screenDefinition) {
        this.pointConverter = pointConverter;
        this.view = view;
        this.referenceProducerController = referenceProducerController;
        this.expressionSlotController = expressionSlotController;
        this.screenDefinition = screenDefinition;
    }

    public static ProducerControllerParent createProducerParent(
            TopLevelExpressionManager topLevelExpressionManager, String defName) {
        return new ProducerControllerParent() {
            @Override
            public TopLevelExpressionController produceExpression(ScreenPoint screenPos) {
                return topLevelExpressionManager.createNewExpression(
                        UserReference.create(defName), screenPos, false /* placeAbovePalette */);
            }
            @Override
            public boolean shouldDeleteExpression(UserExpression expression) {
                return expression instanceof UserReference;
            }
        };
    }

    public SlotControllerParent createSlotParent() {
        return new SlotControllerParent() {
            @Override
            public void updateSlotExpression(@Nullable UserExpression userExpression) {
                screenDefinition = ScreenDefinition
                        .create(screenDefinition.defName(), userExpression,
                                screenDefinition.canvasPos());
            }

            @Override
            public void handleChange() {
                onChangeCallback.onChange(DefinitionControllerImpl.this);
            }
        };
    }

    @Override
    public void setOnChangeCallback(OnDefinitionChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public ScreenDefinition getScreenDefinition() {
        return screenDefinition;
    }

    @Override
    public void onPan() {
        DrawableAreaPoint drawableAreaPoint =
                pointConverter.toDrawableAreaPoint(screenDefinition.canvasPos());
        view.setCanvasPos(drawableAreaPoint);
    }

    @Override
    public List<DragSource> getDragSources() {
        // TODO: It's weird for a function with this name to have a side-effect like this.
        view.getDragObservable().subscribe(dragActionSubject);
        return ImmutableList.of(
                new DefinitionDragSource(),
                referenceProducerController.getDragSource(),
                expressionSlotController.getDragSource());
    }

    @Override
    public List<DropTarget<?>> getDropTargets() {
        return ImmutableList.of(
                referenceProducerController.getDropTarget(),
                expressionSlotController.getDropTarget());
    }

    @Override
    public <T> T visit(Visitor<TopLevelExpressionController, T> expressionVisitor,
            Visitor<DefinitionController, T> definitionVisitor) {
        return definitionVisitor.accept(this);
    }

    @Override
    public void startDrag() {
        // TODO: Do something here.
    }

    @Override
    public void setScreenPos(ScreenPoint screenPos) {
        DrawableAreaPoint drawableAreaPoint = pointConverter.toDrawableAreaPoint(screenPos);
        view.setCanvasPos(drawableAreaPoint);
    }

    @Override
    public void endDrag() {
        // TODO: Do something here.
    }

    @Override
    public void handlePositionChange(ScreenPoint screenPos) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        screenDefinition = ScreenDefinition.create(
                screenDefinition.defName(), screenDefinition.expr(), canvasPos);
        onChangeCallback.onChange(this);
    }

    private class DefinitionDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return dragActionSubject;
        }
        @Override
        public DragData handleStartDrag() {
            return DefinitionControllerImpl.this;
        }
    }
}
