package org.treant.standuptimer.utils;

import android.graphics.Color;

public class TimeFormatHelper {
	/**
	 * ��--->��(**:**)
	 * @param seconds
	 * @return
	 */
	public static String formatTime(int seconds){
		return Integer.toString(seconds/60)+":"+padWithZero(seconds%60);  // minute:second
	}
	//С��10sʱǰ���0
	public static String padWithZero(int seconds){
		return seconds<10?"0"+seconds:Integer.toString(seconds);
	}
	public static int determineColor(int seconds,int warningTime) {
		if(seconds<=0){
			return Color.RED;
		}else if(seconds<warningTime){
			return Color.YELLOW;
		}else{
			return Color.GREEN;
		}
	}
}
