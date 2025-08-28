package main.java.nexus.validators;

import main.java.nexus.model.Course;
import main.java.nexus.model.Section;
import main.java.nexus.model.Student;
import main.java.nexus.repo.CourseRepository;

public class PrerequisiteValidator implements EnrollmentValidator {
    private final CourseRepository courseRepo;

    public PrerequisiteValidator(CourseRepository repo) {
        this.courseRepo = repo;
    }

    @Override
    public ValidationResult validate(Student s, Section sec) {
        Course c = courseRepo.findCourse(sec.courseId).orElse(null);
        if (c == null)
            return ValidationResult.fail("Course not found");
        for (String pre : c.prerequisites) {
            if (!s.hasCompleted(pre))
                return ValidationResult.fail("Missing prerequisite: " + pre);
        }
        return ValidationResult.success();
    }
}
