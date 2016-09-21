package db;

public abstract class DBQuery {
	protected String m_stQuery;
	protected String m_stName;
	protected Object[] m_aObjVals;
	
	public String getQuery() {
		return m_stQuery;
	}

	public String getName() {
		return m_stName;
	}
	
	public Object[] getVals() {
		return m_aObjVals;
	}
}
