package net.stevencai;

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class Firebase {
    private static Firestore db;
    static{
        FileInputStream serviceAccount = null;
        try {
            //serviceAccount = new FileInputStream("resources/symbolic-path-223920-firebase-adminsdk-7zqif-b2652eb035.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.getApplicationDefault())
                    .setProjectId("symbolic-path-223920")
                    .setDatabaseUrl("https://symbolic-path-223920.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            db= FirestoreClient.getFirestore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String searchUser(){
        //TODO: need to search user courses.

        return null;
    }
    private static List<String> addToList(ApiFuture<QuerySnapshot> query) throws ExecutionException, InterruptedException {
        List<String> result=new LinkedList<>();
        QuerySnapshot qs=query.get();
        List<QueryDocumentSnapshot> documents=qs.getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            result.add(document.getId());
        }
        return result;
    }
    public static boolean isSectionOpen(String college, String major, String term, String career, String course, String section) throws ExecutionException, InterruptedException {
        DocumentSnapshot query=db.collection("colleges")
                .document(college).collection("majors")
                .document(major).collection("terminfo")
                .document(term).collection("careerlevel")
                .document(career).collection("coursenumber")
                .document(course).collection("sections")
                .document(section)
                .get().get();
        return query.get("status").equals("Open");
    }
    public static List<String> searchSections(String college, String major, String term, String career, String course) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query=db.collection("colleges")
                .document(college).collection("majors")
                .document(major).collection("terminfo")
                .document(term).collection("careerlevel")
                .document(career).collection("coursenumber")
                .document(course).collection("sections")
                .get();
        return addToList(query);
    }
    public static List<String> searchCourses(String college, String major, String term, String career) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query=db.collection("colleges")
                .document(college).collection("majors")
                .document(major).collection("terminfo")
                .document(term).collection("careerlevel")
                .document(career).collection("coursenumber")
                .get();
        return addToList(query);
    }
    public static List<String> searchCareers(String college, String major, String term) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query=db.collection("colleges")
                .document(college).collection("majors")
                .document(major).collection("terminfo")
                .document(term).collection("careerlevel")
                .get();
        return addToList(query);
    }
    public static List<String> searchTerms(String college, String major) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query=db.collection("colleges")
                .document(college).collection("majors")
                .document(major).collection("terminfo")
                .get();
        return addToList(query);
    }
    public static List<String> searchMajors(String college) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query=db.collection("colleges").document(college).collection("majors").get();
        return addToList(query);
    }

    /**
     * search all the colleges in firebase
     * @return list of colleges code.
     * @throws ExecutionException exception
     * @throws InterruptedException exception
     */
    public static List<String> searchColleges() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query=db.collection("colleges").get();
        return addToList(query);
    }
