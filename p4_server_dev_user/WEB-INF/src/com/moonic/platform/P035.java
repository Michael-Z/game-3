package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.NetClient;


/**
 * ����Ϸ
 * @author xinghuilong
 *
 */
public class P035 extends P
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
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			return new ReturnValue(false, platform + "����,��¼��չ�����쳣");
		}
		String token = extendJson.optString("token");

		if (token.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token=" + token + ",extend=" + extend);
			return new ReturnValue(false, platform + "����,ȱ�ٲ���");
		}

		// ע��ʱ�õ���clientId
		String clientId = "96780250";
		// ע��ʱ�õ���client_secret
		String clientSecret = "8f8b0ed284484148b0a714cc14ccb348";

		String url = "https://open.play.cn/oauth/token";

		String sendStr = "client_id=" + clientId + "&client_secret=" + clientSecret + "&code=" + token + "&grant_type=authorization_code";
		//LogBAC.logout("login/" + platform, "sendStr=" + sendStr);
		LogBAC.logout("login/" + platform, "�û���֤url=" + sendStr);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		netClient.setSendBytes(sendStr.getBytes());

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
					//String msg = resultJson.optString("message");
					if (resultJson.has("user_id"))
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						username = resultJson.optString("user_id");
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						return new ReturnValue(false, "�û���֤ʧ��result=" + result);
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��" + ex.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
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
