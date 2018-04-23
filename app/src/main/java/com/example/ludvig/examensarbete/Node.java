package com.example.ludvig.examensarbete;

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
			left = VSA.hammingDist(VSA.binding(mem.getExperienceVector(), leftSign.getEpisodeVector()), mem.find(HDVECTOR.REWARD));
			right = VSA.hammingDist(VSA.binding(mem.getExperienceVector(), rightSign.getEpisodeVector()), mem.find(HDVECTOR.REWARD)); 
		
		System.out.println("LEFT HAMMINGDIST: "+ left + " RIGHT HAMMINGDIST: "+ right);
		return left < right ? leftPath : rightPath;
	}
	
	public void manualTraining() throws ClassNotFoundException, IOException{
		Memory mem = Memory.getInstance();
		System.out.println("LEFT"+ leftSign.toString());
		System.out.println("RIGHT"+ rightSign.toString());
		
		System.out.println("CHOOSE PATH FOR REWARD l/r?");
		Scanner scnr = new Scanner(System.in);
		int temp = scnr.nextByte();
		if(temp == 1){
			addEpisodeToExperience(leftSign.getEpisodeVector());
			System.out.println("CHOOSED LEFT");
		}else if(temp == 2){
			addEpisodeToExperience(rightSign.getEpisodeVector());
			System.out.println("COOSED RIGHT");
		}
		
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
