package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;

/**
 * �Ƶ�
 * @author 
 */
public class P028yundian extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
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

		if (username.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + username + ",extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}
		
		return new ReturnValue(true, username);

		//String platform_code = "2f642b2";//���̱��  string ����Ϊ��
		/*NetClient netClient = new NetClient();
		netClient.setAddress("http://api.cpo2o.com/v1/API/PayLogin/otherVerifyUser");
		netClient.addParameter("uid", username);
		//netClient.addParameter("platform_code", platform_code);
		ReturnValue rv = netClient.send();
		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					String result = new String(rv.binaryData, "UTF-8");
					//LogBAC.logout("login_error", "platform=" + platform + ",result=" + result);
					JSONObject resultJson = new JSONObject(result);
					String code = resultJson.optString("code");//״̬�� 100000��ʾ�ɹ���������ʾʧ��
					String msg = resultJson.optString("msg");//������Ϣ
					//LogBAC.logout("login/" + platform, "code=" + code);
					//LogBAC.logout("login/" + platform, "msg=" + msg);
					if (code.equals("100000")) 
					{
						//LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + msg);
						BACException.throwInstance("�û���֤ʧ��msg=" + msg + ",uid=" + username);
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��" + ex.toString() + ",str=" + new String(rv.binaryData, "UTF-8") + ",uid=" + username);
					BACException.throwInstance("�û���֤ʧ��" + ex.toString());
				}
			}
			else
			{
				LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��,���ݸ�ʽ�쳣" + ",uid=" + username);
				BACException.throwInstance("�û���֤ʧ��,���ݸ�ʽ�쳣");
			}
		}
		else
		{
			LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��," + rv.info + ",uid=" + username);
			BACException.throwInstance("�û���֤ʧ��," + rv.info);
		}*/
	}
}
