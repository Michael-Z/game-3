package com.moonic.bac;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.txtdata.ShopData;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

/**
 * ��ɫ�����̵�
 * @author wkc
 */
public class PlaFactionShopBAC {
	public static final String tab_faction_shop = "tab_faction_shop";
	public static final String tab_faction_shop_stone = "tab_faction_shop_stone";
	public static final String tab_faction_shop_time = "tab_faction_shop_time";
	public static final String tab_faction_shop_refresh = "tab_faction_shop_refresh";
	
	
	/**
	 * ��ʼ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		JSONArray dataarr = refreshItem(playerid);
		SqlString sqlStr = new SqlString();
		sqlStr.add("item4", dataarr.optJSONArray(0).toString());
		sqlStr.add("buy4", dataarr.optJSONArray(1).toString());
		sqlStr.addDateTime("refreshtime4", MyTools.getTimeStr(getNextRefreshTime()));
		sqlStr.add("times4", 0);
		PlaShopBAC.getInstance().update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ�����̵�����
	 */
	public ReturnValue getShopData(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaFacRs = PlaShopBAC.getInstance().getDataRs(playerid);
			if(!plaFacRs.exist()){
				BACException.throwInstance("�����̵���δ����");
			}
			dbHelper.openConnection();
			JSONArray returnarr = new JSONArray();
			returnarr.add(plaFacRs.getInt("times4"));
			if(MyTools.checkSysTimeBeyondSqlDate(plaFacRs.getTime("refreshtime4"))){
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_SHOP_GETDATA);
				JSONArray refresharr = refresh(dbHelper, playerid, true, gl);
				MyTools.combJsonarr(returnarr, refresharr);
				gl.save();
			} else {
				returnarr.add(new JSONArray(plaFacRs.getString("item4")));
				returnarr.add(new JSONArray(plaFacRs.getString("buy4")));
				returnarr.add(plaFacRs.getTime("refreshtime4"));
			}
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ������Ʒ
	 */
	public ReturnValue buy(int playerid, int index){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaFacRs = PlaShopBAC.getInstance().getDataRs(playerid);
			JSONArray buyarr = new JSONArray(plaFacRs.getString("buy4"));
			if(buyarr.size() == 0){
				BACException.throwInstance("�����̵���δ����");
			}
			
			if(index < 0 || index > buyarr.length()-1){
				BACException.throwInstance("index��������");
			}
			if(buyarr.optInt(index) == 1){
				BACException.throwInstance("����Ʒ�ѹ���");
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_SHOP_BUY);
			JSONArray itemarr = (new JSONArray(plaFacRs.getString("item4"))).optJSONArray(index);
			int need = Tools.splitStrToIntArr(itemarr.optString(1), ",")[1];
			PlaFacBAC.getInstance().subValue(dbHelper, playerid, "factioncon", need, gl, "��ѫ");
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, itemarr.optString(0), ItemBAC.SHORTCUT_MAIL, 39, gl);
			buyarr.put(index, 1);
			SqlString sqlStr = new SqlString();
			sqlStr.add("buy4", buyarr.toString());
			PlaShopBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
			gl.save();
			return new ReturnValue(true, awardarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally{
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����ˢ���̵�
	 */
	public ReturnValue refreshShop(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaFacRs = PlaShopBAC.getInstance().getDataRs(playerid);
			if(!plaFacRs.exist()){
				BACException.throwInstance("�����̵���δ����");
			}
			int times = plaFacRs.getInt("times4");
			int need = 0;
			DBPsRs conListRs  = DBPool.getInst().pQueryS(tab_faction_shop_refresh);
			while(conListRs.next()){
				if(times+1 <= conListRs.getInt("end") || conListRs.getInt("end") == -1){
					need = conListRs.getInt("need");
					break;
				}
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_FACTION_SHOP_REFRESH);
			if(need > 0){
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, need, gl);
			}
			JSONArray returnarr = refresh(dbHelper, playerid, false, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("times4", 1);
			PlaShopBAC.getInstance().update(dbHelper, playerid, sqlStr);
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
	 * ˢ��
	 */
	public JSONArray refresh(DBHelper dbHelper, int playerid, boolean refreshTime, GameLog gl) throws Exception {
		JSONArray refresharr = refreshItem(playerid);
		JSONArray itemarr = refresharr.optJSONArray(0);
		JSONArray buyarr = refresharr.optJSONArray(1);
		JSONArray returnarr = new JSONArray();
		returnarr.add(itemarr);
		returnarr.add(buyarr);
		long nexttime = getNextRefreshTime();
		SqlString sqlStr = new SqlString();
		sqlStr.add("item4", itemarr.toString());
		sqlStr.add("buy4", buyarr.toString());
		if(refreshTime){
			sqlStr.addDateTime("refreshtime4", MyTools.getTimeStr(nexttime));
			returnarr.add(nexttime);
			gl.addRemark("�´�ˢ��ʱ�䣺"+MyTools.getTimeStr(nexttime));
		}
		PlaShopBAC.getInstance().update(dbHelper, playerid, sqlStr);
		gl.addRemark("ˢ�µ���Ʒ��"+itemarr);
		return returnarr;
	}
	
	/**
	 * ˢ����Ʒ
	 */
	public JSONArray refreshItem(int playerid) throws Exception{
		JSONArray itemarr = new JSONArray();//��Ʒ����
		JSONArray buyarr = new JSONArray();//��������
		//��ʯ��
		DBPsRs stoneListRs  = DBPool.getInst().pQueryS(tab_faction_shop_stone);
		while(stoneListRs.next()){
			int[] split = Tools.splitStrToIntArr(stoneListRs.getString("stone"), ",");
			String item = Tools.combineInt((int[])ItemBAC.getInstance().enterItem(split, "lotteryodds")[0], ",");
			JSONArray jsonarr = new JSONArray();
			jsonarr.add(item);//��Ʒ��Ϣ
			jsonarr.add(stoneListRs.getString("price"));//�۸���Ϣ
			itemarr.add(jsonarr);
			buyarr.add(0);
		}
		//��
		int plv = PlayerBAC.getInstance().getIntValue(playerid, "lv");
		DBPaRs shopListRs = DBPool.getInst().pQueryA(tab_faction_shop, "begin<="+plv+" and end>="+plv);
		String[] item = Tools.splitStr(shopListRs.getString("store"), "|");
		String[] price = Tools.splitStr(shopListRs.getString("price"), "|");
		int[] odds = Tools.splitStrToIntArr(shopListRs.getString("odds"), ",");
		int amount = shopListRs.getInt("amount");
		for(int j = 0; j < amount; j++){
			int index = MyTools.getIndexOfRandom(odds);
			int[] split = Tools.splitStrToIntArr(item[index], ",");
			Object[] enter = ItemBAC.getInstance().enterItem(split, "facodds");
			String itemStr = Tools.combineInt((int[])enter[0], ",");
			String priceStr = price[index];
			if(priceStr.equals("0")){
				StringBuffer priceSb = new StringBuffer();
				priceSb.append("8,");
				priceSb.append(((DBPsRs)enter[1]).getInt("facprice"));
				priceStr = priceSb.toString();
			}
			JSONArray jsonarr = new JSONArray();
			jsonarr.add(itemStr);//��Ʒ��Ϣ
			jsonarr.add(priceStr);//�۸���Ϣ
			itemarr.add(jsonarr);
			buyarr.add(0);
			odds[index] = 0;
		}
		JSONArray returnarr = new JSONArray();
		returnarr.add(itemarr);
		returnarr.add(buyarr);
		return returnarr;
	}
	
	/**
	 * ��ȡ�´�ˢ��ʱ��
	 */
	public long getNextRefreshTime() throws Exception{
		return ShopData.getNextRefreshTime(ShopData.time_faction);
	}
	
	//--------��̬��--------
	
	private static PlaFactionShopBAC instance = new PlaFactionShopBAC();
	
	public static PlaFactionShopBAC getInstance(){
		return instance;
	}
	
}
