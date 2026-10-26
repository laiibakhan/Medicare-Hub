package hospital.model;

/**
 * Abstract parent class representing a generic person in the hospital.
 * Provides common attributes: id, name, age, gender.
 * Demonstrates INHERITANCE (parent of Patient and Doctor).
 * All logic in this class is my own original work.
 */
public abstract class Person {

    // Encapsulated fields - private, accessed through getters/setters
    private String id;
    private String name;
    private int age;
    private String gender;

    // Constructor to initialize common fields
    public Person(String id, String name, int age, String gender) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    // Getters and setters (ENCAPSULATION)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    /**
     * Abstract method - each subclass must provide its own role.
     * Demonstrates POLYMORPHISM.
     */
    public abstract String getRole();

    /**
     * Returns a CSV line representation for file storage.
     * Format: role,id,name,age,gender,...
     */
    public String toCSV() {
        return getRole() + "," + id + "," + name + "," + age + "," + gender;
    }

    // toString used for display in GUI tables
    @Override
    public String toString() {
        return id + " - " + name;
    }
}