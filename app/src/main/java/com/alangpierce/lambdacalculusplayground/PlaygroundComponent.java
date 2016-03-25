package com.alangpierce.lambdacalculusplayground;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = PlaygroundModule.class)
public interface PlaygroundComponent {
    TopLevelExpressionManager getTopLevelExpressionManager();
}
