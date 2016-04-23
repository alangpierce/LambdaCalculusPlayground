/**
 * @flow
 */

jest.disableAutomock();

import * as actions from '../actions'
import parseExpression from '../parseExpression'
import store from '../store'

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
        store.dispatch(actions.moveExpression(0, {
            canvasX: 100,
            canvasY: 100,
        }));
        const screenExpr = store.getState().screenExpressions.get(0);
        expect((screenExpr: any).pos.canvasX).toEqual(100);
    });

    it('handles extract body', () => {
        store.dispatch(actions.reset());
        store.dispatch(
            actions.addExpression(makeScreenExpr('L x[L y[L z[x]]]')));
        store.dispatch(
            actions.extractBody({
                exprId: 0,
                pathSteps: ['body']
            }, {
                canvasX: 25,
                canvasY: 25,
            })
        );
        expect(store.getState().screenExpressions.get(0)).toEqual({
            expr: parseExpression('L x[L y[_]]'),
            pos: {
                canvasX: 50,
                canvasY: 50,
            }
        });
        expect(store.getState().screenExpressions.get(1)).toEqual({
            expr: parseExpression('L z[x]'),
            pos: {
                canvasX: 25,
                canvasY: 25,
            }
        });
    });

    const makeScreenExpr = (exprString) => {
        return {
            expr: parseExpression(exprString),
            pos: {
                canvasX: 50,
                canvasY: 50,
            }
        }
    }
});