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

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;


/**
 * @author muhammad.bilal@gmail.com (Muhammad Bilal)
 */
@Path("/Accounts/{accountSid}/Queues.json")
@ThreadSafe
public class QueuesJsonEndpoint extends QueuesEndpoint {

    public QueuesJsonEndpoint() {
        super();
    }

    @GET
    public Response getQueues(@PathParam("accountSid") final String accountSid, @Context UriInfo info) {
        return getQueues(accountSid, info, APPLICATION_JSON_TYPE);
    }

    @GET
    @Path("/{queueSid}")
    public Response getQueue(@PathParam("accountSid") final String accountSid, @PathParam("queueSid") final String queueSid,
            @Context UriInfo info) {
        return getQueue(accountSid, queueSid, info, APPLICATION_JSON_TYPE);
    }

    @Consumes(APPLICATION_FORM_URLENCODED)
    @POST
    public Response createQueue(@PathParam("accountSid") final String accountSid, final MultivaluedMap<String, String> data) {
        return createQueue(accountSid, data, APPLICATION_JSON_TYPE);
    }

    @Consumes(APPLICATION_FORM_URLENCODED)
    @Path("/{queueSid}")
    @POST
    public Response updateQueue(@PathParam("accountSid") final String accountSid, @PathParam("queueSid") final String queueSid,
            final MultivaluedMap<String, String> data) {
        return updateQueue(accountSid, queueSid, data, APPLICATION_JSON_TYPE);
    }

    @Path("/{queueSid}.json")
    @DELETE
    public Response deleteAccountAsJson(@PathParam("queueSid") final String queueSid,
            @PathParam("accountSid") final String accountSid) {
        return deleteQueue(queueSid, accountSid);
    }

}
