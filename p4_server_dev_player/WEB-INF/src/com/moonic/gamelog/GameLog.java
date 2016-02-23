package com.moonic.gamelog;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ehc.common.SqlString;
import com.moonic.bac.ItemBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.MyTools;

import conf.Conf;
import conf.LogTbName;


/**
 * ��Ϸ��־
 * @author John
 */
public class GameLog {
	private static final String tab_game_log_datatype = "tab_game_log_datatype";
	
	private static final String tab_infull_prop_log = "tab_infull_prop_log";
	private static final String tab_coin_log_type = "tab_coin_log_type";
	
	public static final String tab_income_prop_log = "tab_income_prop_log";
	
	private short act;
	
	private int factionid;
	
	public int playerid;
	
	public StringBuffer consumeSb = new StringBuffer();//����
	public StringBuffer obtainSb = new StringBuffer();//���
	public StringBuffer remarkSb = new StringBuffer();//��ע
	
	public JSONObject chadataarr = new JSONObject();
	
	private String centerOrderNo;
	
	/**
	 * ����
	 */
	private GameLog(int playerid, short act, int factionid) {
		this.act = act;
		this.factionid = factionid;
		
		this.playerid = playerid;
	}
	
	/**
	 * ������Ʒ���ݱ仯˵��
	 */
	public GameLog addItemChaNoteArr(JSONArray jsonarr) throws Exception {
		for(int i = 0; jsonarr != null && i < jsonarr.length(); i ++) {
			addItemChaNoteObj(jsonarr.optJSONObject(i));
		}
		return this;
	}
	
	/**
	 * ������Ʒ���ݱ仯˵��
	 */
	public GameLog addItemChaNoteObj(JSONObject jsonobj) throws Exception {
		if(jsonobj != null){
			int id = jsonobj.optInt("id");
			String name = null;
			if(jsonobj.has("name")){
				name = jsonobj.optString("name");
			} else {
				name = ItemBAC.getInstance().getListRs(jsonobj.optInt("type"), jsonobj.optInt("num")).getString("name");
			}
			int oldamount = jsonobj.optInt("oldamount");
			int nowamount = jsonobj.optInt("amount");
			byte oldzone = (byte)jsonobj.optInt("oldzone");
			byte newzone = (byte)jsonobj.optInt("zone");
			addChaNote(formatNameID("["+ItemBAC.itemZoneName[newzone]+"]"+name, id), oldamount, nowamount-oldamount, true);
			if(oldzone != newzone){
				addRemark(formatNameID("["+ItemBAC.itemZoneName[newzone]+"]"+name, id)+"��"+ItemBAC.itemZoneName[oldzone]+"�Ƶ�"+ItemBAC.itemZoneName[newzone]);
			}
			jsonobj.remove("name");
			jsonobj.remove("oldamount");
			jsonobj.remove("oldzone");
			jsonobj.remove("extend");
		}
		return this;
	}
	
	/**
	 * ͭǮ
	 */
	public static final String TYPE_MONEY = "ͭǮ";
	/**
	 * ��
	 */
	public static final String TYPE_COIN = "��";
	
	/**
	 * �������ݱ仯˵��
	 */
	public GameLog addChaNote(String name, long oldVal, long chaVal){
		return addChaNote(name, oldVal, chaVal, true);
	}
	
	/**
	 * �������ݱ仯˵��
	 * @param name ��������
	 * @param oldVal �仯ǰ����ֵ
	 * @param chaVal �仯��
	 * @param sys_change �Ƿ�Ϊϵͳ�仯
	 */
	public GameLog addChaNote(String name, long oldVal, long chaVal, boolean sys_change){
		if(chaVal != 0){
			if(chaVal > 0){
				obtainSb/*.append("���")*/.append(name).append("��").append(formatAmount(oldVal, chaVal)).append("\r\n");
			} else 
			if(chaVal < 0){
				consumeSb/*.append("����")*/.append(name).append("��").append(formatAmount(oldVal, chaVal)).append("\r\n");
			}
			try {
				DBPaRs rs = DBPool.getInst().pQueryA(tab_game_log_datatype, "name='"+name+"'");	
				if(rs.exist()){
					JSONArray arr = chadataarr.optJSONArray(name);
					if(arr == null){
						arr = new JSONArray();
						arr.add(rs.getString("chacol"));
						arr.add(rs.getString("nowcol"));
						arr.add(rs.getString("syscol"));
						arr.add(oldVal);
						arr.add(chaVal);
						arr.add(sys_change?1:0);
						chadataarr.put(name, arr);
					} else {
						arr.put(3, arr.optLong(3)+chaVal);
					}
				}
			} catch (Exception e) {
				System.out.println("name="+name);
				e.printStackTrace();
			}
		}
		return this;
	}
	
