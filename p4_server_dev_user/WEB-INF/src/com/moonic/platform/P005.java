package com.moonic.platform;

import java.io.ByteArrayInputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import server.config.LogBAC;
import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.NetClient;

/**
 * lenovo
 * @author 
 */
public class P005 extends P
{

	public JSONObject login(String channel, String extend, String username, String password, String ip, String imei, String mac, int loginport, SqlString userSqlStr) throws Exception
	{
		JSONObject returnobj = super.login(channel, extend, username, password, ip, imei, mac, loginport, userSqlStr);
		returnobj.put("channeldata", returnobj.optString("username"));
		return returnobj;
	}

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception
	{
		if (extend == null || extend.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "platform" + platform + ",ȱ����չ����extend=" + extend);
		}
		JSONObject extendJson = null;
		try
		{
			extendJson = new JSONObject(extend);
		}
		catch (Exception ex)
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ����չ����extend=" + extend + ",username=" + username + ",ip=" + ip);
			return new ReturnValue(false, "platform" + platform + ",ȱ����չ����extend=" + extend);
		}

		String ticket = extendJson.optString("ticket");
		String appId = extendJson.optString("appId");

		if (ticket.equals("") || appId.equals(""))
		{
			LogBAC.logout("login_error", "platform=" + platform + ",ȱ�ٲ���token,extend=" + extend);
			return new ReturnValue(false, "platform" + platform + ",ȱ�ٲ���token,extend=" + extend);
		}

		String uidCheckUrl = "http://passport.lenovo.com/interserver/authen/1.2/getaccountid";
		String url = uidCheckUrl + "?lpsust=" + ticket + "&realm=" + appId;
		NetClient netClient = new NetClient();

		netClient.setAddress(url);
		LogBAC.logout("login/" + platform, "��¼������������=" + url);
		ReturnValue rv = netClient.send();

		if (rv.success)
		{
			if (rv.dataType == ReturnValue.TYPE_BINARY)
			{
				String result = "";
				try
				{
					result = new String(rv.binaryData, "UTF-8");
					//<?xml version="1.0" encoding="UTF-8"?><IdentityInfo><AccountID>10012674129</AccountID><Username>15000689240</Username><DeviceID>359543054034539</DeviceID><verified>1</verified></IdentityInfo>
					//LogBAC.logout("login/" + platform, "��¼�յ���������=" + result);
					ByteArrayInputStream bais = new ByteArrayInputStream(rv.binaryData);
					SAXReader saxReader = new SAXReader();
					Document document = saxReader.read(bais);
					Element root = document.getRootElement();
					//Element verified = root.element("verified");
					Element AccountID = root.element("AccountID");
					//LogBAC.logout("login/"+platform, "AccountID="+AccountID);

					if (AccountID != null)
					{
						username = AccountID.getText();
						LogBAC.logout("login/" + platform, "��¼�ɹ�username=" + username);
						return new ReturnValue(true, username);
					}
					else
					{
						//LogBAC.logout("login/"+platform, "�û���֤ʧ��xml="+resultXml);
						return new ReturnValue(false, "�û���֤ʧ��");
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
}
