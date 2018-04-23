package com.example.ludvig.examensarbete;

import java.io.Serializable;
import java.util.Random;

public class Vector implements Serializable{

	private byte[] vector;
	private int size;
	Random rnd;
	
	public Vector(){
		rnd = new Random();
		vector = new byte[10000];
		size = 10000;
		randomInit(size);
	}
	
	public Vector(int size){
		rnd = new Random();
		vector = new byte[size];
		this.size = size;
		randomInit(size);
	}
	
	private void randomInit(int bits) {
		for(int i = 0 ; i < bits ; ++i){
			vector[i] = (byte) rnd.nextInt(2);
		}
		
	}

	public int size(){
		return size;
	}
	
	public byte getBit(int index){
		return vector[index];
	}
	
	public void putBit(int index, byte value){
		vector[index] = value;
	}
	
	public String toString(){
		String str = "";
		for(int i = 0; i < size; ++i){
			str += vector[i]+"|";
		}
		return str;
	}
}
