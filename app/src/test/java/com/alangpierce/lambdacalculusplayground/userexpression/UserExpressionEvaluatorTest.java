package com.alangpierce.lambdacalculusplayground.userexpression;

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.definition.DefinitionManagerImpl;
import com.alangpierce.lambdacalculusplayground.evaluator.OptimizedExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.expression.Expression;

import junit.framework.TestCase;

public class UserExpressionEvaluatorTest extends TestCase {
    DefinitionManager definitionManager;
    UserExpressionEvaluatorImpl userExpressionEvaluator;

    @Override
    protected void setUp() throws Exception {
        definitionManager = new DefinitionManagerImpl();
        userExpressionEvaluator = new UserExpressionEvaluatorImpl(
                definitionManager, new OptimizedExpressionEvaluator());

        defineTerm("TRUE", "L t[L f[t]]");
        defineTerm("FALSE", "L t[L f[f]]");
        defineTerm("NOT", "L b[L t[L f[b(f)(t)]]]");

        defineTerm("PAIR", "L x[L y[L b[b(x)(y)]]]");
        defineTerm("LHS", "L p[p(TRUE)]");
        defineTerm("RHS", "L p[p(FALSE)]");

        defineTerm("0", "L s[L z[z]]");
        defineTerm("1", "L s[L z[s(z)]]");
        defineTerm("2", "L s[L z[s(s(z))]]");
        defineTerm("3", "L s[L z[s(s(s(z)))]]");
        defineTerm("4", "L s[L z[s(s(s(s(z))))]]");
        defineTerm("5", "L s[L z[s(s(s(s(s(z)))))]]");
        defineTerm("6", "L s[L z[s(s(s(s(s(s(z))))))]]");
        defineTerm("+", "L n[L m[L s[L z[n(s)(m(s)(z))]]]]");
        defineTerm("*", "L n[L m[L s[L z[n(m(s))(z)]]]]");
        defineTerm("ISZERO", "L n[n(L x[FALSE])(TRUE)]");
        defineTerm("PRED",
                "L n[L s[L z[" +
                        "LHS(" +
                            "n" +
                                "(L x[" +
                                    "PAIR" +
                                        "(RHS(x)(s(LHS(x)))(LHS(x)))" +
                                        "(TRUE)" +
                                "])" +
                                "(PAIR(z)(FALSE))" +
                        ")" +
                "]]]");

        defineTerm("Y", "L f[L x[f(x(x))](L x[f(x(x))])]");

        defineTerm("FACTREC",
                "L f[L n[ISZERO(n)(1)(*(n)(f(PRED(n))))]]");
        defineTerm("FACT", "Y(FACTREC)");

        super.setUp();
    }

    private void assertResult(String expectedResultStr, String exprStr) {
        UserExpression expectedResult = UserExpressionParser.parse(expectedResultStr);
        UserExpression actualResult =
                userExpressionEvaluator.evaluate(UserExpressionParser.parse(exprStr));
        assertEquals(expectedResult, actualResult);
    }

    private void defineTerm(String defName, String exprStr) {
        UserExpression userExpr = UserExpressionParser.parse(exprStr);
        Expression fullExpr = userExpressionEvaluator.convertToExpression(userExpr);
        assertNotNull(fullExpr);
        definitionManager.updateDefinition(defName, fullExpr);
    }

    public void testBooleans() {
        assertResult("FALSE", "NOT(TRUE)");
    }

    public void testMath() {
        assertResult("3", "+(1)(2)");
        assertResult("6", "*(2)(3)");
        assertResult("FALSE", "ISZERO(2)");
        assertResult("TRUE", "ISZERO(0)");
    }

    public void testPair() {
        assertResult("2", "LHS(PAIR(2)(3))");
        assertResult("3", "RHS(PAIR(2)(3))");
    }

    public void testPredecessor() {
        assertResult("0", "PRED(0)");
        assertResult("0", "PRED(1)");
        assertResult("1", "PRED(2)");
        assertResult("2", "PRED(3)");
        assertResult("3", "PRED(4)");
    }

    public void testFact() {
        assertResult("1", "FACTREC(L x[x])(0)");
        assertResult("1", "FACTREC(L x[1])(1)");
        assertResult("2", "FACTREC(L x[1])(2)");
//        assertResult("2", "FACT(2)");
    }
}
