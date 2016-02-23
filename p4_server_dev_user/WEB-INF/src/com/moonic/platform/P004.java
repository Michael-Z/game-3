package com.moonic.platform;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.NetClient;

/**
 * 2016-01-20 14:00:00 ͬ����
 * @author 
 */
public class P004 extends P {
	
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		if (extend == null || extend.equals("")) {
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend);
			BACException.throwInstance("�������" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try {
			extendJson = new JSONObject(extend);
		} catch (Exception ex) {
			LogBAC.logout("login_error", "platform=" + platform + ",��չ����תjson�쳣extend=" + extend);
			BACException.throwInstance(platform + "����,��¼��չ�����쳣");
		}
		
		String session = extendJson.optString("session");
		if (session == null || session.trim().length() <= 0) {
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���" + "session=" + session + ",extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ�ٲ���");
		}
		//http://tgi.tongbu.com/api/LoginCheck.ashx?session=afa1c75257f400079a576588d0bb41bc&appid=100000
		int appid = 160112;
		String url = "http://tgi.tongbu.com/api/LoginCheck.ashx";
		String sendStr = "session=" + session + "&appid=" + appid;
		LogBAC.logout("login/" + platform, "���͵�������֤����sendStr=" + sendStr);

		url = url + "?" + sendStr;
		LogBAC.logout("login/" + platform, "�û���֤url=" + url);

		NetClient netClient = new NetClient();
		netClient.setAddress(url);
		ReturnValue rv = netClient.send();
		if (rv.success) {
			if (rv.dataType == ReturnValue.TYPE_BINARY) {
				try {
					String result = new String(rv.binaryData, "UTF-8");
					LogBAC.logout("login/" + platform, "�����û���֤����" + result);
					int resultNum = Integer.valueOf(result);

					if (resultNum > 0) {
						LogBAC.logout("login/" + platform, "result=" + result);
						return new ReturnValue(true, result);
					} else {
						String errorMsg = resultNum == 0 ? "session�Ѿ�����" : "��ʽ�д�";
						//�û���֤ʧ��
						LogBAC.logout("login/" + platform, "�û���֤ʧ��," + errorMsg);
						return new ReturnValue(false, "�û���֤ʧ��," + errorMsg);
					}
				} catch (Exception e) {
					e.printStackTrace();
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤�쳣," + e.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
					return new ReturnValue(false, "�û���֤�쳣," + e.toString());
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
