package com.moonic.bac;

import org.json.JSONArray;

import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPRs;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPsRs;

/**
 * ��Ұ�������
 * @author John
 */
public class PlaFacApplyBAC extends PlaStorBAC {
	
	/**
	 * ����
	 */
	public PlaFacApplyBAC(){
		super("tab_faction_apply", "playerid", "factionid");
	}
	
	/**
	 * �����������
	 */
	public void clearAllApply(DBHelper dbHelper, int playerid, int nofacid) throws Exception {
		DBPsRs applyRs = query(playerid, "playerid="+playerid+" and factionid!="+nofacid);
		if(applyRs.count() > 0){
			while(applyRs.next()){
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_REVOCATION_APPLY, String.valueOf(playerid), applyRs.getInt("factionid"), 0);
			}
			delete(dbHelper, playerid, "playerid="+playerid);
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public int getAmount(int playerid) throws Exception {
		DBPsRs rs = query(playerid, "playerid="+playerid);
		return rs.count();
	}
	
	/**
	 * ��ȡ������Ϣ
	 */
	public JSONArray getApplyData(DBPRs applyRs) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(applyRs.getInt("playerid"));
		JSONArray arr = new JSONArray();
		arr.add(plaRs.getInt("id"));
		arr.add(plaRs.getString("name"));
		arr.add(plaRs.getInt("lv"));
		arr.add(plaRs.getInt("vip"));
		arr.add(plaRs.getInt("num"));
		arr.add(applyRs.getTime("applytime"));
		return arr;
	}
	
	/**
	 * ��ȡ��������б�
	 */
	public JSONArray getApplyFacList(int playerid) throws Exception {
		JSONArray applyarr = new JSONArray();
		DBPsRs applyRs = query(playerid, "playerid="+playerid);
		while(applyRs.next()){
			applyarr.add(applyRs.getInt("factionid"));
		}
		return applyarr;
	}
	
	//--------------��̬��--------------
	
	private static PlaFacApplyBAC instance = new PlaFacApplyBAC();

	/**
	 * ��ȡʵ��
	 */
	public static PlaFacApplyBAC getInstance() {
		return instance;
	}
}
