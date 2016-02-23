package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * 7k7k
 * @author 
 */
public class P026 extends P
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
		//LogBAC.logout("login/" + platform, "�յ��û���֤����" + extend);
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
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token=" + token);
			return new ReturnValue(false, platform + "����,ȱ�ٲ���");
		}

		String url = "http://sdk.7k7k.com/oauth/check_user_result.php";

		String app_key = "6f9c8605bf10999a";

		//md5(md5(test.sdk.7k7k.com/oauth/check_user_result.php)accesstoken=fd01665ed80884cb7036da3968cd3756&appkey=f1436ab48781)
		String key = MD5.encode(MD5.encode("sdk.7k7k.com/oauth/check_user_result.php") + "accesstoken=" + token + "&appkey=" + app_key);

		String sendStr = "accesstoken=" + token + "&appkey=" + app_key + "&key=" + key;
		//LogBAC.logout("login/" + platform, "��¼������������=" + sendStr);

		url = url + "?" + sendStr;
		//LogBAC.logout("login/" + platform, "�û���֤url=" + url);

		NetClient netClient = new NetClient();
		netClient.setAddress(url);
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
					//{"code":1,"rows":{"userid":"532764337","username":"ygxf0h","token":"3e01b50918a0f4ead13924d2b584e2a3"}}

					JSONObject resultJson = new JSONObject(result);
					String code = resultJson.optString("code");
					String msg = resultJson.optString("msg");
					if (code.equals("1"))
					{
						JSONObject json_Temp = resultJson.optJSONObject("rows");
						username = json_Temp.optString("userid");
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						return new ReturnValue(false, "�û���֤ʧ��msg=" + msg);
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
