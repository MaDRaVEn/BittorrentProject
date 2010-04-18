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

	public Map<String, ?> request(String event, long uploaded, long downloaded,
									long left) {

		String requestURL = getRequestURL(event, uploaded, downloaded, left);

		URL request = null;
		URLConnection connection = null;
		PlainTextInputStream content = null;
		Map<String, ?> trackerResponse = null;
		
		try {
			request = new URL(requestURL);
			connection = request.openConnection();
			content = (PlainTextInputStream) connection.getContent();
			BencodingInputStream trackerStream = new BencodingInputStream(
					content);
			trackerResponse = trackerStream.readMap();
			return trackerResponse;
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String urlEncodeHash(byte[] hash) {

		String urlEncodedHash = "";

		for (int i = 0; i < hash.length; i++) {
			String hexVal = Integer.toHexString(hash[i] & 0xFF);
			if (hexVal.length() != 2) {
				urlEncodedHash += ("%0" + hexVal);
			} else {
				urlEncodedHash += ("%hexVal");
			}
		}

		return urlEncodedHash;
	}

	private String getRequestURL(String event, long uploaded, long downloaded,
									long left) {

		String requestURL = baseURL + "?";
		requestURL += "info_hash=" + info + "&";
		requestURL += "peer_id=" + peerID + "&";
		requestURL += "port=" + Integer.toString(port) + "&";

		if (!event.equals("started")) {
			requestURL += "uploaded=" + Long.toString(uploaded) + "&";
			requestURL += "downloaded=" + Long.toString(uploaded) + "&";
		} else {
			requestURL += "uploaded=" + "0&";
			requestURL += "downloaded=" + "0&";
		}

		requestURL += "left=" + left + "&";
		requestURL += "compact=1&";
		requestURL += "event=" + event;

		return requestURL;
	}

}
