package com.moonic.bac;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.SpriteBox;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.Player;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.MyTools;

/**
 * ���ɸ���
 * @author John
 */
public class FacCopymapBAC {
	public static final String tab_fac_cm_map = "tab_fac_cm_map";
	public static final String tab_fac_cm_point = "tab_fac_cm_point";
	public static final String tab_fac_cm_damage = "tab_fac_cm_damage";
	
	/**
	 * ���븱��
	 */
	public ReturnValue into(int playerid, int pointnum, String posStr){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs pointRs = DBPool.getInst().pQueryA(tab_fac_cm_point, "num="+pointnum);
			if(!pointRs.exist()){
				BACException.throwInstance("��λ������ num="+pointnum);
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			JSONObject placmdataobj = new JSONObject(plafacRs.getString("cmdata"));//���˸�������
			if(placmdataobj.optInt(pointRs.getString("map")) >= 2){//���˽��븱������
				BACException.throwInstance("����˸�����������");
			}
			synchronized (LockStor.getLock(LockStor.FAC_COPYMAP, factionid)) {
				DBPaRs facRs = FactionBAC.getInstance().getDataRs(factionid);
				JSONObject cmdataobj = new JSONObject(facRs.getString("cmdata"));//���ɸ�������
				JSONArray maparr = cmdataobj.optJSONArray(pointRs.getString("map"));//��ͼ����
				if(maparr == null){//û���
					if(pointRs.getInt("posnum") != 1){
						BACException.throwInstance("��λδ����������ͨ��ǰ��ĵ�λ");
					}
					DBPaRs mapRs = DBPool.getInst().pQueryA(tab_fac_cm_map, "num="+pointRs.getString("map"));
					int[][] opencond = Tools.splitStrToIntArr2(mapRs.getString("opencond"), "|", ",");
					for(int i = 0; i < opencond.length; i++){//����Ƿ������������
						if(opencond[i][0] == 1 && facRs.getInt("lv") < opencond[i][1]){
							BACException.throwInstance("��ͼδ���������ɵȼ�����");
						} else 
						if(opencond[i][1] == 2 && !(new JSONArray(facRs.getString("cmpassdata")).contains(opencond[i][1]))){
							BACException.throwInstance("��ͼδ������δͨ��ָ����ͼ");
						}
					}
					maparr = new JSONArray();
					maparr.add(1);//��ǰ�򵽵ĵ�λ����
					maparr.add(0);//�������ʱ��
					//maparr.add(null);//����BOSSʣ��Ѫ��
				} else {
					if(maparr.optInt(0) == -1){//��ǰ�ﵽ�ĵ�λΪ-1��ʾ��ͨ��
						BACException.throwInstance("��ͨ��");
					}
					if(maparr.optInt(0) != pointRs.getInt("posnum")){
						BACException.throwInstance("��λ���� ��ǰ��λ��"+maparr.optInt(0)+" �ͻ��˵�λ��"+pointRs.getInt("posnum"));
					}
					if(maparr.optInt(3) != 0){//��ǰ����ս���Ľ�ɫID
						BACException.throwInstance("�а�������ս�������Ժ����");
					}
				}
				Player pla = SocketServer.getInstance().plamap.get(playerid);
				if(pla.verifybattle_battlebox != null){//����ս����
					BACException.throwInstance("�����˳�����");
				}
				JSONArray posarr = new JSONArray(posStr);
				PartnerBAC.getInstance().checkPosarr(playerid, posarr, 0, 1);
				TeamBox teambox1 = PartnerBAC.getInstance().getTeamBox(playerid, 0, new JSONArray(posStr));
				TeamBox teambox2 = Enemy.getInstance().createTeamBox(pointRs.getString("enemy"), maparr.optJSONArray(2));
				BattleBox battlebox = new BattleBox();
				battlebox.teamArr[0].add(teambox1);
				battlebox.teamArr[1].add(teambox2);
				battlebox.parameterarr = new JSONArray(new int[]{factionid, pointRs.getInt("map"), pointRs.getInt("posnum")});//POSNUM�ǵ�λ���
				pla.verifybattle_battlebox = battlebox;
				
				maparr.put(3, playerid);//���õ�ǰս����Ϊ�Լ�
				cmdataobj.put(pointRs.getString("map"), maparr);//���°��ɸ�������
				SqlString sqlStr = new SqlString();
				sqlStr.add("cmdata", cmdataobj.toString());
				FactionBAC.getInstance().update(dbHelper, factionid, sqlStr);//���°��ɸ�������
				/*
				placmdataobj.put(pointRs.getString("map"), placmdataobj.optInt(pointRs.getString("map"))+1);//��¼����˸�������
				SqlString plaSqlStr = new SqlString();
				plaSqlStr.add("cmdata", placmdataobj.toString());
				PlaFacBAC.getInstance().update(dbHelper, playerid, plaSqlStr);
				*/
				JSONArray pusharr = new JSONArray();
				pusharr.add(playerid);
				pusharr.add(pointnum);//��λ���
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACCOPYMAP_INTO, pusharr.toString(), factionid, playerid);
				
				return new ReturnValue(true, battlebox.getJSONArray().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����ս��������֪ͨ
	 */
	public ReturnValue end(int playerid, String battleRecord){
		DBHelper dbHelper = new DBHelper();
		try {
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			if(pla.verifybattle_battlebox == null){
				BACException.throwInstance("���Ƚ��븱��");
			}
			BattleManager.verifyPVEBattle(pla.verifybattle_battlebox, battleRecord);
			JSONArray parameterarr = pla.verifybattle_battlebox.parameterarr;
			DBPaRs facRs = FactionBAC.getInstance().getDataRs(parameterarr.optInt(0));
			JSONObject cmdataobj = new JSONObject(facRs.getString("cmdata"));
			JSONArray maparr = cmdataobj.optJSONArray(parameterarr.optString(1));//������ͼ����
			int totaldamage = 0;
			JSONArray new_hparr = new JSONArray();
			JSONArray hparr = maparr.optJSONArray(2);
			ArrayList<SpriteBox> enemySprites = pla.verifybattle_battlebox.teamArr[1].get(0).sprites;
			for(int i = 0; i < enemySprites.size(); i++){
				SpriteBox spritebox = enemySprites.get(i);
				int index = spritebox.posNum-1;
				int oldhp = spritebox.battle_prop[Const.PROP_MAXHP];
				if(hparr != null){
					oldhp = hparr.optInt(index);
				}
				int currhp = spritebox.battle_prop[Const.PROP_HP];
				new_hparr.put(index, currhp);
				totaldamage += oldhp - currhp;
			}
			DBPaRs pointRs = DBPool.getInst().pQueryA(tab_fac_cm_point, "map="+parameterarr.optInt(1)+" and posnum="+parameterarr.optInt(2));
			JSONArray cmpassdataarr = new JSONArray(facRs.getString("cmpassdata"));
			boolean passmap = false;
			if(pla.verifybattle_battlebox.winTeam == Const.teamA){
				DBPaRs pointRs2 = DBPool.getInst().pQueryA(tab_fac_cm_point, "map="+parameterarr.optInt(1)+" and posnum="+(parameterarr.optInt(2)+1));
				if(pointRs2.exist()){
					maparr.put(0, parameterarr.optInt(2)+1);
				} else {
					maparr.put(0, -1);//-1��ʾ��ͼȫ��ͨ��
					if(!cmpassdataarr.contains(parameterarr.optInt(1))){//��¼ͨ�ص�ͼ
						cmpassdataarr.add(parameterarr.optInt(1));
					}
					passmap = true;
				}
				if(maparr.length() >= 3){
					maparr.remove(2);//���ʣ��Ѫ��
				}
				int[] memarr = FacMemBAC.getInstance().getFacMemIDs(parameterarr.optInt(0));//�ʼ�����ͨ�ؽ���
				MailBAC.getInstance().sendModelMail(dbHelper, memarr, 1, null, new Object[]{pointRs.getString("name")}, pointRs.getString("award"));
			} else {
				maparr.put(2, new_hparr);//����ʣ��Ѫ��
			}
			pla.verifybattle_battlebox = null;
			
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACCOPYMAP_END);
			maparr.remove(maparr.length()-1);//�������ս�����˵ļ�¼
			SqlString sqlStr = new SqlString();
			sqlStr.add("cmdata", cmdataobj.toString());
			sqlStr.add("cmpassdata", cmpassdataarr.toString());
			FactionBAC.getInstance().update(dbHelper, parameterarr.optInt(0), sqlStr);
			/**/
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			JSONObject placmdataobj = new JSONObject(plafacRs.getString("cmdata"));
			placmdataobj.put(parameterarr.optString(1), placmdataobj.optInt(parameterarr.optString(1))+1);//��¼����˸�������
			SqlString plaSqlStr = new SqlString();
			plaSqlStr.add("cmdata", placmdataobj.toString());
			PlaFacBAC.getInstance().update(dbHelper, playerid, plaSqlStr);
			/**/
			PlaRoleBAC.getInstance().subValue(dbHelper, playerid, "energy", pointRs.getInt("needenergy"), gl, "����");
			
			JSONArray returnarr = new JSONArray();//��������˺��Ľ���
			DBPaRs damageRs = DBPool.getInst().pQueryA(tab_fac_cm_damage, "mindamage<="+totaldamage+" and maxdamage>="+totaldamage);
			if(damageRs.exist()){
				JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, damageRs.getString("award"), ItemBAC.SHORTCUT_MAIL, 1, gl);
				returnarr.add(damageRs.getString("award"));
				returnarr.add(awardarr);
				FactionBAC.getInstance().changeMoney(dbHelper, parameterarr.optInt(0), damageRs.getInt("facmoney"), gl);//�Ӱ����ʽ�
			}
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_TEAMCM, gl);
			
			if(passmap){
				int[] pids = FacMemBAC.getInstance().getFacMemIDs(parameterarr.optInt(0));
				for(int i = 0; pids != null && i < pids.length; i++){
					PlaWelfareBAC.getInstance().updateAchieveProgress(dbHelper, pids[i], PlaWelfareBAC.ACHIEVE_FACCM_NUM, parameterarr.optInt(1), gl); 
				}
			}
			
			PlaWelfareBAC.getInstance().updateAchieveProgress(dbHelper, playerid, PlaWelfareBAC.ACHIEVE_FACCM_TIMES, gl);
			
			JSONArray pusharr = new JSONArray();
			pusharr.add(playerid);//��ɫID
			pusharr.add(pointRs.getInt("num"));//��λ���
			pusharr.add(maparr.optJSONArray(2));//ʣ��Ѫ��
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACCOPYMAP_END, pusharr.toString(), parameterarr.optInt(0), playerid);
			
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 25);
			
