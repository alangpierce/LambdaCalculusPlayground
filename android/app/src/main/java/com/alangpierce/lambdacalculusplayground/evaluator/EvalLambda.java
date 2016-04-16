package com.alangpierce.lambdacalculusplayground.evaluator;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EvalLambda implements EvalExpression {
    public abstract Object varMarker();
    public abstract String originalVarName();
    public abstract EvalExpression body();

    public static EvalLambda create(Object varMarker, String originalVarName, EvalExpression body) {
        return new AutoValue_EvalLambda(varMarker, originalVarName, body);
    }

    @Override
    public <T> T visit(Visitor<EvalLambda, T> lambdaVisitor,
            Visitor<EvalFuncCall, T> funcCallVisitor,
            Visitor<EvalBoundVariable, T> boundVariableVisitor,
            Visitor<EvalUnboundVariable, T> unboundVariableVisitor,
            Visitor<EvalFreeVariable, T> freeVariableVisitor) {
        return lambdaVisitor.accept(this);
    }

    @Override
    public String toString() {
        return "L " + originalVarName() + "[" + body() + "]";
    }
}
