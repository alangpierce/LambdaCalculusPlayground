/**
 * @flow
 */

jest.disableAutomock();

import {IList, IMap} from '../types-collections'
import {buildValueClass, deserialize} from '../types-lib'

describe('types', () => {
    it('generates types', () => {
        const Point = buildValueClass('Point', null, ['x', 'y']);
        const p = Point.make(5, 7);
        expect(p.x).toEqual(5);
        expect(p.withX(9).x).toEqual(9);
        expect(p.updateY(y => y - 3).y).toEqual(4);
    });

    it('serializes and deserializes types', () => {
        const Rect = buildValueClass(
            'Rect', null, ['topLeft', 'bottomRight', 'color', 'depth', 'owner']);
        const Point = buildValueClass('Point', null, ['x', 'y']);
        const rect = Rect.make(
            Point.make(5, 7),
            Point.make(10, 12),
            'green',
            3,
            null,
        );
        const serializedRect = rect.serialize();
        expect(serializedRect).toEqual({
            __SERIALIZED_CLASS: 'Rect',
            topLeft: {
                __SERIALIZED_CLASS: 'Point',
                x: 5,
                y: 7,
            },
            bottomRight: {
                __SERIALIZED_CLASS: 'Point',
                x: 10,
                y: 12,
            },
            color: 'green',
            depth: 3,
            owner: null,
        });
        const rect2 = deserialize(serializedRect);
        expect(rect2.color).toEqual('green');
        expect(rect2.owner).toEqual(null);
        expect(rect2.topLeft.y).toEqual(7);
        expect(rect2.bottomRight.updateX(x => x - 2).x).toEqual(8);
    });

    it('handles immutable maps and lists', () => {
        const map = IMap.make({x: 5, y: 7});
        expect(map.get('x')).toEqual(5);
        const map2 = map.lens().atKey('x').update(x => x + 1);
        expect(map2.get('x')).toEqual(6);
        const map3 = map2.lens().deleteKey('x');
        expect(map3.hasKey('x')).toEqual(false);

        const list = IList.make([2, 8, 4]);
        expect(list.get(1)).toEqual(8);
        const list2 = list.lens().atIndex(1).update(val => val + 3);
        expect(list2.get(1)).toEqual(11);

        for (const val of list) {
            expect(val % 2).toEqual(0);
        }
    });

    it('transforms objects using lenses', () => {
        const Rect = buildValueClass(
            'Rect', null, ['topLeft', 'bottomRight']);
        const Point = buildValueClass('Point', null, ['x', 'y']);
        const rect = Rect.make(Point.make(5, 8), Point.make(12, 15));
        const rect2 = rect.lens().topLeft().y().update(y => y + 3);
        expect(rect2.topLeft.y).toEqual(11);
    });
});