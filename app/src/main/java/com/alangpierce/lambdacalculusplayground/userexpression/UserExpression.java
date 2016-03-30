package com.alangpierce.lambdacalculusplayground.userexpression;

import java.io.Serializable;

/**
 * Syntax tree format similar to the actual lambda calculus, but allowing for certain non-pure
 * features. For example, lambda expression can have missing bodies, which is a useful intermediate
 * step while creating a lambda expression.
 */
public interface UserExpression extends Serializable {
    <T> T visit(
            Visitor<UserLambda, T> lambdaVisitor,
            Visitor<UserFuncCall, T> funcCallVisitor,
            Visitor<UserVariable, T> variableVisitor,
            Visitor<UserReference, T> referenceVisitor);

    interface Visitor<V, R> {
        R accept(V value);
    }
}
