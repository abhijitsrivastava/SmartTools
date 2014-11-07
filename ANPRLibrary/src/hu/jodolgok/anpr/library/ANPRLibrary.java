package hu.jodolgok.anpr.library;

import hu.jodolgok.anpr.R;
import hu.jodolgok.anpr.core.ImageOwner;
import hu.jodolgok.anpr.core.Logger;
import hu.jodolgok.anpr.core.PlateProcessor;
import hu.jodolgok.anpr.core.ResourceOwner;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Wrapper class for PlateProcessor (distributed as a pure Java library).
 * It implements the required functions for the PlateProcessor to:
 * - access android-specific resource files
 * - retrieve the image data
 * - log anything
 * It is designed to be used as a singleton!
 * @author Zoltan Riczko
 */
public class ANPRLibrary implements ResourceOwner, ImageOwner, Logger{

	private static final String LOG_TAG = "ANPR Library";

	// Constants to configure behaviour of plateprocessor
	public static final int ROTATION_AUTO = 0; 
	public static final int ROTATION_KEEP = 1; 
	public static final int ROTATION_P90 = 2; 
	public static final int ROTATION_M90 = 3; 
	public static final int ROTATION_180 = 4; 

	// Make easy to understand the plateprocessor's current state (internal states are undocumented!)
	public static final int STATE_UNKNOWN = -1;
	public static final int STATE_INITED = 0;	
	public static final int STATE_FINISHED = 150;

	// Result constants
	public static final int RESULT_UNKNOWN = -1;
	public static final int RESULT_FAILED = 0;
	public static final int RESULT_SUCCESS = 1;
	
	// Fail codes
	public static final int FAILREASON_UNKNOWN = -1;
	public static final int FAILREASON_OUTOFMEM = 0;
	public static final int FAILREASON_TOOHOMOGEN = 1;
	public static final int FAILREASON_TIMEOUT = 2;

