package edu.cuny.qc.JsoupDemo;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

class Course implements  Serializable{
    public String courseNbr;
    public String time;
    public String sectionNbr;
    public String instructor;
    public boolean hasLab=false;
    public List<Course> labs=new LinkedList<>();
    public String semester;
    public String status;
    
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
