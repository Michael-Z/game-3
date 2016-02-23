package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.NetClient;

/**
 * ����
 * @author 
 */
public class P011Jifeng extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if(extend==null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform="+platform+",ȱ����չ����platform="+platform+",username="+username+",ip="+ip);
			return new ReturnValue(false,"�ʺ�����"+platform+",��¼ȱ����չ����");
		}
		//LogBAC.logout("login/" + platform, "�յ��û���֤����" + extend);
		JSONObject extendJson=null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch(Exception ex)
		{
			LogBAC.logout("login_error","platform="+platform+",��չ�����쳣extend="+extend);				
			return new ReturnValue(false,platform+"����,��¼��չ�����쳣");
		}
		
		username = extendJson.optString("username");
		String token = extendJson.optString("token");

		if (token.equals(""))
		{
			LogBAC.logout("login_error","platform="+platform+",ȱ�ٲ���token="+token+",extend=" + extend);				
			return new ReturnValue(false,platform+"����,ȱ�ٲ���");
		}
		
		String url = "http://api.gfan.com/uc1/common/verify_token";
		//LogBAC.logout("login/" + platform, "�û���֤url=" + url);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		String sendStr = "token=" + token;
		//LogBAC.logout("login/" + platform, "sendStr=" + sendStr);

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
					String code = resultJson.optString("resultCode");
					String msg = "";
					//LogBAC.logout("login/" + platform, "code=" + code);
					//LogBAC.logout("login/" + platform, "msg=" + msg);
					if (code.equals("1"))
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						if (code.equals("-1"))
						{
							msg = "����Ϊ��";
						}
						else if (code.equals("-2"))
						{
							msg = "��Чtoken";
						}
						//LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						return new ReturnValue(false,"�û���֤ʧ��msg=" + msg);
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error","platform="+platform+",�û���֤ʧ��" + ex.toString()+",str="+new String(rv.binaryData, "UTF-8"));
					return new ReturnValue(false,"�û���֤ʧ��" + ex.toString());
				}
			}
			else
			{
				LogBAC.logout("login_error","platform="+platform+"�û���֤ʧ��,���ݸ�ʽ�쳣");					
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
