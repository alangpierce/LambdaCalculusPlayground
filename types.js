/**
 * @flow
 */

export type UserLambda = {
    type: "lambda",
    varName: string,
    body: ?UserExpression,
};

export type UserFuncCall = {
    type: "funcCall",
    func: UserExpression,
    arg: UserExpression,
};

export type UserVariable = {
    type: "variable",
    varName: string,
};

export type UserReference = {
    type: "reference",
    defName: string,
};

export type UserExpression =
    UserLambda | UserFuncCall | UserVariable | UserReference;

export type ScreenExpression = {
    expr: UserExpression,
    pos: CanvasPoint,
};

export type CanvasPoint = {
    canvasX: number,
    canvasY: number,
}

export const canvasPoint = (canvasX: number, canvasY: number): CanvasPoint => ({
    canvasX,
    canvasY,
});

export type PathComponent = 'func' | 'arg' | 'body';

export type ExprPath = {
    exprId: number,
    pathSteps: Array<PathComponent>,
};

export const exprPath =
        (exprId: number, pathSteps: Array<PathComponent>): ExprPath => ({
    exprId,
    pathSteps,
});