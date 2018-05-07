package com.example.ludvig.examensarbete;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class EM {

	private ArrayList<Vector> exp_episodes;
	private Context context;
	private static final String TAG = "EM";
	
	public EM(boolean loadPersistent, Context context) {
		exp_episodes = new ArrayList<Vector>();
		this.context = context;
		if(loadPersistent){
			try {
				loadPersistent();
			} catch (ClassNotFoundException e) {
				Log.e(TAG, "Class not found: "+ e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "IO exception: "+ e.getMessage());
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

		FileOutputStream fs = context.openFileOutput(f.getName(),Context.MODE_PRIVATE);
		ObjectOutputStream oos = new ObjectOutputStream(fs);
		oos.writeObject(exp_episodes);
		oos.close();
		fs.close();
	}
	
	@SuppressWarnings("unchecked")
	public void loadPersistent() throws IOException, ClassNotFoundException{
		File f = new File("VSA_EXP");
		FileInputStream fs = context.openFileInput(f.getName());
		ObjectInputStream ois = new ObjectInputStream(fs);
		exp_episodes = (ArrayList<Vector>) ois.readObject();
		ois.close();
		fs.close();
	}
}
