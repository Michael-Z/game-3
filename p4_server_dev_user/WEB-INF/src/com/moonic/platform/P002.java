package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.NetClient;

/**
 * XY����
 * @author 
 */
public class P002 extends P
{
	@Override
	public ReturnValue checkLogin(String username, String extend, String ip)
			throws Exception {
		if (extend == null || extend.equals("")) {
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try {
			extendJson = new JSONObject(extend);
		} catch (Exception ex) {
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			return new ReturnValue(false, platform + "����,��¼��չ�����쳣");
		}
		int uid = extendJson.optInt("uid");
//		int appid = extendJson.optInt("appid");
		int appid = 100027347;
		String token = extendJson.optString("token");
		if (uid < 0 || token.equals("")) {
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���uid=" + uid + ",token=" + token + ",extend=" + extend);
			return new ReturnValue(false, platform + "����,ȱ�ٲ���");
		}
		String url = "http://passport.xyzs.com/checkLogin.php";
		NetClient netClient = new NetClient();
		String sendStr = "uid=" + uid + "&appid=" + appid + "&token=" + token;
		netClient.setAddress(url);
		netClient.setContentType("application/x-www-form-urlencoded");
		
		netClient.setSendBytes(sendStr.getBytes());
		ReturnValue rv = netClient.send();

		if (rv.success) {
			if (rv.dataType == ReturnValue.TYPE_BINARY) {
				String result = new String(rv.binaryData, "UTF-8");
				LogBAC.logout("login/" + platform, "result=" + result);
				JSONObject resultJson = new JSONObject(result);
				
				String ret = resultJson.optString("ret");
				String error = resultJson.optString("error");
				if (ret.equals("0")) {
					LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username + ",uid=" + uid);
					return new ReturnValue(true, String.valueOf(uid));
				} else {
					LogBAC.logout("login/" + platform, "�û���֤ʧ��error=" + error);
					return new ReturnValue(false, "�û���֤ʧ��error=" + error);
				}
			} else {
				LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��,���ݸ�ʽ�쳣");
				return new ReturnValue(false, "�û���֤ʧ��,���ݸ�ʽ�쳣");
			}
		} else {
			LogBAC.logout("login_error", "platform=" + platform + ",�û���֤ʧ��," + rv.info);
			return new ReturnValue(false, "�û���֤ʧ��," + rv.info);
		}
	}
}
