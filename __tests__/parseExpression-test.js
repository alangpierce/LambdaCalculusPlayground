/**
 * @flow
 */

jest.disableAutomock();

import parseExpression from '../parseExpression'
import * as t from '../types'

describe('parseExpression', () => {
    it('parses basic lambdas', () => {
        expect(parseExpression('L x[x]')).toEqual(
            t.newUserLambda('x', t.newUserVariable('x'))
        );
    });

    it('handles ref capitalization', () => {
        expect(parseExpression('FOO')).toEqual(t.newUserReference('FOO'));
    });

    it('parses function calls', () => {
        expect(parseExpression('L x[L y[x(y(y))]]')).toEqual(
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
            )
        );
    });

    it('treats symbols as references', () => {
        expect(parseExpression('L x[+(x)(y)]')).toEqual(
            t.newUserLambda(
                'x',
                t.newUserFuncCall(
                    t.newUserFuncCall(
                        t.newUserReference('+'),
                        t.newUserVariable('x')
                    ),
                    t.newUserVariable('y')
                )
            )
        );
    });

    it('fails on invalid inputs', () => {
        expect(() => parseExpression('[[')).toThrow();
    })
});