package net.stevencai;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class User implements Serializable {
    public String major;
    public String career;
    public String college;
    public List<Course> coursesNeedRegister=new LinkedList<>();
    public List<Course> registeredCourses=new LinkedList<>();

    public User(){}
    public User(String college,String major,String career,List<Course> coursesNeedRegister){
        this.college=college;
        this.major=major;
        this.career=career;
        this.coursesNeedRegister=coursesNeedRegister;
    }
    public void addRegisteredCourses(Course course){
        registeredCourses.add(course);
    }
    public void addCourse(Course course){
        coursesNeedRegister.add(course);
    }
}

class Course implements  Serializable{
    public String courseNbr;
    public String time;
    public String sectionNbr;
    public String instructor;
    public boolean hasLab=false;
    public List<Course> labs=new LinkedList<>();
    public String status;
    public String semester;
    Course(){}
    public Course(String courseNbr, String time, String sectionNbr, String instructor, boolean hasLab, String semester, String status) {
        this.courseNbr = courseNbr;
        this.time = time;
        this.sectionNbr = sectionNbr;
        this.instructor = instructor;
        this.hasLab = hasLab;
        this.semester = semester;
        this.status = status;
    }

    public Course(String courseNbr, String time, String sectionNbr, String instructor, String semester, String status) {
        this.courseNbr = courseNbr;
        this.time = time;
        this.sectionNbr = sectionNbr;
        this.instructor = instructor;
        this.semester = semester;
        this.status = status;
    }
}
