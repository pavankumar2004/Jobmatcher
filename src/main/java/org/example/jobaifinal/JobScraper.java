package org.example.jobaifinal;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JobScraper {

    public List<Job> scrapeJobs(ResumeSearch.ResumeData resumeData) throws IOException {
        List<Job> jobs = new ArrayList<>();
        TimesJobsScraper timesJobsScraper = new TimesJobsScraper();
        FresherJobsScraper fresherJobsScraper = new FresherJobsScraper();

        jobs.addAll(timesJobsScraper.scrapeJobs(resumeData));
        jobs.addAll(fresherJobsScraper.scrapeJobs(resumeData));

        System.out.println("Scraping completed. Total jobs found: " + jobs.size());
        return jobs;
    }

    public static class Job {
        String title;
        String company;
        String location;
        String experience;
        String salary;
        String jobLink;
        String description;

        Job(String title, String company, String location, String experience, String salary, String jobLink, String description) {
            this.title = title;
            this.company = company;
            this.location = location;
            this.experience = experience;
            this.salary = salary;
            this.jobLink = jobLink;
            this.description = description;
        }
    }

    private static class TimesJobsScraper {
        public List<Job> scrapeJobs(ResumeSearch.ResumeData resumeData) throws IOException {
            String baseUrl = "https://www.timesjobs.com/candidate/job-search.html?";
            List<Job> jobs = new ArrayList<>();

            System.out.println("Starting TimesJobs scraping...");
            System.out.println("Preferred Location: " + resumeData.location);
            System.out.println("Experience: " + resumeData.experience);

            for (String keyword : resumeData.matchedKeywords) {
                for (int i = 1; i <= 3; i++) {
                    String pageUrl = baseUrl + "searchType=personalizedSearch&from=submit" +
                            "&txtKeywords=" + keyword.replace(" ", "+") +
                            "&txtLocation=" + resumeData.location.replace(" ", "+") +
                            "&cboWorkExp1=" + resumeData.experience +
                            "&sequence=" + i;

                    System.out.println("Fetching TimesJobs page for keyword '" + keyword + "': " + pageUrl);
                    Document document = Jsoup.connect(pageUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .timeout(10 * 1000)
                            .get();

                    Elements jobCards = document.select("li.clearfix.job-bx");
                    System.out.println("Found " + jobCards.size() + " job cards for keyword '" + keyword + "' on page " + i);

                    for (Element jobCard : jobCards) {
                        Job job = extractJobInfo(jobCard);
                        if (job != null) {
                            jobs.add(job);
                            System.out.println("TimesJobs job added: " + job.title);
                        }
                    }
                }
            }

            return jobs;
        }

        private Job extractJobInfo(Element jobCard) {
            String title = jobCard.select("h2 a").text();
            String company = jobCard.select("h3.joblist-comp-name").text().trim();
            String location = jobCard.select("ul.top-jd-dtl li:nth-child(3) span").text();
            String experience = jobCard.select("ul.top-jd-dtl li:first-child").text();
            String salary = jobCard.select("ul.top-jd-dtl li:nth-child(2)").text();
            String jobLink = "https://www.timesjobs.com" + jobCard.select("h2 a").attr("href");
            String description = jobCard.select("li:has(label:contains(Job Description:))").text();

            return new Job(title, company, location, experience, salary, jobLink, description);
        }
    }

    private static class FresherJobsScraper {
        public List<Job> scrapeJobs(ResumeSearch.ResumeData resumeData) throws IOException {
            String baseUrl = "https://www.freshersworld.com/jobs/jobsearch/";
            List<Job> jobs = new ArrayList<>();

            System.out.println("Starting FresherJobs scraping...");
            System.out.println("Preferred Location: " + resumeData.location);
            System.out.println("Experience: " + resumeData.experience);

            for (String keyword : resumeData.matchedKeywords) {
                String jobType = keyword.replace(" ", "-").toLowerCase() + "-jobs";
                String location = resumeData.location.toLowerCase();
                String qualification = getQualification(resumeData); // You need to implement this method

                String pageUrl = baseUrl + jobType + "-for-" + qualification + "-in-" + location;

                System.out.println("Fetching FresherJobs page: " + pageUrl);
                Document document = Jsoup.connect(pageUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10 * 1000)
                        .get();

                Elements jobCards = document.select("div.job-container");
                System.out.println("Found " + jobCards.size() + " job cards for keyword '" + keyword + "'");

                for (Element jobCard : jobCards) {
                    Job job = extractFresherJobInfo(jobCard);
                    if (job != null) {
                        jobs.add(job);
                        System.out.println("FresherJobs job added: " + job.title);
                    }
                }
            }

            return jobs;
        }

        private String getQualification(ResumeSearch.ResumeData resumeData) {
            // Implement logic to determine qualification based on resumeData
            return "msc"; // or "be", "btech", etc.
        }

        private Job extractFresherJobInfo(Element jobCard) {
            String title = jobCard.select("div.job-new-title span.wrap-title").text();
            String company = jobCard.select("h3.latest-jobs-title.company-name").text();
            String location = jobCard.select("span.job-location").text();
            String experience = jobCard.select("span.experience").text();
            String salary = jobCard.select("span.qualifications:contains(Salary)").text();
            String jobLink = jobCard.select("div.job-container").attr("job_display_url");
            String description = jobCard.select("span.desc").text();

            return new Job(title, company, location, experience, salary, jobLink, description);
        }
    }
}