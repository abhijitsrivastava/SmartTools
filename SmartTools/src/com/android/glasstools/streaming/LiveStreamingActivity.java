package com.android.glasstools.streaming;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.glasstools.HomeActivity;
import com.android.glasstools.R;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class LiveStreamingActivity extends Activity implements
		RtspClient.Callback, Session.Callback, SurfaceHolder.Callback {

	private AudioManager mAudioManager;
	private GestureDetector mGestureDetector;

	private SurfaceView mSurfaceView;
	// private TextView mTextBitrate;
	private View mProgressBar;
	private Session mSession;
	private RtspClient mClient;

	private String serverUrl;
	private String username;
	private String password;
	private String streamingMode;
	private String glassId;

	private View transparentView;
	private TextView textViewNotStreaming;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Utils.dLog("Start of onCreate");

		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_live_streaming);

		// android.os.Debug.waitForDebugger();
		serverUrl = "rtsp://ec2-54-187-106-124.us-west-2.compute.amazonaws.com:1935/live/12345.stream";
		username = "glass";
		password = "glass";
		streamingMode = "security";
		glassId = "12345";

		mGestureDetector = createGestureDetector(this);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		// mTextBitrate = (TextView) findViewById(R.id.bitrate);
		mProgressBar = (View) findViewById(R.id.progress_bar);
		transparentView = (View) findViewById(R.id.view_layer);
		textViewNotStreaming = (TextView) findViewById(R.id.textview_not_streaming);

		// Configures the SessionBuilder
		mSession = SessionBuilder
				.getInstance()
				.setContext(getApplicationContext())
				// .setAudioEncoder(SessionBuilder.AUDIO_NONE)
				.setAudioEncoder(SessionBuilder.AUDIO_AAC)
				.setAudioQuality(new AudioQuality(8000, 16000))
				.setVideoEncoder(SessionBuilder.VIDEO_H264)
				.setSurfaceView(mSurfaceView).setPreviewOrientation(0)
				.setCallback(this).build();

		// Configures the RTSP client
		mClient = new RtspClient();
		mClient.setSession(mSession);
		mClient.setCallback(this);

		// textViewTranslusantLayer.setText(getResources().getString(
		// R.string.app_name));

		// Use this to force streaming with the MediaRecorder API
		// mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIARECORDER_API);

		// Use this to stream over TCP
		// mClient.setTransportMode(RtspClient.TRANSPORT_TCP);

		// Use this if you want the aspect ratio of the surface view to
		// respect the aspect ratio of the camera preview
		// mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

		mSurfaceView.getHolder().addCallback(this);

		if (streamingMode.equals("teacher")) {
			selectQuality("640x360, 30 fps, 200 Kbps");
		} else {
			selectQuality("176x144, 30 fps, 200 Kbps");
			// 352x288, 30 fps, 400 Kbps
			// selectQuality("640x360, 20 fps, 200 Kbps");
		}

		Utils.dLog("End of onCreate");
	}

	@Override
	public void onStart() {

		Utils.eLog("Start of onStart");
		super.onStart();

		Utils.dLog("End of onStart");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.dLog("onDestroy");
		System.gc();
	}

	@Override
	protected void onResume() {
		Utils.dLog("Start of onResume");
		super.onResume();

		toggleStream();

		// startRepeatingTask();
		Utils.dLog("End of onResume");
	}

	@Override
	protected void onPause() {
		Utils.dLog("start of onPause");
		if (mClient != null) {
			mClient.release();
		}

		if (mSession != null) {
			mSession.release();
		}

		if (mSurfaceView != null) {
			mSurfaceView.getHolder().removeCallback(this);
		}

		mGestureDetector = null;
		mAudioManager = null;

		// stopRepeatingTask();

		super.onPause();
		Utils.dLog("End of onPause");
	}

	@Override
	protected void onStop() {

		Utils.dLog("Start of onStop");
		super.onStop();
		Utils.dLog("End of onStop");
	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	public void toggleStream() {

		mProgressBar.setVisibility(View.VISIBLE);

		if (mClient.isStreaming()) {
			// Stops the stream and disconnects from the RTSP server
			mClient.stopStream();
		} else {

			String ip, port, path;

			Utils.dLog("Server Url: " + serverUrl);
			Utils.dLog("Username: " + username);
			Utils.dLog("Password: " + password);

			// We parse the URI written in the Editext
			Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
			// Matcher m = uri.matcher(mEditTextURI.getText()); m.find();
			Matcher m = uri.matcher(serverUrl);
			m.find();
			ip = m.group(1);
			port = m.group(2);
			path = m.group(3);

			// mClient.setCredentials(mEditTextUsername.getText().toString(),
			// mEditTextPassword.getText().toString());
			mClient.setCredentials(username, password);
			mClient.setServerAddress(ip, Integer.parseInt(port));
			mClient.setStreamPath("/" + path);
			mClient.startStream();

			// View view = findViewById(R.id.view_layer); view.bringToFront();

		}
	}

	private void logError(final String msg) {
		final String error = (msg == null) ? "Error unknown" : msg;
		// Displays a popup to report the eror to the user
		AlertDialog.Builder builder = new AlertDialog.Builder(
				LiveStreamingActivity.this);
		builder.setMessage(msg).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onBitrareUpdate(long bitrate) {
		// mTextBitrate.setText("" + bitrate / 1000 + " kbps");
	}

	@Override
	public void onPreviewStarted() {

	}

	@Override
	public void onSessionConfigured() {

	}

	@Override
	public void onSessionStarted() {

		mProgressBar.setVisibility(View.GONE);
		Utils.showToast(this, "Tap to view options");
	}

	@Override
	public void onSessionStopped() {
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		mProgressBar.setVisibility(View.GONE);
		switch (reason) {
		case Session.ERROR_CAMERA_ALREADY_IN_USE:
			break;
		case Session.ERROR_CAMERA_HAS_NO_FLASH:
			break;
		case Session.ERROR_INVALID_SURFACE:
			break;
		case Session.ERROR_STORAGE_NOT_READY:
			break;
		case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:
			VideoQuality quality = mSession.getVideoTrack().getVideoQuality();
			logError("The following settings are not supported on this phone: "
					+ quality.toString() + " " + "(" + e.getMessage() + ")");
			e.printStackTrace();
			return;
		case Session.ERROR_OTHER:
			break;
		}

		if (e != null) {
			logError(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void onRtspUpdate(int message, Exception e) {
		switch (message) {
		case RtspClient.ERROR_CONNECTION_FAILED:
		case RtspClient.ERROR_WRONG_CREDENTIALS:
			mProgressBar.setVisibility(View.GONE);
			// enableUI();
			logError(e.getMessage());
			e.printStackTrace();
			break;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mSession.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mClient.stopStream();
	}

	private GestureDetector createGestureDetector(final Context context) {

		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {

				if (gesture == Gesture.TAP) {
					Utils.dLog("Gesture.TAP");
					mAudioManager.playSoundEffect(Sounds.TAP);
					openOptionsMenu();
					return true;
				} else if (gesture == Gesture.SWIPE_DOWN) {
					Utils.dLog("Gesture.SWIPE_DOWN");
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					Intent intent = new Intent(LiveStreamingActivity.this,
							HomeActivity.class);
					startActivity(intent);
					finish();
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

	// Send generic motion events to the gesture detector

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

	private void selectQuality(String setting) {

		Pattern pattern = Pattern.compile("(\\d+)x(\\d+)\\D+(\\d+)\\D+(\\d+)");
		Matcher matcher = pattern.matcher(setting);

		matcher.find();
		int width = Integer.parseInt(matcher.group(1));
		int height = Integer.parseInt(matcher.group(2));
		int framerate = Integer.parseInt(matcher.group(3));
		int bitrate = Integer.parseInt(matcher.group(4)) * 1000;

		mSession.setVideoQuality(new VideoQuality(width, height, framerate,
				bitrate));
		// showToast(setting);
		// Log.d(TAG, "Selected resolution: "+ setting);
	}

	public static Intent newIntent(Context context) {
		Intent intent = new Intent(context, LiveStreamingActivity.class);
		return intent;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();

		// Start adding start/stop streaming in menu
		if (mClient.isStreaming()) {
			menu.add(0, 1, Menu.NONE,
					getResources().getString(R.string.menu_stop_streaming));
		} else {
			menu.add(0, 1, Menu.NONE,
					getResources().getString(R.string.menu_start_streaming));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		mAudioManager.playSoundEffect(Sounds.TAP);

		switch (item.getItemId()) {
		case 1:

			if (mClient.isStreaming()) {
				mClient.stopStream();
				mSurfaceView.setVisibility(View.GONE);
				textViewNotStreaming.setVisibility(View.VISIBLE);
				transparentView.setBackgroundColor(Color.WHITE);

			} else {
				mSurfaceView.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
				textViewNotStreaming.setVisibility(View.GONE);
				transparentView.setBackgroundColor(Color.TRANSPARENT);
				mClient.startStream();

			}

			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}