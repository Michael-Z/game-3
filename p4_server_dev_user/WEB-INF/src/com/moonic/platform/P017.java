package com.moonic.platform;

import org.json.JSONObject;
import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.Base64Anzhi;
import com.moonic.util.NetClient;

/**
 * ����
 * @author 
 */
public class P017 extends P
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

		String uid = extendJson.optString("uid");
		String sid = extendJson.optString("sid");

		if (username == null || username.equals(""))
		{
			BACException.throwInstance("�û�������Ϊ��");
		}

		if (uid.equals("") || sid.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���username=" + username + ",uid=" + uid + ",sid=" + sid + ",extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}

		String appKey = "14119186579x9gn5f8xF65QvE9pqve";
		String appSecret = "HW6rxJ25Irx6I189hw5Pl1Pt";
		String time = String.valueOf(System.currentTimeMillis() / 1000);

		String url = "http://user.anzhi.com/web/api/sdk/third/1/queryislogin";
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");

		//��ǰ��¼�û���Ϣת����Ӧjson��ʽ
		//String gameUser = "{\"id\":3,\"loginname\":\"" + username + "\"}";
		//String msgTemp = "{'head':{'appkey':'" + appKey + "','version':'1.0','time':'" + time + "'},'body':{'msg':{'gameUser':'" + gameUser + "','time':'" + time + "'},'ext':{}}}";
		//LogBAC.logout("login/" + platform, "��¼������������=" + msgTemp);
		//String sendStr = Des3Util.encrypt(msgTemp, appSecret);
		String sign = Base64Anzhi.encodeToString(appKey + sid + appSecret);
		String sendStr = "time=" + time + "&appkey=" + appKey + "&sid=" + sid + "&sign=" + sign;
		LogBAC.logout("login/" + platform, "��¼������������=" + sendStr);

		//netClient.ignoreSSL();//����
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
//					{
//						��sc��: ��1��,
//						��st��: ���ɹ�(sid ��Ч) �� ��
//						��time��:��20130228101059123��
//						��msg�� : {
//						��uid��:��123456789��
//						}
//						}
					LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
					if (resultJson.optString("sc").equals("1"))
					{
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + resultJson.optString("st"));
						return new ReturnValue(false, "�û���֤ʧ��msg=" + resultJson.optString("st"));
						//return new ReturnValue(true, username);
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
