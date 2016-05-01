/**
 * @flow
 */
import type {ScreenPoint, ScreenRect} from './types';

const onBottomRight = (p1: ScreenPoint, p2: ScreenPoint): bool => {
    return p1.screenX >= p2.screenX && p1.screenY >= p2.screenY;
};

export const ptInRect = (point: ScreenPoint, rect: ScreenRect): bool => {
    return onBottomRight(point, rect.topLeft) &&
        onBottomRight(rect.bottomRight, point);
};