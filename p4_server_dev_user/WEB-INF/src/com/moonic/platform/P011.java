package com.moonic.platform;

import java.net.URLEncoder;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.NetClient;

/**
 * �㶹��
 * @author 
 */
public class P011 extends P
{

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			BACException.throwInstance("�������" + platform + ",��¼ȱ����չ����");
		}
		//LogBAC.logout("charge/"+platform, "�յ��û���֤����"+extend);
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",extend=" + extend);
			//System.out.println("�㶹��006��չ����תjson�쳣extend="+extend);
			//System.out.println(ex.toString());
			BACException.throwInstance(platform + "����,��¼��չ�����쳣");
		}
		String uid = extendJson.optString("uid");
		String token = extendJson.optString("token");
		username = extendJson.optString("username");

		if (uid.equals("") || token.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + uid + ",token=" + token + ",extend=" + extend);
			//BACException.throwInstance("ȱ�ٲ���");
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}

		String uidCheckUrl = "https://pay.wandoujia.com/api/uid/check";
		String url = uidCheckUrl + "?uid=" + uid + "&token=" + URLEncoder.encode(token, "UTF-8");
		//LogBAC.logout("charge/"+platform, "�û���֤url="+url);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		ReturnValue rv = netClient.send();
		//LogBAC.logout("charge/" + platform, "�û���֤���ؽ��=" + rv.success + " " + rv.info);
		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					String result = new String(rv.binaryData, "UTF-8");
					if (result.equals("true"))
					{
						//LogBAC.logout("login/"+platform, "��¼�ɹ�username="+username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ�� username=" + username + ",binaryData = " + new String(rv.binaryData, "UTF-8"));
						return new ReturnValue(false, "�û���֤ʧ��");
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
