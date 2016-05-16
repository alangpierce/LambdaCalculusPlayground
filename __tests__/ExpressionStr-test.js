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
            t.UserLambda.make('x', t.UserVariable.make('x')));
    });

    it('handles ref capitalization', () => {
        expectExprsEqual(
            parseExpr('FOO'),
            t.UserReference.make('FOO'));
    });

    it('parses function calls', () => {
        expectExprsEqual(
            parseExpr('L x[L y[x(y(y))]]'),
            t.UserLambda.make(
                'x',
                t.UserLambda.make(
                    'y',
                    t.UserFuncCall.make(
                        t.UserVariable.make('x'),
                        t.UserFuncCall.make(
                            t.UserVariable.make('y'),
                            t.UserVariable.make('y')
                        )
                    )
                )
            ));
    });

    it('treats symbols as references', () => {
        expectExprsEqual(
            parseExpr('L x[+(x)(y)]'),
            t.UserLambda.make(
                'x',
                t.UserFuncCall.make(
                    t.UserFuncCall.make(
                        t.UserReference.make('+'),
                        t.UserVariable.make('x')
                    ),
                    t.UserVariable.make('y')
                )
            ));
    });

    it('fails on invalid inputs', () => {
        expect(() => parseExpr('[[')).toThrow();
    });

    it('allows internal whitespace', () => {
        expectExprsEqual(
            parseExpr(' L   x[   x(y  )]  '),
            t.UserLambda.make(
                'x',
                t.UserFuncCall.make(
                    t.UserVariable.make('x'),
                    t.UserVariable.make('y'),
                )
            ));
    });

    const expectExprsEqual = (expr1, expr2) => {
        expect(expr1.toJS()).toEqual(expr2.toJS());
    }
});