package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.graphics.Bitmap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.InputStream;
import java.util.ArrayList;


public class ShareModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    public ShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ReactNativeShareExtension";
    }

    @ReactMethod
    public void close() {
        getCurrentActivity().finish();
    }

    @ReactMethod
    public void data(Promise promise) {
        promise.resolve(processIntent());
    }

    public WritableMap processIntent(Activity currentActivity, Intent intent) {
        WritableMap map = Arguments.createMap();
        WritableArray values = Arguments.createArray();

        String intentAction = "";
        String type = "";
        String text = "";

        if (currentActivity != null && intent != null) {
            intentAction = intent.getAction();
            type = intent.getType();

            if (type == null) {
                type = "";
            }

            if (Intent.ACTION_SEND.equals(intentAction) && "text/plain".equals(type)) {

                text = intent.getStringExtra(Intent.EXTRA_TEXT);

            } else if (Intent.ACTION_SEND.equals(intentAction) && (type.length() > 0)) {

                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                values.pushString("file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri));

            } else if (Intent.ACTION_SEND_MULTIPLE.equals(intentAction) && (type.length() > 0)) {

                ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                for (Uri uri : uris) {
                    values.pushString("file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri));
                }
            }
        }

        map.putString("text", text);
        map.putString("type", type);
        map.putArray("values", values);

        return map;
    }

    public WritableMap processIntent() {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            return this.processIntent(currentActivity, currentActivity.getIntent());
        }

        return this.processIntent(null, null);
    }


    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            WritableMap map = this.processIntent(currentActivity, intent);

            boolean shouldEmit = map.getArray("values").size() > 0 || map.getString("text").length() > 0;

            if (shouldEmit) {
                this.sendEvent("shareData", map);
            }
        }
    }

    private void sendEvent(String eventName, Object params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
