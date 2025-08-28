package main.java.nexus.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
    public final String courseId;
    public final String name;
    public final List<String> prerequisites = new ArrayList<>();

    public Course(String courseId, String name) {
        this.courseId = courseId;
        this.name = name;
    }
}
