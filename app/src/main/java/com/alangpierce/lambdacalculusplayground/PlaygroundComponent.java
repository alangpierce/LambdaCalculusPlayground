package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.ExpressionViewGenerator.ExpressionViewGeneratorFactory;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = PlaygroundModule.class)
public interface PlaygroundComponent {
    ExpressionViewGeneratorFactory getExpressionViewGeneratorFactory();
}
