package com.moonic.bac;

import java.io.File;
import java.sql.ResultSet;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.moonic.socket.GamePushData;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketServer;
import com.moonic.timertask.CBIssueLeaderAwardTT;
import com.moonic.timertask.CBNpcInvadeTT;
import com.moonic.timertask.CBOutPutFacMoneyTT;
import com.moonic.timertask.CBRecoverNPCTT;
import com.moonic.timertask.CBRefWorldLvTT;
import com.moonic.timertask.ClearDataTT;
import com.moonic.timertask.DBIdleAdjustTT;
import com.moonic.timertask.FacRankingTT;
import com.moonic.timertask.JJCAwardIssueTT;
import com.moonic.timertask.MineralsTT;
import com.moonic.timertask.ReplayClearTT;
import com.moonic.timertask.SummonDayTT;
import com.moonic.timertask.SummonWeekTT;
import com.moonic.timertask.TeamActivityTT;
import com.moonic.timertask.TowerSendAwardTT;
import com.moonic.timertask.WorldBossTT;
import com.moonic.util.BACException;
import com.moonic.util.ConfFile;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;
import com.moonic.util.NetResult;
import com.moonic.util.NetSender;
import com.moonic.util.ProcessQueue;

import conf.Conf;

/**
 * ������
 * @author John
 */
public class ServerBAC {
	public static final String tab_server = "tab_server";
	public static final String tab_channel_server = "tab_channel_server";
	
	public static final String tab_notice = "tab_notice";
	
	public static ProcessQueue sender_pq = new ProcessQueue();
	
	/**
	 * ����֤������������
	 */
	public NetResult sendReqToMain(NetSender sender){
		return sendReqToOne(sender, Conf.ms_url);
	}
	
	/**
	 * ��ָ��������������
	 */
	public NetResult sendReqToOne(NetSender sender, String url) {
		NetResult nr = null;
		try {
			String stsurl = url + "sts.do";
			nr = sender.send(stsurl);
		} catch(Exception e){
			e.printStackTrace();
			nr = new NetResult();
			nr.rv = new ReturnValue(false, e.toString());
		}
		return nr;
	}
	
