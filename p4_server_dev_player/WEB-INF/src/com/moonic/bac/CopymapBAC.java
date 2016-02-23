package com.moonic.bac;

import java.util.ArrayList;

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
import com.moonic.servlet.GameServlet;
import com.moonic.socket.Player;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

/**
 * ����BAC
 * @author wkc
 */
public class CopymapBAC extends PlaStorBAC {
	public static final String tab_copymap = "tab_copymap";
	public static final String tab_copymap_dropaward = "tab_copymap_dropaward";
	public static final String tab_copymap_buytimes = "tab_copymap_buytimes";
	public static final String tab_copymap_map = "tab_copymap_map";
	
	/**
	 * ����
	 */
	public CopymapBAC() {
		super("tab_copymap_stor", "playerid", "bigmap");
	}
	
	/**
	 * ���븱��
	 */
	public ReturnValue enter(int playerid, int cmnum, String posStr){
		try {
			int amounts = ItemBAC.getInstance().getAmountByItemtype(playerid, ItemBAC.TYPE_EQUIP_ORDINARY, ItemBAC.ZONE_BAG);
			if (amounts > 50) {
				BACException.throwInstance("װ����������");
			}
			JSONArray posArr = new JSONArray(posStr);
			PartnerBAC.getInstance().checkPosarr(playerid, posArr, 1, 1);
			checkCondition(playerid, cmnum);
			BattleBox battlebox = getBattleBox(playerid, cmnum, posArr);
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			battlebox.parameterarr = new JSONArray().add(System.currentTimeMillis());
			pla.verifybattle_battlebox = battlebox;
			
			DBPaRs cmListRs = getCmListRs(cmnum);
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("����");
			remarkSb.append(cmListRs.getInt("cmtype") == 1 ? "��ͨ":"��Ӣ");
			remarkSb.append("����");
			remarkSb.append(GameLog.formatNameID(cmListRs.getString("name"), cmnum));
			GameLog.getInst(playerid, GameServlet.ACT_COPYMAP_ENTER)
			.addRemark(remarkSb)
			.save();
			return new ReturnValue(true, battlebox.getJSONArray().toString());
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ����ս��������֪ͨ
	 */
	public ReturnValue endChallenge(int playerid, int cmnum, String battleRecord){
		DBHelper dbHelper = new DBHelper();
		try {
			boolean haveStar = false;
			DBPaRs cmListRs = getCmListRs(cmnum);
			int pointType = cmListRs.getInt("pointtype");
			if(pointType == 2){
				haveStar = true;
			}
			int star = verifyBattle(playerid, battleRecord, haveStar);
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_COPYMAP_ENDCHALLENGE);
			JSONArray jsonarr = endBattle(dbHelper, playerid, cmnum, star , gl);
			
			gl.save();
			return new ReturnValue(true, jsonarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������ս����
	 */
	public ReturnValue buy(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs cmListRs = getCmListRs(num);
			int limit = cmListRs.getInt("limit");
			if(limit == -1){
				BACException.throwInstance("�˸����޴�������");
			}
			int bigmap = cmListRs.getInt("bigmap");
			DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
			if(!cmRs.exist()){
				BACException.throwInstance("�˸�����δ��ս��");
			}
			JSONObject timesobj = new JSONObject(cmRs.getString("dailytimes"));
			int times = timesobj.optInt(String.valueOf(num));
			if(times < limit){
				BACException.throwInstance("�˸�����ս��������ʣ��");
			} 
			JSONObject buyobj = new JSONObject(cmRs.getString("buy"));
			int buytimes = buyobj.optInt(String.valueOf(num));
			int need = 0;
			DBPsRs conListRs = DBPool.getInst().pQueryS(tab_copymap_buytimes);
			while(conListRs.next()){
				int[] rank = Tools.splitStrToIntArr(conListRs.getString("rank"), ",");
				if(buytimes+1 <= rank[1] || rank[1] == -1){
					need = conListRs.getInt("consume");
					break;
				}
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_COPYMAP_BUYTIMES);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, need, gl);
			buyobj.put(String.valueOf(num), buytimes+1);
			timesobj.put(String.valueOf(num), 0);
			SqlString sqlStr = new SqlString();
			sqlStr.add("dailytimes", timesobj.toString());
			sqlStr.add("buy", buyobj.toString());
			updateByKey(dbHelper, playerid, sqlStr, bigmap);
			
			gl.addRemark("���򸱱���"+GameLog.formatNameID(cmListRs.getString("name"), num)+"����ս����");
			gl.addChaNote("�������", buytimes, 1);
			gl.save();
			return new ReturnValue(true);
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ɨ������
	 * @param num,�������
	 * @param times,ɨ������
	 */
	public ReturnValue sweep(int playerid, int num, int times){
		DBHelper dbHelper = new DBHelper();
		try {
			int amounts = ItemBAC.getInstance().getAmountByItemtype(playerid, ItemBAC.TYPE_EQUIP_ORDINARY, ItemBAC.ZONE_BAG);
			if (amounts > 50) {
				BACException.throwInstance("װ����������");
			}
			DBPaRs cmListRs = getCmListRs(num);
			int pointType = cmListRs.getInt("pointtype");
			if(pointType == 1){
				BACException.throwInstance("�˸���ΪС�ؿ��������ظ���");
			}
			int limit = cmListRs.getInt("limit");
			int bigmap = cmListRs.getInt("bigmap");
			int cmtype = cmListRs.getInt("cmtype");
			DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
			if(!cmRs.exist()){
				BACException.throwInstance("�˸�����δ��ս��");
			}
			JSONObject passObj = new JSONObject(cmRs.getString("passed"));
			int star = passObj.optInt(String.valueOf(num));
			if(star == 0){
				BACException.throwInstance("�˸�����δͨ��");
			}
			if(star < 3){
				BACException.throwInstance("�˸�����δ3��ͨ��");
			}
			if(limit != -1){
				JSONObject timesobj = new JSONObject(cmRs.getString("dailytimes"));
				if(limit - timesobj.optInt(String.valueOf(num)) < times){
					BACException.throwInstance("�˸���ʣ����ս��������");
				} 
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_COPYMAP_SWEEP);
			int needEnergy = cmListRs.getInt("energy");
			PlaRoleBAC.getInstance().subValue(dbHelper, playerid, "energy", needEnergy*times, gl, "����");
			int dropnum = cmListRs.getInt("dropaward");
			DBPaRs dropListRs = DBPool.getInst().pQueryA(tab_copymap_dropaward, "num="+dropnum);
			if(!dropListRs.exist()){
				BACException.throwInstance("�����Ų�����");
			}
			String[] item = Tools.splitStr(dropListRs.getString("item"), "|");
			int[] odds = Tools.splitStrToIntArr(dropListRs.getString("odds"), ",");
			int[] control = Tools.splitStrToIntArr(dropListRs.getString("control"), ",");
			String paramStrDouble = CustomActivityBAC.getInstance().getFuncActiPara(cmtype == 1 ? CustomActivityBAC.TYPE_CM_DOUBLE : CustomActivityBAC.TYPE_CM_ELITE_DOUBLE);
			int param = 0;
			if(paramStrDouble != null){
				param = Tools.str2int(paramStrDouble);
			}
			int dropAm = 1;
			if(param == 1 || param == 3){
				doubleOddsArray(odds);
				dropAm = 2;
			} 
			JSONObject dropObj = new JSONObject(cmRs.getString("drops"));
			JSONArray dropArr = dropObj.optJSONArray(String.valueOf(num));
			dropArr = initDropsData(dropArr, control);
			JSONArray returnarr = new JSONArray();
			JSONArray contentarr = new JSONArray();//��������
			StringBuffer awardSb = new StringBuffer();
			String moneyaward = cmListRs.getString("moneyaward");//ͭǮ����
			String wipeaward = cmListRs.getString("wipeaward");//ɨ�����⽱��
			for(int i = 0; i < times; i++){
				if(i > 0){
					awardSb.append("|");
				}
				awardSb.append(moneyaward);
				awardSb.append("|");
				awardSb.append(wipeaward);
				dropArr.put(0, dropArr.getInt(0) + 1);
				JSONArray jsonarr = handleDropsAward(odds, control, dropArr, dropAm);//���ص�����
				JSONArray awardindex = jsonarr.getJSONArray(0);//�����±�
				StringBuffer oneSb = new StringBuffer();//ÿ�εĽ���
				for(int j = 0; j < awardindex.size(); j++){
					String itemStr = item[awardindex.getInt(j)];
					if(param == 2 || param == 3){
						itemStr = doubleItemAmount(itemStr);
					}
					if(j > 0){
						oneSb.append("|");
					}
					oneSb.append(itemStr);
					awardSb.append("|");
					awardSb.append(itemStr);
				}
				if(awardindex.size() == 0){//���⴦�����û����κν������͹̶���Ʒ
					oneSb.append("1,1,2,3");
					awardSb.append("|");
					awardSb.append("1,1,2,3");
				}
				//����׷�ӽ���
				String appendStr = getAppendItem(cmtype);
				if(appendStr.length() > 0){
					awardSb.append("|");
					awardSb.append(appendStr);
					oneSb.append("|");
					oneSb.append(appendStr);
				}
				contentarr.add(oneSb.toString());
				JSONArray dropsarr = jsonarr.getJSONArray(1);//�п��Ƶĵ������
				dropArr = handleDropsAfter(dropArr, dropsarr, control);
			}
			if(dropObj.optJSONArray(String.valueOf(num)) == null){
				dropObj.put(String.valueOf(num), dropArr);
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("drops", dropObj.toString());
			JSONObject timesObj = new JSONObject(cmRs.getString("dailytimes"));
			if(limit != -1){
				timesObj.put(String.valueOf(num), timesObj.optInt(String.valueOf(num))+times);
				sqlStr.add("dailytimes", timesObj.toString());
			}
			JSONObject passAmObj = new JSONObject(cmRs.getString("passedam"));
			int currAm = passAmObj.optInt(String.valueOf(num));
			if(currAm < 100){
				passAmObj.put(String.valueOf(num), currAm+times);
				sqlStr.add("passedam", passAmObj.toString());
			}
			updateByKey(dbHelper, playerid, sqlStr, bigmap);
			int exp = cmListRs.getInt("exp");
			PlayerBAC.getInstance().addExp(dbHelper, playerid, exp*times, gl);
			int[] money = Tools.splitStrToIntArr(moneyaward, ",");
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, cmtype == 1 ? PlaWelfareBAC.TYPE_COPYMAP_ORDINARY : PlaWelfareBAC.TYPE_COPYMAP_ELITE, times, gl);
			returnarr.add(contentarr);
			returnarr.add(awardarr);
			returnarr.add(wipeaward);
			returnarr.add(exp);
			returnarr.add(money[1]);
			
			StringBuffer remarkSb = new StringBuffer();
			remarkSb.append("ɨ��");
			remarkSb.append(cmtype == 1 ? "��ͨ":"��Ӣ");
			remarkSb.append("����");
			remarkSb.append(GameLog.formatNameID(cmListRs.getString("name"), num));
			remarkSb.append(times+"��");
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
	 * ��ȡ�Ǽ�����
	 * @param awardnum,�������1~3
	 */
	public ReturnValue getStarAward(int playerid, int bigmap, int awardnum){
		DBHelper dbHelper = new DBHelper();
		try {
			if(!(awardnum >= 1 && awardnum <= 3)){
				BACException.throwInstance("������Ŵ���");
			}
			DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
			if(!cmRs.exist()){
				BACException.throwInstance("�˵�ͼ��δ��ս��");
			}
			DBPaRs mapListRs = DBPool.getInst().pQueryA(tab_copymap_map, "num="+bigmap);
			if(!mapListRs.exist()){
				BACException.throwInstance("�����ڵĸ�����ͼ���"+bigmap);
			}
			JSONArray havedarr = new JSONArray(cmRs.getString("award"));
			if(havedarr.contains(awardnum)){
				BACException.throwInstance("�˽�������ȡ");
			}
			int need = mapListRs.getInt("star"+awardnum);
			JSONObject passObj = new JSONObject(cmRs.getString("passed"));
			int total = passObj.optInt("total");
			if(total < need){
				BACException.throwInstance("�˸�����������");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_COPYMAP_GETSTARAWARD);
			dbHelper.openConnection();
			String award = mapListRs.getString("award"+awardnum);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 1, gl);
			havedarr.add(awardnum);
			SqlString sqlStr = new SqlString();
			sqlStr.add("award", havedarr.toString());
			updateByKey(dbHelper, playerid, sqlStr, bigmap);
			
			gl.addRemark("��ȡ���ͼ���"+bigmap+"���Ǽ��������"+awardnum);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch(Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��սÿ�������Ĵ���
	 */
	public JSONArray endBattle(DBHelper dbHelper, int playerid, int num, int star, GameLog gl) throws Exception{
		DBPaRs cmListRs = getCmListRs(num);
		int limit = cmListRs.getInt("limit");
		int bigmap = cmListRs.getInt("bigmap");
		boolean isFirst = false;//��һ��ͨ��
		SqlString sqlStr = new SqlString();
		StringBuffer awardSb = new StringBuffer();//�����ַ���
		awardSb.append(cmListRs.getString("moneyaward"));
		DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
		JSONArray exparr = new JSONArray();
		boolean updateTimes = true;//�Ƿ���Ҫ���´���
		Player pla = SocketServer.getInstance().plamap.get(playerid);
		if(pla.verifybattle_battlebox.parameterarr.getLong(0) < MyTools.getCurrentDateLong()){//ս������ʱ�Ƿ����0��
			updateTimes = false;
		}
		int cmtype = cmListRs.getInt("cmtype");
		StringBuffer remarkSb = new StringBuffer();
		remarkSb.append("ͨ��");
		remarkSb.append(cmtype == 1 ? "��ͨ":"��Ӣ");
		remarkSb.append("����");
		remarkSb.append(GameLog.formatNameID(cmListRs.getString("name"), num));
		remarkSb.append(star > 0 ? star+"��":"���Ǽ�");
		if(!cmRs.exist()){
			isFirst = true;
			JSONObject passObj = new JSONObject();
			JSONObject passAmObj = new JSONObject();
			if(star != 0){
				passObj.put(String.valueOf(num), star);
				if(star > 0){
					passObj.put("total", star);
				}
				passAmObj.put(String.valueOf(num), 1);
			}
			JSONObject timesobj = new JSONObject();
			if(updateTimes){
				if(limit != -1){
					timesobj.put(String.valueOf(num), 1);
				}
			}
			JSONObject dropobj = new JSONObject();
			sqlStr.add("playerid", playerid);
			sqlStr.add("bigmap", bigmap);
			sqlStr.add("passed", passObj.toString());
			sqlStr.add("drops", dropobj.toString());
			sqlStr.add("dailytimes", timesobj.toString());
			sqlStr.add("buy", new JSONObject().toString());
			sqlStr.add("award", new JSONArray().toString());
			sqlStr.add("passedam", passAmObj.toString());
			insert(dbHelper, playerid, sqlStr);
		} else{
			if(updateTimes){
				JSONObject timesObj = new JSONObject(cmRs.getString("dailytimes"));
				if(limit != -1){
					timesObj.put(String.valueOf(num), timesObj.optInt(String.valueOf(num))+1);
					sqlStr.add("dailytimes", timesObj.toString());
				}
			}
			if(star != 0){//ʤ��
				JSONObject passAmObj = new JSONObject(cmRs.getString("passedam"));
				int currAm = passAmObj.optInt(String.valueOf(num));
				if(currAm < 100){
					passAmObj.put(String.valueOf(num), currAm+1);
					sqlStr.add("passedam", passAmObj.toString());
				}
				JSONObject passObj = new JSONObject(cmRs.getString("passed"));
				int oldStar = passObj.optInt(String.valueOf(num));
				if(oldStar == 0){
					isFirst = true;
				}
				if(oldStar == 0 || star > oldStar){//�Ǽ����ڵ�ǰ�Ǽ�ʱ����
					passObj.put(String.valueOf(num), star);
					if(star > oldStar){
						passObj.put("total", passObj.optInt("total")+(star-oldStar));
					}
					if(oldStar > 0){
						remarkSb.append(",�Ǽ��仯" + oldStar + "->" + star) ;
					}
					sqlStr.add("passed", passObj.toString());
				}
				if(!isFirst){
					int dropnum = cmListRs.getInt("dropaward");
					DBPaRs dropListRs = DBPool.getInst().pQueryA(tab_copymap_dropaward, "num="+dropnum);
					if(!dropListRs.exist()){
						BACException.throwInstance("�����Ų�����");
					}
					String[] item = Tools.splitStr(dropListRs.getString("item"), "|");
					int[] odds = Tools.splitStrToIntArr(dropListRs.getString("odds"), ",");
					int[] control = Tools.splitStrToIntArr(dropListRs.getString("control"), ",");
					String paramStrDouble = CustomActivityBAC.getInstance().getFuncActiPara(cmtype == 1 ? CustomActivityBAC.TYPE_CM_DOUBLE : CustomActivityBAC.TYPE_CM_ELITE_DOUBLE);
					int param = 0;
					if(paramStrDouble != null){
						param = Tools.str2int(paramStrDouble);
					}
					int dropAm = 1;
					if(param == 1 || param == 3){
						doubleOddsArray(odds);
						dropAm = 2;
					} 
					JSONObject dropObj = new JSONObject(cmRs.getString("drops"));
					JSONArray dropArr = dropObj.optJSONArray(String.valueOf(num));
					dropArr = initDropsData(dropArr, control);
					dropArr.put(0, dropArr.getInt(0) + 1);
					JSONArray jsonarr = handleDropsAward(odds, control, dropArr, dropAm);//���ص�������
					JSONArray awardindex = jsonarr.getJSONArray(0);//�����±�
					for(int i = 0; i < awardindex.size(); i++){
						awardSb.append("|");
						String itemStr = item[awardindex.getInt(i)];
						if(param == 2 || param == 3){
							itemStr = doubleItemAmount(itemStr);
						}
						awardSb.append(itemStr);
					}
					if(awardindex.size() == 0){//���⴦�����û����κν������͹̶���Ʒ
						awardSb.append("|");
						awardSb.append("1,1,2,3");
					}
					//����׷�ӽ���
					String appendStr = getAppendItem(cmtype);
					if(appendStr.length() > 0){
						awardSb.append("|");
						awardSb.append(appendStr);
					}
					JSONArray dropsarr = jsonarr.getJSONArray(1);//�п��Ƶĵ������
					dropArr = handleDropsAfter(dropArr, dropsarr, control);
					if(dropObj.optJSONArray(String.valueOf(num)) == null){
						dropObj.put(String.valueOf(num), dropArr);
					}
					sqlStr.add("drops", dropObj.toString());
				}
			}
			if(sqlStr.getColCount() > 0){
				updateByKey(dbHelper, playerid, sqlStr, bigmap);
			}
		}
		int exp = 0;//��������
		int exp1 = 0;//��龭��
		if(star != 0){
			int needEnergy = cmListRs.getInt("energy");
			PlaRoleBAC.getInstance().subValue(dbHelper, playerid, "energy", needEnergy, gl, "����");
			if(isFirst){//�״�ͨ�ؽ���
				String award = cmListRs.getString("firstaward");
				awardSb.append("|");
				awardSb.append(award);
				if(star > 0){
					remarkSb.append(",�״�ͨ���Ǽ�Ϊ"+star);
				}
			}
			exp = cmListRs.getInt("exp");
			exp1 = cmListRs.getInt("exp1");
			exparr.add(exp);
			exparr.add(exp1);
			PlayerBAC.getInstance().addExp(dbHelper, playerid, exp, gl);
			BattleBox battleBox = pla.verifybattle_battlebox;
			for(int i = 0; i< battleBox.teamArr[0].get(0).sprites.size(); i++){
				int partnerId = battleBox.teamArr[0].get(0).sprites.get(i).partnerId;
				if(partnerId != 0){
					PartnerBAC.getInstance().addExp(dbHelper, playerid, partnerId, exp1, gl);
				}
			}
		}
		JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
		PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, cmtype == 1 ? PlaWelfareBAC.TYPE_COPYMAP_ORDINARY : PlaWelfareBAC.TYPE_COPYMAP_ELITE, gl);
		CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 27, num);
		gl.addRemark(remarkSb);
		pla.verifybattle_battlebox = null;
		JSONArray returnarr = new JSONArray();
		returnarr.add(awardSb.toString());//��������
		returnarr.add(awardarr);//��������
		returnarr.add(exp);//��������
		returnarr.add(exp1);//��龭��
		returnarr.add(star);//�Ǽ�
		return returnarr;
	}
	
	/**
	 * ���ݵ����Ż�ý���
	 * @param oddsarr,���伸����ֵ
	 * @param control,���������ֵ
	 * @param droparr,[ѭ���еĴλ���,�ѵ���ĸ���..]
	 * @param dropAm,һ��ѭ���ڵĵ������
	 */
	public JSONArray handleDropsAward(int[] oddsarr, int[] control, JSONArray dropArr, int dropAm) throws Exception{
		int maxAm = 4;//TODO����������������������лʱ���6�����޻ʱ���4��
		JSONArray jsonarr = new JSONArray();
		JSONArray awardarr = new JSONArray();//�����±�
		JSONArray drop = new JSONArray();
		int j = 1;
		int times = dropArr.getInt(0);
		for(int i = 0; control != null && i < control.length; i++){
			if(awardarr.length() >= maxAm){
				break;
			}
			if(control[i] <= 0){//ֱ���������
				int random = MyTools.getRandom(1, 1000);
				if(random <= oddsarr[i]){
					awardarr.add(i);
				}
			} else{
				int droped = 0;//�Ƿ����
				int haveObtainAm = dropArr.getInt(j);
				double odd = (double)((times % control[i] != 0 ? times % control[i] : control[i])*(dropAm-haveObtainAm))/(double)control[i];
				double ran = Math.random();
				if(ran < odd){
					awardarr.add(i);
					droped = 1;
				}
				drop.add(droped);
				j++;
			}
		}
		jsonarr.add(awardarr);
		jsonarr.add(drop);
		return jsonarr;
	}
	
	/**
	 * ��ʼ����������
	 */
	public JSONArray initDropsData(JSONArray dropArr, int[] control) throws Exception{
		if(dropArr == null){//��ʼ�������¼
			dropArr = new JSONArray();
			dropArr.add(0);//�ڼ��ε���
			for(int i = 0; i < control.length; i++){
				if(control[i] != 0){
					dropArr.add(0);//�ѻ�õĸ���
				}
			}
		} else{
			int count = 0;//���������¼
			for(int i = 0; i < control.length; i++){
				if(control[i] != 0){
					count++;
				}
			}
			int addSize = count - (dropArr.size()-1);
			if(addSize > 0){
				for(int i = 0; i < addSize; i++){
					dropArr.add(0);
				}
			}
		} 
		return dropArr;
	}
	
	/**
	 * �����������¼
	 */
	public JSONArray handleDropsAfter(JSONArray dropArr, JSONArray dropsarr, int[] control) throws Exception{
		for(int j = 0; j < dropsarr.size(); j++){
			dropArr.put(j+1, dropArr.getInt(j+1) + dropsarr.getInt(j));
		}
		int index = 1;
		for(int j = 0; j < control.length; j++){
			if(control[j] != 0){
				if(dropArr.getInt(0) % control[j] == 0){
					dropArr.put(index, 0);
				}
				index++;
			}
		}
		int multi = 1;//����ѭ��������0
		for(int k = 0; k < control.length; k++){
			if(control[k] != 0){
				multi *= control[k];
			}
		}
		if((dropArr.getInt(0) % multi) == 0){
			dropArr.put(0, 0);
		}
		return dropArr;
	}
	
	/**
	 * �Լ������鷭��
	 */
	public void doubleOddsArray(int[] oddsarr){
		for(int i = 0; i < oddsarr.length; i++){
			oddsarr[i] *= 2;
		}
	}
	
	/**
	 * ����Ʒ�ַ���������������
	 */
	public String doubleItemAmount(String item){
		int index = item.lastIndexOf(",");
		StringBuffer itemSb = new StringBuffer();
		itemSb.append(item.substring(0, index+1));
		itemSb.append(Tools.str2int(item.substring(index+1))*2);
		return itemSb.toString();
	}
	
	/**
	 * ��ȡ����׷�ӽ�������
	 */
	public String getAppendItem(int cmtype) throws Exception{
		StringBuffer itemSb = new StringBuffer();
		String paramStrAppend = CustomActivityBAC.getInstance().getFuncActiPara(cmtype == 1 ? CustomActivityBAC.TYPE_CM_OTHER_ITEM : CustomActivityBAC.TYPE_CM_ELITE_OTHER_ITEM);
		if(paramStrAppend != null){
			String[] itemStr = Tools.splitStr(paramStrAppend, "|");
			for(int i = 0; i < itemStr.length; i++){
				String[] item = Tools.splitStr(itemStr[i], "#");
				int odds = Tools.str2int(item[1]);
				int random = MyTools.getRandom(1, 1000);
				if(random < odds){
					if(itemSb.length() > 0){
						itemSb.append("|");
					}
					itemSb.append(item[0]);
				}
			}
		}
		return itemSb.toString();
	}
	
	/**
	 * �����븱������
	 */
	public void checkCondition(int playerid, int cmnum) throws Exception{
		DBPaRs cmListRs = getCmListRs(cmnum);
		String condition = cmListRs.getString("condition");
		int[][] conditon = Tools.splitStrToIntArr2(condition, "|", ",");
		for(int i = 0; conditon != null && i < conditon.length; i++){
			int type = conditon[i][0];
			if(type == 1){//�ﵽ�����ȼ�
				int plv = PlayerBAC.getInstance().getIntValue(playerid, "lv");
				if(plv < conditon[i][1]){
					BACException.throwInstance("�����ȼ�δ�ﵽ����");
				}
			} else
			if(type == 2){//ͨ��ָ����Ÿ���
				boolean isPass = checkPass(playerid, conditon[i][1]);
				if(!isPass){
					BACException.throwInstance("��δͨ��ָ������"+conditon[i][1]);
				}
			}
		}
		//�ؿ�����
		int pointType = cmListRs.getInt("pointtype");
		if(pointType == 1){
			boolean isPass = checkPass(playerid, cmnum);
			if(isPass){
				BACException.throwInstance("�˸�����ͨ���������ظ���ս");
			}
		}
		//������������
		int limit = cmListRs.getInt("limit");
		int bigmap = cmListRs.getInt("bigmap");
		if(limit != -1){
			DBPaRs cmRs = CopymapBAC.getInstance().getDataRsByKey(playerid, bigmap);
			if(cmRs.exist()){
				JSONObject jsonobj = new JSONObject(cmRs.getString("dailytimes"));
				int times = jsonobj.optInt(String.valueOf(cmnum));
				if(times >= limit){
					BACException.throwInstance("�˸�����ս����������");
				}
			}
		}
		//��Ҫ����
		int needEnergy = cmListRs.getInt("energy");
		int energy = PlaRoleBAC.getInstance().getIntValue(playerid, "energy");
		if(needEnergy > energy){
			BACException.throwInstance("��������");
		}
	}
	
	/**
	 * ����Ƿ�ͨ��ָ������
	 */
	public boolean checkPass(int playerid, int cmnum) throws Exception{
		boolean isPass = false;
		DBPaRs cmListRs = getCmListRs(cmnum);
		int bigmap = cmListRs.getInt("bigmap");
		DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
		if(cmRs.exist()){
			JSONObject passObj = new JSONObject(cmRs.getString("passed"));
			if(passObj.optInt(String.valueOf(cmnum)) != 0){
				isPass = true;
			}
		}
		return isPass;
	}
	
	/**
	 * ��ȡ��ɫBatteBox
	 */
	public BattleBox getBattleBox(int playerid, int cmnum, JSONArray posArr) throws Exception{
		DBPaRs cmListRs = getCmListRs(cmnum);
		TeamBox myTeamBox = PartnerBAC.getInstance().getTeamBox(playerid, 0, posArr, null);
		int intopos = cmListRs.getInt("intopos");
		BattleBox battleBox = new BattleBox();
		battleBox.bgnum = cmListRs.getInt("scene");
		battleBox.teamArr[0].add(myTeamBox);
		for(int i = intopos; i <= 3; i++){
			String enemy = cmListRs.getString("enemy"+i);
			TeamBox enemyTeamBox = Enemy.getInstance().createTeamBox(enemy);
			battleBox.teamArr[1].add(enemyTeamBox);
		}
		return battleBox;
	}
	
	/**
	 * ��֤ս�����������Ǽ�
	 * @param haveStar���Ƿ����Ǽ�
	 */
	public int verifyBattle(int playerid, String battleRecord, boolean haveStar) throws Exception{
		Player pla = SocketServer.getInstance().plamap.get(playerid);
		if(pla.verifybattle_battlebox == null){
			BACException.throwInstance("���Ƚ�����ս");
		}
		BattleBox battlebox = pla.verifybattle_battlebox;
		ReturnValue rv = BattleManager.verifyPVEBattle(battlebox, battleRecord);
		if(!rv.success){
			BACException.throwInstance(rv.info);
		}
		if(battlebox.winTeam != Const.teamA){//ʧ��
			BACException.throwInstance("ͨ��ʧ��");
		}
		int star = 0;//�Ǽ�
		int deadam = 0;//����ս������˵�����
		if(haveStar){
			//�����Ǽ�
			ArrayList<SpriteBox> sprites = battlebox.teamArr[0].get(0).sprites;
			for(int i = 0; i< sprites.size(); i++){
				if(sprites.get(i).battle_prop[Const.PROP_HP] <= 0){
					deadam++;
				}
			}
			star = 3 - (deadam > 2 ? 2 : deadam);
		} else{//û���Ǽ�ʱΪ-1
			star = -1;
		}
		return star;
	}
	
	/**
	 * ��ȡָ����Ÿ�����ͨ������
	 */
	public int getPassedAm(int playerid, int num) throws Exception{
		int passedAm = 0;
		DBPaRs cmListRs = getCmListRs(num);
		int bigmap = cmListRs.getInt("bigmap");
		DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
		if(cmRs.exist()){
			JSONObject passAmObj = new JSONObject(cmRs.getString("passedam"));
			passedAm = passAmObj.optInt(String.valueOf(num));
		}
		return passedAm;
	}
	
	/**
	 * ���ø�����ÿ����ս����
	 */
	public void resetData(DBHelper dbHelper, int playerid) throws Exception{
		SqlString sqlStr = new SqlString();
		sqlStr.add("dailytimes", new JSONObject().toString());
		sqlStr.add("buy", new JSONObject().toString());
		update(dbHelper, playerid, sqlStr, "playerid="+playerid);
	}
	
	/**
	 * ��ȡ���������б�
	 */
	public DBPaRs getCmListRs(int cmnum) throws Exception{
		DBPaRs cmListRs = DBPool.getInst().pQueryA(tab_copymap, "num="+cmnum);
		if(!cmListRs.exist()){
			BACException.throwInstance("�����ڵĸ������"+cmnum);
		}
		return cmListRs;
	}
	
	/**
	 * ��½ʱ��ȡ��������
	 */
	public JSONObject getLoginData(int playerid) throws Exception{
		JSONObject jsonObj = new JSONObject();
		DBPsRs cmRs = query(playerid, "playerid="+playerid);
		while(cmRs.next()){
			JSONObject bigmapObj = new JSONObject();
			bigmapObj.put("passed", new JSONObject(cmRs.getString("passed")));//��ͨ���ĸ�����ź�����
			bigmapObj.put("dailytimes", new JSONObject(cmRs.getString("dailytimes")));//��������ÿ����ս����
			bigmapObj.put("buy", new JSONObject(cmRs.getString("buy")));//������ÿ�չ������
			bigmapObj.put("award", new JSONArray(cmRs.getString("award")));//����ȡ���Ǽ�����
			bigmapObj.put("passedam", new JSONObject(cmRs.getString("passedam")));//��������ͨ�ش���
			jsonObj.put(String.valueOf(cmRs.getInt("bigmap")), bigmapObj);
		}
		return jsonObj;
	}
	
	//--------------------������------------------
	
	/**
	 * һ��ͨ����������
	 */
	public ReturnValue debugOneKeyPass(int playerid, int start, int end){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			String where = null;
			if(end != 0){
				where = "num>="+start+" and num<="+end;
			}
			DBPsRs cmListRs = DBPool.getInst().pQueryS(tab_copymap, where);
			while(cmListRs.next()){
				int bigmap = cmListRs.getInt("bigmap");
				DBPaRs cmRs = getDataRsByKey(playerid, bigmap);
				int star = -1;
				int pointType = cmListRs.getInt("pointtype");
				if(pointType == 2){
					star = 3;
				}
				int num = cmListRs.getInt("num");
				SqlString sqlStr = new SqlString();
				if(cmRs.exist()){
					JSONObject passObj = new JSONObject(cmRs.getString("passed"));
					if(passObj.optInt(String.valueOf(num)) == 0){
						passObj.put(String.valueOf(num), star);
						if(star != -1){
							passObj.put("total", passObj.optInt("total")+3);
						}
						sqlStr.add("passed", passObj.toString());
						updateByKey(dbHelper, playerid, sqlStr, bigmap);
					}
				} else{
					JSONObject passObj = new JSONObject();
					passObj.put(String.valueOf(num), star);
					if(star != -1){
						passObj.put("total", star);
					}
					sqlStr.add("playerid", playerid);
					sqlStr.add("bigmap", bigmap);
					sqlStr.add("passed", passObj.toString());
					sqlStr.add("drops", new JSONObject().toString());
					sqlStr.add("dailytimes", new JSONObject().toString());
					sqlStr.add("buy", new JSONObject().toString());
					sqlStr.add("award", new JSONArray().toString());
					sqlStr.add("passedam", new JSONObject().toString());
					insert(dbHelper, playerid, sqlStr);
				}
			}
			
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���������ս��¼
	 */
	public ReturnValue debugClearRecord(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			delete(dbHelper, playerid, "playerid="+playerid);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	//--------------------��̬��------------------
	
	private static CopymapBAC instance = new CopymapBAC();
	
	public static CopymapBAC getInstance(){
		return instance;
	}
}
