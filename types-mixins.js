/**
 * @flow
 */

import type {
    ScreenPoint,
    ScreenRect
} from './types'
import {PointDifference} from './types';
import {
    ScreenRectMixinTemplate,
    ScreenPointMixinTemplate
} from './types-mixin-templates';

export class ScreenRectMixin extends ScreenRectMixinTemplate {
    containsPoint(point: ScreenPoint): boolean {
        return point.onBottomRight(this.topLeft) &&
            this.bottomRight.onBottomRight(point);
    }
    overlapsWith(other: ScreenRect): boolean {
        return this.bottomRight.onBottomRight(other.topLeft) &&
                other.bottomRight.onBottomRight(this.topLeft);
    }
    rightSide(): ScreenRect {
        return this.lens().topLeft().screenX().replace(this.bottomRight.screenX);
    }
    plusDiff(diff: PointDifference): ScreenRect {
        return this
            .updateTopLeft(topLeft => topLeft.plusDiff(diff))
            .updateBottomRight(bottomRight => bottomRight.plusDiff(diff));
    }
}

export class ScreenPointMixin extends ScreenPointMixinTemplate {
    onBottomRight(other: ScreenPoint): boolean {
        return this.screenX >= other.screenX && this.screenY >= other.screenY;
    }
    minus(other: ScreenPoint): PointDifference {
        return PointDifference.make(
            this.screenX - other.screenX, this.screenY - other.screenY);
    }
    plusDiff(diff: PointDifference): ScreenPoint {
        return this
            .updateScreenX(x => x + diff.dx)
            .updateScreenY(y => y + diff.dy);
    }
}