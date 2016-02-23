package com.moonic.platform;

import java.math.BigInteger;

import org.json.JSONException;
import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.Base64ex;
import com.moonic.util.MD5;
import com.moonic.util.RSAUtil;

public class P034 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false,"�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		//LogBAC.logout("login/" + platform, "�յ��û���֤����" + extend);
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
		username = extendJson.optString("username");
		String uid = extendJson.optString("uid");
		String time = extendJson.optString("time");
		String sign = extendJson.optString("sign");
		
		String appid = "20003900000001200039";
		String appkey = "QTQ0Q0RDOTFBNTlGOTNDQUM4NzAzMzM5NENBOUZEQzRFNkIzRTQ2Nk1UY3hPVGd3TVRjME5qVTROVEUyTlRnMU1URXJNVE01TXprMU1UUTBNRE0xTkRnMk1qZ3lNakE1TnpZeE9UTTNOekUzT1RrME16TTVOVGt4";
		
		if (validSign(Sign_Login_Callback(appid, time, username, uid), sign, appkey))
		{
			//��¼��ǩ�ɹ�
			return new ReturnValue(true, username);
		}
		else
		{
			//��¼��ǩʧ��
			return new ReturnValue(false,"�û���֤ʧ��,username" + username + ",uid=" + uid + ",time=" + time + ",sign=" + sign);
		}
	}
	
	public static boolean validSign(String transdata, String sign, String key)
	{
		try
		{
//			LogBAC.logout("login_error", "platform=" + platform + ",transdata=" + transdata);
//			LogBAC.logout("login_error", "platform=" + platform + ",sign=" + sign);
//			LogBAC.logout("login_error", "platform=" + platform + ",key=" + key);
			String md5Str = MD5.encode(transdata);
//			LogBAC.logout("login_error", "platform=" + platform + ",md5Str=" + md5Str);

			String decodeBaseStr = Base64ex.decode(key);

			String[] decodeBaseVec = decodeBaseStr.replace('+', '#').split("#");

			String privateKey = decodeBaseVec[0];
			String modkey = decodeBaseVec[1];
			//System.out.println("privateKey="+privateKey);
			//System.out.println("modkey="+modkey);
//			LogBAC.logout("login_error", "platform=" + platform + ",privateKey=" + privateKey);
//			LogBAC.logout("login_error", "platform=" + platform + ",modkey=" + modkey);

			String reqMd5 = RSAUtil.decrypt(sign, new BigInteger(privateKey), new BigInteger(modkey));
			//System.out.println("md5Str="+md5Str);
//			System.out.println("reqMd5="+reqMd5);
//			LogBAC.logout("login_error", "platform=" + platform + ",reqMd5=" + reqMd5);
			if (md5Str.equals(reqMd5))
			{
				return true;
			}
			else
			{
				return false;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public static String Sign_Login_Callback(String appid, String CurrentTime, String userName, String userID)
	{
//		LogBAC.logout("login_error", "platform=" + platform + ",appid=" + appid);
//		LogBAC.logout("login_error", "platform=" + platform + ",CurrentTime=" + CurrentTime);
//		LogBAC.logout("login_error", "platform=" + platform + ",userName=" + userName);
//		LogBAC.logout("login_error", "platform=" + platform + ",userID=" + userID);
		StringBuilder sb = new StringBuilder();
		// ������������
		sb.append(appid).append(CurrentTime).append(userName).append(userID);
		String unSignValue = sb.toString();
//		LogBAC.logout("login_error", "platform=" + platform + ",unSignValue=" + unSignValue);
		return unSignValue;
	}

	public static void main(String[] args)
	{
		String username = "unity";
		String uid = "9d4afdc777af952cb9fa8165b85ec58f";
		String time = "1383902095260";
		String sign = "1c554e40f8c4fdaef59d77bda4913afa 486323587937c41d927669015bb7df09 053acbe857bd437b0927f2dd2a571ecb";

		String appid = "20003900000001200039";
		String appkey = "QTQ0Q0RDOTFBNTlGOTNDQUM4NzAzMzM5NENBOUZEQzRFNkIzRTQ2Nk1UY3hPVGd3TVRjME5qVTROVEUyTlRnMU1URXJNVE01TXprMU1UUTBNRE0xTkRnMk1qZ3lNakE1TnpZeE9UTTNOekUzT1RrME16TTVOVGt4";

		validSign(Sign_Login_Callback(appid, time, username, uid), sign, appkey);

		String a = "";
		JSONObject json;
		String orderNo = null;
		try
		{
			json = new JSONObject(a);

			orderNo = json.optString("exorderno");
			int money = json.optInt("money");
			int result = json.optInt("result");
			System.out.println("orderNo=" + orderNo);
			System.out.println("money=" + money);
			System.out.println("result=" + result);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
