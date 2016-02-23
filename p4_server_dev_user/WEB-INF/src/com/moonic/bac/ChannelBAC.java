package com.moonic.bac;

import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ����BAC
 * @author John
 */
public class ChannelBAC {
	public static String tab_channel = "tab_channel";
	
	/**
	 * ��ȡ������
	 */
	public DBPaRs getChannelListRs(String channel) throws Exception {
		return DBPool.getInst().pQueryA(tab_channel, "code="+channel);
	}
	
	//--------------��̬��--------------
	
	private static ChannelBAC instance = new ChannelBAC();
		
	public static ChannelBAC getInstance() {			
		return instance;
	}
}
