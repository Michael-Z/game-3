package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;

/**
 * pps
 * @author 
 */
public class P030 extends P {
	
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
			return new ReturnValue(false,platform + "����,��¼��չ�����쳣");
		}
		username = extendJson.optString("username");
		String uid = extendJson.optString("uid");
		String time = extendJson.optString("time");
		String fromSign = extendJson.optString("sign");

		if (username.equals("") || uid.equals("") || time.equals("") || fromSign.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���username=" + username + ",uid=" + uid + ",time=" + time + ",fromSign=" + fromSign + ",extend=" + extend);
			return new ReturnValue(false,platform + "����,ȱ�ٲ���");
		}

		String key = "74974bf301ff7e270d0e1e6860735f38";
		String sign = MD5.encode(uid + "&" + time + "&" + key);
//		LogBAC.logout("login_error", "platform=" + platform + ",fromSign=" + fromSign + ",sign=" + sign);
		if (fromSign.equals(sign))
		{
			return new ReturnValue(true, username);
		}
		else
		{
			return new ReturnValue(false,"�û���֤ʧ��fromSign=" + fromSign + ",sign=" + sign + "uid=" + uid + ",time=" + time + ",key=" + key);
		}
	}
}
