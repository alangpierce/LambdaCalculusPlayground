/**
 * @flow
 */

import type {
    ScreenExpression,
    CanvasPoint
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

export type Action =
    { type: 'RESET'} |
    { type: 'ADD_EXPRESSION', screenExpr: ScreenExpression } |
    { type: 'MOVE_EXPRESSION', exprId: number, pos: CanvasPoint};