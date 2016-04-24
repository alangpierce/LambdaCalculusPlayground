#!/usr/local/bin/babel-node

import fs from 'fs'

import generateTypes from './generateTypes'

// To make the syntax much cleaner, we abuse the fact that key order is pretty
// much preserved in JS.
const types = {
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