package com.richitec.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;

import com.richitec.LoggerFactory;

public class AppMessageSender {
	private static Logger log = LoggerFactory.getLogger(AppMessageSender.class);

	private static final int timeoutConnection = 10000;
	private static final int timeoutSocket = 20000;

	private String serverPath;

	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

	public AppMessageSender() {

	}

	public void notifyUserVideoOn(String conferenceId, String userName) {
		log.debug("notifyUserVideoOn - conferenceId: " + conferenceId + " userName: " + userName);
		NameValuePair[] data = { new BasicNameValuePair("conferenceId", conferenceId),
				new BasicNameValuePair("username", userName),
				new BasicNameValuePair("video_status", "on") };

		executeHttpPost(this.serverPath + "/conference/updateAttendeeStatus", data);
	}

	public void notifyUserVideoOff(String conferenceId, String userName) {
		log.debug("notifyUserVideoOff - conferenceId: " + conferenceId + " userName: " + userName);
		NameValuePair[] data = { new BasicNameValuePair("conferenceId", conferenceId),
				new BasicNameValuePair("username", userName),
				new BasicNameValuePair("video_status", "off") };

		executeHttpPost(this.serverPath + "/conference/updateAttendeeStatus", data);
	}

	private void executeHttpPost(String url, NameValuePair[] data) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		HttpConnectionParams.setConnectionTimeout(httpParameters,
				timeoutConnection);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

		HttpPost post = new HttpPost(url);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (NameValuePair nvp : data) {
			params.add(nvp);
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			httpClient.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
