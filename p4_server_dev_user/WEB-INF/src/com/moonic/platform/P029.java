package com.moonic.platform;

import org.json.JSONObject;
import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * ����
 * @author 
 */
public class P029 extends P
{

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			BACException.throwInstance("�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			BACException.throwInstance(platform + "����,��¼��չ�����쳣");
		}
		username = extendJson.optString("username");
		String token = extendJson.optString("token");
		String appId = extendJson.optString("appid");
		String appKey = extendJson.optString("appkey");
		String sign = MD5.encode(MD5.encode(appKey + "_" + token));

		if (username.equals("") || token.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + username + ",extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}
		String uidCheckUrl = "http://app.5gwan.com:9000/user/info.php";
		String url = uidCheckUrl + "?sign=" + sign + "&token=" + token + "&app_id=" + appId;
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		//LogBAC.logout("login/" + platform, "��¼������������=" + url);
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				String result = "";
				try
				{
					result = new String(rv.binaryData, "UTF-8");
					JSONObject resultJson = new JSONObject(result);

					if (resultJson.optInt("state") == 1)
					{
						//username = resultJson.optString("openid");
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + "signǩ������");
						return new ReturnValue(false, "�û���֤ʧ��msg=" + "signǩ������");
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
