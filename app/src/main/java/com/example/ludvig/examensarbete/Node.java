package com.example.ludvig.examensarbete;

import android.util.Log;

import java.io.IOException;
import java.util.Scanner;


public class Node {

	private Node leftPath;
	private Node rightPath;
	private Sign leftSign;
	private Sign rightSign;
	private String name;
	
	public Node(Sign leftSign, Sign rightSign, String name){
		this.leftSign = leftSign;
		this.rightSign = rightSign;
		this.name = name;
		
	}
	
	public Node checkForBestRewardMatch() throws ClassNotFoundException, IOException{
		Memory mem = Memory.getInstance();
		double left = 1;
		double right = 1;
		Vector leftV = leftSign.getEpisodeVector();
		Vector rightV = rightSign.getEpisodeVector();
		if(leftV == null || rightV ==null){
			return null;
		}else {
			left = VSA.hammingDist(VSA.binding(mem.getExperienceVector(), leftV), mem.find(HDVECTOR.REWARD));
			right = VSA.hammingDist(VSA.binding(mem.getExperienceVector(), rightV), mem.find(HDVECTOR.REWARD));
		}
		Log.i("VSANode","LEFT HAMMINGDIST: "+ left + " RIGHT HAMMINGDIST: "+ right);
		return left < right ? leftPath : rightPath;
	}

	
	public void addEpisodeToExperience(Vector episode) throws ClassNotFoundException, IOException{
		Memory mem = Memory.getInstance();
		mem.setExperience(VSA.binding(episode,mem.find(HDVECTOR.REWARD)));
	}
	
	public String getName(){
		return name;
	}
	
	public Node getLeftPath() {
		return leftPath;
	}

	public Node getRightPath() {
		return rightPath;
	}

	public void setLeftPath(Node leftPath) {
		this.leftPath = leftPath;
	}

	public void setRightPath(Node rightPath) {
		this.rightPath = rightPath;
	}
}
