package com.moonic.bac;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.MyPreparedStatement;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.GamePushData;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.txtdata.JJCChallengeData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ��������λս
 * @author John
 */
public class PlaJJCRankingBAC extends PlaBAC {
	public static final String tab_jjc_ranking_award = "tab_jjc_ranking_award";
	public static final String tab_jjc_refresh_award = "tab_jjc_refresh_award";
	public static final String tab_jjc_opp = "tab_jjc_opp";
	public static final String tab_jjc_reset_cha = "tab_jjc_reset_cha";
	
	private static int max_jjcrakning = -1;
	
	/**
	 * ����
	 */
	public PlaJJCRankingBAC() {
		super("tab_pla_jjcranking", "playerid");
		needcheck = false;
	}
	
	/**
	 * ��ʼ��
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		int ranking = createNewRanking(dbHelper);
		JSONArray defformationarr = new JSONArray();
		DBPsRs partnerStorRs = PartnerBAC.getInstance().query(playerid, "playerid="+playerid);
		while(partnerStorRs.next() && defformationarr.length() < 5){
			defformationarr.add(partnerStorRs.getInt("id"));
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.add("ranking", ranking);
		sqlStr.add("challengeamount", 0);
		sqlStr.add("resetamount", 0);
		sqlStr.add("refreshoppam", 0);
		sqlStr.add("defformation", defformationarr.toString());
		sqlStr.add("winning", 0);
		sqlStr.add("getmoney", 0);
		sqlStr.add("getjjccoin", 0);
		sqlStr.add("highranking", ranking);
		sqlStr.add("changetrend", 1);
		sqlStr.add("battlerecord", (new JSONArray()).toString());
		sqlStr.add("wkchaam", 0);
		insert(dbHelper, playerid, sqlStr);
		((JSONObject)parm[0]).put("jjcranking", ranking);//���������뵽�������ܵķ��ؽ����
		((JSONObject)parm[0]).put("jjcdefformation", defformationarr);//��������
	}
	
	/**
	 * ����������
	 */
	public int createNewRanking(DBHelper dbHelper) throws Exception {
		synchronized (LockStor.getLock(LockStor.JJC_MAX_RANKING)) {
			//long t1 = System.currentTimeMillis();
			if(max_jjcrakning < 0){
				ResultSet jjcrankingRs = dbHelper.query("tab_pla_jjcranking", "max(ranking) as max", "serverid="+Conf.sid);
				if(jjcrankingRs.next()){
					max_jjcrakning = jjcrankingRs.getInt("max");
				}
			}
			max_jjcrakning++;
			//long t2 = System.currentTimeMillis();
			//System.out.println("max_jjcrakning:"+max_jjcrakning+" time="+(t2-t1));
			//Out.println("���ɾ��������У�"+max_jjcrakning);
			return max_jjcrakning;
		}
	}
	
