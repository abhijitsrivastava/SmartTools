package com.android.glasstools.picture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.glasstools.R;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class ShareActivity extends Activity {

	private static final String TAG = "GlassTools";
	private static final int SPEECH_REQUEST = 0;
	private GestureDetector mGestureDetector;

	private byte[] imageByte;
	private AudioManager mAudioManager;
	private View progress;

	private Activity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout_share);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		progress = findViewById(R.id.progress);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mGestureDetector = createGestureDetector(this);

		activity = this;
		imageByte = Session.getInstant().getImageBytes();

		if (imageByte != null) {
			Bitmap captureImage = BitmapFactory.decodeByteArray(imageByte, 0,
					imageByte.length);
			ImageView imageView = (ImageView) findViewById(R.id.imageview);
			imageView.setImageBitmap(captureImage);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		mAudioManager = null;
		mGestureDetector = null;
	}

	private GestureDetector createGestureDetector(Context context) {

		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					Log.d(TAG, "Gesture.TAP");
					mAudioManager.playSoundEffect(Sounds.TAP);
					String imagePath = null;
					progress.setVisibility(View.VISIBLE);
					new SharePictureTask(activity, imageByte).execute(imagePath);
					return true;
				}
				return false;
			}
		});

		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				// do something on finger count changes
			}
		});

		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {
				// do something on scrolling
				return true;
			}
		});
		return gestureDetector;
	}

	/*
	 * Send generic motion events to the gesture detector
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {

		}
	}

	private void showToast(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(ShareActivity.this, message, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	private CharSequence wrapInSpan(CharSequence value) {
		SpannableStringBuilder sb = new SpannableStringBuilder(value);
		sb.setSpan(new AbsoluteSizeSpan(26), 0, value.length(), 0);
		return sb;
	}

	public void onCompleteUpdateStatusRequest(String prediction) {
		showToast(prediction);
		progress.setVisibility(View.GONE);
		Toast.makeText(activity, "", Toast.LENGTH_LONG).show();
		finish();
	}
}
