package com.moonic.bac;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.moonic.mgr.LockStor;
import com.moonic.mirror.Mirror;
import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ���������ɹ���
 * @author John
 */
public class ServerFacBAC extends Mirror {
	
	/**
	 * ����
	 */
	public ServerFacBAC(){
		super("tab_faction_stor", "serverid", null);
	}
	
	private static DBPsRs factionRs;
	
	/**
	 * ���°�������
	 */
	public ReturnValue updateFactionRanking(){
		try {
			factionRs  = query(Conf.sid, "serverid="+Conf.sid, "lv desc,money desc");
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ�����б�
	 * @param start ��ʼ�к�
	 * @param end ��ֹ�к�
	 */
	public ReturnValue getFactionList(int playerid, int page){
		try {
			if(page <= 0){
				BACException.throwInstance("ҳ������");
			}
			JSONArray jsonarr = new JSONArray();
			int start_r = (page-1)*9+1;
			int end_r = page*9;
			DBPsRs rs = factionRs;
			synchronized (LockStor.getLock(LockStor.FACTION_RAKNING)) {
				for(int i = start_r; i <= end_r; i++){
					if(rs.count() < i){
						break;
					}
					rs.setRow(i);
					jsonarr.add(FactionBAC.getInstance().getInfo(rs));
				}	
			}
			JSONArray returnarr = new JSONArray();
			returnarr.add(rs.count());
			returnarr.add(jsonarr);
			return new ReturnValue(true, returnarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public ReturnValue getPlaRanking(int playerid){
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			int ranking = getRanking(factionid);
			return new ReturnValue(true, String.valueOf(ranking));
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public ReturnValue getRanking(int playerid, int factionid){
		try {
			int ranking = getRanking(factionid);
			return new ReturnValue(true, String.valueOf(ranking));
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ��������
	 */
	public int getRanking(int factionid) throws Exception {
		DBPsRs rs = factionRs;
		synchronized (LockStor.getLock(LockStor.FACTION_RAKNING)) {
			int ranking = 1;
			rs.beforeFirst();
			while(rs.next()){
				if(rs.getInt("id")==factionid){
					ranking = rs.getRow();
					break;
				}
			}
			return ranking;
		}
	}
	
	/**
	 * ��������
	 */
	public ReturnValue searchFaction(int playerid, String name){
		try {
			MyTools.checkNoChar(name);
			DBPsRs facRs = query(Conf.sid, "serverid="+Conf.sid+" and name='"+name+"'");
			if(!facRs.next()){
				BACException.throwInstance("δ�ҵ�ƥ��İ���");
			}
			JSONArray jsonarr = FactionBAC.getInstance().getInfo(facRs);
			return new ReturnValue(true, jsonarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	//--------------��̬��--------------
	
	private static ServerFacBAC instance = new ServerFacBAC();

	/**
	 * ��ȡʵ��
	 */
	public static ServerFacBAC getInstance() {
		return instance;
	}
}
