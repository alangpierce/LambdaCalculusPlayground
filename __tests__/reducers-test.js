/**
 * @flow
 */

jest.disableAutomock();

import * as actions from '../actions'
import parseExpression from '../parseExpression'
import store from '../store'
import {newCanvasPoint, newExprPath, newScreenExpression} from '../types'

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
        store.dispatch(actions.moveExpression(0, newCanvasPoint(100, 100)));
        const screenExpr = store.getState().screenExpressions.get(0);
        expect((screenExpr: any).pos.canvasX).toEqual(100);
    });

    it('handles extract body', () => {
        store.dispatch(actions.reset());
        store.dispatch(
            actions.addExpression(makeScreenExpr('L x[L y[L z[x]]]')));
        store.dispatch(actions.extractBody(
            newExprPath(0, ['body']), newCanvasPoint(25, 25)
        ));
        expect(store.getState().screenExpressions.get(0)).toEqual(
            newScreenExpression(
                parseExpression('L x[L y[_]]'), newCanvasPoint(50, 50)));
        expect(store.getState().screenExpressions.get(1)).toEqual(
            newScreenExpression(
                parseExpression('L z[x]'), newCanvasPoint(25, 25)));
    });

    const makeScreenExpr = (exprString) => {
        return newScreenExpression(
            parseExpression(exprString), newCanvasPoint(50, 50));
    };
});