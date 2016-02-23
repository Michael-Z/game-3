package com.moonic.platform;

import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.moonic.util.NetClient;

/**
 * ����
 * @author
 */
public class P025 extends P
{
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "platform" + platform + ",ȱ����չ����extend=" + extend);
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "platform" + platform + ",ȱ����չ����extend=" + extend);
		}

		String token = extendJson.optString("token");
		String uid = extendJson.optString("uid");

		if (token.equals("") || uid.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token,extend=" + extend);
			return new ReturnValue(false, "platform" + platform + ",ȱ�ٲ���token,extend=" + extend);
		}

		String app_id = "44940418f0c9b6266b7d";
		String privateKey = "MIICegIBADANBgkqhkiG9w0BAQEFAASCAmQwggJgAgEAAoGBAJKyX5ujSEHi7EneChsgvPeFUFZzapXH4h3nOFdECy9KYFFou0DqAujfsBwGS7ckj1/m9Zzh2+AoKdgpBEiaV++C6GJB906Kg+Qw85hNKGGN1BGOx5fKFxmr7gEu8H+h+3qiA5LMx04romSfcHTsPMROwAZAr7jwjpAWNnvsYttrAgMBAAECgYEAhe5or7YBsHW3eTFJVL8tB2clk+hH9XvKi7agj7vF4mUrTugpnfiIs4gWR1/QHOM6Kzq4H4Vts1e37Gx1dndU7IdYLFQd6rzYzxrNF3IBGsT+QY7vWBX5I3rEHlRLFNbgCQ/Vr3OlGGI4542+ZvdRrxYTn9t8Q+d4qVGBUPYgzVECRQDkHVQGkBLwb+oxULXnHphKtcw736Y+odbWyHbsHBmFNqfo6vSWWJ3gknopm2X/fHsT0hvjEzhKMLUryEccyGn/EubaNwI9AKShIyosZMSy3gepGTbXNR+dxYHqB8RKKpNa4nEB6WA5B3Y5kuRCQ5P4yxfXhXfg9st/v+gn/AIYaOaebQJER4LOMZR+63MaJsWXkNGtilY9pVNyaVYQ5JpJdbFFatZ8gu8NKAi1zY390AdQTWx3pPoMFf6TeeaVFRme+W32lKkXR7cCPEGYlwtZzvj1EDTbMtnAHEGxup3OIzQaHSbDotBGY7fcYVXBEP90S7rs+fdQ/7RJZeylxVB6hhZLpZimbQJEUNvE+XT5g2uv1uYEtvFIkRyFzR2u7deTqKAxWVA1wbyYk6DahwqR5hXQ6DYFaF5f7vyRqk5khcT3Wn1Y55gql561+/g=";// ˽Կ

		String time = System.currentTimeMillis() / 1000 + "";

		Map<String, String> data = new HashMap<String, String>();// ��Ҫ���ô�ǩ��������
		data.put("app_id", app_id);
		data.put("open_uid", uid);
		data.put("access_token", token);
		data.put("timestamp", time);
		data.put("sign_type", "RSA");
		data.put("version", "1.0");

		Map<String, String> map = paramFilter(data);
		//LogBAC.logout("login/" + platform, "map=" + map.toString());
		String content = createLinkString(map);
		//LogBAC.logout("login/" + platform, "content=" + content);
		String sign = createSign(content, privateKey);// signΪ����ǩ��
		//LogBAC.logout("login/" + platform, "sign=" + sign);
		String url = "https://uc.zhangyue.com/open/token/check";
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");
		netClient.ignoreSSL();
		sign = URLEncoder.encode(sign, "UTF-8");
//		uid = URLEncoder.encode(uid, "UTF-8");
//		token = URLEncoder.encode(token, "UTF-8");
//		app_id = URLEncoder.encode(app_id, "UTF-8");
//		time = URLEncoder.encode(time, "UTF-8");

		String sendStr = "app_id=" + app_id + "&open_uid=" + uid + "&access_token=" + token + "&timestamp=" + time + "&sign_type=" + ALGORITHM + "&sign=" + sign + "&version=1.0";
		LogBAC.logout("login/" + platform, "��¼������������=" + sendStr);

		netClient.setSendBytes(sendStr.getBytes());
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				String result = "";
				try
				{
					result = new String(rv.binaryData, "UTF-8");
					LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
					if (resultJson.optString("code").equals("0"))
					{
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + resultJson.optString("msg"));
						return new ReturnValue(false, "�û���֤ʧ��msg=" + resultJson.optString("msg"));
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤�쳣ex=" + ex.toString() + ",��¼�յ���������=" + result);
					return new ReturnValue(false, "�û���֤ʧ��" + ex.toString());
				}
			}
			else
			{
				LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��,���ݸ�ʽ�쳣");
				return new ReturnValue(false, "�û���֤ʧ��,���ݸ�ʽ�쳣");
			}
		}
		else
		{
			LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��," + rv.info);
			return new ReturnValue(false, "�û���֤ʧ��," + rv.info);
		}
	}

	public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
	private static final String ALGORITHM = "RSA";
	private static final String CHARSET = "UTF-8";

	/**
	 * ��ȥ�����еĿ�ֵ��ǩ������
	 * 
	 * @param sArray
	 *            ǩ��������
	 * 
	 */
	public static Map<String, String> paramFilter(Map<String, String> sArray)
	{

		Map<String, String> result = new HashMap<String, String>();

		if (sArray == null || sArray.size() <= 0)
		{
			return result;
		}

		for (String key : sArray.keySet())
		{
			String value = sArray.get(key);
			if (value == null || value.equals(""))
			{
				continue;
			}
			result.put(key, value);
		}
		return result;
	}

	/**
	 * ����������Ԫ�����򣬲����ա�����=����ֵ����ģʽ�á�&���ַ�ƴ�ӳ��ַ���
	 * 
	 * @param params
	 *            ��Ҫ���򲢲����ַ�ƴ�ӵĲ�����
	 * @return ƴ�Ӻ��ַ���
	 */
	public static String createLinkString(Map<String, String> params)
	{
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++)
		{
			String key = keys.get(i);
			String value = params.get(key);
			if (i == keys.size() - 1)
			{// ƴ��ʱ�����������һ��&�ַ�
				prestr = prestr + key + "=" + value;
			}
			else
			{
				prestr = prestr + key + "=" + value + "&";
			}
		}

		return prestr;
	}

	public static String createSign(String content, String privateKey)
	{
		try
		{
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initSign(priKey);
			signature.update(content.getBytes(CHARSET));
			byte[] signed = signature.sign();
			return Base64.encodeBase64String(signed);
		}
		catch (Exception e)
		{
			LogBAC.logout("login/025", "e=" + e.toString());
			e.printStackTrace();
		}
		return null;
	}
}
