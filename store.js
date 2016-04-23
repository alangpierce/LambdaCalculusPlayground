/**
 * @flow
 */

import playgroundApp from './reducers'
import {createStore} from 'redux'

import type {Action} from './actions'
import type {State} from './reducers'

export type Store = {
    dispatch: (action: Action) => void,
    getState: () => State,
}

const store: Store = createStore(playgroundApp);

export default store;