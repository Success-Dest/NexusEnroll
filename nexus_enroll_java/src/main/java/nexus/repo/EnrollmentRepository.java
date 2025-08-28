package main.java.nexus.repo;

import java.util.List;
import java.util.Optional;

import main.java.nexus.model.Enrollment;

public interface EnrollmentRepository {
    void save(Enrollment e);
    List<Enrollment> findBySection(String sectionId);
    List<Enrollment> findByStudent(String studentId);
    Optional<Enrollment> find(String enrollmentId);
}
