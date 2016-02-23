package com.moonic.bac;

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.memcache.MemcachedUtil;
import com.moonic.mgr.LockStor;
import com.moonic.mode.User;
import com.moonic.platform.P;
import com.moonic.servlet.STSServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.MyTools;
import com.moonic.util.STSNetSender;

import conf.LogTbName;

/**
 * �û�BAC
 * @author John
 */
public class UserBAC {
	public static final String tab_user = "tab_user";
	
	private static final char[] chars = new char[]{
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
		};
	
	public static HashMap<String, User> session_usermap = new HashMap<String, User>(131072);
	
	private static int lowMemory=60; //�ֻ��ڴ�����ֵ
	
	/**
	 * ������Ϸ
	 */
	public ReturnValue shortcutGame(String ip, String channel, JSONArray logdata){
		try {
			synchronized (LockStor.getLock(LockStor.USER_REGISTER)) {
				byte tryamount = 0;
				while(true){
					StringBuffer str = new StringBuffer();
					str.append("user");
					for(int i = 0; i < 5; i++){
						str.append(chars[MyTools.getRandom(0, chars.length-1)]);
					}
					String username = str.toString();
					ReturnValue regRv = register(username, username, username, ip, channel, logdata);
					if(regRv.success){
						return new ReturnValue(true, username);
					} else 
					if(tryamount>10){
						return new ReturnValue(false, "����ע��ʧ�ܣ������³���");
					} else 
					{
						System.out.println(regRv.info);
					}
					tryamount++;
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ע��
	 */
	public ReturnValue register(String username, String password, String rePassword, String ip, String channel, JSONArray logdata){
		DBHelper dbHelper = new DBHelper();
		try {
			MyTools.checkNoChar(username);
			MyTools.checkNoChar(password);
			if(!password.equals(rePassword)){
				BACException.throwInstance("�������벻һ��");
			}
			DBPaRs channelRs = ChannelBAC.getInstance().getChannelListRs(channel);
			if(!channelRs.exist()){
				BACException.throwInstance("������������ channel="+channel);
			}
			String platform = channelRs.getString("platform");
			if(username == null || password == null || username.equals("") || password.equals("")){
				BACException.throwInstance("�ʺŻ�������Ϊ��");
			}
			synchronized (LockStor.getLock(LockStor.USER_REGISTER)) {
				dbHelper.openConnection();
				boolean exist = dbHelper.queryExist(tab_user, "username='"+username+"' and platform='"+platform+"'");
				if(exist){
					BACException.throwInstance("�û����Ѵ���");
				}
				dbHelper.closeConnection();
				P.getInstance(platform).register(dbHelper, username, password, rePassword, ip, channel, logdata);
			}
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	private CreateRobotThread createRobotThread;
	
	/**
	 * ע�Ὰ���������ʺ�
	 */
	public ReturnValue registerJJCUser(){
		try {
			if(createRobotThread != null){
				if(createRobotThread.result == null){
					return new ReturnValue(false, "[doing]");
				} else {
					String info = createRobotThread.result;
					createRobotThread = null;
					return new ReturnValue(true, info);
				}
			}
			createRobotThread = new CreateRobotThread();
			(new Thread(createRobotThread)).start();
			return new ReturnValue(false, "[doing]");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	class CreateRobotThread implements Runnable {
		private String result;
		public void run() {
			DBHelper dbHelper = new DBHelper();
			try {
				ResultSet userRs = dbHelper.query("tab_user", "id", "platform='000' and username like 'jjc_%'");
				userRs.last();
				if(userRs.getRow() >= 5000){
					result = "�Ѵ���5000�����������ʺ�";
					return;
				}
				DecimalFormat df = new DecimalFormat("0000");
				int count = 0;
				for(int i = 1; i <= 5000; i++){
					ReturnValue registerRv = register("jjc_"+df.format(i), "abc123", "abc123", "192.168.1.1", "000", webGetLogdata("192.168.1.1"));
					if(registerRv.success){
						count++;
					}
				}
				result = "ע�Ὰ���������ʺ�:"+count;
			} catch (Exception e) {
				e.printStackTrace();
				result = "���������з����쳣:"+e.toString();
			} finally {
				dbHelper.closeConnection();
			}
		}
	}
	
	/**
	 * �����û�����
	 */
	public void insert(DBHelper dbHelper, String username, String password, String channel, String platform, String ip, JSONArray logdata) throws Exception {
		SqlString sqlStr = new SqlString();
		sqlStr.add("username", username);
		sqlStr.add("password", password);
		sqlStr.add("channel", channel);
		sqlStr.add("platform", platform);
		sqlStr.add("systemtype", logdata.optInt(12));
		sqlStr.add("ip", ip);
		sqlStr.addDateTime("regtime", MyTools.getTimeStr());
		sqlStr.add("enable", 1);
		sqlStr.add("onlinestate", 0);
		sqlStr.add("phonevendor", logdata.optString(0));//�ֻ�����
		sqlStr.add("phonemodel", logdata.optString(1));//�ֻ�����
		sqlStr.add("phonesdklv", logdata.optInt(2));//�ֻ�֧�ֵ�sdk����
		sqlStr.add("phonesdkver", logdata.optString(3));//�ֻ�ϵͳ�汾��
		sqlStr.add("resolution", logdata.optString(4));//�ֱ���
		sqlStr.add("netname", logdata.optString(5));//������
		sqlStr.add("nettype", logdata.optInt(6));//��������
		sqlStr.add("wifi", logdata.optInt(7));//WIFI
		sqlStr.add("imei", logdata.optString(8));//IMEI
		sqlStr.add("mac", logdata.optString(9).toUpperCase());//MAC
		sqlStr.add("phonefreemem", logdata.optInt(10));//ʣ���ڴ�MB
		sqlStr.add("phonetotalmem", logdata.optInt(11));//���ڴ�MB
		dbHelper.insert(tab_user, sqlStr);
	}
	
	/**
	 * ��¼
	 * @param loginport 0����Ϸ�ͻ��˵�½ 1:��̨��½
	 */
	public ReturnValue login(String username, String password, String ip, int loginport, JSONArray logdata, String channel, String extend) {		
		DBHelper dbHelper = new DBHelper();
		try {
			MyTools.checkNoChar(username);
			MyTools.checkNoChar(password);
			//�ƹ�������֤
			DBPaRs channelRs = ChannelBAC.getInstance().getChannelListRs(channel);
			if(!channelRs.exist()){
				BACException.throwInstance("������������ channel="+channel);
			}
			if(channelRs.getInt("apply")==0){//�ƹ�����δ��Ӧ��
				BACException.throwInstance("��¼ϵͳά���У����Ժ����ԡ�");
			}
			String platform = channelRs.getString("platform");
			String imei = logdata.optString(8);
			String mac = logdata.optString(9).toUpperCase();
			//������¼���
			boolean client_enforcementlogin = extend!=null && extend.startsWith("Ky4LTF71D4ANrhpl");
			SqlString userSqlStr = new SqlString();
			JSONObject returnobj = null;
			if(client_enforcementlogin){
				returnobj = new JSONObject();
				returnobj.put("username", username);
			} else {
				returnobj = P.getInstance(platform).login(channel, extend, username, password, ip, imei, mac, loginport, userSqlStr);
			}
			username = returnobj.optString("username");
			JSONObject userJson = dbHelper.queryJsonObj(tab_user, "*", "username='"+username+"' and platform='"+platform+"'");
			if(userJson == null && client_enforcementlogin) {
				BACException.throwInstance("ǿ�Ƶ�¼ʧ�ܣ����ʺż�¼");
			}
			//�û���֤�ɹ�,���û����в����ڣ�������û�
			if(userJson == null) {
				insert(dbHelper, username, "", channel, platform, ip, logdata);
				userJson = dbHelper.queryJsonObj(tab_user, null, "username='"+username+"' and platform='"+platform+"'");
			}
			if(client_enforcementlogin && userJson.optString("enforcementlogin").equals("")){
				BACException.throwInstance("ǿ�Ƶ�¼ʧ�ܣ��ʺŲ�����ǿ�Ƶ�¼");
			}
			if(client_enforcementlogin && !extend.endsWith(userJson.optString("enforcementlogin"))){
				BACException.throwInstance("ǿ�Ƶ�¼ʧ�ܣ�ǿ���벻ƥ��");
			}
			if(!client_enforcementlogin && !userJson.optString("enforcementlogin").equals("")){
				BACException.throwInstance("�˺ŵ����У�����ͷ���ϵ");
			}
			if(userJson.optInt("enable")==0) {
				BACException.throwInstance("�ʺű�����");
			}
			boolean isNewUser = userJson.getString("logintime")!=null;
			//�û����������û�����
			boolean online = userJson.optInt("onlinestate")==1;
			if(online){
				if(userJson.optInt("serverid")!=0){
					dbHelper.closeConnection();
					STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_BEOFFLINE);
					sender.dos.writeUTF(userJson.optString("sessionid"));
					ServerBAC.getInstance().sendReqToOne(sender, userJson.optInt("serverid"));
				}
				delUser(userJson.optString("sessionid"));
			}
			
			int userid = userJson.optInt("id");
			String dateStr = MyTools.getDateStr();
			String timeStr = MyTools.getTimeStr();
			//����SESSION
			String session = createSession(userid);
			//�����û���¼��־
			String[] area = {"", ""};//IPAddressBAC.getInstance().getAreaByIP(dbHelper, ip);
			createLoginLog(userid, username, ip, logdata, dateStr, timeStr, session, area, channel, platform);
			//�����û���¼��Ϣ
			userSqlStr.add("ip", ip);
			userSqlStr.addDateTime("logintime", timeStr);
			userSqlStr.add("onlinestate", 1);
			userSqlStr.add("sessionid", session);
			userSqlStr.add("loginport", loginport);
			userSqlStr.add("serverid", 0);
			userSqlStr.add("playerid", 0);
			dbHelper.update(tab_user, userSqlStr, "username='"+username+"' and platform='"+platform+"'");
			//�Ƿ��Ѽ���
			boolean activate = ActivateCodeBAC.getInstance().checkActivate(channel, username);
			//�����¼�û����ڴ�
			dbHelper.closeConnection();
			saveUser(session, userid, channel, username);
			//���û������Ƿ��ǵͶ˻���
			boolean isPoor = PoorPhoneBAC.getInstance().isPoor(dbHelper, logdata.optString(1), logdata.optString(2));
			//������Ϣ
			JSONArray returnJsonarr = new JSONArray();
			returnJsonarr.add(session);
			returnJsonarr.add(activate?1:0);
			returnJsonarr.add(isNewUser?1:0); //�Ƿ��״ε�½�û�(������)
			returnJsonarr.add(isPoor?1:0);//�Ƿ�Ͷ˻���
			returnJsonarr.add(returnobj.opt("channeldata"));//��������
			returnJsonarr.add(lowMemory); //�����ڴ淧ֵ����MB��λ
			return new ReturnValue(true, returnJsonarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	private static char[] session_map = {'Z', 'A', 'X', 'B', 'F', 'T', 'D', 'Q', 'P', 'L'};
	
	/**
	 * ����SESSION
	 */
	private String createSession(int userid){
		StringBuffer sb1 = new StringBuffer();
		sb1.append(userid+102762728);
		sb1.append(MyTools.getRandom(10000, 99999));
		char[] array = sb1.toString().toCharArray();
		StringBuffer sb2 = new StringBuffer();
		for(int i = 0; i < array.length; i++){
			sb2.append(session_map[Integer.valueOf(array[i]+"")]);
		}
		return sb2.toString();
	}
	
	/**
	 * ע��
	 */
	public ReturnValue logout(int userid, String reason){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			logout(dbHelper, userid, reason);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ע��
	 */
	public void logout(DBHelper dbHelper, int userid, String reason) throws Exception {
		JSONObject json = dbHelper.queryJsonObj(tab_user, "playerid,serverid,sessionid,logintime", "id="+userid+" and onlinestate=1");
		if(json != null) {
			if(json.optInt("serverid")!=0 && json.optInt("playerid")!=0){
				STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_LOGOUT);
				sender.dos.writeInt(json.optInt("playerid"));
				sender.dos.writeUTF(reason);
				ServerBAC.getInstance().sendReqToOne(sender, json.optInt("serverid"));
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("onlinestate", 0);
			sqlStr.add("sessionid", 0);
			sqlStr.add("serverid", 0);
			sqlStr.add("playerid", 0);
			dbHelper.update(tab_user, sqlStr, "id="+userid);
			dbHelper.closeConnection();
			delUser(json.optString("sessionid"));
		}
	}
	
	/**
	 * ��USER
	 */
	public User saveUser(String sessionid, int userid, String channel, String username){
		synchronized (session_usermap) {
			if(sessionid != null && !sessionid.equals("")){
				if(session_usermap.size() >= 98000){
					session_usermap.clear();
				}
				User user = new User();
				user.sessionid = sessionid;
				user.uid = userid;
				user.channel = channel;
				user.username = username;
				session_usermap.put(sessionid, user);
				MemcachedUtil.set(sessionid, user.converToStr());
				return user;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * ��ȡUSER
	 */
	public User loadUser(String sessionid){
		synchronized (session_usermap) {
			User user = session_usermap.get(sessionid);
			if(user == null){
				if(sessionid != null && !sessionid.equals("")){
					String str = (String)MemcachedUtil.get(sessionid);
					if(str != null && !str.equals("")){
						user = User.converToUser(str);
					}
					if(user != null){
						session_usermap.put(sessionid, user);
					}
				}
			}
			return user;
		}
	}
	
	/**
	 * �Ƴ�USER
	 */
	public void delUser(String sessionid){
		synchronized (session_usermap) {
			if(sessionid != null && !sessionid.equals("")){
				session_usermap.remove(sessionid);
				MemcachedUtil.delete(sessionid);
			}
		}
	}
	
	/**
	 * ��ȡҳ���¼��Ϣ
	 */
	public JSONArray webGetLogdata(String ip){
		JSONArray logdata = new JSONArray();
		logdata.add("WEB");//�ֻ�����
		logdata.add("WEB");//�ֻ�����
		logdata.add(0);//�ֻ�֧�ֵ�sdk����
		logdata.add("WEB");//�ֻ�ϵͳ�汾��
		logdata.add("WEB");//�ֱ���
		logdata.add("WEB");//������
		logdata.add(0);//��������
		logdata.add(0);//WIFI
		logdata.add(ip);//IMEI
		logdata.add(ip);//MAC
		logdata.add(0);//ʣ���ڴ�
		logdata.add(0);//���ڴ�
		logdata.add(1);//ϵͳ����
		return logdata;
	}
	
	/**
	 * WEB��ȡ�û������ж�����
	 */
	public ReturnValue webGetJudgmentData(String username, String imei){
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Backup());
		try {
			int loginCount = dbHelper.queryCount(LogTbName.TAB_USER_LOGIN_LOG(), "username='"+username+"' and platform='001' and imei='"+imei+"'");
			ResultSet chargeRs = dbHelper.query("tab_charge_order", "sum(price) as total", "username='"+username+"' and platform='001' and result=1");
			chargeRs.next();
			JSONArray returnarr = new JSONArray();
			returnarr.add(loginCount);//��¼����
			returnarr.add(chargeRs.getInt("total"));//�ܳ�ֵ���
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ����û��Ƿ����
	 */
	public ReturnValue webUserExist(String platform, String username) {
		return new ReturnValue(getUserId(platform, username)!=0);
	}
	
	/**
	 * ������½��־
	 */
	public void createLoginLog(int userid, String username, String ip, JSONArray logdata, String dateStr, String timeStr, String sessionid, String[] area, String channel, String platform) throws Exception {
		SqlString logSqlStr = new SqlString();
		logSqlStr.add("userid", userid);//�û�ID
		logSqlStr.add("username", username);//�ʺ�
		logSqlStr.add("phonevendor", logdata.optString(0));//�ֻ�����
		logSqlStr.add("phonemodel", logdata.optString(1));//�ֻ�����
		logSqlStr.add("phonesdklv", logdata.optInt(2));//�ֻ�֧�ֵ�sdk����
		logSqlStr.add("phonesdkver", logdata.optString(3));//�ֻ�ϵͳ�汾��
		logSqlStr.add("resolution", logdata.optString(4));//�ֱ���
		logSqlStr.add("netname", logdata.optString(5));//������
		logSqlStr.add("nettype", logdata.optInt(6));//��������
		logSqlStr.add("wifi", logdata.optInt(7));//WIFI
		logSqlStr.add("ip", ip);//IP
		logSqlStr.add("imei", logdata.optString(8));//IMEI
		logSqlStr.add("mac", logdata.optString(9));//MAC
		logSqlStr.add("phonefreemem", logdata.optInt(10));//ʣ���ڴ�
		logSqlStr.add("phonetotalmem", logdata.optInt(11));//���ڴ�
		logSqlStr.add("sessionid", sessionid);
		logSqlStr.addDateTime("logintime", timeStr);
		logSqlStr.add("country", area[0]);
		logSqlStr.add("city", area[1]);
		logSqlStr.add("channel", channel);
		logSqlStr.add("platform", platform);
		logSqlStr.add("systemtype", logdata.optInt(12));//ϵͳ����
		DBHelper.logInsert(LogTbName.TAB_USER_LOGIN_LOG(), logSqlStr);
	}
	
	/**
	 * ��ȡ�û�ID
	 */
	public int getUserId(String platform, String username) {
		int userid = 0;
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet userRs = dbHelper.query(tab_user, "id", "username='"+username+"' and platform='"+platform+"'");
			if(userRs.next()){
				userid = userRs.getInt("id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
		return userid;
	}
	
	//--------------��̬��--------------
	
	private static UserBAC instance = new UserBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static UserBAC getInstance(){
		return instance;
	}
}
