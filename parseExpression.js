/**
 * Parse string expressions like "L x[L y[x(y)]]" into a UserExpression.
 *
 * This isn't used in the app itself, but is useful for testing and development
 * purposes.
 *
 * @flow
 */

import {
    newUserLambda,
    newUserFuncCall,
    newUserVariable,
    newUserReference
} from './types'
import type {UserExpression} from './types'

const parseExpression = (str: string): UserExpression => {
    if (str.endsWith(']')) {
        // Expression like "L x[x(x)]"
        assertSame('L ', str.substring(0, 2));
        const openBracketIndex = str.indexOf('[');
        const varName = str.substring(2, openBracketIndex);
        const bodyStr = str.substring(openBracketIndex + 1, str.length - 1);
        let body;
        if (bodyStr == '_') {
            body = null;
        } else {
            body = parseExpression(bodyStr);
        }
        return newUserLambda(varName, body);
    } else if (str.endsWith(")")) {
        let level = 1;
        let index = str.length - 1;
        while (level > 0) {
            index--;
            if (str.charAt(index) == ')') {
                level++;
            } else if (str.charAt(index) == '(') {
                level--;
            }
        }
        // Now index is the index of the open-paren character.
        const funcStr = str.substring(0, index);
        const argStr = str.substring(index + 1, str.length - 1);
        return newUserFuncCall(
            parseExpression(funcStr), parseExpression(argStr));
    } else if (str === str.toUpperCase()) {
        assertNoBrackets(str);
        return newUserReference(str);
    } else {
        assertNoBrackets(str);
        return newUserVariable(str);
    }
};

const assertNoBrackets = (str: string) => {
    const message = "Unexpected string " + str;
    assert(str.indexOf("(") === -1, message);
    assert(str.indexOf(")") === -1, message);
    assert(str.indexOf("[") === -1, message);
    assert(str.indexOf("]") === -1, message);
};

const assertSame = (val1: any, val2: any) => {
    assert(val1 === val2, `Expected ${val1} got ${val2}.`);
};

const assert = (condition: bool, message: string) => {
    if (!condition) {
        throw new Error(message || 'Assertion failed.');
    }
};

export default parseExpression;