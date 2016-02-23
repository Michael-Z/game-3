package com.moonic.bac;

import com.moonic.mirror.Mirror;
import com.moonic.util.DBPsRs;

/**
 * ��˿
 * @author John
 */
public class FansBAC extends Mirror {
	
	/**
	 * ����
	 */
	public FansBAC(){
		super("tab_friend", "friendid", null);
	}
	
	/**
	 * ��ȡ��˿ID����
	 */
	public int[] getFansIds(int playerid, byte type) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("friendid="+playerid);
		if(type != FriendBAC.TYPE_ALL){
			sb.append(" and type="+type);
		}
		int[] byids = null;
		DBPsRs fansRs = query(playerid, sb.toString());
		if(fansRs.count() > 0){
			byids = new int[fansRs.count()];
			while(fansRs.next()){
				byids[fansRs.getRow()-1] = fansRs.getInt("playerid");
			}
		}
		return byids;
	}
	
	/**
	 * ���ָ����Ҷ��ҵĹ�ע����
	 */
	public byte getFansType(int playerid, int targetid) throws Exception {
		DBPsRs fansRs = query(playerid, "friendid="+playerid+" and playerid="+targetid);
		byte type = FriendBAC.TYPE_NONE;
		if(fansRs.next()){
			type = fansRs.getByte("type");
		}
		return type;
	}
	
	//--------------��̬��--------------
	
	private static FansBAC instance = new FansBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FansBAC getInstance(){
		return instance;
	}
}
