package org.treant.standuptimer.model;

import java.util.Date;
import java.util.List;

import org.treant.standuptimer.dao.DAOFactory;
import org.treant.standuptimer.dao.MeetingDAO;
import org.treant.standuptimer.utils.Logger;

import android.content.Context;
import android.text.format.DateFormat;


public class Meeting {
	public static final String DESCRIPTION_FORMAT = "MM/dd/yyyy h:mm:ssaa";
	
	private Long id = null;
	private Team team = null;
	private Date dateTime = null;
	private MeetingStatus meetingStats = null;

	private static DAOFactory daoFactory=DAOFactory.getInstance();
	
	public Long getId() {
		return this.id;
	}
	public Team getTeam() {
		return this.team;
	}
	public Date getDateTime() {
		return this.dateTime;
	}
	public MeetingStatus getMeetingStatus(){
		return this.meetingStats;
	}
	public String getDescription(){
		return DateFormat.format(DESCRIPTION_FORMAT, dateTime).toString();
	}
	/**
	 * Meeting构造方法之判断篇  不带id
	 */
	public Meeting(Team team, Date dateTime, int numParticipants, int individualStatusLength,
			int meetingLength, int quickestStatus, int longestStatus){
		if(team==null){
			throw new IllegalArgumentException("Meeting team must not be null!");
		}else{
			this.team=new Team(team.getName());
		}
		if(dateTime==null){
			throw new IllegalArgumentException("Meeting date/time must not be null!");
		}else{
			this.dateTime=dateTime;
		}
		if(numParticipants<1){
			throw new IllegalArgumentException("At least 1 participant is needs for the Meeting!");
		}
		this.meetingStats=new MeetingStatus(numParticipants, individualStatusLength, meetingLength, quickestStatus, longestStatus);
	}
	/**
	 * Meeting构造方法之完整篇  带id
	 */
	public Meeting(Long id, Team team, Date dateTime, int numParticipants, int individualStatusLength, int meetingLength, int quickestStatus, int longestStatus){
		this(team, dateTime, numParticipants, individualStatusLength, meetingLength, quickestStatus, longestStatus);
		this.id=id;
	}
	public Meeting(Long id, Meeting meeting){
		this.id=id;
		this.team=new Team(meeting.getTeam().getName());
		this.dateTime=meeting.dateTime;
		this.meetingStats=meeting.meetingStats;
	}
	
	public Meeting save(Context context){
		Meeting meeting=null;
		MeetingDAO dao=null;
		try{
			dao=daoFactory.getMeetingDAO(context);
			meeting=dao.save(this);
		}catch(Exception e){
			Logger.e(e.getMessage());
		}finally{
			if(dao!=null){
				dao.close();
			}
		}
		return meeting;
	}
	public void delete(Context context){
		MeetingDAO dao=null;
		try{
			dao=daoFactory.getMeetingDAO(context);
			dao.delete(this);
		}finally{
			if(dao!=null){
				dao.close();
			}
		}
	}
	
	public static void deleteAllByTeam(Team team, Context context){
		MeetingDAO dao=null;
		try{
			dao=daoFactory.getMeetingDAO(context);
			dao.deleteAllByTeam(team);
		}finally{
			if(dao!=null){
				dao.close();
			}
		}
	}
	
	public static List<Meeting> findAllByTeam(Team team, Context context){
		MeetingDAO dao=null;
		try{
			dao=daoFactory.getMeetingDAO(context);
			return dao.findAllByTeam(team);
		}finally{
			if(dao!=null){
				dao.close();
			}
		}
	}
	
	public static Meeting findByTeamAndDate(Team team, Date date, Context context){
		MeetingDAO dao=null;
		try{
			dao=daoFactory.getMeetingDAO(context);
			return dao.findAllByTeamAndDate(team, date);
		}finally{
			if(dao!=null){
				dao.close();
			}
		}
	}
}
