package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.AppState;
import com.alangpierce.lambdacalculusplayground.AppStateImpl;
import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressionParser;

import junit.framework.TestCase;

public class DefinitionManagerTest extends TestCase {
    private AppState appState;
    private DefinitionManagerImpl definitionManager;

    @Override
    protected void setUp() throws Exception {
        appState = new AppStateImpl();
        definitionManager = new DefinitionManagerImpl(appState);
        super.setUp();
    }

    private void assertAsNumber(String expected, String expressionStr) {
        UserExpression userExpression = UserExpressionParser.parse(expressionStr);
        Expression expression = definitionManager.toExpression(userExpression);
        assertEquals(expected, ExpressionNumbers.tryResolveToNumber(expression));
    }

    public void testRecognizeNumbers() {
        assertAsNumber("0", "L s[L z[z]]");
        assertAsNumber("3", "L s[L z[s(s(s(z)))]]");
        assertAsNumber(null, "L a[L b[a(a(b))]]");
    }
}
