package com.moonic.bac;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Sortable;
import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.battle.BattleBox;
import com.moonic.battle.BattleManager;
import com.moonic.battle.Const;
import com.moonic.battle.SpriteBox;
import com.moonic.battle.SpriteBoxExtendPropListener;
import com.moonic.battle.TeamBox;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.GamePushData;
import com.moonic.socket.Player;
import com.moonic.socket.SocketServer;
import com.moonic.txtdata.PartnerAwakenData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPRs;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ���
 * @author John
 */
public class PartnerBAC extends PlaStorBAC {
	public static final String tab_partner = "tab_partner";
	public static final String tab_partner_fetter = "tab_partner_fetter";
	public static final String tab_partner_phase_attr = "tab_partner_phase_attr";
	public static final String tab_partner_skill_uplv = "tab_partner_skill_uplv";
	public static final String tab_partner_uplv = "tab_partner_uplv";
	public static final String tab_partner_upphase = "tab_partner_upphase";
	public static final String tab_partner_upstar = "tab_partner_upstar";
	public static final String tab_bskill = "tab_bskill";
	public static final String tab_battlepower = "tab_battlepower";
	public static final String tab_skill_battlepower = "tab_skill_battlepower";
	
	/**
	 * ����
	 */
	public PartnerBAC() {
		super("tab_partner_stor", "playerid", "id");
	}
	
