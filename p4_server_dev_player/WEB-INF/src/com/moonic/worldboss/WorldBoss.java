package com.moonic.worldboss;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;

import server.common.Tools;

import com.moonic.bac.AwardBAC;
import com.moonic.bac.CustomActivityBAC;
import com.moonic.bac.ItemBAC;
import com.moonic.bac.MailBAC;
import com.moonic.bac.PartnerBAC;
import com.moonic.bac.WorldBossBAC;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.SpriteBox;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.txtdata.WorldBossData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyLog;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;
import com.moonic.util.Out;

/**
 * ����BOSS
 * @author wkc 
 */
public class WorldBoss {
	public long startTime;//��ʼʱ��
	public long actiTimeLen;//�ʱ��
	
	public boolean inActi;//��Ƿ������
	
	public Boss boss;//����
	
	private Hashtable<Integer, WBPlayer> pla_tab;//��������
	
	private ScheduledExecutorService timer = MyTools.createTimer(3);//��ʱ��
	
	public MyLog log = new MyLog(MyLog.NAME_CUSTOM, "worldboss", "WORLDBOSS", true, false, true, MyTools.formatTime("yyyy-MM-dd-hh-mm-ss"));
	
	public static final String tab_world_boss_award_damage = "tab_world_boss_award_damage";
	public static final String tab_world_boss_award_rank = "tab_world_boss_award_rank";
	
	/**
	 * ����
	 */
	public WorldBoss(long actiTimeLen){
		this.actiTimeLen = actiTimeLen;
		this.pla_tab = new Hashtable<Integer, WBPlayer>();
	}
	
