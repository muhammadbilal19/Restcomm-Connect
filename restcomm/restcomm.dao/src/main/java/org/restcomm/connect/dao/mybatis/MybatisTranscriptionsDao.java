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

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import org.joda.time.DateTime;

import org.restcomm.connect.dao.DaoUtils;
import org.restcomm.connect.dao.TranscriptionsDao;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.Transcription;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.dao.entities.TranscriptionFilter;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisTranscriptionsDao implements TranscriptionsDao {
    private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.TranscriptionsDao.";
    private final SqlSessionFactory sessions;

    public MybatisTranscriptionsDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addTranscription(final Transcription transcription) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addTranscription", toMap(transcription));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public Transcription getTranscription(final Sid sid) {
        return getTranscription(namespace + "getTranscription", sid);
    }

    @Override
    public Transcription getTranscriptionByRecording(final Sid recordingSid) {
        return getTranscription(namespace + "getTranscriptionByRecording", recordingSid);
    }

    private Transcription getTranscription(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(selector, sid.toString());
            if (result != null) {
                return toTranscription(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<Transcription> getTranscriptions(final Sid accountSid) {
        final SqlSession session = sessions.openSession();
        try {
            final List<Map<String, Object>> results = session
                    .selectList(namespace + "getTranscriptions", accountSid.toString());
            final List<Transcription> transcriptions = new ArrayList<Transcription>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    transcriptions.add(toTranscription(result));
                }
            }
            return transcriptions;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Transcription> getTranscriptions(TranscriptionFilter filter) {

        final SqlSession session = sessions.openSession();

        try {
            final List<Map<String, Object>> results = session.selectList(namespace + "getTranscriptionsByUsingFilters",
                    filter);
            final List<Transcription> cdrs = new ArrayList<Transcription>();

            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    cdrs.add(toTranscription(result));
                }
            }
            return cdrs;
        } finally {
            session.close();
        }
    }

    @Override
    public Integer getTotalTranscription(TranscriptionFilter filter) {
        final SqlSession session = sessions.openSession();
        try {
            final Integer total = session.selectOne(namespace + "getTotalTranscriptionByUsingFilters", filter);
            return total;
        } finally {
            session.close();
        }
    }

    @Override
    public void removeTranscription(final Sid sid) {
        removeTranscriptions(namespace + "removeTranscription", sid);
    }

    @Override
    public void removeTranscriptions(final Sid accountSid) {
        removeTranscriptions(namespace + "removeTranscriptions", accountSid);
    }

    private void removeTranscriptions(final String selector, final Sid sid) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(selector, sid.toString());
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateTranscription(final Transcription transcription) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(namespace + "updateTranscription", toMap(transcription));
            session.commit();
        } finally {
            session.close();
        }
    }

    private Map<String, Object> toMap(final Transcription transcription) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sid", DaoUtils.writeSid(transcription.getSid()));
        map.put("date_created", DaoUtils.writeDateTime(transcription.getDateCreated()));
        map.put("date_updated", DaoUtils.writeDateTime(transcription.getDateUpdated()));
        map.put("account_sid", DaoUtils.writeSid(transcription.getAccountSid()));
        map.put("status", transcription.getStatus().toString());
        map.put("recording_sid", DaoUtils.writeSid(transcription.getRecordingSid()));
        map.put("duration", transcription.getDuration());
        map.put("transcription_text", transcription.getTranscriptionText());
        map.put("price", DaoUtils.writeBigDecimal(transcription.getPrice()));
        map.put("price_unit", DaoUtils.writeCurrency(transcription.getPriceUnit()));
        map.put("uri", DaoUtils.writeUri(transcription.getUri()));
        return map;
    }

    private Transcription toTranscription(final Map<String, Object> map) {
        final Sid sid = DaoUtils.readSid(map.get("sid"));
        final DateTime dateCreated = DaoUtils.readDateTime(map.get("date_created"));
        final DateTime dateUpdated = DaoUtils.readDateTime(map.get("date_updated"));
        final Sid accountSid = DaoUtils.readSid(map.get("account_sid"));
        final String text = DaoUtils.readString(map.get("status"));
        final Transcription.Status status = Transcription.Status.getStatusValue(text);
        final Sid recordingSid = DaoUtils.readSid(map.get("recording_sid"));
        final Double duration = DaoUtils.readDouble(map.get("duration"));
        final String transcriptionText = DaoUtils.readString(map.get("transcription_text"));
        final BigDecimal price = DaoUtils.readBigDecimal(map.get("price"));
        final Currency priceUnit = DaoUtils.readCurrency(map.get("price_unit"));
        final URI uri = DaoUtils.readUri(map.get("uri"));
        return new Transcription(sid, dateCreated, dateUpdated, accountSid, status, recordingSid, duration, transcriptionText,
                price, priceUnit, uri);
    }
}
