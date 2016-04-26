package org.mobicents.servlet.restcomm.rvd.identity;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.http.client.utils.URIBuilder;
import org.mobicents.servlet.restcomm.rvd.RvdConfiguration;
import org.mobicents.servlet.restcomm.rvd.restcomm.RestcommAccountInfoResponse;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Provides accounts by either quereing Restcomm or accessing cache. It follows the application lifecycle (not creatd per-request)
 * Future support for account caching will be added.
 *
 * The class has been implemented as a singleton and is lazily created because it's not possible to initialize it
 * in RvdInitializationServlet (restcommBaseUrl is not available at that time) :-(.
 *
 * @author Orestis Tsakiridis
 */
public class AccountProvider {

    String restcommUrl;

    AccountProvider(String restcommUrl) {
        if (restcommUrl == null)
            throw new IllegalStateException("restcommUrl cannot be null");
        this.restcommUrl = restcommUrl;
    }

    private URI buildAccountQueryUrl(String username) {
        try {
            // TODO url-encode the username
            URI uri = new URIBuilder(restcommUrl).setPath("/restcomm/2012-04-24/Accounts.json/" + username).build();
            return uri;
        } catch (URISyntaxException e) {
            // something really wrong has happened
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the account for username using authorization header as credentials. It can handle both basic http and bearer auth auth.
     * If the authentication fails or the account is not found it returns null.
     * TODO we need to treat differently missing accounts and failed authentications.
     *
     * @param authorizationHeader
     * @return
     */
    public RestcommAccountInfoResponse getAccount(String username, String authorizationHeader) {
        Client jerseyClient = Client.create();
        WebResource webResource = jerseyClient.resource(buildAccountQueryUrl(username));
        webResource.setProperty("Authorization", authorizationHeader);
        ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        if (response.getStatus() != 200)
            return null;
        RestcommAccountInfoResponse accountInfo = response.getEntity(RestcommAccountInfoResponse.class);
        return accountInfo;
    }

    // singleton stuff
    private static AccountProvider instance;
    public static AccountProvider getInstance() {
        if (instance == null) {
            String restcommUrl = RvdConfiguration.getInstance().getRestcommBaseUri().toString();
            instance = new AccountProvider(restcommUrl);
        }
        return instance;
    }

}

