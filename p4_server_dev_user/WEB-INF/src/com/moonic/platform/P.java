package com.moonic.platform;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ChannelBAC;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;

/**
 * ������
 * @author John
 */
public abstract class P {
	protected String platform;
	
	/**
	 * ע��
	 */
	public void register(DBHelper dbHelper, String username, String password, String rePassword, String ip, String channel, JSONArray logdata) throws Exception {
		BACException.throwInstance("�ʺ�����"+platform+"δ�ṩע�Ṧ��");
	}
	
	/**
	 * ��¼
	 */
	public JSONObject login(String channel, String extend, String username, String password, String ip, String imei, String mac, int loginport, SqlString userSqlStr) throws Exception {
		ReturnValue rv = checkLogin(username, extend, ip);
		if(!rv.success){
			BACException.throwInstance(rv.info);
		}
		JSONObject returnobj = new JSONObject();
		returnobj.put("username", rv.info);
		return returnobj;
	}
	
	/**
	 * �Ƿ��ѵ�¼
	 */
	public abstract ReturnValue checkLogin(String username, String extend, String ip) throws Exception ;
	
	//-----------------��̬��------------------
	
	public static Hashtable<String, P> platformTab = new Hashtable<String, P>();
	
	/**
	 * ��ȡ��������
	 */
	public static P getInstanceByChannel(String channel) throws Exception {
		DBPaRs channelRs = ChannelBAC.getInstance().getChannelListRs(channel);
		if(!channelRs.exist()){
			BACException.throwInstance("�������������� channel="+channel);
		}
		return getInstance(channelRs.getString("platform"));
	}
	
	/**
	 * ��ȡ��������
	 */
	public static synchronized P getInstance(String platform) throws Exception 
	{
		P p = platformTab.get(platform);
		if(p == null){
			try {
				p = (P)Class.forName("com.moonic.platform.P"+platform).newInstance();
				platformTab.put(platform, p);		
			} catch (ClassNotFoundException e) {
				BACException.throwInstance("�ʺ����������� platform="+platform);
			}
			p.platform = platform;
		}
		return p;
	}
}
