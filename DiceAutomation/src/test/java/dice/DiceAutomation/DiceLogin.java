package dice.DiceAutomation;

import com.microsoft.playwright.*;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Page.WaitForSelectorOptions;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.Browser.NewContextOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class DiceLogin {
    public static void main(String[] args) throws InterruptedException, IOException {
        // --- prompt for keywords ---
        System.out.println("Enter your job keywords, separated by commas (defaulting in 5 seconds):");
        Scanner scanner = new Scanner(System.in);
        ExecutorService inputExecKeywords = Executors.newSingleThreadExecutor();
        Future<String> lineFuture = inputExecKeywords.submit(() -> scanner.nextLine());
        String line;
        try {
            line = lineFuture.get(5, TimeUnit.SECONDS);
            if (line.trim().isEmpty()) {
                line = "web scraping,Selenium,qa tester,web automation,playwright,sdet,qa test,Automation tester,user acceptance testing,tosca,istqb,JUnit qa,TestNG,Cucumber,BDD,TDD,API Testing,Performance Testing,Load Testing,Stress Testing,Integration Testing,Smoke Testing,Sanity Testing,Regression Testing,Unit Testing,System Testing,Test Case qa,quality assurance software";
            }
        } catch (Exception e) {
                    line = "qa tester";

            // line = "qa test,qa web,web scraping,tosca,istqb,JUnit qa,TestNG,Cucumber,BDD,TDD,API Testing,Performance Testing,Load Testing,Stress Testing,Integration Testing,Smoke Testing,Sanity Testing,Regression Testing,Unit Testing,System Testing,Test Case qa,Defect Tracking,JIRA,Git,Jenkins,CI/CD qa,Cypress,SoapUI,Postman,Accessibility Testing,Agile Testing,Scrum,Test Data Management,quality assurance software,Continuous Testing,Selenium,qa tester,web automation,playwright,sdet,Automation tester,user acceptance testing";
        }
        
        inputExecKeywords.shutdownNow();
        String[] keywords = Arrays.stream(line.split(","))
                                  .map(String::trim)
                                  .filter(s -> !s.isEmpty())
                                  .toArray(String[]::new);

        // --- setup logger ---
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        BufferedWriter logWriter = new BufferedWriter(new FileWriter("login_steps.log", true));
        Consumer<String> log = msg -> {
            String entry = "[" + LocalDateTime.now().format(dtf) + "] " + msg;
            System.out.println(entry);
            try {
                logWriter.write(entry);
                logWriter.newLine();
                logWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // --- initialize Playwright once ---
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
        Random rnd = new Random();
        // Randomize user agent occasionally
        List<String> userAgents = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.1 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1"
        );
        String currentUserAgent = userAgents.get(rnd.nextInt(userAgents.size()));
        BrowserContext context = browser.newContext(new NewContextOptions().setUserAgent(currentUserAgent));
        Page page = context.newPage();

        // --- increase default navigation timeout to 60 seconds ---
        page.setDefaultNavigationTimeout(60_000);

        // --- login (unchanged) ---
        boolean loggedIn = false;
        while (!loggedIn) {
            log.accept("Navigating to login page");
            try {
                page.navigate("https://www.dice.com/dashboard/login",
                    new NavigateOptions()
                        .setWaitUntil(WaitUntilState.LOAD)
                        .setTimeout(45_000)
                );
            } catch (PlaywrightException e) {
                if (!e.getMessage().contains("net::ERR_ABORTED")) throw e;
                log.accept("Ignored ERR_ABORTED");
            }

            try {
                page.waitForSelector("input[name=\"q\"]",
                    new WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(3_000)
                );
                loggedIn = true;
                log.accept("Already logged in");
                break;
            } catch (TimeoutError ignored) { }

            log.accept("Filling email");
            Locator emailInput = page.locator("input[name=\"email\"]");
            emailInput.type("benyamin.mohamadalizadeh61@gmail.com");
            Thread.sleep(50);
            emailInput.press("Enter");

            log.accept("Waiting for password");
            page.waitForSelector("input[name=\"password\"]", new WaitForSelectorOptions().setTimeout(5_000));
            Locator passwordInput = page.locator("input[name=\"password\"]");
            passwordInput.type("EvCqcJn!PDYZ_89");
            Thread.sleep(50);
            passwordInput.press("Enter");

            try {
                page.waitForSelector("input[name=\"q\"]",
                    new WaitForSelectorOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(10_000)
                );
                loggedIn = true;
                log.accept("Login successful");
            } catch (TimeoutError e) {
                log.accept("Login failed, retrying");
            }
        }

        BufferedWriter failedWriter = new BufferedWriter(new FileWriter("failed_jobs.csv", true));

        // --- loop across each keyword ---
        for (String keyword : keywords) {
            String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            // List of all U.S. states
            List<String> states = List.of(

    "Maryland","Virginia","dc"
    // ,
    //  "Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia",
    //             "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", 
    //             "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey",
    //             "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina",
    //             "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Washington", "West Virginia", "Wisconsin", "Wyoming"
            );

            // Generate URLs for each state
            List<String> baseUrls = new ArrayList<>();
            for (String state : states) {
                String encodedState = state.replace(" ", "+");
                // baseUrls.add("https://www.dice.com/jobs?filters.easyApply=true&location=" + encodedState + "%2C+USA&q=" + encoded);
                baseUrls.add("https://www.dice.com/jobs?filters.workplaceTypes=Remote&q=qa tester");
            }

            for (String baseUrl : baseUrls) {
                // collect links
                try {
                    page.navigate(baseUrl + "&page=1",
                        new NavigateOptions()
                            .setWaitUntil(WaitUntilState.LOAD)
                            .setTimeout(45_000)
                    );
                } catch (PlaywrightException e) {
                    log.accept("Error loading search for keyword '" + keyword + "': " + e.getMessage() + "; skipping keyword.");
                    continue;  // skip this keyword and move to the next
                }
                log.accept("=== Starting for keyword: " + keyword + " ===");

                // get total results
                Locator countLoc = page.locator("p:has-text(\"results\")").nth(1);
                countLoc.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE).setTimeout(15_000)
                );
                String countText = countLoc.textContent().trim();
                log.accept("Count: " + countText);
                Matcher m = Pattern.compile("(\\d+) results").matcher(countText);
                int total = m.find() ? Integer.parseInt(m.group(1)) : 0;
                int pages = (total + 19) / 20;
                log.accept("Total pages: " + pages);

                List<String> jobLinks = new ArrayList<>();
                for (int p = 1; p <= pages; p++) {
                    String url = baseUrl + "&page=" + p;
                    log.accept("Page " + p + ": " + url);
                    Thread.sleep(500 + rnd.nextInt(1500)); // random delay to mimic human browsing
                    try {
                        page.navigate(url,
                            new NavigateOptions()
                                .setWaitUntil(WaitUntilState.LOAD)
                                .setTimeout(45_000)
                        );
                    } catch (PlaywrightException e) {
                        log.accept("Error loading page " + p + ": " + e.getMessage() + "; skipping this page.");
                        continue;  // skip this page
                    }
                    Locator links = page.locator("a[data-testid=\"job-search-job-detail-link\"]");
                    links.first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(15_000)
                    );
                    int count = links.count();
                    log.accept("Found links: " + count);
                    for (int i = 0; i < count; i++) {
                        jobLinks.add(links.nth(i).getAttribute("href"));
                        log.accept("Collected: " + links.nth(i).getAttribute("href"));
                    }
                }

                // apply loop
                log.accept("Total collected links: " + jobLinks.size());
                for (String jobUrl : jobLinks) {
                    log.accept("Processing apply for: " + jobUrl);
                    Thread.sleep(100);
                    try {
                        page.navigate(jobUrl,
                            new NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60_000)
                        );
                    } catch (PlaywrightException e) {
                        log.accept("Error navigating to job '" + jobUrl + "': " + e.getMessage() + "; skipping job.");
                        failedWriter.write(jobUrl);
                        failedWriter.newLine();
                        failedWriter.flush();
                        continue;  // skip this job
                    }

                    // Simulate human-like interactions
                    // Random scroll
                    page.mouse().wheel(0, rnd.nextInt(125));
                    Thread.sleep(10);
                    // Random click on a non-interactive element
                    page.mouse().click(rnd.nextInt(200), rnd.nextInt(150));
                    Thread.sleep(10);
                    // Random hover
                    page.hover("body");
                    Thread.sleep(10);

                    // shadow-root click
                    // small random pause before attempting to click Apply
                    Thread.sleep(10);
                    boolean clicked = false;
                    long dead = System.currentTimeMillis() + 15_000;
                    while (!clicked && System.currentTimeMillis() < dead) {
                        try {
                            page.waitForSelector("apply-button-wc.hydrated",
                                new WaitForSelectorOptions().setTimeout(2_000)
                            );
                            // random delay to simulate human reaction
                            Thread.sleep(10);
                            page.locator("apply-button-wc.hydrated").evaluate(
                                "host => {" +
                                  "const btn = host.shadowRoot.querySelector('.btn.btn-primary');" +
                                  "if (btn) { btn.scrollIntoView(); btn.click(); }" +
                                "}"
                            );
                            // brief pause after clicking
                            Thread.sleep(10);
                            log.accept("Clicked Apply");
                            clicked = true;
                        } catch (Exception e) { /* retry */ }
                        Thread.sleep(10);
                    }
                    if (!clicked) {
                        log.accept("Apply button not clicked for " + jobUrl + "; skipping job.");
                        failedWriter.write(jobUrl);
                        failedWriter.newLine();
                        failedWriter.flush();
                        continue;
                    }

                    // Next
                    try {
                        page.locator("button.seds-button-primary.btn-next")
                            .click(new Locator.ClickOptions().setTimeout(5_000));
                        log.accept("Clicked Next");
                        Thread.sleep(10);
                    } catch (Exception e) {
                        log.accept("Next not clicked");
                    }

                    // Submit
                    try {
                        page.locator("button:has(span:text(\"Submit\"))")
                            .click(new Locator.ClickOptions().setTimeout(5_000));
                        log.accept("Clicked Submit");
                        Thread.sleep(10);
                    } catch (Exception e) {
                        log.accept("Submit not clicked");
                    }
                }
            } // end for baseUrl
        } // end for keyword loop
        failedWriter.close();
        log.accept("All done!");
        playwright.close();
    }
}
