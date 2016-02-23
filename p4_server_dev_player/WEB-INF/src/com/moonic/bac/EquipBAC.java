package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import com.moonic.gamelog.GameLog;
import com.moonic.util.DBHelper;

/**
 * װ��������
 * @author John
 */
public abstract class EquipBAC extends PlaStorBAC {
	
	/**
	 * ���췽��
	 * @param table	����
	 * @param mkey	�ؼ��֣�playerid
	 */
	public EquipBAC(String table, String mkey, String id_col) {
		super(table, mkey, id_col);
	}
	
	//-------------------������---------------------
	
	/**
	 * ��ȡ��½��Ʒ��Ϣ
	 */
	public abstract void getLoginItemInfo(int playerid, JSONObject infoobj) throws Exception;
	
	/**
	 * ����װ��
	 */
	public abstract JSONArray create(DBHelper dbHelper, int playerid, int itemid, int num, JSONArray extendarr, int from, GameLog gl) throws Exception;
	
	/**
	 * ��ȡװ������
	 */
	public abstract JSONArray getData(int playerid, int itemid) throws Exception;
	
	/**
	 * ����װ��
	 */
	public abstract void destory(DBHelper dbHelper, int playerid, int itemid, GameLog gl) throws Exception;
}
