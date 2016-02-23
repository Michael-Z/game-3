package com.moonic.bac;

import org.json.JSONArray;

import com.moonic.mirror.Mirror;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPsRs;

/**
 * ��������
 * @author John
 */
public class FacApplyBAC extends Mirror {
	
	/**
	 * ����
	 */
	public FacApplyBAC(){
		super("tab_faction_apply", "factionid", null);
	}
	
	/**
	 * �������������
	 */
	public void clearAllApplyer(DBHelper dbHelper, int playerid, String pname, int factionid) throws Exception {
		DBPsRs applyRs = query(factionid, "factionid="+factionid);
		if(applyRs.count() > 0){
			int[] byids = new int[applyRs.count()];
			while(applyRs.next()){
				byids[applyRs.getRow()-1] = applyRs.getInt("playerid");
			}
			JSONArray pusharr = new JSONArray();
			pusharr.add(factionid);//����ID
			pusharr.add(playerid);//���ID
			pusharr.add(pname);//�����
			pusharr.add(1);//����ʽ
			PushData.getInstance().sendPlaToSome(SocketServer.ACT_FACTION_PROCESS_APPLY, pusharr.toString(), byids);
			delete(dbHelper, factionid, "factionid="+factionid);
		}
	}
	
	/**
	 * ��ȡ����������
	 */
	public int getAmount(int factionid) throws Exception {
		DBPsRs rs = query(factionid, "factionid="+factionid);
		return rs.count();
	}
	
	/**
	 * ��ȡ������������Ϣ
	 */
	public JSONArray getApplyerData(int factionid) throws Exception {
		DBPsRs applyRs = query(factionid, "factionid="+factionid);
		JSONArray applyarr = new JSONArray();
		while(applyRs.next()){
			applyarr.add(PlaFacApplyBAC.getInstance().getApplyData(applyRs));
		}
		return applyarr;
	}
	
	//--------------��̬��--------------
	
	private static FacApplyBAC instance = new FacApplyBAC();

	/**
	 * ��ȡʵ��
	 */
	public static FacApplyBAC getInstance() {
		return instance;
	}
}
