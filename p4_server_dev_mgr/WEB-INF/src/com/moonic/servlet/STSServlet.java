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

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ConfigBAC;
import com.moonic.util.DBHelper;
import com.moonic.util.MyLog;

import conf.Conf;
import conf.LogTbName;

public class STSServlet extends HttpServlet {
	private static final long serialVersionUID = 4598035092703154800L;
	
	//-----------------�û���-----------------
	
	/**
	 * ע�Ὰ���������ʺ�
	 */
	public static final short M_JJC_REGISTER_PC = 108;
	/**
	 * ����ļ�
	 */
	public static final short M_FILE_CHECK = 151;
	/**
	 * ��ȡ���ݿ���Ϣ
	 */
	public static final short M_GET_DBINFO = 251;
	/**
	 * ��ȡ�б����嵥
	 */
	public static final short M_TESTA = 252;
	/**
	 * ��ȡ�ı������嵥
	 */
	public static final short M_TESTB = 253;
	/**
	 * �鿴�б���
	 */
	public static final short M_GET_LISTPOOL = 254;
	/**
	 * ����б���
	 */
	public static final short M_CLEAR_TABPOOL = 255;
	/**
	 * �鿴�ı�����
	 */
	public static final short M_GET_TXTPOOL = 256;
	/**
	 * ����ı�����
	 */
	public static final short M_CLEAR_TXTPOOL = 257;
	/**
	 * ����ı�����
	 */
	public static final short M_CLEAR_COLPOOL = 259;
	/**
	 * ��ȡ������־�߳�״̬
	 */
	public static final short M_INSERTLOG_GET_STATE = 403;
	/**
	 * ���ò�����־ʧ�ܴ���
	 */
	public static final short M_RESET_INSERTLOG_TIMEOUTAM = 404;
	/**
	 * ��ȡ����������״̬
	 */
	public static final short M_GET_SERVER_RUN_STATE = 410;
	/**
	 * ���͸��°�
	 */
	public static final short M_SERVER_UPDATE = 501;
	
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
	 * �Ϸ��˼���
	 */
	public static final short G_MERGERSERVER_EXITFAC = 378;
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
		int currentAct=0;
		
		try {
			String ip = IPAddressUtil.getIp(request);
			
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
				if(ConfigBAC.getBoolean("logout_sts_ex"));
				{		
					DataInputStream edis = new DataInputStream(new ByteArrayInputStream(buff));
					currentAct = edis.readShort();
					LogBAC.logout("sts", request.getSession().getId()+"\tip="+ip+"\tact="+currentAct+"\tstart\tactive="+ServerConfig.getDataBase().getNumActive()+"\tidle="+ServerConfig.getDataBase().getNumIdle());
				}
				
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
			if(!"��Ч����".equals(val.info))
			{				
				if(ConfigBAC.getBoolean("logout_sts_ex"))
				{
					LogBAC.logout("sts", request.getSession().getId()+"\tip="+ip+"\tact="+currentAct+"\tend\tactive="+ServerConfig.getDataBase().getNumActive()+"\tidle="+ServerConfig.getDataBase().getNumIdle()+"\t"+(t3-t2)+"ms");
				}
				
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
	
	public static MyLog stslog = new MyLog(MyLog.NAME_DATE, "log_sts", "STS", false, false, true, null);
	
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
		{
			return new ReturnValue(false, "��Ч���� " + act);
		}
	}
}
