package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressionParser;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class DefinitionManagerImpl implements DefinitionManager {
    private final Map<String, Expression> definitionMap =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<String, UserExpression> userDefinitionMap =
            Collections.synchronizedMap(new HashMap<>());
    private final SetMultimap<Expression, String> namesByExpression =
            Multimaps.synchronizedSetMultimap(HashMultimap.create());

    public DefinitionManagerImpl() {
    }

    public static DefinitionManager createWithDefaults() {
        DefinitionManagerImpl result = new DefinitionManagerImpl();
        result.updateDefinition("+", UserExpressionParser.parse("L n[L m[L s[L z[n(s)(m(s)(z))]]]]"));
        result.updateDefinition("TRUE", UserExpressionParser.parse("L t[L f[t]]"));
        result.updateDefinition("FALSE", UserExpressionParser.parse("L t[L f[f]]"));

        for (int i = 0; i < 20; i++) {
            UserExpression body = UserVariable.create("z");
            for (int j = 0; j < i; j++) {
                body = UserFuncCall.create(UserVariable.create("s"), body);
            }
            result.updateDefinition(
                    Integer.toString(i),
                    UserLambda.create(
                            "s",
                            UserLambda.create(
                                    "z",
                                    body
                            )
                    )
            );
        }
        return result;
    }

    @Override
    public @Nullable UserExpression getUserDefinition(String definitionName) {
        return userDefinitionMap.get(definitionName);
    }

    @Override
    public String tryResolveExpression(Expression expression) {
        Set<String> names = namesByExpression.get(expression);
        if (names.isEmpty()) {
            return null;
        } else {
            // TODO: Maybe do something smarter here.
            return names.iterator().next();
        }
    }

    @Override
    public void updateDefinition(String name, @Nullable UserExpression userExpression) {
        // Get rid of the old definition first. This makes it so any circular definitions will be
        // seen as invalid rather than using the stale definition.
        removeDefinition(name);
        Expression expression;
        try {
            expression = toExpression(userExpression);
        } catch (InvalidExpressionException e) {
            expression = null;
        }
        addDefinition(name, userExpression, expression);
    }

    private void removeDefinition(String name) {
        userDefinitionMap.remove(name);
        Expression oldExpression = definitionMap.remove(name);
        namesByExpression.remove(oldExpression, name);
    }

    private void addDefinition(String name, @Nullable UserExpression userExpression,
            @Nullable Expression expression) {
        userDefinitionMap.put(name, userExpression);
        definitionMap.put(name, expression);
        namesByExpression.put(expression, name);
    }

    /**
     * Given a UserExpression, convert to an Expression if possible.
     * <p>
     * throws InvalidExpressionException if there was a problem.
     */
    @Override
    public Expression toExpression(@Nullable UserExpression e) throws InvalidExpressionException {
        if (e == null) {
            throw new InvalidExpressionException();
        }
        return e.visit(
                lambda -> {
                    if (lambda.body() == null) {
                        throw new InvalidExpressionException();
                    }
                    return Lambda.create(lambda.varName(), toExpression(lambda.body()));
                },
                funcCall -> FuncCall.create(toExpression(funcCall.func()), toExpression(funcCall.arg())),
                variable -> Variable.create(variable.varName()),
                reference -> {
                    Expression expression = definitionMap.get(reference.defName());
                    if (expression == null) {
                        throw new InvalidExpressionException();
                    }
                    return expression;
                }
        );
    }

}
