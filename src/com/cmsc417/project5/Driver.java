package com.cmsc417.project5;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TorrentParser parser = new TorrentParser(new File(args[0]));
		
		byte[] infoHash = parser.getInfoHash();
		
		TrackerRequestHandler handler = new TrackerRequestHandler(parser.getAnnounce(),
																  "-KC0001-910483711192",
																  infoHash,
																  6881);
		Map<String, ?> response = handler.request("started", 0, 0, 0);
		Object peers = response.get("peers");
		
		
		ArrayList<String[]> peerList = parsePeers(peers);
		ConnectionPool pool = new ConnectionPool(6881, infoHash, "-KC0001-910483711192");
		
		pool.addPeers(peerList);
		pool.addPeers(peerList);
	}
	
	public static ArrayList<String[]> parsePeers(Object peers) {
		
		ArrayList<String[]> returned = new ArrayList<String[]>();
		
		if(peers instanceof ArrayList<?>) {
			ArrayList<Map<String, ?>> peerList = (ArrayList<Map<String,?>>)peers;
			
			for(Map<String, ?> peer : peerList) {
				String[] peerTriple = new String[3];
				peerTriple[0] = (String)peer.get("peer id");
				peerTriple[1] = (String)peer.get("ip");
				peerTriple[2] = ((BigInteger)peer.get("port")).toString();
				
				returned.add(peerTriple);
			}
			
			return returned;
		} else {
			
			byte[] peerList = (byte[])peers;
			
			for(int i = 0; i < peerList.length/6; i++) {
				
				String[] ipPair = new String[2];
				
				int base = 6*i;
				String ip = "";

				for(int j = 0; j < 4; j++) {
					int part = peerList[base+j]&(0xFF);
					ip += part;
					if(j < 3)
						ip += ".";
				}

				int port = (((peerList[base+4]&(0xFF)) << 8) | (peerList[base+5] & (0xFF)));
				
				ipPair[0] = ip;
				ipPair[1] = Integer.toString(port);
				
				returned.add(ipPair);
			}
		}
		return returned;
	}

}
