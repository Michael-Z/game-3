package com.moonic.util;


/**
 * �����ص�������
 * @author John
 */
public interface NetListener 
{
	
	/**
	 * ���سɹ�����
	 */
	public static final byte RESULT_SUCCESS = 0;
	/**
	 * ����ʧ������
	 */
	public static final byte RESULT_FAIL = 1;
	/**
	 * �û��ܾ�����
	 */
	public static final byte RESULT_ACCESSDENIED = 2;
	/**
	 * ����ʧ��
	 */
	public static final byte RESULT_NETFAILURE = 3;
	/**
	 * �����쳣
	 */
	public static final byte RESULT_OTHERERROR = 4;
	
	/**
	 * ����ص�
	 * @param act ��������
	 * @param result ���ؽ��
	 * @param strData �����ַ�����Ϣ
	 */
	public abstract void callBack(int act, int result, String strData);
	public abstract void callBack(int act, int result, byte[] strData);
}
