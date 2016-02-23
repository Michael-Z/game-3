package com.moonic.bac;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.SpriteBox;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.Player;
import com.moonic.socket.SocketServer;
import com.moonic.txtdata.TowerData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;
import server.common.Tools;

/**
 * ��ɫ�ֻ���
 * @author wkc
 */
public class PlaTowerBAC extends PlaBAC {
	public static final String tab_tower_npc = "tab_tower_npc";
	public static final String tab_tower_layer = "tab_tower_layer";
	public static final String tab_tower_score = "tab_tower_score";
	
	/**
	 * ����
	 */
	public PlaTowerBAC() {
		super("tab_pla_tower", "playerid");
		needcheck = false;
	}

	/**
	 * ��ʼ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		JSONArray ranArr = randomBossData(playerid, 1);
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.add("layer", 0);
		sqlStr.add("diff", 0);
		sqlStr.add("bossdata", ranArr.toString());
		sqlStr.add("bosshp", new JSONArray().toString());
		sqlStr.add("dead", new JSONArray().toString());
		sqlStr.add("score", 0);
		insert(dbHelper, playerid, sqlStr);
		((JSONObject)parm[0]).put("tower", getBossData(ranArr, 0, null));//������������ݼ��뵽�������ܵķ��ؽ����
	}
	
	/**
	 * ������ս
	 * @param layer,Ҫ��ս�Ĳ���
	 * @param diff,���׳̶�
	 * @param posStr,��������
	 */
	public ReturnValue enter(int playerid, int layer, byte diff, String posStr){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaTowerRs = getDataRs(playerid);
			if(!plaTowerRs.exist()){
				BACException.throwInstance("�ֻ�����δ����");
			}
			DBPaRs towerListRs = DBPool.getInst().pQueryA(tab_tower_layer, "layer="+layer);
			if(!towerListRs.exist()){
				BACException.throwInstance("�����ڵ�����"+layer);
			}
			long current = System.currentTimeMillis();
			if(current >= MyTools.getTimeLong(MyTools.getDateStr() + TowerData.forbiddenstarttime) 
					&& current <= MyTools.getTimeLong(MyTools.getDateStr() + TowerData.forbiddenendtime)){
				BACException.throwInstance("��ʱ����ڲ�������ս");
			}
			if(diff < 1 || diff > 3){
				BACException.throwInstance("�ѶȲ�������");
			}
			int passLayer = plaTowerRs.getInt("layer");
			if(layer != passLayer+1){
				BACException.throwInstance("��ս�����뵱ǰ���Ƚ�����");
			}
			int currDiff = plaTowerRs.getInt("diff");
			if(currDiff != 0 && diff != currDiff){
				BACException.throwInstance("֮ǰ�Ѷ���δͨ��");
			}
			JSONArray posArr = new JSONArray(posStr);
			PartnerBAC.getInstance().checkPosarr(playerid, posArr, 1, 1);
			JSONArray deadArr = new JSONArray(plaTowerRs.getString("dead"));
			for(int i = 0; i < deadArr.size(); i++){
				int deadId = deadArr.getInt(i);
				if(posArr.contains(deadId)){
					BACException.throwInstance("���ID��"+deadId+"��������");
				}
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TOWER_ENTER);
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			BattleBox battleBox = getBattleBox(playerid, layer, diff, posArr, gl);
			pla.verifybattle_battlebox = battleBox;
			if(currDiff == 0){
				SqlString sqlStr = new SqlString();
				sqlStr.add("diff", diff);
				update(dbHelper, playerid, sqlStr);
			}
			
			gl.save();
			return new ReturnValue(true, battleBox.getJSONArray().toString());
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������ս
	 */
	public ReturnValue end(int playerid, String battleRecord){
		DBHelper dbHelper = new DBHelper();
		try {
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			BattleBox battleBox = pla.verifybattle_battlebox;
			if(battleBox == null){
				BACException.throwInstance("���Ƚ�����ս");
			}
			int layer = battleBox.parameterarr.optInt(0);
			int diff = battleBox.parameterarr.optInt(1);
			DBPaRs plaTowerRs = getDataRs(playerid);
			JSONArray deadArr = new JSONArray(plaTowerRs.getString("dead"));
			JSONArray hpArr = new JSONArray(plaTowerRs.getString("bosshp"));
			ReturnValue rv = BattleManager.verifyPVEBattle(battleBox, battleRecord);
			if(!rv.success){
				BACException.throwInstance("��֤ս��ʧ��");
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_TOWER_END);
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("��ս����"+layer+"�Ѷ�"+diff);
			SqlString sqlStr = new SqlString();
			//�����Ǽ�,�������������Ļ��
			int deadam = 0;//�����������
			ArrayList<SpriteBox> sprites = battleBox.teamArr[0].get(0).sprites;
			for(int i = 0; i < sprites.size(); i++){
				if(sprites.get(i).battle_prop[Const.PROP_HP] <= 0){
					deadam++;
					deadArr.add(sprites.get(i).partnerId);
				}
			}
			if(deadam > 0){
				sqlStr.add("dead", deadArr.toString());
			}
			JSONArray returnarr = new JSONArray();
			if(battleBox.winTeam == Const.teamA){//ʤ��
				int star = 3 - (deadam > 2 ? 2 : deadam);
				JSONArray bossArr = new JSONArray();
				DBPaRs towerListRs = DBPool.getInst().pQueryA(tab_tower_layer, "layer="+(layer+1));
				if(towerListRs.exist()){
					bossArr = randomBossData(playerid, layer+1);
				}
				sqlStr.add("layer", layer);
				sqlStr.add("diff", 0);
				sqlStr.add("bossdata", bossArr.toString());
				//��ý���
				JSONArray awardArr = getAward(dbHelper, playerid, layer, (byte)diff, gl);
				//�����Ǽ���û���
				int oldScore = plaTowerRs.getInt("score");
				int score = TowerData.basicArr[diff-1] * TowerData.starArr[star-1];
				sqlStr.addChange("score", score);
				hpArr = new JSONArray();
				returnarr.add(star);//�Ǽ�
				returnarr.add(score);//��û���
				returnarr.add(awardArr);//��������
				returnarr.add(getBossData(bossArr, 0, null));//ˢ�µ�boss����
				remarkSb.append("�ɹ�������Ǽ�"+star+"�����ֱ仯"+oldScore+"->"+(oldScore+score));
			} else{
				JSONArray hpPerArr = new JSONArray();//ʣ��Ѫ���ٷֱ�
				ArrayList<SpriteBox> enemySprites = battleBox.teamArr[1].get(0).sprites;
				for(int i = 0; i< enemySprites.size(); i++){
					SpriteBox spritebox = enemySprites.get(i);
					int index = spritebox.posNum-1;
					int currHp = spritebox.battle_prop[Const.PROP_HP];
					int maxHp = spritebox.battle_prop[Const.PROP_MAXHP];
					hpArr.put(index, currHp);
					hpPerArr.put(index, MyTools.formatNum(((double)currHp) / ((double)maxHp)));
				}
				returnarr.add(hpPerArr);//bossѪ������
				remarkSb.append("ʧ�ܣ�ʣ��Ѫ������"+hpArr);
			}
			returnarr.add(deadArr);//���������
			sqlStr.add("bosshp", hpArr.toString());
			update(dbHelper, playerid, sqlStr);
			pla.verifybattle_battlebox = null;
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_TOWER, gl);
			
			gl.addRemark(remarkSb);
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���ݲ������ѶȻ�ý���
	 */
	public JSONArray getAward(DBHelper dbHelper, int playerid, int layer, byte diff, GameLog gl) throws Exception{
		StringBuffer awardSb = new StringBuffer();
		DBPaRs towerListRs = getTowerListRs(layer);
		String basic = towerListRs.getString("basic"+diff);
		awardSb.append(basic);
		String[] itemStr = Tools.splitStr(towerListRs.getString("random"+diff), "|");
		int[] amarr = Tools.splitStrToIntArr(towerListRs.getString("amount"+diff), ","); 
		int amount = MyTools.getRandom(amarr[0], amarr[1]);
		String paramStr = CustomActivityBAC.getInstance().getFuncActiPara(CustomActivityBAC.TYPE_TOWER_MORE_ITEM);
		if(paramStr != null){
			int param = Tools.str2int(paramStr);
			amount += param;
		} 
		int[] oddsarr = Tools.splitStrToIntArr(towerListRs.getString("odds"+diff), ",");
		for(int i = 0; i < amount; i++){
			awardSb.append("|");
			int index = MyTools.getIndexOfRandom(oddsarr);
			int[] split = Tools.splitStrToIntArr(itemStr[index], ",");
			String item = Tools.combineInt((int[])ItemBAC.getInstance().enterItem(split, "towerodds")[0], ",");
			awardSb.append(item);
			oddsarr[index] = 0;
		}
		String paramStrAppend = CustomActivityBAC.getInstance().getFuncActiPara(CustomActivityBAC.TYPE_TOWER_OTHER_ITEM);
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
		JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
		JSONArray returnarr = new JSONArray();
		returnarr.add(awardSb.toString());
		returnarr.add(awardarr);
		return returnarr;
	}
	
	/**
	 * �����������
	 */
	public JSONArray randomBossData(int playerid, int layer) throws Exception{
		JSONArray retrunArr = new JSONArray();
		int plv = PlayerBAC.getInstance().getIntValue(playerid, "lv");
		DBPaRs towerListRs = getTowerListRs(layer);
		for(int i = 1; i <= 3; i++){
			int[] lvarr = Tools.splitStrToIntArr(towerListRs.getString("lv"+i), ",");
			int npclv = plv + MyTools.getRandom(lvarr[0], lvarr[1]);
			int npctype = TowerData.typeArr[i-1];
			DBPaRs npcListRs = DBPool.getInst().pQueryA(tab_tower_npc, "npclv="+npclv + " and npctype="+npctype);
			if(!npcListRs.exist()){
				BACException.throwAndPrintInstance("npc�ȼ�"+npclv+"������"+npctype+"���ݴ���");
			}
			int[][] partnerNum = CBBAC.getInstance().createNPCTeamBox_1(npcListRs);
			JSONArray bossArr = new JSONArray();
			bossArr.add(npclv);
			JSONArray boss = new JSONArray();
			boss.add(new JSONArray(partnerNum[0]));
			boss.add(new JSONArray(partnerNum[1]));
			bossArr.add(boss);//����ĵ���
			retrunArr.add(bossArr);
		}
		return retrunArr;
	}
	
	/**
	 * ��ȡ����TeamBox
	 */
	public TeamBox getBossTeamBox(JSONArray numArr, int npclv, int npctype) throws Exception{
		DBPaRs npcListRs = DBPool.getInst().pQueryA(tab_tower_npc, "npclv="+npclv + " and npctype="+npctype);
		if(!npcListRs.exist()){
			BACException.throwAndPrintInstance("npc�ȼ�"+npclv+"������"+npctype+"���ݴ���");
		}
		int[][] numarr = new int[2][3];
		for(int i = 0; i < numArr.length(); i++){
			JSONArray linearr = numArr.getJSONArray(i);
			for(int j = 0; j < linearr.length(); j++){
				numarr[i][j] = linearr.getInt(j);
			}
		}
		return CBBAC.getInstance().createNPCTeamBox_2("npc�ȼ�"+npclv+"����"+npctype, npcListRs, numarr);
	}
	
	/**
	 * ��ȡ��ɫBatteBox
	 */
	public BattleBox getBattleBox(int playerid, int layer, byte diff, JSONArray posArr, GameLog gl) throws Exception{
		DBPaRs plaTowerRs = getDataRs(playerid);
		JSONArray bossArr = new JSONArray(plaTowerRs.getString("bossdata"));
		BattleBox battleBox = new BattleBox();
		TeamBox myTeamBox = PartnerBAC.getInstance().getTeamBox(playerid, 0, posArr);
		JSONArray boss = bossArr.getJSONArray(diff-1);
		TeamBox enemyTeamBox = getBossTeamBox(boss.getJSONArray(1), boss.getInt(0), TowerData.typeArr[diff-1]);
		//����Ѫ��
		JSONArray hpArr = new JSONArray(plaTowerRs.getString("bosshp"));
		if(hpArr.size() > 0){
			ArrayList<SpriteBox> enemySprites = enemyTeamBox.sprites;
			for(int i = 0; i < enemySprites.size(); ){
				SpriteBox spritebox = enemySprites.get(i);
				int index = spritebox.posNum-1;
				int currHp = hpArr.optInt(index);
				if(currHp <= 0){
					enemySprites.remove(i);
				} else{
					spritebox.battle_prop[Const.PROP_HP] = currHp;
					i++;
				}
			}
		}
		int bgnum = 103;
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if(hour < 6 || hour > 18){
			bgnum = 104;
		}
		battleBox.bgnum = bgnum;
		battleBox.teamArr[0].add(myTeamBox);
		battleBox.teamArr[1].add(enemyTeamBox);
		battleBox.parameterarr = new JSONArray(new int[]{layer, diff});
		gl.addRemark("��ʼ��ս��"+layer+"�㣬�Ѷ�"+diff+",npc�ȼ�"+boss.getInt(0));
		return battleBox;
	}
	
	/**
	 * ��ȡ�����������
	 */
	public JSONArray getBossData(JSONArray bossArr, int diff, JSONArray hpArr) throws Exception{
		JSONArray bossarr = new JSONArray();//boss����
		JSONArray hparr = new JSONArray();//ʣ��Ѫ���ٷֱ�
		for(int i = 0; i < bossArr.length(); i++){
			JSONArray boss = bossArr.getJSONArray(i);
			TeamBox teamBox = getBossTeamBox(boss.getJSONArray(1), boss.getInt(0), TowerData.typeArr[i]);
			int battlePower = PartnerBAC.getInstance().getTeamBoxBattlePower(teamBox);
			JSONArray one = new JSONArray();
			one.add(boss.getJSONArray(1));
			one.add(boss.getInt(0));
			one.add(battlePower);
			bossarr.add(one);
			if(i == diff-1){
				ArrayList<SpriteBox> enemySprites = teamBox.sprites;
				for(int j = 0; j < enemySprites.size(); j++){
					SpriteBox spritebox = enemySprites.get(j);
					int index = spritebox.posNum-1;
					int currHp = hpArr.optInt(index);
					int maxHp = spritebox.battle_prop[Const.PROP_MAXHP];
					hparr.put(index, MyTools.formatNum(((double)currHp) / ((double)maxHp)));
				}
			}
		}
		JSONArray returnarr = new JSONArray();
		returnarr.add(bossarr);
		returnarr.add(hparr);
		return returnarr;
	}
	
	/**
	 * ��ȡ�ֻ����������б�
	 */
	public DBPaRs getTowerListRs(int layer) throws Exception{
		DBPaRs towerListRs = DBPool.getInst().pQueryA(tab_tower_layer, "layer="+layer);
		if(!towerListRs.exist()){
			BACException.throwInstance("�����ڵ�����"+layer);
		}
		return towerListRs;
	}
	
	/**
	 * ���ͻ��ֽ���
	 * @throws Exception 
	 */
	public void sendAward(DBHelper dbHelper) throws Exception{
		DBPsRs scoreListRs = DBPool.getInst().pQueryS(tab_tower_score);
		ResultSet scoreRs = dbHelper.query("tab_pla_tower", "score,playerid", "serverid="+Conf.sid+" and score >"+0);
		while(scoreRs.next()){
			try {
				int score = scoreRs.getInt("score");
				scoreListRs.beforeFirst();
				while(scoreListRs.next()){
					if(score >= scoreListRs.getInt("begin") && score <= scoreListRs.getInt("end")){
						String award = scoreListRs.getString("award");
						int playerid = scoreRs.getInt("playerid");
						MailBAC.getInstance().sendModelMail(dbHelper, new int[]{playerid}, 7, null, new Object[]{(score)}, award);
						GameLog.getInst(playerid, GameServlet.ACT_TOWER_AWARD)
						.addRemark("�ۼƻ�û���"+score+"������"+award)
						.save();
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ����ÿ���ֻ�������
	 */
	public void resetData(DBHelper dbHelper, int playerid, JSONObject jsonobj) throws Exception {
		DBPaRs plaTowerRs = getDataRs(playerid);
		if(!plaTowerRs.exist()){//����δ����ʱֱ�ӷ���
			return;
		}
		JSONArray ranArr = randomBossData(playerid, 1);
		SqlString sqlStr = new SqlString();
		sqlStr.add("layer", 0);
		sqlStr.add("diff", 0);
		sqlStr.add("bossdata", ranArr.toString());
		sqlStr.add("bosshp", new JSONArray().toString());
		sqlStr.add("dead", new JSONArray().toString());
		sqlStr.add("score", 0);
		update(dbHelper, playerid, sqlStr);
		jsonobj.put("tower", getBossData(ranArr, 0, null));
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getData(int playerid) throws Exception {
		DBPaRs plaTowerRs = getDataRs(playerid);
		if(!plaTowerRs.exist()){
			return null;
		}
		int layer = plaTowerRs.getInt("layer");
		int diff = plaTowerRs.getInt("diff");
		int score = plaTowerRs.getInt("score");
		JSONArray deadArr = new JSONArray(plaTowerRs.getString("dead"));
		JSONArray bossArr = new JSONArray(plaTowerRs.getString("bossdata"));
		JSONArray bossHp = new JSONArray(plaTowerRs.getString("bosshp"));
		JSONArray bossdata = getBossData(bossArr, diff, bossHp);
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(layer);//������ͨ���Ĳ���
		jsonarr.add(bossdata.getJSONArray(1));//��ǰbossʣ��Ѫ��
		jsonarr.add(deadArr);//��ǰ�������Ļ��
		jsonarr.add(score);//�����ѻ�û���
		jsonarr.add(bossdata.getJSONArray(0));//���boss����
		jsonarr.add(diff);//��ǰ��ս�Ѷ�
		return jsonarr;
	}
	
	//------------------��̬��------------------
	
	private static PlaTowerBAC instance = new PlaTowerBAC();
	
	public static PlaTowerBAC getInstance(){
		return instance;
	}
}
