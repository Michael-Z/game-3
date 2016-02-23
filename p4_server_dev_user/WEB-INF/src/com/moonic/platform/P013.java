package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.HmacSHA1Encryption;
import com.moonic.util.NetClient;

/**
 * С��
 * @author 
 */
public class P013 extends P
{

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		String appId = "2882303761517297618"; //appID
		String appKEY = "VrRECrXpmikY74KLUPQ2Yg==";//AppSecret			

		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend);
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
			LogBAC.logout("login_error", "platform=" + platform + ",��¼��չ�����쳣extend=" + extend);
			//System.out.println("С��005��չ����תjson�쳣extend="+extend);
			///System.out.println(ex.toString());
			BACException.throwInstance(platform + "����,��¼��չ�����쳣");
		}
		String uid = extendJson.optString("uid");
		String sessionid = extendJson.optString("sessionid");
		username = extendJson.optString("username");

		if (uid.equals("") || sessionid.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + uid + ",sessionid=" + sessionid + ",extend=" + extend);
			//BACException.throwInstance("ȱ�ٲ���uid="+uid+",sessionid="+sessionid);
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}

		String str = "appId=" + appId + "&session=" + sessionid + "&uid=" + uid;

		String signature = HmacSHA1Encryption.hmacSHA1Encrypt(str, appKEY);
		//LogBAC.logout("charge/"+platform, "�û���֤�ύ����=appId="+appId+"&session="+sessionid+"&uid="+uid+"&signature="+signature);
		NetClient netClient = new NetClient();
		netClient.setAddress("http://mis.migc.xiaomi.com/api/biz/service/verifySession.do");
		netClient.addParameter("appId", appId);
		netClient.addParameter("session", sessionid);
		netClient.addParameter("uid", uid);
		netClient.addParameter("signature", signature);
		ReturnValue rv = netClient.send();
		//LogBAC.logout("charge/"+platform, "�û���֤���ؽ��="+rv.success+" "+rv.info);
		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					JSONObject userjson = new JSONObject(new String(rv.binaryData, "UTF-8"));
					//LogBAC.logout("charge/"+platform, "�û���֤�ɹ����ؽ��="+userjson);
					//LogBAC.logout("charge/"+platform, "uc�û�"+sid+"��֤���ؽ��\r\n"+userjson);
					int errcode = userjson.optInt("errcode");
					String errMsg = userjson.optString("errMsg");

					if (errcode == 200)
					{
						//LogBAC.logout("login/"+platform, "��¼�ɹ�username="+username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/" + platform, "�û���֤ʧ�ܣ�errcode=" + errcode + ",errMsg=" + errMsg);
						return new ReturnValue(false, "�û���֤ʧ�ܣ�errcode=" + errcode + ",errMsg=" + errMsg);
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
