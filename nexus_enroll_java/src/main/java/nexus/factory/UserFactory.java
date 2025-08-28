package main.java.nexus.factory;

import main.java.nexus.model.*;

// Factory Method pattern for user creation
public class UserFactory {
    public static User create(String type, String id, String name, String email) {
        switch (type.toLowerCase()) {
            case "student":
                return new Student(id, name, email);
            case "faculty":
                return new Faculty(id, name, email);
            default:
                return null;
        }
    }
}
