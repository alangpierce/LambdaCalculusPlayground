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
    return t.ScreenPoint.make(canvasPt.canvasX, canvasPt.canvasY);
};

export const screenPtToCanvasPt = (screenPt: ScreenPoint): CanvasPoint => {
    return t.CanvasPoint.make(screenPt.screenX, screenPt.screenY);
};