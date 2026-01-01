package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;

public class BaseClass {

    protected static Properties prop;
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();

    public static final Logger logger = LoggerManager.getLogger(BaseClass.class);
    protected ThreadLocal<SoftAssert> softAssert = ThreadLocal.withInitial(SoftAssert::new);

    public SoftAssert getSoftAssert() {
        return softAssert.get();
    }

    /* ================= LOAD CONFIG ================= */
    @BeforeSuite
    public void loadConfig() throws IOException {
        prop = new Properties();
        FileInputStream fis = new FileInputStream(
                System.getProperty("user.dir") + "/src/main/resources/config.properties");
        prop.load(fis);
        logger.info("config.properties loaded successfully");
    }

    /* ================= SETUP ================= */
    @BeforeMethod
    @Parameters("browser")
    public synchronized void setup(String browser) {
        launchBrowser(browser);
        configureBrowser();

        actionDriver.set(new ActionDriver(getDriver()));
        logger.info("Driver & ActionDriver initialized for thread: " + Thread.currentThread().getId());
    }

    /* ================= LAUNCH BROWSER ================= */
    private synchronized void launchBrowser(String browser) {

        boolean seleniumGrid = Boolean.parseBoolean(prop.getProperty("seleniumGrid"));
        String gridURL = prop.getProperty("gridURL");

        try {
            if (seleniumGrid) {

                if (browser.equalsIgnoreCase("chrome")) {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
                    driver.set(new RemoteWebDriver(new URL(gridURL), options));

                } else if (browser.equalsIgnoreCase("firefox")) {
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("-headless");
                    driver.set(new RemoteWebDriver(new URL(gridURL), options));

                } else if (browser.equalsIgnoreCase("edge")) {
                    EdgeOptions options = new EdgeOptions();
                    options.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                    driver.set(new RemoteWebDriver(new URL(gridURL), options));

                } else {
                    throw new IllegalArgumentException("Unsupported Browser: " + browser);
                }

                logger.info("RemoteWebDriver started on Selenium Grid");

            } else {

                if (browser.equalsIgnoreCase("chrome")) {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
                    driver.set(new ChromeDriver(options));

                } else if (browser.equalsIgnoreCase("firefox")) {
                    FirefoxOptions options = new FirefoxOptions();
                    options.addArguments("-headless");
                    driver.set(new FirefoxDriver(options));

                } else if (browser.equalsIgnoreCase("edge")) {
                    EdgeOptions options = new EdgeOptions();
                    options.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                    driver.set(new EdgeDriver(options));

                } else {
                    throw new IllegalArgumentException("Unsupported Browser: " + browser);
                }

                ExtentManager.registerDriver(getDriver());
                logger.info("Local WebDriver started");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    /* ================= CONFIGURE BROWSER ================= */
    private void configureBrowser() {

        int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        getDriver().manage().window().maximize();

        // SAME URL for Grid & Local
        getDriver().get(prop.getProperty("url"));
    }

    /* ================= TEARDOWN ================= */
    @AfterMethod
    public synchronized void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
            actionDriver.remove();
            logger.info("WebDriver closed successfully");
        }
    }

    /* ================= GETTERS ================= */
    public static WebDriver getDriver() {
        if (driver.get() == null) {
            throw new IllegalStateException("WebDriver not initialized");
        }
        return driver.get();
    }

    public static ActionDriver getActionDriver() {
        if (actionDriver.get() == null) {
            throw new IllegalStateException("ActionDriver not initialized");
        }
        return actionDriver.get();
    }

    public static Properties getProp() {
        return prop;
    }

    /* ================= STATIC WAIT ================= */
    public void staticWait(int seconds) {
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }
}
