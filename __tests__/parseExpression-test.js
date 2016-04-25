/**
 * @flow
 */

jest.disableAutomock();

import parseExpression from '../parseExpression'
import * as t from '../types'

describe('parseExpression', () => {
    it('parses basic lambdas', () => {
        expectExprsEqual(
            parseExpression('L x[x]'),
            t.newUserLambda('x', t.newUserVariable('x')));
    });

    it('handles ref capitalization', () => {
        expectExprsEqual(
            parseExpression('FOO'),
            t.newUserReference('FOO'));
    });

    it('parses function calls', () => {
        expectExprsEqual(
            parseExpression('L x[L y[x(y(y))]]'),
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
            parseExpression('L x[+(x)(y)]'),
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
        expect(() => parseExpression('[[')).toThrow();
    });

    it('allows internal whitespace', () => {
        expectExprsEqual(
            parseExpression(' L   x[   x(y  )]  '),
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