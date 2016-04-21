/**
 * Globals we expose on the window object for manual debugging with the Chrome
 * console.
 *
 * TODO: Consider disabling this for release builds.
 *
 * @flow
 */

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

window.DebugGlobals = {
    store,
    actions,
    makeExpression,
};