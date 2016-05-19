/**
 * @flow
 */

import {emptyIdPath} from './ExprPaths';
import {
    canStepUserExpr, evaluateUserExpr, expandAllDefinitions
} from './UserExpressionEvaluator';
import type {
    Action,
    CanvasExpression,
    CanvasPoint,
    DragData,
    State
} from './types'
import * as t from './types'
import {IMap, ISet} from './types-collections'
import {
    addExpression,
    decomposeExpression,
    insertAsArg,
    insertAsBody,
    updateExprContainer,
} from './ExpressionState'
import {resolveDrop, resolveTouch} from './HitTester'
import {screenPtToCanvasPt} from './PointConversion'
import {deserialize} from './types-lib'
import {getPositionOnScreen} from './ViewTracker';

const initialState: State = t.State.make(
    IMap.make(),
    0,
    IMap.make(),
    IMap.make(),
    IMap.make(),
    IMap.make(),
    ISet.make(),
    ISet.make(),
    ISet.make(),
);

// TODO: Consider adding a top-level try/catch.
const playgroundApp = (state: State = initialState, rawAction: any): State => {
    // Despite our action union, there are some internal redux actions that
    // start with @@, which we want to just ignore.
    if (rawAction.type.startsWith('@@')) {
        return state;
    }
    const action: Action = deserialize(rawAction);

    const exprWithId = (exprId: number): CanvasExpression => {
        const result = state.canvasExpressions.get(exprId);
        if (!result) {
            throw new Error('Expected expression with ID ' + exprId);
        }
        return result;
    };
    return action.match({
        reset: () => initialState,
        addExpression: ({canvasExpr}) => addExpression(state, canvasExpr),
        placeDefinition: ({defName, screenPos}) => {
            return state
                .lens().canvasDefinitions().atKey(defName).replace(
                    screenPtToCanvasPt(screenPos))
                // Create an entry for the definition, which may be null.
                .lens().definitions().atKey(defName).update((def) => def);
        },
        moveExpression: ({exprId, pos}) => {
            return state
                .lens().canvasExpressions().atKey(exprId).pos().replace(pos);
        },
        decomposeExpressionAction: ({path: {container, pathSteps}, targetPos}) => {
            let extracted = null;
            state = updateExprContainer(state, container, expr => {
                const decomposed = decomposeExpression(expr, pathSteps);
                extracted = decomposed.extracted;
                return decomposed.original;
            });
            if (extracted == null) {
                throw new Error('Expected extracted to be set.');
            }
            state = addExpression(
                state, t.CanvasExpression.make(extracted, targetPos));
            return state;
        },
        insertAsArg: ({argExprId, path: {container, pathSteps}}) => {
            state = updateExprContainer(state, container, expr => {
                const argCanvasExpr = exprWithId(argExprId);
                return insertAsArg(expr, argCanvasExpr.expr, pathSteps);
            });
            state = state.lens().canvasExpressions().deleteKey(argExprId);
            return state;
        },
        insertAsBody: ({bodyExprId, path: {container, pathSteps}}) => {
            state = updateExprContainer(state, container, expr => {
                const bodyCanvasExpr = exprWithId(bodyExprId);
                return insertAsBody(expr, bodyCanvasExpr.expr, pathSteps);
            });
            state = state.lens().canvasExpressions().deleteKey(bodyExprId);
            return state;
        },
        evaluateExpression: ({exprId}) => {
            const definitions = expandAllDefinitions(state.definitions);
            const existingExpr = exprWithId(exprId);
            if (!canStepUserExpr(definitions, existingExpr.expr)) {
                return state;
            }
            const evaluatedExpr = evaluateUserExpr(definitions, existingExpr.expr);
            if (!evaluatedExpr) {
                return state;
            }
            // We don't have enough information to place the expression yet,
            // since we don't know how big it is. Instead, place it in a list to
            // be measured, and complete the operation when placePendingResult
            // is triggered.
            const pendingResultId = state.nextExprId;
            return state
                .lens().pendingResults().atKey(pendingResultId).
                    replace(t.PendingResult.make(evaluatedExpr, exprId))
                .withNextExprId(pendingResultId + 1);
        },
        placePendingResult: ({exprId, width}) => {
            const pendingResult = state.pendingResults.get(exprId);
            if (pendingResult == null) {
                return state;
            }
            const {expr, sourceExprId} = pendingResult;
            const resultPos = computeResultPos(sourceExprId, width);
            state = state.lens().pendingResults().deleteKey(exprId);
            return addExpression(state, t.CanvasExpression.make(expr, resultPos));
        },
        fingerDown: ({fingerId, screenPos}) => {
            const dragResult = resolveTouch(state, screenPos);
            state = dragResult.match({
                pickUpExpression: ({exprId, offset, screenRect}) => {
                    const expr = exprWithId(exprId).expr;
                    return state
                        .lens().canvasExpressions().deleteKey(exprId)
                        .lens().activeDrags().atKey(fingerId).replace(
                            t.DragData.make(
                                t.DraggedExpression.make(expr),
                                offset, screenRect));
                },
                pickUpDefinition: ({defName, offset, screenRect}) => {
                    return state
                        .lens().canvasDefinitions().deleteKey(defName)
                        .lens().activeDrags().atKey(fingerId).replace(
                            t.DragData.make(
                                t.DraggedDefinition.make(defName),
                                offset, screenRect));
                },
                extractDefinition: ({defName, offset, screenRect}) => {
                    const expr = state.definitions.get(defName);
                    if (expr == null) {
                        throw new Error('Expected expression to extract.');
                    }
                    return state
                        .lens().definitions().atKey(defName).replace(null)
                        .lens().activeDrags().atKey(fingerId).replace(
                            t.DragData.make(
                                t.DraggedExpression.make(expr),
                                offset, screenRect));
                },
                decomposeExpression: ({exprPath, offset, screenRect}) => {
                    let extracted;
                    state = updateExprContainer(state, exprPath.container, expr => {
                        const decomposed = decomposeExpression(expr, exprPath.pathSteps);
                        extracted = decomposed.extracted;
                        return decomposed.original;
                    });
                    if (extracted == null) {
                        throw new Error('Expected extracted to be set.');
                    }
                    return state.lens().activeDrags().atKey(fingerId).replace(
                        t.DragData.make(
                            t.DraggedExpression.make(extracted),
                            offset, screenRect));
                },
                createExpression: ({expr, offset, screenRect}) => {
                    return state.updateActiveDrags(drags =>
                        drags.set(fingerId,
                            t.DragData.make(
                                t.DraggedExpression.make(expr),
                                offset, screenRect)));
                },
                startPan: () => {
                    // TODO
                    return state;
                },
            });
            return computeHighlights(state);
        },
        fingerMove: ({fingerId, screenPos}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const {grabOffset, screenRect} = dragData;
            const oldGrabPoint = screenRect.topLeft.plusDiff(grabOffset);
            const shiftAmount = screenPos.minus(oldGrabPoint);
            const newScreenRect = screenRect.plusDiff(shiftAmount);
            state = state.lens().activeDrags().atKey(fingerId).screenRect()
                .replace(newScreenRect);
            return computeHighlights(state);
        },
        fingerUp: ({fingerId, screenPos}) => {
            const dragData: ?DragData = state.activeDrags.get(fingerId);
            if (!dragData) {
                return state;
            }
            const dropResult = resolveDrop(state, dragData, screenPos);
            state = state.lens().activeDrags().deleteKey(fingerId);
            state = dropResult.match({
                addToTopLevelResult: ({payload, screenPos}) => {
                    const canvasPos = screenPtToCanvasPt(screenPos);
                    return payload.match({
                        draggedExpression: ({userExpr}) =>
                            addExpression(state,
                                t.CanvasExpression.make(userExpr, canvasPos)),
                        draggedDefinition: ({defName}) =>
                            state.lens().canvasDefinitions().atKey(defName)
                                .replace(canvasPos),
                    });
                },
                insertAsBodyResult: ({lambdaPath: {container, pathSteps}, expr}) =>
                    updateExprContainer(state, container, targetExpr =>
                        insertAsBody(targetExpr, expr, pathSteps)
                    ),
                insertAsArgResult: ({path: {container, pathSteps}, expr}) =>
                    updateExprContainer(state, container, targetExpr =>
                        insertAsArg(targetExpr, expr, pathSteps)
                    ),
                insertAsDefinitionResult: ({defName, expr}) =>
                    state.lens().definitions().atKey(defName).replace(expr),
                removeResult: () => {
                    // Do nothing; we've already removed the expression.
                    return state;
                },
            });
            return computeHighlights(state);
        },
    });
};

