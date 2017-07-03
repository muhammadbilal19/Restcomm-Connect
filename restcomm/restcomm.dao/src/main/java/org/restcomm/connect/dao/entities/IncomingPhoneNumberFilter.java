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
package org.restcomm.connect.dao.entities;

import org.restcomm.connect.commons.annotations.concurrency.Immutable;

/**
 * @author <a href="mailto:jean.deruelle@telestax.com">Jean Deruelle</a>
 */

@Immutable
public class IncomingPhoneNumberFilter {

    private final String accountSid;
    private final String friendlyName;
    private final String phoneNumber;
    private final String sortBy;
    private final String sortDirection;
    private final Integer limit;
    private final Integer offset;

    public IncomingPhoneNumberFilter(String accountSid, String friendlyName, String phoneNumber, String sortBy,
            String sortDirection, Integer limit, Integer offset) {
        this.accountSid = accountSid;
        this.friendlyName = friendlyName;
        // The LIKE keyword uses '%' to match any (including 0) number of characters, and '_' to match exactly one character
        // Add here the '%' keyword so +15126002188 will be the same as 15126002188 and 6002188
        if (phoneNumber != null) {
            phoneNumber = "%" + phoneNumber + "%";
            phoneNumber = phoneNumber.replaceAll("\\*", "_");
        }

        this.phoneNumber = phoneNumber;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.limit = limit;
        this.offset = offset;
    }

    public IncomingPhoneNumberFilter(String accountSid, String friendlyName, String phoneNumber) {
        super();
        this.accountSid = accountSid;
        this.friendlyName = friendlyName;
        // The LIKE keyword uses '%' to match any (including 0) number of characters, and '_' to match exactly one character
        // Add here the '%' keyword so +15126002188 will be the same as 15126002188 and 6002188
        if (phoneNumber != null) {
            phoneNumber = "%" + phoneNumber + "%";
            phoneNumber = phoneNumber.replaceAll("\\*", "_");
        }
        this.phoneNumber = phoneNumber;
        this.sortBy = null;
        this.sortDirection = null;
        this.offset = null;
        this.limit = null;
    }

    public String getAccountSid() {
        return accountSid;
    }

    /**
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @return the sortBy
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * @return the sortDirection
     */
    public String getSortDirection() {
        return sortDirection;
    }

    /**
     * @return the limit
     */
    public Integer getLimit() {
        return limit;
    }

    /**
     * @return the offset
     */
    public Integer getOffset() {
        return offset;
    }

}
