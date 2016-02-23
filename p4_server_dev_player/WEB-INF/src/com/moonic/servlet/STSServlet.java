package com.moonic.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.BkSysMailBAC;
import com.moonic.bac.CBBAC;
import com.moonic.bac.ChargeBAC;
import com.moonic.bac.ChargeSendBAC;
import com.moonic.bac.ConfigBAC;
import com.moonic.bac.FileMgrBAC;
import com.moonic.bac.MailBAC;
import com.moonic.bac.PartnerBAC;
import com.moonic.bac.PlaAssectBAC;
import com.moonic.bac.PlaJJCRankingBAC;
import com.moonic.bac.PlaMineralsBAC;
import com.moonic.bac.PlaTeamBAC;
import com.moonic.bac.PlatformGiftCodeBAC;
import com.moonic.bac.PlayerBAC;
import com.moonic.bac.RankingBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.SystemUpdateBAC;
import com.moonic.bac.TxtFileBAC;
import com.moonic.bac.WorldBossBAC;
import com.moonic.battle.BattleManager;
import com.moonic.mirror.MirrorMgr;
import com.moonic.socket.PushData;
import com.moonic.socket.SocketInfoMgr;
import com.moonic.socket.SocketServer;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.MyLog;

import conf.Conf;
import conf.LogTbName;

public class STSServlet extends HttpServlet {
	private static final long serialVersionUID = 4598035092703154800L;
	
	//-----------------��Դ��-----------------
	
	/**
	 * ���ļ�
	 */
	public static final short R_SAVE_FILE = 101;
		
	//-----------------�û���-----------------
	
	/**
	 * ע���û�
	 */
	public static final short M_USER_LOGOUT = 101;
	/**
	 * ��ѯ֧������״̬
	 */
	public static final short M_QUERY_ORDER = 103;
	/**
	 * ��ȡ��ֵ������
	 */
	public static final short M_GET_ORDERNO = 104;
	/**
	 * ����һ�����
	 */
	public static final short M_CBT_SEND_EXCHANGE = 105;
	/**
	 * �ƹ�Ա��������
	 */
	public static final short M_EXTEN_1 = 106;
	/**
	 * �ƹ�Ա�ɳ����
	 */
	public static final short M_EXTEN_2 = 107;
	
	//-----------------��Ϸ��-----------------
	
