package com.android.glasstools.face;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.util.Log;

public class Utils {

	private static final String TAG = "FaceRecognition";

	public static HttpResponse makeRequest(String url, JSONObject params)
			throws ClientProtocolException, IOException {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		// passes the results to a string builder/entity
		StringEntity se = new StringEntity(params.toString());

		// sets the post request as the resulting string
		httppost.setEntity(se);

		// sets a request header so the page receving the request
		// will know what to do with it
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-type", "application/json");
		httppost.setHeader("X-Mashape-Key",
				"gMoueY07U1mshpinwogEUEk0B13Qp1TUNLejsnkH2IwTJ9iEWF");

		// Execute HTTP Post Request
		return httpclient.execute(httppost);
	}

	public static void dLog(String message) {
		//Log.d(TAG, message);
	}

	public static void eLog(String message) {
		//Log.e(TAG, message);
	}

}
