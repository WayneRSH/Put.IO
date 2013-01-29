package util;

import java.util.prefs.Preferences;

import start.Start;

public class UserPreferences {
    // Preferences object
    public static Preferences PREFS;
    // User preference constants
    public static final String MAX_PARALLEL_DOWNLOADS = "MAX_PARALLEL_DOWNLOADS";
    public static final String DOWNLOAD_PART_COUNT = "DOWNLOAD_PART_COUNT";
    public static final String SERVER_CHECK_INTERVAL = "SERVER_CHECK_INTERVAL";
    public static final String USE_PROXY = "USE_PROXY";
    public static final String PROXY_ADDRESS = "PROXY_ADDRESS";
    public static final String PROXY_PORT = "PROXY_PORT";
    public static final String AUTO_CONNECT = "AUTO_CONNECT";
    public static final String START_IN_TRAY = "START_IN_TRAY";
    public static final String DOWNLOAD_AUTOMATICALLY = "DOWNLOAD_AUTOMATICALLY";
    public static final String DELETE_EMPTY_FOLDERS = "DELETE_EMPTY_FOLDERS";
    public static final String FILE_SIZE_CHECK = "FILE_SIZE_CHECK";
    public static final String FILE_SIZE_FOR_CHECK = "FILE_SIZE_FOR_CHECK";
    public static final String FILE_SIZE_DELETE = "FILE_SIZE_DELETE";
    public static final String DOWNLOAD_TARGET = "DOWNLOAD_TARGET";
    public static final String USERNAME = "USERNAME";
    public static final String API_SECRET = "API_SECRET";
    public static final String PASSWORD = "PASSWORD";
    public static final String USERTOKEN = "USERTOKEN";
    public static final String DONT_ASK_DOWNLOAD_AGAIN = "DONT_ASK_DOWNLOAD_AGAIN";
    public static final String BEHAVIOR_DOWNLOAD_AGAIN = "BEHAVIOR_DOWNLOAD_AGAIN";
    public static final String DONT_ASK_OVERWRITE = "DONT_ASK_OVERWRITE";
    public static final String BEHAVIOR_OVERWRITE = "BEHAVIOR_OVERWRITE";
    public static final String BEHAVIOR_SORT_BY = "BEHAVIOR_SORT_BY";
    public static final String LOAD_SHARED = "LOAD_SHARED";
    // Preference options
    public static final int OPTION_DOWNLOAD_AGAIN = 0;
    public static final int OPTION_OVERWRITE = 0;
    public static final int OPTION_SKIP = 1;
    public static final int OPTION_SKIP_DELETE= 2;
    public static final int OPTION_SKIP_SAME_SIZE = 3;
    public static final int OPTION_SKIP_SAME_SIZE_DELETE= 4;
    public static final int OPTION_SORT_BY_NAME = 0;
    public static final int OPTION_SORT_BY_DATE = 1;
    // User preferences
    public static int PREF_MAX_DOWNLOADS;
    public static int PREF_DOWNLOAD_PART_COUNT;
    public static int PREF_SERVER_CHECK_INTERVAL;
    public static boolean PREF_USE_PROXY;
    public static String PREF_PROXY_ADDRESS;
    public static String PREF_PROXY_PORT;
    public static boolean PREF_AUTO_CONNECT;
    public static boolean PREF_START_IN_TRAY;
    public static boolean PREF_AUTO_DOWNLOAD;
    public static boolean PREF_AUTO_CLEAN;
    public static boolean PREF_FILE_SIZE_CHECK;
    public static float PREF_FILE_SIZE_FOR_CHECK;
    public static boolean PREF_FILE_SIZE_DELETE;
    public static String PREF_DOWNLOAD_TARGET;
    public static String PREF_USERNAME;
    public static String PREF_API_SECRET;
    public static String PREF_PASSWORD;
    public static String PREF_USERTOKEN;
    public static boolean PREF_DONT_ASK_DOWNLOAD_AGAIN;
    public static int PREF_BEHAVIOR_DOWNLOAD_AGAIN;
    public static boolean PREF_DONT_ASK_OVERWRITE;
    public static int PREF_BEHAVIOR_OVERWRITE;
    public static int PREF_BEHAVIOR_SORT_BY;
    public static boolean PREF_LOAD_SHARED;

