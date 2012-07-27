package com.richitec;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.red5.server.adapter.MultiThreadedApplicationAdapter;
import org.red5.server.api.IBasicScope;
import org.red5.server.api.IClient;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.service.IServiceCall;
import org.red5.server.api.service.IServiceCapableConnection;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.api.stream.ISubscriberStream;
import org.slf4j.Logger;

public class QuickServerApplication extends MultiThreadedApplicationAdapter {
	private static Logger log = LoggerFactory.getLogger(QuickServerApplication.class);
	
	private static final String UserID = "UserID";
	
	private static final String JoinMessage = "joinMessage";
	private static final String UnjoinMessage = "unjoinMessage";
	private static final String streamStartMessage = "streamStartMessage";
	private static final String streamCloseMessage = "streamCloseMessage";
	private static final String streamListMessage = "streamListMessage";
	
	private static ConcurrentHashMap<String, Set<String>> streamMap;
	
	
	/**
	 * called just before a child scope is added
	 */
	@Override
	public boolean addChildScope(IBasicScope scope){
		log.debug("addChildScope");
		return true;
	}
	
	/**
	 * called right after a child scope is removed
	 */
	@Override
	public void removeChildScope(IBasicScope scope){
		log.debug("removeChildScope");
	}
	
	/**
	 * called when a service method is requested
	 */
	@Override
	public boolean serviceCall(IConnection conn, IServiceCall call){
		log.debug("serviceCall");
		return true;
	}
	
	/**
	 * called on the first request for an application or room
	 */
    @Override
	public boolean appStart(IScope app) {
    	log.debug("appStart");
    	streamMap = new ConcurrentHashMap<String, Set<String>>();
	    return super.appStart(app);
	}
    
    /**
     * called when a connection is made to an application
     */
    @Override
    public boolean appConnect(IConnection conn, Object[] args){
    	log.debug("appConnect");
    	if (args.length != 1) {
    		System.err.println("Only one argument is expected, but it's " + args.length);
    		return false;
    	}
    	
    	return true;
    }
    
    /**
     * Called every time a client joins an application
     */
    @Override
    public boolean appJoin(IClient client, IScope scope){
    	log.debug("appJoin");
    	return true;
    }
    
    /**
     * Called when a client leaves an application
     */
    @Override
    public void appLeave(IClient client, IScope scope){
    	log.debug("appLeave");
    }
    
    /**
     * Called when a connection disconnects from an application 
     */
    @Override
    public void appDisconnect(IConnection conn) {
    	log.debug("appDisconnect");
    }
    
    /**
     * Called when an application is destroyed, this occurs when an application is stopped.
     */
    @Override
    public void appStop(IScope scope){
    	log.debug("appStop");
    }

    /**
     * Called only once when a room is started
     */
    @Override
    public boolean roomStart(IScope scope){
    	log.debug("roomStart " + scope.getContextPath());
    	return true;
    } 
    
    /**
     * Called on every connection to a room
     */
    @Override
    public boolean roomConnect(IConnection conn, Object [] args){
    	log.debug("roomConnect");
    	return conn.getClient().setAttribute(UserID, args[0]);
    }
    
    /**
     * Called every time a client joins a room
     */
    @Override
    public boolean roomJoin(IClient client, IScope scope){
    	log.debug("roomJoin");
    	/*
    	Set<IClient> allClients = scope.getClients();
    	if (allClients.size() >= 4){
    		System.err.println("The conference " + scope.getContextPath() + 
    				" has reach it's max size!");
    		rejectClient();
    		return false;
    	}
    	
    	Set<String> streamSet = streamMap.get(scope.getContextPath());
    	if (null != streamSet){
    		callClientFunc(streamListMessage, streamSet, client);
    	}
    	
    	broadcast(JoinMessage, client.getAttribute(UserID), scope);
    	*/
    	return true;
    }
    
    /**
     * Called when a client leaves a room
     */
    @Override
    public void roomLeave(IClient client, IScope scope){
    	log.debug("roomLeave");
    	broadcast(UnjoinMessage, client.getAttribute(UserID), scope);
    }
    
    /**
     * Called when a connection disconnects from a room
     */
    @Override
    public void roomDisconnect(IConnection conn){
    	log.debug("roomDisconnect");
    }
    
    /**
     * Called when a room is disposed
     */
    @Override
    public void roomStop(IScope scope){
    	log.debug("roomStop " + scope.getContextPath());
    	streamMap.remove(scope.getContextPath());
    }
    
    @Override
    public void streamBroadcastStart(IBroadcastStream stream){
    	log.debug("stream Broadcast Start " + stream.getPublishedName());
    	
    	String scopeContext = stream.getScope().getContextPath();
    	Set<String> streamSet = streamMap.get(scopeContext);
    	if (null == streamSet){
    		streamSet = new HashSet<String>();
        	streamMap.put(scopeContext, streamSet);
    	}
    	streamSet.add(stream.getPublishedName());
    	broadcast(streamStartMessage, stream.getPublishedName(), stream.getScope());
    	
    	//TODO: transfer to RTP
    	stream.addStreamListener(new IStreamListener() {
			
			@Override
			public void packetReceived(IBroadcastStream stream, IStreamPacket packet) {
				// TODO Auto-generated method stub
			}
		});    	
    }
    
    @Override
    public void streamBroadcastClose(IBroadcastStream stream){
    	log.debug("stream Broadcast Close " + stream.getPublishedName());
    	
    	String scopeContext = stream.getScope().getContextPath();
    	Set<String> streamSet = streamMap.get(scopeContext);
    	if (null != streamSet){
    		streamSet.remove(stream.getPublishedName());
    	}
    	
    	broadcast(streamCloseMessage, stream.getPublishedName(), stream.getScope());
    } 
    
    @Override
    public void streamSubscriberStart(ISubscriberStream stream){
    	log.debug("stream Subscriber Start " + stream.getName());
    } 
    
    @Override
    public void streamSubscriberClose(ISubscriberStream stream){
    	log.debug("stream Subscriber Close " + stream.getName());
    }      
    
	private void broadcast(String func, Object message, IScope scope){
		Set<IClient> clientSet = scope.getClients();
		if (clientSet == null){
			log.debug("scope " + scope.getContextPath() + " has NULL clients.");
			return;
		}
		
		log.debug("scope " + scope.getContextPath() + " has " + clientSet.size() + " clients");
		for (IClient client : clientSet){
			//callClientFunc(func, message, client);
		}
	}
	
	private void callClientFunc(String func, Object message, IClient client){
		Set<IConnection> connSet = client.getConnections();
		if (connSet == null){
			log.debug("client " + client.getAttribute(UserID) + 
					" scope " + scope.getContextPath() + " has NULL connections.");
			return;
		}
		
		for(IConnection conn : connSet){
			IServiceCapableConnection sc = (IServiceCapableConnection)conn;
			sc.invoke(func, new Object[]{ message });
		}		
	}	
}
