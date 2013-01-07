package org.treant.standuptimer;

import org.treant.standuptimer.model.Team;
import org.treant.standuptimer.utils.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ConfigureStandupTimer extends Activity implements OnClickListener {

	private static final String MEETING_LENGTH = "meetingLength";
	private static final String NUMBER_OF_PARTICIPANTS = "numberOfParticipants";
	private static final String TEAM_NAME_POS = "teamNamesPos";

	private static final int MAX_ALLOWED_PARTICIPANTS = Integer.MAX_VALUE;
	private static final int MAX_ALLOWED_MEETING_LENGTH = Integer.MAX_VALUE;

	private int meetingLength = 0;
	private int numParticipants = 0;
	private int teamNamePos = 0;

	private Spinner meetingLengthSpinner = null;
	private EditText meetingLengthEditText = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		initializeGUIElements();
	}

	private void initializeGUIElements() {
		loadState();
		initializeNumberOfParticipants();
		initializeMeetingLength();
		initializeTeamNamesSpinner();
		initializeStartButton();
	}

	private void loadState(){
		SharedPreferences preferences=getPreferences(Context.MODE_PRIVATE);//activity's class name as the preferences name
		meetingLength=preferences.getInt(MEETING_LENGTH, 5);
		numParticipants=preferences.getInt(NUMBER_OF_PARTICIPANTS, 2);
		teamNamePos=preferences.getInt(TEAM_NAME_POS, 0);
		Logger.d("取到的状态值--Retrieved state: meetingLength= "+meetingLength
				+", numParticipants="+numParticipants+", teamNumePos="+teamNamePos);
	}
	
	private void initializeNumberOfParticipants(){
		((TextView)findViewById(R.id.num_participants)).setText(Integer.toString(numParticipants));
	}
	
	private void initializeMeetingLength(){
		ViewGroup meetingLengthContainer=(ViewGroup) findViewById(R.id.meeting_length_container);
		meetingLengthContainer.removeAllViews();
		View meetingLengthView=null;
		if(Prefs.allowVariableMeetingLength(this)){
			meetingLengthView=createMeetingLengthTextBox();
		}else{
			meetingLengthView=createMeetingLengthSpinner();
		}
		meetingLengthContainer.addView(meetingLengthView);
	}
	private View createMeetingLengthTextBox(){
		meetingLengthEditText=new EditText(this);
		meetingLengthEditText.setGravity(Gravity.CENTER);
		meetingLengthEditText.setKeyListener(new DigitsKeyListener());
		meetingLengthEditText.setRawInputType(InputType.TYPE_CLASS_PHONE);//???
		meetingLengthEditText.setLayoutParams(new LayoutParams(dipsToPixels(60), LayoutParams.WRAP_CONTENT));
		meetingLengthEditText.setText(Integer.toString(meetingLength));
		meetingLengthEditText.setLines(1);
		meetingLengthSpinner=null;
		return meetingLengthEditText;
	}
	private View createMeetingLengthSpinner(){
		meetingLengthSpinner=new Spinner(this);
		meetingLengthSpinner.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		meetingLengthSpinner.setPrompt(this.getString(R.string.length_of_meeting));//下拉框的标题栏
		ArrayAdapter<?> adapter=ArrayAdapter.createFromResource(this, R.array.LengOfMeeting, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		meetingLengthSpinner.setAdapter(adapter);
		meetingLengthSpinner.setSelection(getSpinnerPositionFromMeetingLength(meetingLength));//有什么用
		meetingLengthEditText=null;
		return meetingLengthSpinner;
	}
	
	private void initializeTeamNamesSpinner(){
		Spinner spinner=(Spinner)findViewById(R.id.team_names);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
				Team.findAllTeamNames(this));
		adapter.add(" [No Team] ");
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(teamNamePos);
	}

	private void initializeStartButton(){
		findViewById(R.id.start_button).setOnClickListener(this);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;// You must return true for the menu to be displayed; if you
					// return false it will not be shown
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.about:
			Logger.d("Display the about box");
			displayAboutBox();
			return true;
		case R.id.help:
			Logger.d("Display the Help Dialog");
			displayHelpDialog();
			return true;
		case R.id.settings:
			Logger.d("Display the Settings");
			displaySettings();
			return true;
		case R.id.teams:
			Logger.d("Display the Team Configuration");
			displayTeamConfiguration();
			return true;
		default:
			Logger.d("Unknown menu item selected----Default");
			return false;
		}
	}

	private void displayAboutBox() {
		startActivity(new Intent(this, About.class));
	}

	private void displayHelpDialog() {
		startActivity(new Intent(this, Help.class));
	}

	private void displaySettings() {
		startActivity(new Intent(this, Prefs.class));
	}

	private void displayTeamConfiguration() {
		startActivity(new Intent(this, TeamList.class));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent=new Intent(this, StandupTimer.class);
		meetingLength=getMeetingLengthFromUI();
		intent.putExtra("meetingLength", meetingLength);
		
		TextView text=(TextView)findViewById(R.id.num_participants);
		numParticipants=parseNumberOfParticipants(text);
		intent.putExtra("numParticipants", numParticipants);
		
		Spinner teamNameSpinner=(Spinner)findViewById(R.id.team_names);
		teamNamePos=teamNameSpinner.getSelectedItemPosition();
		intent.putExtra("teamName", teamNameSpinner.getSelectedItem().toString());
		
		if(numParticipants<1||(Prefs.allowUnlimitedParticipants(this)==false&&numParticipants>20)){
			showInvalidNumberOfParticipantsDialog();
		}else{
			saveState();
			startTimer(intent);
		}
	}
	
	private void saveState(){
		Logger.d("保存状态--meetingLength="+meetingLength +
				", numParticipants="+numParticipants+", teamNamePos="+teamNamePos);
		SharedPreferences.Editor preferences=getPreferences(Context.MODE_PRIVATE).edit();
		preferences.putInt(MEETING_LENGTH, meetingLength);
		preferences.putInt(NUMBER_OF_PARTICIPANTS, numParticipants);
		preferences.putInt(TEAM_NAME_POS, teamNamePos);
		preferences.commit();
	}
	private void startTimer(Intent intent){
		this.startActivity(intent);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage(getWarningMessage())
		.setCancelable(true)
		.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dismissDialog(0);
			}
			
		});
		return builder.create();
	}
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		// Override this if you need to update a managed dialog based on the 
		// state of the application each time it is shown
		super.onPrepareDialog(id, dialog, args);
		((AlertDialog)dialog).setMessage(this.getString(getWarningMessage()));
	}
	
	private int getWarningMessage(){
		if(Prefs.allowUnlimitedParticipants(this)){//如果不限制人数
			return R.string.valid_num_participants_warning_unlimited; 
		}else{
			return R.string.valid_num_participants_warning;
		}
	}
	
	private void showInvalidNumberOfParticipantsDialog(){
//		Show a dialog managed by this activity. A call to onCreateDialog(int, Bundle) will be
//		made with the same id the first time this is called for a given id. From thereafter,
//		the dialog will be automatically saved and restored.
		showDialog(0);
	}
	private int getMeetingLengthFromUI(){
		if(meetingLengthEditText!=null){
			return getMeetingLengthFromTextArea();
		}else{
			return getMeetingLengthFromSpinner();
		}
	}
	
	private int getMeetingLengthFromTextArea(){
		int length=meetingLength;
		try{
			length=Integer.parseInt(meetingLengthEditText.getText().toString());
		}catch(NumberFormatException e){
			Logger.w("Invalid meeting length provided.  Defaulting to previous value.");
		}
		return length>MAX_ALLOWED_MEETING_LENGTH?MAX_ALLOWED_MEETING_LENGTH:length;
	}
	
	private int getMeetingLengthFromSpinner(){
		int minites=0;
		switch(meetingLengthSpinner.getSelectedItemPosition()){
		case 0: minites=5; break;
		case 1: minites=10; break;
		case 2: minites=15; break;
		case 3: minites=20; break;
		default: break;
		}
		return minites;
	}
	
	private int parseNumberOfParticipants(TextView text){
		int n=numParticipants;
		try{
			n=Integer.parseInt(text.getText().toString());
		}catch(NumberFormatException e){
			Logger.w("Invalid number of participants provided. Defaulting to previous value");
		}
		return n>MAX_ALLOWED_PARTICIPANTS?MAX_ALLOWED_PARTICIPANTS:n;
	}
	
	private int dipsToPixels(int dips){        //dip----->pixel
		return (int)(dips*getResources().getDisplayMetrics().density);
	}
	
	private int getSpinnerPositionFromMeetingLength(int meetingLength){
		int position=0;
		switch(meetingLength){
		case 5: position=0; break;
		case 10: position=1; break;
		case 15: position=2; break;
		case 20: position=3; break;
		default: break;
		}
		return position;
	}
	protected int getMeetingLength() {
		return meetingLength;
	}

	protected int getNumParticipants() {
		return numParticipants;
	}

	protected int getTeamNamePos() {
		return teamNamePos;
	}

	protected Spinner getMeetingLengthSpinner() {
		return meetingLengthSpinner;
	}

	protected EditText getMeetingLengthEditText() {
		return meetingLengthEditText;
	}
}
