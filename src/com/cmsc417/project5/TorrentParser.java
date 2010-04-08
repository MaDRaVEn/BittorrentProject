package com.cmsc417.project5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.googlecode.jbencode.Parser;
import com.googlecode.jbencode.composite.DictionaryValue;
import com.googlecode.jbencode.composite.EntryValue;
import com.googlecode.jbencode.primitive.IntegerValue;
import com.googlecode.jbencode.primitive.StringValue;


public class TorrentParser {
	
	private DictionaryValue dict;
	private String announce = "";
	private String torrentName = "";
	private long fileLength = 0;
	private long pieceLength = 0;
	private ArrayList<byte[]> hashes = null;
	
	
	public TorrentParser(File file) {
		FileInputStream fileInput;
		
		try {
			fileInput = new FileInputStream(file);
			Parser parser = new Parser();
			dict = (DictionaryValue)parser.parse(fileInput);
		} catch (FileNotFoundException e) {
			System.err.println("File: " + file + " not found.");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.parse();
	}
	
	private void parse() {
		
		String dictKey = "";
		
		for(EntryValue entry : dict) {
			dictKey = getKey(entry);
			if(dictKey.equals("announce")) {
				this.announce = getStringValue(entry);
			} else if(dictKey.equals("info")) {
				DictionaryValue infoDict = null;
				String infoKey = "";
				try {
					infoDict = (DictionaryValue)entry.getValue();
				} catch (IOException e) { e.printStackTrace(); }
				if(infoDict != null) {
					for(EntryValue infoEntry : infoDict) {
						infoKey = getKey(infoEntry);
						if(infoKey.equals("name")) {
							this.torrentName = getStringValue(infoEntry);
						} else if(infoKey.equals("length")) {
							this.fileLength = getIntegerValue(infoEntry);
						} else if(infoKey.equals("piece length")) {
							this.pieceLength = getIntegerValue(infoEntry);
						} else if(infoKey.equals("pieces")) {
							this.hashes = getHashesValue(infoEntry);
						}
					}
				}
			} else {
				try { entry.getValue().resolve(); } 
				catch (IOException e) { e.printStackTrace(); }
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
		return hashes;
	}
	
	private String getKey(EntryValue value) {
		StringValue key = null;
		String returned = null;
		
		try {
			key = value.getKey();
			returned = new String(key.resolve());
		} catch (IOException e) { e.printStackTrace(); }
		
		return returned;
	}

	private String getStringValue(EntryValue value) {
		String returned = null;
		StringValue val = null;
		
		try {
			val = (StringValue)value.getValue();
			returned = new String(val.resolve());
		} catch (IOException e) { e.printStackTrace(); }
		
		return returned;
	}
	
	private long getIntegerValue(EntryValue value) {
		long returned = 0;
		IntegerValue val = null;
		
		try {
			val = (IntegerValue)value.getValue();
			returned = val.resolve();
		} catch (IOException e) { e.printStackTrace(); }
		
		return returned;
	}
	
	private ArrayList<byte[]> getHashesValue(EntryValue value) {
		
		byte[] hashes = {};
		ArrayList<byte[]> returned = new ArrayList<byte[]>();
		
		try {
			hashes = ((StringValue)value.getValue()).resolve();
		} catch (IOException e) { e.printStackTrace(); }
		
		//Separate hash block into 20-byte hashes
		for(int i = 0; i < hashes.length/20; i++) {
			byte hash[] = new byte[20];
			for(int j = 0; j < 20; j++) {
				hash[j] = hashes[i*20+j];
			}
			returned.add(hash);
		}
		
		return returned;
	}
}
