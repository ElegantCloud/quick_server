package com.richitec;

import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

public class LoggerFactory {
	
	static Logger getLogger(Class<?> clazz){
		return Red5LoggerFactory.getLogger(clazz, "quick_server");
	}

}
