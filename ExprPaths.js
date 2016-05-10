/**
 * @flow
 */

import * as Immutable from 'immutable'

import type {
    ExprPath,
    PathComponent,
} from './types'
import * as t from './types'

export const emptyIdPath = (exprId: number): ExprPath => {
    return t.newExprPath(t.newExprIdContainer(exprId), new Immutable.List());
};

export const emptyDefinitionPath = (defName: string): ExprPath => {
    return t.newExprPath(
        t.newDefinitionContainer(defName), new Immutable.List());
};

export const step = (path: ExprPath, component: PathComponent): ExprPath => {
    return path.updatePathSteps(steps => steps.push(component));
};