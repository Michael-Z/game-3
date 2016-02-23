package com.moonic.bac;

import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.txtdata.MineralsData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

import conf.Conf;
	
/**
 * ����
 * @author John
 */
public class PlaMineralsBAC {
	private Hashtable<Integer, JSONArray> playertab = new Hashtable<Integer, JSONArray>();//ARR���ݣ������ͣ�ռ��ʱ�䣬����(������û�б��)�����ID
	private Vector<Integer> useNumVec = new Vector<Integer>();//�Ѿ�ʹ�õĿ���(�м���101��ʼ���߼���201��ʼ)
	private boolean isRun;
	
	/**
	 * ռ��
	 */
	public ReturnValue clockIn(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			if(!isRun){
				BACException.throwInstance("���ڻ��");
			}
			int diff = num / 100;
			if(diff <0 || diff > 2){
				BACException.throwInstance("��Ų���ȷ num="+num);
			}
			int posnum = num % 100;
			if(diff != 0 && (posnum < 1 || posnum > MineralsData.posamount[diff])){
				BACException.throwInstance("��Ų���ȷ num="+num);
			}
			synchronized (LockStor.getLock(LockStor.MINERALS, num)) {
				if(useNumVec.contains(num)){
					BACException.throwInstance("������Ѿ�����ռ�� num="+num);
				}
				JSONArray myArr = playertab.get(playerid);
				if(myArr != null){//������һ��λ����
					if(myArr.optInt(2) == num){
						BACException.throwInstance("�������Լ��Ŀ� num="+num);
					}
					issueAward(dbHelper, playerid, myArr.optInt(0), myArr.optLong(1), System.currentTimeMillis(), null, 1);
					if(diff != 0){
						useNumVec.remove((Integer)myArr.optInt(2));
					}
				}
				long starttime = System.currentTimeMillis();
				JSONArray new_myArr = new JSONArray();//�洢�¿�λ��Ϣ
				new_myArr.add(diff);//�Ѷ�
				new_myArr.add(starttime);//��ʼʱ��
				new_myArr.add(num);//��λ���
				new_myArr.add(playerid);//���ID
				playertab.put(playerid, new_myArr);
				
				if(diff != 0){
					useNumVec.add(num);	
				}
				
				if(diff != 0){
					PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_MINERALS_CLOCKIN, new_myArr.toString());	
				}
				
				GameLog.getInst(playerid, GameServlet.ACT_MINERALS_CLOCKIN)
				.addRemark("�Ӻţ�"+num)
				.save();
				return new ReturnValue(true, String.valueOf(starttime));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����
	 */
	public ReturnValue contend(int playerid, int opppid, int num, String posarrStr){
		DBHelper dbHelper = new DBHelper();
		try {
			if(!isRun){
				BACException.throwInstance("���ڻ��");
			}
			synchronized (LockStor.getLock(LockStor.MINERALS, num)) {
				JSONArray oppArr = playertab.get(opppid);
				if(oppArr == null || oppArr.optInt(0) == 0){
					BACException.throwInstance("�Է�û��ռ���и߼���");
				}
				if(oppArr.optInt(2) != num){
					BACException.throwInstance("��λ������ opppid="+opppid+" num="+num);
				}
				int diff = oppArr.optInt(0);
				
				DBPaRs plajjcRs = PlaJJCRankingBAC.getInstance().getDataRs(playerid);
				if(System.currentTimeMillis() < plajjcRs.getTime("wkchatime") + MineralsData.losetime){
					BACException.throwInstance("��ȴʱ��δ����");
				}
				JSONArray myArr = playertab.get(playerid);
				if(myArr != null && myArr.optInt(2) == num){
					BACException.throwInstance("�������Լ��Ŀ� num="+num);
				}
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_MINERALS_CONDENT); 
				if(plajjcRs.getInt("wkchaam") >= MineralsData.robberynum){
					PlayerBAC.getInstance().useCoin(dbHelper, playerid, MineralsData.buyrobbery, gl);
				}
				DBPaRs oppjjcRs = PlaJJCRankingBAC.getInstance().getDataRs(opppid);
				
				TeamBox teambox1 = PartnerBAC.getInstance().getTeamBox(playerid, 0, new JSONArray(posarrStr));
				TeamBox teambox2 = PartnerBAC.getInstance().getTeamBox(opppid, 1, new JSONArray(oppjjcRs.getString("wkdefform")));
				BattleBox battlebox = new BattleBox();
				battlebox.teamArr[0].add(teambox1);
				battlebox.teamArr[1].add(teambox2);
				BattleManager.createPVPBattle(battlebox);
				
				SqlString sqlStr = new SqlString();
				sqlStr.addChange("wkchaam", 1);
				long starttime = 0;
				if(battlebox.winTeam == Const.teamA){//ս��ʤ��
					DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
					if(myArr != null){//������һ��λ����
						issueAward(dbHelper, playerid, myArr.optInt(0), myArr.optLong(1), System.currentTimeMillis(), null, 2);
						useNumVec.remove((Integer)myArr.optInt(2));
					}
					issueAward(dbHelper, opppid, oppArr.optInt(0), oppArr.optLong(1), System.currentTimeMillis(), plaRs.getString("name"), 3);//���������һ��λ����
					
					starttime = System.currentTimeMillis();
					
					JSONArray new_myArr = new JSONArray();//�����ҵ�ռ����Ϣ
					new_myArr.add(diff);
					new_myArr.add(starttime);
					new_myArr.add(num);
					new_myArr.add(playerid);
					playertab.put(playerid, new_myArr);
					
					JSONArray new_oppArr = new JSONArray();//���¶��ֵ�ռ����Ϣ
					new_oppArr.add(0);
					new_oppArr.add(starttime);
					new_oppArr.add(0);
					new_oppArr.add(opppid);
					playertab.put(opppid, new_oppArr);
					
					PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_MINERALS_CONDENT, new_myArr.toString());
				} else {
					sqlStr.addDateTime("wkchatime", MyTools.getTimeStr());
				}
				PlaJJCRankingBAC.getInstance().update(dbHelper, playerid, sqlStr);
				
				JSONArray returnarr = new JSONArray();
				returnarr.add(starttime);
				returnarr.add(battlebox.replayData);
				
				gl.addRemark("�Ӻţ�"+num+" �����"+(battlebox.winTeam == Const.teamA?"�ɹ�":"ʧ��"));
				gl.save();
				return new ReturnValue(true, returnarr.toString());
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
			DBPaRs plajjcrankingRs = PlaJJCRankingBAC.getInstance().getDataRs(playerid);
			if(plajjcrankingRs.getString("wkdefform").equals(posarrStr)){
				BACException.throwInstance("�����ޱ仯");
			}
			JSONArray posarr = new JSONArray(posarrStr);
			PartnerBAC.getInstance().checkPosarr(playerid, posarr, 0, 1);//��������Ƿ�Ϸ�
			SqlString sqlStr = new SqlString();
			sqlStr.add("wkdefform", posarr.toString());
			PlaJJCRankingBAC.getInstance().update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ����������Ϣ
	 */
	public ReturnValue getInfo(int playerid){
		try {
			DBPaRs plajjcRs = PlaJJCRankingBAC.getInstance().getDataRs(playerid);
			JSONArray returnarr = new JSONArray();
			returnarr.add(plajjcRs.getInt("wkchaam"));//����ս����
			returnarr.add(new JSONArray(plajjcRs.getString("wkdefform")));//��������
			returnarr.add(plajjcRs.getTime("wkchatime"));//�����սʱ��
			returnarr.add(isRun?1:0);
			returnarr.add(playertab.get(playerid));//�����ڿ�����
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ��λ��Ϣ
	 */
	public ReturnValue getPosData(){
		try {
			if(!isRun){
				BACException.throwInstance("���ڻ��");
			}
			JSONArray returnarr = new JSONArray();
			JSONArray[] dataarr = playertab.values().toArray(new JSONArray[playertab.size()]);
			for(int i = 0; dataarr != null && i < dataarr.length; i++){
				if(dataarr[i].optInt(0) != 0){
					returnarr.add(dataarr[i]);
				}
			}
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��̨��ȡ��λ��Ϣ
	 */
	public ReturnValue bkGetPosData(){
		try {
			if(!isRun){
				BACException.throwInstance("���ڻ��");
			}
			StringBuffer[] strArr = new StringBuffer[3];
			strArr[0] = new StringBuffer();
			strArr[1] = new StringBuffer();
			strArr[2] = new StringBuffer();
			JSONArray[] dataarr = playertab.values().toArray(new JSONArray[playertab.size()]);
			for(int i = 0; dataarr != null && i < dataarr.length; i++){
				strArr[dataarr[i].optInt(0)].append(dataarr[i].optInt(3)+"(��λ��"+dataarr[i].optInt(2)+"��ʼʱ�䣺"+dataarr[i].optLong(1)+")");
			}
			String returnStr = "��������"+strArr[0] + "\r\n�м�����" + strArr[1] + "\r\n�߼�����" + strArr[2] + "\r\n��ռ�ÿ�λ��" + useNumVec;
			return new ReturnValue(true, returnStr);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ������Ϣ
	 */
	public ReturnValue getOwnerData(int targetpid){
		try {
			if(!isRun){
				BACException.throwInstance("���ڻ��");
			}
			JSONArray tgrArr = playertab.get(targetpid);
			if(tgrArr == null){
				BACException.throwInstance("�����δռ��");
			}
			DBPaRs plajjcRs = PlaJJCRankingBAC.getInstance().getDataRs(targetpid);
			
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(targetpid);
			JSONArray arr = new JSONArray();
			arr.add(targetpid);//ID
			arr.add(plaRs.getString("name"));//����
			arr.add(plaRs.getInt("lv"));//�ȼ�
			arr.add(plaRs.getInt("num"));//ͷ��
			arr.add(PartnerBAC.getInstance().getPlayerBattlePower(targetpid, new JSONArray(plajjcRs.getString("wkdefform")), plajjcRs.getInt("wkbattlepower")));//ս��
			return new ReturnValue(true, arr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ʼ������
	 */
	public void init(DBHelper dbHelper, int playerid) throws Exception {
		DBPaRs plaRs = PlaJJCRankingBAC.getInstance().getDataRs(playerid);
		SqlString sqlStr = new SqlString();
		sqlStr.add("wkdefform", plaRs.getString("defformation"));
		PlaJJCRankingBAC.getInstance().update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ���Ž���
	 */
	public void issueAward(DBHelper dbHelper, int playerid, int diff, long starttime, long endtime, String name, int from) throws Exception {
		int para = 0;
		for(int i = 0; i < MineralsData.awardpara.length; i++){
			if(Conf.worldLevel <= Tools.str2int(MineralsData.awardpara[i][0])){
				para = Tools.str2int(MineralsData.awardpara[i][1]);
				break;
			}
		}
		int money = (int)(para * (MineralsData.markon[diff] / 100d) * ((endtime-starttime) / MineralsData.rewardtime));//�������� * ���� * ʱ�����
		if(name != null){
			MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerid}, 10, null, new Object[]{name, money}, "3,"+money);
		} else {
			MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerid}, 9, null, new Object[]{money}, "3,"+money);
		}
	}
	
	/**
	 * �����
	 */
	public ReturnValue start(String from){
		try {
			if(isRun){
				BACException.throwInstance("����ڽ�����");
			}
			isRun = true;
			PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_MINERALS_START, "");
			Out.println(from+" �����ڿ�");
			return new ReturnValue(true, "�������");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �����
	 */
	public ReturnValue end(String from){
		DBHelper dbHelper = new DBHelper();
		try {
			if(!isRun){
				BACException.throwInstance("δ���л");
			}
			isRun = false;
			long endtime = System.currentTimeMillis();
			JSONArray[] dataarr = playertab.values().toArray(new JSONArray[playertab.size()]);
			for(int i = 0; dataarr != null && i < dataarr.length; i++){
				issueAward(dbHelper, dataarr[i].optInt(3), dataarr[i].optInt(0), dataarr[i].optLong(1), endtime, null, 4);
			}
			playertab.clear();
			useNumVec.clear();
			PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_MINERALS_END, "");
			Out.println(from+" ֹͣ�ڿ�");
			return new ReturnValue(true, "�������");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//------------------��̬��--------------------
	
	private static PlaMineralsBAC instance = new PlaMineralsBAC();

	public static PlaMineralsBAC getInstance() {
		return instance;
	}
}
