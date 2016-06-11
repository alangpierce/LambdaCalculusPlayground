/**
 * @flow
 */

import type {
    ScreenRect
} from './types'
import {
    CanvasPoint,
    PointDifference,
    ScreenPoint
} from './types';
import {
    CanvasPointMixinTemplate,
    PointDifferenceMixinTemplate,
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
    /**
     * Reinterpret the screen point as a difference from the screen origin.
     */
    asDiff(): PointDifference {
        return PointDifference.make(this.screenX, this.screenY);
    }
}

export class CanvasPointMixin extends CanvasPointMixinTemplate {
    plusDiff(diff: PointDifference): CanvasPoint {
        return CanvasPoint.make(
            this.canvasX + diff.dx, this.canvasY + diff.dy);
    }
    minus(other: CanvasPoint): PointDifference {
        return PointDifference.make(
            this.canvasX - other.canvasX, this.canvasY - other.canvasY);
    }
    minusDiff(diff: PointDifference): CanvasPoint {
        return CanvasPoint.make(
            this.canvasX - diff.dx, this.canvasY - diff.dy);
    }
}

export class PointDifferenceMixin extends PointDifferenceMixinTemplate {
    /**
     * Reinterpret the point difference as a screen point (i.e. a difference
     * from the screen origin).
     */
    asScreenPoint(): ScreenPoint {
        return ScreenPoint.make(this.dx, this.dy);
    }
}