package org.treant.standuptimer.model;

import java.util.List;

public class MeetingStatus {
	private float numParticipants = 0;
	private float individualStatusLength = 0;
	private float meetingLength = 0;
	private float quickestStatus = 0;
	private float longestStatus = 0;

	public MeetingStatus(float numParticipants, float individualStatusLength,
			float meetingLength, float quickestStatus, float longestStatus) {
		this.numParticipants = numParticipants;
		this.individualStatusLength = individualStatusLength;
		this.meetingLength = meetingLength;
		this.quickestStatus = quickestStatus;
		this.longestStatus = longestStatus;
	}

	public float getNumParticipants() {
		return numParticipants;
	}

	public float getIndividualStatusLength() {
		return individualStatusLength;
	}

	public float getMeetingLength() {
		return meetingLength;
	}

	public float getQuickestStatus() {
		return quickestStatus;
	}

	public float getLongestStatus() {
		return longestStatus;
	}

	/**
	 * 根据MeetingStatus的List返回Average数据
	 * @param meetingStatusList
	 * @return
	 */
	public static MeetingStatus getAverageStatus(List<MeetingStatus> meetingStatusList){
		float totalNumParticipants=0;
		float totalIndividualStatusLength=0;
		float totalMeetingLength=0;
		float totalQuickestStatus=0;
		float totalLongestStatus=0;
		int listSize=meetingStatusList.size();
		for(MeetingStatus meetingStatus : meetingStatusList){
			totalNumParticipants+=meetingStatus.getNumParticipants();
			totalIndividualStatusLength+=meetingStatus.getIndividualStatusLength();
			totalMeetingLength+=meetingStatus.getMeetingLength();
			totalQuickestStatus+=meetingStatus.getQuickestStatus();
			totalLongestStatus+=meetingStatus.getLongestStatus();
		}
		return new MeetingStatus(totalNumParticipants/listSize,
				totalIndividualStatusLength/listSize,
				totalMeetingLength/listSize,
				totalQuickestStatus/listSize,
				totalLongestStatus/listSize);
	}
}
