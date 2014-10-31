package com.android.glasstools.streaming;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Utils {

	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_URL = "server_url";
	public static final String KEY_MODE = "streaming_mode";
	public static final String KEY_GLASS_ID = "glass_id";
	
	private static final String TAG = "IStream";
	

	public static void saveStringPreferences(Context context, String key,
			String value) {
		SharedPreferences sPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void deleteStringPreferences(Context context, String key) {
		SharedPreferences sPrefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sPrefs.edit();
		editor.remove(key);
		editor.commit();
	}

	public static String getStringPreferences(Context context, String key) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String savedPref = sharedPreferences.getString(key, "");
		return savedPref;
	}

	public static void showToast(final Activity context, final String message) {
		context.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, message, Toast.LENGTH_LONG)
						.show();
			}
		});
	}
	
	public static HttpResponse makeRequest(String url, JSONObject params) throws ClientProtocolException, IOException {
		
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        //passes the results to a string builder/entity
        StringEntity se = new StringEntity(params.toString());

        //sets the post request as the resulting string
        httppost.setEntity(se);
        
        //sets a request header so the page receving the request
        //will know what to do with it
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");
		
        // Execute HTTP Post Request
        return httpclient.execute(httppost);
	}
	
	/*public static Location getLastLocation(Context context) {
	      LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	      Criteria criteria = new Criteria();
	      criteria.setAccuracy(Criteria.NO_REQUIREMENT);
	      List<String> providers = manager.getProviders(criteria, true);
	      List<Location> locations = new ArrayList<Location>();
	      for (String provider : providers) {
	           Location location = manager.getLastKnownLocation(provider);
	           if (location != null && location.getAccuracy()!=0.0) {
	               locations.add(location);
	           }
	      }
	      Collections.sort(locations, new Comparator<Location>() {
	          @Override
	          public int compare(Location location, Location location2) {
	              return (int) (location.getAccuracy() - location2.getAccuracy());
	          }
	      });
	      if (locations.size() > 0) {
	          return locations.get(0);
	      }
	      return null;
	 }*/
	
	public static Location getLastLocation(Context context) {
	    Location result = null;
	    LocationManager locationManager;
	    Criteria locationCriteria;
	    List<String> providers;

	    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	    locationCriteria = new Criteria();
	    locationCriteria.setAccuracy(Criteria.NO_REQUIREMENT);
	    providers = locationManager.getProviders(locationCriteria, true);

	    // Note that providers = locatoinManager.getAllProviders(); is not used because the
	    // list might contain disabled providers or providers that are not allowed to be called.

	    //Note that getAccuracy can return 0, indicating that there is no known accuracy.

	    Log.d("EduShield", providers.size() + " :Providers found");
	    for (String provider : providers) {
	        Location location = locationManager.getLastKnownLocation(provider);
	        if (result == null) {
	            result = location;
	        } 
	        else if (result.getAccuracy() == 0.0) {
	            if (location.getAccuracy() != 0.0) {
	                result = location;
	                break;
	            } else {
	                if (result.getAccuracy() > location.getAccuracy()) {
	                    result = location;
	                }
	            }
	        }
	    }

	    return result;
	}
	
	public static String getGlassSerial(Context context) {
		String prop = "ro.serialno.glass";
		String result = null;
		try {
			Class SystemProperties = context.getClassLoader().loadClass(
					"android.os.SystemProperties");

			Class[] paramtypes = new Class[1];
			paramtypes[0] = String.class;
			Object[] paramvalues = new Object[1];
			paramvalues[0] = new String(prop);
			Method get = SystemProperties.getMethod("get", paramtypes);

			result = (String) get.invoke(SystemProperties, paramvalues);
		} catch (Exception e) {
			result = null;
		}
		return result;
	}
	
	public static void dLog(String message) {
		Log.d(TAG, message);
	}
	
	public static void eLog(String message) {
		Log.e(TAG, message);
	}
	

	public static boolean isCommandCodeYellow(String message) {
		
		return message.toLowerCase().contains("yellow");
	}
	
	public static boolean isCommandCodeRed(String message) {
		
		return message.toLowerCase().contains("red");
	}
}
