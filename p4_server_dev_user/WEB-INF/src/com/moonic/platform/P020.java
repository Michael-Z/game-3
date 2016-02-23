package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * pada
 * @author 
 */
public class P020 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false,"�������" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);				
			return new ReturnValue(false,platform+"����,��¼��չ�����쳣");
		}
		username = extendJson.optString("username");
		String roleId = extendJson.optString("roleId");
		String roleToken = extendJson.optString("roleToken");

		if (roleId.equals("") || roleToken.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���roleId="+roleId+",roleToken="+roleToken+",extend=" + extend);							
			return new ReturnValue(false,platform+"����,ȱ�ٲ���");
		}
		//LogBAC.logout("login/" + platform, "roleId=" + roleId + ",username=" + username + ",roleToken=" + roleToken);

		String url = "http://uac.svc.pada.cc/authRoleToken";
		String appId = "101022";
		String appKey = "af388244b54f74a11c93ba4c171fef1d";
		//MD5_32(appId=123&roleId=123&roleToken=123&AppToken)
		String sendStr = "appId=" + appId + "&roleId=" + roleId + "&roleToken=" + roleToken;
		//LogBAC.logout("login/" + platform, "sendStr=" + sendStr);
		String urlPath = url + "?" + sendStr + "&sign=" + MD5.encode(sendStr + "&" + appKey);
		//LogBAC.logout("login/" + platform, "�û���֤url=" + urlPath);
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
					//LogBAC.logout("login/" + platform, "result=" + result);
					JSONObject resultJson = new JSONObject(result);
					String code = resultJson.optString("rescode");//�����룬�����ͣ�0=�ɹ���1=roleToken����2=appId����3=roleId����4=sign����5=��������
					String msg = resultJson.optString("resmsg");//������Ϣ
					//LogBAC.logout("login/" + platform, "code=" + code);
					//LogBAC.logout("login/" + platform, "msg=" + msg);
					if (code.equals("0"))
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						return new ReturnValue(false,"�û���֤ʧ��msg=" + msg);
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��" + ex.toString()+",str="+new String(rv.binaryData, "UTF-8"));
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
			LogBAC.logout("login_error", "platform="+platform+",�û���֤ʧ��,"+rv.info);
			return new ReturnValue(false,"�û���֤ʧ��," + rv.info);
		}
	}
}
