package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * ���
 * @author 
 */
public class P024 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false,"�ʺ�����" + platform + ",��¼ȱ����չ����");
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
			return new ReturnValue(false,platform+"����,��¼��չ�����쳣");
		}
		username = extendJson.optString("username");
		String Uin = extendJson.optString("Uin");
		String SessionId = extendJson.optString("SessionId");

		if (Uin.equals("") || SessionId.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���Uin="+Uin+",SessionId="+SessionId+",extend="+extend);				
			return new ReturnValue(false,platform+"����,ȱ�ٲ���");
		}
		//LogBAC.logout("login/" + platform, "Uin=" + Uin + ",username=" + username + ",SessionId=" + SessionId);

		String url = "http://pay.mdong.com.cn/phone/index.php/DeveloperServer/Index";
		int Act  = 3;//�̶�ֵ3
		String AppId = "590";
		String app_key = "224a27a7043c0310688eb443e34a7749";
		String Version = "1.07";//�̶�ֵ1.07
		String Sign = MD5.encode("Act="+Act+"&AppId="+AppId+"&SessionId="+SessionId+"&Uin="+Uin+"&Version="+Version+app_key);//MD5(Act=3&AppId=9&SessionId=d891b6f03f361128b10c69d440c92c34&Uin=1326&Version=1.07a123456789b123456789c123456789d1)���к�ɫ����Ϊapp_key�� �벻Ҫ�޸���ɫ�����˳��
		//LogBAC.logout("login/" + platform, "�û���֤url=" + url);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		String sendStr = "Act="+Act+"&AppId="+AppId+"&SessionId="+SessionId+"&Uin="+Uin+"&Version="+Version+"&Sign="+Sign;
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
					String code = resultJson.optString("Error_Code");//�����루0��Ч��1��Ч��
					String msg = resultJson.optString("Sign");//ǩ��ʾ���� MD5(Error_Code=1 a123456789b123456789c123456789d1)���к�ɫ����Ϊapp_key
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
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��" + ex.toString()+",str="+ new String(rv.binaryData, "UTF-8"));
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
