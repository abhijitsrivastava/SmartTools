package com.android.glasstools.picture;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.android.glasstools.HomeActivity;
import com.android.glasstools.R;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardBuilder.Layout;

public class TakeAPictureHomeActivity extends Activity {

	private GestureDetector mGestureDetector;
	private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_take_a_picture_home);
	}

	@Override
	protected void onStart() {
		dLog("onStart");
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mGestureDetector = createGestureDetector(this);
		super.onStart();
	}

	@Override
	protected void onResume() {
		dLog("onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		dLog("onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		dLog("onStop");
		mGestureDetector = null;
		mAudioManager = null;
		super.onStop();
	}

	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
			menu.add(0, 1, Menu.NONE,
					getResources().getString(R.string.menu_send_a_picture));
			menu.add(
					0,
					2,
					Menu.NONE,
					getResources().getString(
							R.string.menu_take_a_picture));
			return true;
		}
		// Pass through to super to setup touch menu.
		return super.onCreatePanelMenu(featureId, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		Intent intent = null;
		if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
			CardBuilder cardBuilder = new CardBuilder(getApplicationContext(), Layout.TEXT);
			cardBuilder.setText("Sorry, This part is under maintenance.");
			switch (item.getItemId()) {
			case 1: // start Stopwatch activity
				//setContentView(cardBuilder.getView());
				intent = new Intent(this, ShowSavedPictureActivity.class);
				startActivity(intent);
				finish();
				break;
			case 2:
				// start Noice Level Meter activity
				//setContentView(cardBuilder.getView());
				intent = new Intent(this, CameraActivity.class);
				startActivity(intent);
				finish();
				break;
			default:
				return true;
			}
			return true;
		}
		// Good practice to pass through to super if not handled
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.clear();

		menu.add(0, 1, Menu.NONE,
				getResources().getString(R.string.menu_send_a_picture));
		menu.add(
				0,
				2,
				Menu.NONE,
				getResources().getString(
						R.string.menu_take_a_picture));

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		mAudioManager.playSoundEffect(Sounds.TAP);
		Intent intent = null;
		CardBuilder cardBuilder = new CardBuilder(getApplicationContext(), Layout.TEXT);
		cardBuilder.setText("Sorry, This part is under maintenance.");
		switch (item.getItemId()) {
		
		
		case 1: // start NumberPlateRecognitionActivity
			//setContentView(cardBuilder.getView());
			intent = new Intent(this, ShowSavedPictureActivity.class);
			startActivity(intent);
			finish();
			break;

		case 2: // start TakeAPictureActivity
			intent = new Intent(this, CameraActivity.class);
			startActivity(intent);
			finish();
			break;

		default:
			eLog("Default option is selected");
			break;
		}

		return super.onOptionsItemSelected(item);
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

	private GestureDetector createGestureDetector(final Context context) {

		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					dLog("Gesture.TAP");
					mAudioManager.playSoundEffect(Sounds.TAP);
					openOptionsMenu();
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					dLog("Gesture.SWIPE_DOWN");
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					Intent intent = new Intent(TakeAPictureHomeActivity.this,HomeActivity.class);
					startActivity(intent);
					finish();
					return false;
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

	public void dLog(String message) {
		Log.d(TakeAPictureHomeActivity.this.getLocalClassName(), message);
	}

	public void eLog(String message) {
		Log.e(TakeAPictureHomeActivity.this.getLocalClassName(), message);
	}

}
