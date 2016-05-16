/**
 * @flow
 */

jest.disableAutomock();

import {formatExpr, parseExpr} from '../ExpressionStr';
import store from '../store';
import * as t from '../types';
import {CanvasExpression, CanvasPoint} from '../types';
import type {PathComponent} from '../types';
import {IList} from '../types-collections';

// Just use a normal function lambda for now so that arguments works.
const list = function(...args): IList<PathComponent> {
    return IList.make(args);
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
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[x]')));
        const state = store.getState();
        expect(state.nextExprId).toEqual(1);
        expect((state: any).canvasExpressions.get(0).expr.varName).toEqual('x');
    });

    it('handles moving expressions', () => {
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[x]')));
        store.dispatch(t.MoveExpression.make(0, CanvasPoint.make(100, 100)));
        const canvasExpr = store.getState().canvasExpressions.get(0);
        expect((canvasExpr: any).pos.canvasX).toEqual(100);
    });

    it('handles extract body', () => {
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[L y[L z[x]]]')));
        store.dispatch(t.DecomposeExpressionAction.make(
            makeIdPath(0, list('body')), CanvasPoint.make(25, 25)
        ));
        assertExpression(0, 'L x[L y[_]]', 50, 50);
        assertExpression(1, 'L z[x]', 25, 25);
    });

    it('handles extract arg', () => {
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[+(2)(x)]')));
        store.dispatch(t.DecomposeExpressionAction.make(
            makeIdPath(0, list('body', 'func')), CanvasPoint.make(25, 25)
        ));
        assertExpression(0, 'L x[+(x)]', 50, 50);
        assertExpression(1, '2', 25, 25);
    });

    it('handles insert body', () => {
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[L y[_]]')));
        store.dispatch(t.AddExpression.make(makeCanvasExpr('x(y)')));
        store.dispatch(
            t.InsertAsBody.make(1, makeIdPath(0, list('body'))));
        assertExpression(0, 'L x[L y[x(y)]]', 50, 50);
        // The other expression should have been removed.
        expect(store.getState().canvasExpressions.size).toEqual(1);
    });

    it('handles insert arg', () => {
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[x(y)]')));
        store.dispatch(t.AddExpression.make(makeCanvasExpr('FOO')));
        store.dispatch(
            t.InsertAsArg.make(1, makeIdPath(0, list('body', 'arg'))));
        assertExpression(0, 'L x[x(y(FOO))]', 50, 50);
        // The other expression should have been removed.
        expect(store.getState().canvasExpressions.size).toEqual(1);
    });

    it('evaluates expressions', () => {
        store.dispatch(t.Reset.make());
        store.dispatch(t.AddExpression.make(makeCanvasExpr('L x[L y[x]](y)')));
        store.dispatch(t.EvaluateExpression.make(0, CanvasPoint.make(25, 25)));
        assertExpression(0, 'L x[L y[x]](y)', 50, 50);
        assertPendingExpression(1, "L y'[y]", 25, 25);
    });

    const assertExpression = (exprId, exprString, canvasX, canvasY) => {
        expect(store.getState().canvasExpressions.get(exprId).serialize()).toEqual(
            CanvasExpression.make(parseExpr(exprString),
                CanvasPoint.make(canvasX, canvasY)).serialize());
    };

    const assertPendingExpression = (exprId, exprString, canvasX, canvasY) => {
        expect(store.getState().pendingResults.get(exprId).serialize()).toEqual(
            t.PendingResult.make(parseExpr(exprString), 0).serialize());
    };

    const makeIdPath = (exprId, steps) => {
        return t.ExprPath.make(t.ExprIdContainer.make(exprId), steps);
    };

    const makeCanvasExpr = (exprString) => {
        return CanvasExpression.make(
            parseExpr(exprString), CanvasPoint.make(50, 50));
    };
});