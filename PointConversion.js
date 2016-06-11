/**
 * @flow
 */
import type {
    CanvasPoint,
    ScreenPoint,
    State,
} from './types'
import * as t from './types'

/**
 * Just an abtraction for now, but when we implement panning this should
 * actually translate the point type.
 */
export const canvasPtToScreenPt = (
        state: State, canvasPt: CanvasPoint): ScreenPoint => {
    return canvasPt.minus(state.panOffset).asScreenPoint();
};

export const screenPtToCanvasPt = (
        state: State, screenPt: ScreenPoint): CanvasPoint => {
    return state.panOffset.plusDiff(screenPt.asDiff());
};