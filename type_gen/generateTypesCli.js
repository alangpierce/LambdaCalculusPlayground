#!/usr/local/bin/babel-node

import fs from 'fs'

import generateTypes from './generateTypes'

// To make the syntax much cleaner, we abuse the fact that key order is pretty
// much preserved in JS.
const types = {
    State: {
        type: 'struct',
        fields: {
            screenExpressions: 'Immutable.Map<number, ScreenExpression>',
            nextExprId: 'number',
        },
    },
    Action: {
        type: 'union',
        cases: {
            /**
             * Clear the state. Useful for testing.
             */
            Reset: {},
            /**
             * Create a new expression at the given position.
             */
            AddExpression: {
                screenExpr: 'ScreenExpression',
            },
            /**
             * Move the existing expression on the canvas to a new point.
             */
            MoveExpression: {
                exprId: 'number',
                pos: 'CanvasPoint',
            },
            /**
             * Given an expression path, which must reference a lambda
             * expression with a body, remove that body and create a new
             * expression from it at the given coordinates.
             */
            ExtractBody: {
                path: 'ExprPath',
                targetPos: 'CanvasPoint',
            },
        },
    },
    UserExpression: {
        type: 'union',
        cases: {
            UserLambda: {
                varName: 'string',
                body: '?UserExpression'
            },
            UserFuncCall: {
                func: 'UserExpression',
                arg: 'UserExpression',
            },
            UserVariable: {
                varName: 'string',
            },
            UserReference: {
                defName: 'string',
            },
        }
    },
    ScreenExpression: {
        type: 'struct',
        fields: {
            expr: 'UserExpression',
            pos: 'CanvasPoint',
        },
    },
    CanvasPoint: {
        type: 'struct',
        fields: {
            canvasX: 'number',
            canvasY: 'number',
        },
    },
    PathComponent: {
        type: 'literal',
        value: "'func' | 'arg' | 'body'",
    },
    ExprPath: {
        type: 'struct',
        fields: {
            exprId: 'number',
            pathSteps: 'Array<PathComponent>',
        }
    }
};

const main = () => {
    const typesFileStr = generateTypes(types);
    fs.writeFile('../types.js', typesFileStr, (err) => {
        if (err) {
            console.log('Error: ' + err);
        } else {
            console.log('Done!');
        }
    });
};

main();