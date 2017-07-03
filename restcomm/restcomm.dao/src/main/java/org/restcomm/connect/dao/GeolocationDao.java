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

package org.restcomm.connect.dao;

import java.util.List;
import org.restcomm.connect.dao.entities.Geolocation;
import org.restcomm.connect.commons.dao.Sid;;

/**
 * @author Fernando Mendioroz (fernando.mendioroz@telestax.com)
 *
 */
public interface GeolocationDao {

    void addGeolocation(Geolocation gl);

    Geolocation getGeolocation(Sid sid);

    List<Geolocation> getGeolocations(Sid sid);

    void removeGeolocation(Sid sid);

    void removeGeolocations(Sid sid);

    void updateGeolocation(Geolocation gl);
}

