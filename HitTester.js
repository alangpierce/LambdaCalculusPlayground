/**
 * @flow
 */
import {emptyIdPath, emptyDefinitionPath, step} from './ExprPaths';
import {getPositionOnScreen} from './ViewTracker';
import {ptInRect, ptMinusPt, rectsOverlap, rightSide} from './Geometry';

import * as t from './types';
import type {
    DragData,
    DragResult,
    DropResult,
    ExprPath,
    ScreenPoint,
    State,
    UserExpression,
    ViewKey,
} from './types';
import {
    DraggedExpression
} from './types';

/**
 * Given a click on the screen, figure out what expression it is under, if any.
 */
export const resolveTouch = (state: State, point: ScreenPoint): DragResult => {
    const yieldExpressionPickUps = function* () {
        for (let [exprId] of state.canvasExpressions) {
            const viewKey = t.ExpressionKey.make(emptyIdPath(exprId));
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.PickUpExpression.make(
                        exprId, ptMinusPt(point, screenRect.topLeft), screenRect),
                    0,
                ];
            }
        }
    };

    const yieldDefinitionPickUps = function* () {
        for (let [defName] of state.canvasDefinitions) {
            const viewKey = t.DefinitionKey.make(defName);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.PickUpDefinition.make(
                        defName, ptMinusPt(point, screenRect.topLeft), screenRect),
                    0,
                ];
            }
        }
    };

    const yieldDefinitionExtracts = function* () {
        for (let [defName] of state.canvasDefinitions) {
            if (!state.definitions.get(defName)) {
                continue;
            }
            console.log("considering " + defName);
            const viewKey = t.ExpressionKey.make(emptyDefinitionPath(defName));
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.ExtractDefinition.make(
                        defName, ptMinusPt(point, screenRect.topLeft), screenRect),
                    1,
                ];
            }
        }
    };

    const yieldExpressionDecomposes = function* () {
        for (let [path, _] of yieldAllExpressions(state)) {
            // You can only decompose an expression that is a lambda body or a
            // function arg.
            const lastStep = path.pathSteps.get(path.pathSteps.size - 1, null);
            if (lastStep !== 'body' && lastStep !== 'arg') {
                continue;
            }
            const viewKey = t.ExpressionKey.make(path);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                const parentPath = path.updatePathSteps((steps) => steps.pop());
                yield [
                    t.DecomposeExpression.make(
                        parentPath, ptMinusPt(point, screenRect.topLeft),
                        screenRect),
                    path.pathSteps.size,
                ];
            }
        }
    };

    const yieldReferenceGenerators = function* () {
        for (let [defName] of state.canvasDefinitions) {
            const viewKey = t.DefinitionRefKey.make(defName);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.CreateExpression.make(
                        t.UserReference.make(defName),
                        ptMinusPt(point, screenRect.topLeft),
                        screenRect),
                    1,
                ];
            }
        }
    };

    const yieldLambdaVarGenerators = function* () {
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (!(expr instanceof t.UserLambda)) {
                continue;
            }
            const viewKey = t.LambdaVarKey.make(path);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.CreateExpression.make(
                        t.UserVariable.make(expr.varName),
                        ptMinusPt(point, screenRect.topLeft),
                        screenRect),
                    path.pathSteps.size + 1,
                ];
            }
        }
    };

    // Yields the drag result and priority.
    const yieldDragCandidates = function* ():
        Generator<[DragResult, number], void, void> {
        yield* yieldExpressionPickUps();
        yield* yieldDefinitionPickUps();
        yield* yieldDefinitionExtracts();
        yield* yieldExpressionDecomposes();
        yield* yieldReferenceGenerators();
        yield* yieldLambdaVarGenerators();
    };

    let bestPriority = -1;
    let bestResult = t.StartPan.make(point);
    for (let [dragResult, priority] of yieldDragCandidates(state)) {
        if (priority > bestPriority) {
            bestResult = dragResult;
            bestPriority = priority;
        }
    }
    return bestResult;
};

