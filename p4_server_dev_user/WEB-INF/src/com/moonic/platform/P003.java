package com.moonic.platform;

import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.BACException;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * PP����
 * 2016-01-20 13:30:00 PP����
 * @author 
 */
public class P003 extends P {
	
	@Override
	public JSONObject login(String channel, String extend, String username, String password, String ip, String imei, String mac, int loginport, SqlString userSqlStr) throws Exception
	{
		JSONObject returnobj = super.login(channel, extend, username, password, ip, imei, mac, loginport, userSqlStr);
		returnobj.put("channeldata", returnobj.optString("username"));
		return returnobj;
	}
	
	@Override
	public ReturnValue checkLogin(String username, String extend, String ip)
			throws Exception {
		if (extend == null || extend.equals("")) {
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend);
			BACException.throwInstance("�ʺ�����" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try {
			extendJson = new JSONObject(extend);
		} catch (Exception ex) {
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			BACException.throwInstance(platform + "����,��¼��չ�����쳣");
		}
		/*
		 {
			"id":1330395827,
			"service":"account.verifySession",
			"data":{"sid":"80a5fe53d3540300005a17e308a4b1fb"},
			"game":{"gameId":12345},
			"encrypt":"md5",
			"sign":"6e9c3c1e7d99293dfc0c81442f9a9984"
		 }
			sign ��ǩ������MD5(sid=... + AppKey)��ȥ�� + �滻 ... Ϊʵ��sid ֵ��
		 */
		// TODO ȱ������������sid�ɿͻ��˷��͹�����appKey��gameId��Ҫ����
		String sid = extendJson.optString("sid");
		if (sid.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���sid,extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ��sid����");
		}
		int gameId = 7306;
		String appKey = "8379b85e6a00eef657ce76fccaa43e81";

		JSONObject json = new JSONObject();
		json.setForceLowerCase(false);
		json.put("id", Tools.getSyncMillisecond() / 1000);	//PP���� ʱ������ܳ���10λ
		json.put("service", "account.verifySession");

		JSONObject dataJson = new JSONObject();
		dataJson.put("sid", sid);
		json.put("data", dataJson);

		JSONObject gameJson = new JSONObject();
		gameJson.setForceLowerCase(false);
		gameJson.put("gameId", gameId);
		json.put("game", gameJson);

		String signSource = "sid=" + sid + appKey;//��װǩ��ԭ��
		LogBAC.logout("login/" + platform, "[ǩ��ԭ��]" + signSource);

		String sign = MD5.encode(signSource);
		LogBAC.logout("login/" + platform, "[ǩ�����]" + sign);
		
		json.put("encrypt", "md5");
		json.put("sign", sign);
		LogBAC.logout("login/" + platform, "uc�û�" + sid + "��֤��������\r\n" + json.toString());
		LogBAC.logout("login/" + platform, "��¼���ͣ�" + json.toString());
		NetClient netClient = new NetClient();
		String url = "http://passport_i.25pp.com:8080/account?tunnel-command=2852126760";
		netClient.setAddress(url); //��ʽ����  
		netClient.setSendBytes(json.toString().getBytes("UTF-8"));
		ReturnValue rv = netClient.send();
		
		if (rv.success) {
			if (rv.dataType == ReturnValue.TYPE_BINARY) {
				try {
					JSONObject userjson = new JSONObject(new String(rv.binaryData, "UTF-8"));
					LogBAC.logout("login/" + platform, "uc�û�" + sid + "��֤���ؽ��\r\n" + userjson);
					JSONObject stateJson = userjson.optJSONObject("state");
					JSONObject dataJson2 = userjson.optJSONObject("data");
					int state = stateJson.optInt("code");
					String msg = stateJson.optString("msg");
					LogBAC.logout("login/" + platform, "��¼�ɹ����أ�" + userjson.toString());
					if (state == 1) {
						String accountId = dataJson2.optString("accountId");
						String creator = dataJson2.optString("creator");
						//��һ��ע�⣬�µ���֤�ӿ�Э���ѵ�����ʹ��creator+accountId��Ψһ��ʶ��
						username = creator + accountId;
						LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					} else {
						LogBAC.logout("login/" + platform, "�û���֤ʧ�ܣ�" + msg);
						return new ReturnValue(false, "�û���֤ʧ�ܣ�" + msg);
					}
				} catch (Exception e) {
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤�쳣" + e.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
					return new ReturnValue(false, "�û���֤ʧ��" + e.toString());
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
	
	/*
	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend);
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
		String sid = extendJson.optString("sid");

		if (sid.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���sid,extend=" + extend);
			BACException.throwInstance(platform + "����,ȱ��sid����");
		}

		int cpId = 25494;
		int gameId = 552716;
		int serverId = 0;
		int channelId = 2;
		String apikey = "47e35a04a8dadee9389c49527af76d4c";

		JSONObject json = new JSONObject();
		json.setForceLowerCase(false);
		json.put("id", Tools.getSyncMillisecond());
		json.put("service", "account.verifySession");

		JSONObject dataJson = new JSONObject();
		dataJson.put("sid", sid);
		json.put("data", dataJson);

		JSONObject gameJson = new JSONObject();
		gameJson.setForceLowerCase(false);
		gameJson.put("gameId", gameId);
		json.put("game", gameJson);

		String signSource = "sid=" + sid + apikey;//��װǩ��ԭ��
		LogBAC.logout("login/" + platform, "[ǩ��ԭ��]" + signSource);

		String sign = MD5.encode(signSource);
		LogBAC.logout("login/" + platform, "[ǩ�����]" + sign);

		json.put("sign", sign);
		LogBAC.logout("login/" + platform, "uc�û�" + sid + "��֤��������\r\n" + json.toString());
		LogBAC.logout("login/" + platform, "��¼���ͣ�" + json.toString());
		NetClient netClient = new NetClient();
		netClient.setAddress("http://sdk.g.uc.cn/cp/account.verifySession"); //��ʽ����  
		netClient.setSendBytes(json.toString().getBytes("UTF-8"));
		ReturnValue rv = netClient.send();
		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				try
				{
					JSONObject userjson = new JSONObject(new String(rv.binaryData, "UTF-8"));
					LogBAC.logout("login/" + platform, "uc�û�" + sid + "��֤���ؽ��\r\n" + userjson);
					JSONObject stateJson = userjson.optJSONObject("state");
					JSONObject dataJson2 = userjson.optJSONObject("data");
					int state = stateJson.optInt("code");
					String msg = stateJson.optString("msg");
					LogBAC.logout("login/" + platform, "��¼�ɹ����أ�" + userjson.toString());
					if (state == 1)
					{
						username = dataJson2.optString("accountId");
						LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ�ܣ�" + msg);
						return new ReturnValue(false, "�û���֤ʧ�ܣ�" + msg);
					}
				}
				catch (Exception ex)
				{
					LogBAC.logout("login_error", "platform=" + platform + ",�û���֤�쳣" + ex.toString() + ",str=" + new String(rv.binaryData, "UTF-8"));
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
*/
}