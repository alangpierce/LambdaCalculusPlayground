/**
 * @flow
 */

import * as Immutable from 'immutable'

import type {
    ExprPath,
    PathComponent,
} from './types'
import * as t from './types'

export const emptyPath = (exprId: number): ExprPath => {
    return t.newExprPath(exprId, new Immutable.List());
};

export const step = (path: ExprPath, component: PathComponent): ExprPath => {
    return path.updatePathSteps(steps => steps.push(component));
};