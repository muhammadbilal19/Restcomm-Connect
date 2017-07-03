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
import org.joda.time.format.DateTimeFormat;
import org.restcomm.connect.dao.DaoUtils;
import org.restcomm.connect.dao.UsageDao;
import org.restcomm.connect.commons.dao.Sid;
import org.restcomm.connect.dao.entities.Usage;
import org.restcomm.connect.commons.annotations.concurrency.ThreadSafe;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.Date;
import java.util.Currency;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author brainslog@gmail.com (Alexandre Mendonca)
 */
@ThreadSafe
public final class MybatisUsageDao implements UsageDao {

  private static final String namespace = "org.mobicents.servlet.sip.restcomm.dao.UsageDao.";
  private final SqlSessionFactory sessions;

  public MybatisUsageDao(final SqlSessionFactory sessions) {
    super();
    this.sessions = sessions;
  }

  @Override
  public List<Usage> getUsage(final Sid accountSid, String uri) {
    return getUsageCalls(accountSid, null, null, null, uri, "getAllTimeCalls");
  }

  @Override
  public List<Usage> getUsageDaily(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate, String uri) {
    return getUsageCalls(accountSid, category, startDate, endDate, uri, "getDailyCalls");
  }

  @Override
  public List<Usage> getUsageMonthly(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate, String uri) {
    return getUsageCalls(accountSid, category, startDate, endDate, uri, "getMonthlyCalls");
  }

  @Override
  public List<Usage> getUsageYearly(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate, String uri) {
    return getUsageCalls(accountSid, category, startDate, endDate, uri, "getYearlyCalls");
  }

  @Override
  public List<Usage> getUsageAllTime(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate, String uri) {
    return getUsageCalls(accountSid, category, startDate, endDate, uri, "getAllTimeCalls");
  }

  @Override
  public List<Usage> getUsage(final Sid accountSid) {
    return getUsage(accountSid, "");
  }

  @Override
  public List<Usage> getUsageDaily(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageDaily(accountSid, category, startDate, endDate, "");
  }

  @Override
  public List<Usage> getUsageMonthly(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageMonthly(accountSid, category, startDate, endDate, "");
  }

  @Override
  public List<Usage> getUsageYearly(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageYearly(accountSid, category, startDate, endDate, "");
  }

  @Override
  public List<Usage> getUsageAllTime(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageAllTime(accountSid, category, startDate, endDate, "");
  }
  /*
  @Override
  public List<Usage> getUsageToday(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageCalls(accountSid, category, startDate, endDate, "getTodayCalls");
  }

  @Override
  public List<Usage> getUsageYesterday(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageCalls(accountSid, category, startDate, endDate, "getYesterdayCalls");
  }

  @Override
  public List<Usage> getUsageThisMonth(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageCalls(accountSid, category, startDate, endDate, "getThisMonthCalls");
  }

  @Override
  public List<Usage> getUsageLastMonth(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate) {
    return getUsageCalls(accountSid, category, startDate, endDate, "getLastMonthCalls");
  }
  */
  private List<Usage> getUsageCalls(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate, final String queryName) {
    return getUsageCalls(accountSid, category, startDate, endDate, "", queryName);
  }
  private List<Usage> getUsageCalls(final Sid accountSid, Usage.Category category, DateTime startDate, DateTime endDate, String uri, final String queryName) {
    long startTime = System.currentTimeMillis();
    final SqlSession session = sessions.openSession();
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("sid", accountSid.toString());
    params.put("startDate", new Date(startDate.getMillis()));
    params.put("endDate", new Date(endDate.getMillis()));
    params.put("uri", uri);
    fillParametersByCategory(category, params);
    try {
      final List<Map<String, Object>> results = session.selectList(namespace + queryName, params);
      final List<Usage> usageRecords = new ArrayList<Usage>();
      if (results != null && !results.isEmpty()) {
        for (final Map<String, Object> result : results) {
          usageRecords.add(toUsageRecord(accountSid, result));
        }
      }
      return usageRecords;
    } finally {
      session.close();
    }
  }

  private Usage toUsageRecord(final Sid accountSid, final Map<String, Object> map) {
    final Usage.Category category = Usage.Category.CALLS;
    final String description = "Total Calls";
    final DateTime startDate = DateTimeFormat.forPattern("yyyyy-MM-dd").parseDateTime(map.get("start_date").toString());
    final DateTime endDate = DateTimeFormat.forPattern("yyyyy-MM-dd").parseDateTime(map.get("end_date").toString());

    final Long usage = DaoUtils.readLong(map.get("usage"));
    final String usageUnit = "minutes";

    final Long count = DaoUtils.readLong(map.get("count"));
    final String countUnit = "calls";

    /* FIXME: readBigDecimal should take Double instead of String ? */
    final BigDecimal price = DaoUtils.readBigDecimal(map.get("price").toString());
    final Currency priceUnit = Currency.getInstance(Locale.US);

    final URI uri = DaoUtils.readUri(map.get("uri"));

    return new Usage(category, description, accountSid, startDate, endDate, usage, usageUnit, count, countUnit, price, priceUnit, uri);
  }

  private Map<String, Object> fillParametersByCategory(Usage.Category category, Map<String, Object> params) {
    // FIXME: handle no category, meaning all
    if (category == null) category = Usage.Category.CALLS;

    params.put("category", category.toString());
    switch (category) {
      case CALLS:
      case CALLS_INBOUND:
      case CALLS_INBOUND_LOCAL:
      case CALLS_INBOUND_TOLLFREE:
      case CALLS_OUTBOUND:
      case CALLS_CLIENT:
      case CALLS_SIP:
        params.put("tableName", "restcomm_call_detail_records");
        //NB: #1690 display duration as minutes rounded up
        params.put("usageExprPre", "COALESCE( CEIL(SUM(");
        params.put("usageExprCol", "duration");
        params.put("usageExprSuf", ") /60),0) ");
        break;
      case SMS:
      case SMS_INBOUND:
      case SMS_INBOUND_SHORTCODE:
      case SMS_INBOUND_LONGCODE:
      case SMS_OUTBOUND:
      case SMS_OUTBOUND_SHORTCODE:
      case SMS_OUTBOUND_LONGCODE:
        params.put("tableName", "restcomm_sms_messages");
        params.put("usageExprPre", "COUNT(");
        params.put("usageExprCol", "sid");
        params.put("usageExprSuf", ")");
        break;
      case PHONENUMBERS:
      case PHONENUMBERS_TOLLFREE:
      case PHONENUMBERS_LOCAL:
      case SHORTCODES:
      case SHORTCODES_VANITY:
      case SHORTCODES_RANDOM:
      case SHORTCODES_CUSTOMEROWNED:
      case CALLERIDLOOKUPS:
      case RECORDINGS:
      case TRANSCRIPTIONS:
      case RECORDINGSTORAGE:
      case TOTALPRICE:
      default:
        params.put("tableName", "restcomm_call_detail_records");
        //NB: #1690 display duration as minutes rounded up
        params.put("usageExprPre", "COALESCE( CEIL(SUM(");
        params.put("usageExprCol", "duration");
        params.put("usageExprSuf", ") /60),0)");
        break;
    }
    return params;
  }

}
