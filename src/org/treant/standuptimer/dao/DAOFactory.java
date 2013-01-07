package org.treant.standuptimer.dao;

import android.content.Context;

public class DAOFactory {
	private static DAOFactory instance = null;
	private Context globalContext = null;
	private boolean cacheDAOInstances = false;
	private TeamDAO cachedTeamDAO = null;
	private MeetingDAO cachedMeetingDAO = null;

	private DAOFactory() {

	}

	/**
	 * 单例模式
	 * 
	 * @return
	 */
	public static DAOFactory getInstance() {
		if (instance == null) {
			instance = new DAOFactory();
		}
		return instance;
	}

	public TeamDAO getTeamDAO(Context context) {
		if (cacheDAOInstances) {
			if (cachedTeamDAO == null) { // 如果 cachedTeamDAO为null就创建新对象并返回
				cachedTeamDAO = new TeamDAO(getProperDAOContext(context));
			}
			return cachedTeamDAO;
		} else {
			return new TeamDAO(getProperDAOContext(context));
		}
	}

	public MeetingDAO getMeetingDAO(Context context) {
		if (cacheDAOInstances) {
			if (cachedMeetingDAO == null) {
				cachedMeetingDAO = new MeetingDAO(getProperDAOContext(context));
			}
			return cachedMeetingDAO;
		} else {
			return new MeetingDAO(getProperDAOContext(context));
		}
	}

	private Context getProperDAOContext(Context context) {
		if (globalContext != null) {
			return globalContext;
		} else {
			return context;
		}
	}
	
//	public void setGlobalContet(Context context){
//		this.globalContext=context;
//	}
//	public void setCachedDAOInstances(boolean cacheDAOInstances){
//		this.cacheDAOInstances=cacheDAOInstances;
//	}
}
