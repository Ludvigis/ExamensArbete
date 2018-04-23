package com.example.ludvig.examensarbete;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Memory {
	
	private HashMap<HDVECTOR,Vector> map = new HashMap<HDVECTOR, Vector>();
	private static Memory mem;
	private static boolean instance = false;
	private static ArrayList<Vector> exp_episodes;
	
	
	public static Memory getInstance() throws ClassNotFoundException, IOException{
		if(instance){
			return mem;
		}
		instance = true;
		mem = new Memory();
		return mem;
	}
	
	private Memory() throws ClassNotFoundException, IOException{
		for(HDVECTOR vec : HDVECTOR.values()){
			if(vec == HDVECTOR.EXPERIENCE){
				
			}else{
				saveRndGeneratedVector(vec);
			}
		}
		exp_episodes = new ArrayList<Vector>();
		
	}
	
	
	public void save(HDVECTOR name , Vector v){
		map.put(name, v);
	}
	
	public Vector find(HDVECTOR name){
		return map.get(name);
	}
	
	public void saveRndGeneratedVector(HDVECTOR name){
		map.put(name, new Vector());
	}
	public void saveRndGeneratedVector(HDVECTOR name, int size){
		map.put(name, new Vector(size));
	}
	
	public void savePersistentMem(Context context) throws IOException{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File f = new File(Environment.getExternalStorageDirectory().getPath(),"VSA_MEM");

			FileOutputStream fs = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fs);
			oos.writeObject(map);
			oos.close();
			fs.close();
		}

	}
	
	public void loadPersistentMem(Context context) throws IOException, ClassNotFoundException{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File f = new File(Environment.getExternalStorageDirectory().getPath(),"VSA_MEM");
			FileInputStream fs = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fs);
			map = (HashMap<HDVECTOR, Vector>) ois.readObject();
			ois.close();
			fs.close();
		}
	}
	
	public void savePersistentExp(Context context) throws IOException{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File f = new File(Environment.getExternalStorageDirectory().getPath(),"VSA_EXP");
			Log.i("asdfg",f.getAbsolutePath());
			FileOutputStream fs = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fs);
			oos.writeObject(exp_episodes);
			oos.close();
			fs.close();
		}
	}
	
	public void loadPersistentExp(Context context) throws IOException, ClassNotFoundException{
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File f = new File(Environment.getExternalStorageDirectory().getPath(),"VSA_EXP");
			FileInputStream fs = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fs);
			exp_episodes = (ArrayList<Vector>) ois.readObject();
			ois.close();
			fs.close();
		}
	}

	public boolean hasKey(HDVECTOR key){
		return this.map.containsKey(key);
	}
	
	public String keysToString(){
		Set<HDVECTOR> HDVECTOR_set = map.keySet();
		String tmp = new String("");
		Object[] a = HDVECTOR_set.toArray();
		for(int i = 0; i < a.length; i++){
			tmp += a[i].toString()+ "\n";
		}
		return tmp;
	}
	
	public Map<HDVECTOR,Vector> getMap(){
		return map;
	}
	
	public void  setExperience(Vector exp){
		exp_episodes.add(exp);
	}
	
	public Vector getExperienceVector(){
		return VSA.bundling(exp_episodes);
	}

}
