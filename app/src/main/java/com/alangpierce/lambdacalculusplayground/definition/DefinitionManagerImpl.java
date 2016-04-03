package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
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
    private final SetMultimap<Expression, String> namesByExpression =
            Multimaps.synchronizedSetMultimap(HashMultimap.create());


    public DefinitionManagerImpl() {
        updateDefinition("+",
                Lambda.create("n",
                        Lambda.create("m",
                                Lambda.create("s",
                                        Lambda.create("z",
                                                FuncCall.create(
                                                        FuncCall.create(
                                                                Variable.create("n"),
                                                                Variable.create("s")
                                                        ),
                                                        FuncCall.create(
                                                                FuncCall.create(
                                                                        Variable.create("m"),
                                                                        Variable.create("s")
                                                                ),
                                                                Variable.create("z")
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        updateDefinition("TRUE",
                Lambda.create("t",
                        Lambda.create("f",
                                Variable.create("t")
                        )
                )
        );

        updateDefinition("FALSE",
                Lambda.create("t",
                        Lambda.create("f",
                                Variable.create("f")
                        )
                )
        );

        for (int i = 0; i < 20; i++) {
            Expression body = Variable.create("z");
            for (int j = 0; j < i; j++) {
                body = FuncCall.create(Variable.create("s"), body);
            }
            updateDefinition(
                    Integer.toString(i),
                    Lambda.create(
                            "s",
                            Lambda.create(
                                    "z",
                                    body
                            )
                    )
            );
        }
    }

    @Override
    public @Nullable Expression resolveDefinition(String definitionName) {
        return definitionMap.get(definitionName);
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
    public void updateDefinition(String name, @Nullable Expression expression) {
        Expression oldExpression = definitionMap.put(name, expression);
        namesByExpression.remove(oldExpression, name);
        namesByExpression.put(expression, name);
    }
}
