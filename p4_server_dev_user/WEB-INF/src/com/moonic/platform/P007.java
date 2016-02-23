package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.nearme.oauth.model.AccessToken;
import com.nearme.oauth.open.AccountAgent;

/**
 * oppo
 * @author 
 */
public class P007 extends P
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
			//System.out.println("3G007��չ����תjson�쳣extend="+extend);
			//System.out.println(ex.toString());
			return new ReturnValue(false, platform + "����,��¼��չ�����쳣");
		}

		username = extendJson.optString("username");
		String uid = extendJson.optString("uid");
		String oauth_token = extendJson.optString("oauth_token");
		String oauth_token_secret = extendJson.optString("oauth_token_secret");

		if (uid.equals("") || oauth_token.equals("") || oauth_token_secret.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + uid + ",oauth_token=" + oauth_token + ",oauth_token_secret=" + oauth_token_secret + ",extend=" + extend);
			//return new ReturnValue(false,"ȱ�ٲ���");
			return new ReturnValue(false, platform + "����,ȱ�ٲ���");
		}

		String gcUserInfo = AccountAgent.getInstance().getGCUserInfo(new AccessToken(oauth_token, oauth_token_secret));

		LogBAC.logout("charge/" + platform, "�û���֤���ؽ��=" + gcUserInfo);

		//�������ص����ݸ�ʽΪJSON�� 
		/*{"BriefUser":
		 * {
		 * "id":"11686668",
		 * "constellation":0,
		 * "sex":true,
		 * "profilePictureUrl":"http://gcfs.nearme.com.cn/avatar/common/male.png",
		 * "name":"ZTEU880E11686668",
		 * "userName":"NM11686668",
		 * "emailStatus":"false",
		 * "mobileStatus":"false",
		 * "status":"Visitor",
		 * "mobile":"",
		 * "email":"",
		 * "gameBalance":"0"}}*/

		try
		{
			JSONObject resultJson = new JSONObject(gcUserInfo);
			JSONObject json = resultJson.optJSONObject("BriefUser");
			int id = json.optInt("id");
			if (id > 0)
			{
				LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
				return new ReturnValue(true, username);
			}
			else
			{
				LogBAC.logout("login/" + platform, "�û���֤ʧ��gcUserInfo=" + gcUserInfo);
				return new ReturnValue(false, "�û���֤ʧ��id=" + id);
			}
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��" + ex.toString() + ",str=" + gcUserInfo);
			return new ReturnValue(false, "�û���֤ʧ��" + ex.toString());
		}
	}
}
