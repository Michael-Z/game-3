package com.moonic.bac;

import server.common.Tools;

import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

public class ActivateCodeBAC {
	public static String tab_activate_code = "tab_activate_code";
	
	private static String[] exemptActivateChannel = {"003"};
	
	/**
	 * ����û��Ƿ��Ѽ���
	 * @param channel ����
	 * @param username �û���
	 */
	public boolean checkActivate(DBHelper dbHelper, String channel, String username) throws Exception {
		if (!ConfigBAC.getBoolean("needactivate")) {
			return true;
		}
		if(Tools.contain(exemptActivateChannel, channel)){
			return true;
		}
		DBPaRs channelRs = DBPool.getInst().pQueryA(ChannelBAC.tab_channel, "code="+channel);
		//tab_activate_code�е�channelʵ������Ϊplatform
		return dbHelper.queryExist(tab_activate_code, "channel='"+channelRs.getString("platform")+"' and activate_user='"+username+"' and activated=1");
	}
	
	//------------------��̬��--------------------
	
	private static ActivateCodeBAC instance = new ActivateCodeBAC();

	public static ActivateCodeBAC getInstance() {
		return instance;
	}
}
