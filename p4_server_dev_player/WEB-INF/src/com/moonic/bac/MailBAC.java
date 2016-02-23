package com.moonic.bac;

import java.sql.ResultSet;
import java.util.ArrayList;

import org.json.JSONArray;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.gamelog.GameLog;
import com.moonic.mgr.LockStor;
import com.moonic.servlet.GameServlet;
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
 * �ʼ�
 * @author John
 */
public class MailBAC {
	public static final String tab_mail_stor = "tab_mail_stor";
	public static final String tab_pla_mail = "tab_pla_mail";
	public static final String tab_mail_model = "tab_mail_model";
	
	/**
	 * ���ʼ�
	 */
	public ReturnValue sendMail(int playerid, String receiveridStr, String title, String content){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			if(title == null || title.equals("")){
				BACException.throwInstance("���ⲻ��Ϊ��");
			}
			if(title.length()>=16){
				BACException.throwInstance("����������");
			}
			if(content == null || content.equals("")){
				BACException.throwInstance("���ݲ���Ϊ��");
			}
			if(content.length()>=140){
				BACException.throwInstance("���ݹ���");
			}
			int[] receiverids = Tools.splitStrToIntArr(receiveridStr, ",");
			if(receiverids == null){
				BACException.throwInstance("û����Ч�ķ��Ͷ���");
			}
			String[] recenames = new String[receiverids.length];
			for(int i = 0; i < receiverids.length; i++){
				DBPaRs receRs = PlayerBAC.getInstance().getDataRs(receiverids[i]);
				if(receRs.getInt("serverid") != Conf.sid){
					BACException.throwInstance("���Ͷ��󲻴���");
				}
				// TODO �����ʼ����������
				recenames[i] = receRs.getString("name");
			}
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_MAIL_SEND);
			gl.addRemark("����������");
			int needmoney = 100 * receiverids.length;
			DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
			PlayerBAC.getInstance().useMoney(dbHelper, playerid, needmoney, gl);
			for(int i = 0; i < receiverids.length; i++){
				sendMail(dbHelper, playerid, plaRs.getString("name"), receiverids[i], recenames[i], title, content, null, 0);
				gl.addRemark(GameLog.formatNameID(recenames[i], receiverids[i]));
			}
			
			ResultSet plamailRs = dbHelper.query(tab_pla_mail, "linklist", "playerid="+playerid);
			boolean exist = plamailRs.next();
			JSONArray linklist = null;
			if(exist){
				linklist = new JSONArray(plamailRs.getString("linklist"));
			} else {
				linklist = new JSONArray();
				linklist.add(new JSONArray());//PID
				linklist.add(new JSONArray());//PNAME
			}
			JSONArray pidlist = linklist.optJSONArray(0);
			JSONArray pnamelist = linklist.optJSONArray(1);
			boolean update = false;
			for(int i = 0; i < receiverids.length; i++){
				if(!pidlist.contains(receiverids[i])){
					if(pidlist.length() < 6){
						pidlist.add(receiverids[i]);
						pnamelist.add(recenames[i]);
						update = true;
					} else {
						pidlist.remove(0);
						pnamelist.remove(0);
						pidlist.add(receiverids[i]);
						pnamelist.add(recenames[i]);
						update = true;
					}
				} 
			}
			if(update){
				if(exist){
					SqlString sqlStr = new SqlString();
					sqlStr.add("linklist", linklist.toString());
					dbHelper.update(tab_pla_mail, sqlStr, "playerid="+playerid);
				} else {
					SqlString sqlStr = new SqlString();
					sqlStr.add("playerid", playerid);
					sqlStr.add("linklist", linklist.toString());
					dbHelper.insert(tab_pla_mail, sqlStr);
				}	
			}
			
