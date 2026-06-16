package Root.Question2_Stream;

import net.datafaker.Faker;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World :D");


        // Testing With Faker
        // 1. Initialize the Faker instance
        Faker faker = new Faker();
        Random random = new Random();

        // 2. Generate Fake Departments
        Department cs = new Department(faker.university().prefix() + " Science");
        Department math = new Department("Mathematics and " + faker.science().element());
        List<Department> departments = Arrays.asList(cs, math);

        // 3. Generate Fake Courses
        List<Course> poolOfCourses = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            poolOfCourses.add(new Course(
                    faker.educator().course(),                     // Random course title
                    faker.number().numberBetween(2, 6),            // Random credits between 2 and 5
                    faker.number().numberBetween(2022, 2027),      // Random academic year
                    departments.get(random.nextInt(departments.size())) // Assign to a random dept
            ));
        }

        // 4. Generate Fake Students with a random selection of courses
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            // Pick 2 or 3 random courses from our pool for each student
            int coursesToTake = random.nextInt(2) + 2;
            List<Course> studentCourses = new ArrayList<>();
            for (int j = 0; j < coursesToTake; j++) {
                studentCourses.add(poolOfCourses.get(random.nextInt(poolOfCourses.size())));
            }

            students.add(new Student(faker.name().fullName(), studentCourses));
        }

        // --- Print Data Summary to Verify Faker Works ---
        System.out.println("=== GENERATED FAKE DATA ===");
        for (Student s : students) {
            System.out.println("Student: " + s.getName());
            for (Course c : s.getCourses()) {
                System.out.println("  -> Course: " + c.getTitle() + " (" + c.getCredits() + " credits, Year: " + c.getYear() + ") [" + c.getDepartment().getName() + "]");
            }
        }
        System.out.println("===========================\n");

        // --- 5. TEST YOUR STREAM FUNCTIONS WITH FAKE DATA ---

        // Test Method 1
        String targetDept = cs.getName();
        System.out.println("--- Test 1: studentsInDepartment (" + targetDept + ") ---");
        System.out.println(studentsInDepartment(students, targetDept));

        // Test Method 2
        System.out.println("\n--- Test 2: topStudentByCredits ---");
        System.out.println("Top Student: " + topStudentByCredits(students));

        // Test Method 3
        int targetYear = 2024;
        System.out.println("\n--- Test 3: courseTitleByDepartmentFromYear (From " + targetYear + " onwards) ---");
        Map<String, List<String>> courseMap = courseTitleByDepartmentFromYear(students, targetYear);
        courseMap.forEach((dept, courses) -> System.out.println(dept + " -> " + courses));
    }

    public static List<String> studentsInDepartment(List<Student> students, String department) {
        return students.stream().filter(student -> student.getCourses().stream()
                        .anyMatch(course -> course.getDepartment().getName().equalsIgnoreCase(department)))
                .map(Student::getName)
                .distinct()
                .sorted()
                .toList();
    }

    public static String topStudentByCredits(List<Student> students) {
        return students.stream()
                .max(Comparator.comparingInt(student -> student.getCourses()
                        .stream()
                        .mapToInt(Course::getCredits)
                        .sum()))
                .map(Student :: getName).orElse(""); // Can't Just use toString :'( -> Returns Optional<Student> a Wrapper
    }

    public static Map<String, List<String>> courseTitleByDepartmentFromYear(List<Student> students, int fromYear) {
//        Map<String, List<String>> courseTitleByDepartment = new HashMap<>();
//
//        for (Student student : students) {
//            student.getCourses().forEach(course -> {
//                if (course.getYear() >= fromYear) {
//                    String department = course.getDepartment().getName();
//                    String courseTitle = course.getTitle();
//                    if (!courseTitleByDepartment.containsKey(department))
//                    {
//                        courseTitleByDepartment.put(department, new ArrayList<>());
//                    }
//                    List<String> courses = courseTitleByDepartment.get(department);
//                    if (!courses.contains(courseTitle)) {
//                        courses.add(courseTitle);
//                    }
//                }
//            });
//        }
//
//        return courseTitleByDepartment;

        return students.stream()
                .flatMap(student -> student.getCourses().stream())
                .filter(course -> course.getYear() >= fromYear)
                .collect(Collectors.groupingBy(
                        course -> course.getDepartment().getName(),
                        Collectors.mapping(
                                Course::getTitle,
                                Collectors.collectingAndThen(
                                        Collectors.toSet(),
                                        ArrayList::new
                                )
                        )
                ));
    }
}