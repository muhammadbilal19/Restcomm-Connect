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
package org.restcomm.connect.dao.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.joda.time.DateTime;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;
import org.restcomm.connect.dao.InstanceIdDao;
import org.restcomm.connect.dao.entities.InstanceId;
import org.restcomm.connect.commons.dao.Sid;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.restcomm.connect.dao.DaoUtils.readDateTime;
import static org.restcomm.connect.dao.DaoUtils.readSid;
import static org.restcomm.connect.dao.DaoUtils.readString;
import static org.restcomm.connect.dao.DaoUtils.writeDateTime;
import static org.restcomm.connect.dao.DaoUtils.writeSid;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
@ThreadSafe
public class MybatisInstanceIdDao implements InstanceIdDao{
    private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.InstanceIdDao.";
    private final SqlSessionFactory sessions;

    public MybatisInstanceIdDao(final SqlSessionFactory sessions) {
        super();
        this.sessions = sessions;
    }

    @Override
    public InstanceId getInstanceId() {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(namespace+"getInstanceId");
            if (result != null) {
                return toInstanceId(result);
            } else {
                return null;
            }
        } finally {
            session.close();
        }
    }

    @Override
    public InstanceId getInstanceIdByHost(String host) {
        final SqlSession session = sessions.openSession();
        try {
            final Map<String, Object> result = session.selectOne(namespace+"getInstanceIdByHost", host);
            if (result != null) {
                return toInstanceId(result);
            } else {
                return null;
            }
        } catch (Exception e) {
            session.close();
            return getInstanceId();
        } finally {
            session.close();
        }
    }

    @Override
    public void addInstancecId(InstanceId instanceId) {
        final SqlSession session = sessions.openSession();
        try {
            session.insert(namespace + "addInstanceId", toMap(instanceId));
            session.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void updateInstanceId(InstanceId instanceId) {
        final SqlSession session = sessions.openSession();
        try {
            session.update(namespace + "updateInstanceId", toMap(instanceId));
            session.commit();
        } finally {
            session.close();
        }
    }

    private InstanceId toInstanceId(Map<String, Object> map) {
        final Sid sid = readSid(map.get("instance_id"));
        String host = readString(map.get("host"));
        if (host == null || host.isEmpty()) {
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {}
        }
        final DateTime dateCreated = readDateTime(map.get("date_created"));
        final DateTime dateUpdated = readDateTime(map.get("date_updated"));
        return new InstanceId(sid, host, dateCreated, dateUpdated);
    }

   private Map<String, Object> toMap(final InstanceId instanceId) {
       final Map<String, Object> map = new HashMap<String, Object>();
       map.put("instance_id", writeSid(instanceId.getId()));
       String host = instanceId.getHost();
       if (host == null || host.isEmpty()) {
           try {
               host = InetAddress.getLocalHost().getHostAddress();
           } catch (UnknownHostException e) {}
       }
       map.put("host", host);
       map.put("date_created", writeDateTime(instanceId.getDateCreated()));
       map.put("date_updated", writeDateTime(instanceId.getDateUpdated()));
       return map;
   }
}
