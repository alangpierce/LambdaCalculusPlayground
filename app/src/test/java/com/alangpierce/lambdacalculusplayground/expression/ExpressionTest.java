package com.alangpierce.lambdacalculusplayground.expression;

import junit.framework.TestCase;

public class ExpressionTest extends TestCase {
    public void testStepExpression() {
        Expression identity = Lambda.create("x", Variable.create("x"));
        Expression result = Expressions.step(FuncCall.create(identity, Variable.create("y")));
        assertEquals(Variable.create("y"), result);
    }
}
