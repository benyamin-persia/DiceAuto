package apaosha.DiceAutomation;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DiceFilterActions {
    private static final Logger logger = LoggerFactory.getLogger(DiceFilterActions.class);

    public static void applyFilters(WebDriver driver, WebDriverWait wait, int postedDateOption,
                                    String empTypeInput, String employerTypeInput,
                                    String easyApplyChoice, String workAuthChoice) {
        try {
            // Posted Date filter (if not default)
            if (postedDateOption != 0) {
                String postedDateText = "";
                switch (postedDateOption) {
                    case 1: postedDateText = "Today"; break;
                    case 2: postedDateText = "Last 3 Days"; break;
                    case 3: postedDateText = "Last 7 Days"; break;
                    default: postedDateText = "Any Date"; break;
                }
                logger.info("Applying Posted Date filter: {}", postedDateText);
                By postedDateButtonXPath = By.xpath("//js-single-select-filter//button[contains(text(),'" + postedDateText + "')]");
                wait.until(ExpectedConditions.elementToBeClickable(postedDateButtonXPath));
                driver.findElement(postedDateButtonXPath).click();
            } else {
                logger.info("No Posted Date filter applied (default: Any Date).");
            }

            // Employment Type filter using number mapping
            if (!empTypeInput.isEmpty()) {
                String[] empTypeNumbers = empTypeInput.split(",");
                List<String> empTypesToApply = new ArrayList<>();
                for (String num : empTypeNumbers) {
                    switch (num.trim()) {
                        case "1": empTypesToApply.add("FULLTIME"); break;
                        case "2": empTypesToApply.add("PARTTIME"); break;
                        case "3": empTypesToApply.add("CONTRACTS"); break;
                        case "4": empTypesToApply.add("THIRD_PARTY"); break;
                        default: logger.warn("Invalid Employment Type option: {}", num);
                    }
                }
                for (String type : empTypesToApply) {
                    logger.info("Applying Employment Type filter: {}", type);
                    By empTypeButtonXPath = By.xpath("//li[@data-cy-value='" + type + "']//button");
                    wait.until(ExpectedConditions.elementToBeClickable(empTypeButtonXPath));
                    driver.findElement(empTypeButtonXPath).click();
                }
            } else {
                logger.info("No Employment Type filter applied (default: all).");
            }

            // Employer Type filter using number mapping
            if (!employerTypeInput.isEmpty()) {
                String[] employerTypeNumbers = employerTypeInput.split(",");
                List<String> employerTypesToApply = new ArrayList<>();
                for (String num : employerTypeNumbers) {
                    switch (num.trim()) {
                        case "1": employerTypesToApply.add("Direct Hire"); break;
                        case "2": employerTypesToApply.add("Recruiter"); break;
                        case "3": employerTypesToApply.add("Other"); break;
                        default: logger.warn("Invalid Employer Type option: {}", num);
                    }
                }
                for (String type : employerTypesToApply) {
                    logger.info("Applying Employer Type filter: {}", type);
                    By employerTypeButtonXPath = By.xpath("//li[@data-cy-value='" + type + "']//button");
                    wait.until(ExpectedConditions.elementToBeClickable(employerTypeButtonXPath));
                    driver.findElement(employerTypeButtonXPath).click();
                }
            } else {
                logger.info("No Employer Type filter applied (default: all).");
            }

            // Easy Apply filter (default Y)
            if (easyApplyChoice.equalsIgnoreCase("Y") || easyApplyChoice.isEmpty()) {
                logger.info("Applying Easy Apply filter.");
                By easyApplyButtonXPath = By.xpath("//button[@aria-label='Filter Search Results by Easy Apply']");
                wait.until(ExpectedConditions.elementToBeClickable(easyApplyButtonXPath));
                driver.findElement(easyApplyButtonXPath).click();
            } else {
                logger.info("Easy Apply filter not applied.");
            }

            // Work Authorization filter
            if (workAuthChoice.equalsIgnoreCase("Y")) {
                logger.info("Applying Work Authorization filter.");
                By workAuthButtonXPath = By.xpath("//button[@aria-label='Filter Search Results by Work Authorization']");
                wait.until(ExpectedConditions.elementToBeClickable(workAuthButtonXPath));
                driver.findElement(workAuthButtonXPath).click();
            } else {
                logger.info("Work Authorization filter not applied.");
            }

            // Click the Job Search button using JavaScript to ensure the click is performed
            try {
                logger.info("Waiting for the job search button to be visible.");
                By searchButtonXPath = By.xpath("//button[@data-testid='job-search-search-bar-search-button']");
                wait.until(ExpectedConditions.visibilityOfElementLocated(searchButtonXPath));
                WebElement searchButton = driver.findElement(searchButtonXPath);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", searchButton);
                Thread.sleep(1000);
                logger.info("Clicking the job search button using JavaScript.");
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", searchButton);
            } catch (Exception e) {
                logger.warn("Search button not found or not clickable. Continuing without clicking it.");
            }
        } catch (Exception e) {
            logger.error("Error during filter interaction: {}", e.getMessage(), e);
        }
    }

    public static List<JobData> extractJobData(WebDriver driver, WebDriverWait wait) {
        List<JobData> jobDataList = new ArrayList<>();
        try {
            logger.info("Waiting for job title elements to be visible.");
            By jobTitleLinksXPath = By.xpath("//a[@target='_self' and @data-cy='card-title-link']");
            wait.until(ExpectedConditions.visibilityOfElementLocated(jobTitleLinksXPath));
            List<WebElement> jobTitleElements = driver.findElements(jobTitleLinksXPath);
            for (WebElement elem : jobTitleElements) {
                String title = elem.getText().trim();
                String id = elem.getAttribute("id").trim();
                if (!title.isEmpty() && !id.isEmpty()) {
                    jobDataList.add(new JobData(title, id));
                }
            }
            logger.info("Extracted {} job titles.", jobDataList.size());
        } catch (Exception e) {
            logger.error("Error retrieving job titles: {}", e.getMessage(), e);
        }
        return jobDataList;
    }
    
    // Static inner class to hold job data
    public static class JobData {
        private String title;
        private String id;
        
        public JobData(String title, String id) {
            this.title = title;
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getId() {
            return id;
        }
    }
}
