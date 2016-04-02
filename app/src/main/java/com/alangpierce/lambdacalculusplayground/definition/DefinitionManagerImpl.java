package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

public class DefinitionManagerImpl implements DefinitionManager {
    private final BiMap<String, Expression> definitionMap = Maps.synchronizedBiMap(HashBiMap.create());

    public DefinitionManagerImpl() {
        definitionMap.put("+",
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

        definitionMap.put("TRUE",
                Lambda.create("t",
                        Lambda.create("f",
                                Variable.create("t")
                        )
                )
        );

        definitionMap.put("FALSE",
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
            definitionMap.put(
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
    public Expression resolveDefinition(String definitionName) {
        Expression result = definitionMap.get(definitionName);
        if (result != null) {
            return result;
        } else {
            throw new UnsupportedOperationException(
                    "Definition " + definitionName + " not supported.");
        }
    }

    @Override
    public String tryResolveExpression(Expression expression) {
        return definitionMap.inverse().get(expression);
    }
}