	/**
	 * ��ȡ��������Ϣ
	 */
	public ReturnValue getInfo(int playerid){
		try {
			DBPaRs plajjcrankingRs = getDataRs(playerid);
			if(!plajjcrankingRs.exist()){
				BACException.throwInstance("��������δ����");
			}
			JSONArray returnarr = new JSONArray();
			returnarr.add(plajjcrankingRs.getInt("ranking"));//��ǰ����
			returnarr.add(plajjcrankingRs.getInt("challengeamount"));//����ս����
			returnarr.add(plajjcrankingRs.getInt("resetamount"));//��������ս����
			returnarr.add(plajjcrankingRs.getTime("nextchatime"));//�´�������սʱ��
			returnarr.add(plajjcrankingRs.getInt("refreshoppam"));//ˢ�¶��ִ���
			returnarr.add(new JSONArray(plajjcrankingRs.getString("defformation")));//��������
			returnarr.add(plajjcrankingRs.getInt("winning"));//��ʤ����
			returnarr.add(plajjcrankingRs.getInt("getmoney"));//�ѻ��ͭǮ��
			returnarr.add(plajjcrankingRs.getInt("getjjccoin"));//�ѻ�þ�������
			returnarr.add(plajjcrankingRs.getInt("highranking"));//��ʷ�������
			returnarr.add(new JSONArray(plajjcrankingRs.getString("battlerecord")));//��ս��¼
			returnarr.add(getOppList(plajjcrankingRs, false));//������Ϣ
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ������������Ϣ
	 */
	public ReturnValue getOpps(int playerid){
		try {
			DBPaRs plajjcrankingRs = getDataRs(playerid);
			if(!plajjcrankingRs.exist()){
				BACException.throwInstance("��������δ����");
			}
			JSONArray returnarr = getOppList(plajjcrankingRs, false);
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡָ����������ľ���������
	 */
	public ReturnValue getRankingData(int playerid){
		try {
			JSONArray returnarr = new JSONArray();
			for(int ranking = 1; ranking <= 50; ranking++){
				DBPaRs jjcrankingRs = JJCRankingBAC.getInstance().getDataRs(ranking);
				if(jjcrankingRs.exist()){
					returnarr.add(getShowData(jjcrankingRs));
				}
			}
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ˢ�¾���������
	 */
	public ReturnValue refreshOpps(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plajjcrankingRs = getDataRs(playerid);
			if(!plajjcrankingRs.exist()){
				BACException.throwInstance("��������δ����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_JJC_RANKING_REFRESH_OPPS);
			if(plajjcrankingRs.getInt("refreshoppam") < 1){
				addValue(dbHelper, playerid, "refreshoppam", 1, gl, "������ˢ�¶��ִ���");
			} else {
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, JJCChallengeData.refreshoppneedcoin, gl);
			}
			JSONArray returnarr = getOppList(plajjcrankingRs, true);
			
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����սCDʱ��
	 */
	public ReturnValue clearCD(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plajjcrankingRs = getDataRs(playerid);
			if(!plajjcrankingRs.exist()){
				BACException.throwInstance("��������δ����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_JJC_RANKING_CLEAR_CD);
			if(MyTools.checkSysTimeBeyondSqlDate(plajjcrankingRs.getTime("nextchatime"))){
				BACException.throwInstance("����Ҫˢ��");
			}
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, JJCChallengeData.clearwaittimeneedcoin, gl);
			setTime(dbHelper, playerid, "nextchatime", MyTools.getTimeStr(), gl, "�������´���սʱ��");
			
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
	 * ������ս����
	 */
	public ReturnValue resetChallengeAmount(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plajjcrankingRs = getDataRs(playerid);
			if(!plajjcrankingRs.exist()){
				BACException.throwInstance("��������δ����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_JJC_RANKING_RESET_CHA_AM);
			if(plajjcrankingRs.getInt("challengeamount") < 5){
				BACException.throwInstance("��ս����δ����");
			}
			int nextresetamount = plajjcrankingRs.getInt("resetamount")+1;
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			int maxamount = VipBAC.getInstance().getVipFuncData(plaRs.getInt("vip"), 7);
			if(nextresetamount > maxamount){
				BACException.throwInstance("���ô���������������VIP�ȼ�");
			}
			DBPaRs jjcresetplaRs = DBPool.getInst().pQueryA(tab_jjc_reset_cha, "minamount<="+nextresetamount+" and maxamount>="+nextresetamount);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, jjcresetplaRs.getInt("needcoin"), gl);
			
			SqlString sqlStr = new SqlString();
			sqlStr.add("challengeamount", 0);
			sqlStr.addChange("resetamount", 1);
			update(dbHelper, playerid, sqlStr);
			
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
	 * ս��
	 */
	public ReturnValue toBattle(int playerid, int opppid, int pRanking, int oppranking, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			long currenttime = System.currentTimeMillis();
			if(currenttime>=MyTools.getCurrentDateLong()+JJCChallengeData.forbiddenstarttime && currenttime<MyTools.getCurrentDateLong()+JJCChallengeData.forbiddenendtime){
				BACException.throwInstance("��������������ͳ���У�"+JJCChallengeData.forbiddenendtime+"�󿪷ž�����ս");
			}
			byte[] lock1 = LockStor.getLock(LockStor.JJC_BATTLE, playerid);
			byte[] lock2 = LockStor.getLock(LockStor.JJC_BATTLE, opppid);
			synchronized (lock1) {
				synchronized (lock2) {
					DBPaRs plajjcrankingRs = getDataRs(playerid);
					if(!plajjcrankingRs.exist()){
						BACException.throwInstance("��������δ����");
					}
					if(pRanking != plajjcrankingRs.getInt("ranking")){
						BACException.throwInstance("��������ѷ����仯");
					}
					JSONArray opprankingarr = new JSONArray(plajjcrankingRs.getString("oppranking"));
					if(!opprankingarr.contains(oppranking)){
						BACException.throwInstance("������ս�б���");
					}
					DBPaRs oppjjcrankingRs = getDataRs(opppid);
					if(!oppjjcrankingRs.exist()){
						BACException.throwInstance("�Է���������δ����");
					}
					if(oppranking != oppjjcrankingRs.getInt("ranking")){
						BACException.throwInstance("���������ѷ����仯");
					}
					int challengeamount = plajjcrankingRs.getInt("challengeamount");
					if(challengeamount >= 5){
						BACException.throwInstance("��ս����������");
					}
					GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_JJC_RANKING_BATTLE);
					
					TeamBox teambox1 = PartnerBAC.getInstance().getTeamBox(playerid, 0, new JSONArray(posarrStr));
					TeamBox teambox2 = PartnerBAC.getInstance().getTeamBox(opppid, 1, new JSONArray(oppjjcrankingRs.getString("defformation")));
					BattleBox battlebox = new BattleBox();
					battlebox.bgnum = 102;
					battlebox.teamArr[0].add(teambox1);
					battlebox.teamArr[1].add(teambox2);
					BattleManager.createPVPBattle(battlebox);
					
					SqlString plaSqlStr = new SqlString();
					SqlString oppSqlStr = new SqlString();
					
					plaSqlStr.addChange("challengeamount", 1);
					
					JSONArray newoppsarr = null;
					
					int new_pRanking = pRanking;
					int new_oppranking = oppranking;
					if(battlebox.winTeam == Const.teamA){
						if(pRanking > oppranking){
							plaSqlStr.add("ranking", oppranking);
							plaSqlStr.add("changetrend", 2);
							plaSqlStr.add("oppranking", (newoppsarr=getOppRankingArr(playerid, oppranking)).toString());
							oppSqlStr.add("ranking", pRanking);
							oppSqlStr.add("changetrend", 0);
							oppSqlStr.add("oppranking", null);
							new_pRanking = oppranking;
							new_oppranking = pRanking;
						} else {
							plaSqlStr.add("changetrend", 1);
							oppSqlStr.add("changetrend", 1);
							plaSqlStr.add("oppranking", (newoppsarr=getOppRankingArr(playerid, pRanking)).toString());
						}
						plaSqlStr.addChange("winning", 1);
						oppSqlStr.add("winning", 0);
					} else {
						plaSqlStr.add("winning", 0);
						plaSqlStr.add("oppranking", (newoppsarr=getOppRankingArr(playerid, pRanking)).toString());
						plaSqlStr.addDateTime("nextchatime", MyTools.getTimeStr(System.currentTimeMillis()+JJCChallengeData.losewaittimelen));
						oppSqlStr.addChange("winning", 1);
					}
					
					if(new_pRanking != pRanking && new_pRanking == 1){
						DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
						DBPaRs oppRs = PlayerBAC.getInstance().getDataRs(opppid);
						GamePushData.getInstance(5)
						.add(plaRs.getString("name"))
						.add(oppRs.getString("name"))
						.sendToAllOL();
					}
					
					int addmoney = 0;
					int addjjccoin = 0;
					int getmoney = plajjcrankingRs.getInt("getmoney");
					int getjjccoin = plajjcrankingRs.getInt("getjjccoin");
					if(battlebox.winTeam == 0){
						addmoney = JJCChallengeData.winmoney;
						addjjccoin = JJCChallengeData.winjjccoin;
					} else {
						addmoney = JJCChallengeData.losemoney;
						addjjccoin = JJCChallengeData.losejjccoin;
					}
					if(getmoney+addmoney>JJCChallengeData.maxmoney){
						addmoney = JJCChallengeData.maxmoney-getmoney;
					}
					if(getjjccoin+addjjccoin>JJCChallengeData.maxjjccoin){
						addjjccoin = JJCChallengeData.maxjjccoin-getjjccoin;
					}
					if(addmoney > 0){
						plaSqlStr.addChange("getmoney", addmoney);
						PlayerBAC.getInstance().addValue(dbHelper, playerid, "money", addmoney, gl, GameLog.TYPE_MONEY);
					}
					if(addjjccoin > 0){
						plaSqlStr.addChange("getjjccoin", addjjccoin);
						PlaRoleBAC.getInstance().addValue(dbHelper, playerid, "jjccoin", addjjccoin, gl, "������");
					}
					
					JSONArray showdata1 = getShowData(plajjcrankingRs);
					showdata1.put(4, new_pRanking);
					showdata1.put(6, plajjcrankingRs.getInt("ranking"));
					JSONArray showdata2 = getShowData(oppjjcrankingRs);
					showdata2.put(4, new_oppranking);
					showdata2.put(6, oppjjcrankingRs.getInt("ranking"));
					
					JSONArray logarr = new JSONArray();
					logarr.add(showdata1);
					logarr.add(showdata2);
					logarr.add(battlebox.winTeam);
					logarr.add(battlebox.battleId);
					logarr.add(System.currentTimeMillis());
					
					JSONArray plalogarr = new JSONArray(plajjcrankingRs.getString("battlerecord"));
					if(plalogarr.length() >= 5){
						plalogarr.remove(0);
					}
					plalogarr.add(logarr);
					
					JSONArray opplogarr = new JSONArray(oppjjcrankingRs.getString("battlerecord"));
					if(opplogarr.length() >= 5){
						opplogarr.remove(0);
					}
					opplogarr.add(logarr);
					
					plaSqlStr.add("battlerecord", plalogarr.toString());
					
					oppSqlStr.add("battlerecord", opplogarr.toString());
					
					int highranking = plajjcrankingRs.getInt("highranking");
					double addcoin = 0;
					if(battlebox.winTeam == 0 && oppranking < highranking){
						DBPsRs refreshawardRs = DBPool.getInst().pQueryS(tab_jjc_refresh_award, null, "maxranking desc");
						int tagranking = highranking+1;
						//System.out.println("refreshawardRs.count():"+refreshawardRs.count());
						while(tagranking > oppranking){
							while(refreshawardRs.getRow() == 0 || tagranking < refreshawardRs.getInt("minranking")){
								refreshawardRs.next();
							}
							addcoin += refreshawardRs.getDouble("award");
							tagranking--;
						}
						MailBAC.getInstance().sendSysMail(dbHelper, playerid, "ˢ����ʷ�����������", "��ʷ���������"+highranking+"������"+oppranking, "4,"+(int)addcoin, 0);
						plaSqlStr.add("highranking", oppranking);
					}
					
					update(dbHelper, playerid, plaSqlStr);
					update(dbHelper, opppid, oppSqlStr);
					
					PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_JJC, gl);
					
					PushData.getInstance().sendPlaToOne(SocketServer.ACT_JJC_RANKING_BATTLE, logarr.toString(), teambox2.playerid);
					
					JSONArray returnarr = new JSONArray();
					returnarr.add(addmoney);//��ý�Ǯ��
					returnarr.add(addjjccoin);//��þ�������
					returnarr.add(logarr);//ս����¼
					returnarr.add(battlebox.replayData);//ս��¼��
					returnarr.add(getOppList(newoppsarr));//�¶����б�
					returnarr.add((int)addcoin);//ˢ����ʷ������õĽ���
					
					BattleReplayBAC.getInstance().saveReplay(battlebox.battleId, battlebox.replayData.toString(), 7, 1);
					
					CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 26);
					
					gl.addRemark(teambox1.pname+"_R"+pRanking+" vs "+teambox2.pname+"("+battlebox.battleId+"_R"+oppranking+") ʤ�����飺"+battlebox.winTeam);
					gl.save();
					return new ReturnValue(true, returnarr.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���÷�������
	 */
	public ReturnValue setDefForm(int playerid, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plajjcrankingRs = getDataRs(playerid);
			if(plajjcrankingRs.getString("defformation").equals(posarrStr)){
				BACException.throwInstance("�����ޱ仯");
			}
			JSONArray posarr = new JSONArray(posarrStr);
			PartnerBAC.getInstance().checkPosarr(playerid, posarr, 0, 1);//��������Ƿ�Ϸ�
			SqlString sqlStr = new SqlString();
			sqlStr.add("defformation", posarr.toString());
			update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ���ֻ����Ϣ
	 */
	public ReturnValue getOppDefFormData(int playerid, int oppid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs oppjjcrankingRs = getDataRs(oppid);
			JSONArray posarr = new JSONArray(oppjjcrankingRs.getString("defformation"));
			JSONArray dataarr = PartnerBAC.getInstance().getPartnerDataByPosarr(oppid, posarr);
			return new ReturnValue(true, dataarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	private CreateRobotThread createRobotThread;
	
	/**
	 * ��������������
	 */
	public ReturnValue createPCPlayer(){
		try {
			if(createRobotThread != null){
				if(createRobotThread.result == null){
					return new ReturnValue(false, "[doing]");
				} else {
					String info = createRobotThread.result;
					createRobotThread = null;
					return new ReturnValue(true, info);
				}
			}
			createRobotThread = new CreateRobotThread();
			(new Thread(createRobotThread)).start();
			return new ReturnValue(false, "[doing]");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	class CreateRobotThread implements Runnable {
		private String result;
		public void run() {
			DBHelper dbHelper = new DBHelper();
			try {
				JSONArray namearr = createRandomName(5000);
				ResultSet userRs = dbHelper.query("tab_user", "id", "platform='000' and username like 'jjc_%'");
				DBPaRs channelServerRs = DBPool.getInst().pQueryA("tab_channel_server", "serverid="+Conf.sid);
				DBPsRs pcRs = DBPool.getInst().pQueryS("tab_jjc_pc", null, "minranking");
				int ranking = 1;
				while(userRs.next() && userRs.getRow() <= 5000){
					ReturnValue createRv = PlayerBAC.getInstance().create(userRs.getInt("id"), channelServerRs.getInt("vsid"), namearr.optString(userRs.getRow()-1), (byte)MyTools.getRandom(1, 10), 0, 1);
					if(createRv.success){
						if(pcRs.getRow() == 0 || ranking > pcRs.getInt("maxranking")){
							pcRs.next();
						}
						int playerid = Integer.valueOf(createRv.info);
						GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
						PlayerBAC.getInstance().shortcutGrow(playerid, pcRs.getInt("grownum"), 1, 0, new JSONArray(), gl);
						ranking++;
					} else {
						//System.out.println("��������������ʧ�ܣ�"+createRv.info);
					}
				}
				result = "�ɹ�����"+(ranking-1)+"������������";
			} catch (Exception e) {
				e.printStackTrace();
				result = "���������з����쳣:"+e.toString();
			} finally {
				dbHelper.closeConnection();
			}
		}
	}
	
	/**
	 * ���������
	 */
	private JSONArray createRandomName(int amount) throws Exception{
		DBPsRs rs1 = DBPool.getInst().pQueryS("tab_random_name", "type=0");
		DBPsRs rs2 = DBPool.getInst().pQueryS("tab_random_name", "type!=0");
		int firLen = rs1.count();
		int secLen = rs2.count();
		ArrayList<String> namearr = new ArrayList<String>();
		while(namearr.size() < amount){
			int fir = MyTools.getRandom(1, firLen);
			int sec = MyTools.getRandom(1, secLen);
			StringBuffer sb = new StringBuffer();
			rs1.setRow(fir);
			sb.append(rs1.getString("name"));
			rs2.setRow(sec);
			sb.append(rs2.getString("name"));
			String newname = sb.toString();
			if(namearr.contains(newname)){
				continue;
			}
			namearr.add(newname);
		}
		return new JSONArray(namearr);
	}
	
	/**
	 * ��ȡ�����б�
	 */
	private JSONArray getOppList(DBPaRs plajjcrankingRs, boolean mustRefresh) throws Exception {
		int playerid = plajjcrankingRs.getInt("playerid");
		int ranking = plajjcrankingRs.getInt("ranking");
		String opprankingStr = null;
		if(!mustRefresh){
			opprankingStr = plajjcrankingRs.getString("oppranking");
		}
		JSONArray opprankingarr = null;
		if(opprankingStr == null){
			opprankingarr = getOppRankingArr(playerid, ranking);
		} else {
			opprankingarr = new JSONArray(opprankingStr);
		}
		return getOppList(opprankingarr);
	}
	
	/**
	 * ��ȡ�����б�
	 */
	private JSONArray getOppList(JSONArray opprankingarr) throws Exception {
		JSONArray returnarr = new JSONArray();
		for(int i = 0; i < opprankingarr.size(); i++){
			DBPaRs jjcrankingRs = JJCRankingBAC.getInstance().getDataRs(opprankingarr.optInt(i));
			if(jjcrankingRs.exist()){
				returnarr.add(getShowData(jjcrankingRs));
			}
		}
		return returnarr;
	}
	
	/**
	 * ��ȡ�����ʾ����
	 */
	private JSONArray getShowData(DBPaRs jjcrankingRs) throws Exception{
		int playerid = jjcrankingRs.getInt("playerid");
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		JSONArray arr = new JSONArray();
		arr.add(playerid);
		arr.add(plaRs.getString("name"));
		arr.add(plaRs.getInt("lv"));
		arr.add(plaRs.getInt("num"));
		arr.add(jjcrankingRs.getInt("ranking"));
		//arr.add(jjcrankingRs.getInt("changetrend"));
		arr.add(PartnerBAC.getInstance().getPlayerBattlePower(playerid, new JSONArray(jjcrankingRs.getString("defformation")), jjcrankingRs.getInt("battlepower")));
		return arr;
	}
	
	/**
	 * ��ȡ������������
	 */
	private JSONArray getOppRankingArr(int playerid, int pRanking) throws Exception {
		DBHelper dbHelper = new DBHelper();
		try {
			JSONArray opprankingarr = new JSONArray();
			DBPaRs oppRs = DBPool.getInst().pQueryA(tab_jjc_opp, "minranking<="+pRanking+" and maxranking>="+pRanking);
			for(int i = 1; i <= 4; i++){
				int[] opprankingrange = Tools.splitStrToIntArr(oppRs.getString("oppranking"+i), ",");
				int oppranking = MyTools.getRandom(pRanking+opprankingrange[0], pRanking+opprankingrange[1]);
				opprankingarr.add(oppranking);
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("oppranking", opprankingarr.toString());
			update(dbHelper, playerid, sqlStr);
			return opprankingarr;
		} catch (Exception e) {
			throw e;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������
	 */
	public void resetData(DBHelper dbHelper, int playerid) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("challengeamount", 0);
		sqlStr.add("resetamount", 0);
		sqlStr.add("refreshoppam", 0);
		sqlStr.add("getmoney", 0);
		sqlStr.add("getjjccoin", 0);
		sqlStr.add("wkchaam", 0);
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ���ž���������
	 */
	public ReturnValue issueAward(String from) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			String sql = "select a.*,b.isrobot from (select playerid,ranking from tab_pla_jjcranking where serverid=?) a left join tab_player b on a.playerid=b.id where b.isrobot=0";
			MyPreparedStatement stmt = dbHelper.getStmt(sql);
			stmt.setInt(1, Conf.sid);
			ResultSet plajjcrankingRs = stmt.executeQuery();
			while(plajjcrankingRs.next()){
				int playerid = plajjcrankingRs.getInt("playerid");
				int ranking = plajjcrankingRs.getInt("ranking");
				DBPaRs rankingawardRs = DBPool.getInst().pQueryA(tab_jjc_ranking_award, "minranking<="+ranking+" and maxranking>="+ranking);
				MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerid}, 4, new Object[]{ranking}, new Object[]{ranking}, rankingawardRs.getString("award"));
			}
			
			GameLog.getInst(0, GameServlet.ACT_JJC_RANKING_ISSUEAWARD)
			.addRemark(from+" ���ž������н���")
			.save();
			return new ReturnValue(true, "�������");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//------------------��̬��--------------------
	
	private static PlaJJCRankingBAC instance = new PlaJJCRankingBAC();

	public static PlaJJCRankingBAC getInstance() {
		return instance;
	}
}
