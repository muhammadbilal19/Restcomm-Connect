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

import static akka.pattern.Patterns.ask;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.configuration.Configuration;
import org.joda.time.DateTime;
import org.restcomm.connect.commons.annotations.concurrency.NotThreadSafe;
import org.restcomm.connect.commons.configuration.RestcommConfiguration;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.AccountsDao;
import org.restcomm.connect.dao.CallDetailRecordsDao;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.dao.QueuesDao;
import org.restcomm.connect.dao.entities.CallDetailRecord;
import org.restcomm.connect.dao.entities.Member;
import org.restcomm.connect.dao.entities.MemberList;
import org.restcomm.connect.dao.entities.Queue;
import org.restcomm.connect.dao.entities.QueueRecord;
import org.restcomm.connect.dao.entities.RestCommResponse;
import org.restcomm.connect.http.converter.MemberConverter;
import org.restcomm.connect.http.converter.MemberListConverter;
import org.restcomm.connect.telephony.api.CallInfo;
import org.restcomm.connect.telephony.api.CallResponse;
import org.restcomm.connect.telephony.api.GetCall;
import org.restcomm.connect.telephony.api.GetCallInfo;
import org.restcomm.connect.telephony.api.UpdateCallScript;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.xstream.XStream;

import akka.actor.ActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * @author muhammad.bilal19@gmail.com (Muhammad Bilal)
 */
@NotThreadSafe
public abstract class MembersEndpoint extends SecuredEndpoint {
    @Context
    private ServletContext context;

    private Gson gson;
    private GsonBuilder builder;
    private XStream xstream;
    private String instanceId;
    private AccountsDao accountsDao;
    private QueuesDao queueDao;
    private DaoManager daos;
    private ActorRef callManager;

    public MembersEndpoint() {
        super();
    }

    @PostConstruct
    public void init() {
        configuration = (Configuration) context.getAttribute(Configuration.class.getName());
        configuration = configuration.subset("runtime-settings");
        callManager = (ActorRef) context.getAttribute("org.mobicents.servlet.restcomm.telephony.CallManager");
        daos = (DaoManager) context.getAttribute(DaoManager.class.getName());
        accountsDao = daos.getAccountsDao();
        queueDao = daos.getQueuesDao();
        super.init(configuration);
        builder = new GsonBuilder();
        builder.registerTypeAdapter(Member.class, new MemberConverter(configuration));
        // builder.registerTypeAdapter(MemberList.class, new MemberListConverter(configuration));
        builder.setPrettyPrinting();
        gson = builder.create();
        xstream = new XStream();
        xstream.alias("RestcommResponse", RestCommResponse.class);
        xstream.registerConverter(new MemberConverter(configuration));
        xstream.registerConverter(new MemberListConverter(configuration));

        instanceId = RestcommConfiguration.getInstance().getMain().getInstanceId();
    }

