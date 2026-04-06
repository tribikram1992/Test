package com.tests;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import com.pages.LoginPage;
import com.report.ExtentReportNG;
import com.utils.TestData;

import io.github.bonigarcia.wdm.WebDriverManager;

/*
 * Developed by Baxter @Webtestops
 * Resource Name - Reddy Pramodh
 * Date: 11th March 2024
 * Last Code Checkin - 11th March 2024
 * This is Baxter Proprietary Framework - Don't use without any permissions
 * Modified By - Reddy Pramodh
 */

public class BaseTest {
    public static String environment = System.getProperty("environment");
    public static String browserName = System.getProperty("browser");
    TestData td = new TestData();

    private static ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<WebDriver>() {
        @Override
        protected WebDriver initialValue() {
            return createDriver();
        }
    };

    public static WebDriver getDriver() {
        return threadLocalDriver.get();
    }

    public static void setDriver(WebDriver driverInstance) {
        threadLocalDriver.set(driverInstance);
    }

    private static WebDriver createDriver() {
        WebDriver webDriver = null;

        // Initialize WebDriver based on the specified browser
        if (browserName.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            WebDriverManager.chromedriver().setup();
            if (browserName.contains("headless")) {
                options.addArguments("--headless");
            }
            webDriver = new ChromeDriver(options);
        } else if (browserName.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            webDriver = new FirefoxDriver();
        } else if (browserName.equalsIgnoreCase("edge")) {
            WebDriverManager.edgedriver().setup();
            webDriver = new EdgeDriver();
        }else {
            throw new IllegalArgumentException("Browser not supported: " + browserName);
        }

        // Set implicit wait and maximize window
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        webDriver.manage().window().maximize();
        return webDriver;
    }

    public LoginPage lp;
    
    @BeforeMethod(alwaysRun = true)
    public synchronized LoginPage launchBrowser() throws IOException {
        if (getDriver() == null) {
            setDriver(createDriver());
        }
        lp = new LoginPage();
        lp.navigateToRtsApplication();
        return lp;
    }

    public String getScreenshot(String testCaseName) throws IOException, AWTException {
    	Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    	BufferedImage screenFullImage = new Robot().createScreenCapture(screenRect);
    	String screenshotPath = System.getProperty("user.dir") + File.separator + "reports"+ExtentReportNG.time + File.separator + testCaseName + ".png";
    	ImageIO.write(screenFullImage, "png", new File(screenshotPath));
        return screenshotPath;
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver driverInstance = getDriver();
        if (driverInstance != null) {
            driverInstance.quit();
            threadLocalDriver.remove(); // Clean up ThreadLocal variable
        }
    }
}
