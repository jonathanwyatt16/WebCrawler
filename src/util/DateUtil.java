package util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

	public static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd hh:mm:ss.SSS a OOOO";
	public static final String LOG_FILE_DATE_FORMAT = "yyyy-MM-dd";

	public static final int NANOS_IN_MILLI = 1000000;
	public static final int MILLIS_IN_SECOND = 1000;
	public static final int SECONDS_IN_MINUTE = 60;
	public static final int MINUTES_IN_HOUR = 60;
	public static final int HOURS_IN_DAY = 24;
	public static final int DAYS_IN_WEEK = 7;

	public static final int NANOS_IN_MINUTE = NANOS_IN_MILLI * MILLIS_IN_SECOND;
	public static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * SECONDS_IN_MINUTE;
	public static final int MILLIS_IN_HOUR = MILLIS_IN_MINUTE * MINUTES_IN_HOUR;
	public static final int MILLIS_IN_DAY = MILLIS_IN_HOUR * HOURS_IN_DAY;
	public static final int MINUTES_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOUR;
	public static final int MINUTES_IN_WEEK = DAYS_IN_WEEK * MINUTES_IN_DAY;

	private static TimeZone s_tZone;
	private ZonedDateTime m_zdt;

	public DateUtil() {
		m_zdt = ZonedDateTime.now();
	}

	public DateUtil(String stDate, String stDateFormat) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(stDateFormat);
		m_zdt = ZonedDateTime.parse(stDate, dtf);
	}

	public DateUtil(Timestamp stamp) {
		long lTime = stamp.getTime();
		Instant instant = Instant.ofEpochMilli(lTime);
		ZoneId zoneId = ZoneId.of(getSystemTimeZoneId());

		m_zdt = ZonedDateTime.ofInstant(instant, zoneId);
	}

	public DateUtil(int nYear, int nMonth, int nDayOfMonth, int nHourOfDay, int nMinuteOfHour, int nSecondOfMinute,
			int nNanoOfSecond, String stZoneId) {
		if (stZoneId == null)
			stZoneId = getSystemTimeZoneId();

		m_zdt = ZonedDateTime.of(nYear, nMonth, nDayOfMonth, nHourOfDay, nMinuteOfHour, nSecondOfMinute, nNanoOfSecond,
				ZoneId.of(stZoneId));

	}

	public static String getSystemTimeZoneId() {
		if (s_tZone == null) {
			s_tZone = TimeZone.getTimeZone("GMT" + ZonedDateTime.now().getOffset().getId());
		}

		return s_tZone.getID();
	}

	public static DateUtil getNextDailyErrandTime(String stTimeOfDay) {
		String[] aStTimeOfDay = stTimeOfDay.trim().split(":");

		DateUtil dNextTime = new DateUtil();
		dNextTime.m_zdt = dNextTime.m_zdt.withHour(Integer.parseInt(aStTimeOfDay[0]));
		dNextTime.m_zdt = dNextTime.m_zdt.withMinute(Integer.parseInt(aStTimeOfDay[1]));
		dNextTime.m_zdt = dNextTime.m_zdt.withSecond(0);
		dNextTime.m_zdt = dNextTime.m_zdt.withNano(0);

		if (dNextTime.hasPassed())
			dNextTime.addDays(1);

		return dNextTime;
	}

	public static String getCurrentTimeStamp(String stDateFormat) {
		DateUtil dNow = new DateUtil();
		return dNow.toString(stDateFormat);
	}

	public static String getCurrentTimeStamp() {
		return getCurrentTimeStamp(DEFAULT_DATE_FORMAT);
	}

	public Timestamp toTimestamp() {
		return new Timestamp(m_zdt.toInstant().toEpochMilli());
	}

	public Date toDate() {
		return Date.from(m_zdt.toInstant());
	}

	public boolean hasPassed() {
		return !isAfter(new DateUtil());
	}

	public boolean isAfter(DateUtil otherDate) {
		return m_zdt.isAfter(otherDate.m_zdt);
	}

	public void addDays(int nDays) {
		m_zdt = m_zdt.plusDays(nDays);
	}

	public void subtractDays(int nDays) {
		m_zdt = m_zdt.minusDays(nDays);
	}

	public String toString(String format) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
		return m_zdt.format(dtf);
	}

	public String toString() {
		return toString(DEFAULT_DATE_FORMAT);
	}
}