	/**
	 * ��װ��
	 */
	public ReturnValue putonEquip(int playerid, int partnerid, int partnerid2, int itemid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			if(CBBAC.cbmgr.checkPartnerInWar(partnerid)){
				BACException.throwInstance("������ڹ�ս�У��޷�����װ��");
			}
			DBPaRs equipStorRs = EquipOrdinaryBAC.getInstance().getDataRsByKey(playerid, itemid);
			if(!equipStorRs.exist()){
				BACException.throwInstance("װ��δ�ҵ�");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_PUTON_EQUIP);
			DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
			int pos = equipRs.getInt("equiptype");
			String colName = "pos"+pos;
			//�ȱ�ָ֤���ռ���ָ��װ��
			if(partnerid2 != 0){
				DBPaRs partnerStorRs2 = getDataRsByKey(playerid, partnerid2);
				if(!partnerStorRs2.exist()){
					BACException.throwInstance("�������δ�ҵ�");
				}
				if(partnerStorRs2.getInt(colName) != itemid){
					BACException.throwInstance("�������δ�ҵ�ָ��װ�� partnerid="+partnerid2+" itemid="+itemid);
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add(colName, 0);
				updateByKey(dbHelper, playerid, sqlStr, partnerid2);
				DBPaRs partnerRs2 = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs2.getInt("num"));
				gl.addRemark(GameLog.formatNameID(partnerRs2.getString("name"), partnerid2)+"ж����"+GameLog.formatNameID(equipRs.getString("name"), itemid));
			} else {
				JSONArray itemarr = ItemBAC.getInstance().moveToZone(dbHelper, playerid, itemid, ItemBAC.ZONE_BAG, ItemBAC.ZONE_USE, false, gl);
				gl.addItemChaNoteArr(itemarr);
			}
			//����������Ѿ����ϵ�װ����������
			int use_itemid = partnerStorRs.getInt(colName);
			if(use_itemid != 0){
				JSONArray use_itemarr = ItemBAC.getInstance().moveToZone(dbHelper, playerid, use_itemid, ItemBAC.ZONE_USE, ItemBAC.ZONE_BAG, true, gl);
				gl.addItemChaNoteArr(use_itemarr);
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add(colName, itemid);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			gl.addRemark(GameLog.formatNameID(partnerRs.getString("name"), partnerid)+"������"+GameLog.formatNameID(equipRs.getString("name"), itemid));
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
	 * һ����װ
	 */
	public ReturnValue shortcutPutonEquip(int playerid, int partnerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(CBBAC.cbmgr.checkPartnerInWar(partnerid)){
				BACException.throwInstance("������ڹ�ս�У��޷�����װ��");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_SHORTCUT_PUTON_EQUIP);
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			int[] fetterarr = Tools.splitStrToIntArr(partnerRs.getString("fetternum"), ",");
			JSONArray fetterEquipArr = new JSONArray();//�ɲ�����װ��
			for(int i = 0; i < fetterarr.length; i++){
				DBPaRs fetterRs = DBPool.getInst().pQueryA(tab_partner_fetter, "num="+fetterarr[i]);
				int[][] funcarr = Tools.splitStrToIntArr2(fetterRs.getString("func"), "|", ",");
				for(int k = 0; k < funcarr.length; k++){
					if(funcarr[k][0] == 2){
						fetterEquipArr.add(funcarr[k][1]);
					}
				}
			}
			JSONArray useingarr = new JSONArray();//��װ��������������ϵ�װ��
			DBPsRs itemStorRs = ItemBAC.getInstance().query(playerid, "playerid="+playerid+" and itemtype="+ItemBAC.TYPE_EQUIP_ORDINARY+" and zone="+ItemBAC.ZONE_USE);
			while(itemStorRs.next()){
				useingarr.add(itemStorRs.getInt("id"));
			}
			for(int i = 1; i <= 6; i++){
				int curr_itemid = partnerStorRs.getInt("pos"+i);
				if(curr_itemid != 0){
					useingarr.remove(useingarr.indexOf(curr_itemid));
				}
			}
			DBPsRs equipStorRs = EquipOrdinaryBAC.getInstance().query(playerid, "playerid="+playerid);
			SPEquip[] spequip = new SPEquip[equipStorRs.count()];
			while(equipStorRs.next()){
				if(!useingarr.contains(equipStorRs.getInt("itemid"))){
					DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
					spequip[equipStorRs.getRow()-1] = new SPEquip(equipStorRs.getInt("itemid"), equipRs.getInt("equiptype"), fetterEquipArr.contains(equipStorRs.getInt("num"))?1:0, equipRs.getInt("rare"), getBattlePower(partnerRs.getInt("battletype"), getEquipBattleData(equipStorRs)));		
				}
			}
			Tools.sort(spequip, 1);
			JSONArray itemarr = new JSONArray();
			SqlString sqlStr = new SqlString();
			for(int i = 1; i <= 6; i++){
				int curr_itemid = partnerStorRs.getInt("pos"+i);
				for(int k = 0; k < spequip.length; k++){
					if(spequip[k] != null && spequip[k].equiptype == i){//��λƥ��
						if(spequip[k].itemid != curr_itemid){//�ȵ�ǰ���ĺ�
							if(curr_itemid != 0){
								JSONArray arr1 = ItemBAC.getInstance().moveToZone(dbHelper, playerid, curr_itemid, ItemBAC.ZONE_USE, ItemBAC.ZONE_BAG, true, gl);
								MyTools.combJsonarr(itemarr, arr1);
							}
							JSONArray arr2 = ItemBAC.getInstance().moveToZone(dbHelper, playerid, spequip[k].itemid, ItemBAC.ZONE_BAG, ItemBAC.ZONE_USE, true, gl);
							MyTools.combJsonarr(itemarr, arr2);
							sqlStr.add("pos"+i, spequip[k].itemid);
						}
						break;//ֻ����õ�
					}
				}
			}
			if(sqlStr.getColCount() <= 0){
				BACException.throwInstance("û�пɸ�����װ��");
			}
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
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
	 * һ����װ����װ��
	 */
	class SPEquip implements Sortable {
		public int itemid;
		public int equiptype;
		public int fetter;
		public int rare;
		public int battlepower;
		
		public SPEquip(int itemid, int equiptype, int fetter, int rare, int battlepower) {
			this.itemid = itemid;
			this.equiptype = equiptype;
			this.fetter = fetter;
			this.rare = rare;
			this.battlepower = battlepower;
		}
		
		public double getSortValue() {
			return fetter*10000000000L+rare*1000000000L+battlepower;
		}
	}
	
	/**
	 * ��װ��
	 */
	public ReturnValue putoffEquip(int playerid, int partnerid, int pos){
		DBHelper dbHelper = new DBHelper();
		try {
			if(pos<1 || pos>6){
				BACException.throwInstance("װ����λ���� pos="+pos);
			}
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			if(CBBAC.cbmgr.checkPartnerInWar(partnerid)){
				BACException.throwInstance("������ڹ�ս�У��޷�����װ��");
			}
			String colName = "pos"+pos;
			int itemid = partnerStorRs.getInt(colName);
			if(itemid == 0){
				BACException.throwInstance("�˲�λ��û�д�װ��");
			}
			DBPaRs equipStorRs = EquipOrdinaryBAC.getInstance().getDataRsByKey(playerid, itemid);
			if(!equipStorRs.exist()){
				BACException.throwInstance("װ��δ�ҵ�");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_PUTOFF_EQUIP);
			JSONArray itemarr = ItemBAC.getInstance().moveToZone(dbHelper, playerid, itemid, ItemBAC.ZONE_USE, ItemBAC.ZONE_BAG, true, gl);
			gl.addItemChaNoteArr(itemarr);
			SqlString sqlStr = new SqlString();
			sqlStr.add(colName, 0);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
			gl.addRemark(GameLog.formatNameID(partnerRs.getString("name"), partnerid)+"������"+GameLog.formatNameID(equipRs.getString("name"), itemid));
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
	 * ������
	 */
	public ReturnValue putonOrb(int playerid, int partnerid, int pos){
		DBHelper dbHelper = new DBHelper();
		try {
			if(pos<1 || pos>6){
				BACException.throwInstance("װ����λ���� pos="+pos);
			}
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			String colName = "orb"+pos;
			if(partnerStorRs.getInt(colName) != 0){
				BACException.throwInstance("�Ѿ�װ��������");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_PUTON_ORB);
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			int[] upphasenumArr = Tools.splitStrToIntArr(partnerRs.getString("upphasenum"), ",");
			DBPaRs partnerUpphaseRs = DBPool.getInst().pQueryA(tab_partner_upphase, "num="+upphasenumArr[partnerStorRs.getInt("phase")-1]);//���׶γ�ʼֵΪ1
			int orbnum = partnerUpphaseRs.getInt("pos"+pos);
			DBPaRs orbRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_ORB, orbnum);
			if(partnerStorRs.getInt("lv") < orbRs.getInt("putonlv")){
				BACException.throwInstance("���ȼ�δ�ﵽ�����ȼ�");
			}
			JSONArray itemarr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_ORB, orbnum, 1, ItemBAC.ZONE_BAG, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.add(colName, orbnum);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			gl.addItemChaNoteArr(itemarr);
			gl.addRemark("�����ߣ�"+GameLog.formatNameID(partnerRs.getString("name"), partnerid));
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
	 * һ��������
	 */
	public ReturnValue shortcutPutonOrb(int playerid, int partnerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			int[] upphasenumArr = Tools.splitStrToIntArr(partnerRs.getString("upphasenum"), ",");
			DBPaRs partnerUpphaseRs = DBPool.getInst().pQueryA(tab_partner_upphase, "num="+upphasenumArr[partnerStorRs.getInt("phase")-1]);//���׶γ�ʼֵΪ1
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_SHORTCUT_PUTON_ORB);
			DBPsRs itemStorRs = ItemBAC.getInstance().query(playerid, "playerid="+playerid);
			JSONObject orbobj = new JSONObject();//���� ���-����
			JSONObject orbdebris = new JSONObject();//������Ƭ ���-����
			while(itemStorRs.next()){
				if(itemStorRs.getInt("itemtype") == ItemBAC.TYPE_ORB){
					orbobj.put(itemStorRs.getString("itemnum"), itemStorRs.getInt("itemamount"));
				} else 
				if(itemStorRs.getInt("itemtype") == ItemBAC.TYPE_ORB_DEBRIS){
					orbdebris.put(itemStorRs.getString("itemnum"), itemStorRs.getInt("itemamount"));
				}
			}
			JSONArray posarr = new JSONArray();//��δ��Ƕ������Ƕ�Ĳ�λ
			JSONArray itemarr = new JSONArray();//��Ʒ�仯
			SqlString sqlStr = new SqlString();
			for(int i = 1; i <= 6; i++){
				String column = "orb"+i;
				if(partnerStorRs.getInt(column) != 0){//����Ƕ����
					continue;
				}
				int orbnum = partnerUpphaseRs.getInt("pos"+i);//��Ҫ��Ƕ��������
				DBPaRs orbRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_ORB, orbnum);
				if(partnerStorRs.getInt("lv") < orbRs.getInt("putonlv")){//����Ƿ�����ȼ�����
					continue;
				}
				if(orbobj.optInt(String.valueOf(orbnum)) > 0){//�г�Ʒ
					JSONArray arr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_ORB, orbnum, 1, ItemBAC.ZONE_BAG, gl);
					MyTools.combJsonarr(itemarr, arr);
					orbobj.put(String.valueOf(orbnum), orbobj.optInt(String.valueOf(orbnum))-1);
				} else {//�ϳ�
					JSONObject new_orbobj = new JSONObject(orbobj.toString());
					JSONObject new_orbdebris = new JSONObject(orbdebris.toString());
					JSONObject remove_orbobj = new JSONObject();
					JSONObject remove_orbdebris = new JSONObject();
					if(!checkOrbShortcutComp(orbnum, 1, new_orbobj, new_orbdebris, remove_orbobj, remove_orbdebris)){//�޳�Ʒ���飬���Կ�ݺϳ�
						continue;
					}
					//System.out.println("remove_orbobj:"+remove_orbobj);
					//System.out.println("remove_orbdebris:"+remove_orbdebris);
					@SuppressWarnings("unchecked")
					Iterator<String> iterator_1 = remove_orbobj.keys();
					while(iterator_1.hasNext()){//�����Ƴ���������Ƴ�����
						String num = iterator_1.next();
						//System.out.println(Tools.str2int(num)+"---------"+remove_orbobj.optInt(num));
						JSONArray arr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_ORB, Tools.str2int(num), remove_orbobj.optInt(num), ItemBAC.ZONE_BAG, gl);
						MyTools.combJsonarr(itemarr, arr);
					}
					@SuppressWarnings("unchecked")
					Iterator<String> iterator_2 = remove_orbdebris.keys();
					while(iterator_2.hasNext()){//�����Ƴ���������Ƴ�������Ƭ
						String num = iterator_2.next();
						//System.out.println(Tools.str2int(num)+"+++++++++"+remove_orbdebris.optInt(num));
						JSONArray arr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_ORB_DEBRIS, Tools.str2int(num), remove_orbdebris.optInt(num), ItemBAC.ZONE_BAG, gl);
						MyTools.combJsonarr(itemarr, arr);
					}
					orbobj = new_orbobj;
					orbdebris = new_orbdebris;
				}
				posarr.add(i);//��¼�ɹ���Ƕ����Ĳ�λ
				sqlStr.add(column, orbnum);//SQL��¼Ҫ���µ��ֶ�
			}
			if(sqlStr.getColCount() <= 0){//�ޱ仯�򷵻���ʾ
				BACException.throwInstance("û�пɴ���������");
			}
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(posarr);//����������Ĳ�λ
			returnarr.add(itemarr);//��Ʒ�仯
			
			gl.addItemChaNoteArr(itemarr);
			gl.addRemark("�����ߣ�"+GameLog.formatNameID(partnerRs.getString("name"), partnerid));
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
	 * ����ϳɼ��
	 * @param orbnum Ҫ�ϳɵ�������
	 * @param needamount ��Ҫ������
	 * @param orbobj �������鼯��
	 * @param orbdebris ����������Ƭ����
	 * @param remove_orbobj �����ٵ����鼯��
	 * @param remove_orbdebris �����ٵ�������Ƭ����
	 * @return �Ƿ�ɺϳ�
	 */
	public boolean checkOrbShortcutComp(int orbnum, int needamount, JSONObject orbobj, JSONObject orbdebris, JSONObject remove_orbobj, JSONObject remove_orbdebris) throws Exception {
		DBPaRs orbRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_ORB, orbnum);
		if(orbRs.getString("need").equals("0")){//�޷��ϳ�
			return false;
		}
		int[][] needarr = Tools.splitStrToIntArr2(orbRs.getString("need"), "|", ",");//�ϳɴ���������Ҫ�Ĳ���
		for(int k = 0; k < needarr.length; k++){
			if(needarr[k][0] == ItemBAC.TYPE_ORB){//��Ҫ��������
				int amount = orbobj.optInt(String.valueOf(needarr[k][1]));//ӵ�е�����
				if(amount < needarr[k][2]*needamount //ӵ�е�����С���������� && ʣ�����������޷�ͨ���ϳɻ��
						&& !checkOrbShortcutComp(needarr[k][1], needarr[k][2]*needamount-amount, orbobj, orbdebris, remove_orbobj, remove_orbdebris) 
						){
					return false;
				}
				if(amount > 0){//�����ǰӵ����������
					if(amount >= needarr[k][2]*needamount){//��ȫ��������
						orbobj.put(String.valueOf(needarr[k][1]), amount-needarr[k][2]*needamount);//��¼����ָ����������
						remove_orbobj.put(String.valueOf(needarr[k][1]), remove_orbobj.optInt(String.valueOf(needarr[k][1]))+needarr[k][2]*needamount);//��¼���Ƴ�����������
					} else {//����ȫ����
						orbobj.put(String.valueOf(needarr[k][1]), 0);//��¼����ָ����������
						remove_orbobj.put(String.valueOf(needarr[k][1]), remove_orbobj.optInt(String.valueOf(needarr[k][1]))+amount);//��¼���Ƴ�����������
					}
				}
			} else {//��Ҫ����������Ƭ
				int amount = orbdebris.optInt(String.valueOf(needarr[k][1]));//��������
				if(amount < needarr[k][2]*needamount){//��������
					return false;
				}
				orbdebris.put(String.valueOf(needarr[k][1]), amount-needarr[k][2]*needamount);//��¼����������Ƭ����
				remove_orbdebris.put(String.valueOf(needarr[k][1]), remove_orbdebris.optInt(String.valueOf(needarr[k][1]))+needarr[k][2]*needamount);//��¼���Ƴ���������Ƭ����
			}
		}
		return true;
	}
	
	/**
	 * ����
	 */
	public ReturnValue upPhase(int playerid, int partnerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			for(int i = 1; i <= 6; i++){
				if(partnerStorRs.getInt("orb"+i) == 0){
					BACException.throwInstance("����δ����");
				}
			}
			int oldPhase = partnerStorRs.getInt("phase");
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			int[] upphasenumArr = Tools.splitStrToIntArr(partnerRs.getString("upphasenum"), ",");
			if(oldPhase > upphasenumArr.length){
				BACException.throwInstance("���������ף��޷�������");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("phase", 1);
			for(int i = 1; i <= 6; i++){
				sqlStr.add("orb"+i, 0);
			}
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			GameLog.getInst(playerid, GameServlet.ACT_PARTNER_UPPHASE)
			.addChaNote(GameLog.formatNameID(partnerRs.getString("name"), partnerid)+"�׼�", oldPhase, 1)
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
	 * �һ����
	 */
	public ReturnValue exchange(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+num);
			if(partnerStorRs.have()){
				BACException.throwInstance("���ܶһ���ӵ�еĻ��");
			}
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+num);
			DBPsRs upStarRs = DBPool.getInst().pQueryS(tab_partner_upstar, "star<="+partnerRs.getInt("initstar"));
			int removeamount = (int)upStarRs.sum("debris");
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_EXCHANGE);
			JSONArray itemarr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_SOUL_STONE, partnerRs.getInt("needsoulstone"), removeamount, ItemBAC.ZONE_BAG, gl);
			int partnerid = create(dbHelper, playerid, num, partnerRs.getInt("awaken")==0?1:0, 5, 1, partnerRs.getInt("initstar"), null, null, null);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(itemarr);
			returnarr.add(partnerid);
			
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			GamePushData.getInstance(3)
			.add(plaRs.getString("name"))
			.add(partnerRs.getString("name"))
			.sendToAllOL();
			
