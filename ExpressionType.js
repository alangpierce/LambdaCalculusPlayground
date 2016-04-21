/**
 * Type definitions for basic expressions.
 * 
 * @flow
 */

export type UserLambda = {
    type: "lambda",
    varName: string,
    body: UserExpression,
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
    x: number,
    y: number,
};