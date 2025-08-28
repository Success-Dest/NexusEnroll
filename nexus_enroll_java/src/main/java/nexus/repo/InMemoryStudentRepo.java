package main.java.nexus.repo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import main.java.nexus.model.Student;

public class InMemoryStudentRepo implements StudentRepository {
    private final Map<String, Student> map = new HashMap<>();
    public Optional<Student> findById(String id) { return Optional.ofNullable(map.get(id)); }
    public void save(Student s) { map.put(s.getId(), s); }
}
