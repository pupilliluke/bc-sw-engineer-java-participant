package com.northstar.crm.ui;

import com.northstar.crm.ui.pages.CustomerFormPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium journeys against the running app on the random port. Headless
 * Chrome per test, explicit waits only, implicit wait pinned to zero. A wait
 * that times out leaves target/ui-failure.png behind before the test fails.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerUiIT {

    @LocalServerPort
    int port;

    WebDriver driver;

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    }

    @AfterEach
    void quit() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void createCustomerViaUi() {
        CustomerFormPage page = new CustomerFormPage(driver).open(baseUrl());
        page.fill("CUS-2001", "Priya Patel", "priya.patel@example.com", "PROSPECT");
        page.submit();

        String result = resultText(page);

        assertTrue(result.contains("CUS-2001"), result);
        assertTrue(result.contains("Priya Patel"), result);
        assertTrue(result.contains("PROSPECT"), result);
    }

    @Test
    void blankNameShowsValidationMessage() {
        CustomerFormPage page = new CustomerFormPage(driver).open(baseUrl());
        page.fill("CUS-1002", "", "ravi.singh@example.com", "PROSPECT");
        page.submit();

        String result = resultText(page);

        assertTrue(result.contains("fullName"), result);
        assertFalse(result.contains("\"id\":\"CUS-1002\""),
                "a rejected create must not render a created customer");
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private String resultText(CustomerFormPage page) {
        try {
            return page.resultText();
        } catch (TimeoutException ex) {
            captureScreenshot();
            throw ex;
        }
    }

    private void captureScreenshot() {
        try {
            Files.write(Path.of("target/ui-failure.png"),
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
