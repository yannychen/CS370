package edu.cuny.qc.JsoupDemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DataAccess {
	
	static FirebaseOptions options;
	static Firestore db;
	static DocumentReference doc;
	static ApiFuture<WriteResult> future;
	static ApiFuture<QuerySnapshot> query;
	static List<QueryDocumentSnapshot> documents;
	
	public DataAccess() {
		doc=null;
		future=null;
		query=null;
		documents=null;
	
	}
	
	public static void connectDB() throws IOException {
		FileInputStream serviceAccount = new FileInputStream("symbolic-path.json");
		
		options = new FirebaseOptions.Builder()
		    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
		    .setDatabaseUrl("https://symbolic-path-223920.firebaseio.com/")
		    .build();

		FirebaseApp.initializeApp(options);
		
		
		db = FirestoreClient.getFirestore();
//		System.out.println("Connected");
		
		
				

				
	}
	
	public static void addCollegeInfo(String[] collegeID, String[] collegeName) throws InterruptedException, ExecutionException {
		Map<String, String> data;
		
	
		for(int i=1; i<collegeID.length; i++) {
			data=new HashMap<String, String>();
			data.put("name", collegeName[i]);
			future=db.collection("colleges").document(collegeID[i]).set(data);
		} 
		
		System.out.println("Update time : " + future.get().getUpdateTime());	
	}
	
	public static void addMajorInfo(Map<String, String> major) throws InterruptedException, ExecutionException {
	for(String key : major.keySet()){			
			Map<String, String> majorInfo = new HashMap<String, String>();
			majorInfo.put(key, major.get(key));
			doc= db.collection("colleges").document("QNS01");
			future= doc.collection("majors").document(key).set(majorInfo);
		}
		
		System.out.println("Update time : " + future.get().getUpdateTime());	
	}
	
	public static void addTermInfo(Map<String, String> term) throws InterruptedException, ExecutionException {
		Map<String, String> termInfo;
		query= doc.collection("majors").get();
		// future.get() blocks on response
		documents= query.get().getDocuments();
		for(QueryDocumentSnapshot document : documents) {
			  //System.out.println(document.getId() + " => " + document.getData());
			for(String key : term.keySet()) {
				termInfo= new HashMap<String, String>();
				termInfo.put("name", term.get(key));
				DocumentReference docs= doc.collection("majors").document(document.getId());
				docs= docs.collection("terminfo").document(key);
				future= docs.set(termInfo);
			}
		}
		System.out.println("Update time : " + future.get().getUpdateTime());	
	}
	
	public static void addCareerLevel(Map<String, String> careerLevel, Map<String, String> term) throws InterruptedException, ExecutionException {
		Map<String, String> level;
		query= doc.collection("majors").get();
		
		documents= query.get().getDocuments();
		
		for(QueryDocumentSnapshot document : documents){
			
			for(String termKey : term.keySet()) {
				DocumentReference docs= doc.collection("majors").document(document.getId());
				docs= docs.collection("terminfo").document(termKey);
				
				for(String levelKey : careerLevel.keySet()) {
					level= new HashMap<String, String>();
					level.put("name", careerLevel.get(levelKey));
					future= docs.collection("careerlevel").document(levelKey).set(level);
				}		
			}		
		}
		System.out.println("Update time : " + future.get().getUpdateTime());	
	}
	
	public static void addCourses(String school, String major, String term, String careerlevel, HashMap<String, HashMap<String, HashMap<String, String>>> courseMap) throws InterruptedException, ExecutionException {		
		CollectionReference courseNumCollection= db
				.collection("colleges").document(school)
				.collection("majors").document(major)
				.collection("terminfo").document(term)
				.collection("careerlevel").document(careerlevel)
				.collection("coursenumber");
		
		for (Entry<String, HashMap<String, HashMap<String, String>>> course : courseMap.entrySet()) {
			DocumentReference courseDocument = courseNumCollection.document(course.getKey().substring(0, course.getKey().indexOf(" - ")));
	
			HashMap<String, Object> courseDocumentFields = new HashMap<>();
			courseDocumentFields.put("name", course.getKey());
			
			courseDocument.set(courseDocumentFields);
			
			CollectionReference sectionsCollection = courseDocument.collection("sections");
			for (Entry<String, HashMap<String, String>> section : course.getValue().entrySet()) {
				sectionsCollection.document(section.getKey()).set(section.getValue());
			}
		}
		
				
		System.out.println("Update time : " + future.get().getUpdateTime());
		System.out.println("done");
	}

}
