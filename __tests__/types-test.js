/**
 * @flow
 */

jest.disableAutomock();

import {buildValueClass} from '../types-lib'

describe('types', () => {
    it('generates types', () => {
        const Point = buildValueClass(['x', 'y']);
        const p = new Point({x: 5, y: 7});
        expect(p.x).toEqual(5);
        expect(p.withX(9).x).toEqual(9);
        expect(p.updateY(y => y - 3).y).toEqual(4);
    });
});