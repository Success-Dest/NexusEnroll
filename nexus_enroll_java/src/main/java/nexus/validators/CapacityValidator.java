package main.java.nexus.validators;

import main.java.nexus.model.Section;
import main.java.nexus.model.Student;

public class CapacityValidator implements EnrollmentValidator {
    @Override
    public ValidationResult validate(Student s, Section sec) {
        if (sec.isFull())
            return ValidationResult.fail("Section is full");
        return ValidationResult.success();
    }
}