    public static void loadUserPreferences() {
	if (PREFS == null)
	    PREFS = Preferences.userNodeForPackage(Start.class);
	PREF_MAX_DOWNLOADS = PREFS.getInt(MAX_PARALLEL_DOWNLOADS, 3);
	PREF_DOWNLOAD_PART_COUNT = PREFS.getInt(DOWNLOAD_PART_COUNT, 3);
	PREF_SERVER_CHECK_INTERVAL = PREFS.getInt(SERVER_CHECK_INTERVAL, 60);
	PREF_USE_PROXY = PREFS.getBoolean(USE_PROXY, false);
	PREF_PROXY_ADDRESS = PREFS.get(PROXY_ADDRESS, "");
	PREF_PROXY_PORT = PREFS.get(PROXY_PORT, "");
	PREF_AUTO_CONNECT = PREFS.getBoolean(AUTO_CONNECT, false);
	PREF_START_IN_TRAY = PREFS.getBoolean(START_IN_TRAY, false);
	PREF_AUTO_DOWNLOAD = PREFS.getBoolean(DOWNLOAD_AUTOMATICALLY, false);
	PREF_AUTO_CLEAN = PREFS.getBoolean(DELETE_EMPTY_FOLDERS, false);
	PREF_FILE_SIZE_CHECK = PREFS.getBoolean(FILE_SIZE_CHECK, false);
	PREF_FILE_SIZE_FOR_CHECK = PREFS.getFloat(FILE_SIZE_FOR_CHECK, 5.0f);
	PREF_FILE_SIZE_DELETE = PREFS.getBoolean(FILE_SIZE_DELETE, false);
	PREF_DOWNLOAD_TARGET = PREFS.get(DOWNLOAD_TARGET, System.getProperty("user.dir"));
	PREF_USERNAME = PREFS.get(USERNAME, "");
	PREF_API_SECRET = PREFS.get(API_SECRET, "");
	PREF_PASSWORD = PREFS.get(PASSWORD, "");
	PREF_USERTOKEN = PREFS.get(USERTOKEN, "");
	PREF_DONT_ASK_DOWNLOAD_AGAIN = PREFS.getBoolean(DONT_ASK_DOWNLOAD_AGAIN, true);
	PREF_BEHAVIOR_DOWNLOAD_AGAIN = PREFS.getInt(BEHAVIOR_DOWNLOAD_AGAIN, 0);
	PREF_DONT_ASK_OVERWRITE = PREFS.getBoolean(DONT_ASK_OVERWRITE, true);
	PREF_BEHAVIOR_OVERWRITE = PREFS.getInt(BEHAVIOR_OVERWRITE, 0);
	PREF_BEHAVIOR_SORT_BY = PREFS.getInt(BEHAVIOR_SORT_BY, 0);
	PREF_LOAD_SHARED = PREFS.getBoolean(LOAD_SHARED, false);
    }

    public static void saveUserPreferences() {
	if (PREFS == null)
	    PREFS = Preferences.userNodeForPackage(Start.class);
	PREFS.putInt(MAX_PARALLEL_DOWNLOADS, PREF_MAX_DOWNLOADS);
	PREFS.putInt(DOWNLOAD_PART_COUNT, PREF_DOWNLOAD_PART_COUNT);
	PREFS.putInt(SERVER_CHECK_INTERVAL, PREF_SERVER_CHECK_INTERVAL);
	PREFS.putBoolean(USE_PROXY, PREF_USE_PROXY);
	PREFS.put(PROXY_ADDRESS, PREF_PROXY_ADDRESS);
	PREFS.put(PROXY_PORT, PREF_PROXY_PORT);
	PREFS.putBoolean(AUTO_CONNECT, PREF_AUTO_CONNECT);
	PREFS.putBoolean(START_IN_TRAY, PREF_START_IN_TRAY);
	PREFS.putBoolean(DOWNLOAD_AUTOMATICALLY, PREF_AUTO_DOWNLOAD);
	PREFS.putBoolean(DELETE_EMPTY_FOLDERS, PREF_AUTO_CLEAN);
	PREFS.putBoolean(FILE_SIZE_CHECK, PREF_FILE_SIZE_CHECK);
	PREFS.putFloat(FILE_SIZE_FOR_CHECK, PREF_FILE_SIZE_FOR_CHECK);
	PREFS.putBoolean(FILE_SIZE_DELETE, PREF_FILE_SIZE_DELETE);
	PREFS.put(DOWNLOAD_TARGET, PREF_DOWNLOAD_TARGET);
	PREFS.put(USERNAME, PREF_USERNAME);
	PREFS.put(API_SECRET, PREF_API_SECRET);
	PREFS.put(PASSWORD, PREF_PASSWORD);
	PREFS.put(USERTOKEN, PREF_USERTOKEN);
	PREFS.putBoolean(DONT_ASK_DOWNLOAD_AGAIN, PREF_DONT_ASK_DOWNLOAD_AGAIN);
	PREFS.putInt(BEHAVIOR_DOWNLOAD_AGAIN, PREF_BEHAVIOR_DOWNLOAD_AGAIN);
	PREFS.putBoolean(DONT_ASK_OVERWRITE, PREF_DONT_ASK_OVERWRITE);
	PREFS.putInt(BEHAVIOR_OVERWRITE, PREF_BEHAVIOR_OVERWRITE);
	PREFS.putInt(BEHAVIOR_SORT_BY, PREF_BEHAVIOR_SORT_BY);
	PREFS.putBoolean(LOAD_SHARED, PREF_LOAD_SHARED);
    }
}
