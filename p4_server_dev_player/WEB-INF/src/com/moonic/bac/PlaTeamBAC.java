package com.moonic.bac;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.mirror.MirrorMgr;
import com.moonic.servlet.GameServlet;
import com.moonic.team.TeamActivity;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;

import conf.Conf;

/**
 * ��ɫ���
 * @author wkc
 */
public class PlaTeamBAC extends PlaBAC {
	public static TeamActivity teamActivity;
	
	public static final String tab_team_boss = "tab_team_boss";

	public PlaTeamBAC() {
		super("tab_pla_team", "playerid");
		needcheck = false;
	}
	
	@Override
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.add("times", 0);
		insert(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ������ӻ
	 */
	public ReturnValue start(long actiTimeLen, byte isConstraint){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(teamActivity != null){
				if(isConstraint == 1){
					teamActivity = null;
				} else{
					BACException.throwInstance("��ӻ������");
				}
			}
			teamActivity = new TeamActivity(actiTimeLen);
			teamActivity.start();
			return new ReturnValue(true, "�����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������
	 * @param type,�������� 1~3
	 */
	public ReturnValue createTeam(int playerid, int type){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			DBPaRs plaTeamRs = getDataRs(playerid);
			if(!plaTeamRs.exist()){
				BACException.throwInstance("������δ����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_CREATE);
			int num = teamActivity.createTeam(playerid, type, gl);
			gl.save();
			return new ReturnValue(true, String.valueOf(num));
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * �������
	 */
	public ReturnValue joinTeam(int playerid, int num){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			DBPaRs plaTeamRs = getDataRs(playerid);
			if(!plaTeamRs.exist()){
				BACException.throwInstance("������δ����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_JOIN);
			JSONArray jsonarr = teamActivity.joinTeam(playerid, num, gl);
			gl.save();
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * �߳�����
	 */
	public ReturnValue kickOut(int playerid, int num, int memberid){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_KICK);
			teamActivity.kickOut(playerid, num, memberid, gl);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ����
	 */
	public ReturnValue format(int playerid, int num, String posStr){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			JSONArray posarr = new JSONArray(posStr);
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_FORMAT);
			int battlePower = teamActivity.format(playerid, num, posarr, gl);
			gl.save();
			return new ReturnValue(true, String.valueOf(battlePower));
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ׼��
	 */
	public ReturnValue beReady(int playerid, int num){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_BEREADY);
			teamActivity.beReady(playerid, num, gl);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ȡ��׼��
	 */
	public ReturnValue cancelReady(int playerid, int num){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_CANCELREADY);
			teamActivity.cancelReady(playerid, num, gl);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ս��
	 */
	public ReturnValue battle(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_BATTLE);
			JSONArray jsonarr = teamActivity.battle(dbHelper, playerid, num, gl);
			gl.save();
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ�����б�
	 * @param type,1~3,��������
	 */
	public ReturnValue getTeamList(int playerid, int type){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			if(type < 1 || type > 3){
				BACException.throwInstance("�������Ͳ�������");
			}
			JSONArray jsonarr = teamActivity.getTeamList(type);
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ��ȡ����
	 */
	public ReturnValue getDate(int playerid){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			DBPaRs plaTeamRs = getDataRs(playerid);
			if(!plaTeamRs.exist()){
				BACException.throwInstance("������δ����");
			}
			JSONArray jsonarr = new JSONArray();
			jsonarr.add(plaTeamRs.getInt("times"));//��������ս����
			jsonarr.add(teamActivity.getTeamList(1));//�����б�
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * �˳�����
	 */
	public ReturnValue exitTeam(int playerid, int num){
		try {
			if(teamActivity == null){
				BACException.throwInstance("��ӻδ��ʼ���ѽ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TEAM_ACTI_EXIT);
			teamActivity.exitTeam(playerid, num, gl);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * �ǳ�
	 */
	public void logout(int playerid) throws Exception {
		if(teamActivity != null){
			teamActivity.logout(playerid);
		} 
	}
	
	/**
	 * ���ÿɻ�ý�������
	 * @throws Exception 
	 */
	public void resetTimes(DBHelper dbHelper) throws Exception{
		SqlString sqlStr = new SqlString();
		sqlStr.add("times", 0);
		dbHelper.update("tab_pla_team", sqlStr, "serverid="+Conf.sid);
		MirrorMgr.clearTabData("tab_pla_team", false);//�ֶ��徵��
	}
	
	/**
	 * ��ȡ��½����
	 */
	public JSONArray getLoginData() {
		JSONArray jsonarr = new JSONArray();
		if(teamActivity != null) {
			jsonarr = teamActivity.getLoginData();
		}
		return jsonarr;
	}
	
	//------------------��̬��------------------
	
	private static PlaTeamBAC instance = new PlaTeamBAC();
	
	public static PlaTeamBAC getInstance(){
		return instance;
	}
	
	//------------------������------------------
	
	/**
	 * �������û�ý�������
	 */
	public ReturnValue debugResetTimes(int playerid) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("times", 0);
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
}
