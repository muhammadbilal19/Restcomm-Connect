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
package org.restcomm.connect.testsuite.http;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Logger;
import org.restcomm.connect.dao.entities.CallDetailRecordList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.thoughtworks.xstream.XStream;

/**
 * @author maria
 */

public class RestcommConferenceParticipantsTool {

    private static RestcommConferenceParticipantsTool instance;
    private static String accountsUrl;
    private static Logger logger = Logger.getLogger(RestcommConferenceParticipantsTool.class);
    
    private RestcommConferenceParticipantsTool() {}

    public static RestcommConferenceParticipantsTool getInstance() {
        if (instance == null)
            instance = new RestcommConferenceParticipantsTool();

        return instance;
    }

    private String getAccountsUrl(String deploymentUrl, String username, String conferenceSid, Boolean json) {
        if (deploymentUrl.endsWith("/")) {
            deploymentUrl = deploymentUrl.substring(0, deploymentUrl.length() - 1);
        }

        accountsUrl = deploymentUrl + "/2012-04-24/Accounts/" + username + "/Conferences/" + conferenceSid + "/Participants" + ((json) ? ".json" : "");

        return accountsUrl;
    }

    public JsonObject getParticipants(String deploymentUrl, String username, String authToken, String conferenceSid) {
        return (JsonObject) getParticipants(deploymentUrl, username, authToken, conferenceSid, null, null, true);
    }

    public JsonObject getParticipants(String deploymentUrl, String username, String authToken, String conferenceSid, Integer page, Integer pageSize,
            Boolean json) {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(deploymentUrl, username, conferenceSid, json);

        WebResource webResource = jerseyClient.resource(url);

        String response = null;

        if (page != null || pageSize != null) {
            MultivaluedMap<String, String> params = new MultivaluedMapImpl();

            if (page != null)
                params.add("Page", String.valueOf(page));
            if (pageSize != null)
                params.add("PageSize", String.valueOf(pageSize));

            response = webResource.queryParams(params).accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                    .get(String.class);
        } else {
            response = webResource.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML).get(String.class);
        }

        JsonParser parser = new JsonParser();

        if (json) {
            JsonObject jsonObject = null;
            try {
                JsonElement jsonElement = parser.parse(response);
                if (jsonElement.isJsonObject()) {
                    jsonObject = jsonElement.getAsJsonObject();
                } else {
                    logger.info("JsonElement: " + jsonElement.toString());
                }
            } catch (Exception e) {
                logger.info("Exception during JSON response parsing, exception: "+e);
                logger.info("JSON response: "+response);
            }
            return jsonObject;
        } else {
            XStream xstream = new XStream();
            xstream.alias("cdrlist", CallDetailRecordList.class);
            JsonObject jsonObject = parser.parse(xstream.toXML(response)).getAsJsonObject();
            return jsonObject;
        }

    }

    public JsonObject getParticipant(String deploymentUrl, String username, String conferenceSid, String authToken, String sid){

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(deploymentUrl, username, conferenceSid, false);

        WebResource webResource = jerseyClient.resource(url);

        String response = null;

        webResource = webResource.path(String.valueOf(sid)+".json");
        logger.info("The URI to sent: "+webResource.getURI());
        
        response = webResource.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .get(String.class);

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        return jsonObject;

    }

    public JsonObject getParticipantsUsingFilter(String deploymentUrl, String username, String conferenceSid, String authToken, Map<String, String> filters) {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(deploymentUrl, username, conferenceSid, true);

        WebResource webResource = jerseyClient.resource(url);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        for (String filterName : filters.keySet()) {
            String filterData = filters.get(filterName);
            params.add(filterName, filterData);
        }
        webResource = webResource.queryParams(params);
        String response = webResource.accept(MediaType.APPLICATION_JSON).get(String.class);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(response).getAsJsonObject();

        return jsonObject;
    }

    public JsonObject modifyCall(String deploymentUrl, String username, String conferenceSid, String authToken, String callSid, Boolean muted) throws Exception {

        Client jerseyClient = Client.create();
        jerseyClient.addFilter(new HTTPBasicAuthFilter(username, authToken));

        String url = getAccountsUrl(deploymentUrl, username, conferenceSid, true);

        WebResource webResource = jerseyClient.resource(url);

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        if (muted != null)
            params.add("Mute", ""+muted);

        JsonObject jsonObject = null;

        try {
            String response = webResource.path(callSid).accept(MediaType.APPLICATION_JSON).post(String.class, params);
            JsonParser parser = new JsonParser();
            jsonObject = parser.parse(response).getAsJsonObject();
        } catch (Exception e) {
            logger.info("Exception e: "+e);
            UniformInterfaceException exception = (UniformInterfaceException)e;
            jsonObject = new JsonObject();
            jsonObject.addProperty("Exception",exception.getResponse().getStatus());
        }
        return jsonObject;
    }
}
