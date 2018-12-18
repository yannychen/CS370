package edu.cuny.qc.JsoupDemo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchCourses {
	
	public static final String USER_AGENT= "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:54.0) Gecko/20100101 Firefox/54.0";
	public static final String url="https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/GUEST/HRMS/c/COMMUNITY_ACCESS.CLASS_SEARCH.GBL";

	public static String[] collegeID;
	public static String[] collegeName;
	public static Map<String, String> termInfo;
	public static Map<String, String> majorInfo;
	public static Map<String, String> careerLevel;
	public static Connection.Response res;
	public static Document doc;
	public static DataAccess db = new DataAccess();

	
	public SearchCourses() {
		collegeID = new String[28];
		collegeName = new String[28];
		termInfo = new HashMap<String, String>();
		majorInfo = new HashMap<String, String>();
		careerLevel = new HashMap<String, String>();
	}
		
	public static void startSearch() throws IOException, InterruptedException, ExecutionException {
		res = Jsoup.connect(url).userAgent(USER_AGENT).method(Connection.Method.GET).execute();
		Document homePage = Jsoup.parse(res.body());
		
		Elements schoolDiv = homePage.select("select[id^=CLASS_SRCH_WRK2_INSTITUTION]");
		
		Elements schoolOptions= (schoolDiv.get(0).children());
		int i=0;
		for(Element schoolName : schoolOptions) {
			collegeID[i]=schoolOptions.get(i).attr("value");	
			collegeName[i]= schoolName.text();
			i++;
		}
		db.connectDB();
		db.addCollegeInfo(collegeID, collegeName);

		Elements e = homePage.select("#win0divPSHIDDENFIELDS");
				
		Connection conn = Jsoup.connect(url)
				.ignoreContentType(true)
				.cookies(res.cookies())
				.userAgent(USER_AGENT);
					
		e.first().children().forEach((ele) -> {
			if (ele.id().equals("ICAction")) {
				return;
			}
			conn.data(ele.id(), ele.val());
		});
				
		
		Connection.Response resh = conn.data("ICAction", "CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH")
				.data("CLASS_SRCH_WRK2_INSTITUTION$31$", "QNS01")	
		        .method(Connection.Method.POST)
		        .execute();							
				 
		doc = Jsoup.parseBodyFragment(resh.body());
	
		Elements container= doc.getAllElements().select("#win0divPAGECONTAINER");
		
		Document innerDoc=Jsoup.parseBodyFragment(container.html());
			
		Elements major= innerDoc.select("div[id^=win0divSSR_CLSRCH_WRK_SUBJECT_SRCH] option");

		for(Element majors : major) {
			if(!majors.val().equals("")) 
				majorInfo.put(majors.val(), majors.text());
		}
		
		db.addMajorInfo(majorInfo);

		
		Elements terms= innerDoc.select("div[id^=win0divCLASS_SRCH_WRK2_STRM] option");
		for(Element termInformation: terms) {
			if(!termInformation.val().equals("")) {
				termInfo.put(termInformation.val(), termInformation.text());
			}		
		}
		db.addTermInfo(termInfo);

		
		Elements careerLev= innerDoc.getAllElements().select("div[id^=win0divSSR_CLSRCH_WRK_ACAD_CAREE] option");
		
		for(Element careerL: careerLev) {
			if(!careerL.val().equals("")) {
				careerLevel.put(careerL.val(), careerL.text());	
			}		
		}
		db.addCareerLevel(careerLevel, termInfo);
		

			for(String majorKey: majorInfo.keySet()) {
				for(String termKey: termInfo.keySet()) {
					for(String careerKey: careerLevel.keySet()) {
						System.out.println(majorKey+" "+ termKey+ " " +careerKey);
						System.out.println("------------");
						Thread thread= new Thread(()->{
							try {
								updateMajorCourses("QNS01", majorKey, termKey, careerKey);
							} catch (IOException | InterruptedException | ExecutionException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						});
						thread.start();
						Thread.sleep(100);			
					}
				}
			}
	}
	
	private static void updateMajorCourses(String college, String major, String term, String careerlevel) throws IOException, InterruptedException, ExecutionException {
		res = Jsoup.connect(url).userAgent(USER_AGENT).method(Connection.Method.GET).execute();
		
		doc = Jsoup.parse(res.body());

		Element elem = doc.selectFirst("#win0divPSHIDDENFIELDS");
		
		Connection conn = Jsoup.connect(url)
				.ignoreContentType(true)
				.cookies(res.cookies())
				.userAgent(USER_AGENT);	
		
		elem.children().forEach((ele) -> {
			if (ele.id().equals("ICAction")) {
				return;
			}
			conn.data(ele.id(), ele.val());
		});	
				
		Connection.Response resh = conn.data("ICAction", "CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH")
				.data("CLASS_SRCH_WRK2_INSTITUTION$31$", college)
				.data("SSR_CLSRCH_WRK_SUBJECT_SRCH$0", major)
				.data("CLASS_SRCH_WRK2_STRM$35$", term)
				.data("SSR_CLSRCH_WRK_ACAD_CAREER$2", careerlevel)
				.data("SSR_CLSRCH_WRK_SSR_OPEN_ONLY$chk$5", "N")
		        .method(Connection.Method.POST)
		        .execute();
		
		if(!resh.body().contains("title=\"Click to sort ascending\">Instructor</a>"))
			return;
		
		doc = Jsoup.parseBodyFragment(resh.body());
						
		Elements elements=doc.getAllElements().select("#win0divPAGECONTAINER");
	
		Document innerDoc =Jsoup.parseBodyFragment(elements.html());	
				
		//Keys: coursenum -> sectionNum e.g. 47051 -> sectionInfoKey e.g. timeDay
		HashMap<String, HashMap<String, HashMap<String, String>>> courseMap = new HashMap<>();

		Elements courses = innerDoc.select("div[id^=win0divSSR_CLSRSLT_WRK_GROUPBOX2]");

		courses.forEach(course -> {
			String courseNum = course.selectFirst("div[id^=win0divSSR_CLSRSLT_WRK_GROUPBOX2GP]").text();
			
			Elements sections = course.select("tr[onmouseover]");
			
			if (sections.isEmpty()) {
				return;
			}
			
			HashMap<String, HashMap<String, String>> sectionsMap = new HashMap<>();
						
			sections.forEach(section -> {
				HashMap<String, String> sectionInfoMap = new HashMap<>();

				Elements sectionInfos = section.select("td");
				if(sectionInfos.size()<7)
					return;
				
				sectionInfoMap.put("time", sectionInfos.get(2).text());
				sectionInfoMap.put("instructor", sectionInfos.get(4).text());
				sectionInfoMap.put("status", sectionInfos.get(6).selectFirst("img").attr("alt"));				
				sectionsMap.put(sectionInfos.get(0).text(), sectionInfoMap);
			});
						
			courseMap.put(courseNum, sectionsMap);
		});
		
		
		db.addCourses(college, major, term, careerlevel, courseMap);
		System.out.println("DB ADD");
		
	}
	
}


