package com.northstar.crm.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/** Page Object — locate via data-testid only. */
public class CustomerFormPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public CustomerFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public CustomerFormPage open(String baseUrl) {
        // TODO: driver.get(baseUrl + "/customers.html")
        throw new UnsupportedOperationException("TODO: open");
    }

    public void fill(String id, String name, String email, String status) {
        // TODO: clear/sendKeys on data-testid fields
        throw new UnsupportedOperationException("TODO: fill");
    }

    public void submit() {
        // TODO: click data-testid=submit-customer
        throw new UnsupportedOperationException("TODO: submit");
    }

    public String resultText() {
        // TODO: wait until create-result is non-empty; return text
        throw new UnsupportedOperationException("TODO: resultText");
    }
}