			gl.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡģ����Ϣ
	 */
	public void sendModelMail(DBHelper dbHelper, int[] receiverids, int num, Object[] titleReplace, Object[] contentReplace) throws Exception {
		sendModelMail(dbHelper, receiverids, num, titleReplace, contentReplace, null);
	}
	
	/**
	 * ��ȡģ����Ϣ
	 */
	public void sendModelMail(DBHelper dbHelper, int[] receiverids, int num, Object[] titleReplace, Object[] contentReplace, String customadjunct) throws Exception {
		DBPaRs modelRs = DBPool.getInst().pQueryA(tab_mail_model, "num="+num);
		String title = modelRs.getString("title");
		String content = modelRs.getString("note");
		String adjunct = null;
		if(modelRs.getString("award").equals("-1")){//-1��ʾ���ⲿ����Ľ���
			adjunct = customadjunct;
		} else 
		if(!modelRs.getString("award").equals("0")){//0��ʾû�н���
			adjunct = modelRs.getString("award");
		}
		for(int i = 0; titleReplace != null && i < titleReplace.length; i++){
			title = title.replace("{"+i+"}", titleReplace[i].toString());
		}
		for(int i = 0; contentReplace != null && i < contentReplace.length; i++){
			content = content.replace("{"+i+"}", contentReplace[i].toString());
		}
		sendSysMail(dbHelper, receiverids, title, content, adjunct, 0);
	}
	
