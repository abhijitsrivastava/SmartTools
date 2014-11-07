package com.android.glasstools.face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.android.glasstools.HomeActivity;
import com.android.glasstools.R;
import com.android.glasstools.streaming.Utils;
import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;

public class ShowIdentityActivity extends BaseGlassActivity {

	private byte[] capturedImage = null;
	private String imagePath = null;
	
	private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.dLog("onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_show_identity);
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		capturedImage = Session.getInstant().getImageBytes();

	}

	@Override
	protected void onStart() {
		Utils.dLog("onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Utils.dLog("onResume");
		super.onResume();
		new FetchIdentityTask(this, capturedImage).execute(imagePath);

	}

	@Override
	protected void onPause() {
		Utils.dLog("onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Utils.dLog("onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Utils.dLog("onDestroy");
		super.onDestroy();
	}

	public void onCompleteUpdateStatusRequest(String identity) {
		Card card = new Card(this);
		if ("" != identity) {
			card.setText("This person is : " + identity);
		} else {
			card.setText("This person is : Unknown");
		}
		card.setFootnote("Swipe Down To Go Back");
		View cardView = card.getView();
		setContentView(cardView);
	}
	
	@Override
	protected boolean onSwipeDown() {
		
		Utils.dLog("Gesture.SWIPE_DOWN");
		mAudioManager.playSoundEffect(Sounds.DISMISSED);
		Intent intent = new Intent(ShowIdentityActivity.this,
				InputFaceActivity.class);
		startActivity(intent);
		finish();
		return false;
	}
}
