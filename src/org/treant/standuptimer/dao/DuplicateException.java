package org.treant.standuptimer.dao;

public class DuplicateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public DuplicateException(String msg){
		super(msg);
	}
}
