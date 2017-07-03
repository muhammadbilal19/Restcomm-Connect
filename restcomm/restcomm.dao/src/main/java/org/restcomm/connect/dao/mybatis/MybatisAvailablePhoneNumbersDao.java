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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.restcomm.connect.dao.DaoUtils;
import org.restcomm.connect.dao.AvailablePhoneNumbersDao;

import org.restcomm.connect.dao.entities.AvailablePhoneNumber;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;

/**
 * @author quintana.thomas@gmail.com (Thomas Quintana)
 */
@ThreadSafe
public final class MybatisAvailablePhoneNumbersDao implements AvailablePhoneNumbersDao {
    private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.AvailablePhoneNumbersDao.";
    private static final char[] lookupTable = new char[] { '2', '2', '2', '3', '3', '3', '4', '4', '4', '5', '5', '5', '6',
            '6', '6', '7', '7', '7', '7', '8', '8', '8', '9', '9', '9', '9' };
    private final SqlSessionFactory sessions;

    public MybatisAvailablePhoneNumbersDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public void addAvailablePhoneNumber(final AvailablePhoneNumber availablePhoneNumber) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addAvailablePhoneNumber", toMap(availablePhoneNumber));
            session.commit();
        } finally {
            session.close();
        }
    }

    private List<AvailablePhoneNumber> getAvailablePhoneNumbers(final String selector, final Object parameter) {
        final SqlSession session = sessions.openSession();
        try {
            List<Map<String, Object>> results = null;
            if (parameter == null) {
                results = session.selectList(selector);
            } else {
                results = session.selectList(selector, parameter);
            }
            final List<AvailablePhoneNumber> availablePhoneNumbers = new ArrayList<AvailablePhoneNumber>();
            if (results != null && !results.isEmpty()) {
                for (final Map<String, Object> result : results) {
                    availablePhoneNumbers.add(toAvailablePhoneNumber(result));
                }
            }
            return availablePhoneNumbers;
        } finally {
            session.close();
        }
    }

    @Override
    public List<AvailablePhoneNumber> getAvailablePhoneNumbers() {
        return getAvailablePhoneNumbers(namespace + "getAvailablePhoneNumbers", null);
    }

    @Override
    public List<AvailablePhoneNumber> getAvailablePhoneNumbersByAreaCode(final String areaCode) {
        final String phoneNumber = new StringBuilder().append("+1").append(areaCode).append("_______").toString();
        return getAvailablePhoneNumbers(namespace + "getAvailablePhoneNumbersByAreaCode", phoneNumber);
    }

    @Override
    public List<AvailablePhoneNumber> getAvailablePhoneNumbersByPattern(final String pattern) throws IllegalArgumentException {
        return getAvailablePhoneNumbers(namespace + "getAvailablePhoneNumbersByPattern", normalizePattern(pattern));
    }

    @Override
    public List<AvailablePhoneNumber> getAvailablePhoneNumbersByRegion(final String region) {
        return getAvailablePhoneNumbers(namespace + "getAvailablePhoneNumbersByRegion", region);
    }

    @Override
    public List<AvailablePhoneNumber> getAvailablePhoneNumbersByPostalCode(final int postalCode) {
        return getAvailablePhoneNumbers(namespace + "getAvailablePhoneNumbersByPostalCode", postalCode);
    }

    private String normalizePattern(final String input) throws IllegalArgumentException {
        final char[] tokens = input.toUpperCase().toCharArray();
        final char[] result = new char[tokens.length];
        for (int index = 0; index < tokens.length; index++) {
            final char token = tokens[index];
            if (token == '+' || Character.isDigit(token)) {
                result[index] = token;
                continue;
            } else if (token == '*') {
                result[index] = '_';
                continue;
            } else if (Character.isLetter(token)) {
                final int delta = 65; // The decimal distance from 0x0000 to 0x0041 which equals to 'A'
                final int position = Character.getNumericValue(token) - delta;
                result[index] = lookupTable[position];
            } else {
                throw new IllegalArgumentException(token + " is not a valid character.");
            }
        }
        return new String(result);
    }

    @Override
    public void removeAvailablePhoneNumber(final String phoneNumber) {
        final SqlSession session = sessions.openSession();
        try {
            session.delete(namespace + "removeAvailablePhoneNumber", phoneNumber);
            session.commit();
        } finally {
            session.close();
        }
    }

    private AvailablePhoneNumber toAvailablePhoneNumber(final Map<String, Object> map) {
        final String friendlyName = DaoUtils.readString(map.get("friendly_name"));
        final String phoneNumber = DaoUtils.readString(map.get("phone_number"));
        final Integer lata = DaoUtils.readInteger(map.get("lata"));
        final String rateCenter = DaoUtils.readString(map.get("rate_center"));
        final Double latitude = DaoUtils.readDouble(map.get("latitude"));
        final Double longitude = DaoUtils.readDouble(map.get("longitude"));
        final String region = DaoUtils.readString(map.get("region"));
        final Integer postalCode = DaoUtils.readInteger(map.get("postal_code"));
        final String isoCountry = DaoUtils.readString(map.get("iso_country"));
        final Boolean voiceCapable = DaoUtils.readBoolean(map.get("voice_capable"));
        final Boolean smsCapable = DaoUtils.readBoolean(map.get("sms_capable"));
        final Boolean mmsCapable = DaoUtils.readBoolean(map.get("mms_capable"));
        final Boolean faxCapable = DaoUtils.readBoolean(map.get("fax_capable"));
        final String cost = DaoUtils.readString(map.get("cost"));
        return new AvailablePhoneNumber(friendlyName, phoneNumber, lata, rateCenter, latitude, longitude, region, postalCode,
                isoCountry, cost, voiceCapable, smsCapable, mmsCapable, faxCapable);
    }

    private Map<String, Object> toMap(final AvailablePhoneNumber availablePhoneNumber) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("friendly_name", availablePhoneNumber.getFriendlyName());
        map.put("phone_number", availablePhoneNumber.getPhoneNumber());
        map.put("lata", availablePhoneNumber.getLata());
        map.put("rate_center", availablePhoneNumber.getRateCenter());
        map.put("latitude", availablePhoneNumber.getLatitude());
        map.put("longitude", availablePhoneNumber.getLongitude());
        map.put("region", availablePhoneNumber.getRegion());
        map.put("postal_code", availablePhoneNumber.getPostalCode());
        map.put("iso_country", availablePhoneNumber.getIsoCountry());
        map.put("voice_capable", availablePhoneNumber.isVoiceCapable());
        map.put("sms_capable", availablePhoneNumber.isSmsCapable());
        map.put("mms_capable", availablePhoneNumber.isMmsCapable());
        map.put("fax_capable", availablePhoneNumber.isFaxCapable());
        map.put("cost", availablePhoneNumber.getCost());
        return map;
    }
}
