package com.moonic.bac;

import org.json.JSONArray;

import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;


public class ChannelBAC {
	public static String tab_channel = "tab_channel";
	
	/**
	 * ���ݱ����ȡ��������
	 */
	public String getName(String code){
		String name = null;
		try {
			DBPaRs channelRs = DBPool.getInst().pQueryA(tab_channel, "code='"+code+"'");
			if(channelRs.exist()){
				name = channelRs.getString("name");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}
	
	/**
	 * ��ȡ��������
	 */
	public JSONArray getAllData(){
		JSONArray jsonarr = null;
		try {
			DBPsRs channelRs = DBPool.getInst().pQueryS(tab_channel);
			jsonarr = channelRs.getJsonarr();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonarr;
	}
	
	//-----------------��̬��--------------------
	
	private static ChannelBAC self = new ChannelBAC();

	public static ChannelBAC getInstance() {
		return self;
	}
}
