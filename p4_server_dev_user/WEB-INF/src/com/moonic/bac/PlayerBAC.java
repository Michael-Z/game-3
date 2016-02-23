package com.moonic.bac;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.Pool;

/**
 * ��ɫBAC
 * @author John
 */
public class PlayerBAC {
	public static final String tab_player = "tab_player";
	
	private static final String popkey = "popkey";
	
	/**
	 * ��ȡ����������������
	 */
	public JSONObject getPop() {
		JSONObject popjson = null;
		synchronized (popkey) {
			popjson = (JSONObject)Pool.getObjectFromPoolById(popkey);
			if(popjson == null) {
				popjson = new JSONObject();
				DBHelper dbHelper = new DBHelper();
				try {
					dbHelper.openConnection();
					ResultSet listDataRs = dbHelper.query(tab_player, "serverid,count(id) as amount", "onlinestate=1 group by serverid");
					while(listDataRs.next()){
						popjson.put(listDataRs.getString("serverid"), listDataRs.getInt("amount"));
					}
					dbHelper.closeRs(listDataRs);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dbHelper.closeConnection();
				}
				Pool.addObjectToPool(popkey, 300, popjson);
			}
		}
		return popjson;
	}
	
	/**
	 * ��ȡָ���û���¼���Ľ�ɫ�ķ���������
	 */
	public JSONArray getUsedServer(DBHelper dbHelper, int userid) throws Exception {
		JSONArray jsonarr = new JSONArray();
		ResultSet logRs = dbHelper.query(tab_player, "vsid", "userid="+userid, "logintime desc");
		while(logRs.next()){
			jsonarr.add(logRs.getInt("vsid"));
		}
		return jsonarr;
	}
	
	/**
	 * WEB��ȡ��ɫ��Ϣ
	 */
	public ReturnValue webGetPlayerInfo(String username, String channel, int vsid) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			DBPaRs channelRs = ChannelBAC.getInstance().getChannelListRs(channel);
			if(!channelRs.exist()){
				BACException.throwInstance("�������������� channel="+channel);
			}
			ResultSet userRs = dbHelper.query(UserBAC.tab_user, "id", "username='"+username+"' and platform='"+channelRs.getString("platform")+"'");
			if(!userRs.next()){
				BACException.throwInstance("�û�δ�ҵ�");
			}
			int userid = userRs.getInt("id");
			ResultSet dataRs = dbHelper.query(tab_player, "id,name,lv", "userid="+userid+" and vsid="+vsid);
			if(!dataRs.next()){
				BACException.throwInstance("��δ������ɫ UID="+userid+" VSID="+vsid);
			}
			JSONObject jsonobj = new JSONObject();
			jsonobj.put("pid", dataRs.getInt("id"));
			jsonobj.put("pname", dataRs.getString("name"));
			jsonobj.put("lv", dataRs.getInt("lv"));
			return new ReturnValue(true, jsonobj.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	private static PlayerBAC instance = new PlayerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static PlayerBAC getInstance(){
		return instance;
	}	
}
