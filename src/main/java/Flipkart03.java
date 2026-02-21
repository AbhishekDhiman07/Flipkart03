import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import java.io.File;
import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Flipkart03 {
    public static void main(String[] args) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/SortTest_" + timestamp + ".html");
        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);
        ExtentTest test = extent.createTest("Flipkart Sorting Verification: Low to High");

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            driver.manage().window().maximize();
            driver.get("https://www.flipkart.com");
            test.info("Navigated to Flipkart");

            // 1. Search for Laptops
            WebElement search = wait.until(ExpectedConditions.elementToBeClickable(By.name("q")));
            search.sendKeys("Laptops" + Keys.ENTER);
            test.info("Searching for Laptops");

            // 2. Click 'Price -- Low to High' Sort option
            WebElement lowToHighSort = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[text()='Price -- Low to High']")
            ));
            lowToHighSort.click();
            test.info("Applied 'Price -- Low to High' sort");
            
            // Wait for the sorting animation to finish
            Thread.sleep(4000); 

            // 3. IMPROVED PRICE XPATH: Targets any div containing the ₹ symbol
            
            List<WebElement> prices = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//div[contains(text(), '₹')]")
            ));

            // Extract and clean the first two prices
            String p1Text = prices.get(0).getText().replaceAll("[^0-9]", "");
            String p2Text = prices.get(1).getText().replaceAll("[^0-9]", "");
            
            if (p1Text.isEmpty() || p2Text.isEmpty()) {
                throw new Exception("Could not extract numerical price values.");
            }

            int price1 = Integer.parseInt(p1Text);
            int price2 = Integer.parseInt(p2Text);
            
            test.info("Price 1: ₹" + price1);
            test.info("Price 2: ₹" + price2);

            // 4. LOGICAL VERIFICATION & SCREENSHOT
            String screenPath = System.getProperty("user.dir") + "/screenshots/SortSuccess_" + timestamp + ".png";
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File(screenPath));

            if (price1 <= price2) {
                test.pass("Sorting is working correctly: ₹" + price1 + " <= ₹" + price2,
                    MediaEntityBuilder.createScreenCaptureFromPath(screenPath).build());
            } else {
                test.fail("Sorting failed: ₹" + price1 + " is greater than ₹" + price2,
                    MediaEntityBuilder.createScreenCaptureFromPath(screenPath).build());
            }

        } catch (Exception e) {
            test.fail("Error during sorting test: " + e.getMessage());
        } finally {
            driver.quit();
            extent.flush(); // This is essential to generate the file!
            System.out.println("Execution finished. Report: reports/SortTest_" + timestamp + ".html");
        }
    }
}