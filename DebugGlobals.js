/**
 * Globals we expose on the window object for manual debugging with the Chrome
 * console.
 *
 * TODO: Consider disabling this for release builds.
 *
 * @flow
 */

import store from './store'
import * as actions from './actions'

window.DebugGlobals = {
    store,
    actions,
};