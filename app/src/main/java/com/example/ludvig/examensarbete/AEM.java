package com.example.ludvig.examensarbete;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Set;



public class AEM {

	private HashMap<HDVECTOR,Vector> map = new HashMap<HDVECTOR, Vector>();
	private Context context;
	private static final String TAG = "AEM";
	public AEM(boolean loadPersistentVectors,Context context){
		this.context = context;
		if(loadPersistentVectors){
			try {
				loadPersistent();

			} catch (ClassNotFoundException | IOException e) {
				System.out.println("Could not load persistent data");
				Log.e(TAG, "Class not found: "+ e.getMessage());
			}
		}else{
			for(HDVECTOR vec : HDVECTOR.values()){
				saveRndGeneratedVector(vec);
			}
		}
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
	
	public void savePersistent() throws IOException{

		FileOutputStream fs = context.openFileOutput("VSA_MEM",Context.MODE_PRIVATE);
		ObjectOutputStream oos = new ObjectOutputStream(fs);
		oos.writeObject(map);
		oos.close();
		fs.close();
	}
	
	@SuppressWarnings("unchecked")
	public void loadPersistent() throws IOException, ClassNotFoundException{
		File f = new File("VSA_MEM");
		FileInputStream fs = context.openFileInput(f.getName());
		ObjectInputStream ois = new ObjectInputStream(fs);
		map = (HashMap<HDVECTOR, Vector>) ois.readObject();
		ois.close();
		fs.close();
	}
}
