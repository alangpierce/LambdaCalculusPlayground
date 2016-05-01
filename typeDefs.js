/**
 * Types to include as types.js.
 */
export default typeDefs = {
    State: {
        type: 'struct',
        fields: {
            screenExpressions: 'Immutable.Map<number, ScreenExpression>',
            nextExprId: 'number',
        },
    },
    Action: {
        // Redux complains if the don't use plain objects for actions.
        type: 'objectUnion',
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
             * Given an expression path, which must reference either a lambda
             * with a body or a function call, remove the body or the call
             * argument, and create a new expression from it at the given
             * coordinates.
             */
            DecomposeExpression: {
                path: 'ExprPath',
                targetPos: 'CanvasPoint',
            },
            InsertAsArg: {
                argExprId: 'number',
                path: 'ExprPath',
            },
            InsertAsBody: {
                bodyExprId: 'number',
                path: 'ExprPath',
            },
            /**
             * If the given expression can be evaluated, evaluate it and place
             * the result as a new expression in the given position.
             */
            EvaluateExpression: {
                exprId: 'number',
                targetPos: 'CanvasPoint',
            },
            FingerDown: {
                fingerId: 'number',
                pos: 'ScreenPoint',
            },
            FingerMove: {
                fingerId: 'number',
                pos: 'ScreenPoint',
            },
            FingerUp: {
                fingerId: 'number',
                pos: 'ScreenPoint',
            },
        },
    },
    Expression: {
        type: 'union',
        cases: {
            Lambda: {
                varName: 'string',
                body: 'Expression',
            },
            FuncCall: {
                func: 'Expression',
                arg: 'Expression',
            },
            Variable: {
                varName: 'string',
            }
        }
    },
    // Slots are mutable, so we just use a plain object definition.
    Slot: {
        type: 'literal',
        value: `{
    isValue: boolean,
    expr: EvalExpression,
    originalVarName: string
}`,
    },
    VarMarker: {
        type: 'literal',
        value: 'number',
    },
    EvalExpression: {
        type: 'union',
        cases: {
            EvalLambda: {
                varMarker: 'VarMarker',
                originalVarName: 'string',
                body: 'EvalExpression',
            },
            EvalFuncCall: {
                func: 'EvalExpression',
                arg: 'EvalExpression',
            },
            EvalBoundVariable: {
                slot: 'Slot',
            },
            EvalUnboundVariable: {
                varMarker: 'VarMarker',
                originalVarName: 'string',
            },
            EvalFreeVariable: {
                varName: 'string',
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
    ScreenPoint: {
        type: 'struct',
        fields: {
            screenX: 'number',
            screenY: 'number',
        },
    },
    ScreenRect: {
        type: 'struct',
        fields: {
            topLeft: 'ScreenPoint',
            bottomRight: 'ScreenPoint',
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
            pathSteps: 'Immutable.List<PathComponent>',
        }
    }
};