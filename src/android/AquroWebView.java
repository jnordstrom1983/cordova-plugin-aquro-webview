package cordova.plugin.aquro.webiew;

import android.graphics.Color;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * This class echoes a string called from JavaScript.
 */
public class AquroWebView extends CordovaPlugin {


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
            view.loadUrl(url);
            return true;
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



                    w.setWebViewClient(new MyWebViewClient());
                    w.getSettings().setJavaScriptEnabled(true);
                    w.getSettings().setDomStorageEnabled(true);
                    w.getSettings().setUseWideViewPort(true);
                    w.setVerticalScrollBarEnabled(true);

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

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
