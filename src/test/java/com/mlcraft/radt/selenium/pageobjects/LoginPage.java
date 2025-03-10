package com.mlcraft.radt.selenium.pageobjects;

import org.jspecify.annotations.Nullable;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

// page_url = https://www.saucedemo.com/
@Component
public class LoginPage {

    WebDriver driver;

    @FindBy(xpath = "//*[@id='user-name']")
    public WebElement inputUsername;

    @FindBy(xpath = "//*[@id='password']")
    public WebElement inputPassword;

    @FindBy(xpath = "//*[@id='login-button']")
    public WebElement inputLoginButton;

    @FindBy(xpath = "//div[contains(@class, 'error')]")
    public WebElement divErrorMessage;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        driver.get("https://www.saucedemo.com/");
    }

    public void login(String username, String password) {
        inputUsername.sendKeys(username);
        inputPassword.sendKeys(password);
        inputLoginButton.click();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean hasErrorMessage() {
        return divErrorMessage.isDisplayed();
    }

    public String getErrorMessage() {
        return divErrorMessage.getText();
    }
}