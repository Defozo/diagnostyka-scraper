package me.defozo.diagnostykascraper;

import me.defozo.diagnostykascraper.dao.ChromeDriverDao;
import me.defozo.diagnostykascraper.dao.GoogleDriveDao;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DiagnostykaService {
    private static final String GOOGLE_DRIVE_FOLDER_ID = System.getenv("GOOGLE_DRIVE_FOLDER_ID");
    private static final String DIAGNOSTYKA_USER_ID = System.getenv("DIAGNOSTYKA_USER_ID");
    private static final String DIAGNOSTYKA_USER_PASSWORD = System.getenv("DIAGNOSTYKA_USER_PASSWORD");
    private static final String DIAGNOSTYKA_WYNIKI_BASE_URL = "https://wyniki.diag.pl/";
    private static final String CSV_REG_EX = "wyniki.*";
    private static final String PDF_REG_EX = ".{4}_\\d+_\\d+_\\d+\\.pdf";

    private final GoogleDriveDao googleDriveDao;
    private final ChromeDriverDao chromeDriverDao;
    private final String sharedDownloadsFolder;

    public DiagnostykaService(GoogleDriveDao googleDriveDao, ChromeDriverDao chromeDriverDao, String sharedDownloadsFolder) {
        this.googleDriveDao = googleDriveDao;
        this.chromeDriverDao = chromeDriverDao;
        this.sharedDownloadsFolder = sharedDownloadsFolder;
    }

    public void downloadFromDiagnostykaWebsiteAndUploadToGoogleDriveAllTestResults() throws Exception {
        chromeDriverDao.getChromeDriver().get(DIAGNOSTYKA_WYNIKI_BASE_URL);
        Actions actions = new Actions(chromeDriverDao.getChromeDriver());
        logInDiagnostykaWynikiWebsite();
        long pageCount = Long.parseLong(chromeDriverDao.getChromeDriver().findElement(By.id("paginationPageCount")).getText());
        for (long currentPage = 1; currentPage <= pageCount; currentPage++) {
            long orderCount = chromeDriverDao.getChromeDriver().findElements(By.xpath("//*[@id='order-results-table']/div")).size();
            for (long currentOrder = 1; currentOrder <= orderCount; currentOrder++) {
                System.out.println("Page: " + currentPage + "/" + pageCount + ", Item: " + currentOrder + "/" + orderCount);
                getResultsFromOrder(actions, currentOrder, googleDriveDao);
                clickOn(10, By.id("backToOrdersList"), actions);
            }
            if (currentPage < pageCount) {
                clickOn(10, By.id("paginationNextButton"), actions);
            }
        }
    }

    private void getResultsFromOrder(Actions actions, long currentItem, GoogleDriveDao googleDriveDao) throws InterruptedException, IOException {
        openResultsPage(currentItem, actions);
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        exitPopUps(actions);
        downloadTestResults(actions);
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        renameFileToBeMoreDescriptiveOfTheCurrentResultAndUploadItToGoogleDrive("csv", CSV_REG_EX, googleDriveDao);
        renameFileToBeMoreDescriptiveOfTheCurrentResultAndUploadItToGoogleDrive("pdf", PDF_REG_EX, googleDriveDao);
    }

    private void renameFileToBeMoreDescriptiveOfTheCurrentResultAndUploadItToGoogleDrive(String fileExtension, String fileRegEx, GoogleDriveDao googleDriveDao) throws IOException {
        String newName = getNewNameOfTheFile(fileExtension);
        renameFileThatMatches(fileRegEx, newName);
        googleDriveDao.uploadToGoogleDrive(new File(sharedDownloadsFolder + newName), GOOGLE_DRIVE_FOLDER_ID);
    }

    private String getNewNameOfTheFile(String extension) {
        String createdOn = chromeDriverDao.getChromeDriver().findElement(By.id("Created")).getText();
        String orderId = chromeDriverDao.getChromeDriver().findElement(By.id("Barcode")).getText();
        return orderId + "_" + createdOn + "." + extension;
    }

    private void openResultsPage(long currentItem, Actions actions) {
        clickOn(50, By.xpath("//div[@id='order-results-table']/div[" + currentItem + "]/div[6]/div/a[2]"), actions);
    }

    private void clickOn(int timeoutInSeconds, By elementToBeClicked, Actions actions) {
        WebElement scanEle = new WebDriverWait(chromeDriverDao.getChromeDriver(), Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable(elementToBeClicked));
        actions.moveToElement(scanEle).click().build().perform();
    }

    private void exitPopUps(Actions actions) {
        exitPopUp("//div[@id='popup-hcv']/div/buttom", "//div[@id='popup-hcv']/div/buttom", actions);
        exitPopUp("//div[@id='popup-alt']/div/div/div/div[2]/p", "//div[@id='popup-alt']/div/buttom", actions);
    }

    private void downloadTestResults(Actions actions) throws InterruptedException {
        downloadTestResultAsCsv(actions);
        downloadTestResultAsPdf(actions);
    }

    private void downloadTestResultAsPdf(Actions actions) throws InterruptedException {
        downloadTestResult(40, By.xpath("//div[@id='documentsPanel']/div/div[3]/button/div/span"), actions);
    }

    private void downloadTestResultAsCsv(Actions actions) throws InterruptedException {
        downloadTestResult(30, By.id("exportToCsvAnchor"), actions);
    }

    private void exitPopUp(String xpathExpression, String xpathExpression1, Actions actions) {
        if (chromeDriverDao.getChromeDriver().findElement(By.xpath(xpathExpression)).isDisplayed()) {
            clickOn(10, By.xpath(xpathExpression1), actions);
        }
    }

    private void downloadTestResult(int waitingTimeBeforeDownloading, By elementId, Actions actions) throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(waitingTimeBeforeDownloading));
        clickOn(waitingTimeBeforeDownloading, elementId, actions);
    }

    private void logInDiagnostykaWynikiWebsite() {
        chromeDriverDao.getChromeDriver().findElement(By.id("Identifier")).sendKeys(DIAGNOSTYKA_USER_ID);
        chromeDriverDao.getChromeDriver().findElement(By.id("Password")).sendKeys(DIAGNOSTYKA_USER_PASSWORD);
        chromeDriverDao.getChromeDriver().findElement(By.id("loginBtn")).click();
    }

    private void renameFileThatMatches(String regEx, String newName) {
        File folder = new File(sharedDownloadsFolder);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().matches(regEx)) {
                        file.renameTo(new File(sharedDownloadsFolder + newName));
                    }
                }
            }
        }
    }
}
