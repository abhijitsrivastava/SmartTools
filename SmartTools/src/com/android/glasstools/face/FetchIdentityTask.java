package com.android.glasstools.face;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cloudinary.Cloudinary;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FetchIdentityTask extends AsyncTask<String, String, String> {

	private Context context;
	private byte[] capturedData;
	private ImageManager mImageManager;
	
	static String Access_Key_ID = "<ENTER YOUR ACCESS KEY>";
	static String Secret_Access_Key = "<ENTER YOUR SECRET KEY>";
	
	static String bucket_name = "bucket-smarttools";

	public FetchIdentityTask(Activity context, byte[] capturedData) {
		this.context = context;
		this.capturedData = capturedData;
	}

	@Override
	protected String doInBackground(String... params) {
		
		String responseString = "";
		
		String savedImagePath = getSavedImagePath();
		
		File file = new File(savedImagePath);
		
		//String imageUrl = uploadImageCloudinary(file);
		String imageUrl = uploadImageAmazon(file);
		
		try {
			com.mashape.unirest.http.HttpResponse<JsonNode> response = Unirest
					.post("https://lambda-face-recognition.p.mashape.com/recognize")
					.header("X-Mashape-Key",
							"gMoueY07U1mshpinwogEUEk0B13Qp1TUNLejsnkH2IwTJ9iEWF")
					.field("album", "TEST_NEW")
					.field("albumkey",
							"7bcc2ac04255c7d4859547b05120c2535e84f624b72f9dd42959a0932a91e557")
					.field("urls", imageUrl).asJson();
			responseString = response.getBody().toString();
			//System.out.println("response is " + response.getBody().toString());
			//Unirest.shutdown();
		} catch (UnirestException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return responseString;
	}

	@Override
	protected void onPostExecute(String result) {
		//Utils.eLog("response" + result);
		super.onPostExecute(result);
		if (!"".equals(result)) {
			try {
				JSONObject response = new JSONObject(result);
				JSONArray jsonArrayPhotos = response.getJSONArray("photos");
				JSONObject jsonObjectPhotosOne = (JSONObject) jsonArrayPhotos
						.get(0);
				JSONArray jsonArrayTags = jsonObjectPhotosOne
						.getJSONArray("tags");
				JSONObject jsonObjectTagsOne = (JSONObject) jsonArrayTags
						.get(0);
				JSONArray jsonArrayUids = jsonObjectTagsOne
						.getJSONArray("uids");
				JSONObject jsonObjectUidsOne = (JSONObject) jsonArrayUids
						.get(0);
				String prediction = jsonObjectUidsOne.optString("prediction");

				((ShowIdentityActivity) context)
						.onCompleteUpdateStatusRequest(prediction);

			} catch (JSONException e) {
				e.printStackTrace();
				((ShowIdentityActivity) context).onCompleteUpdateStatusRequest("");
			}
		} else {
			Utils.dLog("Getting empty response");
			((ShowIdentityActivity) context).onCompleteUpdateStatusRequest("Error Network Issue");
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
	
	protected String getSavedImagePath(){
		
		Bitmap captureImage = null;
		if (capturedData != null) {
			captureImage = getBitmapFromByteArray(capturedData);
		}
		Uri imageUri = null;
		String imageName = "FaceReco.png";
		try {
			mImageManager = new ImageManager(context);
			imageUri = mImageManager.saveImage(imageName, captureImage);
			return imageUri.getPath();
			//Log.v("FaceReco", "Saving image as: " + imageName);
		} catch (IOException e) {
			Log.e("faceReco", "Failed to save image!", e);
		}	
		return null;
	}
	
	
	protected static String uploadImageCloudinary(File fileUri){
		
		File file = fileUri;
		
		String url = "http://res.cloudinary.com/doi38h3hr/image/upload/v1413102074/afuzj1td4virweuqwsmw.jpg";
		/*Cloudinary cloudinary = new Cloudinary(Cloudinary.asMap("cloud_name",
				"doi38h3hr", "api_key", "876521916516342", "api_secret",
				"00L0PfCC13iT_BGzmVNrsesuT0I"));*/
		Cloudinary cloudinary = new Cloudinary(Cloudinary.asMap("cloud_name",
				"dlgxm8obn", "api_key", "179359492776334", "api_secret",
				"rydimGdfJtVvDWC-87A-zJ5ycOI"));
		try {
			JSONObject uploadResult = cloudinary.uploader().upload(new FileInputStream(file), null);
			url = (String) uploadResult.getString("url");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("URL is " + url);

		
		return null;
		
	}
	
	protected static String  uploadImageAmazon(File fileUri) {
		try {
			/*File file = new File(
					"D:\\Personal\\Projects\\ANPRLibrary_Demo_V1_Android_new\\ANPRSample\\res\\drawable-hdpi\\car8.png");
			*/
			File file = fileUri;
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(
					Access_Key_ID, Secret_Access_Key);
			AmazonS3 s3Client = new AmazonS3Client(awsCreds);

			InputStream fis = new FileInputStream(file);
			byte[] buf = new byte[1024];
			int len;
			long streamLength = 0;

			while ((len = fis.read(buf)) > 0) {
				// fOut.write(buf, 0, len);
				streamLength = streamLength + len;
			}
			fis.close();
			fis = new FileInputStream(file);
			
			ObjectMetadata om = new ObjectMetadata();
			om.setContentLength(streamLength);
			
			long timeStamp = new Date().getTime();
			
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					bucket_name, "facereco/pic"+timeStamp+".png", fis, om);
			AccessControlList acl = new AccessControlList();
			acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
			putObjectRequest.setAccessControlList(acl);
			s3Client.putObject(putObjectRequest);
			
			String completeUrl = "https://s3-ap-southeast-1.amazonaws.com/bucket-smarttools/"+"facereco/pic"+timeStamp+".png";
			return completeUrl;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
