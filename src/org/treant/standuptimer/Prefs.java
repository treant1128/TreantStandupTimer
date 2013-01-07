package org.treant.standuptimer;

import android.content.Context;
import android.preference.PreferenceManager;

public class Prefs {

	private static final String VARIABLE_MEETING_LENGTH = "variable_meeting_length";
	private static final boolean VARIABLE_MEETING_LENGTH_DEFAULT = false;
	private static final String UNLIMITED_PARTICIPANTS = "unlimited_participants";
	private static final boolean UNLIMITED_PARTICIPANTS_DEFAULT = false;
	private static final String WARNING_TIME="warning_time";
	private static final int WARNING_TIME_DEFAULT=15;
	
	public static boolean allowVariableMeetingLength(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(VARIABLE_MEETING_LENGTH,
						VARIABLE_MEETING_LENGTH_DEFAULT);
	}

	public static boolean allowUnlimitedParticipants(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(UNLIMITED_PARTICIPANTS,
						UNLIMITED_PARTICIPANTS_DEFAULT);
	}

	public static int getWarningTime(Context context) {
		String value=PreferenceManager.getDefaultSharedPreferences(context)
				.getString(WARNING_TIME, Integer.toString(WARNING_TIME_DEFAULT));
		try{
			return Integer.parseInt(value);
		}catch(NumberFormatException e){
			setWarningTime(context, WARNING_TIME_DEFAULT);
			return WARNING_TIME_DEFAULT;
		}
	}
	public static void setWarningTime(Context context, int warningTime){
		PreferenceManager.getDefaultSharedPreferences(context).edit()
		.putString(WARNING_TIME, Integer.toString(warningTime)).commit();
	}
}
