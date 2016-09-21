package jira;

import util.DateUtil;

public class JiraDate extends DateUtil {

	public static final String JIRA_DATE_FORMAT_1 = "MM/dd/yyyy hh:mm a VV";
	public static final String JIRA_DATE_FORMAT_2 = "yyyy/MM/dd HH:mm";

	public JiraDate(String stDate) {
		super(stDate + " " + getSystemTimeZoneId(), JIRA_DATE_FORMAT_1);
	}
}
