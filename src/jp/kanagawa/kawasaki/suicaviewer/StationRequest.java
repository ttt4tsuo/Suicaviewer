package jp.kanagawa.kawasaki.suicaviewer;

import java.io.UnsupportedEncodingException;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class StationRequest<T> extends Request<T> {

    private final Listener<T> listener;

    public StationRequest(String url, Listener<T> listener, ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = listener;
    }

	@Override
    protected void deliverResponse(T response) {
		listener.onResponse(response);
    }
	@Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data,"utf-8");
            Log.d("myapp",json);
            return (Response<T>) Response.success(
            		json,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
        
}
