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
		this.servedPeers = new ConcurrentHashMap<String, ScheduledFuture<?>>();
		
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
					}
				} else {
					servedPeers.put(ip, threadPool.schedule(new PeerConnection(socket), 
															0, TimeUnit.SECONDS));
				}		
			}
		}
	}
	
	public void removePeer(String peer) {
		if(servedPeers.containsKey(peer)) {
		}
	}
	
	
	public void run() {
		
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
		
		return getHandshakeResponse(socket, null);
		
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
			message[i] = idBytes[i-48];
		}
		
		return message;
	}
	
	private boolean getHandshakeResponse(Socket socket, String expectedPeer) {
		InputStream in = null;
		byte[] buffer = new byte[68];
		int numRead = 0;
		
		try {
			in = socket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			numRead = in.read(buffer);
		} catch (IOException e) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return false;
			}
			try {
				numRead = in.read(buffer);
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		
		while(numRead < 68) {
			try {
				numRead += in.read(buffer, numRead, 68- numRead);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(buffer[0] != 19) {
			return false;
		} else {
			String response = new String(buffer);
			if(!response.substring(1, 20).equals("BitTorrent protocol")) {

				return false;
			}
			
			if(!(new String(infoHash)).equals(response.substring(28, 47))) {
				return false;
			}
		
			
			if(expectedPeer != null && (expectedPeer.equals(response.substring(47)))) {
				return false;
			}
			
			if(response.substring(47).equals(this.peerID)) {
				return false;
			}
			
			return true;
		}
	}
}