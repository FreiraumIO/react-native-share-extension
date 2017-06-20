package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.graphics.Bitmap;

import java.io.InputStream;
import java.util.ArrayList;


public class ShareModule extends ReactContextBaseJavaModule {


    public ShareModule(ReactApplicationContext reactContext) {
        super(reactContext);
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

    public WritableMap processIntent() {
        WritableMap map = Arguments.createMap();
        WritableArray values = Arguments.createArray();

        String intentAction = "";
        String type = "";
        String text = "";

        Activity currentActivity = getCurrentActivity();

        if (currentActivity != null) {
            Intent intent = currentActivity.getIntent();
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
        } else {
            type = "";
        }

        map.putString("text", text);
        map.putString("type", type);
        map.putArray("values", values);

        return map;
    }
}
