package com.android.glasstools.face;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.glasstools.R;

public class CaptureFaceActivity extends BaseGlassActivity {

	public static String TAG = "CaptureFaceActivity";

	private AudioManager mAudioManager;

	private SurfaceView mPreview;
	private SurfaceHolder mPreviewHolder;
	private Camera mCamera;

	private boolean mInPreview = false;
	private boolean mCameraConfigured = false;
	private TextView mZoomLevelView;

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
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_picture);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onResume");
		super.onResume();

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mPreview = (SurfaceView) findViewById(R.id.preview_view);
		mPreviewHolder = mPreview.getHolder();
		mPreviewHolder.addCallback(surfaceCallback);

		mZoomLevelView = (TextView) findViewById(R.id.zoomLevel);

		mCamera = getCameraInstance();
		if (mCamera != null)
			startPreview();

	}

	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		if (mInPreview) {
			mCamera.stopPreview();
			mCameraConfigured = false;

			mCamera.release();
			mCamera = null;
			mInPreview = false;
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	protected boolean onTap() {
		// TODO Auto-generated method stub
		mCamera.takePicture(null, null, mPictureCallback);
		return super.onTap();
	}

	@Override
	protected boolean onSwipeRight() {
		// TODO Auto-generated method stub
		Camera.Parameters parameters = mCamera.getParameters();
		int zoom = parameters.getZoom();
		zoom += 5;
		if (zoom > parameters.getMaxZoom()) {
			zoom = parameters.getMaxZoom();
		}
		mCamera.stopSmoothZoom();
		mCamera.startSmoothZoom(zoom);
		return super.onSwipeRight();
	}

	@Override
	protected boolean onSwipeLeft() {
		// TODO Auto-generated method stub
		Camera.Parameters parameters = mCamera.getParameters();
		int zoom = parameters.getZoom();
		zoom -= 5;
		if (zoom < 0) {
			zoom = 0;
		}
		mCamera.stopSmoothZoom();
		mCamera.startSmoothZoom(zoom);
		return super.onSwipeLeft();
	}

	@Override
	protected boolean onSwipeDown() {
		// TODO Auto-generated method stub
		
		return super.onSwipeDown();
	}

	private void configPreview(int width, int height) {
		if (mCamera != null && mPreviewHolder.getSurface() != null) {
			try {
				mCamera.setPreviewDisplay(mPreviewHolder);
			} catch (Throwable t) {
				Log.e(TAG, "Exception in initPreview()", t);
				Toast.makeText(CaptureFaceActivity.this, t.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			if (!mCameraConfigured) {
				Camera.Parameters parameters = mCamera.getParameters();

				// parameters.setPreviewSize(240, 160);
				parameters.setPreviewFpsRange(30000, 30000);
				parameters.setPreviewSize(640, 360);

				// //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
				// parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
				// parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				// parameters.setJpegQuality(100);

				mCamera.setParameters(parameters);
				mCamera.setZoomChangeListener(onZoomChangeListener);

				mCameraConfigured = true;
			}
		}
	}

	private void startPreview() {
		if (mCameraConfigured && mCamera != null) {
			mCamera.startPreview();
			mInPreview = true;
		}
	}

	Camera.OnZoomChangeListener onZoomChangeListener = new Camera.OnZoomChangeListener() {

		@Override
		public void onZoomChange(int zoomValue, boolean stopped, Camera camera) {
			// TODO Auto-generated method stub
			mZoomLevelView.setText("ZOOM: " + zoomValue);
		}
	};

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// nothing
			Log.v(TAG, "surfaceCreated");
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.v(TAG, "surfaceChanged=" + width + "," + height);
			configPreview(width, height);
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.v(TAG, "surfaceDestroyed");
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	};

	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// copied from
			// http://developer.android.com/guide/topics/media/camera.html#custom-camera
			Intent intent = new Intent(CaptureFaceActivity.this,
					FaceRecognitionActivity.class);
			startActivity(intent);
			Session.getInstant().setImageBytes(data);
			finish(); // works! (after card inserted to timeline)
		}

	};

}
