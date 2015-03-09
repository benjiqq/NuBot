package com.nubits.nubot.utils;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtils{
	
	private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
	
	public static void main(String[] args) {
		
		logger.debug("[MAIN] Current Date : {}", getCurrentDate());
		System.out.println(getCurrentDate());
	}
	
	private static Date getCurrentDate(){
		
		return new Date();
		
	}
	
}
