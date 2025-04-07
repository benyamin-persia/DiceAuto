Below is an example of a complete README.md file for your project. You can copy this content into your repository’s README.md file.

---

```markdown
# Dice Job Automation

This project is a Java and Selenium automation tool that logs into Dice.com, scrapes job titles and detail links based on user-defined filters, saves the data to a CSV file, and then automates the apply process for each job. The automation includes clicking through shadow DOM elements, navigating multiple pages, and handling dynamic content with robust waiting mechanisms.

## Features

- **Login Automation:** Automatically logs into Dice.com using provided credentials.
- **Job Scraping:** Extracts job titles and detail URLs from Dice.com based on filters.
- **CSV Output:** Saves matching job titles and URLs to a CSV file for later review.
- **Automated Apply Process:** Navigates to each job detail page, clicks the "Apply now/Easy apply" button (using shadow DOM traversal), and then clicks the "Next" and "Submit" buttons to complete the process.
- **Customizable Filters:** Easily update the default search URL filters, username, and password in the code.

## Prerequisites

- **Java Development Kit (JDK):** Ensure you have JDK 8 or higher installed.
- **Chrome Browser:** The automation uses ChromeDriver, so you need the Google Chrome browser installed.
- **ChromeDriver v135:** Download the correct version of ChromeDriver that matches your Chrome version.

## Step-by-Step Setup

### 1. Download ChromeDriver v135 Manually

1. Go to the [Chrome for Testing](https://googlechromelabs.github.io/chrome-for-testing/) website.
2. Scroll to the section: **"Find a ChromeDriver for your version"**.
3. Enter **135.0.7049.42** in the search box and download the matching version for:
   - **Platform:** Windows
   - **Architecture:** x64
4. Extract the downloaded file. You should get a path similar to:
   ```
   C:\your-folder\chromedriver-win64\chromedriver.exe
   ```

### 2. Configure the Project

- **Update ChromeDriver Path:**  
  In the source code (e.g., `DiceJobLogin.java`), update the following line with your actual ChromeDriver path:
  ```java
  System.setProperty("webdriver.chrome.driver", "C:\\your-folder\\chromedriver-win64\\chromedriver.exe");
  ```
  
- **Update Credentials:**  
  Modify the following values as needed in the code:
  ```java
  emailInput.sendKeys("soyoxok649@avulos.com");
  passwordInput.sendKeys("!2q3wa4esZ");
  ```
  
- **Customize Default URL Filters:**  
  The job search URL is constructed with default filters. Adjust the query parameters (such as job type, location, etc.) in the `baseUrl` and `pageSizePart` strings if necessary:
  ```java
  String baseUrl = "https://www.dice.com/jobs?q=QA%20testing&location=United%20States"
                   + "&latitude=38.7945952&longitude=-106.5348379&countryCode=US&locationPrecision=Country"
                   + "&radius=30&radiusUnit=mi&page=";
  String pageSizePart = "&pageSize=1000&filters.easyApply=true&language=en&eid=8855";
  ```

- **Mandatory Keywords:**  
  After logging in, the program prompts you to enter mandatory keywords (e.g., `qa,selenium`) to filter the job titles. You can change this as needed.

## How to Run

1. **Compile and Run the Code:**  
   Use your favorite IDE (e.g., IntelliJ IDEA, Eclipse) or compile from the command line:
   ```bash
   javac -cp ".;path/to/selenium-java.jar;path/to/other/dependencies.jar" DiceJobLogin.java
   java -cp ".;path/to/selenium-java.jar;path/to/other/dependencies.jar" dice.DiceJobLogin
   ```
   Make sure all required Selenium libraries and dependencies are included in your classpath.

2. **Process Flow:**  
   - **Phase 1:** The program logs in to Dice.com, prompts you for mandatory keywords, scrapes job pages, and saves matching job titles and detail links to `job_titles.csv`.
   - **Phase 2:** After scraping, the program reads from the CSV file and navigates to each job detail page to execute the apply process:
     - Clicks the "Apply now/Easy apply" button (via shadow DOM).
     - Clicks the "Next" button.
     - Clicks the "Submit" button.
   - At each stage, the current URL is printed to the console for tracking.


## Troubleshooting

- **Apply Button Issues:**  
  If the "Apply now/Easy apply" button does not click, ensure that:
  - The shadow DOM structure has not changed.
  - The correct CSS selector (in this case, `.btn.btn-primary`) is used.
  - The page is fully loaded before the click action.

- **Waiting Mechanisms:**  
  The code uses explicit waits and retry loops to handle dynamic content. Adjust the wait times if necessary.

## Conclusion

This project automates the process of job searching and applying on Dice.com. Customize the credentials, search filters, and other settings as needed. For any questions or issues, please refer to the troubleshooting section or open an issue on GitHub.

Happy Automating!
```

---

Feel free to adjust any sections to better fit your project details.
