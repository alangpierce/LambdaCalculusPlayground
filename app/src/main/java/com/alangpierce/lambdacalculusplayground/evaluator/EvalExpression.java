package com.alangpierce.lambdacalculusplayground.evaluator;

public interface EvalExpression {
    <T> T visit(
            Visitor<EvalLambda, T> lambdaVisitor,
            Visitor<EvalFuncCall, T> funcCallVisitor,
            Visitor<EvalBoundVariable, T> boundVariableVisitor,
            Visitor<EvalUnboundVariable, T> unboundVariableVisitor,
            Visitor<EvalFreeVariable, T> freeVariableVisitor);

    interface Visitor<V, R> {
        R accept(V value);
    }
}
