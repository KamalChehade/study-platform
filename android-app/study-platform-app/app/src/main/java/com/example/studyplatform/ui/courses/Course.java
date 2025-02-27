package com.example.studyplatform.ui.courses;

public class Course {
    private int courseID;
    private String courseName;
    private int teacherID;

    // Constructor
    public Course(int courseID, String courseName, int teacherID) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.teacherID = teacherID;
    }

    // Getters
    public int getCourseID() {
        return courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getTeacherID() {
        return teacherID;
    }
}
