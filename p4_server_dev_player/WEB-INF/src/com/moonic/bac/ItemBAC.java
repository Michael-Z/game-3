package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPRs;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;
import conf.LogTbName;

/**
 * ��ƷBAC
 * @author John
 */
public class ItemBAC extends PlaStorBAC {
	public static final String tab_item_type = "tab_item_type";
	public static final String tab_buy_expitem = "tab_buy_expitem";
	//��Ʒ����
	public static final byte TYPE_ALL = 0;
	public static final byte TYPE_PROP_CONSUME = 1;
	public static final byte TYPE_LOTTERY = 2;
	public static final byte TYPE_GIFT = 3;
	public static final byte TYPE_EQUIP_ORDINARY = 4;
	public static final byte TYPE_ORB = 5;
	public static final byte TYPE_ORB_DEBRIS = 6;
	public static final byte TYPE_SOUL_STONE = 7;
	public static final byte TYPE_EQUIP_DEBRIS = 8;
	public static final byte TYPE_CHOOSE_GIFT = 9;
	public static final byte TYPE_ARTIFACT_DEBRIS = 10;
	//�ռ�����
	public static final byte ZONE_MYALL = -1;//�ҵ����пռ�(���������۳�������Ȳ���ֱ��ӵ�еĿռ�)
	public static final byte ZONE_BAG = 0;//����
	public static final byte ZONE_USE = 3;//ʹ����
	public static final byte ZONE_SELL = 4;//���۳�
	public static final byte ZONE_MAIL = 5;//����
	//�ռ䲻��ʱ�Ĳ���
	public static final byte SHORTCUT_DISCARD = 1;//����
	public static final byte SHORTCUT_MAIL = 2;//�ʼ�����
	
	public static final String[] itemZoneName = {"����", "", "", "ʹ����", "���۳�", "����", ""};
	
	/**
	 * ����
	 */
	public ItemBAC(){
		super("tab_item_stor", "playerid", "id");
	}
	
	/**
	 * ��ȡ��Ʒ�б�
	 */
	public JSONArray getItemList(int playerid) throws Exception {
		DBPsRs dataRs = query(playerid, "playerid="+playerid);
		JSONObject infoobj = new JSONObject();
		EquipOrdinaryBAC.getInstance().getLoginItemInfo(playerid, infoobj);
		JSONArray bagarr = new JSONArray();
		JSONArray usearr = new JSONArray();
		//System.out.println("infoobj:"+infoobj);
		while(dataRs.next()){
			int itemid = dataRs.getInt("id");
			int itemtype = dataRs.getInt("itemtype");
			JSONObject obj = new JSONObject();
			obj.put("id", itemid);
			obj.put("type", itemtype);
			obj.put("num", dataRs.getInt("itemnum"));
			obj.put("amount", dataRs.getInt("itemamount"));
			//System.out.println("itemid:"+itemid);
			Object theobj = infoobj.opt(String.valueOf(itemid));
			if(theobj != null){
				obj.put("info", theobj);
			}
			int zone = dataRs.getInt("zone");
			if(zone == ZONE_BAG){
				bagarr.add(obj);
			} else 
			if(zone == ZONE_USE){
				usearr.add(obj);
			}
		}
		JSONArray jsonarr = new JSONArray();
		jsonarr.add(bagarr);
		jsonarr.add(usearr);
		return jsonarr;
	}
	
	/**
	 * ���Զ���Ʒ
	 */
	public ReturnValue debugDiscardItem(int playerid, int itemid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			JSONObject jsonobj = remove(dbHelper, playerid, itemid, ZONE_MYALL, gl);
			
			gl.addItemChaNoteObj(jsonobj)
			.addRemark("���Զ���Ʒ")
			.save();
			return new ReturnValue(true);
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������Ʒ
	 */
	public ReturnValue discardItem(int playerid, int itemid) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_DISCARD);
			JSONObject jsonobj = remove(dbHelper, playerid, itemid, ZONE_BAG, gl);
			
			gl.addItemChaNoteObj(jsonobj)
			.save();
			return new ReturnValue(true);
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ʹ�����ĵ���
	 */
	public ReturnValue useConsumeProp(int playerid, int target, int itemid, int useamount){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_USECONSUME);
			JSONArray itemarr = useConsumeProp(dbHelper, playerid, target, itemid, useamount, gl);
			
			gl.addItemChaNoteArr(itemarr);
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ʹ�����ĵ���
	 * @param target ʹ��Ŀ�꣬��Ŀ�괫0
	 */
	public JSONArray useConsumeProp(DBHelper dbHelper, int playerid, int target, int itemid, int useamount, GameLog gl) throws Exception {
		JSONArray itemarr = new JSONArray();
		DBPaRs itemStorRs = getDataRs(playerid, TYPE_PROP_CONSUME, itemid, ZONE_BAG);
		int itemnum = itemStorRs.getInt("itemnum");
		DBPaRs listRs = getListRs(TYPE_PROP_CONSUME, itemnum);
		String expireStr = listRs.getString("exprietime");
		if(!"-1".equals(expireStr)){
			if(MyTools.checkSysTimeBeyondSqlDate(expireStr)){
				BACException.throwInstance("��Ʒ�ѳ���ʹ������");
			}
		}
		int itemamount = itemStorRs.getInt("itemamount");
		if(itemamount < useamount){
			BACException.throwInstance("��Ʒ�������� "+useamount+"/"+itemamount);
		}
		int[] effect = Tools.splitStrToIntArr(listRs.getString("effect"), ",");
		int effectType = effect[0];//Ч������
		if(effectType == 1){//��������
			PlaRoleBAC.getInstance().addValue(dbHelper, playerid, "energy", useamount*effect[1], gl, "����");
		} else 
		if(effectType == 2){//���ӻ�龭��
			PartnerBAC.getInstance().addExp(dbHelper, playerid, target, useamount*effect[1], gl);
		}
		JSONObject jsonobj = remove(dbHelper, playerid, itemid, useamount, ZONE_BAG, gl);
		itemarr.add(jsonobj);
		return itemarr;
	}
	
