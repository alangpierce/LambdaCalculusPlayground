package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = PlaygroundModule.class)
public interface PlaygroundComponent {
    ExpressionControllerFactory getExpressionControllerFactory();
}
