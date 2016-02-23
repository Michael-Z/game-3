package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.NetClient;

/**
 * 3G
 * @author 
 */
public class P021 extends P
{

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "�ʺ�����" + platform + ",��¼ȱ����չ����");
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
			//System.out.println("3G��չ����תjson�쳣extend="+extend);
			//System.out.println(ex.toString());
			return new ReturnValue(false, platform + "����,��¼��չ�����쳣");
		}

		username = extendJson.optString("username");
		String uid = extendJson.optString("uid");
		String sessionid = extendJson.optString("sessionid");
		String cpid = extendJson.optString("cpid");
		String gameid = extendJson.optString("gameid");

		/*cpid: 2664
		gameid: 2676
		md5key: 2324koudaihuanshou*/

		if (uid.equals("") || sessionid.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + uid + ",sessionid=" + sessionid + ",extend=" + extend);
			//return new ReturnValue(false,"ȱ�ٲ���");
			return new ReturnValue(false, platform + "����,ȱ�ٲ���");
		}
		if (!cpid.equals("2664") || !gameid.equals("2676"))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",������˲�ͨ��cpid=" + cpid + ",gameid=" + gameid + ",extend=" + extendJson.toString());
			return new ReturnValue(false, platform + "����,������˲�ͨ��");
		}
		Thread.sleep(2000); //�ӳ�2��
		String url = "http://2324.cn/User/userverify.php";
		//LogBAC.logout("charge/"+platform, "�û���֤url="+url);
		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		netClient.addParameter("cpid", cpid);
		netClient.addParameter("gameid", gameid);
		netClient.addParameter("sid", sessionid);
		netClient.addParameter("token", uid);

		ReturnValue rv = netClient.send();
		//LogBAC.logout("charge/" + platform, "�û���֤���ؽ��=" + rv.success + " " + rv.info);
		/*{
			"code":"1",
			"msg":"��֤�ɹ�",
			"sid":"911ee641793438c49e788e1c22587833",
			"token":"1856dc9fda05052fb12587981ed5944d"
			}*/

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					String result = new String(rv.binaryData, "UTF-8");
					JSONObject resultJson = new JSONObject(result);
					int code = resultJson.optInt("code");
					if (code == 1)
					{
						//LogBAC.logout("login/"+platform, "��¼�ɹ�username="+username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/"+platform, "�û���֤ʧ��code="+code+",resultJson="+resultJson);
						return new ReturnValue(false, "�û���֤ʧ��code=" + code);
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
