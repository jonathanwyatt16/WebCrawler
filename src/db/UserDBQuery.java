package db;

import util.DateUtil;

public class UserDBQuery extends DBQuery {
	
	private static String[] s_aStForbiddenStrings = {"insert ", "update ", "delete ", "drop ", "truncate "};
	
	public UserDBQuery(String stQuery) {
		m_stQuery = stQuery;
		m_stName = DateUtil.getCurrentTimeStamp() + " user query";
	}
	
	public boolean isAllowable() {
		String stQuery = m_stQuery.toLowerCase().trim();
		
		for (String stForbidden : s_aStForbiddenStrings) {
			if (stQuery.contains(stForbidden))
				return false;
		}
		
		return true;
	}	
}
