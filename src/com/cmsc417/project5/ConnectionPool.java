package com.cmsc417.project5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
	
	public void addPeers(ArrayList<String[]> peers) {
		for(String[] peerTuple : peers) {
			
			Socket socket = null;
			String ip = null;
			
			if(peerTuple.length == 2) {
				ip = peerTuple[0];
				try {
					socket = new Socket(InetAddress.getByName(ip),
										Integer.parseInt(peerTuple[1]));
				} catch (Exception e) {
					e.printStackTrace();
				} 
			} else {
				ip = peerTuple[1];
				try {
					socket = new Socket(InetAddress.getByName(ip),
										Integer.parseInt(peerTuple[1]));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(socket != null && socket.isConnected()) {
				if(initiateHandshake(socket) == false) {
					if(!socket.isClosed()) {
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						servedPeers.put(ip, threadPool.schedule(new PeerConnection(socket), 
																	0, TimeUnit.SECONDS));
					}
				}
			}
		}
	}
	
	public void removePeer(String peer) {
		if(servedPeers.containsKey(peer)) {
		}
	}
	
	
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	private boolean initiateHandshake(Socket socket) {
		
		OutputStream out = null;
		byte[] message = makeHandshakeMessage();
		
		try {
			out = socket.getOutputStream();
			out.write(message);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return getHandshakeResponse(socket);
		
	}
	
	private byte[] makeHandshakeMessage() {
		
		byte[] message = new byte[68];
		String protocol = "BitTorrent protocol";
		
		message[0] = 19;
		for(int i = 1; i < 20; i++) {
			byte[] protoBytes = protocol.getBytes();
			message[i] = protoBytes[i-1];
		}
		for(int i = 20; i < 28; i++) {
			message[i] = 0;
		}
		for(int i = 28; i < 28+infoHash.length; i++) {
			message[i] = infoHash[i-28];
		}
		for(int i = 48; i < 68; i++) {
			byte[] idBytes = peerID.getBytes();
			message[i] = idBytes[i-28];
		}
		
		return message;
	}
	
	private boolean getHandshakeResponse(Socket socket) {
		InputStream in = null;
		
		try {
			in = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}