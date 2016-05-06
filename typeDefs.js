/**
 * Types to include as types.js.
 */
export default typeDefs = {
    State: {
        type: 'struct',
        fields: {
            canvasExpressions: 'Immutable.Map<number, CanvasExpression>',
            nextExprId: 'number',
            // Map from finger ID to expression ID.
            activeDrags: 'Immutable.Map<number, DragData>',
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
                canvasExpr: 'CanvasExpression',
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
            // TODO: Get rid of this, maybe. Or make namespacing better.
            DecomposeExpressionAction: {
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
                screenPos: 'ScreenPoint',
            },
            FingerMove: {
                fingerId: 'number',
                screenPos: 'ScreenPoint',
            },
            FingerUp: {
                fingerId: 'number',
                screenPos: 'ScreenPoint',
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
    CanvasExpression: {
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
    PointDifference: {
        type: 'struct',
        fields: {
            dx: 'number',
            dy: 'number',
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
    },
    // The action to perform at the start of a drag operation.
    DragResult: {
        type: 'union',
        cases: {
            PickUpExpression: {
                exprId: 'number',
                offset: 'PointDifference',
            },
            DecomposeExpression: {
                exprPath: 'ExprPath',
                offset: 'PointDifference',
                newPos: 'ScreenPoint',
            },
            CreateExpression: {
                expr: 'UserExpression',
                offset: 'PointDifference',
                newPos: 'ScreenPoint',
            },
            StartPan: {
                startPos: 'ScreenPoint',
            }
        },
    },
    DragData: {
        type: 'struct',
        fields: {
            offset: 'PointDifference',
            canvasExpr: 'CanvasExpression',
        },
    },
    // The action performed when dropping.
    DropResult: {
        type: 'union',
        cases: {
            AddToTopLevelResult: {
                canvasExpr: 'CanvasExpression',
            },
            InsertAsBodyResult: {
                lambdaPath: 'ExprPath',
                expr: 'UserExpression',
            },
            InsertAsArgResult: {
                path: 'ExprPath',
                expr: 'UserExpression',
            },
        },
    },
    // An identifier that can be used for any view in the dragging and dropping
    // system. Components identify themselves as having these identifiers, and
    // the rest of the world can work completely in terms of these identifiers
    // when computing things like drop targets.
    ViewKey: {
        type: 'union',
        cases: {
            ExpressionKey: {
                exprPath: 'ExprPath',
            },
            EmptyBodyKey: {
                lambdaPath: 'ExprPath',
            },
            LambdaVarKey: {
                lambdaPath: 'ExprPath',
            }
        }
    }
};