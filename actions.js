/**
 * @flow
 */

import type {ScreenExpression} from './ExpressionType'

export const addExpression = (screenExpr: ScreenExpression): Action => {
    return {
        type: 'ADD_EXPRESSION',
        screenExpr,
    };
};

export type Action =
    { type: 'ADD_EXPRESSION', screenExpr: ScreenExpression };