package com.android.glasstools.picture;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardBuilder.Layout;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class ShowSavedPictureActivity extends Activity {

	private List<View> mCards;
	private CardScrollView mCardScrollView;
	ArrayList<FilenameDir> mPicInfo = new ArrayList<FilenameDir>();
	final private String mAppPicDir = Environment.getExternalStorageDirectory()
			+ "/" + Environment.DIRECTORY_PICTURES + "/SmartCamera";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		createCards();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	GetMoreInfoCardScrollAdapter adapter;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (null != mCards) {
			mCardScrollView = new CardScrollView(this);
			adapter = new GetMoreInfoCardScrollAdapter();
			mCardScrollView.setAdapter(adapter);
			mCardScrollView.activate();
			setContentView(mCardScrollView);
			Toast.makeText(this, "Scroll left/right to explore more",
					Toast.LENGTH_SHORT).show();
		}

		else {
			CardBuilder card = new CardBuilder(this, Layout.TEXT);
			card.setText("No image found.");
			View cardView = card.getView();
			setContentView(cardView);
			// dLog("Getting empty response");
		}

		mCardScrollView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent,
							final View view, int position, long id) {
						 String item = adapter.getItem(position).toString();
						 Toast.makeText(getApplicationContext(), item, Toast.LENGTH_LONG).show();
						 Log.d("item", item);
					}
				});
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
	}

	private void createCards() {
		mCards = new ArrayList<View>();
		getPictureLists(mAppPicDir);

		for (FilenameDir fDir : mPicInfo) {
			CardBuilder card = new CardBuilder(this, Layout.TEXT);
			card.setFootnote(fDir.mDirname + "/" + fDir.mFilename);

			// without scale down, you’ll get
			// "Bitmap too large to be uploaded into a texture" error
			Bitmap myBitmap = BitmapFactory.decodeFile(fDir.mDirname + "/"
					+ fDir.mFilename);
			int h = (int) (myBitmap.getHeight() * (640.0 / myBitmap.getWidth()));
			Bitmap scaled = Bitmap.createScaledBitmap(myBitmap, 640, h, true);

			try {
				File file = new File(mAppPicDir + "/scaled-" + fDir.mFilename);
				FileOutputStream fOut = new FileOutputStream(file);
				scaled.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
				fOut.close();
			} catch (Exception e) {
			}

			card.addImage(BitmapFactory.decodeFile(mAppPicDir + "/scaled-"
					+ fDir.mFilename));
			mCards.add(card.getView());
		}
	}

	private void getPictureLists(String directory) {
		File dir = new File(directory);
		File[] files = dir.listFiles();
		int count = 1; // used to limit the number of photos to add to
						// CardScrollView at one time
		for (File file : files) {
			if (file.isDirectory()) {
				if (file.getAbsolutePath().indexOf("/Pictures/cache") == -1)
					getPictureLists(file.getAbsolutePath());
			} else {
				if (file.getName().indexOf(".jpg") == -1)
					continue;

				if (count++ == 10)
					break; // likely out of memory if more than 20
				mPicInfo.add(new FilenameDir(file.getName(), directory));
			}
		}
	}

	private class FilenameDir {
		private String mFilename;
		private String mDirname;

		public FilenameDir(String filename, String dirname) {
			mFilename = filename;
			mDirname = dirname;
		}
	}

	private class GetMoreInfoCardScrollAdapter extends CardScrollAdapter {

		@Override
		public int getPosition(Object item) {
			return mCards.indexOf(item);
		}

		@Override
		public int getCount() {
			return mCards.size();
		}

		@Override
		public Object getItem(int position) {
			return mCards.get(position);
		}

		@Override
		public int getViewTypeCount() {
			return CardBuilder.getViewTypeCount();
		}

		/*
		 * @Override public int getItemViewType(int position) { return
		 * mCards.get(position).; }
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return (mCards.get(position));
		}
	}

}
