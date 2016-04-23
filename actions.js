/**
 * @flow
 */

import type {
    CanvasPoint,
    ExprPath,
    ScreenExpression,
} from './types'

/**
 * Clear the state. Useful for testing.
 */
export const reset = (): Action => {
    return {
        type: 'RESET',
    }
};

/**
 * Create a new expression at the given position.
 */
export const addExpression = (screenExpr: ScreenExpression): Action => {
    return {
        type: 'ADD_EXPRESSION',
        screenExpr,
    };
};

/**
 * Move the existing expression on the canvas to a new point.
 */
export const moveExpression = (exprId: number, pos: CanvasPoint): Action => {
    return {
        type: 'MOVE_EXPRESSION',
        exprId,
        pos,
    };
};

/**
 * Given an expression path, which must reference a lambda expression with a
 * body, remove that body and create a new expression from it at the given
 * coordinates.
 */
export const extractBody = (path: ExprPath, targetPos: CanvasPoint): Action => {
    return {
        type: 'EXTRACT_BODY',
        path,
        targetPos,
    };
};

export type Action =
    { type: 'RESET'} |
    { type: 'ADD_EXPRESSION', screenExpr: ScreenExpression } |
    { type: 'MOVE_EXPRESSION', exprId: number, pos: CanvasPoint} |
    { type: 'EXTRACT_BODY', path: ExprPath, targetPos: CanvasPoint};