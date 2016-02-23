package com.moonic.platform;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.NetClient;

/**
 * 17173
 * @author 
 */
public class P043 extends P {
	
	public JSONObject login(String channel, String extend, String username, String password, String ip, String imei, String mac, int loginport, SqlString userSqlStr) throws Exception {
		JSONObject returnobj = super.login(channel, extend, username, password, ip, imei, mac, loginport, userSqlStr);
		returnobj.put("channeldata", returnobj.optString("username"));
		return returnobj;
	}

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false,"�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			return new ReturnValue(false,platform + "����,��¼��չ�����쳣");
		}
//		username = extendJson.optString("username");
		String token = extendJson.optString("token");

		if (/*username.equals("") || */token.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���username=" + username + ",token=" + token + ",extend=" + extend);
			return new ReturnValue(false,platform + "����,ȱ�ٲ���");
		}

		/** �ӿڵ�ַ*/
		String BASE_URL = "http://gop.37wanwan.com/api/";
		/** �ӿ�����*/
		String apiName = "verifyUser";
		/** ��Ϸ��ƽ̨�Ϸ����Ψһ��ʶ*/
		String gameId = "39";
		/** ��Ϸƽ̨����Ϸ�����һ��˽���ַ���,ֻ����Ϸ�����̺�ƽ̨֪��������ͨ�ż���У�� */
		String gameSecret = "4a4adb7c34d6be5214d2b3e76d1f529e";
		/** �����̵�����*/
		String vendor = extendJson.optString("vendor");
		/** ʱ��� */
		String date;
		/** SDK�汾*/
		String version = extendJson.optString("version");

		String url = BASE_URL + apiName;//�����ַ+�ӿ���
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		HashMap<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("token", token);
		// ��������ͷ
		netClient.addHttpHead("Accept", "application/json; version=" + version);
		/** ����ʱ��� */
		date = getDate();
		netClient.addHttpHead("Date", date);
		/** ��������ͷ�Ĳ�������Ҫencode*/
		String headerParam = sortParams(paramsMap);
		netClient.addHttpHead("Authentication", getAuthentication(gameId, gameSecret, vendor, date, apiName, headerParam));
		/** ���������������Ҫencode */
		String bodyParam = sortEncoderParams(paramsMap);
//		LogBAC.logout("login/" + platform, "sendStr=" + bodyParam);

		netClient.setSendBytes(bodyParam.getBytes());
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					String result = new String(rv.binaryData, "UTF-8");
					LogBAC.logout("login/" + platform, "result=" + result);
					JSONObject resultJson = new JSONObject(result);
					String usergameid = resultJson.optString("usergameid");
					//String errcode = resultJson.optString("errcode");
					//String msg = resultJson.optString("message");
					if (usergameid != null && !usergameid.equals(""))
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, usergameid);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						return new ReturnValue(false,"�û���֤ʧ��result=" + result);
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��" + ex.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
					return new ReturnValue(false,"�û���֤ʧ��" + ex.toString());
				}
			}
			else
			{
				LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��,���ݸ�ʽ�쳣");
				return new ReturnValue(false,"�û���֤ʧ��,���ݸ�ʽ�쳣");
			}
		}
		else
		{
			LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��," + rv.info);
			return new ReturnValue(false,"�û���֤ʧ��," + rv.info);
		}
	}
	
	/**
	 * �Բ�����������
	 */
	public static String sortParams(HashMap<String, String> params)
	{
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (String key : keys)
		{
			String value = params.get(key);
			prestr = prestr + key + "=" + value + "&";
		}
		prestr = prestr.substring(0, prestr.length() - 1);
		return prestr;
	}

	/**
	 * �Բ�����������+encode
	 */
	public static String sortEncoderParams(HashMap<String, String> params) throws UnsupportedEncodingException
	{
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);
		String prestr = "";
		for (String key : keys)
		{
			String value = params.get(key);
			prestr = prestr + key + "=" + URLEncoder.encode(value, "utf-8") + "&";
		}
		prestr = prestr.substring(0, prestr.length() - 1);
		return prestr;
	}

	/**
	 * ����ʱ���
	 */
	public static String getDate()
	{
		SimpleDateFormat dfs = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ", Locale.US);
		return dfs.format(new Date()).toString() + "GMT";
	}

	/**
	 * ������Ȩ��
	 */
	public static String getAuthentication(String gameId, String gameSecret, String vendor, String date, String apiName, String params)
	{
		String sign = getSign(date, apiName, params, gameSecret);
		String authentication = vendor + " " + gameId + ":" + sign;
		return authentication;
	}

	/**
	 * ����ǩ��
	 */
	private static String getSign(String date, String apiName, String params, String gameSecret)
	{
		String str = date + ":" + apiName + ":" + params + ":" + gameSecret;
		return md5(str);
	}

	/**
	 * MD5����
	 */
	public static String md5(String data)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("md5");
			md.update(data.getBytes());
			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : digest)
			{
				sb.append(String.format("%02x", b & 0xFF));
			}
			return sb.toString();
		}
		catch (Exception e)
		{
			return "";
		}
	}
}
