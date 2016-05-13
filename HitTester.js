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

/**
 * Given a click on the screen, figure out what expression it is under, if any.
 */
export const resolveTouch = (state: State, point: ScreenPoint): DragResult => {
    const yieldExpressionPickUps = function* () {
        for (let [exprId] of state.canvasExpressions) {
            const viewKey = t.newExpressionKey(emptyIdPath(exprId));
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.newPickUpExpression(
                        exprId, ptMinusPt(point, screenRect.topLeft), screenRect),
                    0,
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
            const viewKey = t.newExpressionKey(path);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                const parentPath = path.updatePathSteps((steps) => steps.pop());
                yield [
                    t.newDecomposeExpression(
                        parentPath, ptMinusPt(point, screenRect.topLeft),
                        screenRect),
                    path.pathSteps.size,
                ];
            }
        }
    };

    const yieldLambdaVarGenerators = function* () {
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (expr.type !== 'userLambda') {
                continue;
            }
            const viewKey = t.newLambdaVarKey(path);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (ptInRect(point, screenRect)) {
                yield [
                    t.newCreateExpression(
                        t.newUserVariable(expr.varName),
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
        yield* yieldExpressionDecomposes();
        yield* yieldLambdaVarGenerators();
    };

    let bestPriority = -1;
    let bestResult = t.newStartPan(point);
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
    const intersectsWithView = (key: ViewKey): bool => {
        const rect = getPositionOnScreen(key);
        return !!rect && rectsOverlap(dragData.screenRect, rect);
    };

    const intersectsWithRightSide = (key: ViewKey): bool => {
        const rect = getPositionOnScreen(key);
        return !!rect && rectsOverlap(dragData.screenRect, rightSide(rect));
    };

    const yieldLambdaDrops = function* () {
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (expr.type !== 'userLambda' || expr.body) {
                continue;
            }
            if (intersectsWithView(t.newEmptyBodyKey(path))) {
                yield [
                    t.newInsertAsBodyResult(path, dragData.userExpr),
                    // The lambda body should show up as above the lambda.
                    path.pathSteps.size + 1,
                ];
            }
        }
    };

    const yieldFuncCallDrops = function* () {
        for (let [path, _] of yieldAllExpressions(state)) {
            if (intersectsWithRightSide(t.newExpressionKey(path))) {
                yield [
                    t.newInsertAsArgResult(path, dragData.userExpr),
                    path.pathSteps.size,
                ];
            }
        }
    };

    const yieldParamDeleteDrops = function* () {
        if (dragData.userExpr.type !== 'userVariable') {
            return;
        }
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (expr.type !== 'userLambda') {
                continue;
            }
            if (intersectsWithView(t.newLambdaVarKey(path))) {
                yield [
                    t.newRemoveResult(),
                    path.pathSteps.size + 1,
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
    };

    let bestPriority = -1;
    let bestResult = t.newAddToTopLevelResult(
        dragData.userExpr, dragData.screenRect.topLeft);
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