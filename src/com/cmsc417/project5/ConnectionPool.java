package com.cmsc417.project5;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class ConnectionPool implements Runnable {
	
	private ServerSocket socket;
	private byte[] infoHash;
	private String peerID;
	private ScheduledThreadPoolExecutor threadPool;
	private ConcurrentHashMap<String, ScheduledFuture<?>> servedPeers;
	
	public ConnectionPool(int port,
			   			  byte[] infoHash,
			   			  String peerID) {
		
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.infoHash = infoHash;
		this.peerID = peerID;
		this.threadPool = new ScheduledThreadPoolExecutor(25);
		
	}
	
	public void addPeers(ArrayList<String> peers) {
		for(String peer : peers) {
			if(!servedPeers.containsKey(peer)) {
				threadPool.schedule(new PeerConnection(), 0, TimeUnit.SECONDS);
			}
		}
	}
	
	public void removePeer(String peer) {
		if(servedPeers)
	}
	
	public void run() {
		// TODO Auto-generated method stub
		
	}

}