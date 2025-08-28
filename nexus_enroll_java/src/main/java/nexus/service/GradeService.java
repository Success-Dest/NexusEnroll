package main.java.nexus.service;

import java.util.*;

// Basic grade submission flow - demonstrates state transitions
public class GradeService {
    // For PoC: store a simple map of (student,section)->grade
    private final Map<String, String> grades = new HashMap<>();

    public void submitGradesBatch(Map<String, String> batch) {
        // batch: key = studentId+"|"+sectionId ; value = grade
        for (Map.Entry<String, String> e : batch.entrySet()) {
            String key = e.getKey();
            String grade = e.getValue();
            // simple validation
            if (!Arrays.asList("A", "B", "C", "D", "F").contains(grade)) {
                System.out.println("Invalid grade for " + key + ": " + grade + " -> skipping");
                continue; // graceful handling
            }
            grades.put(key, grade);
        }
        System.out.println("Grades processed. Count=" + grades.size());
    }
}
