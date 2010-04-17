package com.cmsc417.project5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import org.ardverk.coding.BencodingInputStream;



public class TorrentParser {
	
	private String announce;
	private String torrentName;
	private long fileLength;
	private long pieceLength;
	private byte[] hashes;
	
	
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(map != null) {
				this.announce = new String((byte[])map.get("announce"));
				Map<String, ?> infoMap = (Map<String, ?>)map.get("info");
				this.torrentName = new String((byte[])infoMap.get("name"));
				this.fileLength = ((BigInteger)infoMap.get("length")).longValue();
				this.pieceLength = ((BigInteger)infoMap.get("piece length")).longValue();
				this.hashes = (byte[])infoMap.get("pieces");
			}
		}
	}
	
	public String getAnnounce() {
		return announce;
	}

	public String getTorrentName() {
		return torrentName;
	}

	public long getFileLength() {
		return fileLength;
	}

	public long getPieceLength() {
		return pieceLength;
	}

	public ArrayList<byte[]> getHashes() {
		
		ArrayList<byte[]> hashesList = new ArrayList<byte[]>();
		
		for(int i = 0; i < hashes.length/20; i++) {
			byte hash[] = new byte[20];
			for(int j = 0; j < 20; j++) {
				hash[j] = hashes[i*20 + j];
			}
			hashesList.add(hash);
		}
		return hashesList;
	}
}
