/**
 * @flow
 */

import {applyMiddleware, createStore} from 'redux'

import playgroundApp from './reducers'
import * as t from './types'
import {serializeActionsMiddleware} from './types-lib'

export type Store = {
    dispatch: (action: t.Action) => void,
    getState: () => t.State,
}

const store: Store = createStore(
    playgroundApp,
    applyMiddleware(serializeActionsMiddleware)
);

export default store;