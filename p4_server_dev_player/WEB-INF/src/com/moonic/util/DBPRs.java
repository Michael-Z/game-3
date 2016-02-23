package com.moonic.util;

import java.sql.Types;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ��������
 * @author John
 */
public abstract class DBPRs {
	protected String tab;//��
	protected String where;//����
	protected JSONObject colobj;//�ֶ�
	protected JSONArray coltype;//�ֶ�����
	protected JSONArray json;//��ǰָ���������
	protected JSONArray joinarr = new JSONArray();//���ϲ�ѯ
	
	/**
	 * ����
	 */
	public DBPRs(String tab, String where){
		this.tab = tab;
		this.where = where;
		colobj = DBUtil.colmap.optJSONObject(tab);
		coltype = DBUtil.coltypemap.optJSONArray(tab);
	}
	
	/**
	 * ���ϲ�ѯ
	 */
	public void join(String mainColumn, DBPsRs subRs, String subColumn){
		JSONArray arr = new JSONArray();
		arr.add(mainColumn);
		arr.add(subRs);
		arr.add(subColumn);
		joinarr.add(arr);
	}
	
	/**
	 * ��ȡ���ϲ�ѯ����
	 * @param index ��Join˳����±꣬��0��ʼ
	 */
	public DBPaRs getJoinARs(int index) throws Exception {
		JSONArray join = joinarr.optJSONArray(index);
		return ((DBPsRs)join.opt(1)).query(join.optString(2)+"="+getString(join.optString(0)));
	}
	
	/**
	 * ��ȡֵ
	 */
	public String getString(String key){
		if(json == null){
			throw new RuntimeException("������Ѻľ�[TAB:"+tab+",WHERE:"+where+"]");
		}
		if(!colobj.has(key)){
			throw new RuntimeException("��"+key+"����Ч��ʶ��[TAB:"+tab+",WHERE:"+where+"]");
		}
		int index = colobj.optInt(key);
		Object obj = json.opt(index);
		int type = coltype.optInt(index);
		if(type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP){
			return obj!=null?MyTools.getTimeStr(Long.valueOf(obj.toString())):null;
		} else {
			return obj!=null?obj.toString():null;
		}
	}
	
	/**
	 * ��ȡֵ
	 */
	public int getInt(String key){
		return (int)getDouble(key);
	}
	
	/**
	 * ��ȡֵ
	 */
	public long getLong(String key){
		return (long)getDouble(key);
	}
	
	/**
	 * ��ȡֵ
	 */
	public byte getByte(String key){
		return (byte)getDouble(key);
	}
	
	/**
	 * ��ȡֵ
	 */
	public short getShort(String key){
		return (short)getDouble(key);
	}
	
	/**
	 * ��ȡֵ
	 */
	public double getDouble(String key){
		String val = getString(key);
		return val!=null?Double.valueOf(val):0;
	}
	
	/**
	 * ��ȡֵ
	 */
	public long getTime(String key){
		return MyTools.getTimeLong(getString(key));
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONObject getJsonobj() throws Exception {
		JSONObject jsonobj = new JSONObject();
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = colobj.keys();
		while(iterator.hasNext()){
			String key = iterator.next();
			jsonobj.put(key, json.optString(colobj.optInt(key)));
		}
		return jsonobj;
	}
}
