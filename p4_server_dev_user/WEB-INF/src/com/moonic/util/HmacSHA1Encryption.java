package com.moonic.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HmacSHA1Encryption {


	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";
	/**
	 * ʹ�� HMAC-SHA1 ǩ�������Զ�encryptText����ǩ��
	 * @param encryptText ��ǩ�����ַ���
	 * @param encryptKey ��Կ
	 * @return ���ر����ܺ���ַ���
	 * @throws Exception
	 */
	public static String hmacSHA1Encrypt( String encryptText, String encryptKey ) throws Exception
	{
		byte[] data = encryptKey.getBytes( ENCODING );
		// ���ݸ������ֽ����鹹��һ����Կ,�ڶ�����ָ��һ����Կ�㷨������
		SecretKey secretKey = new SecretKeySpec( data, MAC_NAME );
		// ����һ��ָ�� Mac �㷨 �� Mac ����
		Mac mac = Mac.getInstance( MAC_NAME );
		// �ø�����Կ��ʼ�� Mac ����
		mac.init( secretKey );
		byte[] text = encryptText.getBytes( ENCODING );
		// ��� Mac ����
		byte[] digest = mac.doFinal( text );
		StringBuilder sBuilder = bytesToHexString( digest );
		return sBuilder.toString();
	}
	/**
	 * ת����Hex
	 * 
	 * @param bytesArray
	 */
	public static StringBuilder bytesToHexString( byte[] bytesArray )
	{
		if ( bytesArray == null )
		{
			return null;
		}
		StringBuilder sBuilder = new StringBuilder();
		for ( byte b : bytesArray )
		{
			String hv = String.format("%02x", b);
//			String hv = Integer.toHexString( b & 0xFF );
			sBuilder.append( hv );
		}
		return sBuilder;
	}
	/**
	 * ʹ�� HMAC-SHA1 ǩ�������Զ�encryptText����ǩ��
	 * 
	 * @param encryptData ��ǩ�����ַ���
	 * @param encryptKey ��Կ
	 * @return ���ر����ܺ���ַ���
	 * @throws Exception
	 */
	public static String hmacSHA1Encrypt( byte[] encryptData, String encryptKey ) throws Exception
	{
		byte[] data = encryptKey.getBytes( ENCODING );
		// ���ݸ������ֽ����鹹��һ����Կ,�ڶ�����ָ��һ����Կ�㷨������
		SecretKey secretKey = new SecretKeySpec( data, MAC_NAME );
		// ����һ��ָ�� Mac �㷨 �� Mac ����
		Mac mac = Mac.getInstance( MAC_NAME );
		// �ø�����Կ��ʼ�� Mac ����
		mac.init( secretKey );

		// ��� Mac ����
		byte[] digest = mac.doFinal( encryptData );

		StringBuilder sBuilder = bytesToHexString( digest );

		return sBuilder.toString();
	}
}
