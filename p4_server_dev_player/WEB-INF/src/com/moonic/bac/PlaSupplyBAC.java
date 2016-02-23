package com.moonic.bac;

import org.json.JSONArray;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ��ɫ����
 * @author John
 */
public class PlaSupplyBAC extends PlaBAC {
	public static final String tab_buy_money = "tab_buy_money";
	public static final String tab_buy_money_add = "tab_buy_money_add";
	public static final String tab_buy_energy = "tab_buy_energy";
	
	/**
	 * ����
	 */
	public PlaSupplyBAC() {
		super("tab_pla_supply", "playerid");
	}
	
	/**
	 * ��ʼ��
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("playerid", playerid);
		sqlStr.add("buymoneyam", 0);
		sqlStr.add("buyenergyam", 0);
		sqlStr.add("gettqaward", 0);
		insert(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ͭǮ
	 */
	public ReturnValue buyMoney(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plasupplyRs = getDataRs(playerid);
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			int maxamount = VipBAC.getInstance().getVipFuncData(plaRs.getInt("vip"), 1);
			int curramount = plasupplyRs.getInt("buymoneyam");
			if(curramount >= maxamount){
				BACException.throwInstance("�������������");
			}
			DBPaRs buymoneyRs = DBPool.getInst().pQueryA(tab_buy_money, "minam<="+(curramount+1)+" and maxam>="+(curramount+1));
			if(!buymoneyRs.exist()){
				BACException.throwInstance("�������������");
			}
			long oldMoney = plaRs.getLong("money");
			DBPaRs buymoneyaddRs = DBPool.getInst().pQueryA(tab_buy_money_add, "lv="+plaRs.getInt("lv"));
			if(!buymoneyaddRs.exist()){
				BACException.throwInstance("�ȼ��쳣");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_SUPPLY_BUY_MONEY);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, buymoneyRs.getInt("needcoin"), gl);
			int addMoney = buymoneyaddRs.getInt("amount");
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("buymoneyam", 1);
			update(dbHelper, playerid, sqlStr);
			SqlString plaSqlStr = new SqlString();
			plaSqlStr.addChange("money", addMoney);
			PlayerBAC.getInstance().update(dbHelper, playerid, plaSqlStr);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_BUY_MONEY, gl);
			
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 8);
			
			gl.addChaNote("�������", curramount, 1)
			.addChaNote(GameLog.TYPE_MONEY, oldMoney, addMoney)
			.addRemark("VIP�ȼ���"+plaRs.getInt("vip"))
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
	 * ������
	 */
	public ReturnValue buyEnergy(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plasupplyRs = getDataRs(playerid);
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			int maxamount = VipBAC.getInstance().getVipFuncData(plaRs.getInt("vip"), 2);
			int curramount = plasupplyRs.getInt("buyenergyam");
			if(curramount >= maxamount){
				BACException.throwInstance("�������������");
			}
			DBPaRs buyenergyRs = DBPool.getInst().pQueryA(tab_buy_energy, "amount="+(curramount+1));
			if(!buyenergyRs.exist()){
				BACException.throwInstance("�������������");
			}
			DBPaRs plaRoleRs = PlaRoleBAC.getInstance().getDataRs(playerid);
			int oldEnergy = plaRoleRs.getInt("energy");
			int addEnergy = buyenergyRs.getInt("addamount");
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_SUPPLY_BUY_ENERGY);
			PlayerBAC.getInstance().useCoin(dbHelper, playerid, buyenergyRs.getInt("needcoin"), gl);
			
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("buyenergyam", 1);
			update(dbHelper, playerid, sqlStr);
			SqlString roleSqlStr = new SqlString();
			roleSqlStr.addChange("energy", addEnergy);
			PlaRoleBAC.getInstance().update(dbHelper, playerid, roleSqlStr);
			
			PlaWelfareBAC.getInstance().updateTaskProgress(dbHelper, playerid, PlaWelfareBAC.TYPE_BUY_ENERGY, gl);
			
			CustomActivityBAC.getInstance().updateProcess(dbHelper, playerid, 7);
			
			gl.addChaNote("�������", curramount, 1)
			.addChaNote("����", oldEnergy, addEnergy)
			.addRemark("VIP�ȼ���"+plaRs.getInt("vip"))
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
	 * ��ȡÿ����Ȩ��
	 */
	public ReturnValue getTqCoin(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs plasuppRs = getDataRs(playerid);
			if(plasuppRs.getInt("gettqaward") == 1){
				BACException.throwInstance("������Ȩ��������ȡ");
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			int addcoin = TqBAC.getInstance().getTQFuncData(plaRs, 1);
			if(addcoin == 0){
				BACException.throwInstance("���ȹ����¿�");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_SUPPLY_GET_TQCOIN);
			setValue(dbHelper, playerid, "gettqaward", 1, gl, "�Ƿ�����ȡ�¿���");
			PlayerBAC.getInstance().addValue(dbHelper, playerid, "coin", addcoin, gl, GameLog.TYPE_COIN);
			
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
	public void resetData(DBHelper dbHelper, int playerid) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("buymoneyam", 0);
		sqlStr.add("buyenergyam", 0);
		sqlStr.add("gettqaward", 0);
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ��¼����
	 */
	public JSONArray getLoginData(int playerid) throws Exception {
		JSONArray supparr = new JSONArray();
		DBPaRs plasupplyRs = getDataRs(playerid);
		supparr.add(plasupplyRs.getInt("buymoneyam"));//����ͭǮ����
		supparr.add(plasupplyRs.getInt("buyenergyam"));//�ѹ�����������
		supparr.add(plasupplyRs.getInt("gettqaward"));//�Ƿ�����ȡ��Ȩ����
		return supparr;
	}
	
	//------------------��̬��--------------------
	
	private static PlaSupplyBAC instance = new PlaSupplyBAC();
	
	public static PlaSupplyBAC getInstance() {
		return instance;
	}
}
