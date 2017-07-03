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
package org.restcomm.connect.dao;

import java.util.List;

import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.Queue;
import org.restcomm.connect.dao.entities.QueueFilter;
import org.restcomm.connect.dao.entities.QueueRecord;

/**
 * @author muhammad.bilal19@gmail.com (Muhammad Bilal)
 */
public interface QueuesDao {

    Queue getQueue(Sid sid);
    List<Queue> getQueues(QueueFilter filter);
    void addQueue(Queue queue);
    void removeQueue(Sid sid);
    void removeQueues(Sid sid);
    void updateQueue(Queue queue);
    Queue getQueueByFriendlyName(String friendlyName);
    void setQueueBytes(java.util.Queue<QueueRecord> members,Queue queue);
    java.util.Queue<QueueRecord> getQueueBytes(Sid sid);
    int getTotalQueueByAccount(QueueFilter filter);
}
