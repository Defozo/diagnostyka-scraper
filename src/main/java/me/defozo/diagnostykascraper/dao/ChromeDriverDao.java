package me.defozo.diagnostykascraper.dao;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ChromeDriverDao {
    private final String CHROME_DRIVER_URL = System.getenv("CHROME_DRIVER_URL");
    private WebDriver chromeDriver;

    public WebDriver getChromeDriver() {
        return chromeDriver;
    }

    public ChromeDriverDao(String sharedDownloadsFolder) throws IOException {
        configureChromeDriver(sharedDownloadsFolder);
    }

    private void configureChromeDriver(String sharedDownloadsFolder) throws IOException {
        ChromeOptions options = getChromeOptions(sharedDownloadsFolder);
        if (CHROME_DRIVER_URL != null) {
            chromeDriver = new RemoteWebDriver(
                    new URL(CHROME_DRIVER_URL),
                    options);
        } else {
            chromeDriver = new ChromeDriver(options);
        }
        chromeDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    }

    private ChromeOptions getChromeOptions(String sharedDownloadsFolder) {
        HashMap<String,Object> chromePrefs = new HashMap<>();
        ChromeOptions options = new ChromeOptions()
                .setHeadless(true);
        chromePrefs.put("plugins.always_open_pdf_externally", true);
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", sharedDownloadsFolder.substring(0, sharedDownloadsFolder.length()-1));
        chromePrefs.put("browser.setDownloadBehavior", "allow");
        options.setExperimentalOption("prefs", chromePrefs);
        return options;
    }
}
