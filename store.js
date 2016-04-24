/**
 * @flow
 */

import playgroundApp from './reducers'
import {createStore} from 'redux'

import * as t from './types'

export type Store = {
    dispatch: (action: t.Action) => void,
    getState: () => t.State,
}

const store: Store = createStore(playgroundApp);

export default store;