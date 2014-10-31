package com.android.glasstools.face;

import android.app.Activity;

public class Session {

	private byte[] imageBytes = null;
	private Activity activity = null;

	private static Session session = null;

	protected Session() {
		// Exists only to defeat instantiation.
	}

	public static Session getInstant() {
		if (session == null) {
			session = new Session();
		}
		return session;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
