/**
 * @flow
 */

import type {
    ExprPath,
    PathComponent,
} from './types'
import * as t from './types'
import {IList} from './types-collections'

export const emptyIdPath = (exprId: number): ExprPath => {
    return t.ExprPath.make(t.ExprIdContainer.make(exprId), IList.make());
};

export const emptyDefinitionPath = (defName: string): ExprPath => {
    return t.ExprPath.make(
        t.DefinitionContainer.make(defName), IList.make());
};

export const step = (path: ExprPath, component: PathComponent): ExprPath => {
    return path.updatePathSteps(steps => steps.push(component));
};