/**
 * @flow
 */
import type {PointDifference, ScreenPoint, ScreenRect} from './types';

import * as t from './types'

const onBottomRight = (p1: ScreenPoint, p2: ScreenPoint): bool => {
    return p1.screenX >= p2.screenX && p1.screenY >= p2.screenY;
};

export const ptInRect = (point: ScreenPoint, rect: ScreenRect): bool => {
    return onBottomRight(point, rect.topLeft) &&
        onBottomRight(rect.bottomRight, point);
};

export const rectsOverlap = (r1: ScreenRect, r2: ScreenRect): bool => {
    return onBottomRight(r1.bottomRight, r2.topLeft) &&
            onBottomRight(r2.bottomRight, r1.topLeft);
};

export const ptMinusPt = (p1: ScreenPoint, p2: ScreenPoint):
        PointDifference => {
    return t.newPointDifference(
        p1.screenX - p2.screenX, p1.screenY - p2.screenY);
};

export const rectPlusDiff = (rect: ScreenRect, diff: PointDifference):
        ScreenRect => {
    return t.newScreenRect(
        ptPlusDiff(rect.topLeft, diff), ptPlusDiff(rect.bottomRight, diff));
};

export const ptPlusDiff = (pt: ScreenPoint, diff: PointDifference): ScreenPoint => {
    return t.newScreenPoint(pt.screenX + diff.dx, pt.screenY + diff.dy);
};
