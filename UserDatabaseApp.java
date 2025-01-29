import java.sql.*;
import java.util.*;

public class UserDatabaseApp {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/questiondb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "radhaswamiji";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Register Student\n2. Login\n3. Enroll in Course\n4. View Enrolled Courses\n5. Search Course\n6. Add Course (Admin)\n7. List Students in a Course\n8. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    registerStudent(scanner);
                    break;
                case 2:
                    loginStudent(scanner);
                    break;
                case 3:
                    enrollInCourse(scanner);
                    break;
                case 4:
                    viewEnrolledCourses(scanner);
                    break;
                case 5:
                    searchCourse(scanner);
                    break;
                case 6:
                    addCourse(scanner);
                    break;
                case 7:
                    listStudentsInCourse(scanner);
                    break;
                case 8:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void registerStudent(Scanner scanner) {
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO students (name, email) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();
            System.out.println("Student registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loginStudent(Scanner scanner) {
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM students WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Login successful. Welcome " + rs.getString("name") + "!");
            } else {
                System.out.println("Student not found. Register first.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listCourses() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT id, course_name, instructor, capacity FROM courses";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Available Courses:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("course_name") +
                        ", Instructor: " + rs.getString("instructor") +
                        ", Capacity: " + rs.getInt("capacity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void enrollInCourse(Scanner scanner) {
        listCourses();  // Show available courses before enrolling

        System.out.print("Enter Student Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Course ID: ");
        int courseId = scanner.nextInt();
        scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String checkCourseSql = "SELECT id FROM courses WHERE id = ?";
            PreparedStatement checkCourseStmt = conn.prepareStatement(checkCourseSql);
            checkCourseStmt.setInt(1, courseId);
            ResultSet courseRs = checkCourseStmt.executeQuery();

            if (!courseRs.next()) {
                System.out.println("Error: Course ID " + courseId + " does not exist. Please enter a valid course ID.");
                return;
            }

            String sql = "INSERT INTO enrollments (student_email, course_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
            System.out.println("Enrolled successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private static void viewEnrolledCourses(Scanner scanner) {
        System.out.print("Enter Student Email: ");
        String email = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT c.course_name FROM courses c JOIN enrollments e ON c.id = e.course_id WHERE e.student_email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("course_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addCourse(Scanner scanner) {
        System.out.print("Enter Course Name: ");
        String courseName = scanner.nextLine();
        System.out.print("Enter Instructor Name: ");
        String instructor = scanner.nextLine();
        System.out.print("Enter Capacity: ");
        int capacity = scanner.nextInt();
        scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO courses (course_name, instructor, capacity) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseName);
            stmt.setString(2, instructor);
            stmt.setInt(3, capacity);
            stmt.executeUpdate();
            System.out.println("Course added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void searchCourse(Scanner scanner) {
        System.out.print("Enter Course Name or ID: ");
        String input = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM courses WHERE course_name ILIKE ? OR id::TEXT = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + input + "%");
            stmt.setString(2, input);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("course_name") + ", Instructor: " + rs.getString("instructor") + ", Capacity: " + rs.getInt("capacity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void listStudentsInCourse(Scanner scanner) {
        System.out.print("Enter Course ID: ");
        int courseId = scanner.nextInt();
        scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT s.name, s.email FROM students s " +
                    "JOIN enrollments e ON s.email = e.student_email " +
                    "WHERE e.course_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, courseId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Students enrolled in the course:");
            while (rs.next()) {
                System.out.println("Name: " + rs.getString("name") + ", Email: " + rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}