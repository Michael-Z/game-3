package com.moonic.bac;

import java.sql.ResultSet;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.moonic.timertask.DBIdleAdjustTT;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MassNetSender;
import com.moonic.util.MyTools;
import com.moonic.util.NetResult;
import com.moonic.util.NetSender;
import com.moonic.util.ProcessQueue;

import conf.Conf;

/**
 * ��Ϸ������BAC
 * @author John
 */
public class ServerBAC {
	public static final String tab_server = "tab_server";
	public static final String tab_channel_server = "tab_channel_server";
	public static final String tab_notice = "tab_notice";
	
	public static ProcessQueue sender_pq = new ProcessQueue();
	
	String goodColor = "[339f2f]"; //˳��
	String busyColor = "[c53f2a]"; //��æ
	String fireyColor = "[ff0000]"; //��
	String restColor = "[d4210b]"; //ά��
	
	/**
	 * ��ȡ�������б�
	 */
	public ReturnValue getServerList(int userid, String channel) {
		DBHelper dbHelper = new DBHelper();
		try {
			JSONObject popjson = PlayerBAC.getInstance().getPop();
			JSONArray listarr = new JSONArray();
			DBPsRs channelServerRs = getChannelServerList(channel);//����������б�
			JSONObject userJson = dbHelper.queryJsonObj(UserBAC.tab_user, "devuser", "id="+userid);
			while(channelServerRs.next()){
				if(channelServerRs.getInt("istest")==1 && userJson.optInt("devuser")!=1){
					continue;
				}
				int vsid = channelServerRs.getInt("vsid");
				int serverid = channelServerRs.getInt("serverid");
				DBPaRs serverRs = DBPool.getInst().pQueryA(tab_server, "id="+serverid);//���������������
				String servername = channelServerRs.getString("servername");
				int state = channelServerRs.getInt("state")!=-1?channelServerRs.getInt("state"):serverRs.getInt("state");
				long opentime = MyTools.getTimeLong(!channelServerRs.getString("opentime").equals("-1")?channelServerRs.getString("opentime"):serverRs.getString("opentime"));
				int tip = channelServerRs.getInt("tip")!=-1?channelServerRs.getInt("tip"):serverRs.getInt("tip");
				String note = !channelServerRs.getString("note").equals("-1")?channelServerRs.getString("note"):serverRs.getString("note");
				String httpurl = serverRs.getString("http");
				String[] socketdata = Tools.splitStr(serverRs.getString("tcp"), ":");
				int onlineamount = popjson.optInt(String.valueOf(serverid));
				int reslv = serverRs.getInt("reslv");
				if(!MyTools.checkSysTimeBeyondSqlDate(opentime)) {
					state = 1;
					note = "[218ab6]"+MyTools.formatTime(opentime, "M��d�� HH:mm")+"����[-]";
				}
				if(userJson.optInt("devuser")==1) {
					state = 0;
				}
				if(state == 0){
					if(tip==2 || onlineamount>=200){
						note = fireyColor+"��[-]";
					} else 
					if(onlineamount>=100){
						note = busyColor+"��æ[-]";
					} else 
					{
						note = goodColor+"˳��[-]";
					}
				} else {
					if(note==null || note.equals("")) {
						note = restColor+"ά����[-]";
					}
				}
				JSONArray arr = new JSONArray();
				arr.add(serverid);//������ID
				arr.add(servername);//��������
				arr.add(state);//������״̬
				arr.add(tip);//��ǩ
				arr.add(note);//˳��
				arr.add(httpurl);//HTTP��ַ
				arr.add(socketdata[0]);//SOCKET��ַ
				arr.add(socketdata[1]);//SOCKET�˿�
				arr.add(Conf.res_url);//��Դ���ص�ַ
				arr.add(vsid);//���������ID
				arr.add(reslv);//��Դ�ȼ�
				arr.add(Math.max(0, (opentime-System.currentTimeMillis())/1000));//ʣ�࿪��ʱ�䣬��λ��S
				listarr.add(arr);
			}
			dbHelper.openConnection();
			JSONArray usedarr = PlayerBAC.getInstance().getUsedServer(dbHelper, userid);
			JSONArray jsonarr = new JSONArray();
			jsonarr.add(listarr);//�������б�
			jsonarr.add(usedarr);//����ķ������б�
			return new ReturnValue(true, jsonarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
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
	 * �����з�����������
	 */
	public ReturnValue sendReqToAll(NetSender sender){
		try {
			String info = converNrsToString(sendReq(null, sender));
			return new ReturnValue(true, info);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ָ��������������
	 */
	public NetResult sendReqToOne(NetSender sender, int serverid) {
		return sendReqToOne(sender, serverid, "sts.do");
	}
	
	/**
	 * ��ָ��������������
	 */
	public NetResult sendReqToOne(NetSender sender, int serverid, String doStr) {
		NetResult nr = null;
		try {
			nr = sendReq("id="+serverid, sender, doStr)[0];
		} catch(Exception e){
			e.printStackTrace();
			nr = new NetResult();
			nr.rv = new ReturnValue(false, e.toString());
		}
		return nr;
	}
	
	/**
	 * ��������
	 */
	public NetResult[] sendReq(String where, NetSender sender) throws Exception {
		return sendReq(where, sender, "sts.do");
	}
	
	/**
	 * ��������
	 */
	public NetResult[] sendReq(String where, NetSender sender, String doStr) throws Exception {
		String tabname = tab_server;
		if(where != null){
			where = "usestate=1 and ("+where+")";
		} else {
			where = "usestate=1";
		}
		DBPsRs sRs = DBPool.getInst().pQueryS(tabname, where);
		if(!sRs.have()){
			BACException.throwInstance("ָ��Ŀ������������� where:"+where);
		}
		MassNetSender mns = new MassNetSender();
		while(sRs.next()){
			mns.addURL((byte)1, sRs.getInt("id"), sRs.getString("name"), "http://"+sRs.getString("http") + doStr);
		}
		NetResult[] nrs = mns.send(sender);
		return nrs;
	}
	
	/**
	 * ������ת��Ϊ�ַ���
	 */
	private String converNrsToString(NetResult[] nrs){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < nrs.length; i++){
			sb.append("��������");
			sb.append(nrs[i].name);
			sb.append("\t");
			sb.append("�����");
			sb.append(nrs[i].rv.success);
			sb.append(", ");
			sb.append(nrs[i].rv.info);
			sb.append("\r\n\r\n");
		}
		return sb.toString();
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
	
	/**
	 * WEB��ȡָ�����������з�����
	 */
	public ReturnValue webGetServerList(String channel){
		try {
			DBPsRs channelServerRs = getChannelServerList(channel);
			JSONArray returnarr = new JSONArray();
			while(channelServerRs.next()){
				if(channelServerRs.getInt("istest")==0){
					JSONObject obj = new JSONObject();
					obj.put("id", channelServerRs.getInt("vsid"));
					obj.put("name", channelServerRs.getString("servername"));
					returnarr.add(obj);
				}
			}
			return new ReturnValue(true, returnarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * WEB��ȡָ���û��½�ɫ�ķ�����
	 */
	public ReturnValue webGetUserServerList(String platform, String username){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			JSONArray returnarr = new JSONArray();
			ResultSet userRs = dbHelper.query("tab_user", "id,channel", "username='"+username+"' and platform='"+platform+"'");
			if(userRs.next()){
				JSONArray usedarr = PlayerBAC.getInstance().getUsedServer(dbHelper, userRs.getInt("id"));
				DBPsRs channelServerRs = getChannelServerList(userRs.getString("channel"));
				while(channelServerRs.next()){
					if(channelServerRs.getInt("istest")==0){
						if(usedarr.contains(channelServerRs.getInt("vsid"))){
							JSONObject obj = new JSONObject();
							obj.put("id", channelServerRs.getInt("vsid"));
							obj.put("name", channelServerRs.getString("servername"));
							returnarr.add(obj);
						}
					}
				}	
			}
			return new ReturnValue(true, returnarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	public static ScheduledExecutorService timer;
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void initTimer(int serverid){
		timer = MyTools.createTimer(3);
		DBIdleAdjustTT.init();
	}
	
	private static ServerBAC instance = new ServerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ServerBAC getInstance(){
		return instance;
	}
}
