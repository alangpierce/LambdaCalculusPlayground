/**
 * @flow
 */
import type {
    CanvasPoint,
    ScreenPoint,
} from './types'
import * as t from './types'

/**
 * Just an abtraction for now, but when we implement panning this should
 * actually translate the point type.
 */
export const canvasPtToScreenPt = (canvasPt: CanvasPoint): ScreenPoint => {
    return t.newScreenPoint(canvasPt.canvasX, canvasPt.canvasY);
};