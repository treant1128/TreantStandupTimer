package org.treant.standuptimer.dao;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.List;

import org.treant.standuptimer.model.Team;
import org.treant.standuptimer.utils.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TeamDAO extends DAOHelper {

	public TeamDAO(Context ctx){
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public Team save(Team team){
		SQLiteDatabase db=getWritableDatabase();
		if(team.getId()!=null){
			return updateExistingTeam(db, team);
		}else{
			return createNewTeam(db, team);
		}
	}
	//针对SQLiteDatabase的操作  都用try  finally 保证Cursor被close	
	public Team findById(Long id){
		Team team=null;
		Cursor cursor=null;
		try{
			SQLiteDatabase db=getReadableDatabase();
			cursor=db.query(TEAMS_TABLE_NAME, TEAMS_ALL_COLUMNS, _ID+" =?", 
					new String[]{id.toString()}, null, null, null);
			if(cursor.getCount()==1){
				if(cursor.moveToFirst()){
					String name=cursor.getString(1);
					team=new Team(id, name);
				}
			}
		}finally{
			closeCursor(cursor);
		}
		return team;
	}
	
	public Team findByName(String name){
		name=name.trim();
		Cursor cursor=null;
		Team team=null;
		try{
			SQLiteDatabase db=getReadableDatabase();
			cursor=db.query(TEAMS_TABLE_NAME, TEAMS_ALL_COLUMNS, TEAMS_NAME+" =?", 
					new String[]{name}, null, null, null);
			if(cursor.getCount()==1){
				if(cursor.moveToFirst()){
					long id=cursor.getLong(0);
					name=cursor.getString(1);
					team=new Team(id, name);
				}
			}
		}finally{
			closeCursor(cursor);
		}
		Logger.d(team==null?"Unsuccessfully":"Successfully"+" found team with a name of "+name);
		return team;
	}
	
	public List<String> findAllTeamNames(){
		List<String>teamNames=new ArrayList<String>();
		Cursor cursor=null;
		try{
			SQLiteDatabase db=getReadableDatabase();
			cursor=db.query(TEAMS_TABLE_NAME, new String[]{TEAMS_NAME}, // 无须TEAMS_ALL_COLUMNS  return List<String>  not List<Team> 只需查询一列 
					null, null, null, null, TEAMS_NAME);
			while(cursor.moveToNext()){
				teamNames.add(cursor.getString(0));     //只有一列TEAMS_NAME   从零开始
			}
		}finally{
			closeCursor(cursor);
		}
		Logger.d("查询所有TeamNames and size= "+teamNames.size());
		return teamNames;
	}
	
	public void deleteAll(){
		Logger.d("DeleteAllTeams");
		SQLiteDatabase db=getWritableDatabase();
		db.delete(TEAMS_TABLE_NAME, null, null);
	}
	
	public void delete(Team team){
		Logger.d("Delete team with a name of--- "+team.getName());
		if(team.getId()!=null){
			SQLiteDatabase db=getWritableDatabase();
			db.delete(TEAMS_TABLE_NAME, _ID+" =?", new String[]{team.getId().toString()});
		}
	}
	
	private Team updateExistingTeam(SQLiteDatabase db, Team team){
		Logger.d("更新  team with the name of----"+team.getName());
		ContentValues values=new ContentValues();
		values.put(TEAMS_NAME, team.getName());
		long id=db.update(TEAMS_TABLE_NAME, values, _ID+" =?", new String[]{team.getId().toString()});  // update name by ID
		return new Team(id, team.getName());
	}
	
	private Team createNewTeam(SQLiteDatabase db, Team team){
		if(team.getName()==null||team.getName().trim().length()==0){
			String msg="Attemping to create a team with an empty name";
			Logger.w(msg);
			throw new InvalidTeamNameException(msg);
		}
		if(attemptingToCreateDuplicateTeam(team)){
			String msg="Attempting to create Duplicate team with name of "+team.getName();
			Logger.w(msg);
			throw new DuplicateException(msg);
		}
		Logger.d("Creating new team with a name of '"+team.getName()+"'");
		ContentValues values=new ContentValues();
		values.put(TEAMS_NAME, team.getName());
		long id=db.insertOrThrow(TEAMS_TABLE_NAME, null, values);
		return new Team(id, team.getName());
	}
	
	public boolean attemptingToCreateDuplicateTeam(Team team){
		return  team.getId()==null && findByName(team.getName())!=null;
	}
}
