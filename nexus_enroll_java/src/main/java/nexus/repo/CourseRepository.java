package main.java.nexus.repo;

import java.util.List;
import java.util.Optional;

import main.java.nexus.model.Course;
import main.java.nexus.model.Section;

public interface CourseRepository {
    Optional<Course> findCourse(String id);
    Optional<Section> findSection(String sectionId);
    void saveCourse(Course c);
    void saveSection(Section s);
    List<Section> listSectionsForCourse(String courseId);
}
