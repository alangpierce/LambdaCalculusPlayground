package com.alangpierce.lambdacalculusplayground.evaluator;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EvalBoundVariable implements EvalExpression {
    public abstract Slot slot();

    public static EvalBoundVariable create(Slot slot) {
        return new AutoValue_EvalBoundVariable(slot);
    }

    @Override
    public <T> T visit(Visitor<EvalLambda, T> lambdaVisitor,
            Visitor<EvalFuncCall, T> funcCallVisitor,
            Visitor<EvalBoundVariable, T> boundVariableVisitor,
            Visitor<EvalUnboundVariable, T> unboundVariableVisitor,
            Visitor<EvalFreeVariable, T> freeVariableVisitor) {
        return boundVariableVisitor.accept(this);
    }

    @Override
    public String toString() {
        return "{Slot}";
    }
}
