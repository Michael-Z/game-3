package com.moonic.bac;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.mirror.MirrorOne;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPRs;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ����
 * @author John
 */
public class FactionBAC extends MirrorOne {
	public static final String tab_faction_lv = "tab_faction_lv";
	public static final String tab_faction_technology = "tab_faction_technology";
	public static final String tab_faction_welfare = "tab_faction_welfare";
	public static final String tab_faction_worship = "tab_faction_worship";
	
	/**
	 * ����
	 */
	public FactionBAC(){
		super("tab_faction_stor", "id");
		haveServerWhere = true;
	}
	
	/**
	 * ��������
	 */
	public ReturnValue create(int playerid, String name){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_NAME)) {
				MyTools.checkNoCharEx(name, '#');
				if(name.toLowerCase().equals("null")){
					BACException.throwInstance("���ֲ����ã�����ĺ�����");
				}
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				if(plafacRs.getInt("factionid") != 0){
					BACException.throwInstance("�����ڰ����У����ɴ�������");
				}
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				DBPsRs facRs = ServerFacBAC.getInstance().query(Conf.sid, "serverid="+Conf.sid+" and name='"+name+"'");
				if(facRs.next()){
					BACException.throwInstance("�������Ѵ���");
				}
				PlaFacBAC.getInstance().intoCheck(plafacRs);
				dbHelper.openConnection();
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_CREATE);
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, 100, gl);
				SqlString sqlStr = new SqlString();
				sqlStr.add("playerid", playerid);
				sqlStr.add("pname", plaRs.getString("name"));
				sqlStr.add("serverid", Conf.sid);
				sqlStr.add("name", name);
				sqlStr.add("lv", 1);
				sqlStr.add("money", 0);
				sqlStr.add("declaream", 0);
				sqlStr.addDateTime("resetdate", MyTools.getDaiylResetTime(5));
				sqlStr.add("technology", (new JSONObject()).toString());
				sqlStr.add("cmpassdata", (new JSONArray()).toString());
				sqlStr.add("cmdata", (new JSONObject()).toString());
				sqlStr.add("occupyselfcity", 0);
				int factionid = insertByAutoID(dbHelper, sqlStr);
				PlaFacBAC.getInstance().intoFaction(dbHelper, playerid, factionid, name, 2);
				PlaFacApplyBAC.getInstance().clearAllApply(dbHelper, playerid, 0);//֪ͨ�����������
				ServerFacBAC.getInstance().updateFactionRanking();
				
				gl.addObtain("�������ɣ�" + GameLog.formatNameID(name, factionid))
				.save();
				
				MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerid}, 3, new Object[]{name}, new Object[]{name});
				
				JSONArray returnarr = new JSONArray();
				returnarr.add(factionid);//����ID
				return new ReturnValue(true, returnarr.toString());
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����������
	 */
	public ReturnValue setJoinCond(int playerid, String joincond){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") == 0){
				BACException.throwInstance("Ȩ�޲���");
			}
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("joincond", joincond);
			update(dbHelper, factionid, sqlStr);
			
			JSONArray pusharr = new JSONArray();
			pusharr.add(playerid);
			pusharr.add(joincond);
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_JOINCOND, pusharr.toString(), factionid, playerid);
			
			GameLog.getInst(playerid, GameServlet.ACT_FACTION_UPD_INFO)
			.addRemark("�������������" + joincond)
			.save();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �������
	 */
	public ReturnValue join(int playerid, int factionid){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_MEMBER, factionid)) {
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				if(plafacRs.getInt("factionid") != 0){
					BACException.throwInstance("�����ڰ����У����ɼ�����������");
				}
				DBPaRs facRs = getDataRs(factionid);
				if(facRs.getInt("serverid")!=Conf.sid){
					BACException.throwInstance("���ɲ�����");
				}
				int curamount = FacMemBAC.getInstance().getAmount(factionid);
				int maxamount = getTechnologyFunc(facRs, 1, 0);
				if(curamount >= maxamount){
					BACException.throwInstance("�˰��������������޷�����");
				}
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				boolean needApply = false;
				int[][] joincond = Tools.splitStrToIntArr2(facRs.getString("joincond"), "|", ",");
				for(int i = 0; joincond != null && i < joincond.length; i++){
					if(joincond[i][0] == 1){// �ȼ�����
						if (joincond[i][1] > plaRs.getInt("lv")) {
							BACException.throwInstance("�ȼ�����");
						}
					} else if (joincond[i][0] == 4) {// �Ƿ���Ҫ����
						if (joincond[i][1] == 1) {
							needApply = true;
						}
					}
				}
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_JOIN);
				PlaFacBAC.getInstance().intoCheck(plafacRs);
				
				JSONArray returnarr = new JSONArray();
				if (needApply) {
					DBPsRs plaAppRs = PlaFacApplyBAC.getInstance().query(playerid, "playerid="+playerid+" and factionid="+factionid);
					if(plaAppRs.next()){
						BACException.throwInstance("����������˰��ɣ���ȴ��ظ�");
					}
					int applyAm = PlaFacApplyBAC.getInstance().getAmount(playerid);
					if(applyAm >= 3){
						BACException.throwInstance("�������������볷������������ٳ���");
					}
					int applyPlaAm = FacApplyBAC.getInstance().getAmount(factionid);
					if(applyPlaAm >= 30){
						BACException.throwInstance("�˰������������������볢��������������");
					}
					SqlString sqlStr = new SqlString();
					sqlStr.add("playerid", playerid);
					sqlStr.add("factionid", factionid);
					sqlStr.addDateTime("applytime", MyTools.getTimeStr());
					PlaFacApplyBAC.getInstance().insert(dbHelper, playerid, sqlStr);
					DBPsRs applyRs = PlaFacApplyBAC.getInstance().query(playerid, "playerid="+playerid+" and factionid="+factionid);
					applyRs.next();
					JSONArray jsonarr = PlaFacApplyBAC.getInstance().getApplyData(applyRs);
					PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_APPLY, jsonarr.toString(), factionid, 0);
				} else {
					returnarr = intoFaction(dbHelper, playerid, factionid, facRs, new JSONArray(), 0 , SocketServer.ACT_FACTION_JOIN);
				}
				
				gl.addRemark("������ɣ�" + GameLog.formatNameID(facRs.getString("name"), factionid))
				.save();
				return new ReturnValue(true, returnarr.toString());
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}

	/**
	 * �޸İ�ṫ��
	 */
	public ReturnValue updInfo(int playerid, String info){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") == 0){
				BACException.throwInstance("Ȩ�޲���");
			}
			if(info.length() > 90){
				info = info.substring(0, 90);
			}
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("innote", info);
			update(dbHelper, factionid, sqlStr);
			
			JSONArray pusharr = new JSONArray();
			pusharr.add(playerid);
			pusharr.add(info);
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_UPD_INFO, pusharr.toString(), factionid, playerid);
			
			GameLog.getInst(playerid, GameServlet.ACT_FACTION_UPD_INFO)
			.addRemark("���°�ṫ�棺" + info)
			.save();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����ְλ
	 * @param position 2.�峤 1.���峤 0.��Ա
	 */
	public ReturnValue adjustPosition(int playerid, int memberid, byte position){
		DBHelper dbHelper = new DBHelper();
		try {
			if(position != 0 && position != 1){
				BACException.throwInstance("�����ְλ");
			}
			if(playerid == memberid){
				BACException.throwInstance("���ܵ����Լ���ְλ");
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			DBPaRs facRs = getDataRs(factionid);
			if(plafacRs.getInt("position") != 2){
				BACException.throwInstance("Ȩ�޲���");
			}
			DBPaRs memFacRs = PlaFacBAC.getInstance().getDataRs(memberid);
			if(memFacRs.getInt("factionid") != factionid){
				BACException.throwInstance("���ǰ��ɳ�Ա");
			}
			if(memFacRs.getInt("position") == position){
				BACException.throwInstance("�Ѿ��Ǵ�ְλ");
			}
			checkAllowChangePersonnel(3);//����Ƿ��������±䶯
			if(position == 1){
				DBPsRs facmemRs = FacMemBAC.getInstance().query(factionid, "factionid="+factionid+" and position=1");
				if(facmemRs.count() >= 2){
					BACException.throwInstance("���ֻ��������������");
				}
			}
			DBPaRs memRs = PlayerBAC.getInstance().getDataRs(memberid);
			dbHelper.openConnection();
			PlaFacBAC.getInstance().setPosition(dbHelper, memberid, position);
			
			JSONArray pusharr = new JSONArray();
			pusharr.add(memberid);
			pusharr.add(position);
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_ADJUSET_POSITION, pusharr.toString(), factionid, playerid);
			
			StringBuffer sbRemark = new StringBuffer();
			sbRemark
			.append("���ɣ�")
			.append(GameLog.formatNameID(facRs.getString("name"), factionid))
			.append(" �� ")
			.append(GameLog.formatNameID(memRs.getString("name"), memberid))
			.append(" �� ")
			.append(position_name[memFacRs.getInt("position")])
			.append(" ����Ϊ  ")
			.append(position_name[position]);
			GameLog.getInst(playerid, GameServlet.ACT_FACTION_ADJUSET_POSITION, factionid)
			.addRemark(sbRemark)
			.save();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	public static final String[] position_name = {"����", "������", "����"};
	
	/**
	 * �Ƴ���Ա
	 */
	public ReturnValue removeMember(int playerid, String memberidStr){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_MEMBER)) {
				if(memberidStr == null || memberidStr.equals("")){
					BACException.throwInstance("Ҫ�Ƴ��ĳ�Ա�嵥Ϊ��");
				}
				int[] memberids = Tools.splitStrToIntArr(memberidStr, ",");
				if(Tools.intArrContain(memberids, playerid)){
					BACException.throwInstance("�����Ƴ��Լ�");
				}
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				int factionid = plafacRs.getInt("factionid");
				if(factionid == 0){
					BACException.throwInstance("��δ�������");
				}
				int position = plafacRs.getInt("position");
				if(position == 0){
					BACException.throwInstance("Ȩ�޲���");
				}
				DBPsRs facmemRs = FacMemBAC.getInstance().query(factionid, "factionid="+factionid);
				int[] pids = new int[facmemRs.count()];
				while(facmemRs.next()){
					int memid = facmemRs.getInt("playerid");
					if(Tools.intArrContain(memberids, memid) && position <= facmemRs.getInt("position")){
						BACException.throwInstance("Ȩ�޲���");
					}
					pids[facmemRs.getRow()-1] = memid;
				}
				for(int i = 0; i < memberids.length; i++){
					if(!Tools.intArrContain(pids, memberids[i])){
						BACException.throwInstance(memberids[i]+"���ǰ��ɳ�Ա");
					}
				}
				checkAllowChangePersonnel(2);//����Ƿ��������±䶯
				dbHelper.openConnection();
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_REMOVE_MEMBER, factionid);
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				for(int i = 0; i < memberids.length; i++){
					DBPaRs memRs = PlayerBAC.getInstance().getDataRs(memberids[i]);
					PlaFacBAC.getInstance().exitFaction(dbHelper, factionid, memberids[i], gl);
					if(sb1.length() > 0){
						sb1.append("��");
						sb2.append("��");
					}
					sb1.append(memRs.getString("name"));
					sb2.append(GameLog.formatNameID(memRs.getString("name"), memberids[i]));
				}
				
				JSONArray pusharr_2 = new JSONArray();
				pusharr_2.add(playerid);//�����߽�ɫID
				pusharr_2.add((new JSONArray(memberids)));//������Ա
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_REMOVE_MEMBER, pusharr_2.toString(), factionid, playerid);
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				JSONArray pusharr = new JSONArray();
				pusharr.add(factionid);//����ID
				pusharr.add(plafacRs.getString("facname"));//������
				pusharr.add(playerid);//�����߽�ɫID
				pusharr.add(plaRs.getString("name"));//�����߽�ɫ��
				PushData.getInstance().sendPlaToSome(SocketServer.ACT_FACTION_BEREMOVE, pusharr.toString(), memberids);
				
				gl.addRemark("���ɣ�"+GameLog.formatNameID(plafacRs.getString("facname"), factionid)+"����"+sb2.toString()+"�߳�")
				.save();
				return new ReturnValue(true);
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ע������
	 */
	public void dissmiss(DBHelper dbHelper, int factionid) throws Exception {
		delete(dbHelper, factionid, "id="+factionid);//ע������
		CBBAC.getInstance().giveupCity(dbHelper, factionid);
		CBTeamPoolBAC.getInstance().clearTeam(dbHelper, factionid);
		ServerFacBAC.getInstance().updateFactionRanking();
	}
	
	/**
	 * �˳�����
	 */
	public ReturnValue exitFaction(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_MEMBER)) {
				if (CBBAC.getInstance().isInCb(playerid)) {
					BACException.throwInstance("�Ѳμӳ�ս���޷��˳�����");
				}
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				int factionid = plafacRs.getInt("factionid");
				if(factionid == 0){
					BACException.throwInstance("��δ�������");
				}
				checkAllowChangePersonnel(2);//����Ƿ��������±䶯
				int position = plafacRs.getInt("position");
				/*
				if(position == 2){
					BACException.throwInstance("�峤�����˳�����");
				}
				*/
				String facname = plafacRs.getString("facname");
				dbHelper.openConnection();
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_EXIT, factionid);
				PlaFacBAC.getInstance().exitFaction(dbHelper, factionid, playerid, gl);
				int memberid = 0;
				if(position == 2){
					DBPsRs facmemRs = FacMemBAC.getInstance().query(factionid, "factionid="+factionid, "position desc");
					if(facmemRs.next()){
						memberid = facmemRs.getInt("playerid");
						PlaFacBAC.getInstance().setPosition(dbHelper, memberid, 2);
						SqlString sqlStr = new SqlString();
						sqlStr.add("playerid", memberid);
						sqlStr.add("pname", PlayerBAC.getInstance().getStrValue(memberid, "name"));
						update(dbHelper, factionid, sqlStr);
						CBTeamPoolBAC.getInstance().clearTeam(dbHelper, playerid, factionid);
					} else {
						dissmiss(dbHelper, factionid);//ע������
						memberid = -1;
						// ���ɽ�ɢ�����е�������
						FacApplyBAC.getInstance().clearAllApplyer(dbHelper, playerid, PlayerBAC.getInstance().getStrValue(playerid, "name"), factionid);
					}
				}
				
				if(memberid != -1){
					JSONArray pusharr = new JSONArray();
					pusharr.add(playerid);
					if(memberid > 0){
						pusharr.add(memberid);
					}
					PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_EXIT, pusharr.toString(), factionid, playerid);
				}
				
				gl.addRemark("�˳����ɣ�"+GameLog.formatNameID(facname, factionid));
				if(memberid > 0){
					String mname = PlayerBAC.getInstance().getStrValue(memberid, "name");
					gl.addRemark("���峤�� "+GameLog.formatNameID(mname, memberid)+" �̳�");
				} else 
				if(memberid == -1){
					gl.addRemark("���������ɳ�Ա�������Զ���ɢ");
				}
				gl.save();
				return new ReturnValue(true);
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �峤����
	 */
	public ReturnValue shanrang(int playerid, int memberid){
		DBHelper dbHelper = new DBHelper();
		try {
			if(playerid == memberid){
				BACException.throwInstance("�������ø��Լ�");
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") != 2){
				BACException.throwInstance("ֻ���峤��������");
			}
			DBPaRs memfacRs = PlaFacBAC.getInstance().getDataRs(memberid);
			if(memfacRs.getInt("factionid") != factionid){
				BACException.throwInstance("�������߲��ǰ��ɳ�Ա");
			}
			checkAllowChangePersonnel(3);//����Ƿ��������±䶯
			dbHelper.openConnection();
			PlaFacBAC.getInstance().setPosition(dbHelper, playerid, 0);
			PlaFacBAC.getInstance().setPosition(dbHelper, memberid, 2);
			DBPaRs memRs = PlayerBAC.getInstance().getDataRs(memberid);
			SqlString sqlStr = new SqlString();
			sqlStr.add("playerid", memberid);
			sqlStr.add("pname", memRs.getString("name"));
			update(dbHelper, factionid, sqlStr);
			
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_SHANRANG, String.valueOf(memberid), factionid, playerid);
			String mname = PlayerBAC.getInstance().getStrValue(memberid, "name");
			
			StringBuffer sbRemark = new StringBuffer();
			sbRemark
			.append("���ɣ�")
			.append(GameLog.formatNameID(plafacRs.getString("facname"), factionid))
			.append("�����ø���")
			.append(GameLog.formatNameID(mname, memberid));
			GameLog.getInst(playerid, GameServlet.ACT_FACTION_SHANRANG, factionid)
			.addRemark(sbRemark)
			.save();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����峤
	 */
	public ReturnValue impeach(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position")==2){
				BACException.throwInstance("���ܵ����Լ�");
			}
			DBPaRs facRs = getDataRs(factionid);
			DBPaRs headRs = PlayerBAC.getInstance().getDataRs(facRs.getInt("playerid"));
			if(System.currentTimeMillis()-headRs.getTime("logintime") < MyTools.long_day*3){
				BACException.throwInstance("�峤���첻���߲���������");
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTOIN_IMPEACH, factionid);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, 100, gl);
			PlaFacBAC.getInstance().setPosition(dbHelper, playerid, 2);
			PlaFacBAC.getInstance().setPosition(dbHelper, headRs.getInt("id"), 0);
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			SqlString sqlStr = new SqlString();
			sqlStr.add("playerid", playerid);
			sqlStr.add("pname", plaRs.getString("name"));
			update(dbHelper, factionid, sqlStr);
			
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_IMPEACH, ""+playerid, factionid, playerid);
			
			StringBuffer sbRemark = new StringBuffer();
			sbRemark
			.append("���ɣ�")
			.append(GameLog.formatNameID(plafacRs.getString("facname"), factionid))
			.append("�������峤��")
			.append(GameLog.formatNameID(headRs.getString("name"), headRs.getInt("id")));
			gl.addRemark(sbRemark)
			.save();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡָ����ŵİ�����Ϣ
	 */
	public ReturnValue getFactionData(int playerid, int factionid){
		try {
			JSONArray facarr = getAllData(factionid, true, false);
			return new ReturnValue(true, facarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����
	 */
	public ReturnValue rename(int playerid, String newName){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_NAME)) {
				MyTools.checkNoCharEx(newName, '#');
				if(newName.toLowerCase().equals("null")){
					BACException.throwInstance("���ֲ����ã�����ĺ�����");
				}
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				int factionid = plafacRs.getInt("factionid");
				if(factionid == 0){
					BACException.throwInstance("��δ�������");
				}
				DBPaRs facRs2 = getDataRs(factionid);
				String oldName = facRs2.getString("name");
				if(!oldName.contains("#")){
					BACException.throwInstance("�޸����ʸ�");
				}
				DBPsRs facRs = ServerFacBAC.getInstance().query(Conf.sid, "serverid="+Conf.sid+" and name='"+newName+"'");
				if(facRs.next()){
					BACException.throwInstance("�������Ѵ���");
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add("name", newName);
				update(dbHelper, factionid, sqlStr);//����
				SqlString facmemSqlStr = new SqlString();
				facmemSqlStr.add("facname", newName);
				FacMemBAC.getInstance().update(dbHelper, factionid, facmemSqlStr, "factionid="+factionid);//��Ա
				
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_RENAME, newName, factionid, playerid);
				
				GameLog.getInst(playerid, GameServlet.ACT_FACTION_RENAME)
				.addRemark("��"+oldName+"����Ϊ"+newName)
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
	 * ��������
	 */
	public ReturnValue upLevel(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("position") == 0){
				BACException.throwInstance("�޲���Ȩ��");
			}
			DBPaRs facRs = getDataRs(factionid);
			int faclv = facRs.getInt("lv");
			DBPaRs lvRs = DBPool.getInst().pQueryA(tab_faction_lv, "lv="+faclv);
			int needmoney = lvRs.getInt("needmoney");
			if(needmoney == -1){
				BACException.throwInstance("�����Ѵ�����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_UPLEVEL, factionid);
			FactionBAC.getInstance().changeMoney(dbHelper, factionid, -needmoney, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("lv", 1);
			update(dbHelper, factionid, sqlStr);
			JSONArray pusharr = new JSONArray();//����
			pusharr.add(playerid);
			pusharr.add(faclv+1);
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_UPLEVEL, pusharr.toString(), factionid, 0);
			
			gl.addChaNote("���ɵȼ�", faclv, 1)
			.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����ɿƼ�
	 */
	public ReturnValue upTechnology(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs technologyRs = DBPool.getInst().pQueryA(tab_faction_technology, "num="+num);
			if(!technologyRs.exist()){
				BACException.throwInstance("�Ƽ���Ų����� num="+num);
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			int position = plafacRs.getInt("position");
			if(position == 0){
				BACException.throwInstance("Ȩ�޲���");
			}
			DBPaRs facRs = getDataRs(factionid);
			JSONObject techobj = new JSONObject(facRs.getString("technology"));
			int currlv = techobj.optInt(String.valueOf(num));
			if(currlv == 0){
				currlv = 1;//�Ƽ��ȼ���ʼΪ1��
			}
			if(currlv >= technologyRs.getInt("maxlv")){
				BACException.throwInstance("�˿Ƽ�����������");
			}
			if(!technologyRs.getString("needfaclv").equals("-1")){
				int[] needlv = Tools.splitStrToIntArr(technologyRs.getString("needfaclv"), ",");
				if(facRs.getInt("lv") < needlv[currlv-1]){
					BACException.throwInstance("���ɵȼ����㣬�޷�����");
				}	
			}
			int[] needmoney = Tools.splitStrToIntArr(technologyRs.getString("needfacmoney"), ",");
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_UPTECHNOLOGE, factionid);
			FactionBAC.getInstance().changeMoney(dbHelper, factionid, -needmoney[currlv-1], gl);
			techobj.put(String.valueOf(num), currlv+1);
			SqlString sqlStr = new SqlString();
			sqlStr.add("technology", techobj.toString());
			update(dbHelper, factionid, sqlStr);
			
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_UP_TECHNOLOGY, String.valueOf(num), factionid, 0);
			
			gl.addChaNote("�Ƽ�("+technologyRs.getString("name")+")�ȼ�", currlv, 1);
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
	 * ��ȡ���ɿƼ�����ֵ
	 */
	public int getTechnologyFunc(DBPRs facRs, int num, int funcindex) throws Exception {
		int currlv = getTechnologyLv(facRs, num);
		DBPaRs technologyRs = DBPool.getInst().pQueryA(tab_faction_technology, "num="+num);
		int[][] func = Tools.splitStrToIntArr2(technologyRs.getString("func"), "|", ",");
		return func[currlv-1][funcindex];
	}
	
	/**
	 * ��ȡ���ɿƼ��ȼ�
	 */
	public int getTechnologyLv(DBPRs facRs, int num) throws Exception {
		JSONObject techobj = new JSONObject(facRs.getString("technology"));
		int currlv = techobj.optInt(String.valueOf(num));
		if(currlv == 0){
			currlv = 1;//�Ƽ��ȼ���ʼΪ1��
		}
		return currlv;
	}
	
	/**
	 * ��ȡ���ɸ���
	 */
	public ReturnValue getWelfare(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			if(plafacRs.getInt("getwelfare") == 1){
				BACException.throwInstance("��������ȡ�����ɸ���");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_GETWELFARE);
			DBPaRs facRs = getDataRs(factionid);
			int tochnologyLv = getTechnologyLv(facRs, 2);
			DBPaRs welfareRs = DBPool.getInst().pQueryA(tab_faction_welfare, "lv="+tochnologyLv);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, welfareRs.getString("award"), ItemBAC.SHORTCUT_MAIL, 1, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.add("getwelfare", 1);
			PlaFacBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
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
	 * Ĥ��
	 */
	public ReturnValue worship(int playerid, int worshippid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			if(playerid == worshippid){
				BACException.throwInstance("����Ĥ���Լ�");
			}
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			DBPaRs worshipRs = DBPool.getInst().pQueryA(tab_faction_worship, "num="+num);
			if(!worshipRs.exist()){
				BACException.throwInstance("Ĥ�ݷ�ʽ������ num="+num);
			}
			if(plafacRs.getInt("worship"+num) == 1){
				BACException.throwInstance("�����ѽ��й�"+worshipRs.getString("name"));
			}
			DBPaRs plafacRs2 = PlaFacBAC.getInstance().getDataRs(worshippid);
			if(plafacRs2.getInt("factionid") != factionid){
				BACException.throwInstance("ֻ��Ĥ��ͬһ���ɵ����");
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			DBPaRs plaRs2 = PlayerBAC.getInstance().getDataRs(worshippid);
			if(plaRs.getInt("lv") > plaRs2.getInt("lv")){
				BACException.throwInstance("ֻ��Ĥ�ݱ��Լ��ȼ��ߵ����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_WORSHIP);
			int[] consume = Tools.splitStrToIntArr(worshipRs.getString("consume"), ",");
			if(consume[0] == 1){
				PlayerBAC.getInstance().useMoney(dbHelper, playerid, consume[1], gl);
			} else 
			if(consume[0] == 2){
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, consume[1], gl);
			}
			SqlString sqlStr1 = new SqlString();
			sqlStr1.add("worship"+num, 1);
			PlaFacBAC.getInstance().update(dbHelper, playerid, sqlStr1);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, worshipRs.getString("waward"), ItemBAC.SHORTCUT_MAIL, 1, gl);
			JSONObject beworshipObj = new JSONObject(plafacRs2.getString("beworship"));
			beworshipObj.put(String.valueOf(num), beworshipObj.optInt(String.valueOf(num))+1);
			SqlString sqlStr = new SqlString();
			sqlStr.add("beworship", beworshipObj.toString());
			PlaFacBAC.getInstance().update(dbHelper, worshippid, sqlStr);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_WORSHIP, gl);
			
			PushData.getInstance().sendPlaToOne(SocketServer.ACT_FACTION_WORSHIP, String.valueOf(num), worshippid);
			
			gl.addRemark("Ĥ�� "+plaRs2.getString("name"));
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
	 * ��ȡ��Ĥ�ݽ���
	 */
	public ReturnValue getBeWorshipAward(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			JSONObject beworshipObj = new JSONObject(plafacRs.getString("beworship"));
			if(beworshipObj.length() == 0){
				BACException.throwInstance("�޽�������");
			}
			DBPsRs worshipRs = DBPool.getInst().pQueryS(tab_faction_worship);
			StringBuffer sb = new StringBuffer();
			while(worshipRs.next()){
				int amount = beworshipObj.optInt(worshipRs.getString("num"));
				for(int i = 0; i < amount; i++){
					if(sb.length() > 0){
						sb.append("|");
					}
					sb.append(worshipRs.getString("bwaward"));
				}
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_GET_BEWORSHIPAWARD);
			SqlString sqlStr = new SqlString();
			sqlStr.add("beworship", (new JSONObject()).toString());
			PlaFacBAC.getInstance().update(dbHelper, playerid, sqlStr);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, sb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			
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
	 * �Ϸ��˰���
	 */
	public ReturnValue mergerServerExitFactoin(){
		DBHelper dbHelper = new DBHelper();
		try {
			String del_where = " and lv<=15 and rechargermb=0 and logintime<"+MyTools.getDateSQL(MyTools.getCurrentDateLong()-MyTools.long_day*15);
			String sql = 
				"select a.*,b.factionid,b.facname from (" +
				"select id,lv,logintime,serverid,rechargermb from tab_player where serverid=" + Conf.sid + del_where + 
				") a left join tab_pla_faction b on a.id=b.playerid where b.factionid>0";
			ResultSet exitFacRs = dbHelper.executeQuery(sql);
			while(exitFacRs.next()){
				exitFaction(exitFacRs.getInt("id"));
			}
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	

	/**
	 * ����������
	 */
	/*public ReturnValue applyJoin(int playerid, int factionid){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_MEMBER, factionid)) {
				DBPsRs plaAppRs = PlaFacApplyBAC.getInstance().query(playerid, "playerid="+playerid+" and factionid="+factionid);
				if(plaAppRs.next()){
					BACException.throwInstance("����������˰��ɣ���ȴ��ظ�");
				}
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				if(plafacRs.getInt("factionid") != 0){
					BACException.throwInstance("�����ڰ����У��������������������");
				}
				DBPaRs facRs = getDataRs(factionid);
				if(facRs.getInt("serverid")!=Conf.sid){
					BACException.throwInstance("���ɲ�����");
				}
				int curamount = FacMemBAC.getInstance().getAmount(factionid);
				int maxamount = getTechnologyFunc(facRs, 1, 0);
				if(curamount >= maxamount){
					BACException.throwInstance("�˰��������������޷�����");
				}
				int applyAm = PlaFacApplyBAC.getInstance().getAmount(playerid);
				if(applyAm >= 3){
					BACException.throwInstance("�������������볷������������ٳ���");
				}
				int applyPlaAm = FacApplyBAC.getInstance().getAmount(factionid);
				if(applyPlaAm >= 30){
					BACException.throwInstance("�˰������������������볢��������������");
				}
				PlaFacBAC.getInstance().intoCheck(plafacRs);
				dbHelper.openConnection();
				SqlString sqlStr = new SqlString();
				sqlStr.add("playerid", playerid);
				sqlStr.add("factionid", factionid);
				sqlStr.addDateTime("applytime", MyTools.getTimeStr());
				PlaFacApplyBAC.getInstance().insert(dbHelper, playerid, sqlStr);
				
				DBPsRs applyRs = PlaFacApplyBAC.getInstance().query(playerid, "playerid="+playerid+" and factionid="+factionid);
				applyRs.next();
				JSONArray jsonarr = PlaFacApplyBAC.getInstance().getApplyData(applyRs);
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_APPLY, jsonarr.toString(), factionid, 0);
				
				GameLog.getInst(playerid, GameServlet.ACT_FACTION_APPLY)
				.addRemark("���������ɣ�" + GameLog.formatNameID(facRs.getString("name"), factionid))
				.save();
				return new ReturnValue(true);
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}*/
	
	/**
	 * ��������
	 */
	public ReturnValue revocationApply(int playerid, int factionid){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_MEMBER, factionid)) {
				DBPsRs plaappRs = PlaFacApplyBAC.getInstance().query(playerid, "playerid="+playerid+" and factionid="+factionid);
				if(!plaappRs.next()){
					BACException.throwInstance("δ�ҵ������¼");
				}
				DBPaRs facRs = getDataRs(factionid);
				dbHelper.openConnection();
				PlaFacApplyBAC.getInstance().delete(dbHelper, playerid, "playerid="+playerid+" and factionid="+factionid);
				
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_REVOCATION_APPLY, String.valueOf(playerid), factionid, 0);
				
				GameLog.getInst(playerid, GameServlet.ACT_FACTION_REVOCATION_APPLY)
				.addRemark("�������룺" + GameLog.formatNameID(facRs.getString("name"), factionid) + "���ɵ�����")
				.save();
				return new ReturnValue(true);
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������
	 * @param way 0.ͬ�� 1.�ܾ�
	 */
	public ReturnValue processApply(int playerid, int applyid, byte way){
		DBHelper dbHelper = new DBHelper();
		try {
			synchronized (LockStor.getLock(LockStor.FACTION_MEMBER)) {
				if(way !=0 && way != 1){
					BACException.throwInstance("����Ĵ���ʽ");
				}
				DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
				int factionid = plafacRs.getInt("factionid");
				if(factionid == 0){
					BACException.throwInstance("��δ�������");
				}
				if(plafacRs.getInt("position") == 0){
					BACException.throwInstance("Ȩ�޲���");
				}
				DBPaRs facRs = getDataRs(factionid);
				DBPaRs applyerRs = PlayerBAC.getInstance().getDataRs(applyid);
				DBPsRs applyRs = PlaFacApplyBAC.getInstance().query(applyid, "playerid="+applyid+" and factionid="+factionid);
				if(!applyRs.next()){
					BACException.throwInstance("�����ѱ�����");
				}
				JSONArray facInfoArray = new JSONArray();
				JSONArray meminfoarr = new JSONArray();
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				dbHelper.openConnection();
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_PROCESS_APPLY);
				if(way == 0){
					int curamount = FacMemBAC.getInstance().getAmount(factionid);
					int maxamount = getTechnologyFunc(facRs, 1, 0);
					if(curamount >= maxamount){
						BACException.throwInstance("������������");
					}
					DBPaRs appPlaFacRs = PlaFacBAC.getInstance().getDataRs(applyid);
					if(appPlaFacRs.getInt("factionid") != 0){
						BACException.throwInstance("�Է����а���");
					}
					checkAllowChangePersonnel(1);//����Ƿ��������±䶯
					PlaFacApplyBAC.getInstance().delete(dbHelper, applyid, "playerid="+applyid+" and factionid="+factionid);
					PlaFacApplyBAC.getInstance().clearAllApply(dbHelper, applyid, factionid);
					if(maxamount - curamount == 1){
						FacApplyBAC.getInstance().clearAllApplyer(dbHelper, playerid, plaRs.getString("name"), factionid);
					}
					facInfoArray = intoFaction(dbHelper, applyid, factionid, facRs, meminfoarr, playerid, SocketServer.ACT_FACTION_AGREE_APPLY);
				} else {
					PlaFacApplyBAC.getInstance().delete(dbHelper, applyid, "playerid="+applyid+" and factionid="+factionid);
					PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_REFUSE_APPLY, String.valueOf(applyid), factionid, 0);
				}
				
				JSONArray jsonarr = new JSONArray();
				jsonarr.add(factionid);//����ID
				jsonarr.add(playerid);//���������ID
				jsonarr.add(plaRs.getString("name"));//�����������
				jsonarr.add(way);//����ʽ
				if(way == 0){
					jsonarr.add(facInfoArray);//������ϸ��Ϣ
				}
				PushData.getInstance().sendPlaToOne(SocketServer.ACT_FACTION_PROCESS_APPLY, jsonarr.toString(), applyid);
				
				gl.addRemark((way==0?"ͬ��":"�ܾ�") + "��ң�" + GameLog.formatNameID(applyerRs.getString("name"), applyid) + "�������")
				.save();
				return new ReturnValue(true, meminfoarr.toString());
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���Ըı�ֵ
	 */
	public ReturnValue debugChangeValue(int playerid, String column, long value, long min, long max, String logname) {
		try {
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			return super.debugChangeValue(plafacRs.getInt("factionid"), column, value, min, max, logname);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false ,e.toString());
		}
	}
	
	/**
	 * ���ԼӰ����ʽ�
	 */
	public ReturnValue debugAddMoney(int playerid, int addmoney) throws Exception {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
			int factionid = plafacRs.getInt("factionid");
			if(factionid == 0){
				BACException.throwInstance("��δ�������");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			changeMoney(dbHelper, factionid, addmoney, gl);
			
			gl.addRemark("���ԼӰ����ʽ�");
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
	 * ����Ƿ��������±䶯
	 * @param type 1.������ 2.�˰��� 3.��ְλ
	 */
	public void checkAllowChangePersonnel(int type) throws Exception {
		
	}
	
	/**
	 * ��ȡ��¼��Ϣ
	 */
	public JSONArray getLoginData(int playerid) throws Exception {
		DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
		int factionid = plafacRs.getInt("factionid");
		JSONArray facarr = new JSONArray();
		if(factionid != 0){
			facarr = FactionBAC.getInstance().getAllData(factionid, true, true);
		} else {
			facarr = PlaFacApplyBAC.getInstance().getApplyFacList(playerid);
		}
		return facarr;
	}
	
	/**
	 * ��ȡ������ϸ��Ϣ
	 */
	public JSONArray getAllData(int factionid, boolean interactive, boolean deputy) throws Exception{
		JSONArray allarr = new JSONArray();
		DBPaRs facRs = getDataRs(factionid);
		//������Ϣ
		JSONArray facarr = new JSONArray();
		facarr.add(facRs.getString("name"));//����
		facarr.add(facRs.getInt("lv"));//�ȼ�
		facarr.add(facRs.getInt("money"));//�����ʽ�
		facarr.add(facRs.getString("joincond"));//�������
		facarr.add(facRs.getString("innote"));//��ṫ��
		facarr.add(new JSONObject(facRs.getString("technology")));//���ɿƼ�
		facarr.add(new JSONArray(facRs.getString("cmpassdata")));//������ͨ������
		facarr.add(new JSONObject(facRs.getString("cmdata")));//���ɸ�������
		facarr.add(facRs.getInt("occupyselfcity"));//�Ƿ���ռ�����г���
		facarr.add(facRs.getInt("declaream"));//��ս����
		allarr.add(facarr);
		/*--����������--*/
		if(interactive){
			//��Ա��Ϣ
			JSONArray memarr = FacMemBAC.getInstance().getFacMemData(factionid);
			allarr.add(memarr);//���ɳ�Ա��Ϣ
		}
		/*--˽�и�����--*/
		if(deputy){
			//������Ϣ
			JSONArray applyarr = FacApplyBAC.getInstance().getApplyerData(factionid);
			allarr.add(applyarr);
		}
		return allarr;
	}
	
	/**
	 * �������
	 * @return
	 */
	private JSONArray intoFaction(DBHelper dbHelper, int playerid, int factionid, DBPaRs facRs, JSONArray meminfoarr, int applyid, short pushAct) throws Exception {
		PlaFacBAC.getInstance().intoFaction(dbHelper, playerid, factionid, facRs.getString("name"), 0);
		meminfoarr = PlaFacBAC.getInstance().getMemData(playerid, 0);//���ɳ�Ա������Ϣ
		PushData.getInstance().sendPlaToFacMem(pushAct, meminfoarr.toString(), factionid, playerid, applyid);		
		JSONArray returnarr = getAllData(factionid, true, false);		
		MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerid}, 2, new Object[]{facRs.getString("name")}, new Object[]{facRs.getString("name")});		
		return returnarr;
	}
	
	/**
	 * ����������ճ�����
	 */
	public boolean checkAndResetDayData(DBHelper dbHelper, int playerid, boolean must) throws Exception {
		DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
		int factionid = plafacRs.getInt("factionid");
		if(factionid == 0){
			return false;
		}
		synchronized (LockStor.getLock(LockStor.FACTION_RESET_DAYDATA, factionid)) {
			DBPaRs facRs = getDataRs(factionid);
			long resetdate = facRs.getTime("resetdate");
			boolean needreset = must || MyTools.checkSysTimeBeyondSqlDate(resetdate);
			if(needreset){
				JSONObject pushdata = new JSONObject();
				SqlString sqlStr = new SqlString();
				long starttime = resetdate-MyTools.long_day;
				long endtime = System.currentTimeMillis();
				if(MyTools.checkWeek(starttime, endtime)){//����������
					
				}
				if(MyTools.checkMonth(starttime, endtime)){//����������
					
				}
				sqlStr.add("declaream", 0);
				sqlStr.addDateTime("resetdate", MyTools.getDaiylResetTime(5));
				update(dbHelper, factionid, sqlStr);
				PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_RESETDATA, pushdata.toString(), factionid, 0);
			}
			return needreset;
		}
	}
	
	/**
	 * ��ȡ���ɸ�Ҫ��Ϣ
	 */
	public JSONArray getInfo(DBPRs facRs) throws Exception {
		JSONArray arr = new JSONArray();
		arr.add(facRs.getInt("id"));//����ID
		arr.add(facRs.getString("name"));//������
		arr.add(facRs.getString("pname"));//������
		arr.add(facRs.getInt("lv"));//���ɵȼ�
		arr.add(facRs.getString("joincond"));//�������
		arr.add(FacMemBAC.getInstance().getAmount(facRs.getInt("id")));//��ǰ����
		arr.add(new JSONObject(facRs.getString("technology")));//���ɿƼ�
		return arr;
	}
	
	/**
	 * �Ӱ����ʽ�
	 */
	public void changeMoney(DBHelper dbHelper, int factionid, int changemoney, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.FACTION_ADDMONEY, factionid)) {
			DBPaRs facRs = getDataRs(factionid);
			int oldmoney = facRs.getInt("money");//ԭ�ʽ�
			if(changemoney < 0 && oldmoney < -changemoney){
				BACException.throwInstance("�����ʽ���");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("money", changemoney);
			update(dbHelper, factionid, sqlStr);
			PushData.getInstance().sendPlaToFacMem(SocketServer.ACT_FACTION_CHANGEMONEY, String.valueOf(changemoney), factionid, 0);
			gl.addChaNote("�����ʽ�", oldmoney, changemoney);		
		}
	}
	
	//--------------��̬��--------------
	
	private static FactionBAC instance = new FactionBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FactionBAC getInstance(){
		return instance;
	}
}