	//You can modify the following arrays to limit the library to a particular (or additional) character set.
	//It may have significant effects on performance.
	private final static String[] useValues = {
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
		
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
		
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
		
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
		"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "Ä", "Ö", "Ü"};
	
	private final static int[] withResources = {
		R.drawable.m0, R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4, R.drawable.m5, R.drawable.m6, R.drawable.m7, R.drawable.m8, R.drawable.m9,
		R.drawable.ma, R.drawable.mb, R.drawable.mc, R.drawable.md, R.drawable.me, R.drawable.mf, R.drawable.mg, R.drawable.mh, R.drawable.mi, R.drawable.mj, R.drawable.mk, R.drawable.ml, R.drawable.mm, R.drawable.mn, R.drawable.mo, R.drawable.mp, R.drawable.mq, R.drawable.mr, R.drawable.ms, R.drawable.mt, R.drawable.mu, R.drawable.mv, R.drawable.mw, R.drawable.mx, R.drawable.my, R.drawable.mz,
		
		R.drawable.ot_m0, R.drawable.ot_m1, R.drawable.ot_m2, R.drawable.ot_m3, R.drawable.ot_m4, R.drawable.ot_m5, R.drawable.ot_m6, R.drawable.ot_m7, R.drawable.ot_m8, R.drawable.ot_m9,
		R.drawable.ot_ma, R.drawable.ot_mb, R.drawable.ot_mc, R.drawable.ot_md, R.drawable.ot_me, R.drawable.ot_mf, R.drawable.ot_mg, R.drawable.ot_mh, R.drawable.ot_mi, R.drawable.ot_mj, R.drawable.ot_mk, R.drawable.ot_ml, R.drawable.ot_mm, R.drawable.ot_mn, R.drawable.ot_mo, R.drawable.mp, R.drawable.ot_mq, R.drawable.ot_mr, R.drawable.ot_ms, R.drawable.ot_mt, R.drawable.ot_mu, R.drawable.ot_mv, R.drawable.ot_mw, R.drawable.ot_mx, R.drawable.ot_my, R.drawable.ot_mz,
		
		R.drawable.uk_m0, R.drawable.uk_m1, R.drawable.uk_m2, R.drawable.uk_m3, R.drawable.uk_m4, R.drawable.uk_m5, R.drawable.uk_m6, R.drawable.uk_m7, R.drawable.uk_m8, R.drawable.uk_m9,
		R.drawable.uk_ma, R.drawable.uk_mb, R.drawable.uk_mc, R.drawable.uk_md, R.drawable.uk_me, R.drawable.uk_mf, R.drawable.uk_mg, R.drawable.uk_mh, R.drawable.uk_mi, R.drawable.uk_mj, R.drawable.uk_mk, R.drawable.uk_ml, R.drawable.uk_mm, R.drawable.uk_mn, R.drawable.uk_mo, R.drawable.uk_mp, R.drawable.uk_mq, R.drawable.uk_mr, R.drawable.uk_ms, R.drawable.uk_mt, R.drawable.uk_mu, R.drawable.uk_mv, R.drawable.uk_mw, R.drawable.uk_mx, R.drawable.uk_my, R.drawable.uk_mz,
		
		R.drawable.de_m0, R.drawable.de_m1, R.drawable.de_m2, R.drawable.de_m3, R.drawable.de_m4, R.drawable.de_m5, R.drawable.de_m6, R.drawable.de_m7, R.drawable.de_m8, R.drawable.de_m9,
		R.drawable.de_ma, R.drawable.de_mb, R.drawable.de_mc, R.drawable.de_md, R.drawable.de_me, R.drawable.de_mf, R.drawable.de_mg, R.drawable.de_mh, R.drawable.de_mi, R.drawable.de_mj, R.drawable.de_mk, R.drawable.de_ml, R.drawable.de_mm, R.drawable.de_mn, R.drawable.de_mo, R.drawable.de_mp, R.drawable.de_mq, R.drawable.de_mr, R.drawable.de_ms, R.drawable.de_mt, R.drawable.de_mu, R.drawable.de_mv, R.drawable.de_mw, R.drawable.de_mx, R.drawable.de_my, R.drawable.de_mz, R.drawable.de_mua, R.drawable.de_muo, R.drawable.de_muu};

	private static ANPRLibrary instance = null;
	
	private Context context = null;
	private String key = null;
	
	private int res_width = 0;
	private int res_height = 0;
	private int[] resPixels = null;
	
	private Bitmap tmpBitmap = null;
	
	public static ANPRLibrary getInstance(Context context, String key){
		if (instance==null){
			instance = new ANPRLibrary(context, key);
		}
		return instance;
	}
	
	private ANPRLibrary(Context context, String key){
		this.context = context;
		this.key = key;
		PlateProcessor.init((ResourceOwner)this, useValues, withResources, (ImageOwner)null, (Logger)this, key, 0, 0, 0, 0, 0, 0, 0);
	}

	public boolean loadImage(Bitmap bmp, int r, int crl, int crt, int crr, int crb, int cx, int cy){
		boolean ret = false;
		try{
			tmpBitmap = bmp;
			PlateProcessor.init(this, null, null, this, this, key, r, crl, crt, crr, crb, cx, cy);
			ret = true;
		}
		catch(Exception ex){
		}
		return ret;
	}
	
	public int process(){
		return PlateProcessor.process();		
	}
	
	public int getCurrentState(){
		return PlateProcessor.getCurrentState();
	}

	public boolean isProcessing(){
		return PlateProcessor.processing(); 
	}
		
	public boolean isFinished(){
		return !isProcessing();
	}
	
	public boolean isFinishedWithSuccess(){
		return (isFinished() && PlateProcessor.isFinishedWithSuccess());
	}
	
	public String getResult(){
		if (isFinishedWithSuccess())
			return PlateProcessor.getResult();
		else
			return "";
	}
	
	public int getFailReason(){
		if (isFinished()){
			if (isFinishedWithSuccess())
				return FAILREASON_UNKNOWN;
			else
				return PlateProcessor.getFailReason();
		}
		else
			return FAILREASON_UNKNOWN;
	}
	
	public void reset(){
		res_width = 0;
		res_height = 0;
		resPixels = null;
		tmpBitmap = null;
	}
	
	// hu.jodolgok.anpr.core.ResourceOwner interface
	
	public boolean loadResource(int res_id){
		boolean ret = false;
		if (context!=null){
			try{
				BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
				bitmapOptions.inScaled = false;
				Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), res_id, bitmapOptions);
				res_width = bmp.getWidth();
				res_height = bmp.getHeight();
				resPixels = new int[res_width * res_height];
				bmp.getPixels(resPixels, 0, res_width, 0, 0, res_width, res_height);
				ret = true;
			}
			catch(Exception ex){
			}
		}
		return ret;
	}
	
	public int getResourceWidth(){
		return res_width;
	}
	
	public int getResourceHeight(){
		return res_height;
	}
	
	public int[] getResourcePixels(){
		return resPixels;
	}

    // hu.jodolgok.anpr.core.ResourceOwner interface
    
    public int getImageWidth(){
		return tmpBitmap!=null ? tmpBitmap.getWidth() : 0;
	}
	
	public int getImageHeight(){
		return tmpBitmap!=null ? tmpBitmap.getHeight() : 0;
	}
	
	public void getImagePixels(int[] pixels, int offset, int stride, int xo, int yo, int width, int height){
		if (tmpBitmap!=null){
			tmpBitmap.getPixels(pixels, offset, stride, xo, yo, width, height);
		}
	}

	// hu.jodolgok.anpr.core.Logger interface

	public void anprLogI(String msg){
		Log.i(LOG_TAG, msg);
	}

	public void anprLogW(String msg){
		Log.w(LOG_TAG, msg);
	}

	public void anprLogE(String msg){
		Log.e(LOG_TAG, msg);
	}
}
