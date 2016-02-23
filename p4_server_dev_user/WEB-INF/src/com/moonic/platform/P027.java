package com.moonic.platform;

import org.json.JSONObject;
import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * ����Ӱ��
 * @author
 */
public class P027 extends P
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

		if (ticket.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token,extend=" + extend);
			return new ReturnValue(false, "platform" + platform + ",ȱ�ٲ���token,extend=" + extend);
		}

		int cp_id = 10038;
		String cp_key = "1113wetg%$";
		int game_id = 47;
		int server_id = 25;
		String sign = MD5.encode(cp_id + game_id + server_id + ticket + cp_key).toUpperCase();

		String url = "http://sdk.gcenter.baofeng.com/user?service=user.validate";
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");
		String sendStr = "cp_id=" + cp_id + "&game_id=" + game_id + "&server_id=" + server_id + "&ticket=" + ticket + "&sign=" + sign + "&formart=json";
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
					//{"timestamp":123456790,"code":1,"msg":"SUCCESS","data":{"user_id":"123456","nick_name":"","adult":1}}
					LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
					if (resultJson.optString("msg").equals("SUCCESS"))
					{
						username = resultJson.optJSONObject("data").optString("user_id");
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
