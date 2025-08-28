package main.java.nexus.repo;

import java.util.Optional;

import main.java.nexus.model.Student;

public interface StudentRepository {
    Optional<Student> findById(String id);
    void save(Student s);
}
