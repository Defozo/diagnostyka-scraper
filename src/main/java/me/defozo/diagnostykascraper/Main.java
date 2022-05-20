package me.defozo.diagnostykascraper;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    /** Application name. */
    private static final String APPLICATION_NAME = "Diagnostyka Scraper";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String SHARED_DOWNLOADS_FOLDER = System.getenv("SHARED_DOWNLOADS_FOLDER") + FileSystems.getDefault().getSeparator();
    private static final String PRIVATE_KEY_FROM_P12_FILE_PATH = System.getenv("PRIVATE_KEY_FROM_P12_FILE_PATH");
    private static final String SERVICE_ACCOUNT_ID = System.getenv("SERVICE_ACCOUNT_ID");
    private static final String DIAGNOSTYKA_USER_ID = System.getenv("DIAGNOSTYKA_USER_ID");
    private static final String DIAGNOSTYKA_USER_PASSWORD = System.getenv("DIAGNOSTYKA_USER_PASSWORD");
    private static final String GOOGLE_DRIVE_FOLDER_ID = System.getenv("GOOGLE_DRIVE_FOLDER_ID");
    private static final String CHROME_DRIVER_URL = System.getenv("CHROME_DRIVER_URL");
    private static final String DIAGNOSTYKA_WYNIKI_BASE_URL = "https://wyniki.diag.pl/";
    private static final String CSV_REG_EX = "wyniki.*";
    private static final String PDF_REG_EX = ".{4}_\\d+_\\d+_\\d+\\.pdf";

    private static WebDriver driver;
    static JavascriptExecutor js;

    static Drive driveService;

    public static void main(String[] args) throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredential getCredentials = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(SERVICE_ACCOUNT_ID)
                .setServiceAccountPrivateKeyFromP12File(new File(PRIVATE_KEY_FROM_P12_FILE_PATH))
                .setServiceAccountScopes(SCOPES)
                .build();
        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials)
                .setApplicationName(APPLICATION_NAME)
                .build();

        HashMap<String,Object> chromePrefs = new HashMap<>();
        ChromeOptions options = new ChromeOptions();
        chromePrefs.put("plugins.always_open_pdf_externally", true);
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", SHARED_DOWNLOADS_FOLDER.substring(0, SHARED_DOWNLOADS_FOLDER.length()-1));
        chromePrefs.put("browser.setDownloadBehavior", "allow");
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--headless");
        driver = new RemoteWebDriver(
                new URL(CHROME_DRIVER_URL),
                options);
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        js = (JavascriptExecutor) driver;

        downloadBloodTestResults();
        driver.quit();
    }

    public static void downloadBloodTestResults() throws Exception {
        driver.get(DIAGNOSTYKA_WYNIKI_BASE_URL);
        driver.findElement(By.id("Identifier")).sendKeys(DIAGNOSTYKA_USER_ID);
        driver.findElement(By.id("Password")).sendKeys(DIAGNOSTYKA_USER_PASSWORD);
        driver.findElement(By.id("loginBtn")).click();
        long currentPage = 1;
        long pagesCount = Long.parseLong(driver.findElement(By.id("paginationPageCount")).getText());
        while (currentPage <= pagesCount) {
            long currentItem = 1;
            long itemCount = driver.findElements(By.xpath("//*[@id='order-results-table']/div")).size();
            while (currentItem <= itemCount) {
                System.out.println("Page: " + currentPage + "/" + pagesCount + ", Item: " + currentItem + "/" + itemCount);
                WebElement scanEle = new WebDriverWait(driver, Duration.ofSeconds(50)).until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='order-results-table']/div[" + currentItem + "]/div[6]/div/a[2]")));
                Actions actions = new Actions(driver);
                actions.moveToElement(scanEle).click().build().perform();
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                if (driver.findElement(By.xpath("//div[@id='popup-hcv']/div/buttom")).isDisplayed()) {
                    scanEle = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='popup-hcv']/div/buttom")));
                    actions.moveToElement(scanEle).click().build().perform();
                }
                if (driver.findElement(By.xpath("//div[@id='popup-alt']/div/div/div/div[2]/p")).isDisplayed()) {
                    scanEle = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='popup-alt']/div/buttom")));
                    actions.moveToElement(scanEle).click().build().perform();
                }
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
                scanEle = new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.elementToBeClickable(By.id("exportToCsvAnchor")));
                actions.moveToElement(scanEle).click().build().perform();
                Thread.sleep(TimeUnit.SECONDS.toMillis(40));
                scanEle = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='documentsPanel']/div/div[3]/button/div/span")));
                actions.moveToElement(scanEle).click().build().perform();
                String createdOn = driver.findElement(By.id("Created")).getText();
                String orderId = driver.findElement(By.id("Barcode")).getText();
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));

                String newName = orderId + "_" + createdOn + ".csv";
                renameFileThatMatches(CSV_REG_EX, newName);
                uploadToGoogleDrive(new File(SHARED_DOWNLOADS_FOLDER + newName), GOOGLE_DRIVE_FOLDER_ID);

                newName = orderId + "_" + createdOn + ".pdf";
                renameFileThatMatches(PDF_REG_EX, newName);
                uploadToGoogleDrive(new File(SHARED_DOWNLOADS_FOLDER + newName), GOOGLE_DRIVE_FOLDER_ID);

                scanEle = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.id("backToOrdersList"))));
                actions.moveToElement(scanEle).click().build().perform();
                currentItem = (long)js.executeScript("var currentPage = \"" + currentPage + "\";var pagesCount = \"" + pagesCount + "\";var currentItem = \"" + currentItem + "\";var storedVars = { 'currentPage': currentPage,'pagesCount': pagesCount,'currentItem': currentItem }; return " + currentItem + "+1" + "");
            }
            if (currentPage < pagesCount) {
                driver.findElement(By.id("paginationNextButton")).click();
            }
            currentPage = (long)js.executeScript("var currentPage = \"" + currentPage + "\";var pagesCount = \"" + pagesCount + "\";var currentItem = \"" + currentItem + "\";var storedVars = { 'currentPage': currentPage,'pagesCount': pagesCount,'currentItem': currentItem }; return " + currentPage + "+1" + "");
        }
    }

    public static void renameFileThatMatches(String regEx, String newName) {
        File folder = new File(SHARED_DOWNLOADS_FOLDER);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().matches(regEx)) {
                        file.renameTo(new File(SHARED_DOWNLOADS_FOLDER + newName));
                    }
                }
            }
        }
    }

    public static void uploadToGoogleDrive(File file, String folderId) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(file.getName());
        java.io.File filePath = new java.io.File(file.getAbsolutePath());
        FileContent mediaContent = new FileContent(Files.probeContentType(Path.of(file.getAbsolutePath())), filePath);
        List<com.google.api.services.drive.model.File> filesInFolder = getFilesInFolder(folderId);
        boolean updated = false;
        for (com.google.api.services.drive.model.File fileInFolder : filesInFolder) {
            if (fileInFolder.getName().equals(file.getName())) {
                driveService.files().update(fileInFolder.getId(), fileMetadata, mediaContent).execute();
                updated = true;
            }
        }
        if (!updated) {
            fileMetadata.setParents(Collections.singletonList(folderId));
            driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
        }
    }

    public static List<com.google.api.services.drive.model.File> getFilesInFolder(String folderId){
        List<com.google.api.services.drive.model.File> result = new ArrayList<>();
        try{
            Drive.Files.List request = driveService.files().list().setQ("'" + folderId + "' in parents and trashed = false");
            do{
                try{
                    FileList files = request.execute();

                    result.addAll(files.getFiles());
                    request.setPageToken(files.getNextPageToken());
                }
                catch(IOException e){
                    System.out.println("An error occurred: " + e);
                    request.setPageToken(null);
                }
            }
            while(request.getPageToken() != null
                    && request.getPageToken().length() > 0);
        }
        catch(IOException e1){
            e1.printStackTrace();
        }
        return result;
    }


}
