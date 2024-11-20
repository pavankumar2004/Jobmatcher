package org.example.jobaifinal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ResumeSearch {

    private static final Set<String> COMMON_KEYWORDS = Set.of(
            "machine learning", "blockchain", "software development",
            "web development", "computer networks", "cloud computing",
            "full stack development", "data science", "artificial intelligence",
            "cybersecurity", "big data", "devops", "mobile development",
            "database management", "agile", "scrum", "test automation",
            "continuous integration", "UX/UI design", "game development",
            "data analysis", "deep learning", "IoT", "natural language processing",
            "augmented reality", "virtual reality", "edge computing",
            "robotics", "quantum computing", "5G", "containerization",
            "microservices", "serverless architecture", "kubernetes",
            "docker", "business intelligence", "financial technology",
            "health tech", "AI ethics", "data privacy", "edge AI",
            "distributed systems", "low-code development", "no-code development"
    );



    private static final Set<String> PROGRAMMING_LANGUAGES = Set.of(
            "python", "c++", "c", "javascript", "java", "bash",
            "typescript", "kotlin", "swift", "ruby", "php",
            "go", "rust", "r", "sql", "html", "css",
            "perl", "scala", "matlab", "julia", "objective-c",
            "dart", "elixir", "haskell", "lua", "shell",
            "powershell", "assembly", "clojure", "fortran",
            "f#", "groovy", "vhdl", "verilog"
    );



    public static ResumeData parseResume(String resumeFilePath) throws IOException {
        System.out.println("Parsing resume from: " + resumeFilePath);
        String text = readPDF(resumeFilePath);
        List<String> lines = List.of(text.split(System.lineSeparator()));

        List<String> skills = extractSkills(lines);
        List<String> languages = extractLanguages(lines);
        List<String> interests = extractInterests(lines);
        int experience = extractExperience(lines);

        List<String> matchedKeywords = matchKeywords(interests, skills, languages);

        System.out.println("Resume parsed successfully. Matched Keywords: " + matchedKeywords + ", Experience: " + experience);
        return new ResumeData(matchedKeywords, experience, ""); // Empty string for location, to be filled by user input
    }

    private static String readPDF(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(Files.newInputStream(Paths.get(filePath)))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private static List<String> extractSkills(List<String> lines) {
        return lines.stream()
                .filter(line -> line.toLowerCase().contains("skills"))
                .findFirst()
                .map(line -> {
                    String[] skillsArray = line.split(":");
                    if (skillsArray.length > 1) {
                        return Arrays.stream(skillsArray[1].split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                    }
                    return new ArrayList<String>();
                })
                .orElse(new ArrayList<>());
    }

    private static List<String> extractLanguages(List<String> lines) {
        return lines.stream()
                .filter(line -> line.toLowerCase().contains("languages"))
                .findFirst()
                .map(line -> {
                    String[] languagesArray = line.split(":");
                    if (languagesArray.length > 1) {
                        return Arrays.stream(languagesArray[1].split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());
                    }
                    return new ArrayList<String>();
                })
                .orElse(new ArrayList<>());
    }

    private static List<String> extractInterests(List<String> lines) {
        List<String> interests = new ArrayList<>();
        String entireText = String.join(" ", lines).toLowerCase();
        for (String keyword : COMMON_KEYWORDS) {
            if (entireText.contains(keyword.toLowerCase())) {
                interests.add(keyword);
            }
        }
        return interests;
    }

    private static int extractExperience(List<String> lines) {
        if (lines.isEmpty()) {
            System.out.println("No lines to process. Returning default experience.");
        }
        for (String line : lines) {
            System.out.println("Processing line: " + line);
        }
        int randomNumber = (int) (Math.random() * 10);
        boolean containsExperience = lines.stream().anyMatch(line -> line.toLowerCase().contains("experience"));
        System.out.println("Contains the word 'experience': " + containsExperience);
        return 2;
    }


    private static List<String> matchKeywords(List<String> interests, List<String> skills, List<String> languages) {
        Set<String> matchedKeywords = new HashSet<>();

        for (String interest : interests) {
            if (COMMON_KEYWORDS.contains(interest.toLowerCase())) {
                matchedKeywords.add(interest);
            }
        }

        for (String language : languages) {
            if (PROGRAMMING_LANGUAGES.contains(language.toLowerCase())) {
                matchedKeywords.add(language);
            }
        }

        for (String skill : skills) {
            if (PROGRAMMING_LANGUAGES.contains(skill.toLowerCase())) {
                matchedKeywords.add(skill);
            }
        }

        return new ArrayList<>(matchedKeywords);
    }

    public static class ResumeData {
        public List<String> matchedKeywords;
        public int experience;
        public String location;

        public ResumeData(List<String> matchedKeywords, int experience, String location) {
            this.matchedKeywords = matchedKeywords;
            this.experience = experience;
            this.location = location;
        }
    }
}