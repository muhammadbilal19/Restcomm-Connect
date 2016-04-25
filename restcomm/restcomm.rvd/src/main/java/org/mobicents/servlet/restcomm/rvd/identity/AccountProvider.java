package org.mobicents.servlet.restcomm.rvd.identity;

import org.mobicents.servlet.restcomm.rvd.restcomm.RestcommAccountInfoResponse;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Provides accounts by either quireing Restcomm or accessing cache. It follows the application lifecycle (not creatd per-request)
 *
 * @author Orestis Tsakiridis
 */
public class AccountProvider {

    public RestcommAccountInfoResponse getAccountForUsername() {
        throw new NotImplementedException();
    }

    /**
     * Returns the account specified in the authorization header (basic http auth) and authenticates if needed.
     * If the authentication fails of the account is not found it returns null.
     * TODO we need to treat differently missing accounts and failed authentications.
     *
     * @param authorizationHeader
     * @return
     */
    public RestcommAccountInfoResponse getAccountForAuthorizationHeader(String authorizationHeader) {
        throw new NotImplementedException();
    }

    public RestcommAccountInfoResponse getAccountForBearerToken(String authorizationHeader, String username) {
        throw new NotImplementedException();
    }
}

