package me.defozo.diagnostykascraper;

import me.defozo.diagnostykascraper.dao.ChromeDriverDao;
import me.defozo.diagnostykascraper.dao.GoogleDriveDao;

import java.nio.file.FileSystems;

public class Main {
    private static final String SHARED_DOWNLOADS_FOLDER = System.getenv("SHARED_DOWNLOADS_FOLDER") + FileSystems.getDefault().getSeparator();

    public static void main(String[] args) throws Exception {
        GoogleDriveDao googleDriveDao = new GoogleDriveDao();
        ChromeDriverDao chromeDriverDao = new ChromeDriverDao(SHARED_DOWNLOADS_FOLDER);
        DiagnostykaService diagnostykaService = new DiagnostykaService(googleDriveDao, chromeDriverDao, SHARED_DOWNLOADS_FOLDER);
        diagnostykaService.downloadFromDiagnostykaWebsiteAndUploadToGoogleDriveAllTestResults();
        chromeDriverDao.getChromeDriver().quit();
    }
}
