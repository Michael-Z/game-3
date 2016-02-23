package com.moonic.bac;

import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.jspsmart.upload.Request;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;

/**
 * ����
 * @author John
 */
public class FunctionBAC {
	public static final String tab_function = "tab_function";
	public static final String tab_func_check = "tab_func_check";
	
	/**
	 * �����������������Ĺ���
	 */
	public ReturnValue debugOpenAllFunc(int playerid, JSONArray openfunc, int[] mustOpenArr){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			DBPsRs funcRs = DBPool.getInst().pQueryS(tab_function);
			StringBuffer sb = new StringBuffer("���������");
			while(funcRs.next()){
				ReturnValue rv = openFunc(playerid, openfunc, funcRs.getString("num"), mustOpenArr);
				sb.append("\r\n���ܣ�"+GameLog.formatNameID(funcRs.getString("name"), funcRs.getInt("num"))+" �����"+rv.success+" ��Ϣ��"+rv.info);
			}
			return new ReturnValue(true, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	
	/**
	 * ��������
	 * @param num ���ܱ��
	 */
	public ReturnValue openFunc(int playerid, JSONArray openfunc, String numStr, int[] mustOpenArr){
		DBHelper dbHelper = new DBHelper();
		try {
			JSONObject returnobj = new JSONObject();
			int[] nums = Tools.splitStrToIntArr(numStr, ",");
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_OPEN_FUNC);
			boolean havechange = false;
			for(int i = 0; i < nums.length; i++){
				boolean opened = checkFuncOpenByFuncnum(openfunc, nums[i]);
				if(opened){
					continue;
				}
				DBPaRs funcRs = DBPool.getInst().pQueryA(tab_function, "num="+nums[i]);
				if(!funcRs.exist()){
					continue;
				}
				String condStr = funcRs.getString("cond");
				String nameStr = funcRs.getString("name");
				if(condStr.equals("0")){
					continue;
				}
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				int plv = plaRs.getInt("lv");
				int[][] cond = Tools.splitStrToIntArr2(condStr, "|", ",");
				boolean match = true;
				if(!Tools.intArrContain(mustOpenArr, nums[i])){
					for(int j = 0; j < cond.length; j++){
						//TODO ������������
						if(cond[j][0] == 1){//�Ƿ�ﵽָ���ȼ�
							if(plv < cond[j][1]){
								match = false;
								break;
							}
						} else 
						if(cond[j][0] == 2){//����ָ������
							if(!openfunc.contains(cond[j][1])){
								match = false;
								break;
							}
						} else 
						if(cond[j][0] == 3){//ͨ��ָ������
							if(!CopymapBAC.getInstance().checkPass(playerid, cond[j][1])){
								match = false;
								break;
							}
						}
					}	
				}
				if(!match){
					continue;
				}
				openfunc.add(nums[i]);//��¼�¿����Ĺ��ܵ�JSON����
				//TODO ����������չ
				if(nums[i] == 1002){
					PlaJJCRankingBAC.getInstance().init(dbHelper, playerid, returnobj);
				} else
				if(nums[i] == 1003){
					PlaJJShopBAC.getInstance().init(dbHelper, playerid);
				} else
				if(nums[i] == 902){
					PlaFactionShopBAC.getInstance().init(dbHelper, playerid);
				} else
				if(nums[i] == 1301){
					PlaOrdinaryShopBAC.getInstance().init(dbHelper, playerid);
				} else
				if(nums[i] == 1302){
					PlaMysteryShopBAC.getInstance().init(dbHelper, playerid);
				} else
				if(nums[i] == 1303){
					PlaSpShopBAC.getInstance().init(dbHelper, playerid);
				} else
				if(nums[i] == 802){
					PlaTowerShopBAC.getInstance().init(dbHelper, playerid);
				} else
				if(nums[i] == 801){
					PlaTowerBAC.getInstance().init(dbHelper, playerid, returnobj);
				} else
				if(nums[i] == 2302){
					PlaTeamBAC.getInstance().init(dbHelper, playerid);
				} else 
				if(nums[i] == 2401){
					PlaMineralsBAC.getInstance().init(dbHelper, playerid);
				}
				gl.addRemark("������" + GameLog.formatNameID(nameStr, nums[i]));
				havechange = true;
			}
			if(havechange){
				SqlString sqlStr = new SqlString();
				sqlStr.add("openfunc", openfunc.toString());
				PlayerBAC.getInstance().update(dbHelper, playerid, sqlStr);
				
				gl.save();
			}
			return new ReturnValue(true, returnobj.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �رչ���
	 */
	public ReturnValue debugCloseFunc(int playerid, JSONArray openfunc, int num){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			for(int i = 0; i < openfunc.length(); i++){
				if(openfunc.getInt(i) == num){
					openfunc.remove(i);
					break;
				}
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("openfunc", openfunc.toString());
			PlayerBAC.getInstance().update(dbHelper, playerid, sqlStr);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��鹦���Ƿ��ѿ���
	 */
	public ReturnValue checkFuncOpen(JSONArray openfunc, short act){
		try {
			DBPaRs fcRs = DBPool.getInst().pQueryA(tab_func_check, "actnum="+act);
			if(fcRs.exist()){
				boolean opened = checkFuncOpenByFuncnum(openfunc, fcRs.getInt("funcnum"));
				if(!opened){
					BACException.throwInstance(fcRs.getString("funcname")+"������δ����");
				}
			}
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��鹦���Ƿ��ѿ���
	 */
	public ReturnValue checkFuncOpen(JSONArray openfunc, Request request){
		try {
			@SuppressWarnings("unchecked")
			Enumeration<String> names = request.getParameterNames();
			while(names.hasMoreElements()){
				String webkey = names.nextElement();
				DBPaRs fcRs = DBPool.getInst().pQueryA(tab_func_check, "webkey='"+webkey+"'");
				if(fcRs.exist()){
					boolean opened = checkFuncOpenByFuncnum(openfunc, fcRs.getInt("funcnum"));
					if(!opened){
						BACException.throwInstance(fcRs.getString("funcname")+"������δ����");
					}
				}
			}
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��鹦���Ƿ��ѿ���
	 */
	public boolean checkFuncOpenByFuncnum(JSONArray openfunc, int funcnum) throws Exception {
		DBPaRs funcRs = DBPool.getInst().pQueryA(tab_function, "num="+funcnum);
		if(funcRs.getString("cond").equals("0")){
			return true;//�޿��������Ĺ���ֱ��ͨ��
		}
		boolean exist = false;
		for(int i = 0; openfunc!=null && i<openfunc.length(); i++){
			if(openfunc.optInt(i)==funcnum){
				exist = true;
				break;
			}
		}
		return exist;
	}
	
	//--------------��̬��--------------
	
	private static FunctionBAC instance = new FunctionBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FunctionBAC getInstance(){
		return instance;
	}
}
