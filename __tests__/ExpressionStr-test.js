/**
 * @flow
 */

jest.disableAutomock();

import {parseExpr} from '../ExpressionStr'
import * as t from '../types'

describe('parseExpr', () => {
    it('parses basic lambdas', () => {
        expectExprsEqual(
            parseExpr('L x[x]'),
            t.newUserLambda('x', t.newUserVariable('x')));
    });

    it('handles ref capitalization', () => {
        expectExprsEqual(
            parseExpr('FOO'),
            t.newUserReference('FOO'));
    });

    it('parses function calls', () => {
        expectExprsEqual(
            parseExpr('L x[L y[x(y(y))]]'),
            t.newUserLambda(
                'x',
                t.newUserLambda(
                    'y',
                    t.newUserFuncCall(
                        t.newUserVariable('x'),
                        t.newUserFuncCall(
                            t.newUserVariable('y'),
                            t.newUserVariable('y')
                        )
                    )
                )
            ));
    });

    it('treats symbols as references', () => {
        expectExprsEqual(
            parseExpr('L x[+(x)(y)]'),
            t.newUserLambda(
                'x',
                t.newUserFuncCall(
                    t.newUserFuncCall(
                        t.newUserReference('+'),
                        t.newUserVariable('x')
                    ),
                    t.newUserVariable('y')
                )
            ));
    });

    it('fails on invalid inputs', () => {
        expect(() => parseExpr('[[')).toThrow();
    });

    it('allows internal whitespace', () => {
        expectExprsEqual(
            parseExpr(' L   x[   x(y  )]  '),
            t.newUserLambda(
                'x',
                t.newUserFuncCall(
                    t.newUserVariable('x'),
                    t.newUserVariable('y'),
                )
            ));
    });

    const expectExprsEqual = (expr1, expr2) => {
        expect(expr1.toJS()).toEqual(expr2.toJS());
    }
});