	/**
	 * ���ó�ֵ���Ķ�����
	 */
	public GameLog setCenterOrderNo(String centerOrderNo) {
		this.centerOrderNo = centerOrderNo;
		return this;
	}
	
	/**
	 * ��ʽ���仯����
	 */
	private String formatAmount(long oldAmount, long chaAmount) {
		StringBuffer formatStr = new StringBuffer();
		formatStr.append(chaAmount);
		formatStr.append("��");
		formatStr.append(oldAmount);
		formatStr.append("��");
		formatStr.append(oldAmount+chaAmount);
		formatStr.append("��");
		return formatStr.toString();
	}
	
	/**
	 * ��ȡ��ʽ��������ID�ַ���
	 */
	public static String formatNameID(String name, int id){
		return name+"("+id+")";
	}
	
	/**
	 * ��������˵��
	 */
	public GameLog addConsume(StringBuffer consumeSb) {
		if(consumeSb != null && consumeSb.length() > 0){
			this.consumeSb.append(consumeSb.toString()+"\r\n");
		}
		return this;
	}
	
	/**
	 * ��������˵��
	 */
	public GameLog addConsume(String str){
		if(str != null){
			consumeSb.append(str+"\r\n");
		}
		return this;
	}
	
	/**
	 * ���ӻ��˵��
	 */
	public GameLog addObtain(StringBuffer obtainSb) {
		if(obtainSb != null && obtainSb.length() > 0){
			this.obtainSb.append(obtainSb.toString()+"\r\n");
		}
		return this;
	}
	
	/**
	 * ���ӻ��˵��
	 */
	public GameLog addObtain(String str){
		if(str != null){
			obtainSb.append(str+"\r\n");
		}
		return this;
	}
	
	/**
	 * ���ӱ�ע˵��
	 */
	public GameLog addRemark(StringBuffer remarkSb) {
		if(remarkSb != null && remarkSb.length() > 0){
			this.remarkSb.append(remarkSb.toString()+"\r\n");
		}
		return this;
	}
	
	/**
	 * ���ӱ�ע˵��
	 */
	public GameLog addRemark(String str){
		if(str != null){
			remarkSb.append(str+"\r\n");
		}
		return this;
	}
	
