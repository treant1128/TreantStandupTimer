package org.treant.standuptimer.dao;


public class InvalidTeamNameException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public InvalidTeamNameException(String msg){
		super(msg);
	}
}