const computeResultPos = (sourceExprId: number, width: number):
        CanvasPoint => {
    const sourceExprKey = t.ExpressionKey.make(emptyIdPath(sourceExprId));
    const sourceRect = getPositionOnScreen(sourceExprKey);
    if (sourceRect == null) {
        return t.CanvasPoint.make(100, 100);
    }
    const midPoint = (sourceRect.topLeft.screenX + sourceRect.bottomRight.screenX) / 2;
    return t.CanvasPoint.make(
        midPoint - (width / 2),
        sourceRect.bottomRight.screenY + 15,
    )
};

const computeHighlights = (state: State): State => {
    const exprPaths = [];
    const emptyBodyPaths = [];
    const definitions = [];
    for (let [_, dragData] of state.activeDrags) {
        resolveDrop(state, dragData).match({
            addToTopLevelResult: () => {},
            insertAsBodyResult: ({lambdaPath}) => {
                emptyBodyPaths.push(lambdaPath)
            },
            insertAsArgResult: ({path}) => {exprPaths.push(path)},
            insertAsDefinitionResult: ({defName}) => {definitions.push(defName)},
            removeResult: () => {},
        });
    }
    return state
        .withHighlightedExprs(ISet.make(exprPaths))
        .withHighlightedEmptyBodies(ISet.make(emptyBodyPaths))
        .withHighlightedDefinitionBodies(ISet.make(definitions));
};

export default playgroundApp;