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
 * ��ɫ����̵�
 * @author wkc
 */
public class PlaSpShopBAC {
	public static final String tab_sp_shop = "tab_sp_shop";
	public static final String tab_sp_shop_amount = "tab_sp_shop_amount";
	public static final String tab_sp_shop_time = "tab_sp_shop_time";
	public static final String tab_sp_shop_refresh = "tab_sp_shop_refresh";
	
	
	/**
	 * ��ʼ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		JSONArray dataarr = refreshItem(playerid);
		SqlString sqlStr = new SqlString();
		sqlStr.add("item5", dataarr.optJSONArray(0).toString());
		sqlStr.add("buy5", dataarr.optJSONArray(1).toString());
		sqlStr.addDateTime("refreshtime5", MyTools.getTimeStr(getNextRefreshTime()));
		sqlStr.add("times5", 0);
		PlaShopBAC.getInstance().update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ����̵�����
	 */
	public ReturnValue getShopData(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plaSpRs = PlaShopBAC.getInstance().getDataRs(playerid);
			if(!plaSpRs.exist()){
				BACException.throwInstance("����̵���δ����");
			}
			dbHelper.openConnection();
			JSONArray returnarr = new JSONArray();
			returnarr.add(plaSpRs.getInt("times5"));
			if(MyTools.checkSysTimeBeyondSqlDate(plaSpRs.getTime("refreshtime5"))){
				GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_SP_SHOP_GETDATA);
				JSONArray refresharr = refresh(dbHelper, playerid, true, gl);
				MyTools.combJsonarr(returnarr, refresharr);
				gl.save();
			} else {
				returnarr.add(new JSONArray(plaSpRs.getString("item5")));
				returnarr.add(new JSONArray(plaSpRs.getString("buy5")));
				returnarr.add(plaSpRs.getTime("refreshtime5"));
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
			DBPaRs plaSpRs = PlaShopBAC.getInstance().getDataRs(playerid);
			JSONArray buyarr = new JSONArray(plaSpRs.getString("buy5"));
			if(buyarr.size() == 0){
				BACException.throwInstance("����̵���δ����");
			}
			
			if(index < 0 || index > buyarr.length()-1){
				BACException.throwInstance("index��������");
			}
			if(buyarr.optInt(index) == 1){
				BACException.throwInstance("����Ʒ�ѹ���");
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_SP_SHOP_BUY);
			JSONArray itemarr = (new JSONArray(plaSpRs.getString("item"))).optJSONArray(index);
			int need = Tools.splitStrToIntArr(itemarr.optString(1), ",")[1];
			PlaRoleBAC.getInstance().subValue(dbHelper, playerid, "soulpoint", need, gl, "���");
			JSONArray awardarr = AwardBAC.getInstance().getAward(dbHelper, playerid, itemarr.optString(0), ItemBAC.SHORTCUT_MAIL, 39, gl);
			buyarr.put(index, 1);
			SqlString sqlStr = new SqlString();
			sqlStr.add("buy5", buyarr.toString());
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
			DBPaRs plaSpRs = PlaShopBAC.getInstance().getDataRs(playerid);
			if(!plaSpRs.exist()){
				BACException.throwInstance("����̵���δ����");
			}
			int times = plaSpRs.getInt("times5");
			int need = 0;
			DBPsRs conListRs  = DBPool.getInst().pQueryS(tab_sp_shop_refresh);
			while(conListRs.next()){
				if(times+1 <= conListRs.getInt("end") || conListRs.getInt("end") == -1){
					need = conListRs.getInt("need");
					break;
				}
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_SP_SHOP_REFRESH);
			if(need > 0){
				PlayerBAC.getInstance().useCoin(dbHelper, playerid, need, gl);
			}
			JSONArray returnarr = refresh(dbHelper, playerid, false, gl);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("times5", 1);
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
		sqlStr.add("item5", itemarr.toString());
		sqlStr.add("buy5", buyarr.toString());
		if(refreshTime){
			sqlStr.addDateTime("refreshtime5", MyTools.getTimeStr(nexttime));
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
		DBPsRs amListRs = DBPool.getInst().pQueryS(tab_sp_shop_amount);
		amListRs.next();
		for(int i = 1; i <= 2; i++){
			int amount = amListRs.getInt("amount"+i);
			DBPsRs shopListRs  = DBPool.getInst().pQueryS(tab_sp_shop, "itemtype="+i);
			int[] oddsarr = new int[shopListRs.count()];
			while(shopListRs.next()){
				oddsarr[shopListRs.getRow()-1] = shopListRs.getInt("odds"); 
			}
			for(int j = 0; j < amount; j++){
				int index = MyTools.getIndexOfRandom(oddsarr);
				shopListRs.setRow(index+1);
				int[] split = Tools.splitStrToIntArr(shopListRs.getString("item"), ",");
				String item = Tools.combineInt((int[])ItemBAC.getInstance().enterItem(split, "lotteryodds")[0], ",");
				JSONArray jsonarr = new JSONArray();
				jsonarr.add(item);//��Ʒ��Ϣ
				jsonarr.add(shopListRs.getString("price"));//�۸�
				itemarr.add(jsonarr);
				buyarr.add(0);
				oddsarr[index] = 0;
			}
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
		return ShopData.getNextRefreshTime(ShopData.time_sp);
	}
	
	
	//--------��̬��--------
	
	private static PlaSpShopBAC instance = new PlaSpShopBAC();
	
	public static PlaSpShopBAC getInstance(){
		return instance;
	}
	
}
