package me.defozo.diagnostykascraper.dao;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleDriveDao {
    private static final String PRIVATE_KEY_FROM_P12_FILE_PATH = System.getenv("PRIVATE_KEY_FROM_P12_FILE_PATH");
    private static final String SERVICE_ACCOUNT_ID = System.getenv("SERVICE_ACCOUNT_ID");
    /** Application name. */
    private static final String APPLICATION_NAME = "Diagnostyka Scraper";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    static Drive driveService;

    public GoogleDriveDao() throws GeneralSecurityException, IOException {
        configureGoogleDriveService();
    }

    public void uploadToGoogleDrive(File file, String folderId) throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(file.getName());
        File filePath = new File(file.getAbsolutePath());
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

    private List<com.google.api.services.drive.model.File> getFilesInFolder(String folderId){
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

    private void configureGoogleDriveService() throws GeneralSecurityException, IOException {
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
    }
}
