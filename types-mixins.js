/**
 * @flow
 */

import type {
    ScreenPoint,
    ScreenRect
} from './types'
import {PointDifference} from './types';

export class ScreenRectMixin {
    containsPoint(point: ScreenPoint): boolean {
        const self: ScreenRect = (this: any);
        return point.onBottomRight(self.topLeft) &&
            self.bottomRight.onBottomRight(point);
    }
    overlapsWith(other: ScreenRect): boolean {
        const self: ScreenRect = (this: any);
        return self.bottomRight.onBottomRight(other.topLeft) &&
                other.bottomRight.onBottomRight(self.topLeft);
    }
    rightSide(): ScreenRect {
        const self: ScreenRect = (this: any);
        return self.lens().topLeft().screenX().replace(self.bottomRight.screenX);
    }
    plusDiff(diff: PointDifference): ScreenRect {
        const self: ScreenRect = (this: any);
        return self
            .updateTopLeft(topLeft => topLeft.plusDiff(diff))
            .updateBottomRight(bottomRight => bottomRight.plusDiff(diff));
    }
}

export class ScreenPointMixin {
    onBottomRight(other: ScreenPoint): boolean {
        const self: ScreenPoint = (this: any);
        return self.screenX >= other.screenX && self.screenY >= other.screenY;
    }
    minus(other: ScreenPoint): PointDifference {
        const self: ScreenPoint = (this: any);
        return PointDifference.make(
            self.screenX - other.screenX, self.screenY - other.screenY);
    }
    plusDiff(diff: PointDifference): ScreenPoint {
        const self: ScreenPoint = (this: any);
        return self
            .updateScreenX(x => x + diff.dx)
            .updateScreenY(y => y + diff.dy);
    }
}