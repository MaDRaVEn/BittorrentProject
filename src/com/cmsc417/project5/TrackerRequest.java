package com.cmsc417.project5;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;

public class TrackerRequest {

	private String announceurl;
	private int downloaded;
	private int uploaded;
	private int leftToDownload;
	private boolean firstRequest;
	
	
	public static void main(String args[]) throws IOException{
	File torrent = new File(args[0]);
	
	TorrentParser parser = new TorrentParser(torrent);
	new TrackerRequest(parser);
	
	}
	
	public TrackerRequest(TorrentParser parser) throws IOException{
	this.announceurl = parser.getAnnounce();
	System.out.println(announceurl);
	
	System.out.println(new String(parser.getInfoMap()));
	
	MessageDigest md = null;
	try {
		md = MessageDigest.getInstance("SHA-1");
	} catch (NoSuchAlgorithmException e) {
		e.printStackTrace();
	}
	
   md.update(parser.getInfoMap());
	

	byte[] infohash = new byte[20];
	infohash = md.digest();
	
	String info_hash = byte2hexString(infohash);
	
String urlEncodedInfoHash = URLEncoder.encode(info_hash, "UTF-8");
	System.out.println(urlEncodedInfoHash);
	
	SecureRandom sr = null;
	try {
		sr = SecureRandom.getInstance("SHA1PRNG");
	} catch(NoSuchAlgorithmException e) {
		e.printStackTrace();
	}
	
	byte[] peerid = new byte[20];
	
	sr.nextBytes(peerid);
	
	String peer_id = byte2hexString(peerid);
	
	String urlEncodedPeerID = URLEncoder.encode(peer_id,"UTF-8");
	System.out.println(urlEncodedPeerID);
	
	
	URL url = new URL(announceurl);
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	conn.addRequestProperty("info_hash", urlEncodedInfoHash);
	conn.addRequestProperty("peer_id", urlEncodedPeerID);
	conn.addRequestProperty("port", "27714");
	conn.addRequestProperty("uploaded", Integer.toString(this.uploaded));
	conn.addRequestProperty("downloaded", Integer.toString(this.downloaded));
	conn.addRequestProperty("left",Integer.toString(this.leftToDownload));
	conn.addRequestProperty("compact", "1");
	if(firstRequest == true){
	conn.addRequestProperty("event","started");
	firstRequest = false;
	}
	
	
	conn.setRequestMethod("GET");
	conn.connect();
	

	InputStream in = conn.getInputStream();
	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	String text = reader.readLine();
	System.out.println(text);

}
	
/*	public void addParam(String name, String value) {
		String append = encode(name , value);
		if (getQuery().indexOf('?') == -1) {
		setQuery(getQuery() + "?" + append);
		} else {
		setQuery(getQuery() + "&" + append);
		}
		}
		} */
	
	private static String byte2hexString(byte[] b){
		String hex = null;
	StringBuffer hexString = new StringBuffer();

	for (int i = 0; i < b.length; i++){
		String x = Integer.toHexString(0xFF & b[i]).toUpperCase();
		if (x.length() < 2) x = "0" + x;
		hexString.append(x);
	}
	
	hex = hexString.toString();
	return hex;
	}
	
	

}