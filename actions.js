/**
 * @flow
 */

import type {
    ScreenExpression,
    CanvasPoint
} from './types'

export const addExpression = (screenExpr: ScreenExpression): Action => {
    return {
        type: 'ADD_EXPRESSION',
        screenExpr,
    };
};

export const moveExpression = (exprId: number, pos: CanvasPoint): Action => {
    return {
        type: 'MOVE_EXPRESSION',
        exprId,
        pos,
    };
};

export type Action =
    { type: 'ADD_EXPRESSION', screenExpr: ScreenExpression } |
    { type: 'MOVE_EXPRESSION', exprId: number, pos: CanvasPoint};