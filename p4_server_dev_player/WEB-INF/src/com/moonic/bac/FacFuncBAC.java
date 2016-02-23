package com.moonic.bac;

import java.util.Enumeration;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.jspsmart.upload.Request;
import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ���ɹ���
 * @author John
 */
public class FacFuncBAC {
	public static final String tab_faction_acti = "tab_faction_acti";
	public static final String tab_fac_func_check = "tab_fac_func_check";
	
	/**
	 * ����Ƿ��ѿ���
	 */
	public ReturnValue checkFuncOpen(int playerid, short act) throws Exception {
		DBPaRs checkRs = DBPool.getInst().pQueryA(tab_fac_func_check, "actnum="+act);
		if(!checkRs.exist()){
			return new ReturnValue(true);
		}
		return checkFuncOpenByFuncnum(playerid, checkRs.getInt("funcnum"));
	}
	
	/**
	 * ����Ƿ��ѿ���
	 */
	public ReturnValue checkFuncOpen(int playerid, Request request) throws Exception {
		@SuppressWarnings("unchecked")
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements()){
			String webkey = names.nextElement();
			DBPaRs checkRs = DBPool.getInst().pQueryA(tab_fac_func_check, "webkey='"+webkey+"'");
			if(checkRs.exist()){
				return checkFuncOpenByFuncnum(playerid, checkRs.getInt("funcnum"));
			}
		}
		return new ReturnValue(true);
	}
	
	/**
	 * ��鹦���Ƿ��ѿ���
	 */
	public ReturnValue checkFuncOpenByFuncnum(int playerid, int funcnum) {
		try {
			DBPaRs listRs = DBPool.getInst().pQueryA(tab_faction_acti, "num="+funcnum);
			if(!listRs.exist()){
				return new ReturnValue(true);
			}
			String opencondStr = listRs.getString("opencond");
			if(opencondStr.equals("") || opencondStr.equals("0")){
				return new ReturnValue(true);
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			DBPaRs facRs = FactionBAC.getInstance().getDataRs(factionid);
			int[][] opencond = Tools.splitStrToIntArr2(opencondStr, "|", ",");
			for(int i = 0; i < opencond.length; i++){
				if(opencond[i][0] == 1){
					if(facRs.getInt("lv") < opencond[i][1]){
						BACException.throwInstance(listRs.getString("name")+"����δ���������ɵȼ�����");
					}
				}
			}
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	//--------------��̬��--------------
	
	private static FacFuncBAC instance = new FacFuncBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FacFuncBAC getInstance(){
		return instance;
	}
}
