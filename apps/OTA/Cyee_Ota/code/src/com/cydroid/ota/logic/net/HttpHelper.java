package com.cydroid.ota.logic.net;

import android.annotation.TargetApi;
import android.os.Build;
import com.cydroid.ota.execption.SettingUpdateNetException;
import com.cydroid.ota.logic.config.NetConfig;
import com.cydroid.ota.Log;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by borney on 14-9-11.
 */
public class HttpHelper {
    private static final String TAG = "HttpHelper";

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static HttpClient getDefaultHttpClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        BasicHttpParams localBasicHttpParams = new BasicHttpParams();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
                localBasicHttpParams,
                schemeRegistry);
        HttpClient httpClient = new DefaultHttpClient(cm, localBasicHttpParams);
        HttpParams params = httpClient.getParams();
        HttpConnectionParams
                .setSocketBufferSize(params, NetConfig.SOCKET_BUFFER_SIZE);
        HttpConnectionParams
                .setConnectionTimeout(params, NetConfig.GIONEE_CONNECT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, NetConfig.GIONEE_SOCKET_TIMEOUT);
        return httpClient;
    }

    public static HttpEntity executeHttpPost(String uri,
            List<NameValuePair> pairs,
            boolean gzip, Map<String, Object> params,
            Map<String, String> headers) throws SettingUpdateNetException {
        HttpPost request = new HttpPost(uri);
        if (pairs != null && !pairs.isEmpty()) {
            UrlEncodedFormEntity requestEntity = null;
            try {
                requestEntity = new UrlEncodedFormEntity(pairs, HTTP.UTF_8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            ((HttpPost) request).setEntity(requestEntity);
        }
        return executeHttpRequest(request, gzip, params, headers);
    }

    public static HttpEntity executeHttpGet(String uri,
            List<NameValuePair> pairs,
            boolean gzip, Map<String, Object> params,
            Map<String, String> headers) throws SettingUpdateNetException {
        StringBuilder sb = new StringBuilder(uri);
        if (!sb.toString().endsWith("&") && pairs != null && pairs.size() > 0) {
            sb.append("&");
        }
        if (pairs != null && !pairs.isEmpty()) {
            for (NameValuePair pair : pairs) {
                sb.append(pair.getName());
                sb.append("=");
                sb.append(pair.getValue());
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        Log.d(TAG, "executeHttpGet uri = " + sb.toString());
        HttpGet request = new HttpGet(sb.toString());
        return executeHttpRequest(request, gzip, params, headers);
    }

    private static HttpEntity executeHttpRequest(HttpUriRequest request,
            boolean gzip, Map<String, Object> params,
            Map<String, String> headers) throws SettingUpdateNetException {
        HttpClient client = getDefaultHttpClient();

        if (params != null) {
            Set<String> keys = params.keySet();
            HttpParams httpParams = client.getParams();
            for (String key : keys) {
                httpParams.setParameter(key, params.get(key));
            }
        }

        if (headers != null) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                request.removeHeaders(key);
                request.setHeader(key, headers.get(key));
            }
        }

        return executeHttpRequest(request, client, gzip);
    }

    private static HttpEntity executeHttpRequest(HttpUriRequest request,
            HttpClient client,
            boolean gzip) throws SettingUpdateNetException {
        Log.d(TAG, "executeHttpRequest");
        HttpEntity httpEntity = null;
        if (gzip) {
            request.getParams()
                    .setParameter("Accept-Encoding", "gzip, deflate");
        }
        Log.d(TAG, "request = " + request.getURI());
        Header[] headers = request.getAllHeaders();
        for (Header header : headers) {
            Log.d(TAG, "head[" + header.getName() + ", " + header.getValue() + "]");
        }
        HttpParams params = request.getParams();
        //Log.d(TAG, "params = " + params);
        HttpResponse response = null;
        try {
            response = client.execute(request);
            httpEntity = parseResponse(response, HTTP.UTF_8);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException:" + e);
            throw new SettingUpdateNetException(SettingUpdateNetException.ERROR_SOCKET_TIMEOUT);
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e);
            if (e instanceof ConnectTimeoutException) {
                throw new SettingUpdateNetException(SettingUpdateNetException.ERROR_SOCKET_TIMEOUT);
            }
            throw new SettingUpdateNetException(SettingUpdateNetException.ERROR_SOCKET_IO);
        } finally {
            if (response != null) {
                int status = response.getStatusLine().getStatusCode();
                Log.d(TAG, "final url = " + request.getURI().toASCIIString()
                        + "  statusCode = " + status);
            }
            if (response == null) {
                request.abort();
            }
        }
        return httpEntity;
    }

    private static HttpEntity parseResponse(HttpResponse resp, String charset)
            throws SettingUpdateNetException {
        Log.d(TAG, "parseResponse resp is null " + (resp == null));
        if (resp != null) {
            int status = resp.getStatusLine().getStatusCode();
            Log.d(TAG, "resp status = " + status);
            if (status == HttpStatus.SC_OK) {
                return resp.getEntity();
            } else if (status == HttpStatus.SC_PARTIAL_CONTENT) {
                return resp.getEntity();
            } else if (status == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                return resp.getEntity();
            } else {
                throw new SettingUpdateNetException(status);
            }
        }
        return null;
    }

    private static void consume(final HttpEntity entity) {
        if (entity == null) {
            return;
        }
        if (entity.isStreaming()) {
            try {
                InputStream instream = entity.getContent();
                if (instream != null) {
                    instream.close();
                }
            } catch (Throwable t) {
                // do nothing
            }
        }
    }

}