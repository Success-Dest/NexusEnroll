package main.java.nexus.model;

import java.util.HashSet;
import java.util.Set;

public class Student extends User {
    private final Set<String> completedCourses = new HashSet<>();

    public Student(String id, String name, String email) {
        super(id, name, email);
    }

    public void addCompletedCourse(String courseId) {
        completedCourses.add(courseId);
    }

    public boolean hasCompleted(String courseId) {
        return completedCourses.contains(courseId);
    }
}