	/**
	 * ��ϵͳ�ʼ�(����)
	 */
	public ReturnValue sendSysMail(String receiverids, String title, String content, String adjunct, int smailid) throws Exception {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			sendSysMail(dbHelper, receiverids, title, content, adjunct, smailid);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ϵͳ�ʼ�(����)
	 */
	public void sendSysMail(DBHelper dbHelper, String receiverids, String title, String content, String adjunct, int smailid) throws Exception {
		JSONArray receiveridarr = new JSONArray(receiverids);
		for(int i = 0; i < receiveridarr.length(); i++){
			sendSysMail(dbHelper, receiveridarr.optInt(i), title, content, adjunct, smailid);
		}
	}
	
	/**
	 * ��ϵͳ�ʼ�(����)
	 */
	public void sendSysMail(DBHelper dbHelper, int[] receiverids, String title, String content, String adjunct, int smailid) throws Exception {
		for(int i = 0; receiverids != null && i < receiverids.length; i++){
			sendSysMail(dbHelper, receiverids[i], title, content, adjunct, smailid);
		}
	}
	
	/**
	 * ��ϵͳ�ʼ�
	 */
	public void sendSysMail(DBHelper dbHelper, int receiverid, String title, String content, String adjunct, int smailid) throws Exception {
		sendMail(dbHelper, 0, null, receiverid, null, title, content, adjunct, smailid);//PNAMEĿǰ�����ռ�������ϵ�ˣ�����Ϊϵͳ�ʼ������գ�������Ҫ�پ��廯
	}
	
	/**
	 * ���ʼ�
	 * @param senderid ������ID
	 * @param sendername ��������
	 * @param receiverid �ռ���ID
	 * @param receivername �ռ�����
	 * @param title ����
	 * @param content ����
	 * @param adjunct ����(��ʽΪ������ʽ)
	 * @param smailid ϵͳ�ʼ���ID(�ǿ��ʼ���0)
	 */
	public void sendMail(DBHelper dbHelper, int senderid, String sendername, int receiverid, String receivername, String title, String content, String adjunct, int smailid) throws Exception {
		int mailid = insertMail(dbHelper, senderid, sendername, receiverid, receivername, title, content, adjunct, smailid);
		JSONArray pusharr = new JSONArray();
		pusharr.add(mailid);//�ʼ�ID
		pusharr.add(senderid);//���ʼ���ɫID
		pusharr.add(sendername);//���ʼ���ɫ��
		pusharr.add(title);//�ʼ�����
		pusharr.add(adjunct!=null&&!adjunct.equals("")?0:-1);//�Ƿ�����ȡ����
		PushData.getInstance().sendPlaToOne(SocketServer.ACT_MAIL_RECEIVER, pusharr.toString(), receiverid);
	}
	
	/**
	 * �����ʼ���¼
	 */
	public int insertMail(DBHelper dbHelper, int senderid, String sendername, int receiverid, String receivername, String title, String content, String adjunct, int smailid) throws Exception {
		if(smailid != 0){
			synchronized (LockStor.getLock(LockStor.SMAIL_INSERT, receiverid)) {
				ResultSet mailStorRs = dbHelper.query(tab_mail_stor, "id", "playerid="+receiverid+" and smailid="+smailid);
				if(mailStorRs.next()){
					return mailStorRs.getInt("id");
				}
				SqlString sqlStr = new SqlString();
				sqlStr.add("playerid", receiverid);
				sqlStr.add("pname", receivername);
				sqlStr.add("senderid", 0);
				sqlStr.add("sendername", sendername);
				sqlStr.add("readed", 0);
				sqlStr.add("title", title);
				sqlStr.add("content", content);
				sqlStr.add("adjunct", adjunct);
				sqlStr.add("extracted", adjunct!=null&&!adjunct.equals("")?0:-1);
				sqlStr.addDateTime("createtime", MyTools.getTimeStr());
				sqlStr.add("smailid", smailid);
				int mailid = dbHelper.insertAndGetId(tab_mail_stor, sqlStr);
				return mailid;
			}
		} else {
			SqlString sqlStr = new SqlString();
			sqlStr.add("playerid", receiverid);
			sqlStr.add("pname", receivername);
			sqlStr.add("senderid", 0);
			sqlStr.add("sendername", sendername);
			sqlStr.add("readed", 0);
			sqlStr.add("title", title);
			sqlStr.add("content", content);
			sqlStr.add("adjunct", adjunct);
			sqlStr.add("extracted", adjunct!=null&&!adjunct.equals("")?0:-1);
			sqlStr.addDateTime("createtime", MyTools.getTimeStr());
			sqlStr.add("smailid", smailid);
			int mailid = dbHelper.insertAndGetId(tab_mail_stor, sqlStr);
			return mailid;
		}
	}
	
	/**
	 * ��ȡ�ʼ�����
	 */
	public ReturnValue getMailContent(int playerid, int mailid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet mailStorRs = dbHelper.query(tab_mail_stor, "readed,content,adjunct,extracted", "id="+mailid+" and playerid="+playerid);
			if(!mailStorRs.next()){
				BACException.throwInstance("�ʼ�δ�ҵ�");
			}
			if(mailStorRs.getInt("readed")==0){
				SqlString sqlStr = new SqlString();
				sqlStr.add("readed", 1);
				dbHelper.update(tab_mail_stor, sqlStr, "id="+mailid);
			}
			String adjunct = mailStorRs.getString("adjunct");
			JSONArray contentarr = new JSONArray();
			contentarr.add(mailStorRs.getString("content"));//����
			contentarr.add(adjunct);//����
			return new ReturnValue(true, contentarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ����
	 */
	public ReturnValue extractAdjunct(int playerid, int mailid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet mailStorRs = dbHelper.query(tab_mail_stor, "title,adjunct,extracted", "id="+mailid+" and playerid="+playerid);
			if(!mailStorRs.next()){
				BACException.throwInstance("�ʼ�δ�ҵ�");
			}
			if(mailStorRs.getInt("extracted")!=0){
				BACException.throwInstance("�޸�������ȡ");
			}
			String adjunct = mailStorRs.getString("adjunct");
			if(adjunct==null){
				BACException.throwInstance("�޸�������ȡ");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("extracted", 1);
			dbHelper.update(tab_mail_stor, sqlStr, "id="+mailid);
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_MAIL_EXTRACT_ADJUNCT);
			JSONArray itemarr = AwardBAC.getInstance().getAward(dbHelper, playerid, adjunct, ItemBAC.SHORTCUT_MAIL, 0, gl);
			
			gl.addRemark("��ȡ�ʼ���"+GameLog.formatNameID(mailStorRs.getString("title"), mailid));
			gl.save();
			return new ReturnValue(true, itemarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * һ����ȡ����
	 */
	public ReturnValue shortcutExtractAdjunct(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			ResultSet mailStorRs = dbHelper.query(tab_mail_stor, "id,title,adjunct", "playerid="+playerid+" and extracted=0");
			StringBuffer sb = new StringBuffer();
			GameLog gl = GameLog.getInst(playerid, GameServlet.ACT_MAIL_SHORTCUT_EXTRACT_ADJUNCT);
			while(mailStorRs.next()){
				if(sb.length() > 0){
					sb.append("|");
				}
				sb.append(mailStorRs.getString("adjunct"));
				gl.addRemark("��ȡ�ʼ���"+GameLog.formatNameID(mailStorRs.getString("title"), mailStorRs.getInt("id")));
			}
			if(sb.length() <= 0){
				BACException.throwInstance("û�п���ȡ�ĸ���");
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("readed", 1);
			sqlStr.add("extracted", 1);
			dbHelper.update(tab_mail_stor, sqlStr, "playerid="+playerid+" and extracted=0");
			JSONArray itemarr = AwardBAC.getInstance().getAward(dbHelper, playerid, sb.toString(), ItemBAC.SHORTCUT_MAIL, 0, gl);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(sb.toString());//�������� 3,30|4,30..
			returnarr.add(itemarr);
			
			gl.save();
			return new ReturnValue(true, returnarr.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * һ��ɾ��
	 */
	public ReturnValue shortcatDel(int playerid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			dbHelper.delete(tab_mail_stor, "playerid="+playerid+" and extracted!=0 and smailid=0");
			SqlString sqlStr = new SqlString();
			sqlStr.add("readed", 2);
			dbHelper.update(tab_mail_stor, sqlStr, "playerid="+playerid+" and extracted!=0 and smailid!=0");
			
			GameLog.getInst(playerid, GameServlet.ACT_MAIL_SHORTCUT_DEL)
			.addRemark("һ��ɾ���ʼ�")
			.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ɾ���ʼ�
	 */
	public ReturnValue delMail(int playerid, int mailid){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet mailStorRs = dbHelper.query(tab_mail_stor, "extracted,smailid", "id="+mailid+" and playerid="+playerid);
			if(!mailStorRs.next()){
				BACException.throwInstance("�ʼ�δ�ҵ�");
			}
			if(mailStorRs.getInt("extracted")==0){
				BACException.throwInstance("��δ��ȡ�������޷�ɾ��");
			}
			if(mailStorRs.getInt("smailid")!=0){
				SqlString sqlStr = new SqlString();
				sqlStr.add("readed", 2);
				dbHelper.update(tab_mail_stor, sqlStr, "id="+mailid);
			} else {
				dbHelper.delete(tab_mail_stor, "id="+mailid);
			}
			
			GameLog.getInst(playerid, GameServlet.ACT_MAIL_DEL)
			.addRemark("ɾ���ʼ���"+mailid)
			.save();
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ�ʼ��б�
	 */
	public JSONArray getMailList(DBHelper dbHelper, int playerid) throws Exception {
		ResultSet mailStorRs = dbHelper.query(tab_mail_stor, null, "playerid="+playerid);
		JSONArray mailarr = new JSONArray();
		ArrayList<Integer> smailids = new ArrayList<Integer>();
		while(mailStorRs.next()){
			if(mailStorRs.getInt("readed")!=2){//����ɾ״̬
				JSONArray arr = new JSONArray();
				arr.add(mailStorRs.getInt("id"));//�ʼ�ID
				arr.add(mailStorRs.getInt("senderid"));//���ID
				arr.add(mailStorRs.getString("sendername"));//�����
				arr.add(mailStorRs.getInt("readed"));//�Ƿ��Ѷ�
				arr.add(mailStorRs.getString("title"));//�ʼ�����
				arr.add(MyTools.getTimeLong(mailStorRs.getTimestamp("createtime")));
				arr.add(mailStorRs.getInt("extracted"));//�Ƿ�����ȡ����
				mailarr.add(arr);
			}
			smailids.add(mailStorRs.getInt("smailid"));
		}
		DBPsRs smStorRs = DBPool.getInst().pQueryS(BkSysMailBAC.tab_sys_mail_send_log, "tgr=1 and stopsend=0 and endtime>="+MyTools.getTimeStr());
		DBPaRs plaRs = PlayerBAC.getInstance().getDataRs(playerid);
		while(smStorRs.next()){
			String serverStr = smStorRs.getString("server");
			if(!(serverStr.equals("0") || serverStr.contains("|"+Conf.sid+"|"))){
				continue;
			}
			String channelStr = smStorRs.getString("channel");
			if(!(channelStr.equals("0") || channelStr.contains(plaRs.getString("channel")))){
				continue;
			}
			if(smailids.contains(smStorRs.getInt("id"))){
				continue;
			}
			String[][] filter = Tools.splitStrToStrArr2(smStorRs.getString("filtercond"), "|", ",");
			boolean match = true;
			for(int i = 0; filter != null && i < filter.length; i++){
				if(filter[i][0].equals("1")){
					match = plaRs.getTime("savetime") <= MyTools.getTimeLong(filter[i][1]);
				} else 
				if(filter[i][0].equals("2")){
					match = plaRs.getInt("lv") >= Tools.str2int(filter[i][1]);
				} else 
				if(filter[i][0].equals("3")){
					match = plaRs.getInt("vip") >= Tools.str2int(filter[i][1]);
				} else 
				if(filter[i][0].equals("-1")){
					match = Tools.contain(filter[i], plaRs.getString("vsid"));
				}
				if(!match){
					break;
				}
			}
			if(!match){
				continue;
			}
			int mailid = insertMail(dbHelper, 0, null, playerid, null, smStorRs.getString("title"), smStorRs.getString("content"), smStorRs.getString("adjunct"), smStorRs.getInt("id"));
			JSONArray arr = new JSONArray();
			arr.add(mailid);
			arr.add(0);//���ID
			arr.add(0);//�����
			arr.add(0);//�Ƿ��Ѷ�
			arr.add(smStorRs.getString("title"));//�ʼ�����
			arr.add(smStorRs.getTime("createtime"));
			arr.add(smStorRs.getString("adjunct")!=null?0:-1);//�Ƿ�����ȡ����
			mailarr.add(arr);
		}
		dbHelper.closeRs(mailStorRs);
		JSONArray linkmanarr = new JSONArray();
		ResultSet plamailRs = dbHelper.query(tab_pla_mail, "linklist", "playerid="+playerid);
		if(plamailRs.next()){
			JSONArray linklist = new JSONArray(plamailRs.getString("linklist"));
			JSONArray pidlist = linklist.optJSONArray(0);
			JSONArray pnamelist = linklist.optJSONArray(1);
			for(int i = pidlist.length()-1; i >= 0; i--){
				JSONArray arr = new JSONArray();
				arr.add(pidlist.optInt(i));
				arr.add(pnamelist.optString(i));
				linkmanarr.add(arr);
			}
		}
		dbHelper.closeRs(plamailRs);
		JSONArray returnarr = new JSONArray();
		returnarr.add(mailarr);//�ʼ��б�
		returnarr.add(linkmanarr);//������ϵ��
		return returnarr;
	}
	
	//--------------��̬��--------------
	
	private static MailBAC instance = new MailBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static MailBAC getInstance(){
		return instance;
	}
}
