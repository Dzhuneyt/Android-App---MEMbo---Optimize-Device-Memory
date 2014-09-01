package com.hasmobi.rambo.utils;

public abstract class Values {

	// Used for logging
	public static final String DEBUG_TAG = "RAM-BO";
	public static final String ADMOB_AD_UNIT_ID = "a1507c306fdcc36";

	// Name of the SharedPreferences file for the whitelisted apps
	public static final String EXCLUDED_LIST_FILE = "excluded_list";

	// Intent actions to used mainly by the Widgets and BroadcastReceivers
	public static final String CLEAR_RAM = "ClearRam";
	public static final String UPDATE_WIDGETS = "UpdateWidgets";
	public static final String ACTION_AUTOBOOST_ENABLE = "AutoRamClear";
	public static final String ACTION_DO_AUTOBOOST = "AutoRamClearRun";

	public static final int notificationID = 10001;

	public static final Boolean DEBUG_MODE = false;

	public static final String FEEDBACK_EMAIL = "feedback@hasmobi.com";

	public static final int REMIND_RATE_EVERY_N_START = 15;

	public static final int AUTOBOOST_DELAY_SECONDS = 60 * 5;

	public static final int PIE_UPDATE_INTERVAL = 1500; // milliseconds
	
	// Constants related to Analytics SDK custom events
	public static final String ANALYTICS_CATEGORY_RAM = "Ram";
	public static final String ANALYTICS_ACTION_OPTIMIZE = "Optimized";
	
	public static final String ANALYTICS_LABEL_CONTEXT = "Context";
	public static final String ANALYTICS_LABEL_OPTIMIZED_APPS = "Closed apps";
	public static final String ANALYTICS_CONTEXTS_FOOTER_BUTTON = "Footer button";
}