	/**
	 * ����֪ͨ
	 */
	public static final short G_SEND_INFORM = 101;
	/**
	 * ��ϵͳ��Ϣ
	 */
	public static final short G_SEND_SYSMSG = 102;
	/**
	 * ����Ϸ����
	 */
	public static final short G_SEND_GAMEPUSH = 104;
	/**
	 * ������������Ϣ
	 */
	public static final short G_SEND_TOPMSG = 105;
	/**
	 * �򵥸���ҷ�֪ͨ
	 */
	public static final short G_SEND_INFORM_TOONE = 106;
	/**
	 * �Ͽ����ý�ɫ
	 */
	public static final short G_CLEAR_ALLPLAYER = 151;
	/**
	 * ��ȡ���Ͷ�����Ϣ
	 */
	public static final short G_GET_PUSHDATA = 152;
	/**
	 * ����SOCKET������
	 */
	public static final short G_START_SOCKET = 153;
	/**
	 * ֹͣSOCKET������
	 */
	public static final short G_STOP_SOCKET = 154;
	/**
	 * ��ȡSOCKET����״̬
	 */
	public static final short G_SOCKET_GETSTATE = 155;
	/**
	 * ������Ͷ���
	 */
	public static final short G_CLEAR_PUSHDATA = 156;
	/**
	 * �Ͽ�ָ����ɫ
	 */
	public static final short G_BREAK_ONEPLAYER = 157;
	/**
	 * ��ȡSOCKET������Ϣ
	 */
	public static final short G_SOCKET_RUN_INFO = 161;
	/**
	 * ���ô��������������
	 */
	public static final short G_SERVER_OPENREADY = 162;
	/**
	 * ��������������
	 */
	public static final short G_DB_ADJUST_IDLE = 163;
	/**
	 * ��ȡ���ݿ���Ϣ
	 */
	public static final short G_GET_DBINFO = 251;
	/**
	 * ��ȡ�б����嵥
	 */
	public static final short G_TESTA = 252;
	/**
	 * ��ȡ�ı������嵥
	 */
	public static final short G_TESTB = 253;
	/**
	 * �鿴�б���
	 */
	public static final short G_GET_LISTPOOL = 254;
	/**
	 * ����б���
	 */
	public static final short G_CLEAR_TABPOOL = 255;
	/**
	 * �鿴�ı�����
	 */
	public static final short G_GET_TXTPOOL = 256;
	/**
	 * ����ı�����
	 */
	public static final short G_CLEAR_TXTPOOL = 257;
	/**
	 * �������������
	 */
	public static final short G_CLEAR_SERVER_DATA = 258;
	/**
	 * ֪ͨ��������
	 */
	public static final short G_PLAYER_BEOFFLINE = 353;
	/**
	 * ֪ͨ��������
	 */
	public static final short G_PLAYER_LOGOUT = 354;
	/**
	 * ��ֵ
	 */
	public static final short G_PLAYER_RECHARGE = 355;
	/**
	 * ����Ȩ
	 */
	public static final short G_PLAYER_BUY_TQ = 357;
	/**
	 * ���͸��°�
	 */
	public static final short G_SERVER_UPDATE = 358;
	/**
	 * ��������������
	 */
	public static final short G_ORDER_BATCH_GIVE = 359;
	/**
	 * �иı�ֵ
	 */
	public static final short G_PLAYER_CHANGEVALUE = 364;
	/**
	 * ֪ͨ��Ϸ�����˸
	 */
	public static final short G_ICON_FLASH = 365;
	/**
	 * ��ϵͳ�ʼ�
	 */
	public static final short G_BK_SEND_SYS_MAIL = 366;
	/**
	 * ��ȫ��ϵͳ�ʼ�
	 */
	public static final short G_BK_SEND_SERVER_SYS_MAIL = 367;
	/**
	 * ��ɫ���
	 */
	public static final short G_PLAYER_BLANK = 368;
	/**
	 * ��ɫ���
	 */
	public static final short G_PLAYER_UNBLANK = 369;
	/**
	 * ��ɫ����
	 */
	public static final short G_PLAYER_BANNED_MSG = 370;
	/**
	 * ��ɫ���
	 */
	public static final short G_PLAYER_UNBANNED_MSG = 371;
	/**
	 * �ָ���ҲƲ�
	 */
	public static final short G_PLAYER_ASSECT_RECOVER = 372;
	/**
	 * WEB��ȡ���
	 */
	public static final short G_WEB_GET_PLATFORMGIFT = 373;
	/**
	 * ��ϵͳ�ʼ�
	 */
	public static final short G_SEND_SYS_MAIL2 = 379;
	/**
	 * ��ȡ�ļ�����
	 */
	public static final short G_TXT_FILE_GET_CONTENT = 402;
	/**
	 * ��ȡ������־�߳�״̬
	 */
	public static final short G_INSERTLOG_GET_STATE = 403;
	/**
	 * ���ò�����־ʧ�ܴ���
	 */
	public static final short G_RESET_INSERTLOG_TIMEOUTAM = 404;
	/**
	 * ����ļ�
	 */
	public static final short G_FILE_CHECK = 407;
	/**
	 * ��ȡ����
	 */
	public static final short G_WEB_TARGET_GETRANKING = 408;
	/**
	 * ��ȡ��ɫ����
	 */
	public static final short G_BK_GET_PLAYER_DATA = 409;
	/**
	 * ��ȡ����������״̬
	 */
	public static final short G_GET_SERVER_RUN_STATE = 410;
	/**
	 * ��ȡ����
	 */
	public static final short G_MIRROR_GET_TAB = 411;
	/**
	 * ��ȡ��ɫ����
	 */
	public static final short G_MIRROR_GET_PLA = 412;
	/**
	 * ��ȡ��ɫ����
	 */
	public static final short G_MIRROR_GET_PLA_TAB = 413;
	/**
	 * �徵��
	 */
	public static final short G_MIRROR_CLEAR_TAB = 414;
	/**
	 * ˢ����Ϸ���а�
	 */
	public static final short G_REFRESH_GAME_RANKING = 464;
	/**
	 * ��ȡָ��������л���ս������
	 */
	public static final short G_PARTNER_GETSPRITEBOX = 465;
	/**
	 * ��������������
	 */
	public static final short G_JJC_CREATE_PC = 466;
	/**
	 * ��ս-NPC����
	 */
	public static final short G_CB_NPCINVADE = 467;
	/**
	 * ����BOSS-����
	 */
	public static final short G_WB_START = 468;
	/**
	 * ��ȡPVPս����Ϣ
	 */
	public static final short G_PVP_BATTLE_INFO = 469;
	/**
	 * ��ӻ-����
	 */
	public static final short G_TEAM_ACTI_START = 470;
	/**
	 * ��������ȼ�
	 */
	public static final short G_UPDATE_WORLDLEVEL = 471;
	/**
	 * ���ž���������
	 */
	public static final short G_ISSUE_JJCRANKING_AWARD = 472;
	/**
	 * �����ڿ�
	 */
	public static final short G_MINERALS_START = 473;
	/**
	 * ֹͣ�ڿ�
	 */
	public static final short G_MINERALS_END = 474;
	/**
	 * ��ȡ��λ��Ϣ
	 */
	public static final short G_MINERALS_GETPOSDATA = 475;
	
