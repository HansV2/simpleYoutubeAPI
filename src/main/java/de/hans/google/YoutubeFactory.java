package de.hans.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;

import javax.naming.ConfigurationException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collection;

public class YoutubeFactory {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final String pathToClientSecret;
    private final String pathToRefreshStorageFolder;
    private final Collection<String> scopes;
    private final String applicationName;

    /**
     * @param pathToClientSecret
     * @param pathToRefreshStorageFolder used to prevent repeated permission-requests
     * @param scopes
     * @param applicationName
     */
    public YoutubeFactory(
            String pathToClientSecret,
            String pathToRefreshStorageFolder,
            Collection<String> scopes,
            String applicationName) {
        this.pathToClientSecret = pathToClientSecret;
        this.pathToRefreshStorageFolder = pathToRefreshStorageFolder;
        this.scopes = scopes;
        this.applicationName = applicationName;
    }

    /**
     * Create an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        GoogleClientSecrets clientSecrets = loadGoogleClientSecrets();
        GoogleAuthorizationCodeFlow flow = triggerUserAuthorizationIfNecessary(httpTransport, clientSecrets);

        Credential credential = new AuthorizationCodeInstalledApp(
                flow,
                new LocalServerReceiver()
        ).authorize("user");

        return credential;
    }

    private GoogleAuthorizationCodeFlow triggerUserAuthorizationIfNecessary(NetHttpTransport httpTransport, GoogleClientSecrets clientSecrets) throws IOException {
        DataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(pathToRefreshStorageFolder));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, scopes)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .setApprovalPrompt("auto")
                .build();
        return flow;
    }

    private GoogleClientSecrets loadGoogleClientSecrets() throws IOException {
        InputStream in = new FileInputStream(pathToClientSecret);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        return clientSecrets;
    }

    /**
     * Build and return an authorized API client service.
     * Can be used to make multiple requests.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException
     * @throws ConfigurationException
     */
    public YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);

        YouTube build = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(applicationName)
                .build();

        return build;
    }
}
