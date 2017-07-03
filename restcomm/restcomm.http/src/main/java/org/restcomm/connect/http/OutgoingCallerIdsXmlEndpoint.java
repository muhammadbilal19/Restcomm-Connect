/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.http;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;

import static javax.ws.rs.core.MediaType.*;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.*;

import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.dao.entities.Account;
import org.restcomm.connect.dao.entities.OutgoingCallerId;
import org.restcomm.connect.commons.dao.Sid;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@Path("/Accounts/{accountSid}/OutgoingCallerIds")
@ThreadSafe
public final class OutgoingCallerIdsXmlEndpoint extends OutgoingCallerIdsEndpoint {
    public OutgoingCallerIdsXmlEndpoint() {
        super();
    }

    private Response deleteOutgoingCallerId(String accountSid, String sid) {
        Account operatedAccount = super.accountsDao.getAccount(accountSid);
        secure(operatedAccount, "RestComm:Delete:OutgoingCallerIds");
        OutgoingCallerId oci = dao.getOutgoingCallerId(new Sid(sid));
        if (oci != null) {
            secure(operatedAccount,String.valueOf(oci.getAccountSid()), SecuredType.SECURED_STANDARD );
        } // TODO return a NOT_FOUND status code here if oci==null maybe ?
        dao.removeOutgoingCallerId(new Sid(sid));
        return ok().build();
    }

    @Path("/{sid}.json")
    @DELETE
    public Response deleteOutgoingCallerIdAsJson(@PathParam("accountSid") String accountSid, @PathParam("sid") String sid) {
        return deleteOutgoingCallerId(accountSid, sid);
    }

    @Path("/{sid}")
    @DELETE
    public Response deleteOutgoingCallerIdAsXml(@PathParam("accountSid") String accountSid, @PathParam("sid") String sid) {
        return deleteOutgoingCallerId(accountSid, sid);
    }

    @Path("/{sid}.json")
    @GET
    public Response getCallerIdAsJson(@PathParam("accountSid") final String accountSid, @PathParam("sid") final String sid) {
        return getCallerId(accountSid, sid, APPLICATION_JSON_TYPE);
    }

    @Path("/{sid}")
    @GET
    public Response getCallerIdAsXml(@PathParam("accountSid") final String accountSid, @PathParam("sid") final String sid) {
        return getCallerId(accountSid, sid, APPLICATION_XML_TYPE);
    }

    @GET
    public Response getCallerIds(@PathParam("accountSid") final String accountSid) {
        return getCallerIds(accountSid, APPLICATION_XML_TYPE);
    }

    @POST
    public Response putOutgoingCallerId(@PathParam("accountSid") final String accountSid,
            final MultivaluedMap<String, String> data) {
        return putOutgoingCallerId(accountSid, data, APPLICATION_XML_TYPE);
    }

    @Path("/{sid}.json")
    @PUT
    public Response updateOutgoingCallerIdAsJson(@PathParam("accountSid") final String accountSid,
            @PathParam("sid") final String sid, final MultivaluedMap<String, String> data) {
        return updateOutgoingCallerId(accountSid, sid, data, APPLICATION_JSON_TYPE);
    }

    @Path("/{sid}")
    @PUT
    public Response updateOutgoingCallerIdAsXml(@PathParam("accountSid") final String accountSid,
            @PathParam("sid") final String sid, final MultivaluedMap<String, String> data) {
        return updateOutgoingCallerId(accountSid, sid, data, APPLICATION_XML_TYPE);
    }
}
