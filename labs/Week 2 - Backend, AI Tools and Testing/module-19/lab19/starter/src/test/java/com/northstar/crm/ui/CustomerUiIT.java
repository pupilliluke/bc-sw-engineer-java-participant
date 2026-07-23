package com.northstar.crm.ui;

import com.northstar.crm.ui.pages.CustomerFormPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerUiIT {

    @LocalServerPort
    int port;

    WebDriver driver;

    @BeforeAll
    static void setupDriver() {
        // TODO: WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        // TODO: ChromeOptions headless; driver = new ChromeDriver(options)
        throw new UnsupportedOperationException("TODO: Chrome session");
    }

    @AfterEach
    void quit() {
        // TODO: if (driver != null) driver.quit();
    }

    @Test
    void createCustomerViaUi() {
        // TODO: Page Object open → fill CUS-2001 → submit → assert result contains CUS-2001
        throw new UnsupportedOperationException("TODO: UI happy path");
    }
}
