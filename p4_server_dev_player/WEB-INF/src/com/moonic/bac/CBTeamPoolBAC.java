package com.moonic.bac;

import com.moonic.mirror.Mirror;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPsRs;

/**
 * ��ս�����
 * @author John
 */
public class CBTeamPoolBAC extends Mirror {
	
	/**
	 * ����
	 */
	public CBTeamPoolBAC() {
		super("tab_cb_teampool", "factionid", null);
	}
	
	/**
	 * ��ɢ������������
	 */
	public void clearTeam(DBHelper dbHelper, int factionid) throws Exception {
		DBPsRs cityStorRs = query(factionid, "factionid="+factionid);
		if(!cityStorRs.have()){
			return ;
		}
		delete(dbHelper, factionid, "factionid="+factionid);
	}
	
	/**
	 * �˳�������������
	 */
	public void clearTeam(DBHelper dbHelper, int playerid, int factionid) throws Exception {
		DBPsRs cityStorRs = query(factionid, "factionid="+factionid+" and playerid="+playerid);
		if(!cityStorRs.have()){
			return ;
		}
		delete(dbHelper, factionid, "factionid="+factionid+" and playerid="+playerid);
	}
	
	//-----------------��̬��--------------------
	
	private static CBTeamPoolBAC instance = new CBTeamPoolBAC();

	public static CBTeamPoolBAC getInstance() {
		return instance;
	}
}
