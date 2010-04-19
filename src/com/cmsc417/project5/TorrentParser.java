package com.cmsc417.project5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;

import org.ardverk.coding.BencodingInputStream;
import org.ardverk.coding.BencodingOutputStream;

/**
 * @author Chris Kaminski & David Thomas
 *
 */

public class TorrentParser {
	
	private String announce;
	private String torrentName;
	private long fileLength;
	private long pieceLength;
	private byte[] hashes;
	private ByteArrayOutputStream infoMapStream;
	
	
	/**
	 * 
	 * @param file
	 */
	public TorrentParser(File file) {
		FileInputStream fileStream = null;
		BencodingInputStream stream;
		
		try {
			fileStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if(fileStream != null) {
			stream = new BencodingInputStream(fileStream);
			Map<String, ?> map = null;
			
			try {
				map = stream.readMap();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(map != null) {
				
				//Parse out all the required fields.
				this.announce = new String((byte[])map.get("announce"));
				Map<String, ?> infoMap = (Map<String, ?>)map.get("info");
				this.torrentName = new String((byte[])infoMap.get("name"));
				this.fileLength = ((BigInteger)infoMap.get("length")).longValue();
				this.pieceLength = ((BigInteger)infoMap.get("piece length")).longValue();
				this.hashes = (byte[])infoMap.get("pieces");
				
				infoMapStream = new ByteArrayOutputStream();
				BencodingOutputStream infoMapWriter = new BencodingOutputStream(infoMapStream);
				try {
					infoMapWriter.writeMap(infoMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * Return the string representation of the announce URL.
	 * @return
	 */
	public String getAnnounce() {
		return announce;
	}

	/**
	 * Return the torrent file's name.
	 * @return
	 */
	public String getTorrentName() {
		return torrentName;
	}

	/**
	 * Return the length of the file in bytes.
	 * @return
	 */
	public long getFileLength() {
		return fileLength;
	}

	/**
	 * Return the length of a piece.
	 * @return
	 */
	public long getPieceLength() {
		return pieceLength;
	}

	/**
	 * Return the 20-byte hashes for this torrent in an array list.
	 * @return
	 */
	public ArrayList<byte[]> getHashes() {
		
		ArrayList<byte[]> hashesList = new ArrayList<byte[]>();
		
		//Break the single hash field into 20-byte hash strings.
		for(int i = 0; i < hashes.length/20; i++) {
			byte hash[] = new byte[20];
			for(int j = 0; j < 20; j++) {
				hash[j] = hashes[i*20 + j];
			}
			hashesList.add(hash);
		}
		return hashesList;
	}
	
	/**
	 * Return the SHA-1 hash of the info map.
	 * @return
	 */
	public byte[] getInfoHash() {
		
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1");
			byte infoHash[] = digest.digest(infoMapStream.toByteArray());
			return infoHash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
