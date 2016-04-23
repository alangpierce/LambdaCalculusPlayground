/**
 * @flow
 */

jest.disableAutomock();

import parseExpression from '../parseExpression'

describe('parseExpression', () => {
    it('parses basic lambdas', () => {
        expect(parseExpression('L x[x]')).toEqual({
            type: 'lambda',
            varName: 'x',
            body: {
                'type': 'variable',
                'varName': 'x',
            }
        });
    });

    it('handles ref capitalization', () => {
        expect(parseExpression('FOO')).toEqual({
            type: 'reference',
            defName: 'FOO',
        });
    });

    it('parses function calls', () => {
        expect(parseExpression('L x[L y[x(y(y))]]')).toEqual({
            type: 'lambda',
            varName: 'x',
            body: {
                type: 'lambda',
                varName: 'y',
                body: {
                    type: 'funcCall',
                    func: {
                        type: 'variable',
                        varName: 'x',
                    },
                    arg: {
                        type: 'funcCall',
                        func: {
                            type: 'variable',
                            varName: 'y',
                        },
                        arg: {
                            type: 'variable',
                            varName: 'y',
                        },
                    }
                }
            }
        })
    });

    it('treats symbols as references', () => {
        expect(parseExpression('L x[+(x)(y)]')).toEqual({
            type: 'lambda',
            varName: 'x',
            body: {
                type: 'funcCall',
                func: {
                    type: 'funcCall',
                    func: {
                        type: 'reference',
                        defName: '+',
                    },
                    arg: {
                        type: 'variable',
                        varName: 'x',
                    },
                },
                arg: {
                    type: 'variable',
                    varName: 'y',
                },
            },
        })
    });

    it('fails on invalid inputs', () => {
        expect(() => parseExpression('[[')).toThrow();
    })
});