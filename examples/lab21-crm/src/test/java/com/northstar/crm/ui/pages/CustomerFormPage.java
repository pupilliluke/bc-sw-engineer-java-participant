package com.northstar.crm.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/** Page Object — locate via data-testid only. */
public class CustomerFormPage {

    private static final By CUSTOMER_ID = By.cssSelector("[data-testid='customer-id']");
    private static final By FULL_NAME = By.cssSelector("[data-testid='full-name']");
    private static final By EMAIL = By.cssSelector("[data-testid='email']");
    private static final By STATUS = By.cssSelector("[data-testid='status']");
    private static final By SUBMIT = By.cssSelector("[data-testid='submit-customer']");
    private static final By RESULT = By.cssSelector("[data-testid='create-result']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CustomerFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public CustomerFormPage open(String baseUrl) {
        driver.get(baseUrl + "/customers.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(CUSTOMER_ID));
        return this;
    }

    public void fill(String id, String name, String email, String status) {
        type(CUSTOMER_ID, id);
        type(FULL_NAME, name);
        type(EMAIL, email);
        type(STATUS, status);
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT)).click();
    }

    public String resultText() {
        wait.until(driven -> !driven.findElement(RESULT).getText().isBlank());
        return driver.findElement(RESULT).getText();
    }

    private void type(By locator, String text) {
        WebElement element = driver.findElement(locator);
        element.clear();
        if (!text.isEmpty()) {
            element.sendKeys(text);
        }
    }
}
