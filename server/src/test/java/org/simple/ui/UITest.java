package org.simple.ui;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.simple.ui.pages.MainPage;
import org.simple.utils.RunServer;
import org.simple.utils.TestGroupUI;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.springframework.test.util.AssertionErrors.assertNotNull;


@RunServer
@TestGroupUI
public class UITest {

    private static String HUB_URL = "http://localhost:4444/wd/hub";

    private static boolean remoteWebDriver = false;

    @LocalServerPort
    private int randomServerPort;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    private static void setUpAll() throws IOException {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(HUB_URL);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                remoteWebDriver = response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            } catch (HttpHostConnectException e) {}
        }
    }

    @BeforeEach
    private void setUp() throws MalformedURLException {

        if (remoteWebDriver) {
            driver = new RemoteWebDriver(new URL(HUB_URL), DesiredCapabilities.chrome());
        } else {
            System.setProperty("webdriver.chrome.driver", "d:\\Work\\Projects\\chromedriver.exe");
            driver = new ChromeDriver();
        }

        wait = new WebDriverWait(driver, 5);
    }

    @AfterEach
    private void setDown() {
        driver.close();
    }

    @Test
    public void addTest() {
        final String TEST_VALUE = "test string";

        driver.get(String.format("http://localhost:%d/", randomServerPort));

        WebElement input = wait.until(presenceOfElementLocated(By.className("new-todo-label")));
        input.sendKeys(TEST_VALUE + Keys.ENTER);

        List<WebElement> labelList = wait.until(presenceOfAllElementsLocatedBy(By.className("todo-list-item-label")));
        assertTrue(labelList.stream().anyMatch(e -> TEST_VALUE.equals(e.getText())), "added string not found");
    }

    @Test
    public void deleteTest() {
        WebDriver driver = new ChromeDriver();
        MainPage mainPage = new MainPage(driver);
        try {
            driver.get("http://localhost:3000/");

            int todoListSize = mainPage.getAllTodoItems().size();
            Assert.assertTrue("There are nothing to delete in the List", todoListSize > 0);

            mainPage.deleteFirstTodo();
            assertEquals(todoListSize - 1, mainPage.getAllTodoItems().size());
        } finally {
            driver.close();
        }
    }

    @Test
    public void editTest() {
        WebDriver driver = new ChromeDriver();
        MainPage mainPage = new MainPage(driver);
        try {
            driver.get("http://localhost:3000/");

            List<WebElement> todoList = mainPage.getAllTodoItems();
            Assert.assertTrue("There are nothing to edit in the List", todoList.size() > 0);

            mainPage.editFirstTodo(todoList);
            assertEquals(mainPage.getFirstElement().getText(), mainPage.EDIT_VALUE);
        } finally {
            driver.close();
        }
    }
}
