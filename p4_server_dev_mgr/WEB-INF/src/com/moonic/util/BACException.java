package com.moonic.util;

/**
 * ���ݿ�����쳣
 * @author John
 */
public class BACException extends Exception {
	private static final long serialVersionUID = -6840493542701775332L;
	
	/**
	 * ����
	 */
	public BACException(String message){
		super(message);
	}
	
	/**
	 * �쳣��ӡ(��д���෽�������ڿ���̨������쳣��Ϣ)
	 */
	public void printStackTrace() {}
	
	/**
	 * ��д
	 */
	public String toString() {
		return super.getMessage();
	}

	//---------��̬��---------
	
	/**
	 * �׳��쳣����
	 */
	public static void throwInstance(String message) throws BACException{
		throw new BACException(message);
	}
	
	/**
	 * �׳��쳣���������������
	 */
	public static void throwAndOutInstance(String message) throws BACException{
		System.out.println("exception-"+message);
		throw new BACException(message);
	}
	
	/**
	 * �׳��쳣���󲢴�ӡ��ջ
	 */
	public static void throwAndPrintInstance(String message) throws Exception{
		throw new Exception(message);
	}
}
