package org.treant.standuptimer;

import java.util.Timer;
import java.util.TimerTask;

import org.treant.standuptimer.model.Team;
import org.treant.standuptimer.utils.Logger;
import org.treant.standuptimer.utils.TimeFormatHelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class StandupTimer extends Activity implements OnClickListener{
	
	protected static final String REMAINING_INDIVIDUAL_SECONDS="remainingIndividualSeconds";
	protected static final String REMAINING_MEETING_SECONDS="remainingMeetingSeconds";
	protected static final String STARTING_INDIVIDUAL_SECONDS="startingIndividualSeconds";
	protected static final String COMPLETED_PARTICIPANTS="completedParticipants";
	protected static final String TOTAL_PARTICIPANTS="totalParticipants";
	protected static final String CURRENT_INDIVIDUAL_STATUS_SECONDS="currentIndividualStatusSeconds";
	protected static final String MEETING_START_TIME="meetingStartTime";
	protected static final String INDIVIDUAL_STATUS_END_TIME="individualStatusEndTime";
	protected static final String QUICKEST_STATUS="quickestStatus";
	protected static final String LONGEST_STATUS="longestStatus";
	
	private int remainingIndividualSeconds=0; //个人剩余秒数
	private int remainingMeetingSeconds=0; //会议剩余秒数
	private int startingIndividualSeconds=0;//每人开始时的秒数
	private int currentIndividualStatusSeconds=0;//当前个人秒数
	private int completedParticipants=0;//已完成人数
	private int totalParticipants=0;//总人数
	private int warningTime=0;
	
	private boolean finished=false;
	private Timer timer=null;
	private PowerManager.WakeLock wakeLock=null;
	
	private Team team=null;
	private long meetingStartTime=0;
	private long individualStatusStartTime=0;
	private long individualStatusEndTime=0;
	private int quickestStatus=Integer.MAX_VALUE;
	private int longestStatus=0;
	
	private static MediaPlayer bell=null;
	private static MediaPlayer airhorn=null;
	
	private Handler updateDisplayHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			updateDisplay();
		};
	};
	
	private Handler disableIndividualTimerHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			disableIndividualTimer();
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_timer);
		team=Team.findByName(this.getIntent().getStringExtra("teamName"), this);
		if(team!=null){
			Logger.d("开始会议计时Start for team--"+team.getName());
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);//使用音量控制键来设置程序的音量大小
		initializeSounds();
		initializeButtonListeners();
		initializeTimerValues();
		updateDisplay();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		acquireWakeLock();
		startTimer();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	private synchronized void updateDisplay(){
		if(individualStatusInProcess()){
			TextView individualTimeRemaining=(TextView)findViewById(R.id.individual_time_remaining);
			individualTimeRemaining.setText(TimeFormatHelper.formatTime(remainingIndividualSeconds));
			individualTimeRemaining.setTextColor(TimeFormatHelper.determineColor(remainingIndividualSeconds, warningTime));
			
			TextView participantNumber=(TextView)findViewById(R.id.participant_number);
//			participantNumber.append("**");
			participantNumber.setText(getString(R.id.participant_number)
					+" ->"+(completedParticipants+1)+"/"+totalParticipants);
		}else{
			disableIndividualTimer();
		}
	}
	
	private void disableIndividualTimer(){
		Logger.d("个人计时ByeBye---Disable--");
		remainingIndividualSeconds=0;
		TextView participantNumber=(TextView)findViewById(R.id.participant_number);
		participantNumber.setText(R.string.individual_status_complete);
		
		TextView individualTimeRemaining=(TextView)findViewById(R.id.individual_time_remaining);
		individualTimeRemaining.setText(TimeFormatHelper.formatTime(remainingIndividualSeconds));
		individualTimeRemaining.setTextColor(Color.GRAY);
		
		Button nextButton=(Button)findViewById(R.id.next_button);
		nextButton.setClickable(false);
		nextButton.setTextColor(Color.GRAY);
	}
	
	private void initializeSounds(){
		if(bell==null){
			Logger.d("加载-bell sound");
			bell=MediaPlayer.create(this, R.raw.bell);
		}
		if(airhorn==null){
			Logger.d("加载-airhorn sound");
			airhorn=MediaPlayer.create(this, R.raw.airhorn);
		}
	}
	
	private void initializeButtonListeners(){
		View nextButton=findViewById(R.id.next_button);
		nextButton.setOnClickListener(this);
		View finishedButton=findViewById(R.id.finished_button);
		finishedButton.setOnClickListener(this);
	}
	
	private void initializeTimerValues(){
		int meetingLength=getIntent().getIntExtra("meetingLength", 0);
		int numParticipants=getIntent().getIntExtra("numParticipants", 0);
		Logger.d("Data from Intent:meetingLength="+meetingLength);
		Logger.d("Data from Intent:numParticipants="+numParticipants);
		loadState(meetingLength, numParticipants);
	}

	/**
	 * 根据会议时长和参会人数加载状态
	 * @param meetingLength
	 * @param numParticipants
	 */
	private synchronized void loadState(int meetingLength, int numParticipants){
		warningTime=Prefs.getWarningTime(this);
		
		SharedPreferences preferences=getPreferences(Context.MODE_PRIVATE);
		totalParticipants=preferences.getInt(TOTAL_PARTICIPANTS, numParticipants);
		remainingMeetingSeconds=preferences.getInt(REMAINING_MEETING_SECONDS, meetingLength*60);
		startingIndividualSeconds=preferences.getInt(STARTING_INDIVIDUAL_SECONDS, remainingMeetingSeconds/totalParticipants);//
		remainingIndividualSeconds=preferences.getInt(REMAINING_INDIVIDUAL_SECONDS, startingIndividualSeconds);
		completedParticipants=preferences.getInt(COMPLETED_PARTICIPANTS, 0);
		currentIndividualStatusSeconds=preferences.getInt(CURRENT_INDIVIDUAL_STATUS_SECONDS, 0);
		meetingStartTime=preferences.getLong(MEETING_START_TIME, System.currentTimeMillis());
		individualStatusEndTime=preferences.getLong(INDIVIDUAL_STATUS_END_TIME, 0);
		quickestStatus=preferences.getInt(QUICKEST_STATUS, Integer.MAX_VALUE);
		longestStatus=preferences.getInt(LONGEST_STATUS, 0);
		
		team=Team.findByName(this.getIntent().getStringExtra("teamName"), this);
		individualStatusStartTime=meetingStartTime;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean individualStatusInProcess(){
		return completedParticipants<totalParticipants;
	}
	//操作时间的关键代码都要使用同步块   synchronized   synchronized
	private synchronized void startTimer(){
		Logger.d("开始计时-- Starting a new Timer");
		timer=new Timer();
		TimerTask updateTimerTask=new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateTimerValue();
			}
			
		};
		timer.schedule(updateTimerTask, 1000, 1000);
	}
	private synchronized void updateTimerValue(){
		currentIndividualStatusSeconds+++;;
		***
		**
		*
	}
	private void acquireWakeLock(){
		if(wakeLock==null){
			PowerManager pm=(PowerManager)this.getSystemService(Context.POWER_SERVICE);
			wakeLock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,
					this.getClass().getCanonicalName());  //根据对象得到类名
			wakeLock.acquire();
		}
	}
}
