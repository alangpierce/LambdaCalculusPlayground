/**
 * @flow
 */

jest.disableAutomock();

import {evaluateUserExpr} from '../UserExpressionEvaluator'
import parseExpression from '../parseExpression'
import * as DefinitionManager from '../DefinitionManager'

describe('evaluateUserExpression', () => {
    it('can evaluate booleans', () => {
        define('TRUE', 'L t[L f[t]]');
        define('FALSE', 'L t[L f[f]]');
        assertResult('TRUE', 'TRUE');
    });

    const define = (defName: string, exprString: string) => {
        DefinitionManager.define(defName, parseExpression(exprString));
    };

    const assertResult = (expectedResultStr: string, exprStr: string) => {
        expect(notNull(evaluateUserExpr(parseExpression(exprStr))).toJS())
            .toEqual(parseExpression(expectedResultStr).toJS());
    };

    const notNull = function<T>(t: ?T): T {
        if (!t) {
            throw new Error('Unexpected null.');
        }
        return t;
    };
});