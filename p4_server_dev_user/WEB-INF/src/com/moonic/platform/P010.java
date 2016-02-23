package com.moonic.platform;

import org.json.JSONObject;
import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.NetClient;

/**
 * coolpad
 * @author 
 */
public class P010 extends P
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

		String authCode = extendJson.optString("authCode");
		String appId = "5000000450";//extendJson.optString("appId");
		String appKey = "b486d0fd12b74904bcb76747fe560703";//extendJson.optString("appKey");
		String notifyUrl = extendJson.optString("notifyUrl");

		if (authCode.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token,extend=" + extend);
			return new ReturnValue(false, "platform" + platform + ",ȱ�ٲ���token,extend=" + extend);
		}

		String uidCheckUrl = "https://openapi.coolyun.com/oauth2/token";
		String url = uidCheckUrl + "?grant_type=authorization_code" + "&client_id=" + appId + "&redirect_uri=" + appKey + "&client_secret=" + appKey + "&code=" + authCode;
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		LogBAC.logout("login/" + platform, "��¼������������=" + url);
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				String result = "";
				try
				{
					result = new String(rv.binaryData, "UTF-8");
					//{"openid":"103400","expires_in":"2592000","refresh_token":"0.b6037940ec15224e3f711fae3b9fbb74","access_token":"0.e10adc3949ba59abbe56e057f20f883e.cf0243b77a2ba505a1ab2c8c82574736.1410342326163"}
					LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
					if (resultJson.has("openid"))
					{
						username = resultJson.optString("openid");
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + "û���û�openid");
						//return new ReturnValue(true, "18878963");
						return new ReturnValue(false, "�û���֤ʧ��msg=" + "û���û�openid");
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
