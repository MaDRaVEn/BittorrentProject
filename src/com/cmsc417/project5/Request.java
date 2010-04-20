package com.cmsc417.project5;

public class Request {
	
	public int index;
	public int offset;
	public int length;
	
	public Request(int index, int offset, int length) {
		this.index = index;
		this.offset = offset;
		this.length = length;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof Request) {
			Request request = (Request)obj;
			if((this.index == request.index) &&
			   (this.offset == request.offset)) {
				return true;
			}
		}
		return false;
	}

}
