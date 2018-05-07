package com.example.ludvig.examensarbete;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class EM {

	private ArrayList<Vector> exp_episodes;
	
	public EM(boolean loadPersistent) {
		exp_episodes = new ArrayList<Vector>();
		if(loadPersistent){
			try {
				loadPersistent();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void  setExperience(Vector exp){
		exp_episodes.add(exp);
	}
	
	public ArrayList<Vector> getExperienceVector(){
		return exp_episodes;
	}
	
	public void savePersistent() throws IOException{
		File f = new File("VSA_EXP");
		FileOutputStream fs = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fs);
		oos.writeObject(exp_episodes);
		oos.close();
		fs.close();
	}
	
	@SuppressWarnings("unchecked")
	public void loadPersistent() throws IOException, ClassNotFoundException{
		File f = new File("VSA_EXP");
		FileInputStream fs = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fs);
		exp_episodes = (ArrayList<Vector>) ois.readObject();
		ois.close();
		fs.close();
	}
}