			gl.addRemark("����˺���"+totaldamage);
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �˳�����
	 */
	public ReturnValue exit(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			exit(dbHelper, playerid);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �˳�����
	 */
	public void exit(DBHelper dbHelper, int playerid) throws Exception {
		DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
		int factionid = plafacRs.getInt("factionid");
		if(factionid == 0){
			return;
		}
		DBPaRs facRs = FactionBAC.getInstance().getDataRs(factionid);
		JSONObject cmdataobj = new JSONObject(facRs.getString("cmdata"));
		int mapnum = 0;
		int posnum = 0;
		@SuppressWarnings("rawtypes")
		Iterator iterator = cmdataobj.keys();
		while(iterator.hasNext()){
			String num = (String)iterator.next();
			JSONArray maparr = cmdataobj.optJSONArray(num);
			if(maparr.length() >= 4 && maparr.optInt(3) == playerid){
				maparr.remove(maparr.length()-1);//�������ս�����˵ļ�¼
				mapnum = Integer.valueOf(num);
				posnum = maparr.optInt(0);
				break;
			}
		}
		if(mapnum != 0){
			SqlString sqlStr = new SqlString();
			sqlStr.add("cmdata", cmdataobj.toString());
			FactionBAC.getInstance().update(dbHelper, factionid, sqlStr);
			DBPaRs pointRs = DBPool.getInst().pQueryA(tab_fac_cm_point, "map="+mapnum+" and posnum="+posnum);
			JSONArray pusharr = new JSONArray();
			pusharr.add(playerid);//��ɫID
			pusharr.add(pointRs.getInt("num"));//��λ���
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACCOPYMAP_EXIT, pusharr.toString(), factionid, playerid);
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			pla.verifybattle_battlebox = null;
		}
	}
	
