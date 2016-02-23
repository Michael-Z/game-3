package com.moonic.bac;

import java.sql.ResultSet;

import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;

import conf.LogTbName;

/**
 * ս����¼
 * @author John
 */
public class BattleRecordBAC {
	
	/**
	 * ��ȡս����¼
	 */
	public ReturnValue getBattleRecord(long battleid){
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Log());
		try {
			dbHelper.openConnection();
			//System.out.println("battleid="+battleid);
			ResultSet logRs = dbHelper.query(LogTbName.TAB_BATTLE_RECORD(), null, "battleid="+battleid);
			if(!logRs.next()){
				BACException.throwInstance("ս����¼������");
			}
			String replaydata = null;
			String particulardata = null;
			String propdata = null;
			try {
				replaydata = new String(logRs.getBytes("replaydata"), "UTF-8");	
			} catch (Exception e) {}
			try {
				particulardata = new String(logRs.getBytes("particulardata"), "UTF-8");	
			} catch (Exception e) {}
			try {
				propdata = new String(logRs.getBytes("propdata"), "UTF-8");	
			} catch (Exception e) {}
			StringBuffer sb = new StringBuffer();
			sb.append("<font color='#ff0000'>�ط�����</font>");
			sb.append("\r\n");
			sb.append(replaydata);
			sb.append("\r\n");
			sb.append("<font color='#ff0000'>��������</font>");
			sb.append("\r\n");
			sb.append(particulardata);
			sb.append("\r\n");
			sb.append("<font color='#ff0000'>��������</font>");
			sb.append("\r\n");
			sb.append(propdata);
			return new ReturnValue(true, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//-------------------��̬��---------------------
	
	private static BattleRecordBAC instance = new BattleRecordBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static BattleRecordBAC getInstance(){
		return instance;
	}
}
