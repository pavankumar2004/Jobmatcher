    package org.example.jobaifinal;

    import javafx.fxml.FXML;
    import javafx.scene.control.*;
    import javafx.scene.layout.FlowPane;
    import javafx.scene.layout.VBox;
    import javafx.stage.FileChooser;
    import javafx.stage.Stage;
    import javafx.scene.Scene;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;

    import java.io.File;
    import java.io.IOException;
    import java.sql.SQLException;
    import java.util.List;
    import java.util.ArrayList;

    public class ResumeUploadController {

        @FXML
        private TextField resumePathField;

        @FXML
        private TextField locationField;

        @FXML
        private TextField experienceField;

        @FXML
        private FlowPane jobsPane;

        @FXML
        private FlowPane bookmarkedJobsPane;

        @FXML
        private Button logoutButton;

        private JobScraper jobScraper;
        private int currentUserId = -1;
        private List<JobScraper.Job> bookmarkedJobs = new ArrayList<>();

        public ResumeUploadController() {
            jobScraper = new JobScraper();
        }

        public void initData(int userId) {
            currentUserId = userId;
            loadUserDetails();
            loadBookmarkedJobs();
        }

        private void loadUserDetails() {
            try {
                DatabaseOperations.UserDetails userDetails = DatabaseOperations.getUserDetails(currentUserId);
                if (userDetails != null) {
                    resumePathField.setText(userDetails.resumePath);
                    locationField.setText(userDetails.location);
                    experienceField.setText(String.valueOf(userDetails.experience));
                } else {
                    showAlert("Info", "Please upload your resume to view data.");
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to load user details: " + e.getMessage());
            }
        }

        private void loadBookmarkedJobs() {
            try {
                bookmarkedJobs = DatabaseOperations.getBookmarkedJobs(currentUserId);
                displayBookmarkedJobs();
            } catch (SQLException e) {
                showAlert("Error", "Failed to load bookmarked jobs: " + e.getMessage());
            }
        }

        @FXML
        private void searchJobs() {
            String resumePath = resumePathField.getText().trim();
            String location = locationField.getText().trim();
            String experienceStr = experienceField.getText().trim();

            if (resumePath.isEmpty() || location.isEmpty() || experienceStr.isEmpty()) {
                showAlert("Error", "Please fill in all fields");
                return;
            }

            try {
                ResumeSearch.ResumeData parsedResumeData = ResumeSearch.parseResume(resumePath);
                int experience = Integer.parseInt(experienceStr);

                DatabaseOperations.saveUserDetails(currentUserId, resumePath, location, experience);

                ResumeSearch.ResumeData resumeData = new ResumeSearch.ResumeData(
                        parsedResumeData.matchedKeywords, experience, location
                );

                DatabaseOperations.saveResumeData(currentUserId, resumeData);

                List<JobScraper.Job> jobs = jobScraper.scrapeJobs(resumeData);
                displayJobs(jobs);
            } catch (IOException ex) {
                showAlert("Error", "Error reading resume: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid experience input. Please enter a number.");
            } catch (SQLException ex) {
                showAlert("Error", "Database error: " + ex.getMessage());
            }
        }

        @FXML
        private void logout() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/jobaifinal/LoginView.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Error", "Error loading login view: " + e.getMessage());
            }
        }

        @FXML
        private void browseForResume() {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                resumePathField.setText(selectedFile.getAbsolutePath());
            }
        }

        private void displayJobs(List<JobScraper.Job> jobs) {
            jobsPane.getChildren().clear();

            if (jobs.isEmpty()) {
                Label noJobsLabel = new Label("No jobs found for the specified criteria.");
                noJobsLabel.getStyleClass().add("no-jobs-label");
                jobsPane.getChildren().add(noJobsLabel);
            } else {
                for (JobScraper.Job job : jobs) {
                    VBox jobTile = createJobTile(job, false);
                    jobsPane.getChildren().add(jobTile);
                }
            }
        }

        private void displayBookmarkedJobs() {
            bookmarkedJobsPane.getChildren().clear();

            if (bookmarkedJobs.isEmpty()) {
                Label noBookmarksLabel = new Label("No bookmarked jobs.");
                noBookmarksLabel.getStyleClass().add("no-jobs-label");
                bookmarkedJobsPane.getChildren().add(noBookmarksLabel);
            } else {
                for (JobScraper.Job job : bookmarkedJobs) {
                    VBox jobTile = createJobTile(job, true);
                    bookmarkedJobsPane.getChildren().add(jobTile);
                }
            }
        }

        private VBox createJobTile(JobScraper.Job job, boolean isBookmarked) {
            VBox jobTile = new VBox(5);
            jobTile.getStyleClass().add("job-tile");

            Label titleLabel = new Label(job.title);
            titleLabel.getStyleClass().add("job-title");

            Hyperlink companyLink = new Hyperlink(job.company);
            companyLink.setOnAction(e -> openLink(job.jobLink));

            jobTile.getChildren().addAll(
                    titleLabel,
                    companyLink,
                    new Label(job.location),
                    new Label(job.salary)
            );

            Button detailsButton = new Button("View Details");
            detailsButton.setOnAction(e -> showJobDetails(job));
            detailsButton.getStyleClass().add("details-button");

            Button bookmarkButton = new Button(isBookmarked ? "Remove Bookmark" : "Bookmark");
            bookmarkButton.setOnAction(e -> toggleBookmark(job, bookmarkButton));
            bookmarkButton.getStyleClass().add("bookmark-button");

            jobTile.getChildren().addAll(detailsButton, bookmarkButton);

            return jobTile;
        }

        private void openLink(String url) {
            getHostServices().showDocument(url);
        }

        private void showJobDetails(JobScraper.Job job) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(job.title);
            alert.setHeaderText(null);

            // Create a content area with a larger size
            TextArea textArea = new TextArea(job.description + "\n\nApply here: " + job.jobLink);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            // Set the preferred size of the text area
            textArea.setPrefSize(400, 200); // Adjust width and height as needed

            // Add the text area to the alert dialog
            alert.getDialogPane().setContent(textArea);

            // Show the alert
            alert.showAndWait();
        }


        private void toggleBookmark(JobScraper.Job job, Button bookmarkButton) {
            try {
                if (bookmarkedJobs.contains(job)) {
                    DatabaseOperations.removeBookmarkedJob(currentUserId, job);
                    bookmarkedJobs.remove(job);
                    bookmarkButton.setText("Bookmark");
                } else {
                    DatabaseOperations.addBookmarkedJob(currentUserId, job);
                    bookmarkedJobs.add(job);
                    bookmarkButton.setText("Remove Bookmark");
                }
                displayBookmarkedJobs();
            } catch (SQLException e) {
                showAlert("Error", "Failed to update bookmark: " + e.getMessage());
            }
        }

        private void showAlert(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }

        private javafx.application.HostServices getHostServices() {
            return (javafx.application.HostServices) logoutButton.getScene().getWindow().getProperties().get("hostServices");
        }
    }