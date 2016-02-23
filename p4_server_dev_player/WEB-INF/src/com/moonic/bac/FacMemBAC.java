package com.moonic.bac;

import org.json.JSONArray;

import server.common.Tools;

import com.moonic.mirror.Mirror;
import com.moonic.util.DBPsRs;

/**
 * ���ɳ�Ա
 * @author John
 */
public class FacMemBAC extends Mirror {
	
	/**
	 * ����
	 */
	public FacMemBAC() {
		super("tab_pla_faction", "factionid", null);
	}
	
	/**
	 * ��ȡ����ID����
	 * @param excludepid Ҫ�ų������ID
	 */
	public int[] getFacMemIDs(int factionid, int... excludepid) throws Exception {
		int[] byids = null;
		if(factionid > 0){
			DBPsRs plaRs = query(factionid, "factionid="+factionid);
			while(plaRs.next()){
				int playerid = plaRs.getInt("playerid");
				if(!Tools.intArrContain(excludepid, playerid)){
					byids = Tools.addToIntArr(byids, playerid);
				}
			}
		}
		return byids;
	}
	
	/**
	 * ��ȡ���ɹ���ԱID����(���ų��Լ���excludeid to 0)
	 */
	public int[] getFacMgrIDs(int factionid, int excludepid) throws Exception {
		int[] byids = null;
		if(factionid > 0){
			DBPsRs plaRs = query(factionid, "factionid="+factionid+" and position>0 and playerid!="+excludepid);
			while(plaRs.next()){
				byids = Tools.addToIntArr(byids, plaRs.getInt("playerid"));
			}
		}
		return byids;
	}
	
	/**
	 * ��ȡ���ɳ�Ա����
	 */
	public int getAmount(int factionid) throws Exception {
		DBPsRs facmemRs = query(factionid, "factionid="+factionid);
		return facmemRs.count();
	}
	
	/**
	 * ��ȡ������Ա��Ϣ
	 */
	public JSONArray getFacMemData(int factionid) throws Exception {
		DBPsRs facmemRs = query(factionid, "factionid="+factionid);
		JSONArray facmemarr = new JSONArray();
		while(facmemRs.next()){
			facmemarr.add(PlaFacBAC.getInstance().getMemData(facmemRs));
		}
		return facmemarr;
	}
	
	//--------------��̬��--------------
	
	private static FacMemBAC instance = new FacMemBAC();

	/**
	 * ��ȡʵ��
	 */
	public static FacMemBAC getInstance() {
		return instance;
	}
}
