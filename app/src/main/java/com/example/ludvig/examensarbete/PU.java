package com.example.ludvig.examensarbete;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class PU {
	private Random rnd = new Random();
	private AEM aem;
	private EM em;
	
	public PU(AEM atomicEntityMemory, EM experienceMemory){
		aem = atomicEntityMemory;
		em = experienceMemory;
	}
	
	private Vector bundling (Vector ...vectors) throws IOException{
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
	
	private Vector bundling(ArrayList<Vector> list){
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
	
	private Vector binding (Vector v1, Vector v2) throws IOException{
		if( v1.size() != v2.size()){
			throw new IOException();
		}
		Vector vec = new Vector(v1.size());
		for (int i = 0; i < v1.size() ; ++i){
			vec.putBit(i, (byte) (v1.getBit(i) ^ v2.getBit(i)));
		}
		
		return vec;
		
	}
	
	private double hammingDist(Vector v1 , Vector v2) throws IOException{
		if( v1.size() != v2.size()){
			throw new IOException();
		}
		double sum = 0;
		for(int i = 0 ; i < v1.size();++i){
				sum += v1.getBit(i) ^ v2.getBit(i);
			
		}
		
		return sum/v1.size();
	}
	
	public Vector BasicEncoding(Sign s) throws IOException{
		Vector vec =	bundling(binding(aem.find(HDVECTOR.top_left), aem.find(s.getTopLeftShape()))
						,binding(aem.find(HDVECTOR.top_right), aem.find(s.getTopRightShape()))
						,binding(aem.find(HDVECTOR.bottom_left),aem.find(s.getBottomLeftShape()))
						,binding(aem.find(HDVECTOR.bottom_right),aem.find(s.getBottomRightShape())));
		return vec;
	}
	
	public void SaveEncodingAndDirectionToExp(Vector encoding,DIR d) throws IOException{
		Vector temp = null;
		if(d == DIR.LEFT){
			temp = bundling(binding(encoding,aem.find(HDVECTOR.LEFT)),aem.find(HDVECTOR.REWARD));
		}else if(d == DIR.RIGHT){
			temp = bundling(binding(encoding,aem.find(HDVECTOR.RIGHT)),aem.find(HDVECTOR.REWARD));
		}
		if(temp == null){
			return;
		}
		em.setExperience(temp);
		return;
	}
	
	public void SaveEncodingAndDirectionToExp(Sign s,DIR d) throws IOException{
		Vector encoding = BasicEncoding(s);
		Vector temp = null;
		if(d == DIR.LEFT){
			temp = binding(binding(encoding,aem.find(HDVECTOR.LEFT)),aem.find(HDVECTOR.REWARD));
		}else if(d == DIR.RIGHT){
			temp = binding(binding(encoding,aem.find(HDVECTOR.RIGHT)),aem.find(HDVECTOR.REWARD));
		}
		if(temp == null){
			return;
		}
		em.setExperience(temp);
		return;
	}
	
	public DIR checkForBestMatch(Sign left,Sign Right) throws IOException{
		Vector leftVec = binding(BasicEncoding(left),aem.find(HDVECTOR.LEFT));
		Vector rightVec = binding(BasicEncoding(Right), aem.find(HDVECTOR.RIGHT));
		if(em.getExperienceVector().size() == 0){
			return DIR.LEFT;
		}
		Vector mappingVec = bundling(em.getExperienceVector());
		double leftDist = 1;
		double rightDist = 1;
		
		leftDist = hammingDist(binding(mappingVec,leftVec),aem.find(HDVECTOR.REWARD));
		rightDist = hammingDist(binding(mappingVec,rightVec), aem.find(HDVECTOR.REWARD));
		
		System.out.println("LEFT HAMMING DIST -> " + leftDist + "\nRIGHT HAMMING DIST -> "+ rightDist);
		return leftDist < rightDist ? DIR.LEFT : DIR.RIGHT;
	}
}
