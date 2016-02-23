package com.moonic.util;

import org.json.JSONArray;

/**
 * �����ݻ�������
 * @author John
 */
public class DBPaRs extends DBPRs {
	
	/**
	 * ����
	 */
	public DBPaRs(String tab, String where, JSONArray jsonarr) {
		super(tab, where);
		this.json = jsonarr.optJSONArray(0);
	}
	
	/**
	 * ����
	 */
	public DBPaRs(DBPsRs rs) {
		super(rs.tab, rs.where);
		if(rs.next()){
			json = rs.json;
		}
	}
	
	/**
	 * �Ƿ�������
	 */
	public boolean exist(){
		return json!=null;
	}
	
	/**
	 * ��д
	 */
	public String toString() {
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(json);
		return DBUtil.getFormatStr(tab, jsonarr);
	}
}
