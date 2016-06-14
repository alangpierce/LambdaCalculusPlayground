/**
 * @flow
 */
import {emptyIdPath, emptyDefinitionPath, step} from './ExprPaths';
import {getPositionOnScreen} from './ViewTracker';

import {PALLETE_VAR_NAMES} from './constants';
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
    const dragCandidates = [];
    const generateExpressionPickUps = () => {
        for (let [exprId] of state.canvasExpressions) {
            const viewKey = t.ExpressionKey.make(emptyIdPath(exprId));
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.PickUpExpression.make(
                        exprId, point.minus(screenRect.topLeft), screenRect),
                    0,
                ]);
            }
        }
    };

    const generateDefinitionPickUps = () => {
        for (let [defName] of state.canvasDefinitions) {
            const viewKey = t.DefinitionKey.make(defName);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.PickUpDefinition.make(
                        defName, point.minus(screenRect.topLeft), screenRect),
                    0,
                ]);
            }
        }
    };

    const generateDefinitionExtracts = () => {
        for (let [defName] of state.canvasDefinitions) {
            if (!state.definitions.get(defName)) {
                continue;
            }
            const viewKey = t.ExpressionKey.make(emptyDefinitionPath(defName));
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.ExtractDefinition.make(
                        defName, point.minus(screenRect.topLeft), screenRect),
                    1,
                ]);
            }
        }
    };

    const generateExpressionDecomposes = () => {
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
            if (screenRect.containsPoint(point)) {
                const parentPath = path.updatePathSteps((steps) => steps.pop());
                dragCandidates.push([
                    t.DecomposeExpression.make(
                        parentPath, point.minus(screenRect.topLeft),
                        screenRect),
                    pathDepth(path),
                ]);
            }
        }
    };

    const generateReferenceGenerators = () => {
        for (let [defName] of state.canvasDefinitions) {
            const viewKey = t.DefinitionRefKey.make(defName);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.CreateExpression.make(
                        t.UserReference.make(defName),
                        point.minus(screenRect.topLeft),
                        screenRect),
                    1,
                ]);
            }
        }
    };

    const generateLambdaVarGenerators = () => {
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (!(expr instanceof t.UserLambda)) {
                continue;
            }
            const viewKey = t.LambdaVarKey.make(path);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.CreateExpression.make(
                        t.UserVariable.make(expr.varName),
                        point.minus(screenRect.topLeft),
                        screenRect),
                    pathDepth(path) + 1,
                ]);
            }
        }
    };

    const generatePaletteLambdaGenerators = () => {
        for (const varName of PALLETE_VAR_NAMES) {
            const viewKey = t.PaletteLambdaKey.make(varName);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.CreateExpression.make(
                        t.UserLambda.make(varName, null),
                        point.minus(screenRect.topLeft),
                        screenRect),
                    2,
                ]);
            }
        }
    };

    const generatePaletteReferenceGenerators = () => {
        for (const defName of state.definitions.keys()) {
            const viewKey = t.PaletteReferenceKey.make(defName);
            const screenRect = getPositionOnScreen(viewKey);
            if (!screenRect) {
                continue;
            }
            if (screenRect.containsPoint(point)) {
                dragCandidates.push([
                    t.CreateExpression.make(
                        t.UserReference.make(defName, null),
                        point.minus(screenRect.topLeft),
                        screenRect),
                    2,
                ]);
            }
        }
    };

    // generates the drag result and priority.
    const generateDragCandidates = () => {
        generateExpressionPickUps();
        generateDefinitionPickUps();
        generateDefinitionExtracts();
        generateExpressionDecomposes();
        generateReferenceGenerators();
        generateLambdaVarGenerators();
        generatePaletteLambdaGenerators();
        generatePaletteReferenceGenerators();
    };

    generateDragCandidates();
    let bestPriority = -1;
    let bestResult = t.StartPan.make(point);
    for (let [dragResult, priority] of dragCandidates) {
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
        return !!rect && dragData.screenRect.overlapsWith(rect);
    };

    const intersectsWithRightSide = (key: ViewKey): bool => {
        const rect = getPositionOnScreen(key);
        return !!rect && dragData.screenRect.overlapsWith(rect.rightSide());
    };

    const dropCandidates = [];

    const generateLambdaDrops = () => {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        for (let [path, expr] of yieldAllExpressions(state)) {
            if (expr.type !== 'userLambda' || expr.body) {
                continue;
            }
            if (intersectsWithView(t.EmptyBodyKey.make(path))) {
                dropCandidates.push([
                    t.InsertAsBodyResult.make(path, dragPayload.userExpr),
                    // The lambda body should show up as above the lambda.
                    pathDepth(path) + 1,
                ]);
            }
        }
    };

    const generateFuncCallDrops = () => {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        for (let [path, _] of yieldAllExpressions(state)) {
            if (intersectsWithRightSide(t.ExpressionKey.make(path))) {
                dropCandidates.push([
                    t.InsertAsArgResult.make(path, dragPayload.userExpr),
                    pathDepth(path),
                ]);
            }
        }
    };

    const generateParamDeleteDrops = () => {
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
                dropCandidates.push([
                    t.RemoveResult.make(),
                    pathDepth(path) + 1,
                ]);
            }
        }
    };

    const generateDefinitionBodyDrops = () => {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        for (let [defName] of state.canvasDefinitions) {
            // Can't drop into an already-defined definition.
            if (state.definitions.get(defName)) {
                continue;
            }
            if (intersectsWithView(t.DefinitionEmptyBodyKey.make(defName))) {
                dropCandidates.push([
                    t.InsertAsDefinitionResult.make(defName, dragPayload.userExpr),
                    1,
                ]);
            }
        }
    };

    const generateReferenceDeleteDrops = () => {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        if (dragPayload.userExpr.type !== 'userReference') {
            return;
        }
        for (let [defName] of state.canvasDefinitions) {
            if (intersectsWithView(t.DefinitionRefKey.make(defName))) {
                dropCandidates.push([
                    t.RemoveResult.make(),
                    1,
                ]);
            }
        }
    };

    const generatePaletteLambdaDeleteDrops = () => {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        const {userExpr} = dragPayload;
        if (userExpr.type !== 'userLambda' || userExpr.body != null) {
            return;
        }
        for (const varName of PALLETE_VAR_NAMES) {
            if (intersectsWithView(t.PaletteLambdaKey.make(varName))) {
                dropCandidates.push([
                    t.RemoveResult.make(),
                    2,
                ]);
            }
        }
    };

    const generatePaletteReferenceDeleteDrops = () => {
        if (!(dragPayload instanceof DraggedExpression)) {
            return;
        }
        if (dragPayload.userExpr.type !== 'userReference') {
            return;
        }
        for (const defName of state.definitions.keys()) {
            if (intersectsWithView(t.PaletteReferenceKey.make(defName))) {
                dropCandidates.push([
                    t.RemoveResult.make(),
                    2,
                ]);
            }
        }
    };

    const generateDeleteBarDrop = () => {
        if (intersectsWithView(t.DeleteBarKey.make())) {
            dropCandidates.push([
                t.RemoveWithDeleteBarResult.make(),
                1,
            ]);
        }
    };

    const generateDropCandidates = () => {
        generateLambdaDrops();
        generateFuncCallDrops();
        generateParamDeleteDrops();
        generateDefinitionBodyDrops();
        generateReferenceDeleteDrops();
        generatePaletteLambdaDeleteDrops();
        generatePaletteReferenceDeleteDrops();
        generateDeleteBarDrop();
    };

    generateDropCandidates();
    let bestPriority = -1;
    let bestResult = t.AddToTopLevelResult.make(
        dragPayload, dragData.screenRect.topLeft);
    for (let [dropResult, priority] of dropCandidates) {
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

const pathDepth = (path: ExprPath): number => {
    const initialDepth = path.container.match({
        exprIdContainer: () => 0,
        definitionContainer: () => 1,
    });
    return path.pathSteps.size + initialDepth;
};