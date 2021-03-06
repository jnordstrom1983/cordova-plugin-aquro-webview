package cordova.plugin.aquro.webiew;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.DOWNLOAD_SERVICE;


/**
 * This class echoes a string called from JavaScript.
 */
public class AquroWebView extends CordovaPlugin {


    private DownloadManager.Request request;
    private ValueCallback<Uri[]> uploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    private static final String TAG = AquroWebView.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(Build.VERSION.SDK_INT >= 21){
            Uri[] results = null;
            //Check if response is positive
            if(resultCode== Activity.RESULT_OK){
                if(requestCode == FCR){
                    if(null == mUMA){
                        return;
                    }
                    if(intent == null){
                        //Capture Photo if no image available
                        if(mCM != null){
                            results = new Uri[]{Uri.parse(mCM)};
                        }
                    }else{
                        String dataString = intent.getDataString();


                        if(dataString != null){
                            results = new Uri[]{Uri.parse(dataString)};
                        }else {
                            if(mCM != null){
                                results = new Uri[]{Uri.parse(mCM)};
                            }


                        }
                    }
                }
            }
            mUMA.onReceiveValue(results);
            mUMA = null;
        }else{
            if(requestCode == FCR){
                if(null == mUM) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                mUM.onReceiveValue(result);
                mUM = null;
            }
        }
    }



    private CallbackContext callback;
    private Map<String, WebView> webviews;
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("aquro://")){
                String Name = url.split("//")[1];
                webView.loadUrl("javascript:AquroWebViewEvent('" + Name + "')");
                return true;
            }

            if(url.startsWith("http://") || url.startsWith("https://")){
                view.loadUrl(url);
                return true;
            }else{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                view.getContext().startActivity(intent);
                return true;


            }





        }



    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
       if(webviews == null) webviews = new HashMap<String, WebView>();


        if (action.equals("SetURL")) {
            callback = callbackContext;
            final String name = args.getString(0);
            final String url = args.getString(1);
            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView w = webviews.get(name);
                    if(w == null){
                        callback.error("Webview not found");
                    }else {
                        w.loadUrl(url);
                        callback.success();
                    }
                }
            });
            return true;
        }


        if (action.equals("RemoveAllViews")) {
            callback = callbackContext;
            final String name = args.getString(0);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    for (Map.Entry<String, WebView> entry : webviews.entrySet()) {
                        WebView w = entry.getValue();
                        RelativeLayout r = (RelativeLayout)w.getParent();
                        r.removeView(w);
                        FrameLayout l = (FrameLayout)r.getParent();
                        l.removeView(r);


                    }

                    webviews.clear();

                    callback.success();
               }
            });
            return true;
        }


        if (action.equals("DeleteView")) {
            callback = callbackContext;
            final String name = args.getString(0);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView w = webviews.get(name);
                    if(w == null){
                        callback.error("Webview not found");
                    }else {
                        RelativeLayout r = (RelativeLayout)w.getParent();
                        r.removeView(w);
                        FrameLayout l = (FrameLayout)r.getParent();
                        l.removeView(r);
                        webviews.remove(name);
                        callback.success();
                    }
                }
            });
            return true;
        }


        if (action.equals("HideView")) {
            callback = callbackContext;
            final String name = args.getString(0);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView w = webviews.get(name);
                    if(w == null){
                        callback.error("Webview not found");
                    }else {
                        RelativeLayout r = (RelativeLayout)w.getParent();
                        r.setVisibility(RelativeLayout.GONE);
                        callback.success();
                    }
                }
            });
            return true;
        }





        if (action.equals("ShowView")) {
            callback = callbackContext;
            final String name = args.getString(0);

            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView w = webviews.get(name);
                    if(w == null){
                        callback.error("Webview not found");
                    }else {
                        RelativeLayout r = (RelativeLayout)w.getParent();
                        r.setVisibility(RelativeLayout.VISIBLE);
                        callback.success();
                    }
                }
            });
            return true;
        }


        if (action.equals("MoveView")) {
            callback= callbackContext;
            final String name = args.getString(0);
            final int top = args.getInt(1);
            final int left = args.getInt(2);
            final int width = args.getInt(3);
            final int height = args.getInt(4);


            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView w = webviews.get(name);
                    if(w == null){
                        callback.error("Webview not found");
                    }else{
                        RelativeLayout r = (RelativeLayout)w.getParent();
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
                        params.leftMargin = left;
                        params.topMargin = top;
                        params.width = width;
                        params.height = height;
                        r.setLayoutParams(params);

                        ViewGroup.LayoutParams params_wv =  w.getLayoutParams();;
                        params_wv.width = width;
                        params_wv.height = height;
                        w.setLayoutParams(params_wv);
                        callback.success();

                    }




                }
            });

            return true;



        }

        if (action.equals("CreateView")) {

            callback= callbackContext;
            final String name = args.getString(0);
            final String url = args.getString(1);
            final int top = args.getInt(2);
            final int left = args.getInt(3);
            final int width = args.getInt(4);
            final int height = args.getInt(5);


            cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WebView w = new WebView(webView.getContext());

                    FrameLayout f = (FrameLayout) webView.getView().getParent();

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
                    params.leftMargin = left;
                    params.topMargin = top;
                    params.width = width;
                    params.height = height;


                    RelativeLayout a = new RelativeLayout(webView.getContext());
                    a.setLayoutParams(params);
                    w.setLayoutParams(params);



                    w.setWebViewClient(new MyWebViewClient(){


                    });
                    w.setWebChromeClient(new WebChromeClient(){









                        //For Android 3.0+
                        public void openFileChooser(ValueCallback<Uri> uploadMsg){
                            mUM = uploadMsg;
                            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                            i.addCategory(Intent.CATEGORY_OPENABLE);
                            i.setType("*/*");
                            cordova.startActivityForResult(AquroWebView.this,Intent.createChooser(i,"File Chooser"), FCR);
                        }
                        // For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this
                        public void openFileChooser(ValueCallback uploadMsg, String acceptType){
                            mUM = uploadMsg;
                            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                            i.addCategory(Intent.CATEGORY_OPENABLE);
                            i.setType("image/*");
                            cordova.startActivityForResult(AquroWebView.this,
                                    Intent.createChooser(i, "File Browser"),
                                    FCR);
                        }
                        //For Android 4.1+
                        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                            mUM = uploadMsg;
                            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                            i.addCategory(Intent.CATEGORY_OPENABLE);
                            i.setType("image/*");
                            cordova.startActivityForResult(AquroWebView.this,Intent.createChooser(i, "File Chooser"), AquroWebView.FCR);
                        }
                        //For Android 5.0+
                        public boolean onShowFileChooser(
                                WebView webView, ValueCallback<Uri[]> filePathCallback,
                                WebChromeClient.FileChooserParams fileChooserParams){
                            if(mUMA != null){
                                mUMA.onReceiveValue(null);
                            }
                            mUMA = filePathCallback;


                            if(cordova.hasPermission(WRITE_EXTERNAL_STORAGE))
                            {
                                return ChooseFile();
                            }
                            else
                            {
                                cordova.requestPermission(AquroWebView.this, 1, WRITE_EXTERNAL_STORAGE);
                                //filePathCallback.onReceiveValue(null);
                                return true;
                            }


                        }

                    });


                    w.getSettings().setJavaScriptEnabled(true);
                    w.getSettings().setDomStorageEnabled(true);
                    w.getSettings().setUseWideViewPort(true);
                    w.getSettings().setAllowFileAccess(true);
                    w.setVerticalScrollBarEnabled(true);


                    w.setDownloadListener(new DownloadListener() {
                        public void onDownloadStart(String url, String userAgent,
                                                    String contentDisposition, String mimetype,
                                                    long contentLength) {

                            String cookie = CookieManager.getInstance().getCookie(url);

                            request = new DownloadManager.Request(
                                    Uri.parse(url));
                            String fileName = contentDisposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");

                            if(fileName == null) {
                                fileName = URLUtil.guessFileName(url, null, null);
                            }


                            request.addRequestHeader("Cookie", cookie);
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);


                            if(cordova.hasPermission(WRITE_EXTERNAL_STORAGE))
                            {
                               PerformRequest();
                            }
                            else
                            {
                                cordova.requestPermission(AquroWebView.this, 0, WRITE_EXTERNAL_STORAGE);
                            }


                        }
                    });



                    w.loadUrl(url);
                    a.addView(w);
                    f.addView(a);


                    webviews.put(name, w);
                    callback.success();

                }
            });



            return true;

        }
        return false;
    }


    private boolean ChooseFile(){



        boolean foundCamera = false;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity( cordova.getActivity().getApplication().getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCM);
            }catch(IOException ex){
                Log.e(TAG, "Image file creation failed", ex);
            }
            if(photoFile != null){
                mCM = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

            }else{
                takePictureIntent = null;
            }
        }else{


        }
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("*/*");
        Intent[] intentArray;
        if(takePictureIntent != null){
            intentArray = new Intent[]{takePictureIntent};
        }else{
            intentArray = new Intent[0];
        }

//        if(takePictureIntent == null){
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            }catch(IOException ex) {
//                LOG.e(TAG, "Could'nt create file");
//
//            }
//            if(photoFile!=null) {
//                // Camera.
//                final List<Intent> cameraIntents = new ArrayList<Intent>();
//                final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                final PackageManager packageManager = cordova.getActivity().getApplication().getPackageManager();
//                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
//                for (ResolveInfo res : listCam) {
//                    final String packageName = res.activityInfo.packageName;
//                    final Intent intent = new Intent(captureIntent);
//                    intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
//                    intent.setPackage(packageName);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                    cameraIntents.add(intent);
//                }
//                intentArray = cameraIntents.toArray(new Intent[cameraIntents.size()]);
//            }
        //       }


        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        cordova.startActivityForResult(AquroWebView.this,chooserIntent, FCR);
        return true;

    }
    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }
    private void PerformRequest(){
        DownloadManager dm = (DownloadManager) cordova.getActivity().getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);

    }
    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException
    {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                return;
            }
        }
        switch(requestCode)
        {
            case 0:
                PerformRequest();
                break;
            case 1:
                ChooseFile();
                break;
        }
    }
}
