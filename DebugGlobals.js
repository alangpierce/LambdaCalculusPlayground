/**
 * Globals we expose on the window object for manual debugging with the Chrome
 * console.
 *
 * @flow
 */

import {NativeModules} from 'react-native'

import * as actions from './actions'
import store from './store'
import parseExpression from './parseExpression'

/**
 * Parse the given expression text and place the expression on the screen.
 */
const makeExpression = (exprString: string) => {
    const userExpr = parseExpression(exprString);
    const screenExpr = {
        expr: userExpr,
        x: 100,
        y: 100,
    };
    store.dispatch(actions.addExpression(screenExpr));
};

// Only run when we're running in Chrome. We detect this by checking if we're in
// the debuggerWorker.js web worker, which defines a messageHandlers global.
if (__DEV__ && window.messageHandlers !== undefined) {
    window.DebugGlobals = {
        store,
        actions,
        makeExpression,
    };

    // Set a callback to be run regularly. This ensures that pending calls to
    // native code are flushed in a relatively timely manner, so we can run
    // things like redux actions from the Chrome console and have them take
    // effect immediately.
    setInterval(() => {}, 100);

    console.log(
        'Created debug globals. Access them on the DebugGlobals object.');
}