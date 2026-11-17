
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.remote.http.Filter;
import org.openqa.selenium.remote.http.HttpResponse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class SeleniumTest {
    WebDriver driver;
    final String URL = "https://old.vyatsu.ru";
    final String URL2 = "https://otzovik.com";
    final String URL3 = "https://m.cdsvyatka.com";
    final List<String> ALLOWED_DOMAINS = Arrays.asList(
            "vyatsu.ru",
            "otzovik.com",
            "cdsvyatka.com"
    );
    public void takeScreenshot(String screenshotName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            File dir = new File("screenshots");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File destination = new File(dir, screenshotName + ".png");
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Скриншот сохранен");
        } catch (Exception e) {
            System.out.println("Не удалось сделать скриншот: " + e.getMessage());
        }
    }
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            takeScreenshot("FAILED_" + description.getMethodName());
        }
    };
    @Before
    public void setup(){
        driver = new ChromeDriver();
        NetworkInterceptor interceptor = new NetworkInterceptor(
                driver,
                (Filter) next -> req -> {
                    String url = req.getUri();

                    if (url != null && (
                            url.contains("mc.yandex.ru") ||
                                    url.contains("google-analytics.com") ||
                                    url.contains("googletagmanager.com") ||
                                    url.contains("vk.com/rtrg")
                    )) {
                        return new HttpResponse();
                    }
                    return next.execute(req);
                }
        );
    }
    @Test
    public void task1script1(){
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement firstLi =
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@id='jsddm']/li[1]")));
        Actions action = new Actions(driver);
        action.moveToElement(firstLi).perform();
        WebElement targetMenuItem =
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//ul/li//a[contains(text(),'Образовательные стандарты')]") ));
        targetMenuItem.click();
        wait.until(ExpectedConditions.urlToBe("https://new.vyatsu.ru/sveden/eduStandarts/"));
        takeScreenshot("tas1script1");
    }
    @Test
    public void task1script2(){
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='query']")
        ));
        searchInput.sendKeys("отпуск");
        searchInput.sendKeys(Keys.ENTER);
        try {
            List<WebElement> searchResults = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath("//div/h4//a")));
            Assert.assertFalse("Результаты поиска пусты", searchResults.isEmpty());
            System.out.println("Найдено результатов: " + searchResults.size());
            takeScreenshot("task1script2");
        } catch (org.openqa.selenium.TimeoutException e) {
            Assert.fail("Нет результатов поиска");
        }
    }
    @Test
    public void task1Script3(){
        driver.get(URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement studentLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div//ul/li//a/h3[contains(text(), 'Студенту')]")));
        studentLink.click();
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@id='email-input']")));
        emailInput.clear();
        emailInput.sendKeys("invalid-email");
        emailInput.sendKeys(Keys.ENTER);
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        System.out.println("Результат для некорректного email: " + alertText);
        alert.accept();
        Assert.assertTrue("Нет успеха", alertText.toLowerCase().contains("неправильно"));
        takeScreenshot("task1script3wrong");

        emailInput.clear();
        emailInput.sendKeys("test@vyatsu.ru");
        emailInput.sendKeys(Keys.ENTER);
        Alert alert2 = wait.until(ExpectedConditions.alertIsPresent());
        String alert2Text = alert2.getText();
        System.out.println("Результат для корретного email: " + alert2Text);
        alert.accept();
        Assert.assertTrue("Нет успеха", alert2Text.toLowerCase().contains("успешно") || alert2Text.toLowerCase().contains("находится в базе данных"));
        takeScreenshot("task1script3correct");
    }
    @Test
    public void task2script1(){
        driver.get(URL2);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id='header-search-input']")));
        searchInput.sendKeys("яндекс");
        WebElement metroSuggestion = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Yandex Metro - программа для Android')]")));
        metroSuggestion.click();
        wait.until(ExpectedConditions.urlContains("yandex_metro"));
        wait.until(ExpectedConditions.urlToBe("https://otzovik.com/reviews/yandex_metro-karti_metro_ot_yandeks_dlya_android/"));
        List<WebElement> reviews = driver.findElements(
                By.xpath("//div/a/span[contains(@class, 'counter')]"));
        Assert.assertFalse("Список отзывов пуст", reviews.isEmpty());
        takeScreenshot("task2script1");
    }
    @Test
    public void task2script2(){
        driver.get(URL2);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement writeReviewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a/span[contains(text(), 'Написать отзыв')]")));
        writeReviewBtn.click();
        WebElement objectSearchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id = 'tproduct']")));
        objectSearchInput.sendKeys("Ботанический сад в Кирове");
        WebElement kirovGarden = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@class, 'popup-obj') and contains(., 'ВятГУ (Россия, Киров)')]")));
        kirovGarden.click();
        WebElement ratingStar = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//select[@id = 'rating0']/option[@value='5']")));
        ratingStar.click();
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@id ='content_title']")));
        titleInput.sendKeys("Тестовый заголовок для проверки");
        WebElement previewBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@id = 'previewbtn']")));
        previewBtn.click();

        WebElement errorDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class, 'dia-body')]")));
        String dialogText = errorDialog.getText();
        System.out.println("Текст модального окна:\n" + dialogText);
        Assert.assertTrue("В модальном окне нет текста об ошибке", dialogText.contains("ошибк"));
        WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class, 'dia-footer')]//button[contains(text(), 'OK')]")));
        okButton.click();
        takeScreenshot("task2script2");
    }
    @Test
    public void task3script1(){
        driver.get(URL3);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement citySelectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id, 'id_CitySelect')]/option[@value='1']")));
        citySelectElement.click();
        WebElement routeSelectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id, 'id_MarshSelect')]/option[@value='1002']")));
        routeSelectElement.click();
        WebElement busMap = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[contains(@type, 'button') and contains(@id, 'id_buttonMap')]")));
        busMap.click();
        List<WebElement> busMarkers = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("canvas.leaflet-canvas-icon")));
        Assert.assertFalse("На карте нет ни одного автобуса", busMarkers.isEmpty());
        WebElement firstBus = busMarkers.get(0);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstBus);
        WebElement popupContent = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".leaflet-popup-content")));
        String busInfo = popupContent.getText();
        System.out.println("Информация об автобусе:\n" + busInfo);
        Assert.assertTrue("В информации нет слова Автобус", busInfo.contains("Автобус"));
        Assert.assertTrue("В информации нет слова Скорость", busInfo.contains("Скорость"));
        Assert.assertTrue("В информации нет слова Модель", busInfo.contains("Модель"));
        takeScreenshot("task3script1");
    }
    @Test
    public void task3script2(){
        driver.get(URL3);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement citySelectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id, 'id_CitySelect')]/option[@value='1']")));
        citySelectElement.click();
        WebElement routeSelectElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id, 'id_MarshSelect')]/option[@value='1002']")));
        routeSelectElement.click();
        WebElement selectBusStation = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//select[contains(@id, 'id_StopSelect')]/option[@value='119']")));
        selectBusStation.click();
        WebElement busProg = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[contains(@type, 'button') and contains(@id, 'id_buttonPrediction')]")));
        busProg.click();
        wait.until(ExpectedConditions.urlToBe("https://m.cdsvyatka.com/prediction.php?CitySelect=1&MarshSelect=1002&BusSelect=-1&StopSelect=119&allmarshes_for_busstop=0"));
        WebElement minutesCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.className("minutes")));
        String waitingTime = minutesCell.getText().replace('\u00A0', ' ').trim();
        System.out.println("Время ожидания: " + waitingTime);
        Assert.assertFalse("Время ожидания не отображается", waitingTime.isEmpty());
        Assert.assertTrue("Время ожидания не содержит минут", waitingTime.contains("мин"));
        WebElement busNumberCell = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//table//tr[2]/td[4]")));
        String busNumber = busNumberCell.getText().replace('\u00A0', ' ').trim();
        System.out.println("Номер автобуса: " + busNumber);
        Assert.assertFalse("Номер автобуса не отображается", busNumber.isEmpty());
        takeScreenshot("task3script2");
    }
    @After
    public void quit(){
        driver.quit();
    }

}
