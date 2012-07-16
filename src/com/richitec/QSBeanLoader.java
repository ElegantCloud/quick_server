package com.richitec;

import org.red5.server.api.Red5;

import com.richitec.util.AppMessageSender;

public class QSBeanLoader{
	
	public static AppMessageSender getAppMessageSender() {
		return (AppMessageSender)Red5.getConnectionLocal().getScope().getContext().getBean("app.msgsender");
	}
	
}
