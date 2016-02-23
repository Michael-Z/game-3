package com.moonic.util;

import java.security.MessageDigest;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.sun.crypto.provider.SunJCE;

public class Des3Util extends SecurityUtil
{

	private static final String ALGORITHM = "DESede";
	private static SunJCE sunJCE;

	// ���� �����㷨,���� DES,DESede,Blowfish
	// keybyteΪ������Կ������Ϊ24�ֽ�
	// srcΪ�����ܵ����ݻ�������Դ��
	public byte[] encrypt(byte[] keybyte, byte[] src)
	{
		try
		{
			// ������Կ
			SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
			// ����
			Cipher c1 = Cipher.getInstance(ALGORITHM);
			c1.init(Cipher.ENCRYPT_MODE, deskey);
			return c1.doFinal(src);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	// keybyteΪ������Կ������Ϊ24�ֽ�
	// srcΪ���ܺ�Ļ�����
	public byte[] decrypt(byte[] keybyte, byte[] src)
	{
		try
		{
			// ������Կ
			SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
			// ����
			Cipher c1 = Cipher.getInstance(ALGORITHM);
			c1.init(Cipher.DECRYPT_MODE, deskey);
			return c1.doFinal(src);
		}
		catch (java.lang.Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	// ���� Base64��3DES����Ϣ�壩��
	public static String decrypt(String args, String key)
	{
		try
		{
			Des3Util d3u = new Des3Util();

			Security.addProvider(new com.sun.crypto.provider.SunJCE());

			return d3u.decryptFromBase64(key, args, "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
		return null;
	}

	// ���� Base64��3DES����Ϣ�壩��
	public static String encrypt(String body, String key)
	{
		try
		{
			Des3Util d3u = new Des3Util();

			Security.addProvider(new com.sun.crypto.provider.SunJCE());

			return d3u.encryptToBase64(key, body, "UTF-8");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	// ��Ϣǩ��
	public static String getSigned(String buf)
	{
		try
		{
			byte[] input;
			input = buf.getBytes("UTF-8");
			MessageDigest alga = MessageDigest.getInstance("MD5");
			// MessageDigest alga = MessageDigest.getInstance("SHA-1");
			alga.update(input);
			byte[] md5Hash = alga.digest();
//		System.out.println("MD5:" + new String(md5Hash));
			if (md5Hash != null)
			{
				return (new sun.misc.BASE64Encoder()).encode(md5Hash);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
