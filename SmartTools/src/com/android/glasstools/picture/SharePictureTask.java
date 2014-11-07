package com.android.glasstools.picture;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SharePictureTask extends AsyncTask<String, String, String> {

	private Context context;
	private byte[] capturedData;
	private ImageManager mImageManager;

	public SharePictureTask(Activity context, byte[] capturedData) {
		this.context = context;
		this.capturedData = capturedData;
	}

	@Override
	protected String doInBackground(String... params) {

		Bitmap captureImage = null;
		if (capturedData != null) {
			captureImage = getBitmapFromByteArray(capturedData);
		}

		Uri imageUri = null;

		String imageName = "FaceReco.png";
		try {
			mImageManager = new ImageManager(context);
			imageUri = mImageManager.saveImage(imageName, captureImage);
			// Log.v("FaceReco", "Saving image as: " + imageName);
		} catch (IOException e) {
			Log.e("faceReco", "Failed to save image!", e);
		}

		File file = new File(imageUri.getPath());
		String url = "http://res.cloudinary.com/doi38h3hr/image/upload/v1413102074/afuzj1td4virweuqwsmw.jpg";
		/*Cloudinary cloudinary = new Cloudinary(Cloudinary.asMap("cloud_name",
				"doi38h3hr", "api_key", "876521916516342", "api_secret",
				"00L0PfCC13iT_BGzmVNrsesuT0I"));*/
		
		Cloudinary cloudinary = new Cloudinary(Cloudinary.asMap("cloud_name",
				"dlgxm8obn", "api_key", "179359492776334", "api_secret",
				"rydimGdfJtVvDWC-87A-zJ5ycOI"));
		try {
			JSONObject uploadResult = cloudinary.uploader().upload(
					new FileInputStream(file), null);
			url = (String) uploadResult.getString("url");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	@Override
	protected void onPostExecute(String result) {
		// Utils.eLog("response" + result);
		super.onPostExecute(result);
		if (!"".equals(result)) {
			Log.d("uploading ", result);
			((ShareActivity) context)
					.onCompleteUpdateStatusRequest("Pic send successfully.");
		} else {
			Log.d("share pic", "Getting empty response");
			((ShareActivity) context)
					.onCompleteUpdateStatusRequest("Error Network Issue");
		}
	}

	protected Bitmap getBitmapFromByteArray(byte[] captureData) {
		Bitmap captureImage = null;
		captureImage = BitmapFactory.decodeByteArray(captureData, 0,
				captureData.length, null);
		// Mutable copy:
		captureImage = captureImage.copy(Bitmap.Config.ARGB_8888, true);
		return captureImage;
	}

}
