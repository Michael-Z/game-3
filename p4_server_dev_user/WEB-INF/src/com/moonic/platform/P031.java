package com.moonic.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.Text;

import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.moonic.util.MD5;
import com.moonic.util.NetClient;

/**
 * �Ա�
 * @author 
 */
public class P031 extends P
{

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����platform=" + platform + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "�������" + platform + ",��¼ȱ����չ����");
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",��չ�����쳣extend=" + extend);
			return new ReturnValue(false, platform + "����,��¼��չ�����쳣");
		}
		username = extendJson.optString("uid");
		String token = extendJson.optString("token");
		String supplier_id = extendJson.optString("supplier_id");
		String supplier_key = extendJson.optString("supplier_key");

		if (username.equals("") || token.equals("") || supplier_id.equals("") || supplier_key.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���username=" + username + ",token=" + token + ",supplier_id=" + supplier_id + ",supplier_key=" + supplier_key + ",extend=" + extend);
			return new ReturnValue(false, platform + "����,ȱ�ٲ���");
		}

		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("mutk", token);
		hm.put("supplier_id", supplier_id);
		hm.put("time", System.currentTimeMillis() / 1000);
		hm.put("client_ip", "0");

		String str = getUrlParam(hm); // �õ�����

		//ǩ��ֵ
		String sign = MD5.encode(str + supplier_key); // ���ݲ�����key�õ�ǩ��ֵ

		String url = "http://m.wan.liebao.cn/user/validate_mutk?";
		String checkUrl = url + str + "&sign=" + sign;
		NetClient netClient = new NetClient();
		netClient.setAddress(checkUrl);
		//LogBAC.logout("login/" + platform, "��¼������������=" + checkUrl);

		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				String result = "";
				try
				{
					result = new String(rv.binaryData, "UTF-8");
					LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					JSONObject resultJson = new JSONObject(result);
//					{
//						"code" : 1,
//						"data" : { "uid" : "1350762178" },
//						"msg" : "ok"
//						}
					if (resultJson.optString("msg").equals("ok"))
					{
						//username = resultJson.optString("id");
						LogBAC.logout("login/" + platform, "��������username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						LogBAC.logout("login/" + platform, "�û���֤ʧ��msg=" + "ǩ��sign ����");
						return new ReturnValue(false, "�û���֤ʧ��msg=" + "ǩ��sign ����");
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

	/**
	 * �õ���ַ����
	 * @param hm
	 * @param signKey
	 * @return
	 */
	public static String getUrlParam(HashMap<String, Object> hm)
	{
		List<String> list = new ArrayList<String>(hm.keySet());
		Collections.sort(list);
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = list.iterator();
		while (iter.hasNext())
		{
			String key = iter.next();
			if (sb.length() <= 0)
			{
				sb.append(key + "=" + hm.get(key));
			}
			else
			{
				sb.append("&" + key + "=" + hm.get(key));
			}
		}
		return sb.toString();
	}
}
