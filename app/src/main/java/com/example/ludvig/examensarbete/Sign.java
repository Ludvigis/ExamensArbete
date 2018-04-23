package com.example.ludvig.examensarbete;

import android.util.Log;

import java.io.IOException;


public class Sign {
	private HDVECTOR relation;
	private HDVECTOR firstObj;
	private HDVECTOR secObj;
	private HDVECTOR sameDifferent;
	private Memory mem;


	public Sign(Memory mem){
		this.mem = mem;
	}

	public Sign(Memory mem,HDVECTOR relation,HDVECTOR firstObj,HDVECTOR secObj, HDVECTOR sameDifferent){
		this.relation = relation;
		this.firstObj = firstObj;
		this.secObj = secObj;
		this.sameDifferent = sameDifferent;
		this.mem = mem;
	}
	
	public HDVECTOR getRelation() {
		return relation;
	}

	public HDVECTOR getFirstObj() {
		return firstObj;
	}

	public HDVECTOR getSecObj() {
		return secObj;
	}

	public HDVECTOR getSameDifferent() {
		return sameDifferent;
	}
	
	public void setRelation(HDVECTOR relation) {
		this.relation = relation;
	}

	public void setFirstObj(HDVECTOR firstObj) {
		this.firstObj = firstObj;
	}

	public void setSecObj(HDVECTOR secObj) {
		this.secObj = secObj;
	}

	public void setSameDifferent(HDVECTOR sameDifferent) {
		this.sameDifferent = sameDifferent;
	}
	
	public String toString(){
		return "\nSIGN \n"+ "Relation: "+relation +"\nFirst Object: " +firstObj + "\nSecond Object: "+ secObj+ "\nSame Different: "+ sameDifferent;
	}
	
	public Vector getEpisodeVector() throws IOException{
		if(this.relation != null && this.firstObj != null && this.secObj != null && this.sameDifferent != null){
			Log.d("BraTagg",mem.keysToString());

			Vector episode = VSA.bundling(mem.find(relation),VSA.binding(mem.find(HDVECTOR.ObjA), mem.find(firstObj)),VSA.binding(mem.find(HDVECTOR.ObjB),mem.find(secObj)),
					VSA.binding(mem.find(HDVECTOR.sameDifferent),mem.find(sameDifferent)));
			return episode;
		}else{
			return null;
		}
	}

}
