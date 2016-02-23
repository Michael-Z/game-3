package com.moonic.util;

import org.json.JSONArray;

/**
 * ��׼��������
 * @author John
 */
public class DBPsRs extends DBPRs {
	private JSONArray jsonarr;//�����
	private int index;//ָ��
	
	/**
	 * ����
	 */
	public DBPsRs(String tab, String where, JSONArray jsonarr){
		super(tab, where);
		this.jsonarr = jsonarr;
		index = -1;
	}
	
	/**
	 * �Ƿ�������
	 */
	public boolean have(){
		return count()>0;
	}
	
	/**
	 * ��һԪ��
	 */
	public boolean next() {
		boolean result = false;
		if(index < jsonarr.length()-1){
			index++;
			json = jsonarr.optJSONArray(index);
			result = true;
		}
		return result;
	}
	
	/**
	 * ��ȡ������
	 */
	public int count(){
		if(jsonarr != null){
			return jsonarr.length();
		} else {
			return 0;
		}
	}
	
	/**
	 * ���
	 */
	public double sum(String column) {
		int row = getRow();
		double sum = 0;
		beforeFirst();
		while(next()){
			sum += getDouble(column);
		}
		setRow(row);//��ԭָ��λ��
		return sum;
	}
	
	/**
	 * ��ȡ���ϲ�ѯ���󼯺�
	 */
	public DBPsRs getJoinSRs(int index) throws Exception {
		JSONArray join = joinarr.optJSONArray(index);
		String subColumn = join.optString(2);
		String mainColumn = join.optString(0);
		JSONArray queryarr = new JSONArray();
		int row = getRow();
		beforeFirst();
		while(next()){
			JSONArray subarr = DBUtil.jsonQuery(tab, jsonarr, subColumn+"="+getString(mainColumn), null, 0, 0);
			MyTools.combJsonarr(queryarr, subarr);
		}
		setRow(row);//��ԭָ��λ��
		return new DBPsRs(tab, null, queryarr);
	}
	
	/**
	 * ����ָ��λ��
	 */
	public void setRow(int ind){
		if(ind < 0 || ind > jsonarr.length()){
			throw new RuntimeException("ָ�볬������("+ind+")");
		}
		if(jsonarr!=null && ind>=0){
			index = ind-1;
			if(index >= 0){
				json = jsonarr.optJSONArray(index);		
			} else {
				json = null;
			}
		}
	}
	
	/**
	 * ��ȡָ��λ��
	 */
	public int getRow(){
		return index+1;
	}
	
	/**
	 * ������ǰ
	 */
	public void beforeFirst(){
		json = null;
		index = -1;
	}
	
	/**
	 * �������
	 */
	public void last(){
		if(jsonarr != null && jsonarr.length() > 0){
			index = jsonarr.length()-1;
			json = jsonarr.optJSONArray(index);
		}
	}
	
	/**
	 * ��ѯ
	 */
	public DBPaRs query(String where) throws Exception {
		return new DBPaRs(tab, where, DBUtil.jsonQuery(tab, jsonarr, where, null, 0, 0));
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getJsonarr() throws Exception {
		JSONArray newarr = new JSONArray();
		int row = getRow();
		beforeFirst();
		while(next()){
			newarr.add(getJsonobj());
		}
		setRow(row);//��ԭָ��λ��
		return newarr;
	}
	
	/**
	 * ��д
	 */
	public String toString() {
		return DBUtil.getFormatStr(tab, jsonarr);
	}
}
