package com.moonic.util;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RSAUtil {
	/**
	 * 
	 */
	public static final int NUMBIT = 64;

	/**
	 * ��ȡ�������
	 * 
	 * @return
	 */
	public static BigInteger getPrimes() {
		return BigInteger.probablePrime(NUMBIT, new Random());
	}

	/**
	 * ͨ��P,Q����Nֵ
	 * 
	 * @param һ������p
	 * @param һ������q
	 * @return ����P*Q��ֵn
	 */
	public static BigInteger getN(BigInteger p, BigInteger q) {
		return p.multiply(q);
	}

	/**
	 * ͨ��P,Q����ranֵ modkey
	 * 
	 * @param һ������p
	 *            ,����Ϊ��
	 * @param һ������q
	 *            ,����Ϊ��
	 * @return ����(P-1)*(Q-1)��ֵran
	 */
	public static BigInteger getRan(BigInteger p, BigInteger q) {
		return (p.subtract(BigInteger.ONE))
				.multiply(q.subtract(BigInteger.ONE));
	}

	/**
	 * ��ȡ��Կ(128λ)
	 * <p>
	 * �����������: <code><font color="red"><br>
	 * BigInteger p = RSAUtil.getPrimes();<br>
	 * BigInteger q = RSAUtil.getPrimes();<br>
	 * BigInteger ran = RSAUtil.getRan(p, q);<br>
	 * BigInteger n = RSAUtil.getN(p, q);//modkey -- Nֵ<br>
	 * BigInteger pKey = RSAUtil.getPublicKey(ran);//publicKey -- ��Կ<br>
	 * </font></code>
	 * 
	 * @param ran
	 *            ͨ��getRan��̬�������������ֵ
	 * @return
	 */
	public static BigInteger getPublicKey(BigInteger ran) {
		BigInteger temp = null;
		BigInteger e = BigInteger.ZERO;
		do {
			temp = BigInteger.probablePrime(NUMBIT, new Random());
			/*
			 * �������һ�������������Ƿ���ran�Ĺ�Լ��Ϊ1�����Ϊ1��e=temp�˳�ѭ��
			 */
			if ((temp.gcd(ran)).equals(BigInteger.ONE)) {
				e = temp;
			}
		} while (!((temp.gcd(ran)).equals(BigInteger.ONE)));

		return e;
	}

	/**
	 * ��ȡ˽Կ(128λ)
	 * <p>
	 * �����������: <code><font color="red"><br>
	 * BigInteger priKey = RSAUtil.getPrivateKey(ran,pKey);//ran�ǲ�����Կ��ran����,pKey�ǹ�Կ<br>
	 * </font></code>
	 * 
	 * @param ran
	 *            ͨ��getRan��̬�������������ֵ
	 * @param publicKey
	 *            ��Կ
	 * @return
	 */
	public static BigInteger getPrivateKey(BigInteger ran, BigInteger publicKey) {
		return publicKey.modInverse(ran);
	}

	/**
	 * �����Ľ��м��ܣ�ͨ����ʽ ����=(���ģ�e���ݣ� mod m)
	 * 
	 * @param ����em
	 *            ��Ϊ��
	 * @param ��Կe
	 * @param ģ��n
	 * @return ���ܺ������encodeM
	 */
	private static BigInteger[] encodeRSA(byte[][] em, BigInteger e,
			BigInteger n) {
		BigInteger[] encodeM = new BigInteger[em.length];
		for (int i = 0; i < em.length; i++) {
			encodeM[i] = new BigInteger(em[i]);
			encodeM[i] = encodeM[i].modPow(e, n);
		}
		return encodeM;
	}

	/**
	 * �����Ľ��н��ܣ�ͨ����ʽ ���� = �����ģ�d���ݣ�mod m��
	 * 
	 * @param ����encodeM
	 *            ��Ϊ��
	 * @param ��Կd
	 * @param ģ��n
	 * @return ���ܺ������dencodeM
	 */
	private static byte[][] dencodeRSA(BigInteger[] encodeM, BigInteger d,
			BigInteger n) {
		byte[][] dencodeM = new byte[encodeM.length][];
		int i;
		int lung = encodeM.length;
		for (i = 0; i < lung; i++) {
			dencodeM[i] = encodeM[i].modPow(d, n).toByteArray();
		}
		return dencodeM;
	}

	/**
	 * ������byte[]arrayByte,ת��Ϊ��ά����,�ֶμ���/����
	 * 
	 * @param arrayByte
	 * @param numBytes
	 * @return arrayEm ����Ϊ��
	 */
	private static byte[][] byteToEm(byte[] arrayByte, int numBytes) {
		/**
		 * �ֶ�
		 */
		int total = arrayByte.length;
		int dab = (total - 1) / numBytes + 1, iab = 0;
		byte[][] arrayEm = new byte[dab][];
		int i, j;
		for (i = 0; i < dab; i++) {
			arrayEm[i] = new byte[numBytes];

			for (j = 0; j < numBytes && iab < total; j++, iab++) {
				arrayEm[i][j] = arrayByte[iab];
			}
			/**
			 * ����ո��ַ�(ox20=32)
			 */
			for (; j < numBytes; j++) {
				arrayEm[i][j] = ' ';
			}
		}
		return arrayEm;
	}

	/**
	 * 
	 * ����ά����ת��Ϊһά����
	 * 
	 * @param arraySenS
	 * @return
	 */
	private static byte[] StringToByte(byte[][] arraySenS) {
		int i, dab = 0;
		for (i = 0; i < arraySenS.length; i++) {
			if (arraySenS[i] == null) {
				return null;
			}
			dab = dab + arraySenS[i].length;
		}
		List<Byte> listByte = new ArrayList<Byte>();
		int j;
		for (i = 0; i < arraySenS.length; i++) {
			for (j = 0; j < arraySenS[i].length; j++) {
				if (arraySenS[i][j] != ' ') {
					listByte.add(arraySenS[i][j]);
				}
			}
		}
		Byte[] arrayByte = listByte.toArray(new Byte[0]);
		byte[] result = new byte[arrayByte.length];
		for (int k = 0; k < arrayByte.length; k++) {
			result[k] = (arrayByte[k]).byteValue();
		}
		listByte = null;
		arrayByte = null;
		return result;
	}

	/**
	 *<font color="red"> ���ܷ���(���ʹ���˲�����Կ����,����Ҫͬ��ʹ�ô˷�������)</font>
	 * 
	 * @param source
	 *            �� ����
	 * @param e
	 *            ��Կ
	 * @param n
	 *            modkey
	 * @return ���� ��","
	 * @throws Exception
	 */
	public static String encrypt(String source, BigInteger e, BigInteger n)
			throws Exception {
		return encrypt(source, e, n, NUMBIT * 2);
	}

	/**
	 ** ���ܷ���
	 * 
	 * @param source
	 *            �� ����
	 * @param e
	 *            ��Կ
	 * @param n
	 * @return ���� ��","
	 * @throws Exception
	 */
	public static String encrypt(String source, BigInteger e, BigInteger n,
			int numBit) throws Exception {
		String text = URLEncoder.encode(source, "UTF-8");// Ϊ��֧�ֺ��֡����ֺ�Ӣ�Ļ���
		if (text == null || "".equals(text)) {
			throw new Exception("����ת��ΪUTF-8,����ת���쳣!!!");
		}
		byte[] arraySendM = text.getBytes("UTF-8");
		if (arraySendM == null) {
			throw new Exception("����ת��ΪUTF-8,����ת���쳣!!!");
		}
		if (numBit <= 1) {
			throw new Exception("�����λ����������2!!!");
		}
		int numeroByte = (numBit - 1) / 8;
		byte[][] encodSendM = RSAUtil.byteToEm(arraySendM, numeroByte);
		BigInteger[] encodingM = RSAUtil.encodeRSA(encodSendM, e, n);
		StringBuilder encondSm = new StringBuilder();
		for (BigInteger em : encodingM) {
			encondSm.append(em.toString(16));
			encondSm.append(" ");
		}
		return encondSm.toString();
	}

	/**
	 * <font color="red"> �����㷨(���ʹ���˲�����Կ����,����Ҫͬ��ʹ�ô˷�������)</font>
	 * 
	 * @param cryptograph
	 *            :����,��","
	 * @param d
	 *            ˽Կ
	 * @param n
	 *            modkey
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String cryptograph, BigInteger d, BigInteger n)
			throws Exception {
		return decrypt(cryptograph, d, n, NUMBIT * 2);
	}

	/**
	 * �����㷨
	 * 
	 * @param cryptograph
	 *            :����,��","
	 * @param d
	 *            ˽Կ
	 * @param n
	 * @param numBit
	 *            λ��
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String cryptograph, BigInteger d,
			BigInteger n, int numBit) throws Exception {
		String[] chs = cryptograph.split(" ");
		if (chs == null || chs.length <= 0) {
			throw new Exception("���Ĳ�����Ҫ��!!!");
		}
		int numeroToken = chs.length;
		BigInteger[] StringToByte = new BigInteger[numeroToken];
		for (int i = 0; i < numeroToken; i++) {
			StringToByte[i] = new BigInteger(chs[i], 16);
		}
		byte[][] encodeM = RSAUtil.dencodeRSA(StringToByte, d, n);
		byte[] sendMessage = RSAUtil.StringToByte(encodeM);
		String message = new String(sendMessage, "UTF-8");
		String result = URLDecoder.decode(message, "UTF-8");
		return result;
	}

}
