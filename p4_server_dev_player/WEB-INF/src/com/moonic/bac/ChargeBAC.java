package com.moonic.bac;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.GamePushData;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * ��ֵ
 * @author John
 */
public class ChargeBAC {
	public static final String tab_channel_charge_type = "tab_channel_charge_type";
	public static final String tab_charge_type = "tab_charge_type";
	public static final String tab_charge = "tab_charge";
	
	public static final byte FROM_CLIENT = 1; //�ͻ��˹���
	public static final byte FROM_WEB = 2; //��վ����
	public static final byte FROM_ORDERGIVE = 3; //���Ӳ�����
	public static final byte FROM_CONSOLE = 4; //�������ķ���
	
	public static final String[] from_str = {"�ͻ���", "��վ", "����", "��̨"};
	
	public String getFromStr(byte from){
		if(from <= from_str.length){
			return from_str[from-1];
		} else {
			return "δ֪("+from+")";
		}
	}
	
	/**
	 * ���Զ�������act�ĳ�ֵ
	 * @param from ��Դ 1�ͻ��˹��� 2��վ���� 3����
	 */
	public ReturnValue recharge(int playerid, int rechargetype, int rmbam, byte result, String resultnote, byte from, String channel, byte chargepoint, String centerOrderNo) {
		DBHelper dbHelper = new DBHelper();
		try {
			DBPaRs chargetypeRs = DBPool.getInst().pQueryA(tab_charge_type, "num="+rechargetype);
			String ctStr = null;
			if(chargetypeRs.exist()){
				ctStr = chargetypeRs.getString("name");
			} else {
				ctStr = "δ֪("+rechargetype+")";
			}
			if(result == 0){
				GameLog.getInst(playerid, GameServlet.ACT_PLAYER_RECHARGE)
				.addRemark("��ֵʧ�ܣ���ֵ���ͣ�"+ctStr+"��ʧ����Ϣ��"+resultnote+"�����ԣ�"+getFromStr(from))
				.save();
				if(from==FROM_CLIENT)
				{
					JSONObject theobj = new JSONObject();
					theobj.put("result", result);
					theobj.put("note", resultnote);
					theobj.put("from", from);
					PushData.getInstance().sendPlaToOne(SocketServer.ACT_PLAYER_RECHARGE, theobj.toString(), playerid);
				}
				return new ReturnValue(true);
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			int oldcoin = plaRs.getInt("coin");
			JSONArray rechargetypesarr = new JSONArray(plaRs.getString("rechargtypes"));
			DBPsRs chargeRs = DBPool.getInst().pQueryS(tab_charge, "rmb<="+rmbam, "rmb desc");
			boolean isfirst = plaRs.getString("firstrechargetime") == null;
			int buycoin = rmbam * 10; //��ֵ��õĽ�
			int rebatecoin = 0; //�����͵Ľ�
			if(chargeRs.next()){
				rebatecoin = chargeRs.getInt("rebatecoin");
				if(!rechargetypesarr.contains(rmbam)){
					rechargetypesarr.add(rmbam);
				}
			} else {
				System.out.println("δ�ҵ���ֵ���޶�Ӧ���ͽ��� rmb="+rmbam);
			}
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_RECHARGE);
			SqlString sqlStr = new SqlString();
			sqlStr.addChange("coin", buycoin+rebatecoin);
			sqlStr.addChange("rebatecoin", rebatecoin);
			sqlStr.addChange("rechargermb", rmbam);
			sqlStr.addChange("rechargeam", 1);
			sqlStr.add("rechargtypes", rechargetypesarr.toString());
			if(isfirst){
				sqlStr.addDateTime("firstrechargetime", MyTools.getTimeStr());
			}
			int[] vipdata = VipBAC.getInstance().addChangeVIPToSqlStr(plaRs, buycoin, sqlStr, gl);
			PlayerBAC.getInstance().update(dbHelper, playerid, sqlStr);
			
			gl.addChaNote(GameLog.TYPE_COIN, oldcoin, buycoin+rebatecoin)
			.addRemark("��ֵ�ɹ�����ֵ���ͣ�"+ctStr+"������𶧣�"+buycoin+"�������𶧣�"+rebatecoin+"�����ԣ�"+getFromStr(from))
			.setCenterOrderNo(centerOrderNo)
			.save();
			
			JSONObject buyInfo = new JSONObject();
			buyInfo.put("result", result);
			buyInfo.put("buytype", 1); //buytype=1��ʾ����𶧣�������Ȩ��2֮�����ֵ��ʾ
			buyInfo.put("givecoin", buycoin+rebatecoin); //��ֵ�Ľ�����
			String note="���ѳɹ���ֵ"+rmbam+"Ԫ,���"+buycoin+"��";
			if(rebatecoin>0)
			{
				note += ",��������"+rebatecoin+"��";
			}
			if(vipdata[1] > vipdata[0])
			{
				note += ",��ϲ����VIP�ȼ���"+vipdata[0]+"����������"+vipdata[1]+"����";
			}			
			buyInfo.put("note", note); //��ֵ����
			buyInfo.put("viplevel", vipdata[1]); //��ֵ���vip�ȼ�
			buyInfo.put("vipexp", vipdata[2]); //��ֵ���vip����ֵ
			buyInfo.put("rechargermb", rmbam);
			buyInfo.put("from", from); //��ֵ��Դ
			LogBAC.logout("charge/"+channel, "��������push_act="+SocketServer.ACT_PLAYER_RECHARGE+",buyInfo="+buyInfo.toString()+"playerid="+playerid);
			PushData.getInstance().sendPlaToOne(SocketServer.ACT_PLAYER_RECHARGE, buyInfo.toString(), playerid);
			
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���Զ�������act�Ĺ�����Ȩ
	 * @param push_act �Զ�������act
	 */
	public ReturnValue buyTQ(int playerid, byte tqnum, byte result, String resultnote, byte from, String channel, String centerOrderNo) {
		DBHelper dbHelper = new DBHelper();
		try {
			if(result == 0){
				GameLog.getInst(playerid, GameServlet.ACT_PLAYER_BUY_TQ)
				.addRemark("������Ȩʧ�ܣ���Ȩ��ţ�"+tqnum+"��ʧ����Ϣ��"+resultnote+"�����ԣ�"+getFromStr(from))
				.save();
				if(from == FROM_CLIENT)
				{
					JSONObject theobj = new JSONObject();
					theobj.put("result", result);
					theobj.put("note", resultnote);
					theobj.put("from", from);
					PushData.getInstance().sendPlaToOne(SocketServer.ACT_PLAYER_BUY_TQ, theobj.toString(), playerid);
				}
				return new ReturnValue(true);
			}
			DBPaRs tqRs = DBPool.getInst().pQueryA(TqBAC.tab_prerogative, "num="+tqnum);
			if(!tqRs.exist() || tqnum==0){
				BACException.throwInstance("�������Ȩ��ţ�"+tqnum);
			}
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			dbHelper.openConnection();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_PLAYER_BUY_TQ);
			int oldcoin = plaRs.getInt("coin");
			int rebatecoin = tqRs.getInt("rebatecoin");
			SqlString plaSqlStr = new SqlString();
			TqBAC.getInstance().addChangeTQToSqlStr(plaRs, tqRs.getInt("num"), tqRs.getInt("days"), plaSqlStr, gl);
			plaSqlStr.addChange("rechargermb", tqRs.getInt("price"));
			plaSqlStr.addChange("rechargeam", 1);
			if(plaRs.getString("firstrechargetime") == null){
				plaSqlStr.addDateTime("firstrechargetime", MyTools.getTimeStr());
			}
			plaSqlStr.addChange("coin", rebatecoin);
			plaSqlStr.addChange("rebatecoin", rebatecoin);
			PlayerBAC.getInstance().update(dbHelper, playerid, plaSqlStr);
			
			gl.addChaNote(GameLog.TYPE_COIN, oldcoin, rebatecoin)
			.addRemark("������Ȩ�ɹ�����Ȩ��ţ�"+tqnum+"�����ԣ�"+getFromStr(from))
			.save();
			
			plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			JSONObject buyInfo = new JSONObject();
			buyInfo.put("result", result);
			buyInfo.put("buytype", 2);
			buyInfo.put("num", plaRs.getInt("tqnum"));
			buyInfo.put("tqduetime", plaRs.getTime("tqduetime"));
			buyInfo.put("rechargermb", tqRs.getInt("price"));
			buyInfo.put("givecoin", rebatecoin);
			buyInfo.put("from", from); //��ֵ��Դ
			LogBAC.logout("charge/"+channel, "��������push_act="+SocketServer.ACT_PLAYER_BUY_TQ+",buyInfo="+buyInfo.toString()+"playerid="+playerid);
			PushData.getInstance().sendPlaToOne(SocketServer.ACT_PLAYER_BUY_TQ, buyInfo.toString(), playerid);
			
			int userid = 0;
			String username = null;
			String platformname = null;
			String ip = null;
			ResultSet userRs = dbHelper.query("tab_user", "pookid,username,platform,ip", "id="+plaRs.getInt("id"));
			if(userRs.next()){
				userid = userRs.getInt("pookid");
				username = userRs.getString("username");
				DBPaRs platformRs = DBPool.getInst().pQueryA("tab_platform", "code='"+userRs.getString("platform")+"'");
				platformname = platformRs.getString("name");
				ip = userRs.getString("ip");
			}
			DBPaRs channelServerRs = DBPool.getInst().pQueryA(ServerBAC.tab_channel_server, "vsid="+plaRs.getInt("vsid"));
			String gamename = "��������-"+channelServerRs.getString("servername");
			
			SqlString incomeSqlStr = new SqlString();
			incomeSqlStr.add("user_id", userid);
			incomeSqlStr.add("user_name", username);
			incomeSqlStr.add("agent_name", platformname);
			incomeSqlStr.add("prop_type", 0);
			incomeSqlStr.add("game_name", gamename);
			incomeSqlStr.add("prop_name", tqRs.getString("name"));
			incomeSqlStr.add("buy_amount", 1);
			incomeSqlStr.add("money_type", 1);
			incomeSqlStr.add("pay_money", tqRs.getInt("price"));
			incomeSqlStr.addDateTime("pay_time", MyTools.getTimeStr());
			incomeSqlStr.addDateTime("effective_time", MyTools.getTimeStr(System.currentTimeMillis()+tqRs.getInt("days")*MyTools.long_day));
			incomeSqlStr.add("effective_day", tqRs.getInt("days"));
			incomeSqlStr.add("present_info", "������Ȩ");
			incomeSqlStr.add("client_ip", ip);
			incomeSqlStr.add("platform", "�ƶ�");
			incomeSqlStr.add("related_num", centerOrderNo);
			incomeSqlStr.add("company_id", "����");
			incomeSqlStr.addDateTime("createtime", MyTools.getTimeStr());
			dbHelper.insert(GameLog.tab_income_prop_log, incomeSqlStr);
			
			GamePushData.getInstance(14)
			.add(plaRs.getString("name"))
			.add(tqRs.getString("name"))
			.add(tqRs.getString("func1").split("\\|")[0].replace("3,", ""))
			.sendToAllOL();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * @param from 1 �ͻ��˹��� 2��վ���� 3 �û�������
	 */
	public ReturnValue orderBatchGive(JSONObject json, byte from, byte chargepoint) {
		JSONArray array = json.optJSONArray("list");
		for(int i=0;array!=null && i<array.length();i++) {
			JSONObject line = array.optJSONObject(i);
			orderReGive(line, from, chargepoint);
		}
		return new ReturnValue(true);
	}
	
	/**
	 * @param from 1 �ͻ��˹��� 2��վ���� 3 �û������� 
	 */
	public ReturnValue orderReGive(JSONObject json, byte from, byte chargepoint) {
		String orderNo = json.optString("orderno");
		String channel = json.optString("channel");
		int orderResult = json.optInt("result");
		int buytype = json.optInt("buytype");
		int gived = json.optInt("gived");
		int playerId = json.optInt("playerId");
		int orderType = json.optInt("orderType");
		int price = json.optInt("price");
		int getpower= json.optInt("getpower");
		String note = json.optString("note");
		String centerOrderNo = json.optString("corderno");
		if(orderResult==1 || orderResult==-1) {
			if(gived==0 || ((gived==-1 || gived==2) && from==FROM_ORDERGIVE)) {//�����ѷ���״̬���̨����
				ReturnValue rv=null;
				//���·���
				if(buytype==1)
				{
					if(from==FROM_ORDERGIVE) //�û�������
					{
						rv = recharge(playerId, (byte)orderType, price, (byte)(orderResult==1?1:0), note, from, channel, chargepoint, centerOrderNo);	
					}
					else
					if(from==FROM_WEB) //��վ����
					{
						rv = recharge(playerId, (byte)orderType, price, (byte)(orderResult==1?1:0), note, from, channel, chargepoint, centerOrderNo);
					}
					else //�ͻ��˹���
					{
						rv = recharge(playerId, (byte)orderType, price, (byte)(orderResult==1?1:0), note, from, channel, chargepoint, centerOrderNo);
					}
				}
				else
				if(buytype==2)
				{
					if(from==FROM_ORDERGIVE) //�û�������
					{
						rv = buyTQ(playerId, (byte)getpower, (byte)(orderResult==1?1:0), note, from,channel, centerOrderNo);
					}
					else
					if(from==FROM_WEB) //��վ����
					{
						rv = buyTQ(playerId, (byte)getpower, (byte)(orderResult==1?1:0), note, from,channel, centerOrderNo);
					}
					else //�ͻ��˹���
					{
						rv = buyTQ(playerId, (byte)getpower, (byte)(orderResult==1?1:0), note, from,channel, centerOrderNo);
					}					
				}
				else
				{
					return new ReturnValue(false,"�������Ͳ���ȷbuytype="+buytype);
				}
				//��־
				if(from==FROM_ORDERGIVE) //�û�������
				{
					LogBAC.logout("charge/"+channel,"�û�������"+orderNo+"�������="+rv.success);
				}
				else
				if(from==FROM_WEB) //��վ����				
				{
					LogBAC.logout("charge/"+channel,"��վ����"+orderNo+"�������="+rv.success);
				}
				else
				if(from==FROM_CLIENT) //�ͻ��˹���
				{
					LogBAC.logout("charge/"+channel,"�ͻ��˹���"+orderNo+"�������="+rv.success);	
				}
				
				if(orderResult==1)
				{
					try
					{
						if(rv.success)
						{
							ChargeSendBAC.getInstance().createSendOrder(Conf.sid, channel, orderNo, 1);	
						}
						else
						{
							ChargeSendBAC.getInstance().createSendOrder(Conf.sid, channel, orderNo, -1);	
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						return new ReturnValue(false,ex.toString());
					}
				}
				
				return rv;
			}
			else
			{
				if(gived==2)
				{
					return new ReturnValue(false,"����"+orderNo+"���ڷ�����");
				}
				else
				{
					return new ReturnValue(false,"����"+orderNo+"�Ѿ�������");
				}				
			}
		}
		else
		if(orderResult==0)
		{
			return new ReturnValue(false,"����"+orderNo+"���ڴ�����");
		}
		/*else
		if(result==-1)
		{
			return new ReturnValue(false,"��������ʧ��:"+note);
		}*/
		else
		{
			return new ReturnValue(false,"�������ֵ�쳣result="+orderResult+",�����¹���");
		}
	}
	
	/**
	 * ����ָ��������ȡ֧������
	 */
	public JSONArray getChargeType(String channel) throws Exception {
		DBPsRs chargeTypeRs = DBPool.getInst().pQueryS(tab_channel_charge_type);
		JSONArray chargeArr = new JSONArray();
		while(chargeTypeRs.next()){
			if(chargeTypeRs.getString("channel").equals(channel)){
				chargeArr.add(chargeTypeRs.getInt("chargetype"));
			}
		}
		return chargeArr;
	}
	
	//------------------��̬��--------------------
	
	private static ChargeBAC instance = new ChargeBAC();

	public static ChargeBAC getInstance() {
		return instance;
	}
}
