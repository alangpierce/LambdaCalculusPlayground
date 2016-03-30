package com.alangpierce.lambdacalculusplayground.definition;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.FuncCall;
import com.alangpierce.lambdacalculusplayground.expression.Lambda;
import com.alangpierce.lambdacalculusplayground.expression.Variable;

public class DefinitionManagerImpl implements DefinitionManager {
    @Override
    public Expression resolveDefinition(String definitionName) {
        // TODO: Actually look up the reference. For now it's just the plus function.
        if (definitionName.equals("+")) {
            return Lambda.create("n",
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
            );
        } else {
            throw new UnsupportedOperationException("Definition not supported.");
        }
    }
}
