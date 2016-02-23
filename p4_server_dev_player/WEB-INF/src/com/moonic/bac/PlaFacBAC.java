package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPRs;
import com.moonic.util.DBPaRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ��ɫ����
 * @author John
 */
public class PlaFacBAC extends PlaBAC {
	
	/**
	 * ����
	 */
	public PlaFacBAC() {
		super("tab_pla_faction", "playerid");
	}
	
	/**
	 * ��ʼ��Ŀ������
	 */
	public void init(DBHelper dbHelper, int playerid, Object... parm) throws Exception {
		SqlString sqlStr = getInitSqlStr();
		sqlStr.add("playerid", playerid);
		sqlStr.add("factioncon", 0);
		sqlStr.add("cbpartnerrelivedata", "{}");
		sqlStr.add("getwelfare", 0);
		sqlStr.add("worship1", 0);
		sqlStr.add("worship2", 0);
		insert(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ��ʼ����ɫ������Ϣ��SqlStr
	 */
	public SqlString getInitSqlStr(){
		SqlString sqlStr = new SqlString();
		sqlStr.add("factionid", 0);
		sqlStr.add("facname", null);
		sqlStr.add("position", 0);
		sqlStr.add("beworship", (new JSONObject()).toString());
		sqlStr.add("cmdata", (new JSONObject()).toString());//���ղſɽ�����һ�����ɣ��˳���������������Ӱ��
		return sqlStr;
	}
	
	/**
	 * �˳����ɺ���ղ������ٴν������
	 */
	public void intoCheck(DBPaRs plafacRs) throws Exception {
		if(System.currentTimeMillis()-MyTools.getCurrentDateLong(plafacRs.getTime("jointime"))<Conf.joinfacspacetime*MyTools.long_minu){
			BACException.throwInstance("�˳����ɺ���ղſ��ٴ�����");
		}		
	}
	
	/**
	 * �������
	 */
	public void intoFaction(DBHelper dbHelper, int playerid, int factionid, String facname, int position) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("factionid", factionid);
		sqlStr.add("facname", facname);
		sqlStr.add("position", position);
		sqlStr.addDateTime("jointime", MyTools.getTimeStr());
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ����ְλ
	 */
	public void setPosition(DBHelper dbHelper, int playerid, int position) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("position", position);
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * �˳�����
	 */
	public void exitFaction(DBHelper dbHelper, int factionid, int playerid, GameLog gl) throws Exception {
		SqlString sqlStr = getInitSqlStr();
		sqlStr.addDateTime("jointime", MyTools.getTimeStr());
		update(dbHelper, playerid, sqlStr);
	}
	
	/**
	 * ��ȡ��Ա��Ϣ
	 */
	public JSONArray getMemData(DBPRs plafacRs) throws Exception {
		return getMemData(plafacRs.getInt("playerid"), plafacRs.getInt("position"));
	}
	
	/**
	 * ��ȡ��Ա��Ϣ
	 */
	public JSONArray getMemData(int playerid, int position) throws Exception {
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		JSONArray arr = new JSONArray();
		arr.add(plaRs.getInt("id"));//���ID
		arr.add(plaRs.getString("name"));//�����
		arr.add(plaRs.getInt("lv"));//��ҵȼ�
		arr.add(plaRs.getInt("onlinestate"));//�Ƿ�����
		arr.add(plaRs.getInt("num"));//��ұ��
		arr.add(plaRs.getInt("vip"));//VIP�ȼ�
		arr.add(TqBAC.getInstance().getTQNum(plaRs));//��Ȩ
		arr.add(plaRs.getTime("logintime"));//��¼ʱ��
		arr.add(position);//ְλ
		return arr;
	}
	
	/**
	 * ��ȡ��¼��Ϣ
	 */
	public JSONArray getLoginData(int playerid) throws Exception {
		DBPaRs plafacRs = getDataRs(playerid);
		int factionid = plafacRs.getInt("factionid");
		JSONArray otherarr = new JSONArray();
		if(factionid != 0){
			otherarr.add(new JSONObject(plafacRs.getString("cmdata")));//���ɸ�������
		}
		JSONArray plafacarr = new JSONArray();
		plafacarr.add(factionid);//����ID
		plafacarr.add(plafacRs.getInt("factioncon"));//��ѫ
		plafacarr.add(new JSONObject(plafacRs.getString("cbpartnerrelivedata")));//��ս��鸴�����
		plafacarr.add(plafacRs.getInt("getwelfare"));//�Ƿ�����ȡ����
		plafacarr.add(plafacRs.getInt("worship1"));//�Ƿ���Ĥ��
		plafacarr.add(plafacRs.getInt("worship2"));//�Ƿ���Ĥ��
		plafacarr.add(new JSONObject(plafacRs.getString("beworship")));//��Ĥ������
		plafacarr.add(plafacRs.getTime("jointime"));//���һ�μ�����ɻ��˳�����ʱ��
		plafacarr.add(plafacRs.getInt("factioncon"));//��ѫ
		plafacarr.add(plafacRs.getTime("leadercdendtime"));//̫��������ȴʱ��
		plafacarr.add(otherarr);//��������
		return plafacarr;
	}
	
	/**
	 * ����ÿ������
	 */
	public void resetData(DBHelper dbHelper, int playerid, long resetdate) throws Exception {
		DBPaRs plafacRs = getDataRs(playerid);
		int factionid = plafacRs.getInt("factionid");
		SqlString sqlStr = new SqlString();
		if(factionid != 0) {
			sqlStr.add("cmdata", (new JSONObject()).toString());//���ɸ�������
		}
		sqlStr.add("cbpartnerrelivedata", "{}");
		sqlStr.add("getwelfare", 0);//�Ƿ�����ȡ����
		sqlStr.add("worship1", 0);//�Ƿ���Ĥ��
		sqlStr.add("worship2", 0);//�Ƿ���Ĥ��
		update(dbHelper, playerid, sqlStr);
	}
	
	//--------------��̬��--------------
	
	private static PlaFacBAC instance = new PlaFacBAC();

	/**
	 * ��ȡʵ��
	 */
	public static PlaFacBAC getInstance() {
		return instance;
	}
}
