/**
 * Types to include as types.js.
 */
export default typeDefs = {
    State: {
        type: 'struct',
        fields: {
            canvasExpressions: 'IMap<number, CanvasExpression>',
            nextExprId: 'number',
            canvasDefinitions: 'IMap<string, CanvasPoint>',
            definitions: 'IMap<string, ?UserExpression>',
            // Evaluated expressions that haven't been measured yet. We need to
            // measure them before we know where to place them.
            pendingResults: 'IMap<number, PendingResult>',
            // Map from finger ID to expression ID.
            activeDrags: 'IMap<number, DragData>',
            highlightedExprs: 'ISet<ExprPath>',
            // Set of lambda expressions where the body should be highlighted.
            highlightedEmptyBodies: 'ISet<ExprPath>',
            // Set of definition names where the definition body should be
            // highlighted.
            highlightedDefinitionBodies: 'ISet<string>',
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
                canvasExpr: 'CanvasExpression',
            },
            // Create the given definition, or
            PlaceDefinition: {
                defName: 'string',
                screenPos: 'ScreenPoint',
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
            EvaluateExpression: {
                exprId: 'number',
            },
            PlacePendingResult: {
                exprId: 'number',
                width: 'number',
                height: 'number',
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
        },
    },
    CanvasExpression: {
        type: 'struct',
        fields: {
            expr: 'UserExpression',
            pos: 'CanvasPoint',
        },
    },
    PendingResult: {
        type: 'struct',
        fields: {
            expr: 'UserExpression',
            sourceExprId: 'number',
        }
    },
    // The state sent to React land to actually render. Ideally, any interesting
    // logic should happen before this stage rather than in React components.
    DisplayState: {
        type: 'struct',
        fields: {
            screenExpressions: 'IList<ScreenExpression>',
            screenDefinitions: 'IList<ScreenDefinition>',
            measureRequests: 'IList<MeasureRequest>',
        },
    },
    MeasureRequest: {
        type: 'struct',
        fields: {
            expr: 'DisplayExpression',
            resultHandler: '(width: number, height: number) => void',
        },
    },
    ScreenDefinition: {
        type: 'struct',
        fields: {
            defName: 'string',
            expr: '?DisplayExpression',
            pos: 'ScreenPoint',
            defKey: '?DefinitionKey',
            refKey: '?DefinitionRefKey',
            emptyBodyKey: '?DefinitionEmptyBodyKey',
            shouldHighlightEmptyBody: 'boolean',
            // A long-lived expression key to use as the React key.
            key: 'string',
            isDragging: 'boolean',
        },
    },
    ScreenExpression: {
        type: 'struct',
        fields: {
            expr: 'DisplayExpression',
            pos: 'ScreenPoint',
            // A long-lived expression key to use as the React key.
            key: 'string',
            isDragging: 'boolean',
            // If null, don't show an execute button.
            executeHandler: '?() => void',
        },
    },
    DisplayExpression: {
        type: 'union',
        cases: {
            DisplayLambda: {
                exprKey: '?ExpressionKey',
                shouldHighlight: 'boolean',
                varKey: '?LambdaVarKey',
                emptyBodyKey: '?EmptyBodyKey',
                shouldHighlightEmptyBody: 'boolean',
                varName: 'string',
                body: '?DisplayExpression',
            },
            DisplayFuncCall: {
                exprKey: '?ExpressionKey',
                shouldHighlight: 'boolean',
                func: 'DisplayExpression',
                arg: 'DisplayExpression',
            },
            DisplayVariable: {
                exprKey: '?ExpressionKey',
                shouldHighlight: 'boolean',
                varName: 'string',
            },
            DisplayReference: {
                exprKey: '?ExpressionKey',
                shouldHighlight: 'boolean',
                shouldShowError: 'boolean',
                defName: 'string',
            }
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
        mixinClass: 'ScreenPointMixin',
        fields: {
            screenX: 'number',
            screenY: 'number',
        },
    },
    ScreenRect: {
        type: 'struct',
        mixinClass: 'ScreenRectMixin',
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
            container: 'ExprContainer',
            pathSteps: 'IList<PathComponent>',
        }
    },
    // A possible starting point for an expression.
    ExprContainer: {
        type: 'union',
        cases: {
            ExprIdContainer: {
                exprId: 'number',
            },
            DefinitionContainer: {
                defName: 'string',
            },
        },
    },
    // The action to perform at the start of a drag operation.
    DragResult: {
        type: 'union',
        cases: {
            PickUpExpression: {
                exprId: 'number',
                offset: 'PointDifference',
                screenRect: 'ScreenRect',
            },
            PickUpDefinition: {
                defName: 'string',
                offset: 'PointDifference',
                screenRect: 'ScreenRect',
            },
            ExtractDefinition: {
                defName: 'string',
                offset: 'PointDifference',
                screenRect: 'ScreenRect',
            },
            DecomposeExpression: {
                exprPath: 'ExprPath',
                offset: 'PointDifference',
                screenRect: 'ScreenRect',
            },
            CreateExpression: {
                expr: 'UserExpression',
                offset: 'PointDifference',
                screenRect: 'ScreenRect',
            },
            StartPan: {
                startPos: 'ScreenPoint',
            }
        },
    },
    DragData: {
        type: 'struct',
        fields: {
            payload: 'DragPayload',
            // The coordinates of the grab position relative to the top-left of
            // the screen rectangle.
            grabOffset: 'PointDifference',
            // The position of the dragged object on the screen.
            screenRect: 'ScreenRect',
        },
    },
    DragPayload: {
        type: 'union',
        cases: {
            DraggedExpression: {
                userExpr: 'UserExpression',
            },
            DraggedDefinition: {
                defName: 'string',
            },
        },
    },
    // The action performed when dropping.
    DropResult: {
        type: 'union',
        cases: {
            AddToTopLevelResult: {
                payload: 'DragPayload',
                screenPos: 'ScreenPoint',
            },
            InsertAsBodyResult: {
                lambdaPath: 'ExprPath',
                expr: 'UserExpression',
            },
            InsertAsArgResult: {
                path: 'ExprPath',
                expr: 'UserExpression',
            },
            InsertAsDefinitionResult: {
                defName: 'string',
                expr: 'UserExpression',
            },
            RemoveResult: {},
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
            },
            DefinitionKey: {
                defName: 'string',
            },
            DefinitionRefKey: {
                defName: 'string',
            },
            DefinitionEmptyBodyKey: {
                defName: 'string',
            }
        }
    }
};