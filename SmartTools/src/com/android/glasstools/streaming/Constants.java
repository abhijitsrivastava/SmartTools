package com.android.glasstools.streaming;

public class Constants {

	//public static final String CLIENT_ID = "523728010956-5ehm6m10hi1vqhpmm3771l9727v3ldko.apps.googleusercontent.com";
	public static final String CLIENT_ID = "edushield_";
	//public static final String CLIENT_SECRET = "-oObGCKTAqT4PyyYW0BbtQtr";
	public static final String CLIENT_SECRET = "-oObGCKTAqT4PyyYW0BbtQtr";
	
	public static final String HOST = "smtp.googlemail.com";
	public static final int PORT = 587;
	
	public static final String BASE_URL = "http://ec2-54-191-72-198.us-west-2.compute.amazonaws.com:8080/IStream/rest/istreamservice";
	public static final String BROKER_URL_SERVER = "tcp://ec2-54-187-106-124.us-west-2.compute.amazonaws.com:1883";
	
	//public static final String BASE_URL = "http://172.16.3.40:8080/EduShield/rest/edushieldservice";
	//public static final String BROKER_URL_SERVER = "tcp://172.16.3.40:1883";
	
	public static final String SERVICE_UPDATE_LOCATION = "/update-glass-location";
	public static final String SERVICE_UPDATE_STATUS = "/update-glass-status";
	public static final String SERVICE_REQUEST_BACKUP = "/request-backup";
	public static final String SERVICE_SEND_BACKUP_RESPONSE = "/send-backup-response";
	
	// JSON Keys
	public static final String KEY_GLASS_ID 	= "glass_id";
	public static final String KEY_LATITUDE 	= "latitude";
	public static final String KEY_LONGITUDE 	= "longitude";
	public static final String KEY_STATUS 	= "status";
	
	public static final String KEY_RESPONSE_MESSAGE 		= "message";
	public static final String KEY_RESPONSE_STATUS_CODE 	= "statusCode";
	// Status
	public static final String RESPONSE_STATUS_CODE_INACTIVE = "EDU_102";
	public static final String RESPONSE_STATUS_CODE_ACTIVE = "EDU_101";
	public static final String RESPONSE_STATUS_CODE_YELLOW = "EDU_103";
	public static final String RESPONSE_STATUS_CODE_RED = "EDU_104";
	public static final String RESPONSE_STATUS_CODE_BACKUP_REQUEST_SUCCESS = "EDU_115";
	public static final String RESPONSE_STATUS_CODE_BACKUP_REQUEST_FAILED = "EDU_116";
	
	public static final String USERNAME = "username@constant";
	public static final String PASSWORD = "password@md5";
	public static final int CONNECTION_ATTEMPT_LIMIT = 5;
	
	// Status
	public static final String STATUS_CODE_INACTIVE = "0";
	public static final String STATUS_CODE_ACTIVE = "1";
	public static final String STATUS_CODE_YELLOW = "2";
	public static final String STATUS_CODE_RED = "3";
	public static final String STATUS_CODE_ACTIVE_NOT_STREAMING = "4";
	
	// Value will be changed later
	public static String TOPIC_COMMAND_TO_GLASS = "eduglasses/edushield/command-to-glass/";
	public static String TOPIC_COMMAND_TO_HUB_RESPONSE = "eduglasses/edushield/command-to-hub/response/";
	public static String TOPIC_COMMAND_TO_CHAT = "eduglasses/edushield/chat-msg/";
	public static String TOPIC_COMMAND_TO_VOICE_CHAT = "eduglasses/edushield/voice-msg/";
	
	
}