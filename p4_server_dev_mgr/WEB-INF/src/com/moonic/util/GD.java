package com.moonic.util;


/**
 * ���Թ��߼�
 * @author John
 */
public class GD {
	public StringBuffer sb = new StringBuffer();
	public boolean save;
	
	/**
	 * ����
	 * @param save
	 */
	public GD(boolean save){
		this.save = save;
	}
	
	/**
	 * ���
	 */
	public void print(String str){
		if(save){
			sb.append(str);
		}
	}
	
	/**
	 * ���
	 */
	public void println(String str){
		if(save){
			sb.append(str+"\r\n");
		}
	}
}
