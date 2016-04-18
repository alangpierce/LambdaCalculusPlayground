package com.alangpierce.lambdacalculusplayground.reactnative;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.AppState;
import com.alangpierce.lambdacalculusplayground.BuildConfig;
import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.shell.MainReactPackage;

import java.util.Map.Entry;

public class ReactNativeManagerImpl implements ReactNativeManager {
    private final RelativeLayout canvasRoot;
    private final Activity activity;
    private final AppState appState;

    private ReactInstanceManager reactInstanceManager;

    public ReactNativeManagerImpl(RelativeLayout canvasRoot, Activity activity, AppState appState) {
        this.canvasRoot = canvasRoot;
        this.activity = activity;
        this.appState = appState;
    }

    @Override
    public void init() {
        ReactRootView reactRootView = new ReactRootView(activity);
        reactInstanceManager = ReactInstanceManager.builder()
                .setApplication(activity.getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        reactRootView.startReactApplication(reactInstanceManager, "PlaygroundCanvas", null);

        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, screenSize.y / 2);
        layoutParams.topMargin = screenSize.y / 2;
        reactRootView.setLayoutParams(layoutParams);
        reactRootView.setBackgroundColor(Color.GRAY);
        canvasRoot.addView(reactRootView);
    }

    @Override
    public void onResume() {
        reactInstanceManager.onHostResume(activity, () -> {});
    }

    @Override
    public void onPause() {
        reactInstanceManager.onHostPause();
    }

    @Override
    public void showDevOptionsDialog() {
        reactInstanceManager.showDevOptionsDialog();
    }

    @Override
    public void reloadJs() {
        reactInstanceManager.getDevSupportManager().handleReloadJS();
    }

    @Override
    public void invalidateState() {
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
        if (reactContext != null) {
            RCTDeviceEventEmitter eventEmitter =
                    reactContext.getJSModule(RCTDeviceEventEmitter.class);
            eventEmitter.emit("refreshState", buildState());
        }
    }

    private WritableMap buildState() {
        WritableMap state = new WritableNativeMap();
        state.putArray("screenExpressions", buildExpressionState());
        return state;
    }

    private WritableArray buildExpressionState() {
        WritableArray expressionState = new WritableNativeArray();
        for (Entry<Integer, ScreenExpression> entry : appState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            WritableMap exprMap = new WritableNativeMap();
            exprMap.putInt("exprId", exprId);
            // TODO: Maybe offset to account for canvas pos.
            exprMap.putDouble("x", pixelsToDp(screenExpression.canvasPos().getX()));
            exprMap.putDouble("y", pixelsToDp(screenExpression.canvasPos().getY()));
            exprMap.putMap("expr", buildExpressionMap(screenExpression.expr()));
            expressionState.pushMap(exprMap);
        }
        return expressionState;
    }

    private float pixelsToDp(int pixels) {
        return pixels / canvasRoot.getResources().getDisplayMetrics().density;
    }

    private WritableMap buildExpressionMap(UserExpression userExpression) {
        return userExpression.visit(
                lambda -> {
                    WritableMap result = new WritableNativeMap();
                    result.putString("type", "lambda");
                    result.putString("varName", lambda.varName());
                    if (lambda.body() != null) {
                        result.putMap("body", buildExpressionMap(lambda.body()));
                    } else {
                        result.putNull("body");
                    }
                    return result;
                },
                funcCall -> {
                    WritableMap result = new WritableNativeMap();
                    result.putString("type", "funcCall");
                    result.putMap("func", buildExpressionMap(funcCall.func()));
                    result.putMap("arg", buildExpressionMap(funcCall.arg()));
                    return result;
                },
                variable -> {
                    WritableMap result = new WritableNativeMap();
                    result.putString("type", "variable");
                    result.putString("varName", variable.varName());
                    return result;
                },
                reference -> {
                    WritableMap result = new WritableNativeMap();
                    result.putString("type", "reference");
                    result.putString("defName", reference.defName());
                    return result;
                }
        );
    }
}
