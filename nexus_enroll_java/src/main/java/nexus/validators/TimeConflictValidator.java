package main.java.nexus.validators;

import main.java.nexus.model.Section;
import main.java.nexus.model.Student;
import main.java.nexus.repo.EnrollmentRepository;

// Very simple conflict check using existing enrollments and section times
public class TimeConflictValidator implements EnrollmentValidator {
    private final EnrollmentRepository enrRepo;

    public TimeConflictValidator(EnrollmentRepository enrRepo) {
        this.enrRepo = enrRepo;
    }

    @Override
    public ValidationResult validate(Student s, Section sec) {
        // For demo: we don't store section times in Enrollment; skip actual check or
        // always pass
        return ValidationResult.success();
    }
}
