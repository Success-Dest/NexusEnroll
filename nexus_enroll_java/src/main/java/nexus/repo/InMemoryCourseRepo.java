package main.java.nexus.repo;

import java.util.*;
import java.util.stream.Collectors;

import main.java.nexus.model.Course;
import main.java.nexus.model.Section;

public class InMemoryCourseRepo implements CourseRepository {
    private final Map<String, Course> courses = new HashMap<>();
    private final Map<String, Section> sections = new HashMap<>();

    public Optional<Course> findCourse(String id) { return Optional.ofNullable(courses.get(id)); }
    public Optional<Section> findSection(String sectionId) { return Optional.ofNullable(sections.get(sectionId)); }
    public void saveCourse(Course c) { courses.put(c.courseId, c); }
    public void saveSection(Section s) { sections.put(s.sectionId, s); }
    public List<Section> listSectionsForCourse(String courseId) {
        return sections.values().stream().filter(s -> s.courseId.equals(courseId)).collect(Collectors.toList());
    }
}
