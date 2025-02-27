package com.example.studyplatform.ui.study;
public class StudyGroup {
    private String courseName;
    private String description;
    private String groupID;
    private String teacherID;

    public StudyGroup(String courseName, String description, String groupID, String teacherID) {
        this.courseName = courseName;
        this.description = description;
        this.groupID = groupID;
        this.teacherID = teacherID;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getTeacherID() {
        return teacherID;
    }
}