/**
 * @flow
 */

jest.disableAutomock();

import * as Immutable from 'immutable'

import parseExpression from '../parseExpression'
import store from '../store'
import {newCanvasPoint, newExprPath, newScreenExpression} from '../types'
import * as t from '../types'

describe('reducers', () => {
    beforeEach(function () {
        jest.addMatchers({
            is: () => ({
                compare: (actual, expected) => {
                    return actual.toJSON() === expected.toJSON();
                    // return Immutable.is(actual, expected);
                }
            })
        })
    });

    it('handles new expressions', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[x]')));
        const state = store.getState();
        expect(state.nextExprId).toEqual(1);
        expect((state: any).screenExpressions.get(0).expr.varName).toEqual('x');
    });

    it('handles moving expressions', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[x]')));
        store.dispatch(t.newMoveExpression(0, newCanvasPoint(100, 100)));
        const screenExpr = store.getState().screenExpressions.get(0);
        expect((screenExpr: any).pos.canvasX).toEqual(100);
    });

    it('handles extract body', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[L y[L z[x]]]')));
        store.dispatch(t.newExtractBody(
            newExprPath(0, ['body']), newCanvasPoint(25, 25)
        ));
        expect(store.getState().screenExpressions.get(0).toJS()).toEqual(
            newScreenExpression(
                parseExpression('L x[L y[_]]'), newCanvasPoint(50, 50)).toJS());
        expect(store.getState().screenExpressions.get(1).toJS()).toEqual(
            newScreenExpression(
                parseExpression('L z[x]'), newCanvasPoint(25, 25)).toJS());
    });

    const makeScreenExpr = (exprString) => {
        return newScreenExpression(
            parseExpression(exprString), newCanvasPoint(50, 50));
    };
});