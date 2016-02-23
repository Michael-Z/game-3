package com.moonic.socket;

import org.json.JSONArray;
import org.json.JSONObject;

import com.moonic.bac.MsgBAC;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ��Ϸ����
 * @author John
 */
public class GamePushData extends PushData {
	private int channel;
	private JSONObject pushobj;
	private JSONArray msgarr;
	
	/**
	 * ����
	 */
	public GamePushData(int num) throws Exception {
		DBPaRs msgRs = DBPool.getInst().pQueryA(MsgBAC.tab_game_push_msg, "num="+num);
		if(!msgRs.exist()){
			System.out.println("��Ϸ������Ϣ��Ų����� num="+num);
			return;
		}
		pushobj = new JSONObject();
		msgarr = new JSONArray();
		channel = msgRs.getInt("showchannel");
		allowIgnore = msgRs.getInt("allowignore")==1;
		nopool = true;
		add(num);
		pushobj.put("channel", channel);
		pushobj.put("content", msgarr);
	}

	/**
	 * ����Ԫ��
	 */
	public GamePushData add(Object obj){
		msgarr.add(obj);
		return this;
	}
	
	/**
	 * ���ý�ɫ��Ϣ
	 */
	public GamePushData setPlaInfo(DBPaRs plaRs) throws Exception {
		int pid = plaRs.getInt("id");
		String pname = plaRs.getString("name");
		return setPlaInfo(pid, pname);
	}
	
	/**
	 * ���ý�ɫ��Ϣ
	 */
	public GamePushData setPlaInfo(int id, String name) {
		pushobj.put("pid", id);
		pushobj.put("pname", name);
		return this;
	}
	
	/**
	 * ����
	 */
	public void sendToAllOL() {
		if(channel == 0){
			return;
		}
		sendPlaToAllOL(SocketServer.ACT_MESSAGE_GAMEPUSH, pushobj.toString());
	}
	
	/**
	 * ����
	 */
	public void sendToAllFac(int factionid) throws Exception {
		if(channel == 0){
			return;
		}
		sendPlaToFacMem(SocketServer.ACT_MESSAGE_GAMEPUSH, pushobj.toString(), factionid);
	}
	
	/**
	 * ����
	 */
	public void sendToOne(int playerid){
		if(channel == 0){
			return;
		}
		sendPlaToOne(SocketServer.ACT_MESSAGE_GAMEPUSH, pushobj.toString(), playerid);
	}
	
	//--------------��̬��---------------
	
	/**
	 * ��ȡʵ��
	 */
	public static GamePushData getInstance(int num) throws Exception {
		return new GamePushData(num);
	}
}
