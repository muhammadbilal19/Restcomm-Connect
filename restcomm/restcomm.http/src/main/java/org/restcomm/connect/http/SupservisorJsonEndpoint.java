/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.restcomm.connect.http;

import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
@Path("/Accounts/{accountSid}/Supervisor.json")
@ThreadSafe
public class SupservisorJsonEndpoint extends SupervisorEndpoint{

    public SupservisorJsonEndpoint() {
        super();
    }

    //Simple PING/PONG message
    @GET
    public Response ping(@PathParam("accountSid") final String accountSid) {
        return pong(accountSid, APPLICATION_JSON_TYPE);
    }

    //Get statistics
    @Path("/metrics")
    @GET
    public Response getMetrics(@PathParam("accountSid") final String accountSid, @Context UriInfo info) {
        return getMetrics(accountSid, info, APPLICATION_JSON_TYPE);
    }

    //Get live calls
    @Path("/livecalls")
    @GET
    public Response getLiveCalls(@PathParam("accountSid") final String accountSid) {
        return getLiveCalls(accountSid, APPLICATION_JSON_TYPE);
    }

    //Register a remote location where Restcomm will send monitoring updates
    @Path("/remote")
    @POST
    public Response registerForMetricsUpdates(@PathParam("accountSid") final String accountSid, @Context UriInfo info) {
        return registerForUpdates(accountSid, info, APPLICATION_JSON_TYPE);
    }

    //Register a remote location where Restcomm will send monitoring updates for a specific Call
    @Path("/remote/{sid}")
    @POST
    public Response registerForCallMetricsUpdates(@PathParam("accountSid") final String accountSid, @PathParam("sid") final String sid, final MultivaluedMap<String, String> data) {
        return registerForCallUpdates(accountSid, sid, data, APPLICATION_JSON_TYPE);
    }
}