	/**
	 * ��֪ͨ
	 */
	public ReturnValue sendInform(int playerid, String title, String content, String overtimeStr, String extend, boolean sqlAll, int[] byids){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(title == null){
				BACException.throwInstance("���ⲻ��Ϊ��");
			}
			if(content == null){
				BACException.throwInstance("���ݲ���Ϊ��");
			}
			JSONArray jsonarr = new JSONArray();
			jsonarr.add(title);
			jsonarr.add(content);
			if(extend != null && !extend.equals("")){
				jsonarr.add(new JSONObject(extend));	
			}
			if(byids != null){
				PushData.getInstance().setOverTime(overtimeStr).sendPlaToSome(SocketServer.ACT_SYS_INFORM, jsonarr.toString(), byids);
			} else {
				if(playerid == 0){
					if(sqlAll){
						PushData.getInstance().setOverTime(overtimeStr).sendPlaToAllSql(dbHelper, SocketServer.ACT_SYS_INFORM, jsonarr.toString());
					} else {
						PushData.getInstance().setOverTime(overtimeStr).sendPlaToAllOL(SocketServer.ACT_SYS_INFORM, jsonarr.toString());
					}
				} else {
					PushData.getInstance().setOverTime(overtimeStr).sendPlaToOne(SocketServer.ACT_SYS_INFORM, jsonarr.toString(), playerid);
				}	
			}
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ϵͳ��Ϣ
	 */
	public ReturnValue sendSysMsg(String msg){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(msg == null){
				BACException.throwInstance("��Ϣ���ݲ���Ϊ��");
			}
			JSONArray arr = MsgBAC.getInstance().getSysMsgBag(msg);
			PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_MESSAGE_RECEIVE, arr.toString());
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����Ϸ����
	 */
	public ReturnValue sendGamePush(String param){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(param == null){
				BACException.throwInstance("��Ϣ���ݲ���Ϊ��");
			}
			String[] params = Tools.splitStr(param, ",");
			GamePushData gpd = GamePushData.getInstance(Integer.valueOf(params[0]));
			for(int i = 1; i < params.length; i++){
				gpd.add(params[i]);
			}
			gpd.sendToAllOL();
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������Ϣ
	 */
	public ReturnValue sendTopMsg(String msg){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(msg == null){
				BACException.throwInstance("��Ϣ���ݲ���Ϊ��");
			}
			PushData.getInstance().setNopool(true).sendPlaToAllOL(SocketServer.ACT_MESSAGE_TOP, msg);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ɾ������������
	 */
	public ReturnValue clearServerData(){
		deleteConfTxt();
		return new ReturnValue(true);
	}
	
	/**
	 * ɾ��TXT�����ļ�
	 */
	public ReturnValue deleteConfTxt(){
		try {
			String txtFolderPath = ServerConfig.getWebInfPath()+"txt_conf/";
			File txtConf = new File(txtFolderPath);
			if(txtConf.exists()){
				File[] files = txtConf.listFiles();
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}	
			}
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	/**
	 * ����׼��
	 */
	public ReturnValue openServerReady(){
		try {
			int minIdle = ServerConfig.getDataBase().setMinIdleToMax();
			ConfFile.updateFileValue(DBIdleAdjustTT.MIN_IDLE, String.valueOf(minIdle));
			return new ReturnValue(true, "����ɹ�("+minIdle+")");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����������
	 */
	public ReturnValue adjustDBIdle(){
		try {
			int minIdle = ServerConfig.getDataBase().adjustMinIdle();
			ConfFile.updateFileValue(DBIdleAdjustTT.MIN_IDLE, String.valueOf(minIdle));
			return new ReturnValue(true, String.valueOf(minIdle));
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ȡ����ʱ��
	 */
	public long getOpenTime() throws Exception{
		DBPaRs serRs = DBPool.getInst().pQueryA(tab_server, "id="+Conf.sid);
		long time = serRs.getTime("opentime");
		return time;
	}
	
	/**
	 * ��ȡ��������
	 */
	public int getOpenDays() throws Exception{
		long t1 = MyTools.getCurrentDateLong();
		long t2 = MyTools.getCurrentDateLong(getOpenTime());
		int opendays = (int)((t1-t2)/MyTools.long_day)+1;
		if(opendays < 1){
			opendays = 1;
		}
		return opendays;
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getNotice(DBHelper dbHelper, long lastlogintime) throws Exception {
		DBPaRs noticeRs = DBPool.getInst().pQueryA(tab_notice, "serverid="+Conf.sid);
		if(!noticeRs.exist()){
			return null;
		}
		if(noticeRs.getInt("loopshow")==0 && (lastlogintime>noticeRs.getTime("createtime") || System.currentTimeMillis()>noticeRs.getTime("createtime"))){
			return null;
		}
		JSONArray returnarr = new JSONArray();
		returnarr.add(noticeRs.getString("title"));
		returnarr.add(noticeRs.getString("content"));
		return returnarr;
	}
	
	/**
	 * ��ȡָ�������ķ������б�
	 */
	public DBPsRs getChannelServerList(String channel) throws Exception {
		DBPsRs channelServerRs = DBPool.getInst().pQueryS(tab_channel_server, "channel='"+channel+"' and visible=1", "disporder desc");//�Ȳ��������Զ������÷�ʽ
		if(!channelServerRs.have()) {//�������Զ������ã���Ĭ������001
			channelServerRs = DBPool.getInst().pQueryS(tab_channel_server, "channel='001' and visible=1", "disporder desc");
		}			
		return channelServerRs;
	}
	
	/**
	 * ��ȡָ�������ķ�����
	 */
	public DBPsRs getChannelServer(String channel, int vsid) throws Exception {
		//���ԣ���ѯ�����Ƿ����Զ������ã����Զ���ʹ���Զ��壬û�Զ���ʹ��ȫ������
		DBPsRs channelServerRs = DBPool.getInst().pQueryS(tab_channel_server, "channel='"+channel+"' and visible=1 and vsid="+vsid, "disporder desc");//�Ȳ��������Զ������÷�ʽ
		if(!channelServerRs.have()) {//�������Զ������ã���Ĭ������001
			channelServerRs = DBPool.getInst().pQueryS(tab_channel_server, "channel='001' and visible=1 and vsid="+vsid, "disporder desc");
		}			
		return channelServerRs;
	}
	
	/**
	 * ��̨��ȡ����
	 */
	public JSONObject getServerData(){
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Backup());
		try {
			dbHelper.openConnection();
			DBPaRs serverRs = DBPool.getInst().pQueryA(tab_server, "id="+Conf.sid);
			JSONObject serverobj = serverRs.getJsonobj();
			ResultSet amountRs = dbHelper.query("tab_player", "count(*) as amount", "serverid="+Conf.sid);
			amountRs.next();
			serverobj.put("pam", amountRs.getInt("amount"));
			serverobj.put("pamol", SocketServer.getInstance().plamap.size());
			serverobj.put("maxplayer", Conf.max_player);
			return serverobj;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ����������״̬����
	 */
	public ReturnValue getRunState(){
		try {
			JSONArray statearr = new JSONArray();
			statearr.add(ServerConfig.getDataBase().getNumActive());//�������
			statearr.add(ServerConfig.getDataBase().getNumIdle());//����������
			statearr.add(ServerConfig.getDataBase().getMaxThe());//���������
			statearr.add(Runtime.getRuntime().freeMemory()/1000/1000);//�����ڴ�
			statearr.add(Runtime.getRuntime().totalMemory()/1000/1000);//���ڴ�
			statearr.add(Runtime.getRuntime().maxMemory()/1000/1000);//����ڴ�
			statearr.add(Thread.activeCount());//���߳���
			return new ReturnValue(true, statearr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	//--------------�ڲ���--------------
	
	
	//--------------��̬��--------------
	
	public static ScheduledExecutorService timer;
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void initTimer(){
		timer = MyTools.createTimer(3);
		FacRankingTT.init();
		ClearDataTT.init();
		DBIdleAdjustTT.init();
		JJCAwardIssueTT.init();
		SummonDayTT.init();
		SummonWeekTT.init();
		CBOutPutFacMoneyTT.init();
		CBRecoverNPCTT.init();
		CBIssueLeaderAwardTT.init();
		CBNpcInvadeTT.init();
		WorldBossTT.init();
		TowerSendAwardTT.init();
		CBRefWorldLvTT.init();
		TeamActivityTT.init();
		MineralsTT.init();
		ReplayClearTT.init();
	}
	
	private static ServerBAC instance = new ServerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ServerBAC getInstance(){
		return instance;
	}
}