/**
 * Given a dragged item, determine the action that would happen if the item was
 * dropped. This is useful both to perform the drop and to do highlighting.
 */
export const resolveDrop = (state: State, dragData: DragData): DropResult => {
    const dragPayload = dragData.payload;
    const intersectsWithView = (key: ViewKey): bool => {
        const rect = getPositionOnScreen(key);
        return !!rect && rectsOverlap(dragData.screenRect, rect);
    };

    const intersectsWithRightSide = (key: ViewKey): bool => {
        const rect = getPositionOnScreen(key);
        return !!rect && rectsOverlap(dragData.screenRect, rightSide(rect));
    };

    const yieldLambdaDrops = function* () {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (expr.type !== 'userLambda' || expr.body) {
                continue;
            }
            if (intersectsWithView(t.EmptyBodyKey.make(path))) {
                yield [
                    t.InsertAsBodyResult.make(path, dragPayload.userExpr),
                    // The lambda body should show up as above the lambda.
                    path.pathSteps.size + 1,
                ];
            }
        }
    };

    const yieldFuncCallDrops = function* () {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        for (let [path, _] of yieldAllExpressions(state)) {
            if (intersectsWithRightSide(t.ExpressionKey.make(path))) {
                yield [
                    t.InsertAsArgResult.make(path, dragPayload.userExpr),
                    path.pathSteps.size,
                ];
            }
        }
    };

    const yieldParamDeleteDrops = function* () {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        if (dragPayload.userExpr.type !== 'userVariable') {
            return;
        }
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (expr.type !== 'userLambda') {
                continue;
            }
            if (intersectsWithView(t.LambdaVarKey.make(path))) {
                yield [
                    t.RemoveResult.make(),
                    path.pathSteps.size + 1,
                ];
            }
        }
    };

    const yieldDefinitionBodyDrops = function* () {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        for (let [defName] of state.canvasDefinitions) {
            // Can't drop into an already-defined definition.
            if (state.definitions.get(defName)) {
                continue;
            }
            if (intersectsWithView(t.DefinitionEmptyBodyKey.make(defName))) {
                yield [
                    t.InsertAsDefinitionResult.make(defName, dragPayload.userExpr),
                    1,
                ];
            }
        }
    };

    // Yields the drop result and priority.
    const yieldDropCandidates = function* ():
        Generator<[DropResult, number], void, void> {
        yield* yieldLambdaDrops();
        yield* yieldFuncCallDrops();
        yield* yieldParamDeleteDrops();
        yield* yieldDefinitionBodyDrops();
    };

    let bestPriority = -1;
    let bestResult = t.AddToTopLevelResult.make(
        dragPayload, dragData.screenRect.topLeft);
    for (let [dropResult, priority] of yieldDropCandidates(state)) {
        if (priority > bestPriority) {
            bestResult = dropResult;
            bestPriority = priority;
        }
    }
    return bestResult;
};

const yieldAllExpressions = function* (state: State):
        Generator<[ExprPath, UserExpression], void, void> {
    for (let [exprId, canvasExpr] of state.canvasExpressions) {
        yield* yieldExpressions(canvasExpr.expr, emptyIdPath(exprId))
    }
    for (let [defName, _] of state.canvasDefinitions) {
        const userExpr = state.definitions.get(defName);
        if (userExpr != null) {
            yield* yieldExpressions(userExpr, emptyDefinitionPath(defName));
        }
    }
};

const yieldExpressions = function* (expr: UserExpression, path: ExprPath):
        Generator<[ExprPath, UserExpression], void, void> {
    yield [path, expr];
    yield* expr.match({
        userLambda: function* ({body}) {
            if (body) {
                yield* yieldExpressions(body, step(path, 'body'));
            }
        },
        userFuncCall: function* ({func, arg}) {
            yield* yieldExpressions(func, step(path, 'func'));
            yield* yieldExpressions(arg, step(path, 'arg'));
        },
        userVariable: function* () {},
        userReference: function* () {},
    })
};