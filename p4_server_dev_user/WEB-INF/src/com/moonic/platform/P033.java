package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * �����
 * @author 
 */
public class P033 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false,"�������" + platform + ",��¼ȱ����չ����");
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
		String userId = extendJson.optString("userid");

		String partner_key = "p4gh1nsvxlety6by7w";

		String service = "user.validate";
		String partner_id = "1052";
		String game_id = "100590";
		String server_id = "1";
		String ticket = extendJson.optString("ticket");
//		LogBAC.logout("login/" + platform, "ǩ��ǰ=" + (partner_id + game_id + server_id + ticket + partner_key));
		String sign = MD5.encode(partner_id + game_id + server_id + ticket + partner_key).toUpperCase();
		String formart = "json";

		if (userId.equals("") || ticket.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���userId=" + userId + ",ticket=" + ticket + ",extend=" + extend);
			return new ReturnValue(false,platform + "����,ȱ�ٲ���");
		}
		//LogBAC.logout("login/" + platform, "Uin=" + Uin + ",username=" + username + ",SessionId=" + SessionId);

		String url = "http://union.play.ifeng.com/mservice2";
		//LogBAC.logout("login/" + platform, "�û���֤url=" + url);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		String sendStr = "service=" + service + "&partner_id=" + partner_id + "&game_id=" + game_id + "&server_id=" + server_id + "&ticket=" + ticket + "&sign=" + sign + "&formart" + formart;
//		LogBAC.logout("login/" + platform, "sendStr=" + sendStr);
		netClient.setSendBytes(sendStr.getBytes());
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					String result = new String(rv.binaryData, "UTF-8");
					//LogBAC.logout("login/" + platform, "result=" + result);
					JSONObject resultJson = new JSONObject(result);
					String code = resultJson.optString("code");//�����루1��Ч��
					String msg = resultJson.optString("msg");
					//LogBAC.logout("login/" + platform, "code=" + code);
					//LogBAC.logout("login/" + platform, "msg=" + msg);
					if (code.equals("1"))
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						return new ReturnValue(false,"�û���֤ʧ��code=" + code + ",msg=" + msg);
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
