package com.android.glasstools.anpr;

import hu.jodolgok.anpr.library.ANPRLibrary;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.glasstools.HomeActivity;
import com.android.glasstools.R;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardBuilder.Layout;

public class NumberPlateRecognitionActivity extends Activity {

	private GestureDetector mGestureDetector;
	private AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mGestureDetector = createGestureDetector(this);

		CardBuilder cardBuilder = new CardBuilder(getApplicationContext(),
				Layout.TEXT);
		cardBuilder.setText("Sorry, This part is under maintenance.");
		setContentView(cardBuilder.getView());
		
		
		 // Handler to send UI updates
        handler = new Handler();
        
        
        // Init ANPR library
        // In the second parameter you will have to pass your license key!
        // Without that the ANPR Library will work in demo mode (will hide some characters from the result)
        anprLib = ANPRLibrary.getInstance(this, ""); 

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
				
		super.onResume();
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inScaled = false;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.carsmall, bitmapOptions);
		
		byte[] captureData = Session.getInstant().getImageBytes();
		Bitmap captureBitmap = BitmapFactory.decodeByteArray(captureData , 0, captureData .length);
		
		captureBitmap = getResizedBitmap(captureBitmap, 640);
		
		CardBuilder cardBuilder = new CardBuilder(getApplicationContext(),
				Layout.TEXT);
		cardBuilder.addImage(captureBitmap);
		setContentView(cardBuilder.getView());

		//We use:
		// - original bitmap without downscaling to the allowed maximum dimensions (which is 1024x1024!!!)
		// - automatic image rotation (based on light and dark fields on the photo)
		// - left, top, right, bottom clipping: 5% 15% 5% 0% 
		// - focus point is approximately at the center (50%) horizontally and at the 75% vertically (of the ORIGINAL IMAGE)
		if (anprLib.loadImage(captureBitmap, ANPRLibrary.ROTATION_AUTO, 5, 15, 95, 100, 50, 75)){
			processPhoto();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		processThread = null; //Will interrupt the processor thread
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
					Intent intent = new Intent(
							NumberPlateRecognitionActivity.this,
							HomeActivity.class);
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
		Log.d(NumberPlateRecognitionActivity.this.getLocalClassName(), message);
	}

	public void eLog(String message) {
		Log.e(NumberPlateRecognitionActivity.this.getLocalClassName(), message);
	}
	
	


	private ANPRLibrary anprLib;
	private Handler handler = null;
	private volatile Thread processThread = null;
	
	   
    /**
     * Starts and maintains a thread for processing, and publishes the result.
     */
	private void processPhoto() {
		processThread = new Thread() {
			public void run() {

				// Set the thread priority to high but less then the GUI (or
				// anything with higher priority)
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY
								+ android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);

				// Main cycle until ANPR lib finishes or user interrupts (closes
				// the sample app)
				do {
					anprLib.process();

					// Publish the percentage
					handler.post(new Runnable() {
						public void run() {
							// The ANPRLib's state can jump back!
							int value =anprLib.getCurrentState();
							Log.d("processing complete : ", value + "%");
						}
					});
				} while (anprLib.isProcessing()
						&& Thread.currentThread() == processThread);

				// Publish the final result, and re-enable button
				if (Thread.currentThread() == processThread) {
					handler.post(new Runnable() {
						public void run() {
							String result = anprLib.getResult();
							if (anprLib.isFinishedWithSuccess()
									&& result != null && result.length() > 0) {
								Toast.makeText(getApplicationContext(), result,
										Toast.LENGTH_LONG).show();
								Log.d("ANPR Result ", result);
							} else {
								Toast.makeText(
										getApplicationContext(),
										"Failed! (" + anprLib.getFailReason()
												+ ")", Toast.LENGTH_LONG)
										.show();
								Log.d("ANPR Result ",
										"Failed! (" + anprLib.getFailReason()
												+ ")");
							}
						}
					});
				}
			}
		};
		processThread.start();
	}
    
	
	private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
}



}
