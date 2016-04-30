/**
 * Globals we expose on the window object for manual debugging with the Chrome
 * console.
 *
 * @flow
 */

import {NativeModules} from 'react-native'
import * as Immutable from 'immutable'

import store from './store'
import {parseExpr, formatExpr} from './ExpressionStr'
import * as ViewTracker from './ViewTracker'

import * as t from './types'

/**
 * Parse the given expression text and place the expression on the screen.
 */
const makeExpression = (exprString: string) => {
    const userExpr = parseExpr(exprString);
    const screenExpr =
        t.newScreenExpression(userExpr, t.newCanvasPoint(100, 100));
    store.dispatch(t.newAddExpression(screenExpr));
};

const listExpressions = () => {
    const state = store.getState();
    state.screenExpressions.forEach((screenExpression, exprId) => {
        const {expr, pos: {canvasX, canvasY}} = screenExpression;
        console.log(
            `Expr ${exprId} at (${canvasX}, ${canvasY}): ${formatExpr(expr)}`);
    });
};

// Only run when we're running in Chrome. We detect this by checking if we're in
// the debuggerWorker.js web worker, which defines a messageHandlers global.
if (__DEV__ && window.messageHandlers !== undefined) {
    const newGlobals = {
        store,
        t,
        makeExpression,
        listExpressions,
        Immutable,
        ViewTracker,
    };

    Object.keys(newGlobals).forEach((name) => {
        if (window[name] === undefined) {
            window[name] = newGlobals[name];
        } else {
            console.error(`The debug global ${name} was already defined!`);
        }
    });

    // Also tack them on DebugGlobals, which makes them a little easier to
    // discover.
    window.DebugGlobals = newGlobals;

    // Set a callback to be run regularly. This ensures that pending calls to
    // native code are flushed in a relatively timely manner, so we can run
    // things like redux  from the Chrome console and have them take
    // effect immediately.
    setInterval(() => {}, 100);

    console.log('Created debug globals.');
}