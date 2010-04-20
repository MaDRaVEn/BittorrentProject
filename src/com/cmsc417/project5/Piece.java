package com.cmsc417.project5;

public class Piece {
	
	private byte[] data;
	private boolean[] completed;
	private int index;
	
	public Piece(int size, int index) {
		this.data = new byte[size];
		this.completed = new boolean[size];
		this.index = index;
		
		for(int i = 0; i < size; i++) {
			data[i] = 0;
			completed[i] = false;
		}
	}
	
	public void write(int offset, byte[] data) {
		for(int i = offset; i < data.length; i++) {
			if(completed[i] != true) {
				this.data[i] = data[i-offset];
				this.completed[i] = true;
			}
		}
	}
	
	public boolean completed() {
		for(int i = 0; i < data.length; i++) {
			if(completed[i] != true)
				return false;
		}
		return true;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Piece) {
			Piece piece = (Piece)obj;
			if(piece.getIndex() == this.index) {
				return true;
			}
		}
		return false;
	}

}
