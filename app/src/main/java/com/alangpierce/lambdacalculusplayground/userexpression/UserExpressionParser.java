package com.alangpierce.lambdacalculusplayground.userexpression;

import junit.framework.Assert;

public class UserExpressionParser {
    public static UserExpression parse(String str) {
        if (str.endsWith("]")) {
            // Expression like "L x[x(x)]"
            Assert.assertEquals("L ", str.substring(0, 2));
            int openBracketIndex = str.indexOf('[');
            String varName = str.substring(2, openBracketIndex);
            String bodyStr = str.substring(openBracketIndex + 1, str.length() - 1);
            return UserLambda.create(varName, parse(bodyStr));
        } else if (str.endsWith(")")) {
            int level = 1;
            int index = str.length() - 1;
            while (level > 0) {
                index--;
                if (str.charAt(index) == ')') {
                    level++;
                } else if (str.charAt(index) == '(') {
                    level--;
                }
            }
            // Now index is the index of the open-paren character.
            String funcStr = str.substring(0, index);
            String argStr = str.substring(index + 1, str.length() - 1);
            return UserFuncCall.create(parse(funcStr), parse(argStr));
        } else if (str.equals(str.toUpperCase())) {
            assertNoBrackets(str);
            return UserReference.create(str);
        } else {
            assertNoBrackets(str);
            return UserVariable.create(str);
        }
    }

    private static void assertNoBrackets(String str) {
        String message = "Unexpected string " + str;
        Assert.assertFalse(message, str.contains("("));
        Assert.assertFalse(message, str.contains(")"));
        Assert.assertFalse(message, str.contains("["));
        Assert.assertFalse(message, str.contains("]"));
    }
}
