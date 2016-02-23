package com.moonic.platform;

import org.json.JSONException;
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
public class P012 extends P
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
			BACException.throwInstance("����" + platform + "��¼��չ�����쳣");
		}
		String app_id = extendJson.optString("app_id");
		String mid = extendJson.optString("mid");
		String token = extendJson.optString("token");

		if (app_id.equals("") || mid.equals("") || token.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����app_id=" + app_id + ",mid=" + mid + ",token=" + token + ",extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}
		String appKey = "MY69b7JY";
		NetClient netClient = new NetClient();
		netClient.setAddress("http://connect.d.cn/open/member/info/");
		netClient.addParameter("app_id", app_id);
		netClient.addParameter("mid", mid);
		netClient.addParameter("token", token);
		netClient.addParameter("sig", MD5.encode(token + "|" + appKey));
		ReturnValue rv = netClient.send();
		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				/*�ɹ�
				 {
					"memberId":32608510,
					"username":"ym1988ym",
					"nickname":"����_С��",
					"gender":"��",
					"level":11, "avatar_url":"http://d.cn/images/item/35/002.gif",
					"created_date":1346140985873,
					"token":"F9A0F6A0E0D4564F56C483165A607735FA4F324",
					"error_code":0
					}
					ʧ��
					{
					"error_code":211,
					"error_msg":"app_key����"
					}*/
				try
				{
					JSONObject dljson = new JSONObject(new String(rv.binaryData, "UTF-8"));
					//System.out.println("�����û���֤����"+dljson.toString());
					String dlUsername = dljson.getString("username");
					String error_code = dljson.getString("error_code");
					String error_msg = dljson.getString("error_msg");
					if (error_code.equals("0"))
					{
						//LogBAC.logout("login/"+platform, "��¼�ɹ�username="+dlUsername);
						return new ReturnValue(true, dlUsername);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��,error_code=" + error_code + ",error_msg=" + error_msg + ",dlUsername=" + dlUsername);
						//�û���֤ʧ��
						return new ReturnValue(false, "�û���֤ʧ��,error_code=" + error_code + ",error_msg=" + error_msg);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
					LogBAC.logout("login_error", "platform=" + platform + ",e=" + e.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
					//LogBAC.logout("login/"+platform, "�û���֤ʧ��,"+e.toString());
					return new ReturnValue(false, "�û���֤ʧ��," + e.toString());
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
