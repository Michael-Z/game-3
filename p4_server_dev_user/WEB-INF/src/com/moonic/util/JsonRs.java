package com.moonic.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JsonRs
 * @author John
 */
public class JsonRs {
	private JSONArray jsonarr;
	private JSONObject obj;
	private int index = -1;
	
	/**
	 * ����
	 */
	public JsonRs(JSONArray jsonarr){
		this.jsonarr = jsonarr;
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
	public boolean next(){
		boolean result = false;
		if(index < jsonarr.length()-1){
			index++;
			obj = jsonarr.optJSONObject(index);
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
	 * ����ָ��λ��
	 */
	public void setRow(int ind){
		if(ind < 0 || ind > jsonarr.length()){
			throw new RuntimeException("ָ�볬������("+ind+")");
		}
		if(jsonarr!=null && ind>=1){
			index = ind-1;
			obj = jsonarr.optJSONObject(index);
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
		obj = null;
		index = -1;
	}
	
	/**
	 * �������
	 */
	public void last(){
		if(jsonarr != null && jsonarr.length() > 0){
			index = jsonarr.length()-1;
			obj = jsonarr.optJSONObject(index);
		}
	}
	
	/**
	 * ��ȡֵ
	 */
	public Object get(String key){
		if(obj == null){
			throw new RuntimeException("������Ѻľ�");
		}
		return obj.opt(key);
		/*
		if(obj.has(key)){
			return obj.opt(key);
		}
		throw new RuntimeException("��Ч��ʶ����"+key+"��");
		*/
	}
	
	/**
	 * ��ȡֵ
	 */
	public String getString(String key){
		Object obj = get(key);
		return obj!=null?obj.toString():null;
	}
	
	/**
	 * ��ȡֵ
	 */
	public byte[] getBytes(String key){
		Object obj = get(key);
		return obj!=null?(byte[])obj:null;
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
}