	/**
	 * service
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long t1= System.currentTimeMillis();
		InputStream is = request.getInputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[4096];
		int readLen = -1;
		while ((readLen = is.read(buff)) != -1) {
			baos.write(buff, 0, readLen);
		}
		buff = baos.toByteArray();
		long t2= System.currentTimeMillis();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buff));
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());
		try {
			ReturnValue val = null;
			SqlString reqSqlStr = new SqlString();
			reqSqlStr.addDateTime("reqtime", Tools.getCurrentDateTimeStr());
			reqSqlStr.add("reqflow", buff.length);
			if (buff.length == 0) {
				val = new ReturnValue(false, "��Ч����");
			} 
			/*else if(DBHelper.connectionAmount >= 100){
				val = new ReturnValue(false, "��������æ");
			}*/
			else {
				try {
					val = processingReq(request, response, dis, dos, reqSqlStr);		
				} catch (EOFException e) {
					DataInputStream edis = new DataInputStream(new ByteArrayInputStream(buff));
					int act = edis.readShort();
					System.out.println(e.toString()+"(act="+act+")");
					e.printStackTrace();
					val = new ReturnValue(false, e.toString());
				} catch (Exception e) {
					e.printStackTrace();
					val = new ReturnValue(false, e.toString());
				}
			}
			dis.close();
			byte[] responseData = null;
			if(val.getDataType()==ReturnValue.TYPE_STR) {
				responseData = Tools.strNull(val.info).getBytes("UTF-8");
			} else 
			if(val.getDataType()==ReturnValue.TYPE_BINARY) {
				responseData = val.binaryData;
			}
			long t3= System.currentTimeMillis();
			dos.writeByte(val.success ? 1 : 0);
			dos.write(responseData);
			long t4= System.currentTimeMillis();
			reqSqlStr.addDateTime("resptime", Tools.getCurrentDateTimeStr());
			reqSqlStr.add("respflow", responseData.length);
			reqSqlStr.add("respresult", val.success ? 1 : 0);
			reqSqlStr.add("respdatatype", val.getDataType());
			reqSqlStr.add("usedtime", t3-t2);
			reqSqlStr.add("uploadtime", t2-t1);
			reqSqlStr.add("downloadtime", t4 - t3);
			if(!"��Ч����".equals(val.info)){
				if(ConfigBAC.getBoolean("sts_http_log"))
				{
					if(ConfigBAC.getInt("logout_sts_http_threshold")<(t3-t2))
					{
						DBHelper.logInsert(LogTbName.TAB_STS_HTTP_LOG(), reqSqlStr);
					}						
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			dos.writeByte(0);
			dos.write(e.toString().getBytes("UTF-8"));
		}
		finally
		{
			dos.close();
		}
	}
	
