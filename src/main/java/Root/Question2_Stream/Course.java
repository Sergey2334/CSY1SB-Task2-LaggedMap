package Root.Question2_Stream;

public class Course {
    private String title;
    private int credits;
    private int year;
    private Department department;

    // Testing Faker
    public Course(String title, int credits, int year, Department department) {
        this.title = title;
        this.credits = credits;
        this.year = year;
        this.department = department;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return this.credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Department getDepartment() {
        return this.department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}