	/**
	 * ����
	 */
	public void save() {
		DBHelper dbHelper = new DBHelper();
		try {
			SqlString sqlString = new SqlString();
			sqlString.add("playerid", playerid);
			sqlString.add("serverid", Conf.sid);
			sqlString.add("act", act);
			if(consumeSb != null && consumeSb.length() > 0) {
				sqlString.add("consume", consumeSb.toString());
			} 
			if(obtainSb != null && obtainSb.length() > 0) {
				sqlString.add("obtain", obtainSb.toString());
			}
			if(remarkSb != null && remarkSb.length() > 0) {
				sqlString.add("remark", remarkSb.toString());
			}
			if(factionid != 0) {
				sqlString.add("factionid", factionid);
			}
			long coinlog_data = 0;
			JSONArray chadata = chadataarr.toJSONArray();
			for(int i = 0; chadata!=null && i < chadata.length(); i++){
				JSONArray arr = chadata.optJSONArray(i);
				String cha_col = arr.optString(0);
				String now_col = arr.optString(1);
				String sys_col = arr.optString(2);
				long oldVal = arr.optLong(3);
				long chaVal = arr.optLong(4);
				long sysVal = arr.optLong(5);
				sqlString.add(cha_col, chaVal);
				sqlString.add(now_col, oldVal+chaVal);
				sqlString.add(sys_col, sysVal);
				//�𶧽�������
				if(cha_col.equals("changecoin")){
					coinlog_data = chaVal;
				}
			}
			sqlString.addDateTime("createtime", MyTools.getTimeStr());
			DBHelper.logInsert(LogTbName.TAB_GAME_LOG(), sqlString);
			//�𶧽���
			if(coinlog_data != 0){
				DBPaRs typeRs = DBPool.getInst().pQueryA(tab_coin_log_type, "itemid="+act);
				DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
				int userid = 0;
				String username = null;
				String platformname = null;
				String ip = null;
				ResultSet userRs = dbHelper.query("tab_user", "pookid,username,platform,ip", "id="+plaRs.getInt("userid"));
				if(userRs.next()){
					userid = userRs.getInt("pookid");
					username = userRs.getString("username");
					DBPaRs platformRs = DBPool.getInst().pQueryA("tab_platform", "code='"+userRs.getString("platform")+"'");
					platformname = platformRs.getString("name");
					ip = userRs.getString("ip");
				}
				DBPaRs channelServerRs = DBPool.getInst().pQueryA(ServerBAC.tab_channel_server, "vsid="+plaRs.getInt("vsid"));
				String gamename = "��������-"+channelServerRs.getString("servername");
				int type = act;
				int awardtype = -1;
				String itemname = null;
				String obtain = null;
				int validitytype = -1;
				int validityday = 0;
				if(typeRs.exist()){
					awardtype = typeRs.getInt("awardtype");
					itemname = typeRs.getString("itemname");
					obtain = typeRs.getString("obtain");
					validitytype = typeRs.getInt("validitytype");
					validityday = typeRs.getInt("validityday");
				}
				int newcoin = plaRs.getInt("coin");
				SqlString streamSqlStr = new SqlString();
				streamSqlStr.add("user_id", userid);
				streamSqlStr.add("user_name", username);
				streamSqlStr.add("agent_name", platformname);
				streamSqlStr.add("game_name", gamename);
				streamSqlStr.add("money_type", "��");
				streamSqlStr.add("type", type);
				streamSqlStr.add("award_type", awardtype);
				streamSqlStr.add("award_num", coinlog_data);
				streamSqlStr.add("begin_num", newcoin-coinlog_data);
				streamSqlStr.add("end_num", newcoin);
				streamSqlStr.add("client_ip", ip);
				streamSqlStr.add("related_num", "");
				streamSqlStr.addDateTime("createtime", MyTools.getTimeStr());
				dbHelper.insert(tab_infull_prop_log, streamSqlStr);
				if(coinlog_data < 0){
					SqlString incomeSqlStr = new SqlString();
					incomeSqlStr.add("user_id", userid);
					incomeSqlStr.add("user_name", username);
					incomeSqlStr.add("agent_name", platformname);
					incomeSqlStr.add("prop_type", validitytype);
					incomeSqlStr.add("game_name", gamename);
					incomeSqlStr.add("prop_name", itemname);
					incomeSqlStr.add("buy_amount", 1);
					incomeSqlStr.add("money_type", 0);
					incomeSqlStr.add("pay_money", -coinlog_data);
					incomeSqlStr.addDateTime("pay_time", MyTools.getTimeStr());
					incomeSqlStr.addDateTime("effective_time", MyTools.getTimeStr(System.currentTimeMillis()+validityday*MyTools.long_day));
					incomeSqlStr.add("effective_day", validityday);
					incomeSqlStr.add("present_info", obtain);
					incomeSqlStr.add("client_ip", ip);
					incomeSqlStr.add("platform", "�ƶ�");
					incomeSqlStr.add("related_num", centerOrderNo);
					incomeSqlStr.add("company_id", "����");
					incomeSqlStr.addDateTime("createtime", MyTools.getTimeStr());
					dbHelper.insert(tab_income_prop_log, incomeSqlStr);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ������
	 */
	public short getAct() {
		return act;
	}
	
	//--------------��̬��---------------
	
	/**
	 * ��ȡʵ��
	 */
	public static GameLog getInst(int playerid, short act) {
		return getInst(playerid, act, 0);
	}
	
	/**
	 * ��ȡʵ��
	 */
	public static GameLog getInst(int playerid, short act, int factionid) {
		return new GameLog(playerid, act, factionid);
	}
}