    protected Response getFrontQueueMember(final String accountSid, final String queueSid, UriInfo info,
            MediaType responseType) {
        java.util.Queue<QueueRecord> queueList = new java.util.LinkedList<QueueRecord>();
        secure(accountsDao.getAccount(accountSid), "RestComm:Create:Members");

        final Queue queue = queueDao.getQueue(new Sid(queueSid));
        if (queue == null) {
            return status(NOT_FOUND).build();
        }
        queueList = queue.toCollectionFromBytes();
        QueueRecord record = queueList.peek();
        // queueDao.setQueueBytes(queueList, queue);
        Member member = null;

        if (record != null) {
            member = new Member(new Sid(record.getCallerSid()), record.toDateTime(), 0, 0);
        } else {
            return null;
        }
        final RestCommResponse response = new RestCommResponse(member);
        if (APPLICATION_XML_TYPE == responseType) {

            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(response), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    protected Response getQueueMember(final String accountSid, final String queueSid, String callSid, UriInfo info,
            MediaType responseType) {
        java.util.Queue<QueueRecord> queueList = new java.util.LinkedList<QueueRecord>();
        secure(accountsDao.getAccount(accountSid), "RestComm:Create:Members");
        final Queue queue = queueDao.getQueue(new Sid(queueSid));
        if (queue == null) {
            return status(NOT_FOUND).build();
        }
        queueList = queue.toCollectionFromBytes();
        Member member = null;
        boolean found = false;
        int position = 0;
        for (QueueRecord record : queueList) {
            if (record.getCallerSid().equals(callSid)) {
                member = new Member(new Sid(record.getCallerSid()), record.toDateTime(), 0, position);
                // queueList.remove(record);
                found = true;
                break;
            }
            ++position;

        }
        if (!found) {
            return status(BAD_REQUEST).build();
        }
        // queueDao.setQueueBytes(queueList, queue);
        final RestCommResponse response = new RestCommResponse(member);
        if (APPLICATION_XML_TYPE == responseType) {

            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(response), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    protected Response getQueueMembers(final String accountSid, final String queueSid, UriInfo info, MediaType responseType) {
        java.util.Queue<QueueRecord> queueList = new java.util.LinkedList<QueueRecord>();
        secure(accountsDao.getAccount(accountSid), "RestComm:Read:Members");
        final Queue queue = queueDao.getQueue(new Sid(queueSid));
        if (queue == null) {
            return status(NOT_FOUND).build();
        }
        queueList = queue.toCollectionFromBytes();
        final List<Member> members = new ArrayList<Member>();
        Member member = null;
        int position = 0;
        for (QueueRecord record : queueList) {
            member = new Member(new Sid(record.getCallerSid()), record.toDateTime(), 0, position++);
            members.add(member);
        }

        if (APPLICATION_XML_TYPE == responseType) {
            final RestCommResponse response = new RestCommResponse(new MemberList(members));
            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(members), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Response dequeue(final String accountSid, final String queueSid, final String callSid,
            final MultivaluedMap<String, String> data, final MediaType responseType) {

        java.util.Queue<QueueRecord> queueList = new java.util.LinkedList<QueueRecord>();
        secure(accountsDao.getAccount(accountSid), "RestComm:Update:Members");
        final Queue queue = queueDao.getQueue(new Sid(queueSid));
        if (queue == null) {
            return status(NOT_FOUND).build();
        }
        queueList = queue.toCollectionFromBytes();
        QueueRecord record = null;
        Member member = null;
        if (queueList != null) {
            if (callSid.equalsIgnoreCase("Front")) {
                record = queueList.poll();
            } else {
                boolean found = false;
                int position = 0;
                for (QueueRecord rec : queueList) {
                    if (rec.getCallerSid().equals(callSid)) {
                        member = new Member(new Sid(rec.getCallerSid()), rec.toDateTime(), 0, position);
                        queueList.remove(record);
                        found = true;
                        break;
                    }
                    ++position;

                }
                if (!found) {
                    return status(BAD_REQUEST).build();
                }
            }
        } else {
            return status(BAD_REQUEST).build();
        }
        queueDao.setQueueBytes(queueList, queue);

        final CallDetailRecordsDao dao = daos.getCallDetailRecordsDao();
        CallDetailRecord cdr = null;
        try {
            cdr = dao.getCallDetailRecord(new Sid(callSid));

            if (cdr != null) {
                // secureLevelControl(daos.getAccountsDao(), sid, String.valueOf(cdr.getAccountSid()));
                secure(accountsDao.getAccount(accountSid), cdr.getAccountSid(), SecuredType.SECURED_STANDARD);

            } else {
                return Response.status(Status.NOT_ACCEPTABLE).build();
            }
        } catch (Exception e) {
            return status(BAD_REQUEST).build();
        }

        final String url = data.getFirst("Url");
        String method = data.getFirst("Method");

        if (method == null)
            method = "POST";

        String callPath = null;
        final ActorRef call;
        final CallInfo callInfo;
        final Timeout expires = new Timeout(Duration.create(60, TimeUnit.SECONDS));
        try {
            callPath = cdr.getCallPath();
            Future<Object> future = (Future<Object>) ask(callManager, new GetCall(callPath), expires);
            call = (ActorRef) Await.result(future, Duration.create(10, TimeUnit.SECONDS));

            future = (Future<Object>) ask(call, new GetCallInfo(), expires);
            CallResponse<CallInfo> response = (CallResponse<CallInfo>) Await.result(future,
                    Duration.create(10, TimeUnit.SECONDS));
            callInfo = response.get();
        } catch (Exception exception) {
            return status(INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
        }

        if (url != null && call != null) {
            try {
                final String version = getApiVersion(data);
                final URI uri = (new URL(url)).toURI();

                // TODO need to check whether these parameter required or not
                URI fallbackUri = null;
                URI callbackUri = null;
                final UpdateCallScript update = new UpdateCallScript(call, new Sid(accountSid), version, uri, method,
                        fallbackUri, method, callbackUri, method, false);
                callManager.tell(update, null);
            } catch (Exception exception) {
                return status(INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
            }
        }
        if (record != null) {
            member = new Member(new Sid(record.getCallerSid()), record.toDateTime(), 0, 0);
        } else {
            return null;
        }
        final RestCommResponse response = new RestCommResponse(member);
        if (APPLICATION_XML_TYPE == responseType) {

            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(response), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }

    protected Response enqueue(final String accountSid, final String queueSid, final String callSid,
            final MultivaluedMap<String, String> data, final MediaType responseType) {

        java.util.Queue<QueueRecord> queueList = new java.util.LinkedList<QueueRecord>();
        secure(accountsDao.getAccount(accountSid), "RestComm:Create:Members");
        final Queue queue = queueDao.getQueue(new Sid(queueSid));
        if (queue == null) {
            return status(NOT_FOUND).build();
        }
        queueList = queue.toCollectionFromBytes();
        QueueRecord record = new QueueRecord(new Sid(callSid).toString(), new Date());
        queueList.offer(record);
        queueDao.setQueueBytes(queueList, queue);
        Member member = new Member(new Sid(callSid), new DateTime(), 0, queueList.size());
        final RestCommResponse response = new RestCommResponse(member);
        if (APPLICATION_XML_TYPE == responseType) {

            return ok(xstream.toXML(response), APPLICATION_XML).build();
        } else if (APPLICATION_JSON_TYPE == responseType) {
            return ok(gson.toJson(response), APPLICATION_JSON).build();
        } else {
            return null;
        }
    }
}