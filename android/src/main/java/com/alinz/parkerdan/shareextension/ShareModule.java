package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import android.graphics.Bitmap;
import java.io.InputStream;
import java.util.ArrayList;
import android.util.Log;

import android.support.v4.content.FileProvider;

public class ShareModule extends ReactContextBaseJavaModule {

  public ShareModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "ReactNativeShareExtension";
  }

  @ReactMethod
  public void clear() {
    Activity currentActivity = getCurrentActivity();

    if (currentActivity != null) {
      Intent intent = currentActivity.getIntent();
      intent.setAction("");
      intent.removeExtra(Intent.EXTRA_TEXT);
      intent.removeExtra(Intent.EXTRA_STREAM);
    }
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
    WritableArray images = Arguments.createArray();
    WritableArray mixedFiles = Arguments.createArray();
    WritableArray pdfs = Arguments.createArray();

    String text = "";
    String type = "";
    String action = "";
    String filepath = "";

    Activity currentActivity = getCurrentActivity();

    if (currentActivity != null) {
      Intent intent = currentActivity.getIntent();

      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

      action = intent.getAction();
      type = intent.getType();
      if (type == null) {
        type = "";
      }

      if (Intent.ACTION_VIEW.equals(action) && type.endsWith("pdf")) {
        // Get the file from the intent object
        Uri file_uri = intent.getData();
        if (file_uri != null) {
          filepath = file_uri.getPath();
          String resolvedPath = filepath.replace("document/raw:/", "");
          pdfs.pushString("file://" + resolvedPath);
        } else {
          filepath = "No file";
        }
      }

      // WHEN WE SHARE SINGLE PDF WITH HOST APP
      else if (Intent.ACTION_SEND.equals(action) && type.endsWith("pdf")) {
        Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        filepath = uri.getPath();
        String resolvedPath = filepath.replace("document/raw:/", "");
        pdfs.pushString("file://" + resolvedPath);
      }

      // WHEN WE SHARE MULTIPLE PDFS WITH HOST APP
      else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type.endsWith("pdf")) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        for (Uri uri : uris) {
          filepath = uri.getPath();
          String resolvedPath = filepath.replace("document/raw:/", "");
          pdfs.pushString("file://" + resolvedPath);
        }
      }

      // WHEN WE SHARE SINGLE TEXT WITH HOST APP
      else if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
        text = intent.getStringExtra(Intent.EXTRA_TEXT);
      }

      // WHEN WE SHARE SINGLE IMAGE WITH HOST APP
      else if (Intent.ACTION_SEND.equals(action) && type.startsWith("image")) {
        Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        filepath = uri.getPath();
        String resolvedPath = filepath.replace("document/raw:/", "");
        images.pushString("file://" + resolvedPath);
      }

      // WHEN WE SHARE MULTIPLE IMAGES WITH HOST APP
      else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type.startsWith("image")) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        for (Uri uri : uris) {
          filepath = uri.getPath();
          String resolvedPath = filepath.replace("document/raw:/", "");
          images.pushString("file://" + resolvedPath);
        }
      }

      // WHEN WE SHARE MULTIPLE IMAGES AND PDFS MIXED!! WITH HOST APP
      else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type.startsWith("*/*")) {

        // Log.v("SHARE_MULTIPLE", "actiontype: " + type);
        // Log.v("SHARE_MULTIPLE", "actiontype image: " + type.startsWith("image"));
        // Log.v("SHARE_MULTIPLE", "actiontype pdf: " + type.startsWith("pdf"));

        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        for (Uri uri : uris) {
          filepath = uri.getPath();
          String resolvedPath = filepath.replace("document/raw:/", "");
          mixedFiles.pushString("file://" + resolvedPath);
        }
      }
    }

    map.putString("type", type);
    map.putString("text", text);
    map.putArray("images", images);
    map.putArray("pdfs", pdfs);
    map.putArray("mixedFiles", mixedFiles);

    return map;
  }
}