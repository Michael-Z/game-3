package com.moonic.bac;

import java.sql.ResultSet;

import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTools;

import conf.LogTbName;

public class BattleReplayBAC {
	
	/**
	 * ��ȡս���ط�����
	 */
	public ReturnValue getBattleReplay(long battleid){//TODO ���Ż�
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Log());
		try {
			ResultSet rs = dbHelper.query(LogTbName.tab_battle_replay(), "replaydata", "battleid="+battleid);
			if(!rs.next()){
				BACException.throwInstance("δ�ҵ������־");
			}
			return new ReturnValue(true, new String(rs.getBytes("replaydata"), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ս���ط�
	 */
	public void saveReplay(long battleId, String replayData, int validityday, int battlefrom){
		try {
			SqlString logSqlStr = new SqlString();
			logSqlStr.add("battleid", battleId);
			logSqlStr.addBlob("replaydata", replayData.getBytes("UTF-8"));
			logSqlStr.addDateTime("expirationtime", MyTools.getTimeStr(System.currentTimeMillis()+MyTools.long_day*validityday));
			logSqlStr.add("battlefrom", battlefrom);
			DBHelper.logInsert(LogTbName.tab_battle_replay(), logSqlStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �������ս��¼��
	 */
	public ReturnValue clearExpirationReplay(){
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Log());
		try {
			dbHelper.delete(LogTbName.tab_battle_replay(), "expirationtime<="+MyTools.getTimeStr()+" or battlefrom=2");
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//-----------------��̬��--------------------
	
	private static BattleReplayBAC instance = new BattleReplayBAC();

	public static BattleReplayBAC getInstance() {
		return instance;
	}
}
