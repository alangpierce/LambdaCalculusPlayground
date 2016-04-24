/**
 * @flow
 */

import playgroundApp from './reducers'
import {createStore} from 'redux'

import type {State} from './reducers'
import * as t from './types'

export type Store = {
    dispatch: (action: t.Action) => void,
    getState: () => State,
}

const store: Store = createStore(playgroundApp);

export default store;