	/**
	 * �������д����۵ĵ���
	 */
	public ReturnValue sellMoneyItem(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_SELLMONEYITEM);
			JSONArray numarr = new JSONArray();
			JSONArray pricearr = new JSONArray();
			DBPsRs moneyitemRs = DBPool.getInst().pQueryS(getTab(TYPE_PROP_CONSUME), "effect='3'");
			while(moneyitemRs.next()){
				numarr.add(moneyitemRs.getInt("num"));
				pricearr.add(moneyitemRs.getInt("sellprice"));
			}
			int addmoney = 0;
			JSONArray itemarr  = new JSONArray();
			DBPsRs itemStorRs = query(playerid, "playerid="+playerid+" and itemtype="+TYPE_PROP_CONSUME+" and zone="+ZONE_BAG);
			while(itemStorRs.next()){
				int index = numarr.indexOf(itemStorRs.getInt("itemnum"));
				if(index != -1){
					addmoney += pricearr.optInt(index)*itemStorRs.getInt("itemamount");
					JSONObject obj = remove(dbHelper, playerid, itemStorRs.getInt("id"), ZONE_BAG, gl);
					itemarr.add(obj);
				}
			}
			if(addmoney <= 0){
				BACException.throwInstance("û�д����۵ĵ���");
			}
			PlayerBAC.getInstance().addValue(dbHelper, playerid, "money", addmoney, gl, GameLog.TYPE_MONEY);
			
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
	 * ����ҩ
	 */
	public ReturnValue buyExpItem(int playerid, int buynum, int amount){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs buyexpitemRs = DBPool.getInst().pQueryA(tab_buy_expitem, "num="+buynum);
			if(!buyexpitemRs.exist()){
				BACException.throwInstance("�����Ų����� buynum="+buynum);
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_BUY_EXPITEM);
			int[] pricearr = Tools.splitStrToIntArr(buyexpitemRs.getString("price"), ",");
			if(pricearr[0] == 1){
				PlayerBAC.getInstance().useMoney(dbHelper, playerid, pricearr[1]*amount, gl);
			} else 
			if(pricearr[0] == 2){
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, pricearr[1]*amount, gl);
			}
			JSONArray itemarr = add(dbHelper, playerid, TYPE_PROP_CONSUME, buyexpitemRs.getInt("itemnum"), buyexpitemRs.getInt("amountmul")*amount, ZONE_BAG, SHORTCUT_MAIL, 1, gl);
			
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
	 * ʹ�ó齱��Ʒ
	 */
	public ReturnValue useLottery(int playerid, int itemid, int amount){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_USELOTTERY);
			DBPaRs itemStorRs = getDataRs(playerid, TYPE_LOTTERY, itemid, ZONE_BAG);
			DBPRs itemRs = getListRs(itemStorRs.getInt("itemtype"), itemStorRs.getInt("itemnum"));
			JSONArray indexarr = new JSONArray();
			int[][] award = new int[amount][];
			for(int i = 0; i < amount; i++){
				int index = MyTools.getIndexOfRandom(itemRs.getString("odds"));//���ȷ����Ʒ
				int[] split = Tools.splitStrToIntArr2(itemRs.getString("obtain"), "|", ",")[index];
				indexarr.add(index);
				award[i] = (int[])enterItem(split, "lotteryodds")[0];
			}
			JSONObject itemobj = remove(dbHelper, playerid, itemid, amount, ZONE_BAG, gl);
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, ItemBAC.SHORTCUT_MAIL, 2, gl);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(indexarr);//�����±�
			returnarr.add(awardarr);//����
			
			gl.addItemChaNoteObj(itemobj);
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ʹ��ѡ�������
	 */
	public ReturnValue useChooseGift(int playerid, int itemid, int index, int amount){
		DBHelper dbHelper = new DBHelper();
		try {
			if(index < 0){
				BACException.throwInstance("�±���� index="+index);
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_USE_CHOOSEGIFT);
			DBPaRs itemStorRs = getDataRs(playerid, TYPE_CHOOSE_GIFT, itemid, ZONE_BAG);
			DBPRs itemRs = getListRs(itemStorRs.getInt("itemtype"), itemStorRs.getInt("itemnum"));
			int[][] chooselist = Tools.splitStrToIntArr2(itemRs.getString("chooselist"), "|", ",");
			if(index >= chooselist.length){
				BACException.throwInstance("�±���� index="+index+" len="+chooselist.length);
			}
			JSONObject itemobj = remove(dbHelper, playerid, itemid, amount, ZONE_BAG, gl);
			int[][] award = new int[amount][];
			for(int i = 0; i < amount; i++){
				award[i] = chooselist[index];
			}
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, award, SHORTCUT_MAIL, 1, gl);
			
			gl.addItemChaNoteObj(itemobj);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ģ����Ʒת��Ϊȷ����Ʒ
	 */
	public Object[] enterItem(int[] split, String oddsCol) throws Exception {
		if(split[0] == 304){//װ��Ʒ�ʣ�����
			DBPsRs randomItemRs = DBPool.getInst().pQueryS(getTab(TYPE_EQUIP_ORDINARY), "rare="+split[1]);
			int totalodds = 0;
			while(randomItemRs.next()){
				totalodds += randomItemRs.getInt(oddsCol);
			}
			int ran = MyTools.getRandom(1, totalodds);
			randomItemRs.beforeFirst();
			int currodds = 0;
			while(randomItemRs.next()){
				currodds += randomItemRs.getInt(oddsCol);
				if(ran <= currodds){
					break;
				}
			}
			return new Object[]{new int[]{1, TYPE_EQUIP_ORDINARY, randomItemRs.getInt("num"), split[2]}, randomItemRs};
		} else 
		if(split[0] == 305){//����Ʒ�ʣ�����
			DBPsRs randomItemRs = DBPool.getInst().pQueryS(getTab(TYPE_ORB), "rare="+split[1]);
			int totalodds = 0;
			while(randomItemRs.next()){
				totalodds += randomItemRs.getInt(oddsCol);
			}
			int ran = MyTools.getRandom(1, totalodds);
			randomItemRs.beforeFirst();
			int currodds = 0;
			while(randomItemRs.next()){
				currodds += randomItemRs.getInt(oddsCol);
				if(ran <= currodds){
					break;
				}
			}
			return new Object[]{new int[]{1, TYPE_ORB, randomItemRs.getInt("num"), split[2]}, randomItemRs};
		} else 
		if(split[0] == 306){//������ƬƷ�ʣ�����
			DBPsRs randomItemRs = DBPool.getInst().pQueryS(getTab(TYPE_ORB_DEBRIS), "rare="+split[1]);
			int totalodds = 0;
			while(randomItemRs.next()){
				totalodds += randomItemRs.getInt(oddsCol);
			}
			int ran = MyTools.getRandom(1, totalodds);
			randomItemRs.beforeFirst();
			int currodds = 0;
			while(randomItemRs.next()){
				currodds += randomItemRs.getInt(oddsCol);
				if(ran <= currodds){
					break;
				}
			}
			return new Object[]{new int[]{1, TYPE_ORB_DEBRIS, randomItemRs.getInt("num"), split[2]}, randomItemRs};
		} else 
		if(split[0] == 307){//��ʯ�飬����
			DBPsRs randomItemRs = DBPool.getInst().pQueryS(getTab(TYPE_SOUL_STONE), "lotterygroup="+split[1]);
			int totalodds = 0;
			while(randomItemRs.next()){
				totalodds += randomItemRs.getInt(oddsCol);
			}
			int ran = MyTools.getRandom(1, totalodds);
			randomItemRs.beforeFirst();
			int currodds = 0;
			while(randomItemRs.next()){
				currodds += randomItemRs.getInt(oddsCol);
				if(ran <= currodds){
					break;
				}
			}
			return new Object[]{new int[]{1, TYPE_SOUL_STONE, randomItemRs.getInt("num"), split[2]}, randomItemRs};
		} else 
		if(split[0] == 308){//װ����ƬƷ�ʣ�����
			DBPsRs randomItemRs = DBPool.getInst().pQueryS(getTab(TYPE_EQUIP_DEBRIS), "rare="+split[1]);
			int totalodds = 0;
			while(randomItemRs.next()){
				totalodds += randomItemRs.getInt(oddsCol);
			}
			int ran = MyTools.getRandom(1, totalodds);
			randomItemRs.beforeFirst();
			int currodds = 0;
			while(randomItemRs.next()){
				currodds += randomItemRs.getInt(oddsCol);
				if(ran <= currodds){
					break;
				}
			}
			return new Object[]{new int[]{1, TYPE_EQUIP_DEBRIS, randomItemRs.getInt("num"), split[2]}, randomItemRs};
		} else 
		{
			return new Object[]{split, null};
		}
	}
	
	/**
	 * ������
	 */
	public ReturnValue compOrb(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_COMPORB);
			DBPaRs orbRs = getListRs(TYPE_ORB, num);
			if(orbRs.getString("need").equals("0")){
				BACException.throwInstance("�������޷��ϳ� num="+num);
			}
			int[][] needarr = Tools.splitStrToIntArr2(orbRs.getString("need"), "|", ",");
			JSONArray itemarr = new JSONArray();
			for(int i = 0; i < needarr.length; i++){
				JSONArray arr = remove(dbHelper, playerid, needarr[i][0], needarr[i][1], needarr[i][2], ZONE_BAG, gl);
				MyTools.combJsonarr(itemarr, arr);
			}
			JSONArray arr2 = add(dbHelper, playerid, TYPE_ORB, num, 1, ZONE_BAG, SHORTCUT_MAIL, 1, gl);
			MyTools.combJsonarr(itemarr, arr2);
			
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
	 * ��װ��
	 */
	public ReturnValue compEquip(int playerid, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_COMPEQUIP);
			DBPaRs equipRs = getListRs(TYPE_EQUIP_ORDINARY, num);
			if(equipRs.getString("need").equals("0")){
				BACException.throwInstance("��װ���޷��ϳ� num="+num);
			}
			int[] needdata = Tools.splitStrToIntArr(equipRs.getString("need"), ",");
			JSONArray itemarr = new JSONArray();
			JSONArray arr = remove(dbHelper, playerid, TYPE_EQUIP_DEBRIS, num, needdata[0], ZONE_BAG, gl);
			MyTools.combJsonarr(itemarr, arr);
			JSONArray arr2 = add(dbHelper, playerid, TYPE_EQUIP_ORDINARY, num, 1, ZONE_BAG, SHORTCUT_MAIL, 1, gl);
			MyTools.combJsonarr(itemarr, arr2);
			
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
	 * ������Ʒ
	 */
	public ReturnValue sell(int playerid, int itemid, int sellamount){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_SELL);
			DBPaRs itemStorRs = getDataRs(playerid, TYPE_ALL, itemid, ZONE_BAG);
			DBPaRs itemRs = getListRs(itemStorRs.getInt("itemtype"), itemStorRs.getInt("itemnum"));
			if(itemRs.getInt("sellprice") == -1){
				BACException.throwInstance("����Ʒ�޷�����");
			}
			JSONObject itemobj = remove(dbHelper, playerid, itemid, sellamount, ZONE_BAG, gl);
			PlayerBAC.getInstance().addValue(dbHelper, playerid, "money", itemRs.getInt("sellprice")*sellamount, gl, GameLog.TYPE_MONEY);
			
			gl.addItemChaNoteObj(itemobj);
			gl.save();
			return new ReturnValue(true, itemobj.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ڻ�
	 */
	public ReturnValue smelt(int playerid, int itemid, int smeltamount){
		DBHelper dbHelper = new DBHelper();
		try {
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_SMELT);
			DBPaRs itemStorRs = getDataRs(playerid, TYPE_ALL, itemid, ZONE_BAG);
			JSONObject itemobj = remove(dbHelper, playerid, itemid, smeltamount, ZONE_BAG, gl);
			DBPaRs itemRs = getListRs(itemStorRs.getInt("itemtype"), itemStorRs.getInt("itemnum"));
			PlaRoleBAC.getInstance().addValue(dbHelper, playerid, "soulpoint", itemRs.getInt("soulpoint")*smeltamount, gl, "���");
			
			gl.addItemChaNoteObj(itemobj);
			gl.save();
			return new ReturnValue(true, itemobj.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���Լ���Ʒ
	 */
	public ReturnValue debugAddItem(int playerid, int itemtype, int itemnum, int addamount, byte zone, String extendStr){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_DEBUG_GAME_LOG);
			JSONArray jsonarr = add(dbHelper, playerid, itemtype, itemnum, addamount, zone, SHORTCUT_MAIL, !extendStr.equals("")?new JSONArray(Tools.splitStrToIntArr(extendStr, ",")):null, 0, gl);
			
			gl.addItemChaNoteArr(jsonarr);
			gl.addRemark("���Լ���Ʒ");
			gl.save();
			return new ReturnValue(true, MyTools.getFormatJsonarrStr(jsonarr));
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����
	 */
	public ReturnValue openGift(int playerid, int itemid, int amount){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			DBPaRs itemStorRs = getDataRs(playerid, TYPE_GIFT, itemid, ZONE_BAG);
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			DBPRs itemRs = getListRs(itemStorRs.getInt("itemtype"), itemStorRs.getInt("itemnum"));
			int uselv = itemRs.getInt("uselv");
			if(plaRs.getInt("lv") < uselv){
				BACException.throwInstance("�����㿪���ȼ�����");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_OPEN_GIFT);
			JSONObject jsonobj = remove(dbHelper, playerid, itemid, amount, ZONE_BAG, gl);
			StringBuffer awardSb = new StringBuffer();
			for(int i = 0; i < amount; i++){
				if(awardSb.length() > 0){
					awardSb.append("|");
				}
				awardSb.append(itemRs.getString("obtain"));
			}
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, awardSb.toString(), ItemBAC.SHORTCUT_MAIL, 1, gl);
			
			gl.addItemChaNoteObj(jsonobj);
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ָ����Ʒ�ƶ���ָ���ռ�
	 */
	public ReturnValue moveToZone(int playerid, int itemid, byte srczone, byte targetzone){
		DBHelper dbHelper = new DBHelper();
		try {
			if(srczone == targetzone){
				BACException.throwInstance("��Ч�ƶ�");
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_ITEM_MOVE);
			JSONArray jsonarr = moveToZone(dbHelper, playerid, itemid, srczone, targetzone, true, gl);
			String returnStr = null;
			if(jsonarr != null){
				returnStr = jsonarr.toString();
			}
			
			gl.addItemChaNoteArr(jsonarr);
			gl.save();
			return new ReturnValue(true, returnStr);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ��Ʒ����
	 */
	public ReturnValue getItemDataEx(int ownerid, int itemid) throws Exception {
		try {
			JSONObject itemobj = getItemData(ownerid, itemid, ZONE_MYALL);
			return new ReturnValue(true, itemobj.toString());		
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �ƶ���Ʒ��ָ���ռ�
	 * @param checkGroup �Ƿ��Բ���ϲ���ΪTRUEʱ���Բ���Ŀ��ռ���ͬ��Ʒ�飬ΪFALSEʱֱ�Ӹı���Ʒ���ڿռ�
	 */
	public JSONArray moveToZone(DBHelper dbHelper, int playerid, int itemid, byte srczone, byte targetzone, boolean checkGroup, GameLog gl) throws Exception {
		DBPaRs itemStorRs = getDataRs(playerid, TYPE_ALL, itemid, srczone);
		int itemtype = itemStorRs.getInt("itemtype");
		int itemnum = itemStorRs.getInt("itemnum");
		int itemamount = itemStorRs.getInt("itemamount");
		DBPaRs typeRs = getTypeListRs(itemtype);
		DBPaRs itemRs = getListRs(itemtype, itemnum);
		JSONArray jsonarr = null;//����ֵ
		if((typeRs.getInt("multi")==2 || (typeRs.getInt("multi")==1 && itemamount<itemRs.getInt("groupam"))) && checkGroup) {//��Ʒ�ɵ��ӣ��Ҳ���һ�飬��Ҫ���飬�����ȳ��Բ���Ŀ��ռ�
			JSONObject jsonobj = new JSONObject().put("jsonarr", new JSONArray());//����ִ����Ʒ�仯�Ķ���
			addByJson(playerid, itemtype, itemnum, itemamount, targetzone, null, jsonobj);//������Ʒ����Ʒ�仯����
			jsonarr = jsonobj.optJSONArray("jsonarr");
			JSONObject srcobj = jsonarr.optJSONObject(jsonarr.length()-1);//���һ������
			if(srcobj.optInt("id")==0){//��ʣ�����������ԭʼ��Ʒ�޸�(ǰ��Ϊ�ƶ�����Ʒ�������ᳬ����������)
				jsonarr.remove(srcobj);//�Ƴ�ʣ�������Ч���ݣ�Դ��Ʒֻ�ᱻ�����������޸Ŀռ䣬����������
				if(jsonarr.length() >= 1){//�в������(����һ����������)�����޸Ŀռ�ͼ�������
					changeSqlByJson(dbHelper, playerid, targetzone, SHORTCUT_MAIL, jsonobj, 0, gl);
					SqlString itemSqlStr = new SqlString();
					itemSqlStr.add("itemamount", srcobj.optInt("amount"));
					itemSqlStr.add("zone", targetzone);
					update(dbHelper, playerid, itemid, srczone, itemSqlStr);
					jsonarr.add(getJsonObj(itemid, itemtype, itemnum, srcobj.optInt("amount"), itemamount, targetzone, srczone, null));//�����������ڿռ�
				} else {//�޲�����̣����޸Ŀռ�
					SqlString sqlStr = new SqlString();
					sqlStr.add("zone", targetzone);
					update(dbHelper, playerid, itemid, srczone, sqlStr);
					jsonarr.add(getJsonObj(itemid, itemtype, itemnum, itemamount, itemamount, targetzone, srczone, null));//�����ڿռ�
				}
			} else {//��ʣ����������ֱ��ɾ��
				changeSqlByJson(dbHelper, playerid, targetzone, SHORTCUT_MAIL, jsonobj, 0, gl);
				delete(dbHelper, playerid, itemid, srczone);
				jsonarr.add(getJsonObj(itemid, itemtype, itemnum, 0, itemamount, srczone, srczone, null));//������
			}
		} else {//����Ҫ��������Ʒ�����޸���Ʒ���ڿռ�
			if(getRemainZone(playerid, targetzone)==0){
				BACException.throwInstance(itemZoneName[targetzone]+"�ռ䲻��");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("zone", targetzone);
			update(dbHelper, playerid, itemid, srczone, sqlStr);
			jsonarr = new JSONArray();
			jsonarr.add(getJsonObj(itemid, itemtype, itemnum, itemamount, itemamount, targetzone, srczone, null));//�����ڿռ�
		}
		return jsonarr;
	}
	
	/**
	 * ����Ʒ(Ĭ������չ����)
	 */
	public JSONArray add(DBHelper dbHelper, int playerid, int itemtype, int itemnum, int addamount, byte zone, byte shortcut, int from, GameLog gl) throws Exception{
		return add(dbHelper, playerid, itemtype, itemnum, addamount, zone, shortcut, null, from, gl);
	}
	
	/**
	 * ����Ʒ
	 */
	public JSONArray add(DBHelper dbHelper, int playerid, int itemtype, int itemnum, int addamount, byte zone, byte shortcut, JSONArray extendarr, int from, GameLog gl) throws Exception{
		JSONObject jsonobj = new JSONObject().put("jsonarr", new JSONArray());
		addByJson(playerid, itemtype, itemnum, addamount, zone, extendarr, jsonobj);
		changeSqlByJson(dbHelper, playerid, zone, shortcut, jsonobj, from, gl);
		return jsonobj.optJSONArray("jsonarr");
	}
	
	/**
	 * ����Ʒ(��ID) �����Ƴ�
	 */
	public JSONObject remove(DBHelper dbHelper, int playerid, int itemid, byte zone, GameLog gl) throws Exception{
		return remove(dbHelper, playerid, itemid, 0, zone, gl);
	}
	
	/**
	 * ����Ʒ(��ID)
	 * @param removeamount �������� Ϊ0ʱ�Ƴ�����
	 */
	public JSONObject remove(DBHelper dbHelper, int playerid, int itemid, int removeamount, byte zone, GameLog gl) throws Exception{
		JSONObject jsonobj = new JSONObject().put("jsonarr", new JSONArray());
		removeByJson(playerid, itemid, removeamount, zone, jsonobj);
		changeSqlByJson(dbHelper, playerid, zone, SHORTCUT_MAIL, jsonobj, 0, gl);
		return jsonobj.optJSONArray("jsonarr").getJSONObject(0);
	}
	
	/**
	 * ����Ʒ(������)
	 */
	public JSONArray remove(DBHelper dbHelper, int playerid, int itemtype, int itemnum, int removeamount, byte zone, GameLog gl) throws Exception{
		JSONObject jsonobj = new JSONObject().put("jsonarr", new JSONArray());
		removeByJson(playerid, itemtype, itemnum, removeamount, zone, jsonobj);
		changeSqlByJson(dbHelper, playerid, zone, SHORTCUT_MAIL, jsonobj, 0, gl);
		return jsonobj.optJSONArray("jsonarr");
	}
	
	/**
	 * ��ȡָ�����ͱ����Ʒ������
	 */
	public int getAmount(int playerid, int itemtype, int itemnum, byte zone) throws Exception {
		DBPsRs rs = getDataRs(playerid, itemtype, itemnum, zone, null);
		int amount = (int)rs.sum("itemamount");
		return amount;
	}
	
	/**
	 * ����Ʒ
	 */
	public void addByJson(int playerid, int itemtype, int itemnum, int addamount, byte zone, JSONArray extendarr, JSONObject jsonobj) throws Exception{
		int needzone = jsonobj.optInt("needzone");
		JSONArray jsonarr = jsonobj.optJSONArray("jsonarr");
		DBPaRs typeRs = getTypeListRs(itemtype);
		if(typeRs.getInt("multi")==0){
			for(int i = 0; i < addamount; i++){
				needzone++;
				jsonarr.add(getJsonObj(0, itemtype, itemnum, 1, 0, zone, zone, extendarr));
			}
		} else {
			int groupam = 0;//������������
			if(typeRs.getInt("multi") == 1){
				groupam = getListRs(itemtype, itemnum).getInt("groupam");
			} else 
			if(typeRs.getInt("multi") == 2){
				groupam = Integer.MAX_VALUE;
			}
			while(addamount > groupam){
				needzone++;
				jsonarr.add(getJsonObj(0, itemtype, itemnum, groupam, 0, zone, zone, extendarr));
				addamount -= groupam;
			}
			DBPsRs rs = getDataRs(playerid, itemtype, itemnum, zone, null);
			while(rs.next()){
				int theAmount = rs.getInt("itemamount");//�������
				if(theAmount < groupam){//����δ��
					int id = rs.getInt("id");
					int offamount = groupam - theAmount;//�����Է��µ�����
					if(addamount <= offamount){//ʣ��Ҫ���ӵ���������ȫ���ŵ�����
						jsonarr.add(getJsonObj(id, itemtype, itemnum, theAmount+addamount, theAmount, zone, zone, extendarr));
						addamount = 0;
					} else {
						jsonarr.add(getJsonObj(id, itemtype, itemnum, groupam, theAmount, zone, zone, extendarr));
						addamount -= offamount;
					}
				}
			}
			if(addamount > 0){//����ʣ���
				needzone++;
				jsonarr.add(getJsonObj(0, itemtype, itemnum, addamount, 0, zone, zone, extendarr));
			}
		}
		jsonobj.put("needzone", needzone);
	}
	
	/**
	 * �Ƴ���Ʒ(��IDȫ��)
	 */
	public void removeByJson(int playerid, int itemid, byte zone, JSONObject jsonobj) throws Exception{
		removeByJson(playerid, itemid, 0, zone, jsonobj);
	}
	
	/**
	 * ����Ʒ(��ID)
	 * @param removeamount Ϊ0ʱ��ID�Ƴ�ȫ����Ʒ��������ʾ��������
	 */
	public void removeByJson(int playerid, int itemid, int removeamount, byte zone, JSONObject jsonobj) throws Exception{
		int needzone = jsonobj.optInt("needzone");
		JSONArray jsonarr = jsonobj.optJSONArray("jsonarr");
		DBPaRs rs = getDataRs(playerid, TYPE_ALL, itemid, zone);
		int itemtype = rs.getInt("itemtype");
		int itemnum = rs.getInt("itemnum");
		int itemamount = rs.getInt("itemamount");
		if(itemamount < removeamount){
			BACException.throwAndPrintInstance("�������������� PID="+playerid+" TYPE="+itemtype+" NUM="+itemnum+" AMOUNT="+removeamount+" ZONE="+zone);
		}
		if(removeamount == 0 || removeamount >= itemamount){
			needzone--;
			itemamount = 0;
		} else 
		if(removeamount > 0){
			itemamount -= removeamount;
		}
		jsonarr.add(getJsonObj(itemid, itemtype, itemnum, itemamount, rs.getInt("itemamount"), rs.getByte("zone"), rs.getByte("zone"), null));
		jsonobj.put("needzone", needzone);
	}
	
	/**
	 * ����Ʒ(������)
	 */
	public void removeByJson(int playerid, int itemtype, int itemnum, int removeamount, byte zone, JSONObject jsonobj) throws Exception{
		//DBPaRs typeRs = getTypeListRs(itemtype);
		/*if(typeRs.getInt("multi")==0){
			BACException.throwAndPrintInstance("���ɵ�����Ʒ���ṩ���������Ʒ");
		}*/
		DBPsRs rs = getDataRs(playerid, itemtype, itemnum, zone, "zone,itemamount");//�����ð���Ʒ
		int totalamount = (int)rs.sum("itemamount");
		if(totalamount < removeamount){
			BACException.throwAndPrintInstance("�������������� PID="+playerid+" TYPE="+itemtype+" NUM="+itemnum+" AMOUNT="+removeamount+" ZONE="+zone);
		}
		int needzone = jsonobj.optInt("needzone");
		JSONArray jsonarr = jsonobj.optJSONArray("jsonarr");
		while(rs.next()){
			int thisamount = rs.getInt("itemamount");
			if(thisamount <= removeamount){
				needzone--;
				jsonarr.add(getJsonObj(rs.getInt("id"), itemtype, itemnum, 0, thisamount, rs.getByte("zone"), rs.getByte("zone"), null));
				removeamount -= thisamount;
			} else {
				jsonarr.add(getJsonObj(rs.getInt("id"), itemtype, itemnum, thisamount-removeamount, thisamount, rs.getByte("zone"), rs.getByte("zone"), null));
				removeamount = 0;
			}
			if(removeamount <= 0){
				break;
			}
		}
		jsonobj.put("needzone", needzone);
	}
	
	/**
	 * ����JSON�������ݿ�
	 */
	public void changeSqlByJson(DBHelper dbHelper, int playerid, byte zone, byte shortcut, JSONObject jsonobj, int from, GameLog gl) throws Exception {
		int needzone = jsonobj.optInt("needzone");
		JSONArray jsonarr = jsonobj.optJSONArray("jsonarr");
		int remainzone = 0;
		if(needzone > 0){
			remainzone = getRemainZone(playerid, zone);
		}
		int addusezone = 0;//����������Ʒʹ�õĿռ���
		StringBuffer mail_adjunctSb = new StringBuffer(); 
		for(int i = 0; i < jsonarr.length(); i++){
			JSONObject obj = jsonarr.getJSONObject(i);
			int item_id = obj.getInt("id");
			int item_type = obj.getInt("type");
			int item_num = obj.getInt("num");
			int item_amount = obj.getInt("amount");//������
			int item_oldamount = obj.getInt("oldamount");//ԭ����
			byte item_oldzone = (byte)obj.getInt("oldzone");//ԭ���ڿռ�
			JSONArray extendarr = obj.optJSONArray("extend");//��չ
			DBPRs listRs = getListRs(item_type, item_num);
			String item_name = listRs.getString("name");
			if(item_id == 0){//������Ʒ
				if(needzone == 0 || remainzone == -1 || addusezone < remainzone){//ʹ�ÿռ���ʣ��ռ�֮�ڻ�ǿ�Ƽ���
					addusezone++;
					obj = addData(dbHelper, playerid, item_type, item_num, item_amount, item_oldamount, zone, item_oldzone, extendarr);
					item_id = obj.getInt("id");
					jsonarr.put(i, obj);
					JSONArray infoarr = addItemExtend(dbHelper, playerid, item_id, item_type, item_num, extendarr, from, gl);
					if(infoarr != null){//��Ʒ������Ϣ
						obj.put("info", infoarr);
					}
				} else {
					obj.put("add", shortcut);
					if(shortcut == SHORTCUT_DISCARD){
						item_name += "(�Ѷ���)";
					} else 
					if(shortcut == SHORTCUT_MAIL){
						if(mail_adjunctSb.length() > 0){
							mail_adjunctSb.append("|");
						}
						mail_adjunctSb.append(1);
						mail_adjunctSb.append(",");
						mail_adjunctSb.append(item_type);
						mail_adjunctSb.append(",");
						mail_adjunctSb.append(item_num);
						mail_adjunctSb.append(",");
						mail_adjunctSb.append(item_amount);
						for(int k = 0; extendarr != null && k < extendarr.length(); k++){
							mail_adjunctSb.append(",");
							mail_adjunctSb.append(extendarr.optInt(k));
						}
						item_name += "(�ʼ�����)";
					}
				}
			} else {//�������仯
				if(item_amount > 0){
					SqlString sqlStr = new SqlString();
					sqlStr.add("itemamount", item_amount);
					update(dbHelper, playerid, item_id, zone, sqlStr);
				} else {
					addusezone--;
					removeItemExtend(dbHelper, playerid, item_id, item_type, gl);
					delete(dbHelper, playerid, item_id, zone);
				}
			}
			obj.put("name", item_name);
			if(from != 0 && item_amount > item_oldamount){
				SqlString logSqlStr = new SqlString();
				logSqlStr.add("playerid", playerid);
				logSqlStr.add("serverid", Conf.sid);
				logSqlStr.add("itemtype", item_type);
				logSqlStr.add("itemnum", item_num);
				logSqlStr.add("itemamount", item_amount-item_oldamount);
				logSqlStr.add("itemfrom", from);
				logSqlStr.addDateTime("createtime", MyTools.getTimeStr());
				DBHelper.logInsert(LogTbName.TAB_ITEM_FORM_LOG(), logSqlStr);
			}
		}
		if(mail_adjunctSb.length() > 0){
			MailBAC.getInstance().sendSysMail(dbHelper, playerid, itemZoneName[zone]+"�ռ�����", itemZoneName[zone]+"�ռ�������ͨ���ʼ�����", mail_adjunctSb.toString(), 0);
		}
	}
	
	/**
	 * ������Ʒ��չ������
	 */
	private JSONArray addItemExtend(DBHelper dbHelper, int playerid, int itemid, int itemtype, int itemnum, JSONArray extendarr, int from, GameLog gl) throws Exception {
		JSONArray returnarr = null;
		if(itemtype == TYPE_EQUIP_ORDINARY){
			returnarr = EquipOrdinaryBAC.getInstance().create(dbHelper, playerid, itemid, itemnum, extendarr, from, gl);
		}
		return returnarr;
	}
	
	/**
	 * �Ƴ���Ʒ��չ������
	 */
	private void removeItemExtend(DBHelper dbHelper, int playerid, int itemid, int itemtype, GameLog gl) throws Exception {
		if(itemtype == TYPE_EQUIP_ORDINARY){
			EquipOrdinaryBAC.getInstance().destory(dbHelper, playerid, itemid, gl);
		}
	}
	
	/**
	 * ��ȡ��Ʒ����
	 */
	public JSONObject getItemData(int playerid, int itemid, byte zone) throws Exception {
		DBPaRs itemStorRs = getDataRs(playerid, TYPE_ALL, itemid, zone);
		int type = itemStorRs.getInt("itemtype");
		JSONObject itemobj = getJsonObj(itemid, type, itemStorRs.getInt("itemnum"), itemStorRs.getInt("itemamount"), itemStorRs.getInt("itemamount"), itemStorRs.getByte("zone"), itemStorRs.getByte("zone"), null);
		JSONArray infoarr = null;
		if(type == TYPE_EQUIP_ORDINARY){
			infoarr = EquipOrdinaryBAC.getInstance().getData(playerid, itemid);
		}
		if(infoarr != null){
			itemobj.put("info", infoarr);
		}
		return itemobj;
	}
	
	/**
	 * �ӱ�����¼
	 */
	private JSONObject addData(DBHelper dbHelper, int playerid, int itemtype, int itemnum, int itemamount, int olditemamount, byte zone, byte oldzone, JSONArray extendarr) throws Exception{
		int itemid = insert(dbHelper, playerid, itemtype, itemnum, itemamount, zone);
		return getJsonObj(itemid, itemtype, itemnum, itemamount, olditemamount, zone, oldzone, extendarr);
	}
	
	/**
	 * ���ݲ�����ȡJSON����
	 */
	private JSONObject getJsonObj(int id, int itemtype, int itemnum, int itemamount, int olditemamount, byte zone, byte oldzone, JSONArray extendarr){
		JSONObject obj = new JSONObject();
		obj.put("id", id);
		obj.put("type", itemtype);
		obj.put("num", itemnum);
		obj.put("amount", itemamount);
		obj.put("oldamount", olditemamount);
		obj.put("zone", zone);
		obj.put("oldzone", oldzone);
		obj.put("extend", extendarr);
		return obj;
	}
	
	/**
	 * ��ȡָ���ռ��ʣ�������
	 * @return ʣ������� ����-1��ʾ�޸�������
	 */
	public int getRemainZone(int playerid, byte zone) throws Exception{
		return -1;//TODO 
	}
	
	/**
	 * ͨ����ƷID��ȡ���
	 */
	public int getItemNumById(int playerid, int itemtype, int itemid) throws Exception{
		DBPaRs bagRs = ItemBAC.getInstance().getDataRs(playerid, itemtype, itemid, ItemBAC.ZONE_USE);
		int num = bagRs.getInt("itemnum");
		return num;
	}
	
	/**
	 * ��ѯ��Ʒ���ݼ�(����ID)
	 */
	public DBPaRs getDataRs(int playerid, int itemtype, int itemid, byte zone) throws Exception{
		StringBuffer whereSb = new StringBuffer();
		whereSb.append("playerid="+playerid+" and id="+itemid);
		if(itemtype != TYPE_ALL){
			whereSb.append(" and itemtype="+itemtype);
		}
		if(zone != ZONE_MYALL){
			whereSb.append(" and zone="+zone);
		} else {
			whereSb.append(" and zone!="+ZONE_SELL+" and zone!="+ZONE_MAIL);
		}
		DBPaRs rs = new DBPaRs(query(playerid, whereSb.toString()));
		if(!rs.exist()){
			BACException.throwAndPrintInstance("��Ʒδ�ҵ� itemid="+itemid);
		}
		return rs;
	}
	
	/**
	 * ��ѯ��Ʒ���ݼ�(������)
	 */
	public DBPsRs getDataRs(int playerid, int itemtype, int itemnum, byte zone, String order) throws Exception {
		StringBuffer whereSb = new StringBuffer();
		whereSb.append("playerid="+playerid+" and itemtype="+itemtype+" and itemnum="+itemnum);
		if(zone != ZONE_MYALL){
			whereSb.append(" and zone="+zone);
		} else {
			whereSb.append(" and zone!="+ZONE_SELL+" and zone!="+ZONE_MAIL);
		}
		return query(playerid, whereSb.toString(), order);
	}
	
	/**
	 * ������ƷID����
	 */
	private void update(DBHelper dbHelper, int playerid, int itemid, byte zone, SqlString sqlStr) throws Exception {
		StringBuffer whereSb = new StringBuffer();
		whereSb.append("playerid="+playerid+" and id="+itemid);
		if(zone != ZONE_MYALL){
			whereSb.append(" and zone="+zone);
		} else {
			whereSb.append(" and zone!="+ZONE_SELL+" and zone!="+ZONE_MAIL);
		}
		update(dbHelper, playerid, sqlStr, whereSb.toString());
	}
	
	/**
	 * ����
	 */
	private int insert(DBHelper dbHelper, int playerid, int itemtype, int itemnum, int itemamount, byte zone) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("itemtype", itemtype);
		sqlStr.add("itemnum", itemnum);
		sqlStr.add("itemamount", itemamount);
		sqlStr.add("zone", zone);
		int id = insert(dbHelper, playerid, sqlStr);
		return id;
	}
	
	/**
	 * �Ƴ��ռ��е�ָ����¼
	 */
	private void delete(DBHelper dbHelper, int playerid, int itemid, byte zone) throws Exception{
		StringBuffer whereSb = new StringBuffer();
		whereSb.append("playerid="+playerid+" and id="+itemid);
		if(zone != ZONE_MYALL){
			whereSb.append(" and zone="+zone);
		} else {
			whereSb.append(" and zone!="+ZONE_SELL+" and zone!="+ZONE_MAIL);
		}
		delete(dbHelper, playerid, whereSb.toString());
	}
	
	/**
	 * �����Ʒ�б����ݼ�
	 */
	public DBPaRs getListRs(int itemtype, int itemnum) throws Exception{
		String tab = getTab(itemtype);
		DBPaRs rs = DBPool.getInst().pQueryA(tab, "num="+itemnum);
		if(!rs.exist()){
			BACException.throwAndPrintInstance("�����ڵ���Ʒ��� itemtype="+itemtype+" itemnum="+itemnum);
		}
		return rs;
	}
	
	/**
	 * �������ͻ�ȡ��Ӧ�ı���
	 */
	public String getTab(int itemtype) throws Exception {
		DBPaRs typeRs = getTypeListRs(itemtype);
		return typeRs.getString("tabname");
	}
	
	/**
	 * ��ȡ��Ʒ�������ݼ�
	 */
	public DBPaRs getTypeListRs(int itemtype) throws Exception{
		DBPaRs rs = DBPool.getInst().pQueryA(tab_item_type, "itemtype="+itemtype);
		if(!rs.exist()){
			BACException.throwAndPrintInstance("�����ڵ���Ʒ���ͣ�" + itemtype);
		}
		return rs;
	}
	
	/**
	 * ������Ʒ���ͺ�ָ�������ռ��ȡ������
	 * @param itemtype	��Ʒ����
	 * @param zone	ָ���ռ�
	 */
	public int getAmountByItemtype(int playerid, int itemtype, short zone) throws Exception {
		StringBuffer whereSb = new StringBuffer();
		whereSb.append("playerid="+playerid+" and itemtype="+itemtype);
		if(zone != ZONE_MYALL){
			whereSb.append(" and zone="+zone);
		} else {
			whereSb.append(" and zone!="+ZONE_SELL+" and zone!="+ZONE_MAIL);
		}
		DBPsRs dbPsRs = query(playerid, whereSb.toString());
		return dbPsRs.count();
	}
	
	//--------------��̬��--------------
	
	private static ItemBAC instance = new ItemBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ItemBAC getInstance(){
		return instance;
	}
}
