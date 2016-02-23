package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.GamePushData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ��ͨװ��
 * @author John
 */
public class EquipOrdinaryBAC extends EquipBAC {
	public static final String tab_equip_stre = "tab_equip_stre";
	public static final String tab_equip_upstar = "tab_equip_upstar";
	public static final String tab_equip_smelt = "tab_equip_smelt";
	
	/**
	 * ����
	 */
	public static final byte EQ_WEAPON = 1;
	/**
	 * ͷ��
	 */
	public static final byte EQ_HELMET = 2;
	/**
	 * �·�
	 */
	public static final byte EQ_CLOTHES = 3;
	/**
	 * Ь��
	 */
	public static final byte EQ_SHOES = 4;
	/**
	 * ��ָ
	 */
	public static final byte EQ_RING = 5;
	/**
	 * ����
	 */
	public static final byte EQ_TREASURE = 6;
	
	/**
	 * ����
	 */
	public EquipOrdinaryBAC() {
		super("tab_equip_stor", "playerid", "itemid");
	}
	
	/**
	 * ǿ��
	 */
	public ReturnValue stre(int playerid, int itemid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs equipStorRs = getDataRsByKey(playerid, itemid);
			if(!equipStorRs.exist()){
				BACException.throwInstance("װ��δ�ҵ�");
			}
			int strelv = equipStorRs.getInt("strelv");
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			if(strelv >= plaRs.getInt("lv")){
				BACException.throwInstance("װ��ǿ���ȼ����ܳ�����ҵȼ�");
			}
			DBPaRs streRs = DBPool.getInst().pQueryA(tab_equip_stre, "strelv="+(strelv+1));
			if(!streRs.exist()){
				BACException.throwInstance("����������");
			}
			DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_EQUIP_STRE);
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, streRs.getInt("q"+equipRs.getInt("rare")), gl);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("strelv", 1);
			updateByKey(dbHelper, playerid, sqlStr, itemid);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_EQUIP_STRENGTHEN, gl);
			
			gl.addChaNote(GameLog.formatNameID(equipRs.getString("name"), itemid)+" ǿ���ȼ�", strelv, 1);
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
	 * ����
	 */
	public ReturnValue upStar(int playerid, int itemid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs equipStorRs = getDataRsByKey(playerid, itemid);
			if(!equipStorRs.exist()){
				BACException.throwInstance("װ��δ�ҵ�");
			}
			DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
			int rare = equipRs.getInt("rare");
			if(rare < 4){
				BACException.throwInstance("װ���޷�����");
			}
			int starlv = equipStorRs.getInt("starlv");
			int nextStar = starlv+1;
			DBPaRs starRs = DBPool.getInst().pQueryA(tab_equip_upstar, "star="+nextStar);
			if(!starRs.exist()){
				BACException.throwInstance("����������");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_EQUIP_UPSTAR);
			int needmoney = starRs.getInt("needmoney");
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, needmoney, gl);
			JSONArray itemarr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_EQUIP_DEBRIS, equipStorRs.getInt("num"), starRs.getInt("q"+rare), ItemBAC.ZONE_BAG, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("starlv", 1);
			updateByKey(dbHelper, playerid, sqlStr, itemid);
			
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			GamePushData.getInstance(1)
			.add(plaRs.getString("name"))
			.add(equipRs.getString("name"))
			.add(nextStar)
			.sendToAllOL();
			
			gl.addChaNote(GameLog.formatNameID(equipRs.getString("name"), itemid)+" �Ǽ�", starlv, 1);
			gl.addItemChaNoteArr(itemarr);
			gl.save();
			return new ReturnValue(true, itemarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���
	 */
	public ReturnValue dismantle(int playerid, int itemid){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_EQUIP_DISMANTLE);
			DBPaRs equipStorRs = getDataRsByKey(playerid, itemid);
			if(!equipStorRs.exist()){
				BACException.throwInstance("װ��δ�ҵ�");
			}
			DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
			if(equipRs.getString("need").equals("0")){
				BACException.throwInstance("����Ʒ�޷����");
			}
			int[] needdata = Tools.splitStrToIntArr(equipRs.getString("need"), ",");
			int addamount = needdata[0];
			int starlv = equipStorRs.getInt("starlv");
			if(starlv > 0){
				DBPsRs upStarRs = DBPool.getInst().pQueryS(tab_equip_upstar, "star<="+starlv);
				addamount += upStarRs.sum("q"+equipRs.getInt("rare"));
			}
			if(equipStorRs.getInt("strelv") > 0){
				DBPsRs equipStreRs = DBPool.getInst().pQueryS(tab_equip_stre, "strelv<="+equipStorRs.getInt("strelv"));
				int stremoney = (int)equipStreRs.sum("q"+equipRs.getInt("rare"));
				PlayerBAC.getInstance().addValue(dbHelper, playerid, "money", stremoney, gl, GameLog.TYPE_MONEY);
			}
			JSONObject obj = ItemBAC.getInstance().remove(dbHelper, playerid, itemid, ItemBAC.ZONE_BAG, gl);
			JSONArray itemarr = ItemBAC.getInstance().add(dbHelper, playerid, ItemBAC.TYPE_EQUIP_DEBRIS, equipStorRs.getInt("num"), addamount, ItemBAC.ZONE_BAG, ItemBAC.SHORTCUT_MAIL, 1, gl);
			itemarr.add(obj);
			
			gl.addItemChaNoteArr(itemarr);
			gl.save();
			return new ReturnValue(true, itemarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����
	 */
	public ReturnValue smelt(int playerid, String itemidStr){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_EQUIP_SMELT);
			int[] itemidarr = Tools.splitStrToIntArr(itemidStr, ",");
			if(itemidarr == null){
				BACException.throwInstance("Ҫ�ںϵ�װ��δָ��");
			}
			DBPsRs equipStorRs = query(playerid, "playerid="+playerid+" and ("+MyTools.converWhere("or", "itemid", "=", itemidarr)+")");
			if(equipStorRs.count() < itemidarr.length){
				BACException.throwInstance("Ҫ�ں�װ��������Ч����");
			}
			int rare = 0;
			int stremoney = 0;
			while(equipStorRs.next()){
				DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
				if(rare == 0){
					rare = equipRs.getInt("rare");
					if(rare == 5){
						BACException.throwInstance("��ɫװ���޷�����");
					}
				} else {
					if(equipRs.getInt("rare") != rare){
						BACException.throwInstance("Ҫ�ںϵ�װ��Ʒ�ʲ�һ��");
					}
				}
				if(equipStorRs.getInt("starlv") > 0){
					BACException.throwInstance("���ǹ���װ���޷�����");
				}
				if(equipStorRs.getInt("strelv") > 0){
					DBPsRs equipStreRs = DBPool.getInst().pQueryS(tab_equip_stre, "strelv<="+equipStorRs.getInt("strelv"));
					stremoney += equipStreRs.sum("q"+rare);
				}
			}
			DBPaRs smeltRs = DBPool.getInst().pQueryA(tab_equip_smelt, "rare1="+rare);
			if(itemidarr.length != smeltRs.getInt("needequip")){
				BACException.throwInstance("Ҫ�ں�װ������������ȷ "+itemidarr.length+"/"+smeltRs.getInt("needequip"));
			}
			int returnCounts = smeltRs.getInt("equips");
			if (returnCounts > itemidarr.length) {
				BACException.throwInstance("������װ���������� " + returnCounts);
			}
			int needmoney = smeltRs.getInt("needmoney");
			if(needmoney > stremoney){
				PlayerBAC.getInstance().useMoney(dbHelper, playerid, needmoney-stremoney, gl);
			} 
			int odds = smeltRs.getInt("odds");
			DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
			JSONArray failarr = new JSONArray(plaroleRs.getString("eqsmeltfailam"));
			odds += failarr.optInt(rare-1) * smeltRs.getInt("failgain");
			int ran = MyTools.getRandom(1, 100);
			byte obtaintype = 0;
			int[] returnEquip = null;
			if(ran <= odds){
				failarr.put(rare-1, 0);
				obtaintype = ItemBAC.TYPE_EQUIP_ORDINARY;
			} else {
				failarr.put(rare-1, failarr.optInt(rare-1)+1);
				obtaintype = ItemBAC.TYPE_EQUIP_DEBRIS;
				for (int i = 0; i < returnCounts; i ++) {
					int ranIndex = Tools.getRandomNumber(0, itemidarr.length - 1);
					returnEquip = Tools.addToIntArr(returnEquip, itemidarr[ranIndex]);
					itemidarr = Tools.removeOneFromIntArr(itemidarr, ranIndex);
				}
			}
			DBPsRs tabRs = DBPool.getInst().pQueryS(ItemBAC.getInstance().getTab(obtaintype), "rare="+smeltRs.getInt("rare2"));
			int[] oddsarr = new int[tabRs.count()];
			while(tabRs.next()){
				oddsarr[tabRs.getRow()-1] = tabRs.getInt("smeltodds");
			}
			tabRs.setRow(MyTools.getIndexOfRandom(oddsarr)+1);
			JSONArray itemarr = new JSONArray();
			equipStorRs.beforeFirst();
			while(equipStorRs.next()){
				if (returnEquip != null && Tools.contain(returnEquip, equipStorRs.getInt("itemid"))) {
					// ����װ�������ǿ���ȼ�����0�������Ϊ0
					if (equipStorRs.getInt("strelv") > 0) {
						SqlString sqlStr = new SqlString();
						sqlStr.add("strelv", 0);
						updateByKey(dbHelper, playerid, sqlStr, equipStorRs.getInt("itemid"));
					}
					JSONObject itemData = ItemBAC.getInstance().getItemData(playerid, equipStorRs.getInt("itemid"), ItemBAC.ZONE_BAG);
					itemarr.add(itemData);
				} else {
					JSONObject obj = ItemBAC.getInstance().remove(dbHelper, playerid, equipStorRs.getInt("itemid"), ItemBAC.ZONE_BAG, gl);
					itemarr.add(obj);
				}
			}
			
			if(needmoney < stremoney){
				PlayerBAC.getInstance().addValue(dbHelper, playerid, "money", stremoney-needmoney, gl, GameLog.TYPE_MONEY);
			}
			JSONArray arr2 = ItemBAC.getInstance().add(dbHelper, playerid, obtaintype, tabRs.getInt("num"), 1, ItemBAC.ZONE_BAG, ItemBAC.SHORTCUT_MAIL, 1, gl);
			MyTools.combJsonarr(itemarr, arr2);
			PlaRoleBAC.getInstance().setValue(dbHelper, playerid, "eqsmeltfailam", failarr.toString(), gl, "����ʧ�ܴ���");
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(itemarr);
			returnarr.add(stremoney-needmoney);//ͭǮ�仯
			
			if(obtaintype == ItemBAC.TYPE_EQUIP_ORDINARY && smeltRs.getInt("rare2") >= 4){
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				GamePushData.getInstance(4)
				.add(plaRs.getString("name"))
				.add(tabRs.getString("name"))
				.sendToAllOL();
			}
			
			gl.addItemChaNoteArr(itemarr);
			gl.addRemark("����ǿ��ͭǮ��"+stremoney+" �����շѣ�"+needmoney);
			gl.save();
			return new ReturnValue(true, itemarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ��½��ͨװ������
	 */
	public void getLoginItemInfo(int playerid, JSONObject infoobj) throws Exception {
		DBPsRs ordinaryRs = query(playerid, "playerid="+playerid);
		while(ordinaryRs.next()){
			int itemid = ordinaryRs.getInt("itemid");
			JSONArray arr = getData(ordinaryRs);
			infoobj.put(String.valueOf(itemid), arr);
		}
	}
	
	/**
	 * ��ȡ��ͨװ������
	 */
	public JSONArray getData(int playerid, int itemid) throws Exception {
		DBPsRs equipRs = query(playerid, "playerid="+playerid+" and itemid="+itemid);
		if(!equipRs.next()) {
			BACException.throwAndPrintInstance("װ�����ݲ����� " + itemid);
		}
		JSONArray infoarr = getData(equipRs);
		return infoarr;
	}
	
	/**
	 * ����װ��
	 */
	public JSONArray create(DBHelper dbHelper, int playerid, int itemid, int num, JSONArray extendarr, int from, GameLog gl) throws Exception {
		int strelv = 0;
		int starlv = 0;
		if(extendarr != null){
			strelv = extendarr.optInt(0);
			starlv = extendarr.optInt(1);
		}
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.add("itemid", itemid);
		sqlStr.add("num", num);
		sqlStr.add("strelv", strelv);
		sqlStr.add("starlv", starlv);
		insert(dbHelper, playerid, sqlStr);
		
		JSONArray result = new JSONArray();
		result.add(itemid);//װ����ƷID
		result.add(num);//װ�����
		return result;
	}
	
	/**
	 * ��ȡװ�������ֶ�����
	 */
	public JSONArray getData(DBPsRs equipRs) throws Exception {
		JSONArray json = new JSONArray();
		json.add(equipRs.getInt("itemid"));//װ����ƷID
		json.add(equipRs.getInt("num"));//װ�����
		json.add(equipRs.getInt("strelv"));//ǿ���ȼ�
		json.add(equipRs.getInt("starlv"));//�Ǽ�
		return json;
	}
	
	/**
	 * ������ͨװ��
	 */
	public void destory(DBHelper dbHelper, int playerid, int itemid, GameLog gl) throws Exception {
		PlaAssectBAC.getInstance().saveLog(this, playerid, "playerid=" + playerid + " and itemid="+itemid, 1, new String[]{"tab_item_stor"}, new String[]{"zone"}, new String[]{"0"});
		deleteByKey(dbHelper, playerid, itemid);
	}

	//--------------��̬��--------------
	
	private static EquipOrdinaryBAC instance = new EquipOrdinaryBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static EquipOrdinaryBAC getInstance(){
		return instance;
	}
}
