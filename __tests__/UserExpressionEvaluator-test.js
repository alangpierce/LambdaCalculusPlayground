/**
 * @flow
 */

jest.disableAutomock();

import {evaluateUserExpr} from '../UserExpressionEvaluator'
import parseExpression from '../parseExpression'
import * as DefinitionManager from '../DefinitionManager'

describe('evaluateUserExpression', () => {
    beforeEach(() => {
        define('TRUE', 'L t[L f[t]]');
        define('FALSE', 'L t[L f[f]]');
        define("TRUE", "L t[L f[t]]");
        define("FALSE", "L t[L f[f]]");
        define("NOT", "L b[L t[L f[b(f)(t)]]]");

        define("PAIR", "L x[L y[L b[b(x)(y)]]]");
        define("LHS", "L p[p(TRUE)]");
        define("RHS", "L p[p(FALSE)]");

        define("0", "L s[L z[z]]");
        define("1", "L s[L z[s(z)]]");
        define("2", "L s[L z[s(s(z))]]");
        define("3", "L s[L z[s(s(s(z)))]]");
        define("4", "L s[L z[s(s(s(s(z))))]]");
        define("5", "L s[L z[s(s(s(s(s(z)))))]]");
        define("6", "L s[L z[s(s(s(s(s(s(z))))))]]");
        define("+", "L n[L m[L s[L z[n(s)(m(s)(z))]]]]");
        define("*", "L n[L m[L s[L z[n(m(s))(z)]]]]");
        define("ISZERO", "L n[n(L x[FALSE])(TRUE)]");
        define("PRED", `
            L n[L s[L z[
                LHS(
                    n
                        (L x[
                            PAIR
                                (RHS(x)(s(LHS(x)))(LHS(x)))
                                (TRUE)
                        ])
                        (PAIR(z)(FALSE))
                )
            ]]]`);

        define("Y", "L f[L x[f(x(x))](L x[f(x(x))])]");

        define("FACTREC",
            "L f[L n[ISZERO(n)(1)(*(n)(f(PRED(n))))]]");
        define("FACT", "Y(FACTREC)");
    });

    it('can evaluate booleans', () => {
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