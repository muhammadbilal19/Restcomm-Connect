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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.restcomm.connect.commons.annotations.concurrency.NotThreadSafe;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.dao.OutgoingCallerIdsDao;
import org.restcomm.connect.dao.entities.Account;
import org.restcomm.connect.dao.entities.OutgoingCallerId;
import org.restcomm.connect.dao.entities.OutgoingCallerIdList;
import org.restcomm.connect.dao.entities.RestCommResponse;
import org.restcomm.connect.http.converter.OutgoingCallerIdConverter;
import org.restcomm.connect.http.converter.OutgoingCallerIdListConverter;
import org.restcomm.connect.http.converter.RestCommResponseConverter;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@NotThreadSafe
public abstract class OutgoingCallerIdsEndpoint extends SecuredEndpoint {
    @Context
    protected ServletContext context;
    protected Configuration configuration;
    protected OutgoingCallerIdsDao dao;
    protected Gson gson;
    protected XStream xstream;

    public OutgoingCallerIdsEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        final DaoManager storage = (DaoManager) context.getAttribute(DaoManager.class.getName());
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        super.init(configuration);
        dao = storage.getOutgoingCallerIdsDao();
        final OutgoingCallerIdConverter converter = new OutgoingCallerIdConverter(configuration);
        final GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(OutgoingCallerId.class, converter);
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(converter);
        xstream.registerConverter(new OutgoingCallerIdListConverter(configuration));
        xstream.registerConverter(new RestCommResponseConverter(configuration));
    }

    private OutgoingCallerId createFrom(final Sid accountSid, final MultivaluedMap<String, String> data) {
        final Sid sid = Sid.generate(Sid.Type.PHONE_NUMBER);
        final DateTime now = DateTime.now();
        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        PhoneNumber phoneNumber = null;
        try {
            phoneNumber = phoneNumberUtil.parse(data.getFirst("PhoneNumber"), "US");
        } catch (final NumberParseException ignored) {
        }
        String friendlyName = phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
        if (data.containsKey("FriendlyName")) {
            friendlyName = data.getFirst("FriendlyName");
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append("/").append(getApiVersion(null)).append("/Accounts/").append(accountSid.toString())
                .append("/OutgoingCallerIds/").append(sid.toString());
        final URI uri = URI.create(buffer.toString());
        return new OutgoingCallerId(sid, now, now, friendlyName, accountSid, phoneNumberUtil.format(phoneNumber,
                PhoneNumberFormat.E164), uri);
    }

    protected Response getCallerId(final String accountSid, final String sid, final MediaType responseType) {
        Account operatedAccount = accountsDao.getAccount(accountSid);
        secure(operatedAccount, "RestComm:Read:OutgoingCallerIds");
        final OutgoingCallerId outgoingCallerId = dao.getOutgoingCallerId(new Sid(sid));
        if (outgoingCallerId == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(operatedAccount, outgoingCallerId.getAccountSid(), SecuredType.SECURED_STANDARD);
            if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(outgoingCallerId), APPLICATION_JSON).build();
            } else if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(outgoingCallerId);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else {
                return null;
            }
        }
    }

    protected Response getCallerIds(final String accountSid, final MediaType responseType) {
        secure(accountsDao.getAccount(accountSid), "RestComm:Read:OutgoingCallerIds");
        final List<OutgoingCallerId> outgoingCallerIds = dao.getOutgoingCallerIds(new Sid(accountSid));
        if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(outgoingCallerIds), APPLICATION_JSON).build();
        } else if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new OutgoingCallerIdList(outgoingCallerIds));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else {
            return null;
        }
    }

    protected Response putOutgoingCallerId(final String accountSid, final MultivaluedMap<String, String> data,
            final MediaType responseType) {
        secure(accountsDao.getAccount(accountSid), "RestComm:Create:OutgoingCallerIds");
        try {
            validate(data);
        } catch (final NullPointerException exception) {
            return status(BAD_REQUEST).entity(exception.getMessage()).build();
        }
        final OutgoingCallerId outgoingCallerId = createFrom(new Sid(accountSid), data);
        dao.addOutgoingCallerId(outgoingCallerId);
        if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(outgoingCallerId), APPLICATION_JSON).build();
        } else if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(outgoingCallerId);
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else {
            return null;
        }
    }

    protected Response updateOutgoingCallerId(final String accountSid, final String sid,
            final MultivaluedMap<String, String> data, final MediaType responseType) {
        Account operatedAccount = accountsDao.getAccount(accountSid);
        secure(operatedAccount, "RestComm:Modify:OutgoingCallerIds");
        OutgoingCallerId outgoingCallerId = dao.getOutgoingCallerId(new Sid(sid));
        if (outgoingCallerId == null) {
            return status(NOT_FOUND).build();
        } else {
            secure(operatedAccount, outgoingCallerId.getAccountSid(), SecuredType.SECURED_STANDARD);
            if (data.containsKey("FriendlyName")) {
                final String friendlyName = data.getFirst("FriendlyName");
                outgoingCallerId = outgoingCallerId.setFriendlyName(friendlyName);
            }
            dao.updateOutgoingCallerId(outgoingCallerId);
            if (APPLICATION_JSON_TYPE == responseType) {
                return ok(gson.toJson(outgoingCallerId), APPLICATION_JSON).build();
            } else if (APPLICATION_XML_TYPE == responseType) {
                final RestCommResponse response = new RestCommResponse(outgoingCallerId);
                return ok(xstream.toXML(response), APPLICATION_XML).build();
            } else {
                return null;
            }
        }
    }

    private void validate(final MultivaluedMap<String, String> data) throws RuntimeException {
        if (!data.containsKey("PhoneNumber")) {
            throw new NullPointerException("Phone number can not be null.");
        }
        try {
            PhoneNumberUtil.getInstance().parse(data.getFirst("PhoneNumber"), "US");
        } catch (final NumberParseException exception) {
            throw new IllegalArgumentException("Invalid phone number.");
        }
    }
}
