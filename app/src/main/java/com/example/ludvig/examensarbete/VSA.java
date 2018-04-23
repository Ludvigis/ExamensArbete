package com.example.ludvig.examensarbete;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;



public class VSA {
	static Random rnd = new Random();
	
	public static Vector bundling (Vector ...vectors) throws IOException{
		int size =  vectors[1].size();
		for(Vector vec : vectors){
			if(vec.size() != size){
				throw new IOException(); 
			}
		}
		
		Vector vec = new Vector(size);		
		for(int i = 0; i < size; ++i){
			double sum = 0.0f;
			for(int j = 0; j < vectors.length ; ++j){
				sum += vectors[j].getBit(i);
			}
			sum = (sum/vectors.length);
			if(sum== 0.5f){
				vec.putBit(i, (byte)rnd.nextInt(2));
			}else{
				vec.putBit(i, (byte)Math.round(sum));
			}
		}
		return vec;
		
	}
	
	public static Vector bundling (ArrayList<Vector> list){
		int size = list.get(0).size();
		Vector vec = new Vector(size);		
		for(int i = 0; i < size; ++i){
			double sum = 0.0f;
			for(int j = 0 ; j < list.size(); ++j){
				sum += list.get(j).getBit(i);
			}
			sum = (sum/list.size());;
			if(sum== 0.5f){
				vec.putBit(i, (byte)rnd.nextInt(2));
			}else{
				vec.putBit(i, (byte)Math.round(sum));
			}
		}
		return vec;
	}
	
	public static Vector binding (Vector v1, Vector v2) throws IOException{
		if( v1.size() != v2.size()){
			throw new IOException();
		}
		Vector vec = new Vector(v1.size());
		for (int i = 0; i < v1.size() ; ++i){
			vec.putBit(i, (byte) (v1.getBit(i) ^ v2.getBit(i)));
		}
		
		return vec;
		
	}
	
	public static double hammingDist(Vector v1 , Vector v2) throws IOException{
		if( v1.size() != v2.size()){
			throw new IOException();
		}
		double sum = 0;
		for(int i = 0 ; i < v1.size();++i){
				sum += v1.getBit(i) ^ v2.getBit(i);
			
		}
		
		return sum/v1.size();
	}
	
	
	
	
}
