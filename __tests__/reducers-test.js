/**
 * @flow
 */

jest.disableAutomock();

import * as actions from '../actions'
import parseExpression from '../parseExpression'
import store from '../store'
import {canvasPoint, exprPath} from '../types'

describe('reducers', () => {
    it('handles new expressions', () => {
        store.dispatch(actions.reset());
        store.dispatch(actions.addExpression(makeScreenExpr('L x[x]')));
        const state = store.getState();
        expect(state.nextExprId).toEqual(1);
        expect((state: any).screenExpressions.get(0).expr.varName).toEqual('x');
    });

    it('handles moving expressions', () => {
        store.dispatch(actions.reset());
        store.dispatch(actions.addExpression(makeScreenExpr('L x[x]')));
        store.dispatch(actions.moveExpression(0, canvasPoint(100, 100)));
        const screenExpr = store.getState().screenExpressions.get(0);
        expect((screenExpr: any).pos.canvasX).toEqual(100);
    });

    it('handles extract body', () => {
        store.dispatch(actions.reset());
        store.dispatch(
            actions.addExpression(makeScreenExpr('L x[L y[L z[x]]]')));
        store.dispatch(actions.extractBody(
            exprPath(0, ['body']), canvasPoint(25, 25)
        ));
        expect(store.getState().screenExpressions.get(0)).toEqual({
            expr: parseExpression('L x[L y[_]]'),
            pos: canvasPoint(50, 50),
        });
        expect(store.getState().screenExpressions.get(1)).toEqual({
            expr: parseExpression('L z[x]'),
            pos: canvasPoint(25, 25),
        });
    });

    const makeScreenExpr = (exprString) => {
        return {
            expr: parseExpression(exprString),
            pos: canvasPoint(50, 50),
        };
    };
});