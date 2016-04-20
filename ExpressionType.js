/**
 * Type definitions for basic expressions.
 * 
 * @flow
 */

export type LambdaType = {
    type: "lambda",
    varName: string,
    body: ExpressionType,
};

export type FuncCallType = {
    type: "funcCall",
    func: ExpressionType,
    arg: ExpressionType,
};

export type VariableType = {
    type: "variable",
    varName: string,
};

export type ReferenceType = {
    type: "reference",
    defName: string,
};

export type ExpressionType =
    LambdaType | FuncCallType | VariableType | ReferenceType;