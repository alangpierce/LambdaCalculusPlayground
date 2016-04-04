package com.alangpierce.lambdacalculusplayground.evaluator;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EvalFreeVariable implements EvalExpression {
    public abstract String varName();

    public static EvalFreeVariable create(String varName) {
        return new AutoValue_EvalFreeVariable(varName);
    }

    @Override
    public <T> T visit(Visitor<EvalLambda, T> lambdaVisitor,
            Visitor<EvalFuncCall, T> funcCallVisitor,
            Visitor<EvalBoundVariable, T> boundVariableVisitor,
            Visitor<EvalUnboundVariable, T> unboundVariableVisitor,
            Visitor<EvalFreeVariable, T> freeVariableVisitor) {
        return freeVariableVisitor.accept(this);
    }

    @Override
    public String toString() {
        return varName();
    }
}
