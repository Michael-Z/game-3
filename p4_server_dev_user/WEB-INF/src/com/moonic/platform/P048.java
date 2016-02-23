package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * ��ţ
 * @author 
 */
public class P048 extends P {
	
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
		username = extendJson.optString("username");

		String AppId = "132537";
		String Act = "4";
		String Uin = extendJson.optString("Uin");
		String SessionId = extendJson.optString("SessionId");
		String AppKey = "a3290dcdb1af6efd7c598285bcca4f40ed1b82e8";

		String string = AppId + Act + Uin + SessionId + AppKey;
		LogBAC.logout("login/" + platform, "��ǩǰ��=" + string);
		String Sign = MD5.encode(string).toLowerCase();
		LogBAC.logout("login/" + platform, "��ǩ���=" + Sign);

		if (username.equals("") || Uin.equals("") || SessionId.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���username=" + username + ",Uin=" + Uin + ",SessionId=" + SessionId + ",extend=" + extend);
			return new ReturnValue(false,platform + "����,ȱ�ٲ���");
		}

		String url = "http://api.app.snail.com/usercenter/ap";
		String sendStr = "AppId=" + AppId + "&Act=" + Act + "&Uin=" + Uin + "&SessionId=" + SessionId + "&Sign=" + Sign;
		//LogBAC.logout("login/" + platform, "sendStr=" + sendStr);
		String urlPath = url + "?" + sendStr;
		LogBAC.logout("login/" + platform, "�û���֤url=" + urlPath);
		NetClient netClient = new NetClient();
		netClient.setAddress(urlPath);

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
					String code = resultJson.optString("ErrorCode");//1��Ч
					//String msg = resultJson.optString("ErrorDesc");
					if (code.equals("1"))
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
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
}
