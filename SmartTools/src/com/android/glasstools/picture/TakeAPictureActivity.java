package com.android.glasstools.picture;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.glasstools.HomeActivity;
import com.android.glasstools.R;
import com.android.glasstools.face.BaseGlassActivity;
import com.android.glasstools.streaming.Utils;
import com.google.android.glass.media.Sounds;

public class TakeAPictureActivity extends BaseGlassActivity implements
		SurfaceHolder.Callback {

	private static final String TAG = "TakePictureActivity";

	private Camera camera;
	private boolean mHasSurface;
	private boolean isImageNotCaptured = true;

	private boolean mCameraConfigured = false;

	private AudioManager mAudioManager;

	private TextView mZoomLevelView;

	public static Intent newIntent(Context context) {
		Intent intent = new Intent(context, TakeAPictureActivity.class);
		return intent;
	}

	// code copied from
	// http://developer.android.com/guide/topics/media/camera.html
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.e(TAG, "Camera is not available");
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_take_picture);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Log.d(TAG, "onCreate");

		mHasSurface = false;

		// uncomment to debug the application.
		// android.os.Debug.waitForDebugger();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// Log.d(TAG, "onStart");

	}

	@Override
	public void onResume() {
		// Log.d(TAG, "onResume");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		super.onResume();
		startCamera();
	}

	@Override
	protected void onPause() {
		// Log.d(TAG, "onPause");
		if (!mHasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		if (null != camera) {
			camera.release();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		// Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// Log.d(TAG, "onDestroy");
		super.onDestroy();
		camera = null;
	}

	private void startCamera() {
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (mHasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}

		try {
			// camera = Camera.open();
			camera = getCameraInstance();
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			Toast.makeText(TakeAPictureActivity.this,
					"Unable to start camera, please restart and try again",
					Toast.LENGTH_LONG).show();
			// Log.d(TAG, e.getMessage());
			// e.printStackTrace();
		} catch (Exception e) {
			Toast.makeText(TakeAPictureActivity.this,
					"Unable to start camera, please restart and try again",
					Toast.LENGTH_LONG).show();
			// Log.d(TAG, e.getMessage());
			// e.printStackTrace();
		}
		if (!mCameraConfigured) {
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewFpsRange(30000, 30000);
			parameters.setPreviewSize(640, 360);
			camera.setParameters(parameters);
			mCameraConfigured = true;
		}
		if (mCameraConfigured && null != camera) {
			mZoomLevelView = (TextView) findViewById(R.id.zoomLevel);
			camera.setZoomChangeListener(onZoomChangeListener);
			camera.startPreview();
		}
	}

	@Override
	protected boolean onTap() {
		if (isImageNotCaptured) {
			isImageNotCaptured = false;
			camera.takePicture(null, null, mPicture);
			// takePicture = true;
		}
		return super.onTap();
	}

	@Override
	protected boolean onSwipeDown() {
		Utils.dLog("Gesture.SWIPE_DOWN");
		mAudioManager.playSoundEffect(Sounds.DISMISSED);
		Intent intent = new Intent(TakeAPictureActivity.this,
				HomeActivity.class);
		startActivity(intent);
		finish();
		return false;
	}

	@Override
	protected boolean onSwipeLeft() {
		// TODO Auto-generated method stub
		Camera.Parameters parameters = camera.getParameters();
		int zoom = parameters.getZoom();
		zoom -= 5;
		if (zoom < 0) {
			zoom = 0;
		}
		camera.stopSmoothZoom();
		camera.startSmoothZoom(zoom);
		return super.onSwipeLeft();
	};

	@Override
	protected boolean onSwipeRight() {
		// TODO Auto-generated method stub
		Camera.Parameters parameters = camera.getParameters();
		int zoom = parameters.getZoom();
		zoom += 5;
		if (zoom > parameters.getMaxZoom()) {
			zoom = parameters.getMaxZoom();
		}
		camera.stopSmoothZoom();
		camera.startSmoothZoom(zoom);
		return super.onSwipeRight();
	};

	PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] captureData, Camera camera) {

			Intent intent = new Intent(getApplicationContext(),
					ShareActivity.class);
			Session.getInstant().setImageBytes(captureData);
			isImageNotCaptured = true;
			startActivity(intent);
			// finish();
		}
	};

	protected Bitmap getBitmapFromByteArray(byte[] captureData) {
		Bitmap captureImage = null;
		captureImage = BitmapFactory.decodeByteArray(captureData, 0,
				captureData.length, null);
		// Mutable copy:
		captureImage = captureImage.copy(Bitmap.Config.ARGB_8888, true);
		return captureImage;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			// Log.e(TAG,
			// "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!mHasSurface) {
			mHasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHasSurface = false;
	}

	Camera.OnZoomChangeListener onZoomChangeListener = new Camera.OnZoomChangeListener() {

		@Override
		public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
			// TODO Auto-generated method stub
			mZoomLevelView.setText("ZOOM: " + zoomValue);
		}
	};

}
