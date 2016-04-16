package com.alangpierce.lambdacalculusplayground.evaluator;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EvalUnboundVariable implements EvalExpression {
    public abstract Object varMarker();
    public abstract String originalVarName();

    public static EvalUnboundVariable create(Object varMarker, String originalVarName) {
        return new AutoValue_EvalUnboundVariable(varMarker, originalVarName);
    }

    @Override
    public <T> T visit(Visitor<EvalLambda, T> lambdaVisitor,
            Visitor<EvalFuncCall, T> funcCallVisitor,
            Visitor<EvalBoundVariable, T> boundVariableVisitor,
            Visitor<EvalUnboundVariable, T> unboundVariableVisitor,
            Visitor<EvalFreeVariable, T> freeVariableVisitor) {
        return unboundVariableVisitor.accept(this);
    }

    @Override
    public String toString() {
        return "<" + originalVarName() + ">";
    }
}
