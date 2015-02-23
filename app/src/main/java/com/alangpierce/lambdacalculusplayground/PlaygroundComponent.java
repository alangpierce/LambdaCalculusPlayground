package com.alangpierce.lambdacalculusplayground;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = PlaygroundModule.class)
public interface PlaygroundComponent {
    ExpressionViewGenerator getExpressionViewGenerator();
}
