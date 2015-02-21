package com.alangpierce.lambdacalculusplayground.expression;

import junit.framework.TestCase;

public class ExpressionTest extends TestCase {
    public void testStepExpression() {
        Expression identity = new Lambda("x", new Variable("x"));
        Expression result = Expressions.step(new FuncCall(identity, new Variable("y")));
        assertEquals(new Variable("y"), result);
    }
}
