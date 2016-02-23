package com.moonic.team;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import com.moonic.bac.AwardBAC;
import com.moonic.bac.Enemy;
import com.moonic.bac.ItemBAC;
import com.moonic.bac.PartnerBAC;
import com.moonic.bac.PlaTeamBAC;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.txtdata.TeamActivityData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.MyLog;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

import server.common.Tools;

/**
 * ��ӻ
 * @author wkc
 */
public class TeamActivity {
	public long startTime;// ��ʼʱ��
	public long actiTimeLen;// �ʱ��

	public Hashtable<Integer, Team> teamTable = new Hashtable<Integer, Team>();// ����table
	
	public Hashtable<Integer, Integer> pla_tab = new Hashtable<Integer, Integer>();//������ڶ�����

	public MyLog log = new MyLog(MyLog.NAME_CUSTOM, "teamacti", "TEAMACTI", true, false, true, MyTools.formatTime("yyyy-MM-dd-hh-mm-ss"));// ��־

	private ScheduledExecutorService timer = MyTools.createTimer(3);//��ʱ��
	
	/**
	 * ����
	 */
	public TeamActivity(long actiTimeLen){
		this.startTime = System.currentTimeMillis();
		this.actiTimeLen = actiTimeLen;
	}
	
	/**
	 * ����
	 */
	public void start() {
		Out.println("��ӻ��ʼ");
		log.d("��ӻ����ʱ" + actiTimeLen / 1000 + "s");
		MyTimerTask endTT = new MyTimerTask() {
			public void run2() {
				DBHelper dbHelper = new DBHelper();
				try {
					PlaTeamBAC.teamActivity = null;
					Out.println("��ӻ����");
					dbHelper.openConnection();
					PlaTeamBAC.getInstance().resetTimes(dbHelper);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dbHelper.closeConnection();
				}
			}
		};
		timer.schedule(endTT, actiTimeLen, TimeUnit.MILLISECONDS);
		JSONArray pusharr = new JSONArray();
		pusharr.add(startTime);
		pusharr.add(actiTimeLen);
		PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_TEAM_ACTI_START, pusharr.toString());
	}

	/**
	 * ��������
	 * @param type,��������
	 * @return teamNum,������
	 * @throws Exception
	 */
	public int createTeam(int playerid, int type, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI)) {
			if(pla_tab.containsKey(playerid)){
				BACException.throwInstance("������ڶ�����");
			}
			Member member = new Member(playerid);
			if(member.times >= 3){
				BACException.throwInstance("��ս����������");
			}
			member.isLeader = true;
			member.isReady = true;
			Team team = new Team(member, type);
			team.memberTable.put(playerid, member);
			int teamNum = team.num;
			teamTable.put(teamNum, team);
			pla_tab.put(playerid, teamNum);
			log.d(member.name+"��������"+GameLog.formatNameID(team.name, teamNum));
			gl.addRemark("��������" + GameLog.formatNameID(team.name, teamNum));
			return teamNum;
		}
	}

	/**
	 * �������
	 * @param num,������
	 * @throws Exception
	 */
	public JSONArray joinTeam(int playerid, int num, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			if(pla_tab.containsKey(playerid)){
				BACException.throwInstance("������ڶ�����");
			}
			Team team = teamTable.get(num);
			if (team == null) {
				BACException.throwInstance("�����Ų�����");
			}
			if (team.memberTable.containsKey(playerid)) {
				BACException.throwInstance("���ڴ˶�����");
			}
			if (team.memberTable.size() >= 3) {
				BACException.throwInstance("�����Ա����");
			}
			Member member = new Member(playerid);
			if (member.lv < team.lvLimit) {
				BACException.throwInstance("�˶������Ƶȼ�Ϊ" + team.lvLimit);
			}
			JSONArray memberArr = team.getMemberList();
			team.memberTable.put(playerid, member);
			pla_tab.put(playerid, num);
			team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_JOIN, member.getMemberInfo().toString(), playerid);
			log.d(member.name+"�������"+GameLog.formatNameID(team.name, num));
			gl.addRemark("�������" + GameLog.formatNameID(team.name, num));
			return memberArr;
		}
	}

	/**
	 * �߳�����
	 * @param num,������
	 * @param memberid,��ԱID
	 * @throws Exception
	 */
	public void kickOut(int playerid, int num, int memberid, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			Team team = teamTable.get(num);
			if (team == null) {
				BACException.throwInstance("�����Ų�����");
			}
			if (team.leader.id != playerid) {
				BACException.throwInstance("ֻ�жӳ���������");
			}
			Member member = team.memberTable.get(memberid);
			if (member == null) {
				BACException.throwInstance("��Ҳ��ڴ˶�����");
			}
			team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_EXIT, String.valueOf(memberid), playerid);
			team.memberTable.remove(memberid);
			pla_tab.remove(memberid);
			log.d(team.leader.name+"�Ӷ���" + GameLog.formatNameID(team.name, num) + "�߳�" + member.name);
			gl.addRemark("�Ӷ���" + GameLog.formatNameID(team.name, num) + "�߳�" + member.name);
		}
	}

	/**
	 * ����
	 * @param num,������
	 * @throws Exception
	 */
	public int format(int playerid, int num, JSONArray posarr, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			Team team = teamTable.get(num);
			if (team == null) {
				BACException.throwInstance("�����Ų�����");
			}
			Member member = team.memberTable.get(playerid);
			if (member == null) {
				BACException.throwInstance("��Ҳ��ڴ˶�����");
			}
			if (!member.isLeader && member.isReady) {
				BACException.throwInstance("׼��״̬�²��ܲ���");
			}
			if (posarr.length() != 2) {
				BACException.throwInstance("���ͳ��ȴ���");
			}
			JSONArray partnerarr = new JSONArray();
			for (int i = 0; i < posarr.length(); i++) {
				JSONArray partner = new JSONArray();
				int partnerId = posarr.getInt(i);
				if (partnerId != 0) {
					DBPaRs partnerRs = PartnerBAC.getInstance().getDataRsByKey(playerid, partnerId);
					if (!partnerRs.exist()) {
						BACException.throwInstance("��鲻����");
					}
					partner.add(partnerId);//ID
					partner.add(partnerRs.getInt("num"));//���
					partner.add(partnerRs.getInt("lv"));//�ȼ�
					partner.add(partnerRs.getInt("star"));//�Ǽ�
					partner.add(partnerRs.getInt("phase"));//�׶�
				} 
				partnerarr.add(partner);
			}
			member.partnerArr = partnerarr;
			TeamBox teambox = PartnerBAC.getInstance().getTeamBox(playerid, 0, posarr);
			int battlePower = PartnerBAC.getInstance().getTeamBoxBattlePower(teambox);
			member.battlePower = battlePower;
			JSONArray pusharr = new JSONArray();
			pusharr.add(playerid);
			pusharr.add(battlePower);
			pusharr.add(member.partnerArr);
			team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_FORMAT, pusharr.toString(), playerid);
			log.d(member.name+"��������Ϊ"+posarr);
			gl.addRemark("���ͱ�Ϊ" + posarr);
			return battlePower;
		}
	}

	/**
	 * ׼��
	 * @param num,������
	 * @throws Exception
	 */
	public void beReady(int playerid, int num, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			Team team = teamTable.get(num);
			if (team == null) {
				BACException.throwInstance("�����Ų�����");
			}
			Member member = team.memberTable.get(playerid);
			if (member == null) {
				BACException.throwInstance("��Ҳ��ڴ˶�����");
			}
			if (member.isReady) {
				BACException.throwInstance("��׼��");
			}
			if(member.getPartnerAm() == 0){
				BACException.throwInstance("��������һ�����");
			}
			member.isReady = true;
			team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_BEREADY, String.valueOf(playerid), playerid);
			gl.addRemark("׼��");
		}
	}

	/**
	 * ȡ��׼��
	 * @param num,������
	 * @throws Exception
	 */
	public void cancelReady(int playerid, int num, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			Team team = teamTable.get(num);
			if (team == null) {
				BACException.throwInstance("�����Ų�����");
			}
			Member member = team.memberTable.get(playerid);
			if (member == null) {
				BACException.throwInstance("��Ҳ��ڴ˶�����");
			}
			if (!member.isReady) {
				BACException.throwInstance("δ׼��");
			}
			member.isReady = false;
			team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_CANCELREADY, String.valueOf(playerid), playerid);
			gl.addRemark("ȡ��׼��");
		}
	}
	
	/**
	 * �˳�����
	 * @throws Exception
	 */
	public void exitTeam(int playerid, int num, GameLog gl) throws Exception{
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			Team team = teamTable.get(num);
			if (team != null) {
				Member member = team.memberTable.get(playerid);
				if (member != null) {
					if(member.isLeader){//�ӳ��˳��رշ���
						Iterator<Integer> keys = team.memberTable.keySet().iterator();
						while(keys.hasNext()){
							pla_tab.remove(keys.next());
						}
						teamTable.remove(num);
						log.d("�ӳ�"+member.name+"�뿪����"+GameLog.formatNameID(team.name, num));
						team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_CLOSE, "", playerid);
						if(gl != null){
							gl.addRemark("�ӳ��뿪����"+GameLog.formatNameID(team.name, num));
						}
					} else{//��Ա�˳�
						pla_tab.remove(playerid);
						team.memberTable.remove(playerid);
						log.d("��Ա"+member.name+"�뿪����"+GameLog.formatNameID(team.name, num));
						team.sendMsgToTeam(SocketServer.ACT_TEAM_ACTI_EXIT, String.valueOf(playerid), playerid);
					}
				}
			}
		}
	}
	
	/**
	 * ս��
	 * @param num,������
	 * @throws Exception
	 */
	public JSONArray battle(DBHelper dbHelper, int playerid, int num, GameLog gl) throws Exception {
		synchronized (LockStor.getLock(LockStor.TEAM_ACTI, num)) {
			Team team = teamTable.get(num);
			if (team == null) {
				BACException.throwInstance("�����Ų�����");
			}
			if (team.leader.id != playerid) {
				BACException.throwInstance("ֻ�жӳ����ܿ�ʼս��");
			}
			if (team.leader.getPartnerAm() == 0) {
				BACException.throwInstance("��������һ�����");
			}
			team.checkReady();
			teamTable.remove(num);//�Ƴ�����
			BattleBox battleBox = new BattleBox();
			battleBox.teamArr[0].add(team.getTeamBox());
			battleBox.teamArr[1].add(Enemy.getInstance().createTeamBox(team.boss));
			BattleManager.createPVPBattle(battleBox);
			JSONArray jsonarr = new JSONArray();
			jsonarr.add(battleBox.replayData);// ս��¼��
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("����"+GameLog.formatNameID(team.name, num)+"��ս");
			remarkSb.append(battleBox.winTeam == Const.teamA ? "�ɹ�" : "ʧ��");
			Iterator<Member> members = team.memberTable.values().iterator();
			log.d(remarkSb.toString());
			while(members.hasNext()){
				Member member = members.next();
				pla_tab.remove(member.id);
				if(battleBox.winTeam == Const.teamA && member.times < TeamActivityData.times){
					JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, member.id, team.award, ItemBAC.SHORTCUT_MAIL, 1, gl);
					jsonarr.add(team.award);
					jsonarr.add(awardarr);
					member.times++;//���ӻ񽱴���
					PlaTeamBAC.getInstance().addValue(dbHelper, member.id, "times", 1, gl, "��ý�������");
				}
				if(!member.isLeader){
					PushData.getInstance().sendPlaToOne(SocketServer.ACT_TEAM_ACTI_BATTLE, jsonarr.toString(), member.id);
				}
			}
			gl.addRemark(remarkSb);
			return jsonarr;
		}
	}
	
	/**
	 * �ǳ�,�رն���
	 * @throws Exception 
	 */
	public void logout(int playerid) throws Exception{
		if(pla_tab.containsKey(playerid)){
			exitTeam(playerid, pla_tab.get(playerid), null);
		}
	}
	
	/**
	 * ��ȡָ���������͵Ķ����б�
	 */
	public JSONArray getTeamList(int type) {
		JSONArray jsonarr = new JSONArray();
		Team[] teamArr = teamTable.values().toArray(new Team[teamTable.size()]);
		Tools.sort(teamArr, 0);
		for (int i = 0; i < teamArr.length; i++) {
			if(teamArr[i].type == type){
				jsonarr.add(teamArr[i].getTeamInfo());
			}
		}
		return jsonarr;
	}
	
	/**
	 * ��ȡ��½����
	 */
	public JSONArray getLoginData(){
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(startTime);
		jsonarr.add(actiTimeLen);
		return jsonarr;
	}
}
