package com.moonic.util;

import com.ehc.common.ReturnValue;

/**
 * ���ؽ��
 * @author John
 */
public class NetResult {
	public byte servertype;//����
	public int serverid;//ID
	public String name;//����
	public String urlStr;//���͵�ַ
	public byte result;//������
	public byte[] buff;//����������
	public String strData;//�ַ�������
	public ReturnValue rv;//�����ReturnValue�����ʽ
	
	/**
	 * ������
	 */
	public void check() throws Exception {
		if(result==0){
			BACException.throwInstance(strData);
		}
	}
}