	/**
	 * ���õ�ͼ
	 */
	public ReturnValue resetMap(int playerid, int mapnum){
		DBHelper dbHelper = new DBHelper();
		try {
			//TODO �Ƿ���Ӫҵʱ����
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") == 0){
				BACException.throwInstance("û��Ȩ��");
			}
			DBPaRs mapRs = DBPool.getInst().pQueryA(tab_fac_cm_map, "num="+mapnum);
			if(!mapRs.exist()){
				BACException.throwInstance("��ͼ������ mapnum="+mapnum);
			}
			synchronized (LockStor.getLock(LockStor.FAC_COPYMAP, factionid)) {
				DBPaRs facRs = FactionBAC.getInstance().getDataRs(factionid);
				JSONObject cmdataobj = new JSONObject(facRs.getString("cmdata"));
				JSONArray maparr = cmdataobj.optJSONArray(String.valueOf(mapnum));
				if(maparr == null || maparr.optInt(0) == 1){
					BACException.throwInstance("����Ҫ����");
				} else 
				if(maparr.optInt(0) != -1){
					BACException.throwInstance("��ͼͨ�غ�ſ�����");
				}
				long currdatelong = MyTools.getCurrentDateLong();
				if(MyTools.getCurrentDateLong(maparr.optLong(1)) == currdatelong){//�������ʱ���ڽ��죬���ʾ���������ù�
					BACException.throwInstance("���������ù�");
				}
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACCOPYMAP_RESETMAP);
				FactionBAC.getInstance().changeMoney(dbHelper, factionid, -mapRs.getInt("resetmoney"), gl);
				maparr.put(0, 1);
				maparr.put(1, currdatelong);
				if(maparr.length() >= 3){
					maparr.remove(2);//���ʣ��Ѫ��
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add("cmdata", cmdataobj.toString());
				FactionBAC.getInstance().update(dbHelper, factionid, sqlStr);
				
				JSONArray pusharr = new JSONArray();
				pusharr.add(playerid);
				pusharr.add(mapnum);//��ͼ���
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACCOPYMAP_RESETMAP, pusharr.toString(), factionid, playerid);
				
				gl.addRemark("���õ�ͼ��"+GameLog.formatNameID(mapRs.getString("name"), mapnum))
				.save();
				return new ReturnValue(true);		
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	private static FacCopymapBAC instance = new FacCopymapBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FacCopymapBAC getInstance(){
		return instance;
	}
}
