package dice;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class DiceJobLogin {

    public static void main(String[] args) {
        // Set path to your manually downloaded ChromeDriver v135
        System.setProperty("webdriver.chrome.driver", "C:\\your-folder\\chromedriver-win64\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Phase 1: Scrape jobs and save to CSV
        try {
            // ------------------ LOGIN PROCESS ------------------
            driver.get("https://www.dice.com/dashboard/login");
            System.out.println("🔐 Logging in...");

            // Wait for email field and enter email
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
            emailInput.sendKeys("soyoxok649@avulos.com");
            System.out.println("Email entered.");

            // Click Continue button
            WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='sign-in-button']")));
            continueBtn.click();
            System.out.println("Continue button clicked.");

            // Wait for password field and enter password
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
            passwordInput.clear();
            passwordInput.sendKeys("!2q3wa4esZ");
            System.out.println("Password entered.");

            // Submit the login form
            passwordInput.sendKeys(Keys.ENTER);
            System.out.println("Submitted login form.");

            // Wait until fully loaded and URL contains /dashboard
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
            wait.until(ExpectedConditions.urlContains("/dashboard"));
            System.out.println("✅ Logged in successfully!");

            // ------------------ GET MANDATORY KEYWORDS ------------------
            Scanner scanner = new Scanner(System.in);
            System.out.print("🔍 Enter mandatory keywords separated by commas (e.g., manual,selenium,entry): ");
            String[] keywords = scanner.nextLine().toLowerCase().split(",");
            // (Leave scanner open if needed later)

            // ------------------ JOB SCRAPING PROCESS ------------------
            String csvFile = "job_titles.csv";
            try (FileWriter fw = new FileWriter(csvFile);
                 PrintWriter pw = new PrintWriter(fw)) {

                // Write CSV header
                pw.println("Job Title,Job Link");

                // Define base URL parts for job search (1000 jobs per page)
                String baseUrl = "https://www.dice.com/jobs?q=QA%20testing&location=United%20States"
                        + "&latitude=38.7945952&longitude=-106.5348379&countryCode=US&locationPrecision=Country"
                        + "&radius=30&radiusUnit=mi&page=";
                String pageSizePart = "&pageSize=1000&filters.postedDate=ONE&filters.employmentType=FULLTIME&filters.easyApply=true&language=en&eid=8855";

                // Go to first page to get total job count
                String firstPageUrl = baseUrl + "1" + pageSizePart;
                driver.get(firstPageUrl);

                // Extract total job count from element with data-cy="search-count"
                WebElement searchCountElement = wait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-cy='search-count']"))
                );
                String totalJobsStr = searchCountElement.getText().replaceAll(",", "").trim();
                int totalJobs = Integer.parseInt(totalJobsStr);
                int maxPages = (int) Math.ceil(totalJobs / 1000.0);
                System.out.println("Total jobs found: " + totalJobs + ". Estimated max pages: " + maxPages);

                int matchedJobs = 0;
                // Loop through pages 1 to maxPages
                for (int page = 1; page <= maxPages; page++) {
                    System.out.println("\n📄 Visiting page " + page + " of " + maxPages);
                    String pagedUrl = baseUrl + page + pageSizePart;
                    driver.get(pagedUrl);

                    // Wait for job card elements
                    List<WebElement> jobCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                            By.cssSelector("a[data-cy='card-title-link']")));
                    if (jobCards.isEmpty()) {
                        System.out.println("🚫 No job titles found on page " + page + ". Ending pagination.");
                        break;
                    }

                    // Process each job card on the page
                    for (WebElement jobCard : jobCards) {
                        String title = jobCard.getText().trim();
                        String titleLower = title.toLowerCase();
                        boolean isMatched = false;
                        for (String keyword : keywords) {
                            if (titleLower.contains(keyword.trim())) {
                                isMatched = true;
                                break;
                            }
                        }
                        if (isMatched && !title.isEmpty()) {
                            // Build the job detail URL using the job card's id attribute
                            String jobId = jobCard.getAttribute("id");
                            String jobUrl = "https://www.dice.com/job-detail/" + jobId;
                            System.out.println("🧾 " + title + " | " + jobUrl);
                            pw.println("\"" + title + "\"," + jobUrl);
                            matchedJobs++;
                            pw.flush();
                        }
                    }
                    Thread.sleep(2000); // Brief pause between pages
                }
                System.out.println("\n🎉 Scraping finished! Total matched jobs: " + matchedJobs);
            }
        } catch (Exception e) {
            System.out.println("❌ Error during scraping: " + e.getMessage());
        }

        // ------------------ PHASE 2: APPLY PROCESS ------------------
        try {
            System.out.println("\n🛠 Starting apply process from CSV file...");
            // Read the CSV file and process each job
            try (BufferedReader br = new BufferedReader(new FileReader("job_titles.csv"))) {
                String line;
                // Skip header
                br.readLine();
                while ((line = br.readLine()) != null) {
                    // Assume CSV format: "Job Title",Job Link
                    // Split on ",", then remove starting quote from first field.
                    String[] parts = line.split("\",");
                    if (parts.length < 2) continue;
                    String jobTitle = parts[0].replaceAll("^\"", "").trim();
                    String jobUrl = parts[1].trim();
                    
                    System.out.println("\n🔗 Applying for job: " + jobTitle + " | " + jobUrl);
                    
                    // Navigate to job detail page and wait for full load
                    driver.get(jobUrl);
                    new WebDriverWait(driver, Duration.ofSeconds(30))
                            .until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
                    Thread.sleep(2000);
                    
                    // ----- Attempt to click the "Apply now/Easy apply" button using single shadow DOM -----
                    boolean clicked = false;
                    long endTime = System.currentTimeMillis() + 15000; // wait up to 15 seconds
                    while (System.currentTimeMillis() < endTime && !clicked) {
                        try {
                            // Locate the shadow host element using the CSS selector from your snippet
                            WebElement shadowHost = driver.findElement(By.cssSelector("apply-button-wc[class='hydrated']"));
                            Thread.sleep(1000);
                            // Get the shadow root
                            SearchContext shadow = shadowHost.getShadowRoot();
                            Thread.sleep(1000);
                            // Find the apply button using the relative selector ".btn.btn-primary"
                            WebElement applyButton = shadow.findElement(By.cssSelector(".btn.btn-primary"));
                            // Scroll into view and click via JavaScript
                            js.executeScript("arguments[0].scrollIntoView(true);", applyButton);
                            js.executeScript("arguments[0].click();", applyButton);
                            System.out.println("✅ Clicked 'Apply now/Easy apply' for: " + jobTitle);
                            clicked = true;
                            // After clicking, print the current URL
                            String currentUrl = driver.getCurrentUrl();
                            System.out.println("🔗 Current URL after click: " + currentUrl);
                            
                            // ----- Click the "Next" button on the current page -----
                            try {
                                WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(
                                        By.cssSelector("button.seds-button-primary.btn-next")));
                                nextButton.click();
                                System.out.println("✅ Clicked 'Next' button. New URL: " + driver.getCurrentUrl());
                                
                                // ----- Finally, click the "Submit" button -----
                                try {
                                    WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                                            By.xpath("//button[contains(@class,'seds-button-primary') and contains(@class,'btn-next')]//span[normalize-space()='Submit']/parent::button")));
                                    submitButton.click();
                                    System.out.println("✅ Clicked 'Submit' button. Final URL: " + driver.getCurrentUrl());
                                } catch(Exception e) {
                                    System.out.println("❌ 'Submit' button not clicked for: " + jobTitle);
                                }
                                
                            } catch(Exception e) {
                                System.out.println("❌ 'Next' button not clicked for: " + jobTitle);
                            }
                            break;
                        } catch (StaleElementReferenceException | NoSuchElementException e) {
                            // Retry if element reference becomes stale or is not found
                        }
                        Thread.sleep(500);
                    }
                    if (!clicked) {
                        System.out.println("❌ 'Apply now/Easy apply' button not clicked for: " + jobTitle);
                    }
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error during apply process: " + e.getMessage());
        } finally {
//            driver.quit();
        }
    }
}
