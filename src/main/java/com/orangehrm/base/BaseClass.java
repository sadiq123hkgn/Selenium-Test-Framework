package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.asserts.SoftAssert;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;

public class BaseClass {

    protected static Properties prop;

    // ThreadLocal WebDriver and ActionDriver for parallel execution
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();

    protected ThreadLocal<SoftAssert> softAssert = ThreadLocal.withInitial(SoftAssert::new);

    public SoftAssert getSoftAssert() {
        return softAssert.get();
    }

    public static final Logger logger = LoggerManager.getLogger(BaseClass.class);

    // Load configuration before the test suite
    @BeforeSuite
    public void loadConfig() throws IOException {
        prop = new Properties();
        FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "/src/main/resources/config.properties");
        prop.load(fis);
        logger.info("config.properties file loaded");
        // ExtentManager.getReporter() is implemented in TestListener
    }

    // Setup WebDriver before each test method
    @BeforeMethod
    public synchronized void setup() {
        logger.info("Setting up WebDriver for: " + this.getClass().getSimpleName());
        launchBrowser();
        configureBrowser();
        staticWait(2);
        logger.info("WebDriver initialized and browser maximized");

        actionDriver.set(new ActionDriver(getDriver()));
        logger.info("ActionDriver initialized for thread: " + Thread.currentThread().getId());
    }

    // Launch browser based on config
    private synchronized void launchBrowser() {
        String browser = prop.getProperty("browser");
        try {
            switch (browser.toLowerCase()) {
                case "chrome":
                    driver.set(new ChromeDriver());
                    logger.info("ChromeDriver instance created");
                    break;
                case "firefox":
                    driver.set(new FirefoxDriver());
                    logger.info("FirefoxDriver instance created");
                    break;
                case "edge":
                    driver.set(new EdgeDriver());
                    logger.info("EdgeDriver instance created");
                    break;
                default:
                    throw new IllegalArgumentException("Browser not supported: " + browser);
            }
            ExtentManager.registerDriver(getDriver());
        } catch (Exception e) {
            logger.error("Failed to initialize browser: " + browser, e);
            throw new RuntimeException(e);
        }
    }

    // Configure browser settings
    private void configureBrowser() {
        int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        getDriver().manage().window().maximize();

        try {
            getDriver().get(prop.getProperty("url"));
        } catch (Exception e) {
            logger.error("Failed to navigate to URL: " + prop.getProperty("url"), e);
        }
    }

    // Tear down WebDriver after each test method
    @AfterMethod
    public synchronized void tearDown() {
        if (getDriver() != null) {
            try {
                getDriver().quit();
                logger.info("WebDriver instance closed for thread: " + Thread.currentThread().getId());
            } catch (Exception e) {
                logger.error("Unable to quit WebDriver: " + e.getMessage(), e);
            } finally {
                driver.remove();
                actionDriver.remove();
            }
        }
    }

    // Getter for Properties
    public static Properties getProp() {
        return prop;
    }

    // Getter for WebDriver
    public static WebDriver getDriver() {
        if (driver.get() == null) {
            throw new IllegalStateException("WebDriver is not initialized for thread: " + Thread.currentThread().getId());
        }
        return driver.get();
    }

    // Getter for ActionDriver
    public static ActionDriver getActionDriver() {
        if (actionDriver.get() == null) {
            throw new IllegalStateException("ActionDriver is not initialized for thread: " + Thread.currentThread().getId());
        }
        return actionDriver.get();
    }

    // Simple static wait in seconds
    public void staticWait(int seconds) {
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }
}
