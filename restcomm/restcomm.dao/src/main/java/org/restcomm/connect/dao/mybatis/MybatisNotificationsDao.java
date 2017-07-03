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
package org.restcomm.connect.dao.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.dao.NotificationsDao;
import org.restcomm.connect.dao.entities.Notification;
import org.restcomm.connect.commons.dao.Sid;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.restcomm.connect.dao.DaoUtils.readDateTime;
import static org.restcomm.connect.dao.DaoUtils.readInteger;
import static org.restcomm.connect.dao.DaoUtils.readSid;
import static org.restcomm.connect.dao.DaoUtils.readString;
import static org.restcomm.connect.dao.DaoUtils.readUri;
import static org.restcomm.connect.dao.DaoUtils.writeDateTime;
import static org.restcomm.connect.dao.DaoUtils.writeSid;
import static org.restcomm.connect.dao.DaoUtils.writeUri;
import org.restcomm.connect.dao.entities.NotificationFilter;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisNotificationsDao implements NotificationsDao {
    private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.NotificationsDao.";
    private final SqlSessionFactory sessions;

    public MybatisNotificationsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addNotification(final Notification notification) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addNotification", toMap(notification));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Notification getNotification(final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(namespace + "getNotification", sid.toString());
            if (result != null) {
                return toNotification(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Notification> getNotifications(final Sid accountSid) {
        return getNotifications(namespace + "getNotifications", accountSid.toString());
    }

    @Override
    public List<Notification> getNotifications(NotificationFilter filter) {

        final SqlSession session = sessions.openSession();

        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getNotificationsByUsingFilters",
                    filter);
            final List<Notification> cdrs = new ArrayList<Notification>();

            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toNotification(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }
    }

    @Override
    public Integer getTotalNotification(NotificationFilter filter) {
        final SqlSession session = sessions.openSession();
        try {
            final Integer total = session.selectOne(namespace + "getTotalNotificationByUsingFilters", filter);
            return total;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Notification> getNotificationsByCall(final Sid callSid) {
        return getNotifications(namespace + "getNotificationsByCall", callSid.toString());
    }

    @Override
    public List<Notification> getNotificationsByLogLevel(final int logLevel) {
        return getNotifications(namespace + "getNotificationsByLogLevel", logLevel);
    }

    @Override
    public List<Notification> getNotificationsByMessageDate(final DateTime messageDate) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("start_date", messageDate.toDate());
        parameters.put("end_date", messageDate.plusDays(1).toDate());
        return getNotifications(namespace + "getNotificationsByMessageDate", parameters);
    }

    private List<Notification> getNotifications(final String selector, final Object input) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session.selectList(selector, input);
            final List<Notification> notifications = new ArrayList<Notification>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    notifications.add(toNotification(result));
                }
            }
            return notifications;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeNotification(final Sid sid) {
        removeNotifications(namespace + "removeNotification", sid);
    }

    @Override
    public void removeNotifications(final Sid accountSid) {
        removeNotifications(namespace + "removeNotifications", accountSid);
    }

    @Override
    public void removeNotificationsByCall(final Sid callSid) {
        removeNotifications(namespace + "removeNotificationsByCall", callSid);
    }

    private void removeNotifications(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    private Map<String, Object> toMap(final Notification notification) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", writeSid(notification.getSid()));
        map.put("date_created", writeDateTime(notification.getDateCreated()));
        map.put("date_updated", writeDateTime(notification.getDateUpdated()));
        map.put("account_sid", writeSid(notification.getAccountSid()));
        map.put("call_sid", writeSid(notification.getCallSid()));
        map.put("api_version", notification.getApiVersion());
        map.put("log", notification.getLog());
        map.put("error_code", notification.getErrorCode());
        map.put("more_info", writeUri(notification.getMoreInfo()));
        map.put("message_text", notification.getMessageText());
        map.put("message_date", writeDateTime(notification.getMessageDate()));
        map.put("request_url", writeUri(notification.getRequestUrl()));
        map.put("request_method", notification.getRequestMethod());
        map.put("request_variables", notification.getRequestVariables());
        map.put("response_headers", notification.getResponseHeaders());
        map.put("response_body", notification.getResponseBody());
        map.put("uri", writeUri(notification.getUri()));
        return map;
    }

    private Notification toNotification(final Map<String, Object> map) {
        final Sid sid = readSid(map.get("sid"));
        final DateTime dateCreated = readDateTime(map.get("date_created"));
        final DateTime dateUpdated = readDateTime(map.get("date_updated"));
        final Sid accountSid = readSid(map.get("account_sid"));
        final Sid callSid = readSid(map.get("call_sid"));
        final String apiVersion = readString(map.get("api_version"));
        final Integer log = readInteger(map.get("log"));
        final Integer errorCode = readInteger(map.get("error_code"));
        final URI moreInfo = readUri(map.get("more_info"));
        final String messageText = readString(map.get("message_text"));
        final DateTime messageDate = readDateTime(map.get("message_date"));
        final URI requestUrl = readUri(map.get("request_url"));
        final String requestMethod = readString(map.get("request_method"));
        final String requestVariables = readString(map.get("request_variables"));
        final String responseHeaders = readString(map.get("response_headers"));
        final String responseBody = readString(map.get("response_body"));
        final URI uri = readUri(map.get("uri"));
        return new Notification(sid, dateCreated, dateUpdated, accountSid, callSid, apiVersion, log, errorCode, moreInfo,
                messageText, messageDate, requestUrl, requestMethod, requestVariables, responseHeaders, responseBody, uri);
    }
}
