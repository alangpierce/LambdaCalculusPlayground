package com.alangpierce.lambdacalculusplayground.evaluator;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EvalFuncCall implements EvalExpression {
    public abstract EvalExpression func();
    public abstract EvalExpression arg();

    public static EvalFuncCall create(EvalExpression func, EvalExpression arg) {
        return new AutoValue_EvalFuncCall(func, arg);
    }

    @Override
    public <T> T visit(Visitor<EvalLambda, T> lambdaVisitor,
            Visitor<EvalFuncCall, T> funcCallVisitor,
            Visitor<EvalBoundVariable, T> boundVariableVisitor,
            Visitor<EvalUnboundVariable, T> unboundVariableVisitor,
            Visitor<EvalFreeVariable, T> freeVariableVisitor) {
        return funcCallVisitor.accept(this);
    }

    @Override
    public String toString() {
        return "" + func() + "(" + arg() + ")";
    }
}
