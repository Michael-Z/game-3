package com.moonic.platform;

import org.json.JSONObject;
import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.NetClient;

/**
 * 360
 * @author
 */
public class P006 extends P
{

	public JSONObject login(String channel, String extend, String username, String password, String ip, String imei, String mac, int loginport, SqlString userSqlStr) throws Exception
	{
		JSONObject returnobj = super.login(channel, extend, username, password, ip, imei, mac, loginport, userSqlStr);
		returnobj.put("channeldata", returnobj.optString("username"));
		return returnobj;
	}

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

		if (token.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token,extend=" + extend);
			return new ReturnValue(false, "platform" + platform + ",ȱ�ٲ���token,extend=" + extend);
		}
		/*appid��202241421
		appkey��dddaeb2de31f7e4c5ac939cefd67dfcf
		appsecret��7a3670342a5bb51f4bce86ec9d8ca1fa�����ܣ�*/

		String url = "https://openapi.360.cn/user/me.json";
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");
		String sendStr = "access_token=" + token + "&fields=" + "id,name";
		LogBAC.logout("login/" + platform, "��¼������������=" + sendStr);

		netClient.ignoreSSL();//����
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
					//{"id":"308524290","name":"XHL_��ɵ���","avatar":"http://u1.qhimg.com/qhimg/quc/48_48/22/02/55/220255dq9816.3eceac.jpg?f=9c73c1a60722c956cc51a18b4ea2f0ee"}
					LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
					if (resultJson.has("id"))
					{
						username = resultJson.optString("id");
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + "û���û�id");
						return new ReturnValue(false, "�û���֤ʧ��msg=" + "û���û�id");
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

}
