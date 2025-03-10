package com.mlcraft.radt.selenium;

import com.mlcraft.radt.selenium.pageobjects.LoginPage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;

import java.util.stream.Stream;

@Slf4j
@SpringBootTest
public class SwagLabsSeleniumTests {

    @Autowired
    @Lazy
    private WebDriver driver;

    private LoginPage loginPage;

    @BeforeEach
    public void setup() {
        this.loginPage = new LoginPage(this.driver);
    }

    @Test
    public void shouldLoginWithCorrectCredentials() {
        loginPage.login("standard_user", "secret_sauce");

        Assertions.assertEquals(loginPage.getCurrentUrl(), "https://www.saucedemo.com/inventory.html");
    }

    @ParameterizedTest
    @MethodSource("failLoginTestCases")
    public void shouldFailLoginWithMissingUsername(String username, String password, String errorMessage) {
        loginPage.login(username, password);

        Assertions.assertTrue(loginPage.hasErrorMessage());
        Assertions.assertEquals(loginPage.getErrorMessage(), errorMessage);
    }

    private static Stream<Arguments> failLoginTestCases() {
        return Stream.of(
            Arguments.of("", "secret_sauce", "Epic sadface: Username is required"),
            Arguments.of("standard_user", "", "Epic sadface: Password is required"),
            Arguments.of("bad_username", "bad_password", "Epic sadface: Username and password do not match any user in this service")
        );
    }
}
