package in.goods24.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static com.loopj.android.http.AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT;

/**
 * Created by Shikhar on 10/15/2017.
 */

public class HttpUtils {
    private static final String BASE_URL = "http://goods24.in/app/rest/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    /*public void setTimeout(int value) {
        value = value < 1000 ? DEFAULT_SOCKET_TIMEOUT : value;
        client.setConnectTimeout(value);
        client.setResponseTimeout(value);
    }*/

}