			gl.addItemChaNoteArr(itemarr);
			gl.addRemark("�һ���飺"+partnerRs.getString("name"));
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
	 * ����
	 */
	public ReturnValue upStar(int playerid, int partnerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			int nextStar = partnerStorRs.getInt("star")+1;
			DBPaRs upstarRs = DBPool.getInst().pQueryA(tab_partner_upstar, "star="+nextStar);
			if(!upstarRs.exist()){
				BACException.throwInstance("����������");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_UPSTAR);
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, upstarRs.getInt("needmoney"), gl);
			int oldStar = partnerStorRs.getInt("star");
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			JSONArray itemarr = ItemBAC.getInstance().remove(dbHelper, playerid, ItemBAC.TYPE_SOUL_STONE, partnerRs.getInt("needsoulstone"), upstarRs.getInt("debris"), ItemBAC.ZONE_BAG, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("star", 1);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			if(nextStar >= 3){
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				GamePushData.getInstance(2)
				.add(plaRs.getString("name"))
				.add(partnerRs.getString("name"))
				.add(nextStar)
				.sendToAllOL();	
			}
			
			gl.addItemChaNoteArr(itemarr);
			gl.addChaNote(GameLog.formatNameID(partnerRs.getString("name"), partnerid)+"�Ǽ�", oldStar, 1);
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
	 * ����
	 */
	public ReturnValue awaken(int playerid, int partnerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			int awakennum = partnerRs.getInt("awaken");
			if(awakennum == 0){
				BACException.throwInstance("�޷�����");
			}
			if(partnerStorRs.getInt("phase") < PartnerAwakenData.awaken_needphase){
				BACException.throwInstance("������׼�����");
			}
			if(partnerStorRs.getInt("star") < PartnerAwakenData.awaken_needstar){
				BACException.throwInstance("�������Ǽ�����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_AWAKEN);
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, PartnerAwakenData.awaken_needmoney, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.add("num", awakennum);
			sqlStr.add("awaken", 1);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			gl.addRemark(GameLog.formatNameID(partnerRs.getString("name"), partnerid)+"����");
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
	 * ��������
	 */
	public ReturnValue upskilllv(int playerid, int partnerid, int pos, int upamount){
		DBHelper dbHelper = new DBHelper();
		try {
			if(pos<1 || pos>6){
				BACException.throwInstance("�������ܴ��� pos="+pos);
			}
			if(upamount < 1){
				BACException.throwInstance("�������ܴ��� upamount="+upamount);
			}
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			if(pos != 1){
				int[] bskilldata = Tools.splitStrToIntArr2(partnerRs.getString("bskill"), "|", ",")[pos-2];
				if(bskilldata[1] == 0){
					BACException.throwInstance("���Ѻ󿪷�");
				}
				if(partnerStorRs.getInt("phase") < bskilldata[1]){
					BACException.throwInstance("������Ʒ������");
				}
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_UPSKILLLV);
			String colName = "skilllv"+pos;
			int oldSkillLv = partnerStorRs.getInt(colName);
			DBPaRs partnerSkillUplvRs = DBPool.getInst().pQueryA(tab_partner_skill_uplv, "skilllv="+(oldSkillLv+upamount));
			if(!partnerSkillUplvRs.exist() || partnerSkillUplvRs.getString("skill"+pos).equals("0")){
				BACException.throwInstance("�޷�����ָ���ȼ� num="+partnerStorRs.getInt("num")+" tgrlv="+(oldSkillLv+upamount));
			}
			int[] uplvdata = Tools.splitStrToIntArr(partnerSkillUplvRs.getString("skill"+pos), ",");
			if(partnerStorRs.getInt("lv") < uplvdata[0]){
				BACException.throwInstance("���ȼ�����");
			}
			DBPsRs partnerSkillupLvRs2 = DBPool.getInst().pQueryS(tab_partner_skill_uplv, "skilllv>="+(oldSkillLv+1)+" and skilllv<="+(oldSkillLv+upamount));
			int needMoney = 0;
			while(partnerSkillupLvRs2.next()){
				needMoney += Tools.splitStrToIntArr(partnerSkillupLvRs2.getString("skill"+pos), ",")[1];
			}
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, needMoney, gl);
			
			SqlString sqlStr = new SqlString();
			sqlStr.addChange(colName, upamount);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_PARTNER_SKILL_UP, upamount, gl);
			
			gl.addChaNote(GameLog.formatNameID(partnerRs.getString("name"), partnerid)+"����"+pos, oldSkillLv, upamount);
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
	 * һ��ǿ��
	 */
	public ReturnValue shortcutStreEquip(int playerid, int partnerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			int plaLv = plaRs.getInt("lv");
			int money = plaRs.getInt("money");
			int usemoney = 0;
			int sumStreLv = 0;//�ۼ�ǿ���ȼ�
			JSONArray strearr = new JSONArray();
			for(int i = 1; i <= 6; i++){
				int itemid = partnerStorRs.getInt("pos"+i);
				if(itemid != 0){
					DBPaRs equipStorRs = EquipOrdinaryBAC.getInstance().getDataRsByKey(playerid, itemid);
					int strelv = equipStorRs.getInt("strelv");
					int addStreLv = 0;
					DBPsRs streRs = DBPool.getInst().pQueryS(EquipOrdinaryBAC.tab_equip_stre, "strelv>"+strelv+" and strelv<="+plaLv, "strelv");
					DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, equipStorRs.getInt("num"));
					boolean haveStre = false;
					while(streRs.next()){
						int needmoney = streRs.getInt("q"+equipRs.getInt("rare"));
						if(money < needmoney){
							break;
						}
						money -= needmoney;
						usemoney += needmoney;
						addStreLv++;
						sumStreLv++;
						haveStre = true;
					}
					if(haveStre){
						JSONArray arr = new JSONArray();
						arr.add(itemid);
						arr.add(strelv);
						arr.add(addStreLv);
						arr.add(equipRs.getString("name"));
						strearr.add(arr);
					}
				}
			}
			if(usemoney == 0){
				BACException.throwInstance("����Ҫǿ��");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PARTNER_SHORTCUT_STRE_EQUIP);
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, usemoney, gl);
			JSONArray returnarr = new JSONArray();
			for(int i = 0; i < strearr.length(); i++){
				JSONArray arr = strearr.optJSONArray(i);
				SqlString equipSqlStr = new SqlString();
				equipSqlStr.addChange("strelv", arr.optInt(2));
				EquipOrdinaryBAC.getInstance().updateByKey(dbHelper, playerid, equipSqlStr, arr.optInt(0));
				gl.addChaNote(GameLog.formatNameID(arr.optString(3), arr.optInt(0))+" ǿ���ȼ�", arr.optInt(1), arr.optInt(2));
				returnarr.add(new int[]{arr.optInt(0), arr.optInt(1)+arr.optInt(2)});
			}
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_EQUIP_STRENGTHEN, sumStreLv, gl);
			
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
	 * ���Լӻ��
	 */
	public ReturnValue debugAdd(int playerid, int num, int phase, int star){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+num);
			if(!partnerRs.exist()){
				BACException.throwInstance("����Ų����� num="+num);
			}
			DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+num);
			if(partnerStorRs.have()){
				BACException.throwInstance("���ܴ�����ӵ�еĻ��");
			}
			create(dbHelper, playerid, num, partnerRs.getInt("awaken")==0?1:0, 1, phase, star, null, null, null);
			
			GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG)
			.addRemark("�������ӻ�� "+partnerRs.getString("name"))
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
	 * ����ɾ���
	 */
	public ReturnValue debugDelete(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+num);
			if(!partnerRs.exist()){
				BACException.throwInstance("����Ų����� num="+num);
			}
			DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+num);
			if(!partnerStorRs.have()){
				BACException.throwInstance("δӵ�д˻��");
			}
			while(partnerStorRs.next()){
				for(int i = 1; i <= 6; i++){
					int itemid = partnerStorRs.getInt("pos"+i);
					if(itemid != 0){
						putoffEquip(playerid, partnerStorRs.getInt("id"), i);
					}
				}
			}
			delete(dbHelper, playerid, "playerid="+playerid+" and num="+num);
			
			GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG)
			.addRemark("����ɾ����� "+partnerRs.getString("name"))
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
	 * ���Լӽ׼�
	 */
	public ReturnValue debugAddPhase(int playerid, int partnerid, int add){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("phase", add);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG)
			.addRemark("�������ӻ��׼� "+partnerRs.getString("name"))
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
	 * ���Լ��Ǽ�
	 */
	public ReturnValue debugAddStar(int playerid, int partnerid, int add){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
			if(!partnerStorRs.exist()){
				BACException.throwInstance("���δ�ҵ�");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("star", add);
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
			GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG)
			.addRemark("�������ӻ���Ǽ� "+partnerRs.getString("name"))
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
	 * ���ԼӾ���
	 */
	public ReturnValue debugAddExp(int playerid, int partnerid, int add){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			addExp(dbHelper, playerid, partnerid, add, gl);
			
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
	 * ��ȡ���ս��������
	 */
	public ReturnValue bkGetSpriteBox(int playerid){
		try {
			ArrayList<SpriteBox> sprites = getSpriteBoxList(playerid, null, null);
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < sprites.size(); i++){
				SpriteBox spritebox = sprites.get(i);
				sb.append(spritebox.getIngredientStr());
				sb.append("\r\n");
				sb.append("����ս����"+(getSpriteBoxBattlePower(spritebox)-spritebox.skillAddBattlerPower)+"\r\n");
				sb.append("����ս����"+spritebox.skillAddBattlerPower+"\r\n");
				sb.append("��ս����"+getSpriteBoxBattlePower(spritebox)+"\r\n");
			}
			return new ReturnValue(true, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ���Է����ս��
	 * @param playerid
	 * @param posarr1 �ṹ��JSONARRAY[1��λ���ID��2��λ���ID��3��λ���ID��4��λ���ID��5��λ���ID��6��λ���ID]
	 * @param oppid ���ֽ�ɫID
	 * @param posarr2 ���ֻ��վλ
	 * @return
	 */
	public ReturnValue debugServerBattle(int playerid, String posarr1, int oppid, String posarr2){
		try {
			TeamBox teambox1 = getTeamBox(playerid, 0, new JSONArray(posarr1));
			TeamBox teambox2 = getTeamBox(oppid, 1, new JSONArray(posarr2));
			BattleBox battlebox = new BattleBox();
			battlebox.teamArr[0].add(teambox1);
			battlebox.teamArr[1].add(teambox2);
			BattleManager.createPVPBattle(battlebox);
			return new ReturnValue(true, battlebox.replayData.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ���Ի�ȡBATTLEBOX
	 */
	public ReturnValue debugGetBattleBox(int playerid, String posarr1, int oppid, String posarr2){
		try {
			TeamBox teambox1 = getTeamBox(playerid, 0, new JSONArray(posarr1));
			TeamBox teambox2 = getTeamBox(oppid, 1, new JSONArray(posarr2));
			BattleBox battlebox = new BattleBox();
			battlebox.teamArr[0].add(teambox1);
			battlebox.teamArr[1].add(teambox2);
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			pla.verifybattle_battlebox = battlebox;
			return new ReturnValue(true, battlebox.getJSONArray().toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ������֤ս��
	 */
	public ReturnValue debugServerVerify(int playerid, String battleRecord){
		try {
			Player pla = SocketServer.getInstance().plamap.get(playerid);
			BattleManager.verifyPVEBattle(pla.verifybattle_battlebox, battleRecord);
			pla.verifybattle_battlebox = null;
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ��¼����
	 */
	public JSONArray getLoginData(int playerid) throws Exception {
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid);
		JSONArray returnarr = new JSONArray();
		while(partnerStorRs.next()){
			JSONArray arr = new JSONArray();
			arr.add(partnerStorRs.getInt("id"));//���ID
			arr.add(partnerStorRs.getInt("num"));//���
			arr.add(partnerStorRs.getInt("lv"));//�ȼ�
			arr.add(partnerStorRs.getInt("exp"));//����
			arr.add(partnerStorRs.getInt("phase"));//�׼�
			arr.add(partnerStorRs.getInt("star"));//�Ǽ�
			for(int i = 1; i <= 6; i++){
				arr.add(partnerStorRs.getInt("pos"+i));//װ����λ	
			}
			for(int i = 1; i <= 6; i++){
				arr.add(partnerStorRs.getInt("orb"+i));//���鲿λ
			}
			for(int i = 1; i <= 5; i++){
				arr.add(partnerStorRs.getInt("skilllv"+i));	//���ܵȼ�
			}
			returnarr.add(arr);
		}
		return returnarr;
	}
	
	/**
	 * �Ӿ���
	 */
	public JSONArray addExp(DBHelper dbHelper, int playerid, int partnerid, long addexp, GameLog gl) throws Exception {
		DBPaRs partnerStorRs = getDataRsByKey(playerid, partnerid);
		if(!partnerStorRs.exist()){
			BACException.throwInstance("���δ�ҵ�");
		}
		DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+partnerStorRs.getInt("num"));
		int lv = partnerStorRs.getInt("lv");
		int maxpartnerlv = PlayerBAC.getInstance().getMaxPartnerLv(playerid);
		JSONArray returnarr = ExpBAC.getInstance().addExp(tab_partner_uplv, lv, partnerStorRs.getInt("exp"), addexp, maxpartnerlv, GameLog.formatNameID(partnerRs.getString("name"), partnerid), gl);
		if(returnarr != null){
			SqlString sqlStr = new SqlString();
			sqlStr.add("lv", returnarr.optInt(0));
			sqlStr.add("exp", returnarr.optInt(1));
			updateByKey(dbHelper, playerid, sqlStr, partnerid);
			if(returnarr.optInt(0) > lv){
				PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_PARTNER_LV_UP, gl);
			}
		}
		return returnarr;
	}
	
	/**
	 * �������ݼ��
	 */
	public JSONArray checkPosarr(int playerid, JSONArray posarr, int minlevel, int minamount) throws Exception {
		if(posarr.length() != 6){
			BACException.throwInstance("���ݳ��Ȳ�����6");
		}
		JSONObject posobj = new JSONObject();
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid);
		while(partnerStorRs.next()){
			posobj.put(partnerStorRs.getString("id"), partnerStorRs.getInt("lv"));
		}
		JSONArray posarr2 = new JSONArray();
		for(int i = 0; i < posarr.length(); i++){
			int partnerid = posarr.getInt(i);
			if(partnerid != 0){
				if(posarr2.contains(partnerid)){
					BACException.throwInstance("���ظ��Ļ�� "+partnerid);
				}
				if(!posobj.has(String.valueOf(partnerid))){
					BACException.throwInstance("��鲻���� "+partnerid);
				}
				if(posobj.optInt(String.valueOf(partnerid)) < minlevel){
					BACException.throwInstance("������ȼ�Ҫ�� "+partnerid);
				}
				posarr2.add(partnerid);
			}
		}
		if(posarr2.length() < minamount){
			BACException.throwInstance("�����������"+minamount+"��");
		}
		return posarr2;
	}
	
	/**
	 * ���»��ս��
	 */
	public void updateBattlePower(DBHelper dbHelper, int playerid) throws Exception {
		ArrayList<SpriteBox> sprites = getSpriteBoxList(playerid, null, null);
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid);
		JSONObject powerobj = new JSONObject();
		while(partnerStorRs.next()){
			powerobj.put(partnerStorRs.getString("id"), partnerStorRs.getInt("battlepower"));
		}
		//���ս��
		int totalbattlepower = 0;
		for(int i = 0; i < sprites.size(); i++){
			SpriteBox spritebox = sprites.get(i);
			int power = getSpriteBoxBattlePower(spritebox);
			if(power != powerobj.optInt(String.valueOf(spritebox.partnerId))){
				SqlString sqlStr = new SqlString();
				sqlStr.add("battlepower", power);
				updateByKey(dbHelper, playerid, sqlStr, spritebox.partnerId);
				powerobj.put(String.valueOf(spritebox.partnerId), power);
				//System.out.println("--------PartnerBAC----------update------------------");
			}
			totalbattlepower += power;
		}
		//��ս��
		DBPaRs plaroleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
		if(totalbattlepower != plaroleRs.getInt("totalbattlepower")){
			SqlString sqlStr = new SqlString();
			sqlStr.add("totalbattlepower", totalbattlepower);
			PlaRoleBAC.getInstance().update(dbHelper, playerid, sqlStr);
		}
		//��������������ս��
		DBPaRs plajjcrankingRs = PlaJJCRankingBAC.getInstance().getDataRs(playerid);
		if(plajjcrankingRs.exist()){
			int power = 0;
			JSONArray posarr = new JSONArray(plajjcrankingRs.getString("defformation"));
			for(int i = 0; i < posarr.length(); i++){
				if(posarr.optInt(i) != 0){
					power += powerobj.optInt(posarr.optString(i));
				}
			}
			SqlString sqlStr = new SqlString();
			if(power != plajjcrankingRs.getInt("battlepower")){
				sqlStr.add("battlepower", power);
				//System.out.println("--------PlaJJCRankingBAC----------update------------------");
			}
			if(plajjcrankingRs.getString("wkdefform") != null){
				int wkpower = 0;
				JSONArray wkposarr = new JSONArray(plajjcrankingRs.getString("wkdefform"));
				for(int i = 0; i < posarr.length(); i++){
					if(wkposarr.optInt(i) != 0){
						wkpower += powerobj.optInt(wkposarr.optString(i));
					}
				}
				if(wkpower != plajjcrankingRs.getInt("wkbattlepower")){
					sqlStr.add("wkbattlepower", wkpower);
					//System.out.println("--------wkbattlepower----------update------------------");
				}
			}
			if(sqlStr.getColCount() > 0){
				PlaJJCRankingBAC.getInstance().update(dbHelper, playerid, sqlStr);
			}
		}
		//��ս�����ս��
		DBPaRs plafacRs = PlaFacBAC.getInstance().getDataRs(playerid);
		int factoinid = plafacRs.getInt("factionid");
		if(factoinid != 0){
			DBPsRs teamRs = CBTeamPoolBAC.getInstance().query(factoinid, "factionid="+factoinid+" and playerid="+playerid);
			while(teamRs.next()){
				JSONArray posarr = new JSONArray(teamRs.getString("teamdata"));
				int power = 0;
				for(int i = 0; i < posarr.length(); i++){
					if(posarr.optInt(i) != 0){
						power += powerobj.optInt(posarr.optString(i));
					}
				}
				if(power != teamRs.getInt("battlepower")){
					SqlString sqlStr = new SqlString();
					sqlStr.add("battlepower", power);
					CBTeamPoolBAC.getInstance().update(dbHelper, factoinid, sqlStr, "factionid="+factoinid+" and id="+teamRs.getInt("id"));
					//System.out.println("--------CBTeamPoolBAC----------update------------------");
				}
			}
		}
		//̫��ս��
		DBPsRs cityStorRs = CBBAC.getInstance().query(Conf.sid, "serverid="+Conf.sid+" and leaderid="+playerid);
		while(cityStorRs.next()){
			JSONArray posarr = new JSONArray(cityStorRs.getString("leaderposarr"));
			int power = 0;
			for(int i = 0; i < posarr.length(); i++){
				if(posarr.optInt(i) != 0){
					power += powerobj.optInt(posarr.optString(i));
				}
			}
			if(power != cityStorRs.getInt("leaderbattlepower")){
				SqlString sqlStr = new SqlString();
				sqlStr.add("leaderbattlepower", power);
				CBBAC.getInstance().update(dbHelper, Conf.sid, sqlStr, "serverid="+Conf.sid+" and id="+cityStorRs.getInt("id"));
				//System.out.println("--------CBBAC----------update------------------");
			}
		}
	}
	
	/**
	 * �����������ݷ��ػ���������
	 */
	public JSONArray getPartnerDataByPosarr(int playerid, JSONArray posarr) throws Exception {
		JSONObject posobj = new JSONObject();
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid);
		while(partnerStorRs.next()){
			JSONArray arr = new JSONArray();
			arr.add(partnerStorRs.getInt("id"));
			arr.add(partnerStorRs.getInt("num"));
			arr.add(partnerStorRs.getInt("lv"));
			arr.add(partnerStorRs.getInt("phase"));
			arr.add(partnerStorRs.getInt("star"));
			posobj.put(partnerStorRs.getString("id"), arr);
		}
		JSONArray dataarr = new JSONArray();
		for(int i = 0; i < posarr.length(); i++){
			int partnerid = posarr.getInt(i);
			if(partnerid != 0){
				dataarr.put(i, posobj.optJSONArray(String.valueOf(partnerid)));
			}
		}
		return dataarr;
	}
	
	/**
	 * ��ȡ����������
	 */
	public TeamBox getTeamBox(int playerid, int teamType, JSONArray posarr) throws Exception {
		return getTeamBox(playerid, teamType, posarr, null);
	}
	
	/**
	 * ��ȡSpriteBox����
	 * @param posarr ��NULL���ʾ��ȡ���л��
	 * @param extendlistener ����ֵ��չ�ص�
	 */
	public ArrayList<SpriteBox> getSpriteBoxList(int playerid, JSONArray posarr, SpriteBoxExtendPropListener extendlistener) throws Exception {
		JSONArray allPosarr = null;
		if(posarr == null){//�������ʾ��ȡ���л�������
			allPosarr = new JSONArray();
		}
		ArrayList<SpriteBox> sprites = new ArrayList<SpriteBox>();
		JSONObject fetter_partner = new JSONObject();//����
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid);
		while(partnerStorRs.next()){
			if(allPosarr != null){
				allPosarr.add(partnerStorRs.getInt("id"));
			}
			fetter_partner.put(partnerStorRs.getString("num"), partnerStorRs.getInt("star"));
		}
		JSONObject fetter_equip = new JSONObject();//װ������
		DBPsRs equipStorRs = EquipOrdinaryBAC.getInstance().query(playerid, "playerid="+playerid);
		while(equipStorRs.next()){
			fetter_equip.put(equipStorRs.getString("itemid"), new int[]{equipStorRs.getInt("num"), equipStorRs.getInt("strelv"), equipStorRs.getInt("starlv")});
		}
		double[][] artifactprop = ArtifactBAC.getInstance().getBattleProp(playerid);//��������
		if(allPosarr != null){
			posarr = allPosarr;
		}
		for(int i = 0; i < posarr.length(); i++){
			int partnerid = posarr.optInt(i);
			if(partnerid == 0){//��λ��û�л��
				sprites.add(null);
				continue;
			}
			partnerStorRs.beforeFirst();
			while(partnerStorRs.next()){//���һ��
				if(partnerStorRs.getInt("id") == partnerid){
					break;
				}
				if(partnerStorRs.getRow() == partnerStorRs.count()){
					BACException.throwAndPrintInstance("���ݴ��󣬻��IDδ�ҵ� partnerid="+partnerid);
				}
			}
			int num = partnerStorRs.getInt("num");
			int star = partnerStorRs.getInt("star");
			int lv = partnerStorRs.getInt("lv");
			int phase = partnerStorRs.getInt("phase");
			int[] orbnumarr = new int[6];//��������
			for(int k = 1; k <= 6; k++){
				orbnumarr[k-1] = partnerStorRs.getInt("orb"+k);
			}
			int[][] equiparr = new int[6][3];//װ������
			for(int k = 1; k <= 6; k++){
				int itemid = partnerStorRs.getInt("pos"+k);
				if(itemid != 0){
					int[] eqdata = (int[])fetter_equip.opt(String.valueOf(itemid));
					if(eqdata != null){
						equiparr[k-1] = eqdata;
					} else {
						System.out.println("===========ERROR��װ�������쳣��partnerid="+partnerid+" itemid="+itemid);
					}
				}
			}
			int[] skilllvarr = new int[5];//��������
			for(int k = 1; k <= 5; k++){
				skilllvarr[k-1] = partnerStorRs.getInt("skilllv"+k);
			}
			SpriteBox spritebox = getSpriteBox(playerid, partnerid, num, star, lv, phase, orbnumarr, equiparr, skilllvarr, fetter_partner, artifactprop, extendlistener);
			sprites.add(spritebox);
		}
		return sprites;
	}
	
	/**
	 * ��ȡ����������
	 */
	public TeamBox getTeamBox(int playerid, int teamType, JSONArray posarr, SpriteBoxExtendPropListener extendlistener) throws Exception {
		ArrayList<SpriteBox> sprites = getSpriteBoxList(playerid, posarr, extendlistener);
		TeamBox teambox = new TeamBox();
		for(int i = 0; i < sprites.size(); i++){
			SpriteBox spritebox = sprites.get(i);
			if(spritebox != null){
				spritebox.teamType = (byte)teamType;
				spritebox.posNum = (byte)(i+1);
				teambox.sprites.add(spritebox);
			}
		}
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		teambox.playerid = playerid;
		teambox.pname = plaRs.getString("name");
		teambox.pnum = plaRs.getInt("num");
		teambox.teamType = (byte)teamType;
		return teambox;
	}
	
	/**
	 * ��ȡ����������
	 */
	public TeamBox getTeamBox(int playerid, String pname, int pnum, int teamType, SpriteBox[][] spriteboxarr) throws Exception {
		TeamBox teambox = new TeamBox();
		for(int i = 0; i < spriteboxarr.length; i++){
			for(int j = 0; j < spriteboxarr[i].length; j++){
				if(spriteboxarr[i][j] != null){
					spriteboxarr[i][j].teamType = (byte)teamType;
					spriteboxarr[i][j].posNum = (byte)(i*3+j+1);
					teambox.sprites.add(spriteboxarr[i][j]);
				}
			}
		}
		teambox.playerid = playerid;
		teambox.pname = pname;
		teambox.pnum = pnum;
		teambox.teamType = (byte)teamType;
		return teambox;
	}
	
	/**
	 * ��ȡս������������
	 */
	public SpriteBox getSpriteBox(int playerid, int partnerid, int num, int star, int lv, int phase, int[] orbnumarr, int[][] equiparr, int[] skilllvarr, JSONObject fetter_partner, double[][] artifactprop) throws Exception {
		return getSpriteBox(playerid, partnerid, num, star, lv, phase, orbnumarr, equiparr, skilllvarr, fetter_partner, artifactprop, null);
	}
	
	/**
	 * ��ȡս������������
	 */
	public SpriteBox getSpriteBox(int playerid, int partnerid, int num, int star, int lv, int phase, int[] orbnumarr, int[][] equiparr, int[] skilllvarr, JSONObject fetter_partner, double[][] artifactprop, SpriteBoxExtendPropListener extendlistener) throws Exception {
		DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+num);
		SpriteBox spritebox = new SpriteBox();
		//��������
		spritebox.playerId = playerid;
		spritebox.partnerId = partnerid;
		spritebox.type = 1;
		spritebox.num = num;
		spritebox.level = (short)lv;
		spritebox.name = partnerRs.getString("name");
		spritebox.phase = (byte)phase;
		spritebox.star = (byte)star;
		spritebox.battletype = partnerRs.getByte("battletype");
		spritebox.sex = partnerRs.getByte("sex");
		/*
		 * ��������� = (���� + �ɳ�&�Ǽ� + ���� + �׼�)*�ٷֱ� + �������� + װ�� + ���� + ����ֵ
		 */
		//��ʼֵ
		for(int i = 0; i < 12; i++){
			if(i == Const.PROP_HP){
				continue;
			}
			spritebox.addProp(1, i, 1, partnerRs.getInt("init"+i));
		}
		spritebox.updateIngredientData("��ʼֵ");
		//�ɳ�&�Ǽ�
		DBPaRs starRs = DBPool.getInst().pQueryA(tab_partner_upstar, "star="+star);
		double growvalue = starRs.getDouble("growvalue")/10000;
		for(int i = 0; i <= Const.PROP_MAGICDEF; i++){
			int addvalue = (int)(partnerRs.getDouble("grow"+i) * growvalue * (lv-1));
			spritebox.addProp(1, i , 1, addvalue);
		}
		spritebox.updateIngredientData("�ɳ�&�Ǽ�");
		//����
		if(phase > 1){
			int[] upphasenumArr = Tools.splitStrToIntArr(partnerRs.getString("upphasenum"), ",");
			for(int i = 0; i < phase-1; i++){
				DBPaRs upphaseRs = DBPool.getInst().pQueryA(tab_partner_upphase, "num="+upphasenumArr[i]);
				for(int p = 1; p <= 6; p++){
					DBPaRs orbRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_ORB, upphaseRs.getInt("pos"+p));
					spritebox.addProp(orbRs.getString("attr"));
				}
			}
		}
		for(int i = 0; i < orbnumarr.length; i++){
			if(orbnumarr[i] != 0){
				DBPaRs orbRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_ORB, orbnumarr[i]);
				spritebox.addProp(orbRs.getString("attr"));
			}
		}
		spritebox.updateIngredientData("����");
		//�׼�
		DBPsRs phaseattrRs = DBPool.getInst().pQueryS(tab_partner_phase_attr, "num<="+phase);
		while(phaseattrRs.next()){
			spritebox.addProp(phaseattrRs.getString("battletype"+partnerRs.getInt("battletype")));
		}
		spritebox.updateIngredientData("�׼�");
		//�洢���ڼ���ٷֱȵ�����
		spritebox.saveBattlePropSave();
		//װ��
		JSONArray fetter_equiparr = new JSONArray();
		for(int i = 0; i < equiparr.length; i++){
			if(equiparr[i][0] != 0){
				int[] equipdata = getEquipBattleData(equiparr[i][0], equiparr[i][1], equiparr[i][2]);
				spritebox.addBattleProp(equipdata);
				fetter_equiparr.add(equiparr[i][0]);
			}
		}
		spritebox.updateIngredientData("װ��");
		//����
		spritebox.addProp(artifactprop);
		spritebox.updateIngredientData("����");
		//����
		spritebox.addSkill(partnerRs.getInt("skill"), skilllvarr[0]);
		int[][] bskilldata = Tools.splitStrToIntArr2(partnerRs.getString("bskill"), "|", ",");
		for(int i = 0; i < bskilldata.length; i++){
			if(bskilldata[i][1]!=0 && phase>=bskilldata[i][1]){
				spritebox.addSkill(bskilldata[i][0], skilllvarr[i+1]);
			}
		}
		//spritebox.updateIngredientData("");//����������Ӽ��ܣ����Բ��ü�¼
		//�
		int[] fetternumarr = Tools.splitStrToIntArr(partnerRs.getString("fetternum"), ",");
		for(int i = 0; i < fetternumarr.length; i++){
			DBPaRs fetterRs = DBPool.getInst().pQueryA(tab_partner_fetter, "num="+fetternumarr[i]);
			boolean match = true;
			int[][] func = Tools.splitStrToIntArr2(fetterRs.getString("func"), "|", ",");
			for(int k = 0; k < func.length; k++){
				if(func[k][0] == 1){
					match = fetter_partner.optInt(String.valueOf(func[k][1]))>=func[k][2];
				} else 
				if(func[k][0] == 2){
					match = fetter_equiparr.contains(func[k][1]);
				}
				if(!match){
					break;
				}
			}
			if(match){
				spritebox.addProp(fetterRs.getString("attr"));
			}
		}
		spritebox.updateIngredientData("�");
		if(extendlistener != null){//�Զ�����չ����
			extendlistener.extend(spritebox);
		}
		//ת��
		spritebox.conver();
		return spritebox;
	}
	
	/**
	 * ����Ƿ�ӵ��ָ�����
	 */
	public boolean checkHave(int playerid, int partnernum) throws Exception {
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+partnernum);
		return partnerStorRs.have();
	}
	
	/**
	 * ��ȡָ�������Ǽ�
	 */
	public int getStar(int playerid, int partnernum) throws Exception {
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+partnernum);
		if(partnerStorRs.next()){
			return partnerStorRs.getInt("star");
		} else {
			return 0;
		}
	}
	
	/**
	 * ��ȡָ���Ǽ��Ļ������
	 */
	public int getAmountByStar(int playerid, int star) throws Exception {
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and star>="+star);
		return partnerStorRs.count();
	}
	
	/**
	 * ��ȡ����
	 * @param posarr ����
	 * @param offlinenum ����ʹ��ֵ
	 */
	public int getPlayerBattlePower(int playerid, JSONArray posarr, int offlinenum) throws Exception {
		if(SocketServer.getInstance().checkOnline(playerid)){
			TeamBox teambox = getTeamBox(playerid, 0, posarr);
			return getTeamBoxBattlePower(teambox);		
		} else {
			return offlinenum;
		}
	}
	
	/**
	 * ��ȡ����
	 */
	public int getTeamBoxBattlePower(TeamBox teambox) throws Exception {
		int totalGrade = 0;
		for(int i = 0; i < teambox.sprites.size(); i++){
			SpriteBox spritebox = teambox.sprites.get(i);
			totalGrade += getSpriteBoxBattlePower(spritebox);
		}
		return totalGrade;
	}
	
	/**
	 * ��ȡ����
	 */
	public int getSpriteBoxBattlePower(SpriteBox spritebox) throws Exception {
		int battlepower = getBattlePower(spritebox.battletype, spritebox.battle_prop);
		battlepower += spritebox.skillAddBattlerPower;
		return battlepower;
	}
	
	/**
	 * ��ȡս��
	 * @return
	 */
	public int getBattlePower(int battletype, int[] battledata) throws Exception {
		double battlepower = 0;
		DBPsRs battlepowerRs = DBPool.getInst().pQueryS(tab_battlepower);
		while(battlepowerRs.next()){
			battlepower += battlepowerRs.getDouble("battletype"+battletype)*battledata[battlepowerRs.getInt("num")];
		}
		return (int)battlepower;
	}
	
	/**
	 * ��ȡװ�����ӵ�ս��ֵ
	 */
	public int[] getEquipBattleData(DBPRs equipStorRs) throws Exception {
		int num = equipStorRs.getInt("num");
		int strelv = equipStorRs.getInt("strelv");
		int starlv = equipStorRs.getInt("starlv");
		return getEquipBattleData(num, strelv, starlv);
	}
	
	/**
	 * ��ȡװ�����ӵ�ս��ֵ
	 */
	public int[] getEquipBattleData(int num, int strelv, int starlv) throws Exception {
		int[] equipdata = new int[SpriteBox.BATTLE_PROP_LEN];
		DBPaRs equipRs = ItemBAC.getInstance().getListRs(ItemBAC.TYPE_EQUIP_ORDINARY, num);
		int[] attrtypearr = Tools.splitStrToIntArr(equipRs.getString("attrtype"), ",");
		int[] baseattrarr = Tools.splitStrToIntArr(equipRs.getString("baseattr"), ",");
		int[] stregrowarr = Tools.splitStrToIntArr(equipRs.getString("stregrow"), ",");
		double star_percent = 0;
		if(starlv > 0){
			DBPaRs upstarRs = DBPool.getInst().pQueryA(EquipOrdinaryBAC.tab_equip_upstar, "star="+starlv);
			star_percent = upstarRs.getDouble("add"+equipRs.getInt("rare"));
		}
		for(int k = 0; k < attrtypearr.length; k++){
			int addvalue = (int)(baseattrarr[k] + stregrowarr[k] * strelv * ((100 + star_percent) / 100));
			//System.out.println("addvalue:"+addvalue);
			equipdata[attrtypearr[k]] += addvalue;
			//System.out.println("--------num-"+num+"-strelv-"+strelv+"-starlv-"+starlv+"-baseattrarr-"+baseattrarr[k]+"-stregrowarr-"+stregrowarr[k]+"-star_percent-"+star_percent+"-------addvalue-"+addvalue+"-equ-"+equipdata[attrtypearr[k]]+"----------");
		}
		return equipdata;
	}
	
	/**
	 * �����鴩��״̬ת��Ϊ�������
	 */
	public int[] converOrbStateToNum(int phase, String upphasenumStr, String stateStr) throws Exception {
		int[] numarr = new int[6];
		int[] upphasenumArr = Tools.splitStrToIntArr(upphasenumStr, ",");
		DBPaRs partnerUpphaseRs = DBPool.getInst().pQueryA(PartnerBAC.tab_partner_upphase, "num="+upphasenumArr[phase-1]);//���׶γ�ʼֵΪ1
		int[] statearr = Tools.splitStrToIntArr(stateStr, ",");
		for(int k = 0; k < numarr.length; k++){
			if(statearr[k] == 1){
				numarr[k] = partnerUpphaseRs.getInt("pos"+(k+1));
			}
		}
		return numarr;
	}
	
	/**
	 * ��װ������״̬ת��Ϊ��������
	 */
	public int[][] converEquipStateToData(String equipStr) throws Exception {
		int[][] equiparr = new int[6][3];
		int[][] parequip = null;
		if(!equipStr.equals("0")){
			parequip = Tools.splitStrToIntArr2(equipStr, "|", ",");
		}
		for(int k = 0; parequip != null && k < equiparr.length; k++){
			int equipnum = 0;
			if(parequip[k][0] == 1){
				equipnum = parequip[k][1];
			} else 
			if(parequip[k][0] == 2){
				DBPsRs equipRs = DBPool.getInst().pQueryS(ItemBAC.getInstance().getTab(ItemBAC.TYPE_EQUIP_ORDINARY), "equiptype="+(k+1)+" and rare="+parequip[k][1]);
				equipRs.setRow(MyTools.getRandom(1, equipRs.count()));
				equipnum = equipRs.getInt("num");
			}
			equiparr[k][0] = equipnum;//���
			equiparr[k][1] = parequip[k][2];//ǿ���ȼ�
			equiparr[k][2] = parequip[k][3];//�Ǽ�
		}
		return equiparr;
	}
	
	/**
	 * ��û��
	 */
	public void obtainPartner(DBHelper dbHelper, int playerid, int num, JSONArray itemarr, JSONArray partnerarr, GameLog gl) throws Exception {
		DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+num);
		DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+num);
		if(partnerStorRs.have()){
			int initstar = partnerRs.getInt("initstar");
			//DBPsRs upStarRs = DBPool.getInst().pQueryS(tab_partner_upstar, "star<="+initstar);
			int addamount = 0;
			if(initstar == 1){
				addamount = 7;
			} else 
			if(initstar == 2){
				addamount = 16;
			} else 
			if(initstar == 3){
				addamount = 30;
			}
			JSONArray arr = ItemBAC.getInstance().add(dbHelper, playerid, ItemBAC.TYPE_SOUL_STONE, partnerRs.getInt("needsoulstone"), addamount, ItemBAC.ZONE_BAG, ItemBAC.SHORTCUT_MAIL, 1, gl);
			MyTools.combJsonarr(itemarr, arr);
			gl.addRemark("��ӵ�л�� "+partnerRs.getString("name")+" �����λ�û��ֽ�Ϊ��ʯ");
		} else {
			int partnerid = create(dbHelper, playerid, num, partnerRs.getInt("awaken")==0?1:0, 1, 1, partnerRs.getInt("initstar"), null, null, null);
			partnerarr.add(new JSONArray(new int[]{num, partnerid}));
			gl.addRemark("��û�� "+GameLog.formatNameID(partnerRs.getString("name"), partnerid));
		}
	}
	
	/**
	 * ���Ի�ԭ���
	 */
	public ReturnValue debugResetPartner(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPsRs partnerStorRs = query(playerid, "playerid="+playerid+" and num="+num);
			if(!partnerStorRs.next()){
				BACException.throwInstance("��δ��ô˻��");
			}
			DBPaRs partnerRs = DBPool.getInst().pQueryA(tab_partner, "num="+num);
			SqlString sqlStr = new SqlString();
			sqlStr.add("lv", 1);
			sqlStr.add("exp", 0);
			sqlStr.add("phase", 1);
			sqlStr.add("star", partnerRs.getInt("initstar"));
			for(int i = 0; i < 6; i++){
				if(partnerStorRs.getInt("pos"+(i+1)) != 0){
					putoffEquip(playerid, partnerStorRs.getInt("id"), i+1);
					sqlStr.add("pos"+(i+1), 0);
				}
			}
			for(int i = 0; i < 6; i++){
				sqlStr.add("orb"+(i+1), 0);
			}
			for(int i = 0; i < 5; i++){
				sqlStr.add("skilllv"+(i+1), 1);
			}
			update(dbHelper, playerid, sqlStr, "playerid="+playerid+" and num="+num);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������
	 */
	public int create(DBHelper dbHelper, int playerid, int num, int awaken, int lv, int phase, int star, int[] equiparr, int[] orbarr, int[] skilvarr) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("serverid", Conf.sid);
		sqlStr.add("num", num);
		sqlStr.add("awaken", awaken);
		sqlStr.add("lv", lv);
		sqlStr.add("exp", 0);
		sqlStr.add("phase", phase);
		sqlStr.add("star", star);
		for(int i = 0; i < 6; i++){
			if(equiparr != null){
				sqlStr.add("pos"+(i+1), equiparr[i]);
			} else {
				sqlStr.add("pos"+(i+1), 0);
			}
		}
		for(int i = 0; i < 6; i++){
			if(equiparr != null){
				sqlStr.add("orb"+(i+1), orbarr[i]);
			} else {
				sqlStr.add("orb"+(i+1), 0);
			}
		}
		for(int i = 0; i < 5; i++){
			if(skilvarr != null){
				sqlStr.add("skilllv"+(i+1), skilvarr[i]);
			} else {
				sqlStr.add("skilllv"+(i+1), 1);
			}
		}
		return insert(dbHelper, playerid, sqlStr);
	}
	
	//--------------��̬��--------------
	
	private static PartnerBAC instance = new PartnerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static PartnerBAC getInstance(){
		return instance;
	}
}
