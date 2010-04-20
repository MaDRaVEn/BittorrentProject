package com.cmsc417.project5;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class PeerConnection implements Runnable {

	private Socket socket;
	private boolean amChoking;
	private boolean amInterested;
	private boolean peerChoking;
	private boolean peerInterested;
	
	private byte[] peerBitfield;
	private ArrayList<Request> peerRequests;
	private ArrayList<Piece> myPieces;
	
	private int pieceSize;
	
	public PeerConnection(Socket socket, int numPieces, int pieceSize) {
		this.socket = socket;
		this.amChoking = true;
		this.amInterested = false;
		this.peerChoking = true;
		this.peerInterested = false;
		this.peerBitfield = new byte[numPieces/8];
		
		for(int i = 0; i < peerBitfield.length; i++) {
			peerBitfield[i] = 0;
		}
		
		this.peerRequests = new ArrayList<Request>();
		this.myPieces = new ArrayList<Piece>();
		this.pieceSize = pieceSize;
	}
	
	public void run() {
		
	}
	
	private void readMessage() {
		
		InputStream in = null;
		byte[] lengthBytes;
		byte[] buffer;
		int length = 0;
		
		try {
			in = socket.getInputStream();
			if(in.available() != 0) {
				
				lengthBytes = readBytes(in, 4);
				length = bytesToInt(lengthBytes);
				
				if(length != 0) {
					buffer = readBytes(in, length);
					if(buffer.length > 1) {
						byte[] payload = new byte[buffer.length-1];
						for(int i = 1; i < buffer.length; i++) {
							payload[i-1] = buffer[i];
						}
						handleMessage(buffer[0], payload);
					} else {
						handleMessage(buffer[0], null);
					}
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private byte[] readBytes(InputStream in, int num) {
		int bytesRead = 0;
		byte[] buffer = new byte[num];
		
		try {
			bytesRead = in.read(buffer);
			
			while(bytesRead < num) {
				bytesRead += in.read(buffer, bytesRead, num - bytesRead);
			}
			
		} catch (IOException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return null;
			}
		}
		return buffer;
	}
	
	private int bytesToInt(byte[] bytes) {
		
		int result = 0;
		
		if(bytes.length < 5) { 
			int shift = (bytes.length-1) * 8;
		
			for(int i = 0; i < bytes.length; i++) {
				result = result | ((0xFF) & bytes[i] << shift);
				shift -= 8;
			}
		}
		
		return result;
	}
	
	private void handleMessage(byte ID, byte[] payload) {
		
		switch(ID) {
		case 0: setPeerChoking(true); break;
		case 1: setPeerChoking(false); break;
		case 2: setPeerInterested(true); break;
		case 3: setPeerInterested(false); break;
		case 4: peerHas(bytesToInt(payload)); break;
		case 5:
			
			for(int i = 0; i < peerBitfield.length; i++) {
				peerBitfield[i] |= (0xFF & payload[i]);
			}
			break;
			
		default:
			
			if(ID == 6 || ID == 7 || ID == 8) {
				
				byte[] indexBytes = new byte[4];
				byte[] offsetBytes = new byte[4];
				
				
				int index, offset = 0;
				
				for(int i = 0; i < 4; i++) {
					indexBytes[i] = payload[i];
				}
				
				for(int i = 4; i < 8; i++) {
					offsetBytes[i-4] = payload[i];
				}
				
				index = bytesToInt(indexBytes);
				offset = bytesToInt(offsetBytes);
				
				if(ID == 6 || ID == 8) {
					
					byte[] lengthBytes = new byte[4];
					int length = 0;
					
					for(int i = 8; i < 12; i++) {
						lengthBytes[i-8] = payload[i];
					}
					
					length = bytesToInt(lengthBytes);
					Request request = new Request(index, offset, length);
					
					if(peerRequests.contains(request)) {
						peerRequests.remove(request);
					}
					
					if(ID == 6)
						peerRequests.add(request);
					
				} else if(ID == 7) {
					
					byte[] data = new byte[payload.length - 8];
					for(int i = 8; i < payload.length; i++) {
						data[i-8] = payload[i];
					}
					
					Piece writtenPiece = new Piece(pieceSize, index);
					
					for(Piece piece : myPieces) {
						if(piece.getIndex() == index) {
							writtenPiece = piece;
							break;
						}
					}
					
					writtenPiece.write(offset, data);
					if(!myPieces.contains(writtenPiece)) {
						myPieces.add(writtenPiece);
					}
				}
			}		
		}
	}
	
	private synchronized void peerHas(int piece) {
		int byteIndex = piece/8;
		int bitOffset = piece - (8*byteIndex) - 1;
		peerBitfield[byteIndex] |= (0x80 >> bitOffset);
	}
	
	private synchronized void setPeerChoking(boolean choking) {
		this.peerChoking = choking;
	}
	
	private synchronized void setPeerInterested(boolean interested) {
		this.peerInterested = interested;
	}

}