	public static MyLog stslog = new MyLog(MyLog.NAME_DATE, "log_sts", "STS", Conf.debug, false, true, null);
	
	/**
	 * ��������
	 */
	private ReturnValue processingReq(HttpServletRequest request, HttpServletResponse response, DataInputStream dis, DataOutputStream dos, SqlString reqSqlStr) throws Exception{
		short act = dis.readShort();
		String senderkey = dis.readUTF();
		stslog.d("���յ����� " + senderkey + " ������ "+ act);
		
		reqSqlStr.add("act", act);
		reqSqlStr.add("reqserver", senderkey);
		reqSqlStr.add("respserver", Conf.stsKey);
		if(act == G_SEND_INFORM){
			String title = dis.readUTF();
			String content = dis.readUTF();
			String overtimeStr = dis.readUTF();
			String extend = dis.readUTF();
			byte isAll = dis.readByte();
			return ServerBAC.getInstance().sendInform(0, title, content, overtimeStr, extend, isAll==1, null);
		} else 
		if(act == G_SEND_SYSMSG){
			String msg = dis.readUTF();
			return ServerBAC.getInstance().sendSysMsg(msg);
		} else 
		if(act == G_SEND_GAMEPUSH){
			String param = dis.readUTF();
			return ServerBAC.getInstance().sendGamePush(param);
		} else 
		if(act == G_SEND_TOPMSG){
			String msg = dis.readUTF();
			return ServerBAC.getInstance().sendTopMsg(msg);
		} else 
		if(act == G_SEND_INFORM_TOONE){
			int playerid = dis.readInt();
			String title = dis.readUTF();
			String content = dis.readUTF();
			String overtimeStr = dis.readUTF();
			String extend = dis.readUTF();
			return ServerBAC.getInstance().sendInform(playerid, title, content, overtimeStr, extend, false, null);
		} else 
		if(act == G_CLEAR_ALLPLAYER){
			String info = dis.readUTF();
			byte type = dis.readByte();
			return SocketServer.getInstance().clearAllPla(info, type);
		} else 
		if(act == G_GET_PUSHDATA){
			return new ReturnValue(true, SocketServer.getInstance().getPushQueueData());
		} else 
		if(act == G_START_SOCKET){
			return SocketServer.getInstance().start();
		} else 
		if(act == G_STOP_SOCKET){
			return SocketServer.getInstance().stop();
		} else 
		if(act == G_SOCKET_GETSTATE){
			return SocketServer.getInstance().getRunState();
		} else 
		if(act == G_CLEAR_PUSHDATA){
			return new ReturnValue(true, SocketServer.getInstance().clearPushQueue());
		} else 
		if(act == G_BREAK_ONEPLAYER){
			int playerid = dis.readInt();
			String info = dis.readUTF();
			return SocketServer.getInstance().breakOnePla(playerid, info);
		} else 
		if(act == G_SOCKET_RUN_INFO){
			return SocketInfoMgr.getInstance().getSocketRunData();
		} else 
		if(act == G_SERVER_OPENREADY){
			return ServerBAC.getInstance().openServerReady();
		} else 
		if(act == G_DB_ADJUST_IDLE){
			return ServerBAC.getInstance().adjustDBIdle();
		} else 
		if(act == G_GET_DBINFO){
			return new ReturnValue(true, DBHelper.getConnAmInfo());
		} else 
		if(act == G_TESTA){
			return DBPool.getInst().TestA();
		} else 
		if(act == G_TESTB){
			return DBPool.getInst().TestB();
		} else 
		if(act == G_GET_LISTPOOL){
			String tab = dis.readUTF();
			return DBPool.getInst().Test1(tab);
		} else 
		if(act == G_CLEAR_TABPOOL){
			String tab = dis.readUTF();
			return DBPool.getInst().Test2(tab);
		} else 
		if(act == G_GET_TXTPOOL){
			String key = dis.readUTF();
			return DBPool.getInst().Test3(key);
		} else 
		if(act == G_CLEAR_TXTPOOL){
			String key = dis.readUTF();
			return DBPool.getInst().Test4(key);
		} else 
		if(act == G_CLEAR_SERVER_DATA){
			return ServerBAC.getInstance().clearServerData();
		} else 
		if(act == G_PLAYER_BEOFFLINE){
			String sessionid = dis.readUTF();
			return PlayerBAC.getInstance().beOffline(sessionid);
		} else 
		if(act == G_PLAYER_LOGOUT){
			int playerid = dis.readInt();
			String reason = dis.readUTF();
			return PlayerBAC.getInstance().logout(playerid, "STS"+reason);
		} else 
		if(act == G_PLAYER_RECHARGE){
			byte result = dis.readByte();
			String note = dis.readUTF(); //����
			byte from = dis.readByte(); //��Դ 1 �ͻ��� 2��վ
			String channel = dis.readUTF(); //��������
			String orderNo = dis.readUTF(); //������
			String centerOrderNo = dis.readUTF();//��ֵ���Ķ�����
			int playerid = dis.readInt();
			byte chargepoint = dis.readByte();//��ֵ��
			short rechargetype = 0;
			int rmbam = 0;
			if(result==1){
				rechargetype = dis.readShort();
				rmbam = dis.readInt();	
			}
			//System.out.println("�յ��û�������𶧵Ķ���"+orderNo+"���result="+result+",note="+note+",channel="+channel+",playerid="+playerid);
			LogBAC.logout("charge/"+channel,"�յ��û�������𶧵Ķ���"+orderNo+"���result="+result+",note="+note+",channel="+channel+",playerid="+playerid);
			ReturnValue rv = ChargeBAC.getInstance().recharge(playerid, rechargetype, rmbam, result, note, from, channel, chargepoint, centerOrderNo);
			if(result==1)
			{
				LogBAC.logout("charge/"+channel,"����"+orderNo+"�������="+rv.success);
				if(rv.success)
				{
					ChargeSendBAC.getInstance().createSendOrder(Conf.sid, channel, orderNo, 1);	
				}
				else
				{
					ChargeSendBAC.getInstance().createSendOrder(Conf.sid, channel, orderNo, -1);	
				}
			}
			return rv;
		} else 
		if(act == G_PLAYER_BUY_TQ){
			byte result = dis.readByte();
			String note = dis.readUTF(); //����
			byte from = dis.readByte(); //��Դ 1 �ͻ��� 2��վ
			String channel = dis.readUTF(); //��������
			String orderNo = dis.readUTF(); //������	
			String centerOrderNo = dis.readUTF();//��ֵ���Ķ�����
			int playerid = dis.readInt();
			byte tqnum = 0;
			if(result == 1){
				tqnum = dis.readByte();
			}
			//System.out.println("�յ��û���������Ȩ�Ķ���"+orderNo+"���result="+result+",note="+note+",channel="+channel+",playerid="+playerid);
			LogBAC.logout("charge/"+channel,"�յ��û���������Ȩ�Ķ���"+orderNo+"���result="+result+",note="+note+",channel="+channel+",playerid="+playerid);
			ReturnValue rv = ChargeBAC.getInstance().buyTQ(playerid, tqnum, result, note, from, channel, centerOrderNo);
			if(result==1)
			{
				LogBAC.logout("charge/"+channel,"����"+orderNo+"�������="+rv.success);
				if(rv.success)
				{
					ChargeSendBAC.getInstance().createSendOrder(Conf.sid, channel, orderNo, 1);
				}
				else
				{
					ChargeSendBAC.getInstance().createSendOrder(Conf.sid, channel, orderNo, -1);
				}
			}
			return rv;
		} else
		if(act == G_SERVER_UPDATE) {
			String filename = dis.readUTF();
			int fileLen = dis.readInt();
			byte[] zipBytes = new byte[fileLen];
			dis.read(zipBytes);
			return SystemUpdateBAC.getInstance().updateSystem(filename, zipBytes);
		} else 
		if(act == G_ORDER_BATCH_GIVE) {
			byte chargepoint = dis.readByte();//��ֵ��
			int len = dis.readInt();
			byte[] bytes = new byte[len];
			dis.read(bytes);
			String jsonStr = new String(bytes, "UTF-8");
			LogBAC.logout("charge_regive","�յ��û�����������,jsonStr="+jsonStr);
			//System.out.println("�յ�"+jsonStr);
			JSONObject jsonObj = new JSONObject(jsonStr);
			return ChargeBAC.getInstance().orderBatchGive(jsonObj, ChargeBAC.FROM_ORDERGIVE, chargepoint);
		} else 
		if(act == G_PLAYER_CHANGEVALUE){
			int playerid = dis.readInt();
			byte type = dis.readByte();
			String changevalue = dis.readUTF();
			String from = dis.readUTF();
			return PlayerBAC.getInstance().changeValue(playerid, type, changevalue, from);
		} else 
		if(act == G_ICON_FLASH){
			int code = dis.readInt();			
			int activity = dis.readInt();	
			JSONArray pusharr = new JSONArray();
			pusharr.add(code);			
			pusharr.add(activity);
			PushData.getInstance().sendPlaToAllOL(SocketServer.ACT_ICON_FLASH, pusharr.toString());
			return new ReturnValue(true);
		} else 
		if(act == G_BK_SEND_SYS_MAIL){
			String receiverids = dis.readUTF();
			int smailid = dis.readInt();
			return BkSysMailBAC.getInstance().sendToSome(receiverids, smailid);
		} else 
		if(act == G_BK_SEND_SERVER_SYS_MAIL){
			int smailid = dis.readInt();
			return BkSysMailBAC.getInstance().sendToServer(smailid);
		} else 
		if(act == G_PLAYER_BLANK){
			int playerid = dis.readInt();
			String date = dis.readUTF();
			return PlayerBAC.getInstance().blankOffPlayer(playerid, date);
		} else 
		if(act == G_PLAYER_UNBLANK){
			int playerid = dis.readInt();
			return PlayerBAC.getInstance().unBlankOffPlayer(playerid);
		} else 
		if(act == G_PLAYER_BANNED_MSG){
			int playerid = dis.readInt();
			String date = dis.readUTF();
			return PlayerBAC.getInstance().bannedToPostPlayer(playerid, date);
		} else 
		if(act == G_PLAYER_UNBANNED_MSG){
			int playerid = dis.readInt();
			return PlayerBAC.getInstance().unBannedToPostPlayer(playerid);
		} else 
		if(act == G_PLAYER_ASSECT_RECOVER){
			int id = dis.readInt();
			return PlaAssectBAC.getInstance().recover(id);
		} else 
		if(act == G_WEB_GET_PLATFORMGIFT){
			int playerid = dis.readInt();
			String code = dis.readUTF();
			return PlatformGiftCodeBAC.getInstance().webGetPlatformGift(playerid, code);
		} else 
		if(act == G_SEND_SYS_MAIL2){
			String receiverids = dis.readUTF();
			String title = dis.readUTF();
			String content = dis.readUTF();
			String adjunct = dis.readUTF();
			return MailBAC.getInstance().sendSysMail(receiverids, title, content, adjunct.equals("")?null:adjunct, 0);
		} else 
		if(act == G_TXT_FILE_GET_CONTENT){
			int fileid = dis.readInt();
			return TxtFileBAC.getInstance().getFileContent(fileid);
		} else 
		if(act == G_INSERTLOG_GET_STATE){
			return DBHelper.getSaveLogPQState();
		} else 
		if(act == G_RESET_INSERTLOG_TIMEOUTAM){
			return DBHelper.resetInsertLogTimeoutAm();
		} else 
		if(act == G_FILE_CHECK){
			boolean del = dis.readBoolean();
			return FileMgrBAC.getInstance().checkFile(del);
		} else 
		if(act == G_WEB_TARGET_GETRANKING){
			byte type = dis.readByte();
			return PlayerBAC.getInstance().WebGetRanking(type);
		} else 
		if(act == G_BK_GET_PLAYER_DATA){
			int playerid = dis.readInt();
			return PlayerBAC.getInstance().bkGetAllData(playerid);
		} else 
		if(act == G_GET_SERVER_RUN_STATE){
			return ServerBAC.getInstance().getRunState();
		} else 
		if(act == G_MIRROR_GET_TAB){
			int pid = dis.readInt();
			return new ReturnValue(true, MirrorMgr.getPlaMirrorData(pid));
		} else 
		if(act == G_MIRROR_GET_PLA){
			int pid = dis.readInt();
			String tab = dis.readUTF();
			return new ReturnValue(true, MirrorMgr.getPlaMirrorData(pid, tab));
		} else 
		if(act == G_MIRROR_GET_PLA_TAB){
			String tab = dis.readUTF();
			return new ReturnValue(true, MirrorMgr.getTabMirrorData(tab));
		} else 
		if(act == G_MIRROR_CLEAR_TAB){
			String tab = dis.readUTF();
			MirrorMgr.clearTabData(tab, true);
			return new ReturnValue(true);
		} else 
		if(act == G_REFRESH_GAME_RANKING){
			long refreshtime = dis.readLong();
			String data = dis.readUTF();
			return RankingBAC.getInstance().refreshRanking(refreshtime, data);
		} else 
		if(act == G_PARTNER_GETSPRITEBOX){
			int playerid = dis.readInt();
			return PartnerBAC.getInstance().bkGetSpriteBox(playerid);
		} else 
		if(act == G_JJC_CREATE_PC){
			return PlaJJCRankingBAC.getInstance().createPCPlayer();
		} else 
		if(act == G_CB_NPCINVADE){
			int citynum = dis.readInt();
			int npcinfluence = dis.readInt();
			int[] npcamount = Tools.splitStrToIntArr(dis.readUTF(), ",");
			return CBBAC.getInstance().npcInvade(citynum, npcinfluence, npcamount);
		} else
		if(act == G_WB_START){
			long actiTimeLen = dis.readLong();
			byte isConstraint = dis.readByte();
			return WorldBossBAC.getInstance().start(actiTimeLen, isConstraint);
		} else 
		if(act == G_PVP_BATTLE_INFO){
			return BattleManager.getPVPBattleInfo();
		} else
		if(act == G_TEAM_ACTI_START){
			long actiTimeLen = dis.readLong();
			byte isConstraint = dis.readByte();
			return PlaTeamBAC.getInstance().start(actiTimeLen, isConstraint);
		} else 
		if(act == G_UPDATE_WORLDLEVEL){
			return CBBAC.getInstance().updateWorldLevel();
		} else 
		if(act == G_ISSUE_JJCRANKING_AWARD){
			return PlaJJCRankingBAC.getInstance().issueAward("��̨");
		} else 
		if(act == G_MINERALS_START){
			return PlaMineralsBAC.getInstance().start("��̨");
		} else 
		if(act == G_MINERALS_END){
			return PlaMineralsBAC.getInstance().end("��̨");
		} else 
		if(act == G_MINERALS_GETPOSDATA){
			return PlaMineralsBAC.getInstance().bkGetPosData();
		} else 
		{
			return new ReturnValue(false, "��Ч���� " + act);
		}
	}
}
