package main.java.nexus.validators;

import main.java.nexus.model.Section;
import main.java.nexus.model.Student;

public interface EnrollmentValidator {
    ValidationResult validate(Student s, Section sec);
}
