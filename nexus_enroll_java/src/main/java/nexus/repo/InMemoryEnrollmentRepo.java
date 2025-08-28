package main.java.nexus.repo;

import java.util.*;
import java.util.stream.Collectors;

import main.java.nexus.model.Enrollment;

public class InMemoryEnrollmentRepo implements EnrollmentRepository {
    private final Map<String, Enrollment> map = new HashMap<>();
    public void save(Enrollment e) { map.put(e.enrollmentId, e); }
    public List<Enrollment> findBySection(String sectionId) {
        return map.values().stream().filter(e -> e.sectionId.equals(sectionId)).collect(Collectors.toList());
    }
    public List<Enrollment> findByStudent(String studentId) {
        return map.values().stream().filter(e -> e.studentId.equals(studentId)).collect(Collectors.toList());
    }
    public Optional<Enrollment> find(String enrollmentId) { return Optional.ofNullable(map.get(enrollmentId)); }
}
