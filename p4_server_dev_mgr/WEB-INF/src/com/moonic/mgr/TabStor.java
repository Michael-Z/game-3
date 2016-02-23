package com.moonic.mgr;

import java.sql.ResultSet;

import server.config.ServerConfig;

import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ���ݱ��
 * @author John
 */
public class TabStor {
	//---------------���ݱ�---------------
	/**
	 * ��ֵ��ʽ
	 */
	public static final String tab_charge_type = "tab_charge_type";
	/**
	 * ��Ϸϵͳ
	 */
	public static final String tab_game_sys = "tab_game_sys";
	/**
	 * ��Ϸ����
	 */
	public static final String tab_game_func = "tab_game_func";
	/**
	 * �ʺ�����
	 */
	public static final String tab_platform = "tab_platform";
	/**
	 * ��������
	 */
	public static final String tab_channel = "tab_channel";
	/**
	 * �ٸ���
	 */
	public static final String tab_fakeodds_item = "tab_fakeodds_item";
	/**
	 * ƽ̨���
	 */
	public static final String tab_platform_gift = "tab_platform_gift";
	/**
	 * ��ɫ
	 */
	public static final String tab_role = "tab_role";
	/**
	 * �����ӵ�
	 */
	public static final String tab_role_base_prop = "tab_role_base_prop";
	/**
	 * �ӵ���������
	 */
	public static final String tab_base_type = "tab_base_type";
	/**
	 * װ����λ
	 */
	public static final String tab_eqpos_type = "tab_eqpos_type";
	/**
	 * ���һ���Ʒ
	 */
	public static final String tab_cbt_exchange = "tab_cbt_exchange";
	/**
	 * ����
	 */
	public static final String tab_pet = "tab_pet";
	/**
	 * ��Ȩ
	 */
	public static final String tab_prerogative = "tab_prerogative";
	/**
	 * ��Ҵ�������
	 */
	public static final String tab_player_change_type = "tab_player_change_type";
	
	//---------------Ӧ�ñ�---------------
	/**
	 * �����
	 */
	public static final String tab_faction_stor = "tab_faction_stor";
	
	/**
	 * ����ID��ȡӦ�ñ�������������
	 */
	public static String getDataName(String table, int id) {
		if(id != 0){
			return getDataVal(table, "id="+id, "name");		
		} else {
			return "";
		}
	}
	
	/**
	 * ��ȡ���ݱ�����ֵ
	 */
	public static String getListVal(String table, String where, String column){
		String value = null;
		try {
			DBPaRs rs = DBPool.getInst().pQueryA(table, where);
			if(rs.exist()){
				value = rs.getString(column);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 * ��ȡӦ�ñ�����ֵ
	 */
	public static String getDataVal(String table, String where, String column) {
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Backup());
		try {
			dbHelper.openConnection();
			ResultSet facRs = dbHelper.query(table, column, where);
			if(!facRs.next()){
				BACException.throwInstance("δ�ҵ���¼");
			}
			return facRs.getString(column);
		} catch(Exception ex) {
			ex.printStackTrace();
			return "error:"+ex.toString();
		} finally {
			dbHelper.closeConnection();
		}
	}
}
