package net.stevencai;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class RegisterCourseThread extends Thread
{
    static boolean hasSpots=true;
    private String EMPLID="";
    private String CAREER="UGRD";
    private String INSTITUTION="QNS01";
    private String STRM="1192";
    private String courseNbr;
    private boolean valid=true;
    private String deviceID;
    private static final String CUNYFIRSTLOGIN="https://home.cunyfirst.cuny.edu";

    private static final String baseURL="https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES_2.SSR_SSENRL_CART.GBL?";

    private String CARTURL=baseURL +
        "Page=SSR_SSENRL_CART&Action=A&ACAD_CAREER="+CAREER
                +"&EMPLID="+EMPLID+
        "&INSTITUTION="+INSTITUTION
                +"&STRM="+STRM
                +"&TargetFrameName=None"; ;
    private Set<Cookie> cookies;
    /**
     * print pages
     * @param page page
     * @param filename destination file.
     */
    private  void printPage(Page page,String filename){
        try(PrintWriter writer=new PrintWriter(filename); ) {
            if(page instanceof  HtmlPage)
                writer.write(((HtmlPage)page).asXml());
            else if(page instanceof  XmlPage)
                writer.write(((XmlPage)page).asXml());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * init webclient
     * @return webclient
     */
    public  WebClient initClient(){
        WebClient client=new WebClient(BrowserVersion.FIREFOX_52);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setUseInsecureSSL(true);
        client.getOptions().setRedirectEnabled(true);
        client.getOptions().setJavaScriptEnabled(true);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setTimeout(10000);
        client.getOptions().setActiveXNative(false);
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.getOptions().setActiveXNative(false);
        client.setAjaxController(new NicelyResynchronizingAjaxController());
        client.getCookieManager().setCookiesEnabled(true);
        return client;
    }

//    /**
//     * login to cunyfirst
//     * @param client client
//     * @return login page
//     * @throws IOException throw IOException.
//     */
//    public Page login(WebClient client) throws IOException {
//        HtmlPage loginPage=client.getPage(CUNYFIRSTLOGIN);
//        client.waitForBackgroundJavaScript(1000);
//        final HtmlForm form = loginPage.getFormByName("loginform");
//
//
//        final HtmlTextInput usernameH = form.getInputByName("usernameH");
//        usernameH.type(USERNAME+"@login.cuny.edu");
//
//        final HtmlPasswordInput password = form.getInputByName("password");
//        password.type(PASSWORD);
//        HtmlButton button =(HtmlButton) loginPage.getElementById("submit");
//
//
//        if(button.isDisabled())
//            button.removeAttribute("disabled");
//        HtmlPage homePage=button.click();
//
//        storeCookies(client.getCookieManager().getCookies());
//
//        return homePage;
//    }

    /**
     * common parameters
     * @param list store in a list.
     */
    private void setCommonPara(List<NameValuePair> list){
        //hard code parts of the required parameters.
        list.add(new NameValuePair("ICAJAX","1"));
        list.add(new NameValuePair("ICNAVTYPEDROPDOWN","1"));
        list.add(new NameValuePair("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$7$","9999"));
        list.add(new NameValuePair("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$8$","9999"));
        list.add(new NameValuePair("ptus_defaultlocalnode","PSFT_CNYHCPRD"));
        list.add(new NameValuePair("ptus_dbname","CNYHCPRD"));
        list.add(new NameValuePair("ptus_portal","EMPLOYEE"));
        list.add(new NameValuePair("ptus_node","HRMS"));
        list.add(new NameValuePair("ptus_workcenterid",""));
        list.add(new NameValuePair("ptus_componenturl","https://hrsa.cunyfirst.cuny.edu/psp/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES_2.SSR_SSENRL_CART.GBL"));
    }

    /**
     * get the course number from course string. e.g. "CSCI 90-35(54582)"
     * @param course course string
     * @return the course number.
     */
    private String getCourseNbr(String course){
        int begin=course.indexOf("(");
        return course.substring(begin+1,course.length()-1);
    }
    /**
     * list all the selected courses.
     * @param page webpage
     */
    private int getSelectedCourses(HtmlPage page,String courseNbr){
        int index=0;
        DomElement element=null;
        while((element=page.getElementById("P_CLASS_NAME$span$"+index))!=null) {
            if(courseNbr.equals(getCourseNbr(element.asText()))){
                return index;
            }
            ++index;
        }
        return -1;
    }

    /**
     * check if the course already been registered.
     * @param page web page
     * @param courseNbr course number
     * @return true if already been registered, otherwise return false
     */
    private boolean isCoursesRegistered(HtmlPage page,String courseNbr){
        //TODO check if the course is already registered.
        int index=0;
        DomElement element=null;
        while((element=page.getElementById("E_CLASS_NAME$"+index))!=null){
            if(courseNbr.equals(getCourseNbr(element.asText())))
                return true;
            ++index;
        }
        return false;
    }
    /**
     * check if shopping car is empty.
     * @param page webpage
     * @return true if empty, otherwise return false;
     */
    private boolean cartEmpty(Page page){
        Element element=null;
        if(page instanceof HtmlPage){
            element=Jsoup.parse(((HtmlPage)page).asXml()).getElementById("win0divP_NO_CLASSES$0");
        }
        else if(page instanceof  XmlPage){
            element=Jsoup.parse(((XmlPage)page).asXml()).getElementById("win0divP_NO_CLASSES$0");
        }
        if(element==null) return false;
        return true;
    }

    /**
     * find hidden values in XML page.
     * @param page xml page
     * @return list
     */
    private List<NameValuePair> findHiddenValues(XmlPage page){
        String content=page.asXml();
        int begin=content.indexOf("win0divPSHIDDENFIELDS");
        int end=content.indexOf("</FIELD>",begin);
        Document doc=Jsoup.parse(content.substring(begin+32,end));
        Elements inputs=doc.select("input");
        List<NameValuePair> list=new LinkedList<>();
        addToList(list,inputs);
        return list;
    }
    /**
     *
     * @param client weclient
     * @return web page.
     * @throws IOException exception that might be thrown.
     */
    public boolean addToCart(WebClient client,String courseNbr) throws IOException {
        //get the shopping  cart page.
        HtmlPage homePage=client.getPage(CARTURL);


        //check if the course need a lab.
        String[] courses;
        boolean hasLab=false;
        if(courseNbr.contains("&")) {
            courses = courseNbr.split("&");
            hasLab=true;
        }
        else courses=new String[]{courseNbr};

        //check if the course has already been registered.
        if(isCoursesRegistered(homePage,courses[0])) {
            return false;
        }
        //check if the course is in the shopping cart.
        int courseIndex=getSelectedCourses(homePage,courses[0]);
        if(courseIndex!=-1) return false;

        //go to the next page.
        URL url=new URL(baseURL);


        WebRequest firstReq=new WebRequest(url);
        firstReq.setHttpMethod(HttpMethod.POST);
        List<NameValuePair> list=getHiddenInput(homePage);

        list.add(new NameValuePair("ICAction","DERIVED_REGFRM1_SSR_PB_ADDTOLIST2$9$"));
        list.add(new NameValuePair("DERIVED_REGFRM1_CLASS_NBR",courses[0]));
        //list.add(new NameValuePair("P_SELECT$chk$0","N"));
        list.add(new NameValuePair("DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$","06"));

//        for (NameValuePair nameValuePair : list) {
//            System.out.println(nameValuePair);
//        }

        firstReq.setRequestParameters(list);
        XmlPage page=client.getPage(firstReq);

        //printPage(page,"shopping.html");

        WebRequest secondReq=new WebRequest(url);
        List<NameValuePair> list2;
        if(hasLab) {

            String index="";
            String content=page.asText();
            int end=content.indexOf("'>"+courses[1]);

            content=content.substring(0,end);
            int begin=content.lastIndexOf("$");
            index=content.substring(begin+1);

            List<NameValuePair> secondList=findHiddenValues(page);
            secondList.add(new NameValuePair("ICAction","DERIVED_CLS_DTL_NEXT_PB"));
            secondList.add(new NameValuePair("SSR_CLS_TBL_R1$sels$"+index+"$$0",index));

            WebRequest req=new WebRequest(url);

            req.setRequestParameters(secondList);
            req.setHttpMethod(HttpMethod.POST);
            homePage=client.getPage(req);

            list2=getHiddenInput(homePage);
        }
        //get the second page.
        else
            list2=getHiddenInput(page);

        list2.add(new NameValuePair("ICAction","DERIVED_CLS_DTL_NEXT_PB$280$"));
        list2.add(new NameValuePair("DERIVED_CLS_DTL_WAIT_LIST_OKAY$125$$chk","N"));
        list2.add(new NameValuePair("DERIVED_CLS_DTL_CLASS_PRMSN_NBR$118$",""));
        list2.add(new NameValuePair("DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$","06"));
        secondReq.setRequestParameters(list2);

        page=client.getPage(secondReq);
       // System.out.println("in the cart++++++++++++++++++++");
       // printPage(page,"inshopping.html");
        if(!page.asXml().contains(courses[0])) return false;
        return true;
    }
    private void addToList(List<NameValuePair> list,Elements elements){
        for(Element e:elements) {
            if(e.attr("name").equals("ICAction")) continue;
            list.add(new NameValuePair(e.attr("name"),e.attr("value")));
        }
    }
    /**
     * get hidden values from the hidden input
     * @param page web page.
     * @return list of hidden values.
     */
    private List<NameValuePair> getHiddenInput(Page page){
        List<NameValuePair> list=new ArrayList<>();
        setCommonPara(list);
        Elements elements=null;
        if(page instanceof  XmlPage) {
            elements = Jsoup.parse(((XmlPage) page).asXml()).getElementById("win0divPSHIDDENFIELDS").children();
        }
        else if(page instanceof  HtmlPage) {
            elements = Jsoup.parse(((HtmlPage) page).asXml()).getElementById("win0divPSHIDDENFIELDS").children();
        }

        addToList(list,elements);
        return list;
    }

    /**
     * validate the course ,check if user is able to register this course.
     * @param client
     * @param courseNbr
     * @return
     * @throws IOException
     */
    private boolean validateCourse(WebClient client, String courseNbr) throws IOException {
        HtmlPage homePage=client.getPage(CARTURL);

        //check if the course need a lab.
        String[] courses;
        boolean hasLab=false;
        if(courseNbr.contains("&")) {
            courses = courseNbr.split("&");
            hasLab=true;
        }
        else courses=new String[]{courseNbr};

        int courseIndex=getSelectedCourses(homePage,courses[0]);
        //go to the next page.
        URL url=new URL(baseURL);

        WebRequest req=new WebRequest(url);
        req.setHttpMethod(HttpMethod.POST);
        List<NameValuePair> list=getHiddenInput(homePage);
        list.add(new NameValuePair("ICAction","DERIVED_REGFRM1_SSR_VIEW_STAT_RPT"));
        list.add(new NameValuePair("DERIVED_REGFRM1_CLASS_NBR",""));
        list.add(new NameValuePair("P_SELECT$chk$"+courseIndex,"Y"));
        list.add(new NameValuePair("P_SELECT$"+courseIndex,"Y"));
        list.add(new NameValuePair("DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$","06"));
        WebRequest request3=new WebRequest(url);
        request3.setRequestParameters(list);
        request3.setHttpMethod(HttpMethod.POST);
        XmlPage resultPage=client.getPage(request3);
        printPage(resultPage,"result.html");
        String content=resultPage.asText();
        String firstStr="id=\'win0divDERIVED_REGFRM1_SSR_STATUS_LONG$0\'";
        int begin=content.indexOf(firstStr)+firstStr.length();
        content=content.substring(begin);
        firstStr="alt=\"";
        begin=content.indexOf(firstStr)+firstStr.length();
        int end=content.indexOf("\" class=\"SSSIMAGECENTER\"");

        if(content.substring(begin,end).equals("Error")) {
            valid=false;
            return false;
        }
        return true;
    }

    /**
     *
     * @param client webclient
     * @return web page.
     * @throws IOException exception that might be thrown.
     */
    private  boolean enroll(WebClient client) throws IOException {
        URL url=new URL("https://hrsa.cunyfirst.cuny.edu/psc/cnyhcprd/EMPLOYEE/HRMS/c/SA_LEARNER_SERVICES_2.SSR_SSENRL_CART.GBL");
        //get the shopping cart page.
        HtmlPage homePage=client.getPage(CARTURL);

        //if this course need lab. the coureNbr contains 2 course numbers.
        String []courses=null;
        if(courseNbr.contains("&")) {
            courses = courseNbr.split("&");
        }
        else{
            courses=new String[]{courseNbr};
        }

        //check if the course is in the shopping cart.
        //if not in the cart, put it into shopping cart first.
        int courseIndex=getSelectedCourses(homePage,courses[0]);
        if(courseIndex==-1) {
            if(!addToCart(client,courseNbr))
                return false;
            homePage=client.getPage(CARTURL);
            courseIndex=getSelectedCourses(homePage,courses[0]);
        }
     //   System.out.println("--------------------");
        if(!validateCourse(client,courseNbr))
            return false;
        //get the first page to enroll which is a xmlpage.
        //use this xml page to find the request id.

        homePage=client.getPage(CARTURL);
        List<NameValuePair> list3=getHiddenInput(homePage);
        list3.add(new NameValuePair("ICAction","DERIVED_REGFRM1_LINK_ADD_ENRL"));
        list3.add(new NameValuePair("DERIVED_REGFRM1_CLASS_NBR",""));
        list3.add(new NameValuePair("P_SELECT$chk$"+courseIndex,"Y"));
        list3.add(new NameValuePair("P_SELECT$"+courseIndex,"Y"));
        list3.add(new NameValuePair("DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$","06"));
        // list3.add(new NameValuePair("P_SELECT$chk$1","N"));

        WebRequest request3=new WebRequest(url);
        request3.setRequestParameters(list3);
        request3.setHttpMethod(HttpMethod.POST);
        XmlPage p=client.getPage(request3);

        //find the request id which is stored in the confirmURL.
        //use this URL to send GET request to get the next page.
        String confirmURL=p.asText();
        int begin=confirmURL.indexOf("document.location='");
        int end=confirmURL.indexOf("';", begin);
        confirmURL=confirmURL.substring(begin+19,end);
        homePage=client.getPage(confirmURL);


        //the last step to enroll the course.
        WebRequest request4=new WebRequest(url);
        List<NameValuePair> list4=getHiddenInput(homePage);
        setCommonPara(list4);
        list4.add(new NameValuePair("ICAction","DERIVED_REGFRM1_SSR_PB_SUBMIT"));
        list4.add(new NameValuePair("DERIVED_REGFRM1_SSR_CLS_SRCH_TYPE$249$","06"));

        request4.setRequestParameters(list4);
        XmlPage resultPage=client.getPage(request4);

        String content=resultPage.asText();
        String firstStr="<img src=\"/cs/cnyhcprd/cache850/PS_CS_STATUS_SUCCESS_ICN_1.gif\" width=\"16\" height=\"16\" alt=\"";
        begin=content.indexOf(firstStr)+firstStr.length();
        end=content.indexOf("\" class=\"SSSIMAGECENTER\"");
        printPage(resultPage,"after.html");
        if(content.substring(begin,end).equals("Success"))
            return true;
        else
            hasSpots=false;
       // System.out.println("111111111111111111111111111");
        return false;
    }

    /**
     *
     * @param cookies cookies set.
     * @throws IOException exception that might be thrown.
     */
    private void storeCookies(Set<Cookie> cookies) throws IOException {
        Set<Cookie> newCookies=new HashSet<>();
        cookies.forEach(c->{
            Date date= new Date(System.currentTimeMillis()+1000*60*60*24*10);
            Cookie cookie=new Cookie(c.getDomain(),c.getName(),c.getValue(),c.getPath(),date,c.isSecure());
            newCookies.add(cookie);
        });
        ObjectOutputStream output=new ObjectOutputStream(new FileOutputStream("cookies.bin"));
        output.writeObject(cookies);
        output.close();
    }

    /**
     *
     * @return cookies set.
     * @throws IOException exception that might be thrown
     * @throws ClassNotFoundException exception that might be thrown
     */
    private Set<Cookie> readCookies() throws IOException, ClassNotFoundException {
        ObjectInputStream input=new ObjectInputStream(new FileInputStream("newCookies.bin"));
        Set<Cookie> cookies=(Set<Cookie>) input.readObject();
        input.close();
        return cookies;
    }

    private void setCookies(WebClient client) throws IOException, ClassNotFoundException {
        Iterator<Cookie> itr = cookies.iterator();
        while (itr.hasNext()) {
            client.getCookieManager().addCookie(itr.next());
        }
    }
//    public static void main( String[] args ) throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
//        RegisterCourseThread thread=new RegisterCourseThread();
//        thread.deviceID="ak2fqFQyYsWwjIBm9pBzqMqT92T2";
//
//        thread.cookies=Firebase.getUserCookie("ak2fqFQyYsWwjIBm9pBzqMqT92T2");
//
//        thread.INSTITUTION="QNS01";
//        thread.STRM="1192";
//        thread.CAREER="UGRD";
//        thread.courseNbr="54524";
//        //WebClient client=thread.initClient();
//        //thread.login(client);
//        //thread.enroll(client);
//        thread.run();
//    }

    public RegisterCourseThread(){}
    public RegisterCourseThread(Set<Cookie> cookies,String section,
                                String career,
                                String term, String college,String deviceID){
        this.cookies=cookies;
        this.CAREER=career;
        this.STRM=term;
        this.INSTITUTION=college;
        this.courseNbr =section;
        this.deviceID=deviceID;
        this.CARTURL=baseURL +
                "Page=SSR_SSENRL_CART&Action=A&ACAD_CAREER="+CAREER
                +"&EMPLID="+EMPLID+
                "&INSTITUTION="+INSTITUTION
                +"&STRM="+STRM
                +"&TargetFrameName=None";
    }
    public void run(){
        WebClient client=initClient();
        final List collectedAlerts = new ArrayList();
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
        try {
            setCookies(client);
            if(enroll(client)){
                Firebase.deleteUserFromQueue(INSTITUTION,STRM,CAREER,courseNbr,deviceID);
                Firebase.updateUserInfo(courseNbr,deviceID,"Registered","Successful","Successfully registered the section "+courseNbr);
               // System.out.println("&&&&&&&&&&&&&&&&&&");
            }
            else{
                if(!valid){
                    Firebase.deleteUserFromQueue(INSTITUTION,STRM,CAREER,courseNbr,deviceID);
                    Firebase.updateUserInfo(courseNbr,deviceID,"Unable to register","Error","You cannot register the section "+courseNbr);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        finally{
            client.close();
        }

    }
}
