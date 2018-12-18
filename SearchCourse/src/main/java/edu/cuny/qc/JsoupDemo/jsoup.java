package edu.cuny.qc.JsoupDemo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;


public class jsoup {
	
//	public static final String USER_AGENT= "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:54.0) Gecko/20100101 Firefox/54.0";
	
	public static void main(String args[]) throws UnsupportedEncodingException, IOException, InterruptedException, ExecutionException {
		SearchCourses search= new SearchCourses();
		while(true)
			search.startSearch();		
	}
}