//    /**
//     * get alll the user cookies from users table.
//     * @return return the list of set of cookies
//     * @throws ExecutionException exception
//     * @throws InterruptedException exception
//     */
//    public static List<Set<Cookie>> getCookie() throws ExecutionException, InterruptedException {
//        List<Set<Cookie>> results=new LinkedList<>();
//        ApiFuture<QuerySnapshot> query=db.collection("users").get();
//        QuerySnapshot snapshot=query.get();
//        List<QueryDocumentSnapshot> documents=snapshot.getDocuments();
//
//        for(DocumentSnapshot d:documents){
//            Set<Cookie> cookies=getUserCookie(d.getId());
//            results.add(cookies);
//        }
//        return results;
//    }

    /**
     * get the use cookies using device id.
     * @param tokenid device id.
     * @return the cookie set.
     * @throws ExecutionException exception.
     * @throws InterruptedException exception.
     */
    public static Set<Cookie> getUserCookie(String tokenid) throws ExecutionException, InterruptedException {
        Set<Cookie> cookieset = new HashSet<>();
        ApiFuture<DocumentSnapshot> qs = db.collection("users").document(tokenid).get();

        DocumentSnapshot document = qs.get();
        Map<String, String> cookies = (Map<String, String>) document.get("cookies");
        if (cookies != null)
            cookies.forEach((k, v) -> {
                if(!k.startsWith("https://ssologin")) return;
                String[] array = v.split("\\s*;\\s*");

                for (String a : array) {
                    String[] pair = a.split("=");
                    Date date = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 10);
                    k = k.replace("https://", "");
                    if (pair.length == 2) {
                        Cookie cookie = new Cookie(k, pair[0], pair[1], "/", date, true);
                        cookieset.add(cookie);
                    } else {
                        Cookie cookie = new Cookie(k, pair[0], "", "/", date, true);
                        cookieset.add(cookie);
                    }
                }
            });
        return cookieset;
    }

    /**
     * get the use token id.
     * @param college college
     * @param term term
     * @param career career
     * @param section section
     * @return device id.
     * @throws ExecutionException exception
     * @throws InterruptedException exception
     */
    private static List<String> getUserToken(String college,String term,String career,String section) throws ExecutionException, InterruptedException {
        Query query=db.collection("priority1")
                .document(college).collection(term)
                .document(career).collection(section)
                .orderBy("time");
        List<QueryDocumentSnapshot> snapshots= query.get().get().getDocuments();
        List<String> deviceids=new LinkedList<>();
        for (QueryDocumentSnapshot snapshot : snapshots) {
            deviceids.add(snapshot.getId());
        }
        return deviceids;
    }
    public static void searchOpenCoursesAndRegister() throws ExecutionException, InterruptedException {
        List<String> colleges=searchColleges();
        colleges.forEach(college->{
            try {
                searchMajors(college).forEach(major->{
                    try {
                        searchTerms(college,major).forEach(term->{
                            if(term.equals("1189")) return;
                            try {
                                searchCareers(college,major,term).forEach(career->{
                                    //if(career.equals("GRAD")) return;
                                    try {
                                        searchCourses(college,major,term,career).forEach(course->{
                                            try {
                                                searchSections(college,major,term,career,course).forEach(section->{
////
                                                    try {
                                                        if (isSectionOpen(college, major, term, career, course, section))
                                                         {
                                                             List<String> deviceIDs=getUserToken(college,term,career,section);
                                                             for (String deviceID : deviceIDs) {
                                                                 if(!RegisterCourseThread.hasSpots) break;
                                                                 Set<Cookie> cookies=getUserCookie(deviceID);
                                                                 RegisterCourseThread thread=new RegisterCourseThread(cookies,section,career,term,college,deviceID);
                                                                 thread.start();
                                                                 //System.out.println(course+" "+section);
                                                                 Thread.sleep(1000);
                                                             }
                                                             RegisterCourseThread.hasSpots=true;

                                                        }
                                                    } catch (ExecutionException e) {
                                                        e.printStackTrace();
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    catch(Exception ex){
                                                        //ex.printStackTrace();
                                                    }
                                                    finally {
                                                        {
                                                            RegisterCourseThread.hasSpots=true;
                                                        }
                                                    }
                                                });
                                            } catch (ExecutionException e) {
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * delete user from priority table.
     * @param college
     * @param term
     * @param career
     * @param section
     * @param deviceID
     */
    public static void deleteUserFromQueue(String college,String term,String career,String section,String deviceID){
        ApiFuture<WriteResult> writeResult = db.collection("priority1").document(college)
                .collection(term).document(career)
                .collection(section).document(deviceID)
                .delete();

    }

    public static void updateUserInfo(String section,String deviceID,String status,String title,String body) throws ExecutionException, InterruptedException {
        Map<String, Object> user= db.collection("users")
                .document(deviceID)
                .get().get().getData();

        String fcmToken = (String) user.get("fcmToken");
       // System.out.println(fcmToken);
        Message fcmMessage = Message.builder()
                .setNotification(new Notification(title, body))
                .setToken(fcmToken)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(fcmMessage);
           // System.out.println(response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

     //   System.out.println(user.keySet());
       // System.out.println(section);
        Map<String,Object> sectionname= (Map<String, Object>) user.get(section);
        Map<String,Object> map=new HashMap<>();
        sectionname.put("status",status);
        map.put(section,sectionname);
        ApiFuture<WriteResult> update=db.collection("users")
                .document(deviceID)
                .set(map,SetOptions.merge());

//        System.out.println("+++++++++++++");
    }
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

//        Set<Cookie> cookies=getUserCookie("Xm45YTvcGDYzxBgo27uQYuaT3xF3");
//        RegisterCourseThread thread=new RegisterCourseThread(cookies,
//                "54524","UGRD","1192","QNS01",
//                "Xm45YTvcGDYzxBgo27uQYuaT3xF3");
//        thread.start();
    }

}
