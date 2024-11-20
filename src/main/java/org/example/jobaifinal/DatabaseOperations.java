package org.example.jobaifinal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/jobai";
    private static final String USER = "root";
    private static final String PASS = "pavan";

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("Connected to the database.");

                // Create users table
                String sql = "CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "username VARCHAR(50) NOT NULL UNIQUE," +
                        "password VARCHAR(255) NOT NULL)";
                Statement stmt = conn.createStatement();
                stmt.execute(sql);

                // Create resume_data table
                sql = "CREATE TABLE IF NOT EXISTS resume_data (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT," +
                        "keywords TEXT," +
                        "experience INT," +
                        "location VARCHAR(100)," +
                        "FOREIGN KEY(user_id) REFERENCES users(id))";
                stmt.execute(sql);

                // Create user_details table
                sql = "CREATE TABLE IF NOT EXISTS user_details (" +
                        "user_id INT PRIMARY KEY," +
                        "resume_path VARCHAR(255)," +
                        "location VARCHAR(100)," +
                        "experience INT," +
                        "FOREIGN KEY (user_id) REFERENCES users(id)" +
                        ")";
                stmt.execute(sql);

                // Create bookmarked_jobs table
                sql = "CREATE TABLE IF NOT EXISTS bookmarked_jobs (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id INT," +
                        "title VARCHAR(255)," +
                        "company VARCHAR(255)," +
                        "location VARCHAR(255)," +
                        "salary VARCHAR(255)," +
                        "job_link VARCHAR(255)," +
                        "description TEXT," +
                        "FOREIGN KEY(user_id) REFERENCES users(id)" +
                        ")";
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        }
    }

    public static boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    public static int getUserId(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1; // User not found
    }

    public static void saveResumeData(int userId, ResumeSearch.ResumeData resumeData) throws SQLException {
        String sql = "INSERT INTO resume_data(user_id, keywords, experience, location) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, String.join(",", resumeData.matchedKeywords));
            pstmt.setInt(3, resumeData.experience);
            pstmt.setString(4, resumeData.location);
            pstmt.executeUpdate();
        }
    }

    public static List<ResumeSearch.ResumeData> getResumeDataForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM resume_data WHERE user_id = ?";
        List<ResumeSearch.ResumeData> resumeDataList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String[] keywords = rs.getString("keywords").split(",");
                int experience = rs.getInt("experience");
                String location = rs.getString("location");
                resumeDataList.add(new ResumeSearch.ResumeData(List.of(keywords), experience, location));
            }
        }
        return resumeDataList;
    }

    public static void saveUserDetails(int userId, String resumePath, String location, int experience) throws SQLException {
        String sql = "INSERT INTO user_details (user_id, resume_path, location, experience) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE resume_path = ?, location = ?, experience = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, resumePath);
            pstmt.setString(3, location);
            pstmt.setInt(4, experience);
            pstmt.setString(5, resumePath);
            pstmt.setString(6, location);
            pstmt.setInt(7, experience);
            pstmt.executeUpdate();
        }
    }

    public static UserDetails getUserDetails(int userId) throws SQLException {
        String sql = "SELECT resume_path, location, experience FROM user_details WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String resumePath = rs.getString("resume_path");
                String location = rs.getString("location");
                int experience = rs.getInt("experience");
                return new UserDetails(resumePath, location, experience);
            }
        }
        return null;
    }

    public static void addBookmarkedJob(int userId, JobScraper.Job job) throws SQLException {
        String sql = "INSERT INTO bookmarked_jobs (user_id, title, company, location, salary, job_link, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, job.title);
            pstmt.setString(3, job.company);
            pstmt.setString(4, job.location);
            pstmt.setString(5, job.salary);
            pstmt.setString(6, job.jobLink);
            pstmt.setString(7, job.description);
            pstmt.executeUpdate();
        }
    }

    public static void removeBookmarkedJob(int userId, JobScraper.Job job) throws SQLException {
        String sql = "DELETE FROM bookmarked_jobs WHERE user_id = ? AND job_link = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, job.jobLink);
            pstmt.executeUpdate();
        }
    }

    public static List<JobScraper.Job> getBookmarkedJobs(int userId) throws SQLException {
        String sql = "SELECT * FROM bookmarked_jobs WHERE user_id = ?";
        List<JobScraper.Job> bookmarkedJobs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                JobScraper.Job job = new JobScraper.Job(
                        rs.getString("title"),
                        rs.getString("company"),
                        rs.getString("location"),
                        "",  // experience is not stored
                        rs.getString("salary"),
                        rs.getString("job_link"),
                        rs.getString("description")
                );
                bookmarkedJobs.add(job);
            }
        }
        return bookmarkedJobs;
    }

    public static class UserDetails {
        public String resumePath;
        public String location;
        public int experience;

        public UserDetails(String resumePath, String location, int experience) {
            this.resumePath = resumePath;
            this.location = location;
            this.experience = experience;
        }
    }
}