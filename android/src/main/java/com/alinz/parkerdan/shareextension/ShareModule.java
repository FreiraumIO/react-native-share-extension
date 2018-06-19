package com.alinz.parkerdan.shareextension;

import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import android.graphics.Bitmap;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;

public class ShareModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactApplicationContext;

  public ShareModule(ReactApplicationContext reactContext) {
    super(reactContext);
    reactApplicationContext = reactContext;
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

    String value = "";
    String type = "";
    String action = "";

    Activity currentActivity = getCurrentActivity();

    if (currentActivity != null) {
      Intent intent = currentActivity.getIntent();
      action = intent.getAction();
      type = intent.getType();
      Log.e("FILE TYPE : ", type);
      Log.e("FILE PATH : ", (((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM)).getPath()));
      if (type == null) {
        type = "";
      }
      if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(type)) {
        value = intent.getStringExtra(Intent.EXTRA_TEXT);
      } else if (Intent.ACTION_SEND.equals(action) && ("image/*".equals(type) || "image/jpeg".equals(type)
          || "image/png".equals(type) || "image/jpg".equals(type))) {
        Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri.toString().startsWith("content://")) {
          try {
            value = "file://" + convertToBitmapAndCreateAFile((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
          } catch (IOException e) {
            value = "";
            e.printStackTrace();
          }
        } else {
          value = "file://" + RealPathUtil.getRealPathFromURI(currentActivity, uri);
        }

      } else {
        value = "";
      }
    } else {
      value = "";
      type = "";
    }

    map.putString("type", type);
    map.putString("value", value);

    return map;
  }

  /**
   * Edited by : AASHIK HAMEED : Convert the Uri content to bitmap and reduce the
   * quality of the image then save it inside a cache file.
   * 
   * @param Uri
   * @return
   * @throws IOException
   */
  private String convertToBitmapAndCreateAFile(Uri Uri) throws IOException {
    Bitmap bitmap = MediaStore.Images.Media.getBitmap(reactApplicationContext.getContentResolver(), Uri);

    // create a file to write bitmap data
    File f = new File(reactApplicationContext.getCacheDir(), "shareImage_ " + System.currentTimeMillis() + ".jpeg");
    f.createNewFile();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
    byte[] bitmapdata = bos.toByteArray();

    FileOutputStream fos = new FileOutputStream(f);
    fos.write(bitmapdata);
    fos.flush();
    fos.close();

    return f.getAbsolutePath();
  }
}