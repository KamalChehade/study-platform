package com.example.studyplatform.ui.StudyGroupDetails;


public class Member {
    private String name;
    private String role;
    private String universityID;

    public Member(String name, String role, String universityID) {
        this.name = name;
        this.role = role;
        this.universityID = universityID;
    }

    public String getName() {
        return name;
    }


}

