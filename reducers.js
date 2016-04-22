/**
 * @flow
 */

import type {Action} from './actions'
import type {ScreenExpression} from './types'

export type State = {
    screenExpressions: Map<number, ScreenExpression>,
    nextExprId: number,
};

const initialState = {
    screenExpressions: new Map(),
    nextExprId: 0,
};

const playgroundApp = (state: State = initialState, action: Action) => {
    if (action.type === 'ADD_EXPRESSION') {
        const nextExprId = state.nextExprId;
        const newScreenExpressions = new Map(state.screenExpressions);
        newScreenExpressions.set(nextExprId, action.screenExpr);
        return {
            screenExpressions: newScreenExpressions,
            nextExprId: nextExprId + 1,
        };
    } else if (action.type === 'MOVE_EXPRESSION') {
        const newScreenExpressions = new Map(state.screenExpressions);
        const screenExpr = newScreenExpressions.get(action.exprId);
        if (screenExpr) {
            newScreenExpressions.set(action.exprId, {
                expr: screenExpr.expr,
                pos: action.pos,
            });
        }
        return {
            screenExpressions: newScreenExpressions,
            nextExprId: state.nextExprId,
        };
    }
    return state;
};

export default playgroundApp;