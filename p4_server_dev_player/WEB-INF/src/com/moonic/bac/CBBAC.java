package com.moonic.bac;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

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
import com.moonic.cb.CBMgr;
import com.moonic.cb.City;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.mirror.Mirror;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.GamePushData;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.timertask.CBRefWorldLvTT;
import com.moonic.txtdata.CBDATA;
import com.moonic.util.BACException;
import com.moonic.util.ConfFile;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPRs;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ��ս
 * @author John
 */
public class CBBAC extends Mirror {
	public static final String tab_cb_city = "tab_cb_city";
	public static final String tab_cb_npc = "tab_cb_npc";
	public static final String tab_cb_killaward = "tab_cb_killaward";
	//public static final String tab_cb_relive = "tab_cb_relive"; TODO ɾ��
	public static final String tab_cb_declarewar = "tab_cb_declarewar";
	public static final String tab_cb_fatigue = "tab_cb_fatigue";
	
	public static CBMgr cbmgr = new CBMgr();
	
	/**
	 * ����
	 */
	public CBBAC() {
		super("tab_cb_city_stor", "serverid", null);//�˿���ֻ������Ϊ3�ĳ���
	}

	/**
	 * ��ս
	 */
	public ReturnValue declareWar(int playerid, int citynum, String teamidStr){
		DBHelper dbHelper = new DBHelper();
		try {
			if(System.currentTimeMillis()<MyTools.getCurrentDateLong()+CBDATA.declarewarstarttime || System.currentTimeMillis()>MyTools.getCurrentDateLong()+CBDATA.declarewarendtime){
				BACException.throwInstance("������սʱ����");
			}
			DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+citynum+" and display=1");
			if(!cityRs.exist()){
				BACException.throwInstance("Ŀ����в����� num="+citynum);
			}
			if(cityRs.getInt("citytype") == 2){
				BACException.throwInstance("�޷��Դ˳���ս");
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") == 0){
				BACException.throwInstance("ֻ�а����͸�����������ս");
			}
			if(cbmgr.declarefactionidList.contains(factionid)){
				BACException.throwInstance("ͬһʱ��ֻ�������һ������");
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			synchronized (LockStor.getLock(LockStor.CB_DECLAREWAR, citynum)) {
				DBPaRs facRs = FactionBAC.getInstance().getDataRs(factionid);
				if(cityRs.getInt("citytype") != 1 && facRs.getInt("occupyselfcity") == 0){
					DBPaRs cityOneRs = DBPool.getInst().pQueryA(tab_cb_city, "num=1");
					BACException.throwInstance("���ȹ���"+cityOneRs.getString("name")+ "��Ϊ���ǰ��ɵĵ�һ����ŵ�");
				}
				int occupy_fid = 0;//ռ���߰���ID
				int occupy_influencenum = 0;//ռ���������
				long nowarendtime = 0;//��ս����ʱ��
				int defnpclv = 0;//�س�NPC�ȼ�
				int defnpcam = 0;//�س�NPC����
				int mapkey = 0;//��սKEY
				if(cityRs.getInt("citytype") == 1){
					if(facRs.getInt("occupyselfcity") == 1){
						occupy_fid = factionid;	
					}
					defnpclv = cityRs.getInt("defnpclv");
					defnpcam = cityRs.getInt("defnpcamount");
					mapkey = -factionid;
				} else 
				if(cityRs.getInt("citytype") == 3){
					DBPsRs cityStorRs = getCityStorRs(dbHelper, cityRs);
					occupy_fid = cityStorRs.getInt("factionid");
					occupy_influencenum = cityStorRs.getInt("influencenum");
					nowarendtime = cityStorRs.getTime("nowarendtime");
					defnpclv = cityStorRs.getInt("defnpclv");
					defnpcam = cityStorRs.getInt("defnpcam");
					mapkey = citynum;
				}
				if(cbmgr.checkCityInWar(mapkey)){
					BACException.throwInstance("�˳�����ս����");
				}
				if(plaRs.getInt("lv") < cityRs.getInt("openlv")){
					BACException.throwInstance("�����ȼ����㣬�޷���ս");
				}
				if(occupy_fid == factionid){
					BACException.throwInstance("���ܶ��Լ��İ�����ս");
				}
				if(plaRs.getInt("lv") < cityRs.getInt("openlv")){
					BACException.throwInstance("�����ȼ����㣬�޷���ս");
				}
				if(!MyTools.checkSysTimeBeyondSqlDate(nowarendtime)){
					BACException.throwInstance("��ս�У��޷���ս");
				}
				
				DBPsRs cityStorRs2 = query(Conf.sid, "serverid="+Conf.sid+" and factionid="+factionid);
				int maxcity = FactionBAC.getInstance().getTechnologyFunc(facRs, 4, 0);
				if(cityStorRs2.count() >= maxcity){
					BACException.throwInstance("ռ��ǳ����Ѵ�����");
				}
				int declaream = facRs.getInt("declaream")+1;
				DBPaRs declarRs = DBPool.getInst().pQueryA(tab_cb_declarewar, "minam<="+declaream+" and maxam>="+declaream);
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_CB_DECLAREWAR, factionid);
				int needfacmoney = declarRs.getInt("needfacmoney");
				FactionBAC.getInstance().changeMoney(dbHelper, factionid, -needfacmoney, gl);
				ArrayList<TeamBox> teamlist1 = new ArrayList<TeamBox>();
				JSONArray teamidarr = new JSONArray(teamidStr);
				for(int i = 0; i < teamidarr.length(); i++){
					DBPsRs teamRs = CBTeamPoolBAC.getInstance().query(factionid, "factionid="+factionid+" and id="+teamidarr.optInt(i));
					if(!teamRs.next()){
						BACException.throwInstance("���鲻���� teamid="+teamidarr.optInt(i));
					}
					DBPaRs batRs = PlayerBAC.getInstance().getDataRs(teamRs.getInt("playerid"));
					TeamBox teambox = PartnerBAC.getInstance().getTeamBox(teamRs.getInt("playerid"), 0, new JSONArray(teamRs.getString("teamdata")));
					teambox.parameterarr = new JSONArray();
					teambox.parameterarr.add(0);
					teambox.parameterarr.add(batRs.getInt("lv"));
					teambox.parameterarr.add(batRs.getInt("num"));
					teamlist1.add(teambox);
					PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, teamRs.getInt("playerid"), PlaWelfareBAC.TYPE_CITYBATTLE, gl);
				}
				ArrayList<TeamBox> teamlist2 = new ArrayList<TeamBox>();
				DBPaRs npcRs = DBPool.getInst().pQueryA(tab_cb_npc, "npclv="+defnpclv+" and npctype=1");
				for(int i = 0; i < defnpcam; i++){
					teamlist2.add(createNPCTeamBox(cityRs.getString("name")+"����", npcRs));
				}
				String occupy_fname = null;
				if(occupy_fid > 0){
					occupy_fname = FactionBAC.getInstance().getStrValue(occupy_fid, "name");
					
					int[] memarr = FacMemBAC.getInstance().getFacMemIDs(occupy_fid);
					MailBAC.getInstance().sendModelMail(dbHelper, memarr, 13, new Object[]{cityRs.getString("name")}, new Object[]{cityRs.getString("name"), facRs.getString("name"), teamidarr.length()});
				} else 
				if(occupy_influencenum > 0){
					occupy_fname = CBDATA.getInfluenceName(occupy_influencenum);
				} else 
				{
					occupy_fname = cityRs.getString("name");
				}
				City city = cbmgr.startCB(mapkey, citynum, cityRs.getInt("citytype"), 0, factionid, facRs.getString("name"), teamlist1, occupy_influencenum, occupy_fid, occupy_fname, teamlist2);
				SqlString facSqlStr = new SqlString();
				facSqlStr.addChange("declaream", 1);
				FactionBAC.getInstance().update(dbHelper, factionid, facSqlStr);
				for(int i = 0; i < teamidarr.size(); i++){
					CBTeamPoolBAC.getInstance().delete(dbHelper, factionid, "factionid="+factionid+" and id="+teamidarr.optInt(i));
				}
				
				JSONArray pusharr = new JSONArray();
				pusharr.add(citynum);
				pusharr.add(city.getData2());
				PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_CB_DECLAREWAR, pusharr.toString());
				
