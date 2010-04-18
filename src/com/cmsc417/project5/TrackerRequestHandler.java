package com.cmsc417.project5;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.ardverk.coding.BencodingInputStream;

import sun.net.www.content.text.PlainTextInputStream;

/**
 * @author Chris Kaminski & David Thomas
 *
 */
public class TrackerRequestHandler {

	private String baseURL;
	private String info = "";
	private String peerID;
	private int port;

	public TrackerRequestHandler(String baseURL, String peerID,
									byte[] infoDictionary, int port) {

		this.baseURL = baseURL;
		try {

			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte infoHash[] = digest.digest(infoDictionary);

			this.info = urlEncodeHash(infoHash);
			this.peerID = URLEncoder.encode(peerID, "UTF-8");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		this.port = port;
	}

	
	/**
	 * Generate a request to a tracker. Returns a <String, Value> map corresponding
	 * to the tracker's response. Call with event as null or equal to "" to get
	 * periodic updates on available peers. uploaded and downloaded are ignored when
	 * event is equal to "started".
	 * 
	 * @param event
	 * @param uploaded
	 * @param downloaded
	 * @param left
	 * @return
	 */
	public Map<String, ?> request(String event, long uploaded, long downloaded,
									long left) {

		String requestURL = getRequestURL(event, uploaded, downloaded, left);
		
		try {
			URL request = new URL(requestURL);
			URLConnection connection = request.openConnection();
			PlainTextInputStream content = (PlainTextInputStream) connection.getContent();
			BencodingInputStream trackerStream = new BencodingInputStream(
					content);
			Map<String, ?> trackerResponse = trackerStream.readMap();
			return trackerResponse;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get a URL encoded version of the info hash, since the java implementation 
	 * won't work for this.
	 * 
	 * @param hash
	 * @return
	 */
	private String urlEncodeHash(byte[] hash) {

		String urlEncodedHash = "";

		//Convert all bytes to their hex value and prefix with %
		for (int i = 0; i < hash.length; i++) {
			String hexVal = Integer.toHexString(hash[i] & 0xFF);
			//Pad if the leading zero was cut off
			if (hexVal.length() != 2) {
				urlEncodedHash += ("%0" + hexVal);
			} else {
				urlEncodedHash += ("%hexVal");
			}
		}

		return urlEncodedHash;
	}

	/**
	 * Get a formatted request URL for this set of parameters.
	 * 
	 * @param event
	 * @param uploaded
	 * @param downloaded
	 * @param left
	 * @return
	 */
	private String getRequestURL(String event, long uploaded, long downloaded,
									long left) {

		String requestURL = baseURL + "?";
		requestURL += "info_hash=" + info + "&";
		requestURL += "peer_id=" + peerID + "&";
		requestURL += "port=" + Integer.toString(port) + "&";

		//Make uploaded and downloaded = 0 if the started event is being requested
		if (event == null || !event.equals("started")) {
			requestURL += "uploaded=" + Long.toString(uploaded) + "&";
			requestURL += "downloaded=" + Long.toString(uploaded) + "&";
		} else if(event != null && event.equals("started")) {
			requestURL += "uploaded=" + "0&";
			requestURL += "downloaded=" + "0&";
		}

		requestURL += "left=" + left + "&";
		requestURL += "compact=1&";
		
		if(event != null && !event.equals(""))
			requestURL += "event=" + event;

		return requestURL;
	}

}
