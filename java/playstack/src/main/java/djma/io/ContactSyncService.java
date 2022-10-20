package djma.io;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.common.util.concurrent.Uninterruptibles;

import djma.common.Env;
import djma.db.DB;
import djma.db.KeyValStoreService;
import djma.db.generated.tables.records.ContactRecord;

import static djma.common.Common.ifNull;
import static djma.common.Common.optChain;
import static djma.db.generated.tables.Contact.CONTACT;

/**
 * Syncs contacts from Google People API to the database.
 */
public class ContactSyncService {
    private static ContactSyncService INSTANCE;

    private static final String APPLICATION_NAME = "Google People API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CONTACT_SYNC_TOKEN_KEY = "contact_sync_token";

    private static NetHttpTransport HTTP_TRANSPORT = null;

    private DB db = DB.get();
    private KeyValStoreService kvStore = KeyValStoreService.get();

    private Credential credential;
    private PeopleService peopleService;

    private ContactSyncService() {
        final JsonFactory jsonFactory = JSON_FACTORY;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        Env env = Env.get();

        // Instead, we save the oauth tokens and directly load them
        Credential.Builder credBuilder = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod());
        credBuilder.setJsonFactory(jsonFactory);
        credBuilder.setTransport(HTTP_TRANSPORT);
        credBuilder.setTokenServerUrl(new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL));
        credBuilder.setClientAuthentication(new ClientParametersAuthentication(
                env.get("GOOGLE_OAUTH_CLIENTID"),
                env.get("GOOGLE_OAUTH_CLIENTSECRET")));
        Credential credential = credBuilder.build();
        credential = credential.setRefreshToken(env.get("GOOGLE_OAUTH_MADAVIDJ_REFRESHTOKEN"));
        this.credential = credential;

        // Build a new authorized API client service.
        this.peopleService = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static ContactSyncService get() {
        if (INSTANCE == null) {
            INSTANCE = new ContactSyncService();
        }
        return INSTANCE;
    }

    public void loop() {
        CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Uninterruptibles.sleepUninterruptibly(Duration.ofMinutes(1));
                }
            }
        });
    }

    private void sync() throws IOException {
        String syncToken = getLastSyncToken();
        ListConnectionsResponse contactSyncResponse = null;

        // Fetch incremental changes using the sync token returned in the last
        // fullSyncResponse.
        try {
            com.google.api.services.people.v1.PeopleService.People.Connections.List contactSyncRequest = peopleService
                    .people().connections().list("people/me")
                    .setRequestSyncToken(true)
                    .setPersonFields("metadata,names,emailAddresses,phoneNumbers");
            if (syncToken != null) {
                System.out.println("Syncing contacts using sync token: " + syncToken);
                contactSyncRequest.setSyncToken(syncToken);
            }
            contactSyncResponse = contactSyncRequest.execute();
            for (Person person : ifNull(contactSyncResponse.getConnections(), List.of())) {
                handlePerson(person);
            }

            // Fetch all the pages
            while (contactSyncResponse.getNextPageToken() != null) {
                contactSyncRequest.setPageToken(contactSyncResponse.getNextPageToken());
                contactSyncResponse = contactSyncRequest.execute();
                for (Person person : contactSyncResponse.getConnections()) {
                    handlePerson(person);
                }
            }
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 410) {
                // Sync token expired. Make full sync request.
            }
        } finally {
            if (contactSyncResponse != null) {
                saveSyncToken(contactSyncResponse.getNextSyncToken());
            }
        }

    }

    private void saveSyncToken(String nextSyncToken) {
        System.out.println("Saving sync token: " + nextSyncToken);
        kvStore.set(CONTACT_SYNC_TOKEN_KEY, nextSyncToken);
    }

    private String getLastSyncToken() {
        return kvStore.get(CONTACT_SYNC_TOKEN_KEY);
    }

    private void handlePerson(Person person) {
        if (ifNull(person.getMetadata().getDeleted(), false)) {
            // Handle deleted person
            System.out.println("Deleted person: " + person.getResourceName());
            db.run(ctx -> {
                return ctx.deleteFrom(CONTACT)
                        .where(CONTACT.RESOURCENAME.eq(person.getResourceName()))
                        .execute();
            });
        } else {
            // Handle changed person
            ContactRecord c = personToContactRecord(person);
            System.out.println(c.toString());
            db.run(ctx -> {
                return ctx.insertInto(CONTACT)
                        .set(c)
                        .onDuplicateKeyUpdate()
                        .set(c)
                        .execute();
            });
        }
    }

    private static ContactRecord personToContactRecord(Person person) {
        return new ContactRecord(
                person.getResourceName(),
                optChain(person.getNames(), names -> names.get(0).getDisplayName()),
                optChain(person.getEmailAddresses(), emails -> emails.get(0).getValue()),
                optChain(person.getPhoneNumbers(), phones -> phones.get(0).getValue()));
    }
}