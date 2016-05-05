/**
 * @flow
 */

jest.disableAutomock();

import * as Immutable from 'immutable'

import {formatExpr, parseExpr} from '../ExpressionStr'
import store from '../store'
import type {PathComponent} from '../types'
import {newCanvasPoint, newExprPath, newScreenExpression} from '../types'
import * as t from '../types'

// Just use a normal function lambda for now so that arguments works.
const list = function(): Immutable.List<PathComponent> {
    return new Immutable.List(arguments);
};

describe('reducers', () => {
    beforeEach(function () {
        jest.addMatchers({
            is: () => ({
                compare: (actual, expected) => {
                    return actual.toJSON() === expected.toJSON();
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
        store.dispatch(t.newDecomposeExpressionAction(
            newExprPath(0, list('body')), newCanvasPoint(25, 25)
        ));
        assertExpression(0, 'L x[L y[_]]', 50, 50);
        assertExpression(1, 'L z[x]', 25, 25);
    });

    it('handles extract arg', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[+(2)(x)]')));
        store.dispatch(t.newDecomposeExpressionAction(
            newExprPath(0, list('body', 'func')), newCanvasPoint(25, 25)
        ));
        assertExpression(0, 'L x[+(x)]', 50, 50);
        assertExpression(1, '2', 25, 25);
    });

    it('handles insert body', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[L y[_]]')));
        store.dispatch(t.newAddExpression(makeScreenExpr('x(y)')));
        store.dispatch(
            t.newInsertAsBody(1, newExprPath(0, list('body'))));
        assertExpression(0, 'L x[L y[x(y)]]', 50, 50);
        // The other expression should have been removed.
        expect(store.getState().screenExpressions.size).toEqual(1);
    });

    it('handles insert arg', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[x(y)]')));
        store.dispatch(t.newAddExpression(makeScreenExpr('FOO')));
        store.dispatch(
            t.newInsertAsArg(1, newExprPath(0, list('body', 'arg'))));
        assertExpression(0, 'L x[x(y(FOO))]', 50, 50);
        // The other expression should have been removed.
        expect(store.getState().screenExpressions.size).toEqual(1);
    });

    it('evaluates expressions', () => {
        store.dispatch(t.newReset());
        store.dispatch(t.newAddExpression(makeScreenExpr('L x[L y[x]](y)')));
        store.dispatch(t.newEvaluateExpression(0, newCanvasPoint(25, 25)));
        assertExpression(0, 'L x[L y[x]](y)', 50, 50);
        assertExpression(1, "L y'[y]", 25, 25);
    });

    const assertExpression = (exprId, exprString, canvasX, canvasY) => {
        expect(store.getState().screenExpressions.get(exprId).toJS()).toEqual(
            newScreenExpression(parseExpr(exprString),
                newCanvasPoint(canvasX, canvasY)).toJS());
    };

    const makeScreenExpr = (exprString) => {
        return newScreenExpression(
            parseExpr(exprString), newCanvasPoint(50, 50));
    };
});