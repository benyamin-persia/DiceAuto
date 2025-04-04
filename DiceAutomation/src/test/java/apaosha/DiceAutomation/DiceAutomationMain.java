package apaosha.DiceAutomation;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class DiceAutomationMain {
    private static final Logger logger = LoggerFactory.getLogger(DiceAutomationMain.class);
    private static final String DEFAULT_EMAIL = "soyoxok649@avulos.com";
    private static final String DEFAULT_PASSWORD = "!2q3wa4esZ";
    private static final String CREDENTIALS_FILE = "credentials.txt";

    public static void main(String[] args) {
        logger.info("Starting Dice automation (Login, Job Search, and Data Extraction).");
        Scanner scanner = new Scanner(System.in);
        String email, password, jobTitle, location;
        int postedDateOption = 0;
        String empTypeInput, employerTypeInput, easyApplyChoice, workAuthChoice;

        // --- Prompt for login credentials ---
        File credFile = new File(CREDENTIALS_FILE);
        if (credFile.exists()) {
            logger.info("Stored credentials file found.");
            System.out.print("Stored credentials found. Use stored credentials? (Y/N): ");
            String useStored = scanner.nextLine();
            if (useStored.equalsIgnoreCase("Y")) {
                try (BufferedReader br = new BufferedReader(new FileReader(credFile))) {
                    email = br.readLine();
                    password = br.readLine();
                    logger.info("Using stored credentials for email: {}", email);
                } catch (IOException e) {
                    logger.error("Error reading credentials file. Using default credentials.", e);
                    email = DEFAULT_EMAIL;
                    password = DEFAULT_PASSWORD;
                }
            } else {
                logger.info("User opted not to use stored credentials.");
                System.out.print("Use test credentials (" + DEFAULT_EMAIL + ", " + DEFAULT_PASSWORD + ")? (Y/N): ");
                String useTest = scanner.nextLine();
                if (useTest.equalsIgnoreCase("Y")) {
                    email = DEFAULT_EMAIL;
                    password = DEFAULT_PASSWORD;
                    saveCredentials(email, password);
                    logger.info("Using test credentials.");
                } else {
                    System.out.print("Enter your email: ");
                    email = scanner.nextLine();
                    System.out.print("Enter your password: ");
                    password = scanner.nextLine();
                    saveCredentials(email, password);
                    logger.info("Using user-provided credentials for email: {}", email);
                }
            }
        } else {
            logger.info("No stored credentials found.");
            System.out.print("No stored credentials found. Use test credentials (" + DEFAULT_EMAIL + ", " + DEFAULT_PASSWORD + ")? (Y/N): ");
            String useTest = scanner.nextLine();
            if (useTest.equalsIgnoreCase("Y")) {
                email = DEFAULT_EMAIL;
                password = DEFAULT_PASSWORD;
                saveCredentials(email, password);
                logger.info("Using test credentials.");
            } else {
                System.out.print("Enter your email: ");
                email = scanner.nextLine();
                System.out.print("Enter your password: ");
                password = scanner.nextLine();
                saveCredentials(email, password);
                logger.info("Using user-provided credentials for email: {}", email);
            }
        }

        // --- Prompt for job search details ---
        System.out.print("Enter job title/skill (default 'QA tester'): ");
        jobTitle = scanner.nextLine().trim();
        if (jobTitle.isEmpty()) {
            jobTitle = "QA tester";
        }
        System.out.print("Enter location (default 'United States'): ");
        location = scanner.nextLine().trim();
        if (location.isEmpty()) {
            location = "United States";
        }
        logger.info("Job title/skill set to: {}", jobTitle);
        logger.info("Location set to: {}", location);

        // --- Prompt for Posted Date filter ---
        System.out.println("Select Posted Date filter (default is 0 - Any Date):");
        System.out.println("[0] Any Date");
        System.out.println("[1] Today");
        System.out.println("[2] Last 3 Days");
        System.out.println("[3] Last 7 Days");
        String postedDateChoice = scanner.nextLine().trim();
        try {
            if (!postedDateChoice.isEmpty()) {
                postedDateOption = Integer.parseInt(postedDateChoice);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid input for Posted Date filter, defaulting to Any Date.");
        }
        logger.info("Posted Date filter selected: {}", postedDateOption);

        // --- Prompt for Employment Type filter using numbers ---
        System.out.println("Select Employment Type filter(s) by numbers (comma separated). Options:");
        System.out.println("[1] FULLTIME");
        System.out.println("[2] PARTTIME");
        System.out.println("[3] CONTRACTS");
        System.out.println("[4] THIRD_PARTY");
        System.out.println("Press ENTER to select all (default).");
        empTypeInput = scanner.nextLine().trim();

        // --- Prompt for Employer Type filter using numbers ---
        System.out.println("Select Employer Type filter(s) by numbers (comma separated). Options:");
        System.out.println("[1] Direct Hire");
        System.out.println("[2] Recruiter");
        System.out.println("[3] Other");
        System.out.println("Press ENTER to select all (default).");
        employerTypeInput = scanner.nextLine().trim();

        // --- Prompt for Easy Apply filter (default Y) ---
        System.out.print("Filter by Easy Apply? (Y/N, default Y): ");
        easyApplyChoice = scanner.nextLine().trim();

        // --- Prompt for Work Authorization filter (default N) ---
        System.out.print("Filter by Work Authorization? (Y/N, default N): ");
        workAuthChoice = scanner.nextLine().trim();

        // --- Check for saved filters specific to this user ---
        String filterFileName = "filters_" + email.replaceAll("[^a-zA-Z0-9]", "") + ".txt";
        File filterFile = new File(filterFileName);
        if (filterFile.exists()) {
            System.out.print("Saved filter settings found. Use saved filters? (Y/N): ");
            String useSavedFilters = scanner.nextLine().trim();
            if (useSavedFilters.equalsIgnoreCase("Y")) {
                // Load filters from file (format: postedDateOption,empTypeInput,employerTypeInput,easyApplyChoice,workAuthChoice)
                try (BufferedReader br = new BufferedReader(new FileReader(filterFile))) {
                    String line = br.readLine();
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        postedDateOption = Integer.parseInt(parts[0]);
                        empTypeInput = parts[1];
                        employerTypeInput = parts[2];
                        easyApplyChoice = parts[3];
                        workAuthChoice = parts[4];
                        logger.info("Loaded saved filters: PostedDate={}, EmploymentType={}, EmployerType={}, EasyApply={}, WorkAuth={}",
                                postedDateOption, empTypeInput, employerTypeInput, easyApplyChoice, workAuthChoice);
                    }
                } catch (IOException e) {
                    logger.error("Error reading filter file, proceeding with current inputs.", e);
                }
            } else {
                logger.info("User chose to modify filters. Saving new filters.");
                saveFilters(filterFileName, postedDateOption, empTypeInput, employerTypeInput, easyApplyChoice, workAuthChoice);
            }
        } else {
            logger.info("No saved filters found. Saving current filter selections.");
            saveFilters(filterFileName, postedDateOption, empTypeInput, employerTypeInput, easyApplyChoice, workAuthChoice);
        }

        // --- Setup WebDriver and login ---
        System.setProperty("webdriver.chrome.silentOutput", "true");
        logger.info("Setting up ChromeDriver.");
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        logger.info("Maximizing the browser window.");
        driver.manage().window().maximize();
        logger.info("Navigating to https://www.dice.com/dashboard/login");
        driver.get("https://www.dice.com/dashboard/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        try {
            // --- Login Process ---
            logger.info("Waiting for the login email input field.");
            By loginEmailXPath = By.xpath("//input[@placeholder='Please enter your email' and @type='email']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(loginEmailXPath));
            logger.info("Entering email: {}", email);
            driver.findElement(loginEmailXPath).sendKeys(email);

            logger.info("Waiting for the Continue button.");
            By continueButtonXPath = By.xpath("//button[@data-testid='sign-in-button']");
            wait.until(ExpectedConditions.elementToBeClickable(continueButtonXPath));
            logger.info("Clicking the Continue button.");
            driver.findElement(continueButtonXPath).click();

            logger.info("Waiting for the password input field.");
            By passwordFieldXPath = By.xpath("//input[@placeholder='Enter Password' and @type='password']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(passwordFieldXPath));
            logger.info("Entering password.");
            driver.findElement(passwordFieldXPath).sendKeys(password);

            logger.info("Waiting for the Sign In button.");
            By signInButtonXPath = By.xpath("//button[@data-testid='submit-password']");
            wait.until(ExpectedConditions.elementToBeClickable(signInButtonXPath));
            logger.info("Clicking the Sign In button.");
            driver.findElement(signInButtonXPath).click();
        } catch (Exception e) {
            logger.error("Error during login interaction: {}", e.getMessage(), e);
        }

        logger.info("Login submitted. Waiting for job search page to load.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            logger.error("Interrupted while waiting for job search page.", ie);
        }

        // --- Fill in the job search fields ---
        try {
            logger.info("Waiting for the job title input field.");
            By jobTitleXPath = By.xpath("//input[@aria-label='Job title, skill, company, keyword' and @placeholder='Job title, skill, company, keyword']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(jobTitleXPath));
            logger.info("Entering job title/skill: {}", jobTitle);
            driver.findElement(jobTitleXPath).clear();
            driver.findElement(jobTitleXPath).sendKeys(jobTitle);

            logger.info("Waiting for the location input field.");
            By locationXPath = By.xpath("//input[@aria-label='Location Field' and @placeholder='Location (ex. Denver, remote)']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(locationXPath));
            logger.info("Entering location: {}", location);
            WebElement locationField = driver.findElement(locationXPath);
            locationField.clear();
            locationField.sendKeys(location);
            logger.info("Sending ENTER key to location field.");
            locationField.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            logger.error("Error during job search fields interaction: {}", e.getMessage(), e);
        }

        // --- Apply Filters ---
        DiceFilterActions.applyFilters(driver, wait, postedDateOption, empTypeInput, employerTypeInput, easyApplyChoice, workAuthChoice);

        // --- Extract Job Titles and IDs from all pages ---
        // We'll loop from page 1 until no job titles are found.
        String baseUrl = driver.getCurrentUrl();
        logger.info("Starting job title extraction from all pages.");
        int currentPage = 1;
        // CSV file for saving results
        String csvFile = "job_results.csv";
        try (BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile))) {
            // Write CSV header
            csvWriter.write("Job Title,Job Link");
            csvWriter.newLine();
            while (true) {
                String pageUrl;
                if (baseUrl.contains("&page=")) {
                    pageUrl = baseUrl.replaceAll("&page=\\d+", "&page=" + currentPage);
                } else {
                    pageUrl = baseUrl + "&page=" + currentPage;
                }
                logger.info("Navigating to page {}: {}", currentPage, pageUrl);
                driver.get(pageUrl);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    logger.error("Interrupted while waiting for page load.", ie);
                }
                List<DiceFilterActions.JobData> jobDataList = DiceFilterActions.extractJobData(driver, wait);
                if (jobDataList.isEmpty()) {
                    logger.info("No job titles found on page {}. Ending extraction.", currentPage);
                    break;
                }
                logger.info("Page {} job titles:", currentPage);
                for (DiceFilterActions.JobData jobData : jobDataList) {
                    logger.info(jobData.getTitle());
                    // Write to CSV: enclose in quotes in case of commas
                    String jobLink = "https://www.dice.com/job-detail/" + jobData.getId();
                    csvWriter.write("\"" + jobData.getTitle().replace("\"", "\"\"") + "\"," + jobLink);
                    csvWriter.newLine();
                }
                csvWriter.flush();
                currentPage++;
            }
        } catch (IOException e) {
            logger.error("Error writing to CSV file: {}", e.getMessage(), e);
        }

        logger.info("Job title extraction completed. Results saved to {}", csvFile);
        logger.info("Press ENTER to exit and close the browser.");
        scanner.nextLine();
        logger.info("Closing the browser.");
        driver.quit();
        logger.info("Dice automation finished.");
    }

    private static void saveCredentials(String email, String password) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            bw.write(email);
            bw.newLine();
            bw.write(password);
        } catch (IOException e) {
            logger.error("Error saving credentials: {}", e.getMessage(), e);
        }
    }
    
    private static void saveFilters(String filterFileName, int postedDateOption, String empTypeInput, 
                                    String employerTypeInput, String easyApplyChoice, String workAuthChoice) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filterFileName))) {
            // Save filters as comma-separated values
            bw.write(postedDateOption + "," + empTypeInput + "," + employerTypeInput + "," + easyApplyChoice + "," + workAuthChoice);
        } catch (IOException e) {
            logger.error("Error saving filters: {}", e.getMessage(), e);
        }
    }
}
