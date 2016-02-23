package com.moonic.bac;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.concurrent.ScheduledExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.jspsmart.upload.SmartUpload;
import com.moonic.mgr.DBPoolMgr;
import com.moonic.servlet.STSServlet;
import com.moonic.timertask.ClearDataTT;
import com.moonic.timertask.GetServerStateTT;
import com.moonic.timertask.RefreshRankingTT;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.FileUtil;
import com.moonic.util.MassNetSender;
import com.moonic.util.MyLog;
import com.moonic.util.MyTools;
import com.moonic.util.NetResult;
import com.moonic.util.NetSender;
import com.moonic.util.Out;
import com.moonic.util.STSNetSender;

import conf.Conf;

/**
 * ��Ϸ������
 * @author John
 */
public class ServerBAC extends BaseActCtrl{
	public static final String tab_server = "tab_server";
	public static final String tab_channel_server = "tab_channel_server";
	public static final String tab_user_server= "tab_user_server";
	public static final String tab_notice = "tab_notice";
	public static final String tab_server_exc_mail_addr = "tab_server_exc_mail_addr";
	 
	public ServerBAC() {
		super.setTbName(tab_server);
		setDataBase(ServerConfig.getDataBase());
	}
	
	/**
	 * ����������
	 */
	public ReturnValue openGameServer(int serverid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("state", 0);
			String where = null;
			if(serverid > 0){
				where = "id="+serverid;
			}
			dbHelper.update(tab_server, sqlStr, where);
			DBPoolMgr.getInstance().addClearTablePoolTask(tab_server, null);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ά��������
	 */
	public ReturnValue maintain(int serverid, String note, String prompt, String shownote, int type){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("state", 1);
			sqlStr.add("note", note);
			sqlStr.add("shownote", shownote);
			String where = null;
			if(serverid > 0){
				where = "id="+serverid;
			}
			dbHelper.update(tab_server, sqlStr, where);
			dbHelper.closeConnection();
			DBPoolMgr.getInstance().addClearTablePoolTask(tab_server, null);
			STSNetSender sender = new STSNetSender(STSServlet.G_CLEAR_ALLPLAYER);
			sender.dos.writeUTF(prompt);
			sender.dos.writeByte((byte)type);
			ReturnValue rv = ServerBAC.getInstance().sendReqToAllOrOneBySid(ServerBAC.STS_GAME_SERVER, sender, serverid);
			return rv;
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��������
	 */
	public ReturnValue createNotice(String title, String content, String overtime, int loopshow, int[] sidarr){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			for(int i = 0; sidarr!=null && i<sidarr.length; i++){
				SqlString sqlStr = new SqlString();
				sqlStr.add("serverid", sidarr[i]);
				sqlStr.add("title", title);
				sqlStr.add("content", content);
				sqlStr.addDateTime("createtime", MyTools.getTimeStr());
				sqlStr.addDateTime("overtime", overtime);
				sqlStr.add("loopshow", loopshow);
				boolean exist = dbHelper.queryExist(tab_notice, "serverid="+sidarr[i]);
				if(exist){
					dbHelper.update(tab_notice, sqlStr, "serverid="+sidarr[i]);			
				} else {
					dbHelper.insert(tab_notice, sqlStr);
				}
			}
			DBPoolMgr.getInstance().addClearTablePoolTask(tab_notice, null);
			return new ReturnValue(true);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����û���
	 */
	public ReturnValue openMainServerLogin(){
		try {
			ConfigBAC.getInstance().setValue("openlogin", "1", null);
			return new ReturnValue(true);
		} catch (Exception e) {
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �ر��û���
	 */
	public ReturnValue closeMainServerLogin(final String note){
		try {
			String[] names = new String[]{"openlogin"};
			String[] values = new String[]{"0"};
			if(note!=null && !note.equals("")){
				names = Tools.addToStrArr(names, "closeloginnote");
				values = Tools.addToStrArr(values, note);
			}
			ConfigBAC.getInstance().setValue(names, values, null);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����׼��
	 */
	public ReturnValue openReady(int serverid){
		try {
			STSNetSender sender = new STSNetSender(STSServlet.G_SERVER_OPENREADY);
			NetResult nr = ServerBAC.getInstance().sendReqToOne(ServerBAC.STS_GAME_SERVER, sender, serverid);
			return nr.rv;
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����������
	 */
	public ReturnValue createRobot(int serverid){
		try {
			if(createRobotThread != null){
				BACException.throwInstance("���ڴ��������Ժ�");
			}
			createRobotThread = new CreateRobotThread(serverid);
			(new Thread(createRobotThread)).start();
			return new ReturnValue(true, "�����������߳�");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	private CreateRobotThread createRobotThread; 
	
	class CreateRobotThread implements Runnable {
		private int serverid;
		public CreateRobotThread(int serverid){
			this.serverid = serverid;
		}
		public void run() {
			try {
				ReturnValue rv = null;
				Out.println("SID="+serverid+"��ʼ����������");
				do {
					Out.println("SID="+serverid+"�ȴ������������ʺ�...");
					DBPsRs userServerRs = DBPool.getInst().pQueryS(tab_user_server);
					userServerRs.next();
					STSNetSender sender1 = new STSNetSender(STSServlet.M_JJC_REGISTER_PC);
					NetResult nr1 = ServerBAC.getInstance().sendReqToOne(ServerBAC.STS_USER_SERVER, sender1, userServerRs.getInt("id"));
					rv = nr1.rv;
					if(!rv.success){
						Out.println("SID="+serverid+" rv.info:"+rv.info);
						if(rv.info != null && rv.info.contains("[doing]")){
							Thread.sleep(5000);
						} else {
							BACException.throwInstance("���ɹ����з����쳣 "+rv.info);
						}
					}
				} while(!rv.success);
				Out.println("SID="+serverid+"�����������ʺ���� ���ɽ����"+rv.info);
				do {
					Out.println("SID="+serverid+"�ȴ����������˽�ɫ...");
					STSNetSender sender2 = new STSNetSender(STSServlet.G_JJC_CREATE_PC);
					NetResult nr2 = ServerBAC.getInstance().sendReqToOne(ServerBAC.STS_GAME_SERVER, sender2, serverid);
					rv = nr2.rv;
					if(!rv.success){
						Out.println("SID="+serverid+" rv.info:"+rv.info);
						if(rv.info != null && rv.info.contains("[doing]")){
							Thread.sleep(5000);
						} else {
							BACException.throwInstance("SID="+serverid+"���ɹ����з����쳣 "+rv.info);
						}
					}
				} while(!rv.success);
				Out.println("SID="+serverid+"���������˽�ɫ���");
				createRobotThread = null;
				Out.println("SID="+serverid+"�������������");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ��ȡ�����������б�
	 */
	public DBPsRs getChannelServerList(String channel) {
		try {
			return DBPool.getInst().pQueryS(tab_channel_server, "channel='"+channel+"'");		
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ����SID�Ƿ�Ϊ0�жϷ�ָ�����������з�
	 */
	public ReturnValue sendReqToAllOrOneBySid(byte type, NetSender sender, int serverid){
		try {
			String info = null;
			if(serverid == 0){
				info = converNrsToString(sendReq(type, null, sender, "sts.do"));	
			} else {
				info = converNrsToString(sendReq(type, "id="+serverid, sender, "sts.do"));
			}
			return new ReturnValue(true, info);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ��ָ��������������
	 */
	public NetResult sendReqToOne(byte type, NetSender sender, int serverid) {
		return sendReqToOne(type, sender, serverid, "sts.do");
	}
	
	/**
	 * ��ָ��������������
	 */
	public NetResult sendReqToOne(byte type, NetSender sender, int serverid, String doStr) {
		NetResult nr = null;
		try {
			nr = sendReq(type, "id="+serverid, sender, doStr)[0];
		} catch(Exception e){
			e.printStackTrace();
			nr = new NetResult();
			nr.rv = new ReturnValue(false, e.toString());
		}
		return nr;
	}
	
	/**
	 * ����������������
	 */
	public ReturnValue sendReqToSome(byte type, NetSender sender, int[] sids){
		try {
			if(sids==null || sids.length==0){
				BACException.throwInstance("ѡ��ķ�����Ϊ��");
			}
			StringBuffer whereSb = new StringBuffer();
			for(int i = 0; i < sids.length; i++){
				if(whereSb.length() > 0){
					whereSb.append(" or ");
				}
				whereSb.append("id="+sids[i]);
			}
			String info = converNrsToString(sendReq(type, whereSb.toString(), sender));
			return new ReturnValue(true, info);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �����з�����������
	 */
	public ReturnValue sendReqToAll(byte type, NetSender sender){
		try {
			String info = converNrsToString(sendReq(type, null, sender));
			return new ReturnValue(true, info);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	public static final byte STS_USER_SERVER = 0;
	public static final byte STS_GAME_SERVER = 1;
	
	/**
	 * ��������
	 */
	public NetResult[] sendReq(byte type, String where, NetSender sender) throws Exception {
		return sendReq(type, where, sender, "sts.do");
	}
	
	/**
	 * ��������(�ײ��Ѷ�����Ϸ������������й��ˣ���Ϸ��δ����ʱ������������)
	 */
	public NetResult[] sendReq(byte type, String where, NetSender sender, String doStr) throws Exception {
		String tabname = null;
		if(type == STS_USER_SERVER){
			tabname = tab_user_server;
		} else 
		if(type == STS_GAME_SERVER){
			tabname = tab_server;
			if(where != null){
				where = "usestate=1 and ("+where+")";
			} else {
				where = "usestate=1";
			}
		}
		DBPsRs sRs = DBPool.getInst().pQueryS(tabname, where);
		if(sRs.count() <= 0){
			BACException.throwInstance("ָ��Ŀ������������� where:"+where);
		}
		MassNetSender mns = new MassNetSender();
		while(sRs.next()){
			mns.addURL(type, sRs.getInt("id"), sRs.getString("name"), "http://"+sRs.getString("http") + doStr);
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
	 * ת��NRSΪ��ʾ�ַ���
	 */
	public String converNrsToPromptStr(NetResult[] nrs){
		int succam = 0;
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < nrs.length; i++){
			if(nrs[i].rv.success){
				succam++;
			} else {
				sb.append(nrs[i].name+" �쳣��"+nrs[i].rv.info+"\\r\\n");
			}
		}
		String str = "��"+succam+"�����������³ɹ�\\r\\n"+sb.toString();
		return str;
	}
	
	/**
	 * �����־
	 */
	public ReturnValue clearLog(String table){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			boolean succ = dbHelper.execute("truncate table "+table);
			return new ReturnValue(succ);		
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���ָ��������������
	 */
	public ReturnValue clearServerData(int serverid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(serverid == 0) {
				BACException.throwInstance("��ѡ��Ҫ������ݵ���Ϸ������");
			} 
			String where = "serverid="+serverid;
			String str = deletePlayerDataByWhere(dbHelper, where);
			dbHelper.delete("tab_cb_city_stor", "serverid="+serverid);//�����ս������Ϣ�������ݺ��������������
			STSNetSender sender = new STSNetSender(STSServlet.G_CLEAR_SERVER_DATA);
			ServerBAC.getInstance().sendReqToOne(ServerBAC.STS_GAME_SERVER, sender, serverid);
			return new ReturnValue(true, str);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ɾ��ָ����ɫ����������
	 */
	public ReturnValue deletePlayerData(String arr){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			int[] pids = Tools.splitStrToIntArr(arr, ",");
			if(pids.length == 0){
				BACException.throwInstance("Ҫɾ�������Ϊ��");
			}
			StringBuffer whereSb = new StringBuffer();
			for(int i = 0; i < pids.length; i++){
				if(whereSb.length() > 0){
					whereSb.append(" or ");
				}
				whereSb.append("id="+pids[i]);
			}
			String returnStr = deletePlayerDataByWhere(dbHelper, whereSb.toString());
			return new ReturnValue(true, returnStr);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	public static final String[] exclude_talbe = {
		"TAB_USER", "TAB_COINRETURN", "TAB_CHARGE_ORDER"
	};
	public static final String[][] delete_other = {
		{"TAB_FRIEND", "FRIENDID"}
	};
	
	/**
	 * ɾ�����������Ľ�ɫ����������
	 */
	public String deletePlayerDataByWhere(DBHelper dbHelper, String where) throws Exception {
		ResultSet plaRs = PlayerBAC.getInstance().getDataRs(dbHelper, "id", where);
		plaRs.last();
		StringBuffer sb = new StringBuffer();
		sb.append("ɾ���������"+plaRs.getRow()+"\r\n");
		dbHelper.closeRs(plaRs);
		long t2 = System.currentTimeMillis();
		ResultSet listRs = dbHelper.executeQuery("select * from user_tables");
		sb.append("\r\n������\r\n");
		while(listRs.next()){
			String tabname = listRs.getString("table_name");
			if(Tools.contain(exclude_talbe, tabname)){
				continue;
			}
			ResultSet tabRs = dbHelper.executeQuery("select * from " + tabname);
			tabRs.next();
			ResultSetMetaData rsmd = tabRs.getMetaData();
			int colCount = rsmd.getColumnCount();
			for(int i = 1; i <= colCount; i++){
				String colName = rsmd.getColumnName(i);
				if(colName.toLowerCase().equals("playerid")){
					boolean succ = dbHelper.execute("delete from "+tabname+" where playerid in(select id as playerid from tab_player where "+where+")");
					sb.append(tabname+"("+succ+")\r\n");
					break;
				}
			}
			dbHelper.closeRs(tabRs);
		}
		dbHelper.closeRs(listRs);
		for(int i = 0; delete_other != null && i < delete_other.length; i++){
			boolean succ = dbHelper.execute("delete from "+delete_other[i][0]+" where "+delete_other[i][1]+" in(select id as "+delete_other[i][1]+" from tab_player where "+where+")");
			sb.append(delete_other[i][0]+"("+succ+")\r\n");
		}
		boolean succ = dbHelper.execute("delete from tab_player where "+where);
		sb.append("TAB_PLAYER("+succ+")\r\n\r\n");
		long t3 = System.currentTimeMillis();
		sb.append("ɾ����ʱ��" + (t3-t2));
		MyLog delplalog= new MyLog(MyLog.NAME_CUSTOM, "log_delpla", "DEL_PLA", true, false, true, MyTools.formatTime("yyyy-MM-dd-HH-mm-ss"));
		delplalog.d(sb.toString());
		delplalog.d("----------------------------");
		delplalog.save();
		return sb.toString();
	}
	
	public static final String[][] transferTab = {
		{"TAB_PLAYER", "SERVERID"}, 
		{"TAB_EQUIP_STOR", "SERVERID"},
		{"TAB_PLA_PHOTO_STOR", "SERVERID"},
		{"TAB_PLA_CITY", "SERVERID"},
		{"TAB_CHARGE_ORDER", "SERVERID"},
		{"TAB_CHARGE_SEND", "SERVERID"},
		{"TAB_PLATFORM_GIFT_CODE", "SERVERID"},
		{"TAB_WELFARE_USER", "SERVERID"},
		{"TAB_PET_STOR", "SERVERID"},
		{"TAB_FACTION_STOR", "SERVERID"},
		{"TAB_AUCTION_ITEM_STOR", "SERVERID"},
		{"TAB_AUCTION_BID_STOR", "SERVERID"},
		{"TAB_AUCTION_STOW_STOR", "SERVERID"},
		{"TAB_PLA_JJC", "SERVERID"},
		{"TAB_VOICEROOM", "SERVERID"},
		{"TAB_EXTENSION_AGENT", "SERVERID"},
		{"TAB_EXTENSION_AGENT", "INVITEMESID"},
		{"TAB_PLA_JJCRANKING", "SERVERID"},
		{"TAB_PLA_MALL", "SERVERID"}
	};
	
	/**
	 * �Ϸ�
	 */
	public ReturnValue mergerServer(String srcServeridStr, int tgrServerid){
		DBHelper dbHelper = new DBHelper();
		try {
			//BACException.throwInstance("�˹�����δ����");
			int[] srcServerids = Tools.splitStrToIntArr(srcServeridStr, ",");
			for(int i = 0; i < srcServerids.length; i++){
				if(srcServerids[i] == tgrServerid){
					BACException.throwInstance("Դ��������Ŀ���������ͬ���޷��Ϸ�");
				}
			}
			dbHelper.openConnection();
			System.out.println("��ʼ�Ϸ�...");
			//���м���ķ�����ɫ�˳�����
			String del_where = " and lv<=15 and rechargermb=0 and logintime<"+MyTools.getDateSQL(MyTools.getCurrentDateLong()-MyTools.long_day*15);
			System.out.println("��ʼǿ���˳�����...");
			STSNetSender exitfacSender = new STSNetSender(STSServlet.G_MERGERSERVER_EXITFAC);
			ServerBAC.getInstance().sendReqToSome(STS_GAME_SERVER, exitfacSender, srcServerids);
			ServerBAC.getInstance().sendReqToOne(STS_GAME_SERVER, exitfacSender, tgrServerid);
			System.out.println("����ǿ���˳�����");
			//ɾ��������ɫ
			System.out.println("��ʼɾ��������ɫ...");
			deletePlayerDataByWhere(dbHelper, "serverid="+tgrServerid+del_where);
			for(int i = 0; i < srcServerids.length; i++){
				deletePlayerDataByWhere(dbHelper, "serverid="+srcServerids[i]+del_where);
			}
			System.out.println("���ɾ��������ɫ");
			//�����ɫ����
			System.out.println("��ʼ��ɫ��������...");
			ResultSet planameRs = dbHelper.executeQuery("select * from (select name,count(*) as amount from tab_player where ("+MyTools.converWhere("or", "serverid", "=", srcServerids)+" or serverid="+tgrServerid+") group by name) where amount>=2");
			while(planameRs.next()){
				for(int i = 0; i < srcServerids.length; i++){
					JSONArray pidarr = new JSONArray();
					ResultSet mailplaRs = dbHelper.query("tab_player", "id", "serverid="+srcServerids[i]+" and name='"+planameRs.getString("name")+"'");
					while(mailplaRs.next()){
						pidarr.add(mailplaRs.getInt("id"));
					}
					dbHelper.closeRs(mailplaRs);
					STSNetSender mailSender = new STSNetSender(STSServlet.G_SEND_SYS_MAIL2);
					mailSender.dos.writeUTF(pidarr.toString());
					mailSender.dos.writeUTF("��ɫ������֪ͨ");
					mailSender.dos.writeUTF("���Ľ�ɫ���ںϷ���ϵͳ�Զ�������������ǰ�����ǽ�����и���");
					mailSender.dos.writeUTF("");
					ServerBAC.getInstance().sendReqToOne(STS_GAME_SERVER, mailSender, srcServerids[i]);
				}
				dbHelper.execute("update tab_player set name=name||'#'||vsid where ("+MyTools.converWhere("or", "serverid", "=", srcServerids)+") and name='"+planameRs.getString("name")+"'");
			}
			System.out.println("��ɽ�ɫ��������");
			//�����������
			ResultSet facnameRs = dbHelper.executeQuery("select * from (select name,count(*) as amount from tab_faction_stor where ("+MyTools.converWhere("or", "serverid", "=", srcServerids)+" or serverid="+tgrServerid+") group by name) where amount>=2");
			while(facnameRs.next()){
				for(int i = 0; i < srcServerids.length; i++){
					JSONArray pidarr = new JSONArray();
					ResultSet mailplaRs = dbHelper.query("tab_faction_stor", "playerid", "serverid="+srcServerids[i]+" and name='"+planameRs.getString("name")+"'");
					while(mailplaRs.next()){
						pidarr.add(mailplaRs.getInt("playerid"));
					}
					dbHelper.closeRs(mailplaRs);
					STSNetSender mailSender = new STSNetSender(STSServlet.G_SEND_SYS_MAIL2);
					mailSender.dos.writeUTF(pidarr.toString());
					mailSender.dos.writeUTF("����������֪ͨ");
					mailSender.dos.writeUTF("���ļ������ںϷ���ϵͳ�Զ�������������ǰ�����������и���");
					mailSender.dos.writeUTF("");
					ServerBAC.getInstance().sendReqToOne(STS_GAME_SERVER, mailSender, srcServerids[i]);
				}
				dbHelper.execute("update tab_faction_stor set name=name||'#'||serverid where ("+MyTools.converWhere("or", "serverid", "=", srcServerids)+") and name='"+facnameRs.getString("name")+"'");
			}
			System.out.println("��ɼ�����������");
			//�ϲ�������
			System.out.println("��ʼ�����ɫ����������...");
			for(int s = 0; s < srcServerids.length; s++){
				for(int i = 0; i < transferTab.length; i++){
					SqlString sqlStr = new SqlString();
					sqlStr.add(transferTab[i][1], tgrServerid);
					dbHelper.update(transferTab[i][0], sqlStr, transferTab[i][1]+"="+srcServerids[s]);
				}
			}
			System.out.println("��ɴ����ɫ����������");
			//�������ϲ���ʹ��״̬
			System.out.println("��ʼ�������ϲ���ʹ��״̬...");
			DBPaRs serverRs = DBPool.getInst().pQueryA("tab_server", "id="+tgrServerid);
			SqlString sqlStr = new SqlString();
			sqlStr.add("usestate", 0);
			sqlStr.add("usenote", "����"+serverRs.getString("name"));
			dbHelper.update("tab_server", sqlStr, MyTools.converWhere("or", "id", "=", srcServerids));
			DBPoolMgr.getInstance().addClearTablePoolTask(tab_server, null);
			System.out.println("��ɵ������ϲ���ʹ��״̬");
			System.out.println("��ɺϷ�");
			return new ReturnValue(true, "ִ�гɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����������
	 */
	public ReturnValue exportPlayerData(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet listRs = dbHelper.executeQuery("select * from user_tables");
			StringBuffer sb = new StringBuffer();
			while(listRs.next()){
				String tabname = listRs.getString("table_name");
				if(tabname.equals("tab_user")){
					continue;
				}
				File cehckfile = new File(ServerConfig.getWebInfPath() + "res/tab_data/"+tabname.toLowerCase()+".txt");
				if(!cehckfile.exists()){
					continue;
				}
				boolean isPlaData = false;
				if(tabname.toLowerCase().equals("tab_player")){
					isPlaData = true;
				}
				if(!isPlaData){
					ResultSet tabRs = dbHelper.executeQuery("select * from " + tabname);
					ResultSetMetaData rsmd1 = tabRs.getMetaData();
					int colCount1 = rsmd1.getColumnCount();
					for(int i = 1; i <= colCount1; i++){
						String colName = rsmd1.getColumnName(i);
						if(colName.toLowerCase().equals("playerid")){
							isPlaData = true;
							break;
						}
					}
					for(int i = 1; i <= colCount1; i++){
						if(rsmd1.getColumnType(i) == Types.BLOB){
							isPlaData = false;
							break;
						}
					}
					dbHelper.closeRs(tabRs);
				}
				if(!isPlaData){
					continue;
				}
				String keyColumn = "playerid";
				if(tabname.toLowerCase().equals("tab_player")){
					keyColumn = "id";
				}
				ResultSet dataRs = dbHelper.executeQuery("select * from " + tabname + " where "+keyColumn+"=" + playerid);
				ResultSetMetaData rsmd2 = dataRs.getMetaData();
				int colCount2 = rsmd2.getColumnCount();
				while(dataRs.next()){
					SqlString sqlStr = new SqlString();
					for(int i = 1; i <= colCount2; i++){
						String colName = rsmd2.getColumnName(i);
						String colValue = dataRs.getString(colName);
						if(colValue != null){
							int datatype = rsmd2.getColumnType(i);
							if(datatype == Types.NUMERIC){
								sqlStr.add(colName, Tools.str2double(colValue));
							} else 
							if(datatype == Types.NVARCHAR || datatype == Types.VARCHAR){
								sqlStr.add(colName, colValue);		
							} else 
							if(datatype == Types.DATE || datatype == Types.TIMESTAMP){
								if(rsmd2.getColumnDisplaySize(i) == 11){
									sqlStr.addDateTimeMS(colName, colValue);
								} else {
									sqlStr.addDateTime(colName, colValue);
								}
							} else 
							{
								System.out.println("δ��������ͣ�"+rsmd2.getColumnTypeName(i));
							}
						}
					}
					String sql = "insert into "+tabname+"("+sqlStr.colString()+")"+" values("+sqlStr.valueString()+");\r\n";
					sb.append(sql);
				}
				dbHelper.closeRs(dataRs);
			}
			FileUtil fileutil = new FileUtil();
			fileutil.writeNewToTxt(Conf.logRoot+ServerConfig.getDataBase().getUsername()+"_"+playerid+".txt", sb.toString());
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	public ReturnValue save(PageContext pageContext)
	{
		SmartUpload smartUpload = new SmartUpload();
		smartUpload.setEncode("UTF-8");
		try {
			smartUpload.initialize(pageContext);
			smartUpload.upload();
			com.jspsmart.upload.Request request = smartUpload.getRequest();				
			
			int id=Tools.str2int(request.getParameter("id"));
			int serverid=Tools.str2int(request.getParameter("serverid"));
			String name=request.getParameter("name");
			
			
			String http=request.getParameter("http");
			String tcp=request.getParameter("tcp");
			String opentime=request.getParameter("opentime");
			int reslv = Tools.str2int(request.getParameter("reslv"));
			
			int maxplayer = Tools.str2int(request.getParameter("maxplayer"));
			if(maxplayer==0)maxplayer=2000;
			
			int state=Tools.str2int(request.getParameter("state"));
			int tip=Tools.str2int(request.getParameter("tip"));
			String note=request.getParameter("note");
			
			int usestate = Tools.str2int(request.getParameter("usestate"));
			String usenote = request.getParameter("usenote");
			String shownote = request.getParameter("shownote");
			
			FormXML formXML = new FormXML();
			formXML.add("name",name);			
			formXML.add("state",state);
			formXML.add("tip",tip);
			formXML.add("note",note);
			formXML.add("http",http);
			formXML.add("tcp",tcp);
			formXML.add("maxplayer",maxplayer);
			formXML.addDateTime("opentime",opentime);
			formXML.add("reslv", reslv);
			formXML.add("usestate", usestate);
			formXML.add("usenote", usenote);
			formXML.add("shownote", shownote);
			
			if(id>0)
			{
				int count = getCount("id="+serverid+" and id <>"+id);
				if(count>0)
				{
					return new ReturnValue(false,"������id�ظ�");
				}
				count = getCount("name='"+name+"' and id <>"+id);
				if(count>0)
				{
					return new ReturnValue(false,"���������ظ�");
				}				
			}
			else
			{
				int count = getCount("id="+serverid);
				if(count>0)
				{
					return new ReturnValue(false,"������id�ظ�");
				}
				count = getCount("name='"+name+"'");
				if(count>0)
				{
					return new ReturnValue(false,"���������ظ�");
				}				
			}
			if(id>0)  //�޸�
			{	
				formXML.add("id",serverid); //�Լ�����id
				formXML.setAction(FormXML.ACTION_UPDATE);
				formXML.setWhereClause("id=" + id);
				ReturnValue rv = save(formXML);	
				if(rv.success)
				{
					return new ReturnValue(true,"�޸ĳɹ�");
				}else
				{
				  return new ReturnValue(false,"�޸�ʧ��");
				}					
			}else  //���
			{
				formXML.add("id",serverid); //�Լ�����id
				formXML.setAction(FormXML.ACTION_INSERT);
				ReturnValue rv =save(formXML);
				if(rv.success)
				{
				  return new ReturnValue(true,"����ɹ�");
				}else
				{
				  return new ReturnValue(false,"����ʧ��");
				}			
			}
		} 
		catch (Exception e) 
		{			
			e.printStackTrace();
			return new ReturnValue(false,e.toString());
		} 		
	}	
	
	public void downloadDataZip(PageContext pageContext)
	{
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
		String folder = ServerConfig.getAppRootPath()+"WEB-INF/res/";
		File folderPath = new File(folder);
		File[] subFolders = folderPath.listFiles();
		try {			
			for(int i=0;i<subFolders.length;i++)
			{
				//System.out.println(subFolders[i].getName());
				if(subFolders[i].getName().startsWith("tab_"))
				{
					//��ȡ��Ŀ¼�ļ�
					File[] files = subFolders[i].listFiles();
					for(int j=0;j<files.length;j++)
					{
						
						byte[] fileBytes = Tools.getBytesFromFile(files[j]);
						//System.out.println("��ȡ"+subFolders[i].getName()+"/"+files[j].getName()+"��"+fileBytes.length+"�ֽ�");
						ZipEntry zipEntry = new ZipEntry(subFolders[i].getName()+"/"+files[j].getName());
						zipEntry.setSize(fileBytes.length);
						zipOutputStream.putNextEntry(zipEntry);
						zipOutputStream.write(fileBytes);
						zipOutputStream.closeEntry();						
					}
				}
			}
			zipOutputStream.close();
			baos.close();
			
			response.reset();
			response.setContentType("application/zip");
			response.setContentLength(baos.size());
			response.setHeader("Content-disposition", new String(("attachment;filename=res.zip").getBytes(),"ISO-8859-1"));
			
			OutputStream os = response.getOutputStream();
			os.write(baos.toByteArray());
			os.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ȡ����������������������
	 */
	public JSONObject getServerPlayerAmount(){
		JSONObject returnobj = new JSONObject();
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Backup());
		try {
			dbHelper.openConnection();
			ResultSet rs1 = dbHelper.query(PlayerBAC.tab_player, "serverid,count(1) as amount", null, null, "serverid");
			while(rs1.next()){
				returnobj.put("t"+rs1.getInt("serverid"), rs1.getString("amount"));
			}
			ResultSet rs2 = dbHelper.query(PlayerBAC.tab_player, "serverid,count(1) as amount", "onlinestate=1", null, "serverid");
			while(rs2.next()){
				returnobj.put("o"+rs2.getInt("serverid"), rs2.getString("amount"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
		return returnobj;
	}
	
	public ReturnValue clearAllPlayerData(String serverName ,int serverId)
	{		
		ReturnValue rv = clearServerData(serverId);
		if(rv.success)
		{			
			return new ReturnValue(true,serverName+"("+serverId+")�������������ɹ�");
		}
		else
		{
			return new ReturnValue(false,rv.info);
		}
	}
	
	public String getNameById(int serverid){
		String name = null;
		try {
			if(serverid != 0){
				name = DBPool.getInst().pQueryA(tab_server, "id="+serverid).getString("name");		
			} else {
				name = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}
	
	//--------------�ڲ���-------------- 
	
	//--------------��̬��--------------
	
	public static ScheduledExecutorService timer;
	
	/**
	 * ��ʼ����ʱ��
	 */
	public static void initTimer(){
		timer = MyTools.createTimer(3);
		GetServerStateTT.init();
		ClearDataTT.init();
		RefreshRankingTT.init();
	}
	
	private static ServerBAC instance = new ServerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ServerBAC getInstance(){
		return instance;
	}
}
