package com.moonic.bac;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.Out;

/**
 * VIP
 * @author John
 */
public class VipBAC {
	public static final String tab_vip = "tab_vip";
	
	/**
	 * ����VIP���
	 */
	public ReturnValue buyVipGift(int playerid, int buyvip){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs vipRs = DBPool.getInst().pQueryA(tab_vip, "lv="+buyvip);
			if(!vipRs.exist()){
				BACException.throwInstance("��������� buyvip="+buyvip);
			}
			int vip = PlayerBAC.getInstance().getIntValue(playerid, "vip");
			if(vip < buyvip){
				BACException.throwInstance("VIP�ȼ����� vip/buyvip="+vip+"/"+buyvip);
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_BUY_VIPGIFT);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, vipRs.getInt("gprice"), gl);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, vipRs.getString("gcontent"), ItemBAC.SHORTCUT_MAIL, 1, gl);
			
			gl.addRemark("����VIP��� buyvip="+buyvip);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���VIPָ�������Ƿ��ѿ���
	 */
	public boolean checkVipFuncOpen(int viplevel, int funcnum) throws Exception {
		return getVipFuncData(viplevel, funcnum)==1;
	}
	
	/**
	 * ��ȡVIPָ����������
	 */
	public int getVipFuncData(int viplevel, int funcnum) throws Exception {
		DBPaRs vipRs = DBPool.getInst().pQueryA(tab_vip, "lv="+viplevel);
		if(vipRs.exist()){
			return vipRs.getInt("func"+funcnum);
		} else {
			Out.println("�쳣��VIP�ȼ���"+viplevel);
			return 0;
		}
	}
	
	/**
	 * �ı�VIP
	 */
	public void changeVIP(DBHelper dbHelper, int playerid, int addexp, GameLog gl) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		SqlString sqlStr = new SqlString();
		addChangeVIPToSqlStr(plaRs, addexp, sqlStr, gl);
		PlayerBAC.getInstance().update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ���Ӹı�VIP���ݵ�SqlString
	 */
	public int[] addChangeVIPToSqlStr(DBPaRs plaRs, int addvipexp, SqlString sqlStr, GameLog gl) throws Exception {
		int p_vip = plaRs.getInt("vip");
		int p_buycoin = plaRs.getInt("buycoin");
		int new_vip = p_vip;
		DBPsRs vipRs = DBPool.getInst().pQueryS(VipBAC.tab_vip, "lv>"+p_vip, "lv");
		while(vipRs.next()){
			if(p_buycoin+addvipexp < vipRs.getInt("rechargecoin")){
				continue;
			}
			new_vip = vipRs.getInt("lv");
		}
		if(new_vip > p_vip){
			sqlStr.add("vip", new_vip);
			gl.addChaNote("VIP", p_vip, new_vip-p_vip);
		}
		sqlStr.addChange("buycoin", addvipexp);
		gl.addChaNote("VIP����", p_buycoin, addvipexp);
		return new int[]{p_vip, new_vip, p_buycoin+addvipexp};
	}
	
	//------------------��̬��--------------------
	
	private static VipBAC instance = new VipBAC();

	public static VipBAC getInstance() {
		return instance;
	}
}
