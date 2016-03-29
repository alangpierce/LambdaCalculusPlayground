package com.alangpierce.lambdacalculusplayground.compat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.MotionEvent;
import android.view.ViewPropertyAnimator;

import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.google.common.base.Throwables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This is an attempt at being disciplined about backward compatibility and making it easier to
 * reason about how the API level affects what we can do. This class should include any usage of the
 * Android SDK that is variable based on the device level. The hope is that this class will include
 * all version checks and also all uses of deprecated functions.
 */
public class Compat {
    public static Drawable getDrawable(Context context, @DrawableRes int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(resId);
        } else {
            return context.getResources().getDrawable(resId);
        }
    }

    // In API level 20, the nativePtr type changed from int to long, so the signature of the private
    // method nativeGetRawAxisValue and the field mNativePtr both changed. But in all SDK versions,
    // the two methods agree on their definition of the native pointer type. That means that we can
    // still just pass mNativePtr in and the types will work, but we can no longer use
    // getDeclaredMethod to look up the nativeGetRawAxisValue method.
    private static final Method NATIVE_GET_RAW_AXIS_VALUE = getNativeGetRawAxisValue();
    private static Method getNativeGetRawAxisValue() {
        for (Method method : MotionEvent.class.getDeclaredMethods()) {
            if (method.getName().equals("nativeGetRawAxisValue")) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new RuntimeException("nativeGetRawAxisValue method not found.");
    }

    /**
     * Get raw screen coordinates for an event. We need to use raw screen coordinates because the
     * drag handle (the origin point of our move operation) may or may not move as we drag.
     *
     * The API doesn't provide this, so we need to compute it more directly:
     * http://stackoverflow.com/questions/6517494/get-motionevent-getrawx-getrawy-of-other-pointers
     */
    public static ScreenPoint getRawCoords(MotionEvent event, int pointerIndex) {
        try {
            Field nativePtrField = MotionEvent.class.getDeclaredField("mNativePtr");
            Field historyCurrentField = MotionEvent.class.getDeclaredField("HISTORY_CURRENT");
            nativePtrField.setAccessible(true);
            historyCurrentField.setAccessible(true);

            float x = (float) NATIVE_GET_RAW_AXIS_VALUE.invoke(null, nativePtrField.get(event),
                    MotionEvent.AXIS_X, pointerIndex, historyCurrentField.get(null));
            float y = (float) NATIVE_GET_RAW_AXIS_VALUE.invoke(null, nativePtrField.get(event),
                    MotionEvent.AXIS_Y, pointerIndex, historyCurrentField.get(null));
            return ScreenPoint.create((int)x, (int)y);
        } catch (Exception e) {
            // Just catch Exception because Android Studio warns about compatibility issues with
            // multi-catch on the various specific exceptions or ReflectiveOperationException..
            // TODO: Catch a more specific exception when we require API level 19.
            throw Throwables.propagate(e);
        }
    }

    public static void translationZBy(ViewPropertyAnimator animator, float value) {
        // When we don't support elevation, just don't do anything with it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.translationZBy(value);
        }
    }
}
