package net.stevencai;

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;




public class Firebase {
    private static Firestore db;
    static{
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("resources/symbolic-path-223920-firebase-adminsdk-7zqif-b2652eb035.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://symbolic-path-223920.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            db= FirestoreClient.getFirestore();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
    private static String getUserToken(String college,String term,String career,String section) throws ExecutionException, InterruptedException {
        Query query=db.collection("priority1")
                .document(college).collection(term)
                .document(career).collection(section)
                .orderBy("time").limit(1);
        String deviceid= query.get().get().getDocuments().get(0).getId();
        return deviceid;
    }

    public static void searchOpenCoursesAndRegister() throws ExecutionException, InterruptedException {
        List<String> colleges=searchColleges();
        colleges.forEach(college->{
            try {
                searchMajors(college).forEach(major->{
                    try {
                        searchTerms(college,major).forEach(term->{
                            try {
                                searchCareers(college,major,term).forEach(career->{
                                    try {
                                        searchCourses(college,major,term,career).forEach(course->{
                                            try {
                                                searchSections(college,major,term,career,course).forEach(section->{
////
                                                    try {
                                                        if (isSectionOpen(college, major, term, career, course, section))
                                                         {
                                                             String deviceID=getUserToken(college,term,career,section);
                                                             Set<Cookie> cookies=getUserCookie(deviceID);
                                                             RegisterCourseThread thread=new RegisterCourseThread(cookies,section,career,term,college,deviceID);
                                                             thread.start();
                                                        }
                                                    } catch (ExecutionException e) {
                                                        e.printStackTrace();
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    catch(Exception ex){
                                                        //ex.printStackTrace();
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

    public static void updateUserInfo(String section,String deviceID,String message) throws ExecutionException, InterruptedException {
        Map<String,Object> selectedSections= (Map<String, Object>) db.collection("users")
                .document(deviceID)
                .get().get().get("selectedCourses");
        Map<String,Object> map=new HashMap<>();
        //map.put("selectedCourses",selectedSections);

        Map<String,Object> sectionname= (Map<String, Object>) selectedSections.get(section);
        sectionname.put("status",message);
        selectedSections.put(section,sectionname);
        ApiFuture<WriteResult> update=db.collection("users")
                .document(deviceID)
                .set(selectedSections,SetOptions.merge());
    }
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

    }

}