	/**
	 * ����
	 */
	public void start(){
		Out.println("����BOSS��ʼ");
		log.d("����BOSS����ʱ"+actiTimeLen/1000+"s");
		MyTimerTask endTT = new MyTimerTask() {
			public void run2() {
				DBHelper dbHelper = new DBHelper();
				try {
					dbHelper.openConnection();
					endHandle(dbHelper);
				} catch (Exception e){
					e.printStackTrace();
				} finally {
					dbHelper.closeConnection();
				}
			}
		};
		timer.schedule(endTT, actiTimeLen, TimeUnit.MILLISECONDS);
		startTime = System.currentTimeMillis();
		inActi = true;
		JSONArray pusharr = new JSONArray();
		pusharr.add(startTime);
		pusharr.add(actiTimeLen);
		PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_WORLD_BOSS_START, pusharr.toString());
	}
	
	/**
	 * ����
	 */
	public JSONArray join(int playerid) throws Exception{
		WBPlayer player = pla_tab.get(playerid);
		if(player == null){
			player = new WBPlayer(playerid);
			pla_tab.put(playerid, player);
			log.d("���"+player.name+"����������BOSS");
			GameLog.getInst(playerid, GameServlet.ACT_WORLD_BOSS_JOIN)
			.addRemark("��ң�"+GameLog.formatNameID(player.name, playerid)+"��������BOSS")
			.save();
		} 
		return getData(playerid);
	}
	
	/**
	 * ��ս
	 */
	public JSONArray toBattle(DBHelper dbHelper, int playerid, String posStr, GameLog gl) throws Exception{
		if(!inActi){
			BACException.throwInstance("����BOSS��ѽ���");
		}
		WBPlayer player = pla_tab.get(playerid);
		if(player == null){
			BACException.throwInstance("��Ҳ�������BOSS���");
		}
		if(player.chaTimes >= WorldBossData.chaTimes){
			BACException.throwInstance("��������BOSS����ս����������");
		}
		this.boss = new Boss();
		JSONArray posArr = new JSONArray(posStr);
		PartnerBAC.getInstance().checkPosarr(playerid, posArr, 1, 1);
		for(int i = 0; i < posArr.length(); i++){
			int partnerId = posArr.getInt(i);
			if(partnerId != 0 && player.partnerArr.contains(partnerId)){
				BACException.throwInstance("����Ѳ�ս��ID��"+partnerId+")");
			}
		}
		BattleBox battlebox = getBattleBox(playerid, posArr);
		BattleManager.createPVPBattle(battlebox);
		//�����˺�
		int damage = 0;
		for(int i = 0; i < boss.teamBox.sprites.size(); i++){
			SpriteBox spriteBox = boss.teamBox.sprites.get(i);
			damage += (spriteBox.battle_prop[Const.PROP_MAXHP] - spriteBox.battle_prop[Const.PROP_HP]);
		}
		//�����˺���ý���
		JSONArray returnarr = new JSONArray();
		String award = null;
		if(damage > 0){
			player.totalDamage += damage;
			DBPsRs damageListRs = DBPool.getInst().pQueryS(tab_world_boss_award_damage);
			while(damageListRs.next()){
				if(damage <= damageListRs.getLong("end")){
					award = damageListRs.getString("award");
					break;
				}
			}
		}
		JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
		returnarr.add(damage);//�˺�
		returnarr.add(award);//��������string
		returnarr.add(awardarr);//��������arr
		returnarr.add(battlebox.replayData);//ս��¼��
		//������ս����
		player.chaTimes++;
		//�����ѳ�ս���
		ArrayList<SpriteBox> mySprites = battlebox.teamArr[0].get(0).sprites;
		for(int i = 0; i < mySprites.size(); i++){
			player.partnerArr.add(mySprites.get(i).partnerId);
		}
		log.d("���" + player.name + "��"+ player.chaTimes + "����ս����BOSS���" + damage + "���˺�");
		gl.addRemark("��"+ player.chaTimes + "����ս��BOSS���" + damage + " ���˺�");
		return returnarr;
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getData(int playerid) throws Exception{
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(getRankData(playerid));
		WBPlayer player = pla_tab.get(playerid);
		if(player == null){
			player = new WBPlayer(playerid);
		}
		jsonarr.add(player.getData());
		return jsonarr;
	}
	
	/**
	 * ��ȡս��BatteBox
	 */
	public BattleBox getBattleBox(int playerid, JSONArray posArr) throws Exception{
		TeamBox myTeamBox = PartnerBAC.getInstance().getTeamBox(playerid, 0, posArr);
		BattleBox battleBox = new BattleBox();
		battleBox.bgnum = WorldBossData.bgNum;
		battleBox.teamArr[0].add(myTeamBox);
		battleBox.teamArr[1].add(boss.teamBox);
		return battleBox;
	}
	
	/**
	 * ���������
	 */
	public void endHandle(DBHelper dbHelper) throws Exception {
		inActi = false;
		log.d("------------����BOSS��������------------");
		//��������
		DBPsRs rankListRs = DBPool.getInst().pQueryS(tab_world_boss_award_rank);
		WBPlayer[] playerarr = pla_tab.values().toArray(new WBPlayer[pla_tab.size()]);
		Tools.sort(playerarr, 1);
		int begin = 0;//��ʼ���±�
		while(rankListRs.next()){
			int end = rankListRs.getInt("end");
			StringBuffer awardSb = new StringBuffer();
			awardSb.append(rankListRs.getString("award"));
			String paramStrAppend = CustomActivityBAC.getInstance().getFuncActiPara(CustomActivityBAC.TYPE_WORLDBOSS_OTHER_ITEM);
			if(paramStrAppend != null){
				String[] itemStrAppend = Tools.splitStr(paramStrAppend, "|");
				for(int i = 0; i < itemStrAppend.length; i++){
					String[] item = Tools.splitStr(itemStrAppend[i], "#");
					int odds = Tools.str2int(item[1]);
					int random = MyTools.getRandom(1, 1000);
					if(random < odds){
						awardSb.append("|"+item[0]);
					}
				}
			}
			while(begin < playerarr.length && begin < end && playerarr[begin].totalDamage > 0){
				MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerarr[begin].id}, 6, null, new Object[]{(begin+1)}, awardSb.toString());
				GameLog.getInst(playerarr[begin].id, GameServlet.ACT_WORLD_BOSS_AWARD_RANK)
				.addRemark("����˺�������"+(begin+1)+"��������"+awardSb.toString())
				.save();
				begin++;
			}
			if(begin >= playerarr.length || playerarr[begin].totalDamage == 0){
				break;
			}
		}
		WorldBossBAC.worldboss = null;
		log.save();
		Out.println("����BOSS����");
	}
	
	/**
	 * ��ȡ��������
	 */
	public JSONArray getRankData(int playerid){
		JSONArray rankarr = new JSONArray();
		WBPlayer[] playerarr = pla_tab.values().toArray(new WBPlayer[pla_tab.size()]);
		Tools.sort(playerarr, 1);
		for(int i = 0; i < playerarr.length; i++){
			if(i < 5){
				rankarr.add(playerarr[i].getData1());
			}
			if(playerarr[i].id == playerid){
				playerarr[i].rank = i+1; 
			}
		}
		return rankarr;
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
