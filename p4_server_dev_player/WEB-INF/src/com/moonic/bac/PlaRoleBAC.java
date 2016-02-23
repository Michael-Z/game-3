package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.txtdata.ArtifactData;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ��ɫ����
 * @author John
 */
public class PlaRoleBAC extends PlaBAC {
	
	/**
	 * ����
	 */
	private PlaRoleBAC() {
		super("tab_pla_role", "playerid");
	}

	/**
	 * ��ʼ��
	 */
	public void init(DBHelper dbHelper, int playerid, Object... param) throws Exception {
		DBPaRs playeruplvRs = DBPool.getInst().pQueryA(PlayerBAC.tab_player_uplv, "lv=1");
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.add("energy", playeruplvRs.getInt("maxenergy"));
		sqlStr.addDateTime("energystarttime", MyTools.getTimeStr());
		sqlStr.add("soulpoint", 0);
		sqlStr.add("eqsmeltfailam", "[]");
		sqlStr.add("jjccoin", 0);
		sqlStr.add("artifactdata", "{}");
		sqlStr.add("towercoin", 0);
		sqlStr.add("artifactrobtimes", 0);
		sqlStr.add("artifactprotecttimes", 0);
		sqlStr.add("moneytimes", 0);
		sqlStr.add("exptimes", 0);
		sqlStr.add("partnertimes", "{}");
		sqlStr.add("present", "[]");
		sqlStr.add("bepresent", "{}");
		sqlStr.add("deletefriend", "[]");
		sqlStr.add("totalbattlepower", 0);
		insert(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * �ָ�����
	 */
	public ReturnValue recoverEnergy(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ROLE_RECOVERENERGY);
			int recoveramount = recoverEnergy(dbHelper, playerid, gl);
			
			gl.save();
			return new ReturnValue(true, String.valueOf(recoveramount));
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ָ�������Ƭ�������
	 */
	public ReturnValue recoverArtifactRobTimes(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ARTIFACT_RECOVERROBTIMES);
			int recoveramount = recoverArtifactRobTimes(dbHelper, playerid, gl);
			
			gl.save();
			return new ReturnValue(true, String.valueOf(recoveramount));
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ָ�����
	 */
	public int recoverEnergy(DBHelper dbHelper, int playerid, GameLog gl) throws Exception {
		DBPaRs dataRs = getDataRs(playerid);
		int energy = dataRs.getInt("energy");
		int maxenergy = PlayerBAC.getInstance().getMaxEnergy(playerid);
		if(energy >= maxenergy){
			return 0;
		}
		long starttime = dataRs.getTime("energystarttime");//��ʼ����ʱ��
		long nowtime = System.currentTimeMillis();//��ǰʱ��
		long offtime = nowtime - starttime;//����ʱ��
		long timelen = MyTools.long_minu * 6;//�ָ�һ���ʱ��
		int group = (int) (offtime / timelen);//���ӵ�����
		if(group < 0){
			return 0;
		}
		int oldenergy = energy;
		energy += group;
		if(energy > maxenergy){
			energy = maxenergy;
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("energy", energy);
		sqlStr.addDateTime("energystarttime", MyTools.getTimeStr(starttime + timelen * group));
		update(dbHelper, playerid, sqlStr);
		
		int recoveramount = energy - oldenergy;
		gl.addChaNote("����", oldenergy, recoveramount);
		gl.addRemark("�ۻ�ʱ����"+(group*timelen/60000)+"����");
		return recoveramount;
	}
	
	/**
	 * �ָ��������
	 */
	public int recoverArtifactRobTimes(DBHelper dbHelper, int playerid, GameLog gl) throws Exception {
		DBPaRs dataRs = getDataRs(playerid);
		int times = dataRs.getInt("artifactrobtimes");
		if(times >= ArtifactData.maxrobtimes){
			return 0;
		}
		long starttime = dataRs.getTime("artifactrobstarttime");//��ʼ����ʱ��
		long nowtime = System.currentTimeMillis();//��ǰʱ��
		long offtime = nowtime - starttime;//����ʱ��
		long timelen = ArtifactData.robrecovertimelen;//�ָ�һ���ʱ��
		int group = (int) (offtime / timelen);//���ӵ�����
		if(group < 0){
			return 0;
		}
		int oldtimes = times;
		times += group;
		if(times > ArtifactData.maxrobtimes){
			times = ArtifactData.maxrobtimes;
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("artifactrobtimes", times);
		sqlStr.addDateTime("artifactrobstarttime", MyTools.getTimeStr(starttime + timelen * group));
		update(dbHelper, playerid, sqlStr);
		
		int recoveramount = times - oldtimes;
		gl.addChaNote("������Ƭ�������", oldtimes, recoveramount);
		gl.addRemark("�ۻ�ʱ����"+(group*timelen/60000)+"����");
		return recoveramount;
	}
	
	/**
	 * ��������
	 */
	public void upLevelOperate(DBHelper dbHelper, int playerid, int oldlv, int newlv, GameLog gl) throws Exception {
		DBPaRs plaroleRs = getDataRs(playerid);
		int oldenergy = plaroleRs.getInt("energy");
		int addenergy = 0;
		DBPsRs playeruplvRs = DBPool.getInst().pQueryS(PlayerBAC.tab_player_uplv, "lv>"+oldlv+" and lv<="+newlv);
		while(playeruplvRs.next()){
			addenergy += playeruplvRs.getInt("addenergy");
		}
		SqlString sqlStr = new SqlString();
		sqlStr.addChange("energy", addenergy);
		update(dbHelper, playerid, sqlStr);
		gl.addChaNote("����", oldenergy, addenergy);
	}
	
	/**
	 * ��ֵ�ص�
	 */
	public void subTrigger(DBHelper dbHelper, int playerid, String col, long srcVal, long nowVal) throws Exception {
		if(col.equals("energy")){
			int maxenergy = PlayerBAC.getInstance().getMaxEnergy(playerid);
			if(srcVal >= maxenergy && nowVal < maxenergy){
				SqlString sqlStr = new SqlString();
				sqlStr.addDateTime("energystarttime", MyTools.getTimeStr());
				update(dbHelper, playerid, sqlStr);
			}
//			PlaMysteryShopBAC.getInstance().handleEnergyConsume(dbHelper, playerid, (int)(srcVal-nowVal));
		} else 
		if(col.equals("artifactrobtimes")){
			if(srcVal >= ArtifactData.maxrobtimes && nowVal < ArtifactData.maxrobtimes){
				SqlString sqlStr = new SqlString();
				sqlStr.addDateTime("artifactrobstarttime", MyTools.getTimeStr());
				update(dbHelper, playerid, sqlStr);
			}
		}
	}

	/**
	 * ��ȡ����
	 */
	public JSONArray getLoginData(int playerid) throws Exception {
		DBPaRs plaroleRs = getDataRs(playerid);
		JSONArray arr = new JSONArray();
		arr.add(plaroleRs.getInt("energy"));//����
		arr.add(plaroleRs.getTime("energystarttime"));//�ϴλָ�����ʱ��
		arr.add(plaroleRs.getInt("soulpoint"));//���
		arr.add(new JSONArray(plaroleRs.getString("eqsmeltfailam")));//װ������ʧ�ܴ���
		arr.add(plaroleRs.getInt("jjccoin"));//������
		arr.add(new JSONObject(plaroleRs.getString("artifactdata")));//��������
		arr.add(plaroleRs.getInt("towercoin"));//����
		arr.add(plaroleRs.getTime("artifactendtime"));//������Ƭ���ᱣ������ʱ��
		arr.add(plaroleRs.getInt("artifactrobtimes"));//������Ƭ���������
		arr.add(plaroleRs.getInt("artifactprotecttimes"));//����������Ƭ���ᱣ������
		arr.add(plaroleRs.getTime("artifactrobstarttime"));;//�������������ʼʱ��
		arr.add(new JSONArray(plaroleRs.getString("present")));//���ͺ�������
		arr.add(new JSONObject(plaroleRs.getString("bepresent")));//��������������
		return arr;
	}
	
	/**
	 * ��������
	 */
	public void resetData(DBHelper dbHelper, int playerid) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("artifactprotecttimes", 0);
		sqlStr.add("moneytimes", 0);
		sqlStr.add("exptimes", 0);
		sqlStr.add("partnertimes", "{}");
		sqlStr.add("present", "[]");
		update(dbHelper, playerid, sqlStr);
	}
	//--------------��̬��--------------
	
	private static PlaRoleBAC instance = new PlaRoleBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static PlaRoleBAC getInstance(){
		return instance;
	}
}
