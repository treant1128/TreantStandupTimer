package org.treant.standuptimer.dao;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.treant.standuptimer.model.Meeting;
import org.treant.standuptimer.model.Team;
import org.treant.standuptimer.utils.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MeetingDAO extends DAOHelper {
	public MeetingDAO(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}
	/**
	 * Save Meeting and return Meeting
	 * @param meeting
	 * @return
	 */
	public Meeting save(Meeting meeting){
		if(meeting.getId()==null){  // id not exist and create new Meeting
			SQLiteDatabase db=getWritableDatabase();
			return createNewMeeting(db, meeting);
		}else{   // throw Exception
			String msg="Attemping to update a existing meetings, entries cann't be updated";
			Logger.w(msg);
			throw new CannotUpdateMeetingException(msg);
		}
	}
	public Meeting findById(Long id){
		Cursor cursor=null;
		Meeting meeting=null;
		try{
			SQLiteDatabase db=getReadableDatabase();
			cursor=db.query(MEETINGS_TABLE_NAME, MEETINGS_ALL_COLUMNS, _ID+" =?", new String[]{id.toString()}, null, null, null);
			if(cursor.getCount()==1){
				if(cursor.moveToFirst()){
					meeting=createMeetingFromCursorData(cursor);
				}
			}
		}finally{
			closeCursor(cursor);
		}
		return meeting;
	}
	public List<Meeting> findAllByTeam(Team team){
		List<Meeting> meetings=new ArrayList<Meeting>();
		Cursor cursor=null;
		//�õ�����finally�е�ģ��һ���ܹ�ִ�е����ԡ����ĳ�δ���һ��Ҫִ�У������Ƿ����쳣����
		//�������Դ���ͷţ���Ӧ�÷���finallyģ���С�
		//���û��try,ֻ��finallyҲ�Ǳ��벻ͨ���ģ�try...û��catch��finally��tryҲ��ûʲô������
		try{
			SQLiteDatabase db=getReadableDatabase();
			cursor=db.query(MEETINGS_TABLE_NAME, MEETINGS_ALL_COLUMNS, MEETINGS_TEAM_NAME+" =?",
					new String[]{team.getName()}, null, null, MEETINGS_MEETING_TIME); //��ʱ������
			while(cursor.moveToNext()){
				meetings.add(createMeetingFromCursorData(cursor));
			}
		}finally{
			closeCursor(cursor);
		}
		Logger.d("���ֵ�Meetings������"+meetings.size());
		Collections.reverse(meetings);//���Ϸ���
		return meetings;
	}
	//ƴ��String��ʱ��Ҫע��ո�������
	public Meeting findAllByTeamAndDate(Team team, Date date){
		Cursor cursor=null;
		Meeting meeting=null;
		try{
			long startTime=date.getTime();
			Date endDate=new Date(startTime);
			endDate.setSeconds(endDate.getSeconds()+1);
			long endTime=endDate.getTime();     //end  1s  after start
			SQLiteDatabase db=getReadableDatabase();
			cursor=db.query(MEETINGS_TABLE_NAME, MEETINGS_ALL_COLUMNS, 
					MEETINGS_TEAM_NAME+" = ? and"+MEETINGS_MEETING_TIME+ " >=? and"+MEETINGS_MEETING_TIME+" <?", 
					new String[]{team.getName(), Long.toString(startTime), Long.toString(endTime)},
					null, null, null);
			if(cursor.getCount()==1){
				if(cursor.moveToFirst()){
					meeting=createMeetingFromCursorData(cursor);
				}
			}
		}finally{
			closeCursor(cursor);
		}
		return meeting;
	}
	public void deleteAll(){
		Logger.d("ɾ��ȫ����");
		SQLiteDatabase db=getWritableDatabase();
		db.delete(MEETINGS_TABLE_NAME, null, null);
	}
	public void deleteAllByTeam(Team team){
		Logger.d("ɾ��All Meeting for team--"+team.getName());
		SQLiteDatabase db=getWritableDatabase();
		db.delete(MEETINGS_TABLE_NAME, MEETINGS_TEAM_NAME+" =?", new String[]{team.getName()});
	}
	public void delete(Meeting meeting){
		Logger.d("DELETE--Team--"+meeting.getTeam().getName()+" with a date/time of-- '" + meeting.getDateTime() + "'");
		if(meeting.getId()!=null){  // delete by _ID
			SQLiteDatabase db=getWritableDatabase();
			db.delete(MEETINGS_TABLE_NAME, _ID+" =?", new String[]{meeting.getId().toString()});
		}
	}
	private Meeting createMeetingFromCursorData(Cursor cursor){
		long id=cursor.getLong(0);  //_ID
		String teamName=cursor.getString(1);  //MEETINGS_TEAM_NAME
		Date meetingTime=new Date(cursor.getLong(2));  //MEETINGS_MEETING_TIME
		int numParticipants=cursor.getInt(3);  //MEETINGS_NUM_PARTICIPANTS
		int individualStatusLength=cursor.getInt(4); //MEETINGS_INDIVIDUAL_STATUS_LENGTH
		int meetingLength=cursor.getInt(5);   //MEETINGS_MEETING_LENGTH
		int quickestStatus=cursor.getInt(6);  //MEETINGS_QUICKEST_STATUS
		int longestStatus=cursor.getInt(7);   //MEETINGS_LONGEST_STATUS
		return new Meeting(id, new Team(teamName), meetingTime, numParticipants, 
				individualStatusLength, meetingLength,quickestStatus, longestStatus);
	}
	/**
	 * Create New Meeting
	 * @param db
	 * @param meeting
	 * @return
	 */
	private Meeting createNewMeeting(SQLiteDatabase db, Meeting meeting){
		Logger.d("Creating new meeting for"+ meeting.getTeam().getName()+
				" with a data/time of **"+meeting.getDateTime()+"**");
		ContentValues values=createContentValues(meeting);
		long id=db.insert(MEETINGS_TEAM_NAME, null, values);
		return new Meeting(id, meeting);
	}
	/**
	 * ����Meeting������Ӧ��ContentValues
	 * @param meeting
	 * @return
	 */
	private ContentValues createContentValues(Meeting meeting){
		ContentValues values=new ContentValues();
		values.put(MEETINGS_TEAM_NAME, meeting.getTeam().getName());
		values.put(MEETINGS_MEETING_TIME, meeting.getDateTime().getTime());
		values.put(MEETINGS_NUM_PARTICIPANTS, meeting.getMeetingStatus().getNumParticipants());
		values.put(MEETINGS_INDIVIDUAL_STATUS_LENGTH, meeting.getMeetingStatus().getIndividualStatusLength());
		values.put(MEETINGS_MEETING_LENGTH, meeting.getMeetingStatus().getMeetingLength());
		values.put(MEETINGS_QUICKEST_STATUS, meeting.getMeetingStatus().getQuickestStatus());
		values.put(MEETINGS_LONGEST_STATUS, meeting.getMeetingStatus().getLongestStatus());
		return values;
	}
}
