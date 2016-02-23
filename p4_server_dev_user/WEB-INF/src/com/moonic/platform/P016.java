package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.NetClient;

/**
 * Ӧ�û�
 * @author 
 */
public class P016 extends P
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

		String ticket = extendJson.optString("ticket");
		String app_id = extendJson.optString("app_id");
		String app_key = extendJson.optString("app_key");

		if (ticket.equals("") || app_id.equals("") || app_key.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token,extend=" + extend);
			return new ReturnValue(false, "platform" + platform + ",ȱ�ٲ���token,extend=" + extend);
		}

		String uidCheckUrl = "http://api.appchina.com/appchina-usersdk/user/get.json";
		String url = uidCheckUrl + "?app_id=" + app_id + "&app_key=" + app_key + "&ticket=" + ticket;
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		//LogBAC.logout("login/" + platform, "��¼������������=" + url);
		netClient.setContentType("application/x-www-form-urlencoded");
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				String result = "";
				try
				{
					result = new String(rv.binaryData, "UTF-8");

					//{ "data": {"nick_name": "jakyzhang", "user_name": null, "phone": null, "avatar_url": null, "email": "jakyzhang@live.com", "ticket": "e84b04d2-8866-11e1-8501-782bcb60a987", "state": "state", "user_id": 16058, "actived": true }, "status": 0, "message": "OK" }
					//LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
					JSONObject json = resultJson.optJSONObject("data");
					if (resultJson.optString("message").equals("OK"))
					{
						username = json.optString("user_id");
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + "û���û�user_id");
						return new ReturnValue(false, "�û���֤ʧ��msg=" + "û���û�user_id");
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
