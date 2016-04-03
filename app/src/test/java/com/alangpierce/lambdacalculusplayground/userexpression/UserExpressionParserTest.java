package com.alangpierce.lambdacalculusplayground.userexpression;

import junit.framework.TestCase;

public class UserExpressionParserTest extends TestCase {
    public void testBasicParsing() {
        assertEquals(
                UserLambda.create(
                        "x",
                        UserFuncCall.create(
                                UserFuncCall.create(
                                        UserReference.create("+"),
                                        UserVariable.create("x")
                                ),
                                UserVariable.create("y")
                        )
                ),
                UserExpressionParser.parse("L x[+(x)(y)]")
        );
    }
}
