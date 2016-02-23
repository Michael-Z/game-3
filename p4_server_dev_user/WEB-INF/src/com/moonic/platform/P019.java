package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * ѾѾ��
 * @author 
 */
public class P019 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if(extend==null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform="+platform+",ȱ����չ����platform="+platform+",username="+username+",ip="+ip);
			return new ReturnValue(false,"�������"+platform+",��¼ȱ����չ����");
		}
		
		JSONObject extendJson=null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch(Exception ex)
		{
			LogBAC.logout("login_error", "platform="+platform+",��չ�����쳣extend="+extend);				
			return new ReturnValue(false,platform+"����,��¼��չ�����쳣");
		}
		username = extendJson.optString("username");
		String uid = extendJson.optString("uid");
		String token = extendJson.optString("token");

		if (uid.equals("") || token.equals(""))
		{
			LogBAC.logout("login_error", "platform="+platform+",ȱ�ٲ���uid="+uid+",token="+token+",extend=" + extend);				
			return new ReturnValue(false,platform+"����,ȱ�ٲ���");
		}
		//LogBAC.logout("login/" + platform, "uid=" + uid + ",username=" + username + ",token=" + token);

		String url = "http://passport.yayawan.com/oauth/userinfo";
		//LogBAC.logout("login/" + platform, "�û���֤url=" + url);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		String app_id = "4024916039";
		String yayawan_game_key = "63dc61f72a97e485625687335c1e8a57";
		String sendStr = "app_id=" + app_id + "&uid=" + uid + "&token=" + token;
		sendStr += "&sign=" + MD5.encode(token + "|" + yayawan_game_key);
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
					//String id = resultJson.optString("id");//���ֺ�
					username = resultJson.optString("username");//�û���
					//String reg_time = resultJson.optString("reg_time");//ע��ʱ�� ��since��1970-1-1 00:00:00��
					token = resultJson.optString("token");//�ӿڷ������ƣ�ԭ������
					String code = resultJson.optString("error_code");//������
					String msg = resultJson.optString("error_msg");//��������
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
					LogBAC.logout("login_error", "platform="+platform+",�û���֤ʧ��" + ex.toString()+",str="+new String(rv.binaryData, "UTF-8"));
					return new ReturnValue(false,"�û���֤ʧ��" + ex.toString());
				}
			}
			else
			{
				LogBAC.logout("login_error", "platform="+platform+",�û���֤ʧ��,���ݸ�ʽ�쳣");					
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