				JSONArray pusharr2 = new JSONArray();
				pusharr2.add(citynum);
				pusharr.add(Const.teamA);
				pusharr2.add(teamidarr);
				pusharr2.add(city.batteamidarr1);
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_CB_DISPATCH, pusharr2.toString(), factionid, playerid);
				
				GamePushData.getInstance(6)
				.add(facRs.getString("name"))
				.add(cityRs.getString("name"))
				.sendToAllOL();
				
				gl.addRemark(GameLog.formatNameID(facRs.getString("name"), factionid) + teamidarr.length() + "����������� "+cityRs.getString("name")+" ��ս");
				gl.save();
				return new ReturnValue(true, city.batteamidarr1.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * NPC����
	 */
	public ReturnValue npcInvade(int citynum, int npcinfluence, int[] npcamount){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+citynum);
			if(!cityRs.exist()){
				BACException.throwInstance("Ŀ����в����� num="+citynum);
			}
			synchronized (LockStor.getLock(LockStor.CB_DECLAREWAR, citynum)) {
				int mapkey = citynum;
				if(cbmgr.checkCityInWar(mapkey)){
					BACException.throwInstance("�˳�����ս����");
				}
				String influenceName = CBDATA.getInfluenceName(npcinfluence);
				DBPsRs cityStorRs = getCityStorRs(dbHelper, cityRs);
				ArrayList<TeamBox> teamlist1 = new ArrayList<TeamBox>();
				DBPaRs npcRs1 = DBPool.getInst().pQueryA(tab_cb_npc, "npclv="+Conf.worldLevel+" and npctype=1");
				for(int i = 0; i < npcamount[0]; i++){
					teamlist1.add(createNPCTeamBox(influenceName+"����", npcRs1));
				}
				DBPaRs npcRs2 = DBPool.getInst().pQueryA(tab_cb_npc, "npclv="+Conf.worldLevel+" and npctype=2");
				for(int i = 0; i < npcamount[1]; i++){
					teamlist1.add(createNPCTeamBox(influenceName+"����", npcRs2));
				}
				ArrayList<TeamBox> teamlist2 = new ArrayList<TeamBox>();
				DBPaRs npcRs = DBPool.getInst().pQueryA(tab_cb_npc, "npclv="+cityStorRs.getInt("defnpclv")+" and npctype=1");
				for(int i = 0; i < cityStorRs.getInt("defnpcam"); i++){
					teamlist2.add(createNPCTeamBox(cityRs.getString("name")+"����", npcRs));
				}
				int factionid = cityStorRs.getInt("factionid");
				int influencenum = cityStorRs.getInt("influencenum");
				String factionname = null;
				if(factionid > 0){
					factionname = FactionBAC.getInstance().getStrValue(factionid, "name");
					
					int[] memarr = FacMemBAC.getInstance().getFacMemIDs(factionid);
					MailBAC.getInstance().sendModelMail(dbHelper, memarr, 13, new Object[]{cityRs.getString("name")}, new Object[]{cityRs.getString("name"), influenceName, npcamount.length});
				} else 
				if(influencenum > 0){
					factionname = CBDATA.getInfluenceName(influencenum);
				} else 
				{
					factionname = cityRs.getString("name");
				}
				City city = cbmgr.startCB(mapkey, citynum, cityRs.getInt("citytype"), npcinfluence, 0, influenceName, teamlist1, influencenum, factionid, factionname, teamlist2);
				
				JSONArray pusharr = new JSONArray();
				pusharr.add(citynum);
				pusharr.add(city.getData2());
				PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_CB_DECLAREWAR, pusharr.toString());
				
				GamePushData.getInstance(6)
				.add(influenceName)
				.add(cityRs.getString("name"))
				.sendToAllOL();
				
				return new ReturnValue(true, "�����ɹ�");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����NPCTeamBox
	 */
	public TeamBox createNPCTeamBox(String teamName, DBPaRs npcRs) throws Exception {
		return createNPCTeamBox_2(teamName, npcRs, createNPCTeamBox_1(npcRs));
	}
	
	public int[][] createNPCTeamBox_1(DBPaRs npcRs) throws Exception {
		int[][] parnumarr = Tools.splitStrToIntArr2(npcRs.getString("parnum"), "|", ",");
		int[][] ran_parnumarr = new int[2][3];
		int ran = MyTools.getRandom(2, 3);//ǰ������
		for(int m = 0; m < 5; m++){
			int l_ind = 0;
			int pos = m;//վλ
			if(m >= ran){//����
				l_ind = 1;
				pos = m-ran;
			}
			int r_ind = MyTools.getRandom(0, parnumarr[l_ind].length-1);
			ran_parnumarr[l_ind][pos] = parnumarr[l_ind][r_ind];
			parnumarr[l_ind] = Tools.removeOneFromIntArr(parnumarr[l_ind], r_ind);
		}
		//System.out.println(new JSONArray(ran_parnumarr[0])+" "+new JSONArray(ran_parnumarr[1]));
		return ran_parnumarr;
	}
	
	public TeamBox createNPCTeamBox_2(String teamName, DBPaRs npcRs, int[][] ran_parnumarr) throws Exception {
		JSONObject fetter_partner = new JSONObject();
		for(int i = 0; i < ran_parnumarr.length; i++){
			for(int j = 0; j < ran_parnumarr[i].length; j++){
				fetter_partner.put(String.valueOf(ran_parnumarr[i][j]), npcRs.getInt("parstar"));
			}
		}
		SpriteBox[][] spriteboxarr = new SpriteBox[2][3];
		for(int m = 0; m < ran_parnumarr.length; m++){
			for(int n = 0; n < ran_parnumarr[m].length; n++){
				if(ran_parnumarr[m][n] != 0){
					int num = ran_parnumarr[m][n];
					DBPaRs partnerRs = DBPool.getInst().pQueryA(PartnerBAC.tab_partner, "num="+num);
					int star = npcRs.getInt("parstar");
					int lv = npcRs.getInt("npclv");
					int phase = npcRs.getInt("parquality");
					int[] orbnumarr = PartnerBAC.getInstance().converOrbStateToNum(phase, partnerRs.getString("upphasenum"), npcRs.getString("parorb"));
					int[][] equiparr = PartnerBAC.getInstance().converEquipStateToData(npcRs.getString("parequip"));
					int[] skilllvarr = Tools.splitStrToIntArr(npcRs.getString("skilllv"), ",");
					SpriteBox spritebox = PartnerBAC.getInstance().getSpriteBox(0, 0, num, star, lv, phase, orbnumarr, equiparr, skilllvarr, fetter_partner, null);
					spriteboxarr[m][n] = spritebox;
				}
			}
		}
		TeamBox teambox = PartnerBAC.getInstance().getTeamBox(0, teamName, 0, 1, spriteboxarr);
		if(teambox.sprites.size() < 5){
			System.out.println("----------ERROR:NPC���ӻ������������("+teamName+")--"+MyTools.getTimeStr());
		}
		teambox.parameterarr = new JSONArray();
		teambox.parameterarr.add(npcRs.getInt("npctype"));
		teambox.parameterarr.add(npcRs.getInt("npclv"));
		return teambox;
	}
	
	/**
	 * ��ȡ���е�RS
	 */
	public DBPsRs getCityStorRs(DBHelper dbHelper, DBPRs cityRs) throws Exception {
		synchronized (LockStor.getLock(LockStor.CB_DECLAREWAR, cityRs.getInt("num"))) {
			if(cityRs.getInt("citytype") != 3){
				BACException.throwInstance("�ǹ������У��޷�ִ�д˲���");
			}
			String where = "serverid="+Conf.sid+" and citynum="+cityRs.getInt("num");
			DBPsRs cityStorRs = query(Conf.sid, where);
			if(!cityStorRs.next()){
				SqlString sqlStr = new SqlString();
				sqlStr.add("citynum", cityRs.getInt("num"));
				sqlStr.add("serverid", Conf.sid);
				sqlStr.add("defnpclv", cityRs.getInt("defnpclv"));
				sqlStr.add("defnpcam", cityRs.getInt("defnpcamount"));
				insert(dbHelper, Conf.sid, sqlStr);
				cityStorRs = query(Conf.sid, where);
				cityStorRs.next();
			}
			return cityStorRs;
		}
	}
	
	/**
	 * ��ӷ�������
	 */
	public ReturnValue createTeamToPool(int playerid, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factoinid = plafacRs.getInt("factionid");
			if(factoinid == 0){
				BACException.throwInstance("��δ�������");
			}
			JSONArray posarr = new JSONArray(posarrStr);
			cbmgr.checkJoin(playerid, posarr);//������
			checkPartnerInPool(playerid, factoinid, posarr);//����ؼ��
			SqlString sqlStr = new SqlString();
			sqlStr.add("playerid", playerid);
			sqlStr.add("factionid", factoinid);
			sqlStr.add("teamdata", posarr.toString());
			int teamid = CBTeamPoolBAC.getInstance().insert(dbHelper, factoinid, sqlStr);
			return new ReturnValue(true, String.valueOf(teamid));
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ȡ��������еı��
	 */
	public ReturnValue cancelTeamFromPool(int playerid, int teamid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factoinid = plafacRs.getInt("factionid");
			if(factoinid == 0){
				BACException.throwInstance("��δ�������");
			}
			DBPsRs teamRs = CBTeamPoolBAC.getInstance().query(factoinid, "factionid="+factoinid+" and id="+teamid);
			if(!teamRs.next()){
				BACException.throwInstance("����δ�ҵ� teamid="+teamid);
			}
			if(teamRs.getInt("playerid") != playerid){
				BACException.throwInstance("�ⲻ����Ķ��� playerid="+playerid);
			}
			CBTeamPoolBAC.getInstance().delete(dbHelper, factoinid, "factionid="+factoinid+" and id="+teamid);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ������еĶ�������
	 */
	public ReturnValue getTeamPoolData(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factoinid = plafacRs.getInt("factionid");
			if(factoinid == 0){
				BACException.throwInstance("��δ�������");
			}
			DBPsRs teamRs = CBTeamPoolBAC.getInstance().query(factoinid, "factionid="+factoinid);
			JSONArray returnarr = new JSONArray();
			while(teamRs.next()){
				JSONArray arr = new JSONArray();
				arr.add(teamRs.getInt("id"));
				arr.add(teamRs.getInt("playerid"));
				arr.add(PartnerBAC.getInstance().getPlayerBattlePower(teamRs.getInt("playerid"), new JSONArray(teamRs.getString("teamdata")), teamRs.getInt("battlepower")));
				returnarr.add(arr);
			}
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ����ս״̬
	 */
	public ReturnValue getPartnerState(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray partnerarr = new JSONArray();
			DBPsRs partnerStorRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid);
			while(partnerStorRs.next()){
				partnerarr.add(partnerStorRs.getInt("id"));
			}
			JSONArray battlearr = cbmgr.getPartnerWarState(partnerarr);
			JSONArray poolarr = null;
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid != 0){
				DBPsRs teampoolRs = CBTeamPoolBAC.getInstance().query(factionid, "factionid="+factionid+" and playerid="+playerid);
				if(teampoolRs.have()){
					poolarr = new JSONArray();
					while(teampoolRs.next()){
						JSONArray arr = new JSONArray();
						arr.add(teampoolRs.getInt("id"));
						arr.add(new JSONArray(teampoolRs.getString("teamdata")));
						poolarr.add(arr);
					}
				}
			}
			JSONArray returnarr = new JSONArray();
			returnarr.add(battlearr);//ս���л��
			returnarr.add(poolarr);//����صĶ���
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������Ƿ��ڶ������
	 */
	public void checkPartnerInPool(int playerid, int factionid, JSONArray posarr) throws Exception {
		if(factionid != 0){
			JSONArray poolarr = new JSONArray();
			DBPsRs teampoolRs = CBTeamPoolBAC.getInstance().query(factionid, "factionid="+factionid+" and playerid="+playerid);
			if(teampoolRs.have()){
				while(teampoolRs.next()){
					MyTools.combJsonarr(poolarr, new JSONArray(teampoolRs.getString("teamdata")));
				}
			}
			for(int i = 0; i < posarr.length(); i++){
				if(posarr.optInt(i) != 0 && poolarr.contains(posarr.optInt(i))){
					BACException.throwInstance("���("+posarr.optInt(i)+")�Ѿ��ڶ������");
				}
			}
		}
	}
	
	/**
	 * ��ȡָ������ս������
	 */
	public ReturnValue getCityBattleData(int playerid, int mapkey){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray dataarr = cbmgr.getCityData(mapkey);
			return new ReturnValue(true, dataarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ս
	 */
	public ReturnValue joinWar(int playerid, int mapkey, byte teamType, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			if(System.currentTimeMillis()<MyTools.getCurrentDateLong()+CBDATA.joinwarstarttime || System.currentTimeMillis()>MyTools.getCurrentDateLong()+CBDATA.joinwarendtime){
				BACException.throwInstance("���ڲ�սʱ����");
			}
			if(teamType != 0 && teamType != 1){
				BACException.throwInstance("��������teamType="+teamType);
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			JSONArray posarr = new JSONArray(posarrStr);
			cbmgr.checkJoin(playerid, posarr);//������
			checkPartnerInPool(playerid, factionid, posarr);//����ؼ��
			int myTeamtype = cbmgr.getFactionTeamType(mapkey, factionid);
			if(myTeamtype != -1 && myTeamtype != teamType){
				BACException.throwInstance("�޷�����Է�������ս");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_CB_JOINWAR);
			if(myTeamtype == -1){
				PlaRoleBAC.getInstance().subValue(dbHelper, playerid, "energy", CBDATA.assist, gl, "����");
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			TeamBox teambox = PartnerBAC.getInstance().getTeamBox(playerid, teamType, new JSONArray(posarrStr));
			teambox.parameterarr = new JSONArray();
			teambox.parameterarr.add(0);
			teambox.parameterarr.add(plaRs.getInt("lv"));
			teambox.parameterarr.add(plaRs.getInt("num"));
			int teamid = cbmgr.join(mapkey, teamType, teambox);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_CITYBATTLE, gl);
			
			City city = cbmgr.getCity(mapkey);
			gl.addRemark("���У�"+city.name+" �μ�"+(teamType==1?"��["+GameLog.formatNameID(city.faction2.factionname, city.faction2.factionid)+"]":"��["+GameLog.formatNameID(city.faction1.factionname, city.faction1.factionid)+"]")+"�� myfactionid="+factionid+" ������ɣ�"+teambox.getTeamDataStr());
			gl.save();
			return new ReturnValue(true, String.valueOf(teamid));
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����ս���
	 */
	public ReturnValue relivePartner(int playerid, String partneridarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factoinid = plafacRs.getInt("factionid");
			if(factoinid == 0){
				BACException.throwInstance("��δ�������");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_CB_RELIVEPARTNER);
			cbmgr.relivePartner(dbHelper, playerid, partneridarrStr, gl);
			
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �Ƿ��ж����ڹ�ս��
	 */
	public boolean isInCb(int playerid){
		return cbmgr.playermap.containsKey(playerid);
	}
	
	/**
	 * ��ȡ��������
	 */
	public ReturnValue getBattlerList(int playerid, int mapkey, byte teamType){
		try {
			if(teamType != Const.teamA && teamType != Const.teamB){
				BACException.throwInstance("�������ʹ��� teamType="+teamType);
			}
			JSONArray rankingarr = cbmgr.getBattlerList(mapkey, teamType);
			return new ReturnValue(true, rankingarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ��ɱ����
	 */
	public ReturnValue getKillRanking(int playerid, int mapkey){
		try {
			JSONArray rankingarr = cbmgr.getKillRanking(playerid, mapkey);
			return new ReturnValue(true, rankingarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ������ϯ
	 */
	public ReturnValue contendLeader(int playerid, int citynum, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			if(System.currentTimeMillis()<MyTools.getCurrentDateLong()+CBDATA.leaderstarttime || System.currentTimeMillis()>MyTools.getCurrentDateLong()+CBDATA.leaderendtime){
				BACException.throwInstance("����������ϯʱ����");
			}
			DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+citynum+" and display=1");
			if(!cityRs.exist()){
				BACException.throwInstance("Ŀ����в����� num="+citynum);
			}
			if(cityRs.getInt("citytype") != 3){
				BACException.throwInstance("�˳�����̫��ְλ");
			}
			DBPsRs cityStorRs = getCityStorRs(dbHelper, cityRs);
			int leaderid = cityStorRs.getInt("leaderid");
			if(leaderid == playerid){
				BACException.throwInstance("���Ѿ��Ǵ˳ǵ���ϯ");
			}
			DBPsRs cityStorRs2 = query(Conf.sid, "serverid="+Conf.sid+" and leaderid="+playerid);
			if(cityStorRs2.have()){
				BACException.throwInstance("���Ѿ��������ǵ���ϯ");
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			if(!MyTools.checkSysTimeBeyondSqlDate(plafacRs.getTime("leadercdendtime"))){
				BACException.throwInstance("��սʱ�����޷���ս");
			}
			synchronized (LockStor.getLock(LockStor.CB_LEADER, citynum)) {
				JSONArray posarr = new JSONArray(posarrStr);
				PartnerBAC.getInstance().checkPosarr(playerid, posarr, 0, 1);//��������Ƿ�Ϸ�
				TeamBox teambox1 = PartnerBAC.getInstance().getTeamBox(playerid, 0, posarr);
				//System.out.println("teambox1.getTeamDataStr():"+teambox1.getTeamDataStr()); 
				TeamBox teambox2 = null;
				if(leaderid == 0){
					DBPaRs npcRs = DBPool.getInst().pQueryA(tab_cb_npc, "npclv="+cityStorRs.getInt("defnpclv")+" and npctype=1");
					teambox2 = createNPCTeamBox(cityRs.getString("name")+"��ϯ", npcRs);
				} else {
					JSONArray leaderposarr = new JSONArray(cityStorRs.getString("leaderposarr"));
					teambox2 = PartnerBAC.getInstance().getTeamBox(leaderid, 1, leaderposarr);
					//System.out.println("teambox2.getTeamDataStr():"+teambox2.getTeamDataStr());
				}
				BattleBox battlebox = new BattleBox();
				battlebox.teamArr[0].add(teambox1);
				battlebox.teamArr[1].add(teambox2);
				BattleManager.createPVPBattle(battlebox);
				if(battlebox.winTeam == Const.teamA){
					DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
					SqlString sqlStr = new SqlString();
					sqlStr.add("leaderid", playerid);
					sqlStr.add("leadername", plaRs.getString("name"));
					sqlStr.add("leaderposarr", posarr.toString());
					sqlStr.addDateTime("giveupleadercdtime", MyTools.getTimeStr(System.currentTimeMillis()+CBDATA.leadergiveupspacetimelen*MyTools.long_minu));
					update(dbHelper, Conf.sid, sqlStr, "serverid="+Conf.sid+" and id="+cityStorRs.getInt("id"));
					JSONArray pusharr = new JSONArray();//��������
					pusharr.add(citynum);
					pusharr.add(playerid);
					PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_CB_CHANGELEADER, pusharr.toString());
					
					GamePushData.getInstance(12)
					.add(plaRs.getString("name"))
					.add(leaderid!=0?PlayerBAC.getInstance().getStrValue(leaderid, "name"):"����")
					.add(cityRs.getString("name"))
					.sendToAllOL();
				} else {
					SqlString sqlStr = new SqlString();
					sqlStr.addDateTime("leadercdendtime", MyTools.getTimeStr(System.currentTimeMillis()+CBDATA.leaderspacetimelen*MyTools.long_minu));
					PlaFacBAC.getInstance().update(dbHelper, playerid, sqlStr);
				}
				
				//BattleReplayBAC.getInstance().saveReplay(battlebox.battleId, battlebox.replayData.toString(), 1, 3);
				//System.out.println("battlebox.battleId:"+battlebox.battleId);
				
				CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 23);
				
				GameLog.getInst(playerid, GameServlet.ACT_CB_CONTENDLEADER)
				.addRemark("����̫�س��У�"+cityRs.getString("name")+" ��������"+(battlebox.winTeam==Const.teamA?"�ɹ�":"ʧ��"))
				.save();
				
				return new ReturnValue(true, battlebox.replayData.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������ϯ��������
	 */
	public ReturnValue setLeaderDefForm(int playerid, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid+" and leaderid="+playerid);
			if(!cityStorRs.next()){
				BACException.throwInstance("�㲻����ϯ");
			}
			synchronized (LockStor.getLock(LockStor.CB_LEADER, cityStorRs.getInt("citynum"))) {
				if(cityStorRs.getString("leaderposarr").equals(posarrStr)){
					BACException.throwInstance("�����ޱ仯");
				}
				JSONArray posarr = new JSONArray(posarrStr);
				PartnerBAC.getInstance().checkPosarr(playerid, posarr, 0, 1);//��������Ƿ�Ϸ�
				SqlString sqlStr = new SqlString();
				sqlStr.add("leaderposarr", posarr.toString());
				update(dbHelper, Conf.sid, sqlStr, "serverid="+Conf.sid+" and id="+cityStorRs.getInt("id"));
				
				return new ReturnValue(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������ϯ
	 */
	public ReturnValue giveupLeader(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid+" and leaderid="+playerid);
			if(!cityStorRs.next()){
				BACException.throwInstance("�㲻����ϯ");
			}
			if(!MyTools.checkSysTimeBeyondSqlDate(cityStorRs.getTime("giveupleadercdtime"))){
				BACException.throwInstance(MyTools.getTimeStr(cityStorRs.getTime("giveupleadercdtime"))+"֮����ܷ�����ϯ");
			}
			synchronized (LockStor.getLock(LockStor.CB_LEADER, cityStorRs.getInt("citynum"))) {
				SqlString sqlStr = new SqlString();
				sqlStr.add("leaderid", null);
				sqlStr.add("leadername", null);
				sqlStr.add("leaderposarr", null);
				sqlStr.addDateTime("giveupleadercdtime", null);
				update(dbHelper, Conf.sid, sqlStr, "serverid="+Conf.sid+" and id="+cityStorRs.getInt("id"));
				
				JSONArray pusharr = new JSONArray();
				pusharr.add(cityStorRs.getInt("citynum"));
				pusharr.add(0);
				PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_CB_CHANGELEADER, pusharr.toString());
				
				GameLog.getInst(playerid, GameServlet.ACT_CB_GIVEUP_LEADER)
				.addRemark("���б�ţ�"+cityStorRs.getInt("citynum"))
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
	
	/**
	 * ��ȡ���ڰ���ռ���������
	 */
	public int getHaveCityCount(int playerid) throws Exception {
		DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
		int factionid = plafacRs.getInt("factionid");
		if(factionid == 0){
			return 0;
		}
		int amount = 0;
		DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid+" and factionid="+factionid);
		if(cityStorRs.have()){
			amount = cityStorRs.count()+1;
		} else {
			amount = FactionBAC.getInstance().getDataRs(factionid).getInt("occupyselfcity");
		}
		return amount;
	}
	
	/**
	 * ��ȡ��ս����
	 */
	public ReturnValue getData(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid);
			JSONArray pubarr = new JSONArray();
			while(cityStorRs.next()){
				JSONArray arr = new JSONArray();
				arr.add(cityStorRs.getInt("citynum"));
				arr.add(cityStorRs.getInt("factionid"));
				arr.add(cityStorRs.getInt("defnpclv"));
				arr.add(cityStorRs.getInt("defnpcam"));
				arr.add(cityStorRs.getTime("nowarendtime"));//��սʱ��
				arr.add(cityStorRs.getInt("leaderid"));
				arr.add(cityStorRs.getString("leaderposarr")!=null?new JSONArray(cityStorRs.getString("leaderposarr")):null);//��ϯ��������
				arr.add(cityStorRs.getTime("giveupleadercdtime"));//������ϯ��ȴʱ��
				arr.add(cbmgr.getCityData2(cityStorRs.getInt("citynum")));//�ǳ�ս����Ϣ
				arr.add(cityStorRs.getString("factionname"));//ռ�������
				arr.add(cityStorRs.getInt("influencenum"));//ռ���������
				arr.add(cityStorRs.getString("leadername"));
				pubarr.add(arr);
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			JSONArray selfarr = null;
			if(plafacRs.getInt("factionid") != 0){
				selfarr = cbmgr.getCityData2(-plafacRs.getInt("factionid"));
			}
			JSONArray returnarr = new JSONArray();
			returnarr.add(pubarr);
			returnarr.add(selfarr);
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ��ϯ����
	 */
	public ReturnValue getLeaderData(int playerid, int citynum){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+citynum);
			DBPsRs cityStorRs = getCityStorRs(dbHelper, cityRs);
			int leaderid = cityStorRs.getInt("leaderid");
			if(leaderid == 0){
				BACException.throwInstance("�˳�������ҵ�����ϯ");
			}
			JSONArray posarr = new JSONArray(cityStorRs.getString("leaderposarr"));
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(leaderid);
			JSONArray plaarr = new JSONArray();
			plaarr.add(plaRs.getInt("num"));
			plaarr.add(plaRs.getString("name"));
			plaarr.add(plaRs.getInt("lv"));
			plaarr.add(PartnerBAC.getInstance().getPlayerBattlePower(leaderid, posarr, cityStorRs.getInt("leaderbattlepower")));
			JSONArray partnerarr = PartnerBAC.getInstance().getPartnerDataByPosarr(leaderid, posarr);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(plaarr);//��������
			returnarr.add(partnerarr);//�������
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ɳ�����ض���
	 */
	public ReturnValue dispatchTeampool(int playerid, int citynum, String teamidStr){
		DBHelper dbHelper = new DBHelper();
		try {
			if(System.currentTimeMillis()<MyTools.getCurrentDateLong()+CBDATA.joinwarstarttime || System.currentTimeMillis()>MyTools.getCurrentDateLong()+CBDATA.joinwarendtime){
				BACException.throwInstance("���ڲ�սʱ����");
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") == 0){
				BACException.throwInstance("ֻ�а����͸��������ɳ�����");
			}
			byte teamtype = cbmgr.getFactionTeamType(citynum, factionid);
			if(teamtype == -1){
				BACException.throwInstance("�����ڰ���û���ڴ˳��в�ս");
			}
			JSONArray teamidarr = new JSONArray(teamidStr);
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_CB_DISPATCH_TEAMPOOL);
			ArrayList<TeamBox> teamlist = new ArrayList<TeamBox>();
			for(int i = 0; i < teamidarr.length(); i++){
				DBPsRs teamRs = CBTeamPoolBAC.getInstance().query(factionid, "factionid="+factionid+" and id="+teamidarr.optInt(i));
				if(!teamRs.next()){
					BACException.throwInstance("���鲻���� teamid="+teamidarr.optInt(i));
				}
				DBPaRs batRs = PlayerBAC.getInstance().getDataRs(teamRs.getInt("playerid"));
				TeamBox teambox = PartnerBAC.getInstance().getTeamBox(teamRs.getInt("playerid"), 0, new JSONArray(teamRs.getString("teamdata")));
				teambox.parameterarr = new JSONArray();
				teambox.parameterarr.add(0);
				teambox.parameterarr.add(batRs.getInt("lv"));
				teambox.parameterarr.add(batRs.getInt("num"));
				teamlist.add(teambox);
				PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, teamRs.getInt("playerid"), PlaWelfareBAC.TYPE_CITYBATTLE, gl);
			}
			for(int i = 0; i < teamidarr.size(); i++){
				CBTeamPoolBAC.getInstance().delete(dbHelper, factionid, "factionid="+factionid+" and id="+teamidarr.optInt(i));
			}
			JSONArray batteamidarr = new JSONArray();
			for(int i = 0; i < teamlist.size(); i++){
				int batteamid = cbmgr.join(citynum, teamtype, teamlist.get(i));
				batteamidarr.add(batteamid);
			}
			JSONArray pusharr = new JSONArray();
			pusharr.add(citynum);
			pusharr.add(teamtype);
			pusharr.add(teamidarr);
			pusharr.add(batteamidarr);
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_CB_DISPATCH, pusharr.toString(), factionid, playerid);
			
			City city = cbmgr.cbmap.get(citynum);
			StringBuffer remark = new StringBuffer();
			remark.append(city == null ? "" : city.name)
			.append(teamtype == 1 ? "�ط�" : "����")
			.append("����ս��������")
			.append(teamidarr.length());
			gl.addRemark(remark)
			.save();
			return new ReturnValue(true, batteamidarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ɢ���ɷ����ǳ�
	 */
	public void giveupCity(DBHelper dbHelper, int factionid) throws Exception {
		DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid+" and factionid="+factionid);
		if(!cityStorRs.have()){
			return ;
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("factionid", 0);
		sqlStr.add("factionname", null);
		update(dbHelper, Conf.sid, sqlStr, "serverid="+Conf.sid+" and factionid="+factionid);
	}
	
	/**
	 * ���������ʽ�
	 */
	public ReturnValue outputFacMoney(){
		DBHelper dbHelper = new DBHelper();
		try {
			if(System.currentTimeMillis()<MyTools.getCurrentDateLong()+CBDATA.cityoutputstarttime || System.currentTimeMillis()>MyTools.getCurrentDateLong()+CBDATA.cityoutputendtime){
				BACException.throwInstance("���ڲ����ʽ�ʱ����");
			}
			DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid+" and factionid!=0");
			HashMap<Integer, Integer> counterMap = new HashMap<Integer, Integer>();
			while(cityStorRs.next()){
				int factionid = cityStorRs.getInt("factionid");
				DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+cityStorRs.getInt("citynum"));
				DBPaRs facRs = FactionBAC.getInstance().getDataRs(factionid);
				int addpercent = FactionBAC.getInstance().getTechnologyFunc(facRs, 4, 1);
				int facmoney = cityRs.getInt("facmoney")*(100+addpercent)/100;
				
				int pos = cityRs.getInt("pos");
				int[] ids = FacMemBAC.getInstance().getFacMgrIDs(factionid, 0);
				MailBAC.getInstance().sendModelMail(dbHelper, ids, 12, new Object[]{cityRs.getString("name"), pos}, new Object[]{cityRs.getString("name"), facmoney});
				
				GameLog gl = GameLog.getInst(0, GameServlet.ACT_CB_ISSUE_PUB_AWARD, factionid);
				FactionBAC.getInstance().changeMoney(dbHelper, factionid, facmoney, gl);
				int current = counterMap.get(factionid) == null ? 0 : counterMap.get(factionid);
				counterMap.put(factionid, current + 1);
				
				if (cityStorRs.getInt("citynum") != 1) {
					int[] facMemIds = FacMemBAC.getInstance().getFacMemIDs(factionid);
					for (int i = 0; i < facMemIds.length; i++) {
						CustomActivityBAC.getInstance().updateProcess(dbHelper, facMemIds[i], 31, pos);
					}
				}
				
				StringBuffer remark = new StringBuffer();
				remark.append(GameLog.formatNameID(facRs.getString("name"), factionid))
				.append("�����ڳǣ�")
				.append(GameLog.formatNameID(cityRs.getString("name"), cityStorRs.getInt("citynum")))
				.append("���Ƽ��ȼ���")
				.append(FactionBAC.getInstance().getTechnologyLv(facRs, 4));
				
				gl.addRemark(remark)
				.save();
			}
			Iterator<Entry<Integer, Integer>> it = counterMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Integer, Integer> entry = it.next();
				int factionId = entry.getKey();
				int count = entry.getValue();
				int[] facMemIds = FacMemBAC.getInstance().getFacMemIDs(factionId);
				for (int i = 0; i < facMemIds.length; i++) {
					CustomActivityBAC.getInstance().updateProcess(dbHelper, facMemIds[i], 30, count);
				}
				PushData.getInstance().sendPlaToSome(SocketServer.ACT_CB_FACTION_AWARD, String.valueOf(count), facMemIds);
			}
			
			return new ReturnValue(true, "���������ʽ����");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ָ�NPC
	 */
	public ReturnValue recoverNPC(){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid);
			while(cityStorRs.next()){
				DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+cityStorRs.getInt("citynum"));
				int mapkey = cityStorRs.getInt("citynum");
				if(cbmgr.checkCityInWar(mapkey)){//ս���г��в��ָ�NPC
					continue;
				}
				if(cityStorRs.getInt("defnpcam") >= cityRs.getInt("defnpcamount")){
					continue;
				}
				SqlString sqlStr = new SqlString();
				sqlStr.addChange("defnpcam", CBDATA.npcrecoverspeed);
				update(dbHelper, Conf.sid, sqlStr, "serverid="+Conf.sid+" and id="+cityStorRs.getInt("id"));
			}
			return new ReturnValue(true, "�ָ�NPC���");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������ϯ����
	 */
	public ReturnValue issueLeaderAward(){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs cityStorRs = query(Conf.sid, "serverid="+Conf.sid+" and leaderid!=0");
			while(cityStorRs.next()){
				int leaderid = cityStorRs.getInt("leaderid");
				int citynum = cityStorRs.getInt("citynum");
				DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num="+citynum);
				int pos = cityRs.getInt("pos");
				MailBAC.getInstance().sendSysMail(dbHelper, leaderid, "��ϯ����|"+pos, "��ϯ����", cityRs.getString("leaderaward"), 0);
				CustomActivityBAC.getInstance().updateProcess(dbHelper, leaderid, 28);
				if (citynum != 1) {
					CustomActivityBAC.getInstance().updateProcess(dbHelper, leaderid, 29, pos);
				}
			}
			return new ReturnValue(true, "�������");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �������г��н���
	 */
	public ReturnValue issueSelfCityAward(){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs factionStorRs = ServerFacBAC.getInstance().query(Conf.sid, "serverid="+Conf.sid+" and occupyselfcity=1");
			while(factionStorRs.next()){
				int factionid = factionStorRs.getInt("id");
				DBPaRs cityRs = DBPool.getInst().pQueryA(tab_cb_city, "num=1");
				int addpercent = FactionBAC.getInstance().getTechnologyFunc(factionStorRs, 4, 1);
				int facmoney = cityRs.getInt("facmoney")*(100+addpercent)/100;
				
				int[] ids = FacMemBAC.getInstance().getFacMgrIDs(factionid, 0);
				MailBAC.getInstance().sendModelMail(dbHelper, ids, 11, null, new Object[]{facmoney});
				
				GameLog gl = GameLog.getInst(0, GameServlet.ACT_CB_ISSUE_SELF_AWARD, factionid);
				FactionBAC.getInstance().changeMoney(dbHelper, factionid, facmoney, gl);
				gl.save();
			}
			return new ReturnValue(true, "�������");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������ȼ�
	 */
	public ReturnValue updateWorldLevel(){
		DBHelper dbHelper = new DBHelper();
		try {
			int newlv = 0;
			int amount = 0;
			ResultSet plasRs = dbHelper.query("tab_player", "lv", "serverid="+Conf.sid+" and isrobot=0", "lv desc", 100);
			while(plasRs.next()){
				newlv += plasRs.getInt("lv");
				amount++;
			}
			if(amount > 0){
				newlv = newlv / amount;
				if(newlv <= 11){
					newlv = 1;
				}
			}
			if(newlv <= Conf.worldLevel){
				BACException.throwInstance("����ȼ��ޱ仯��������� ��ǰ����ȼ���"+Conf.worldLevel);
			}
			Conf.worldLevel = newlv;
			ConfFile.updateFileValue(CBRefWorldLvTT.WORLDLEVEL, String.valueOf(Conf.worldLevel));
			
			GameLog.getInst(0, GameServlet.ACT_CB_UPDATE_WORLDLEVEL)
			.addRemark("����ȼ�����Ϊ��"+Conf.worldLevel)
			.save();
			return new ReturnValue(true, "������ɣ��µ�����ȼ�="+Conf.worldLevel);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//-----------------��̬��--------------------
	
	private static CBBAC instance = new CBBAC();

	public static CBBAC getInstance() {
		return instance;
	}
}
