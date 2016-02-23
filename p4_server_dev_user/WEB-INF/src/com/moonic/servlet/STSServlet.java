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

import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ChargeOrderBAC;
import com.moonic.bac.ConfigBAC;
import com.moonic.bac.FileMgrBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.SystemUpdateBAC;
import com.moonic.bac.UserBAC;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.DBUtil;
import com.moonic.util.MyLog;

import conf.Conf;
import conf.LogTbName;

public class STSServlet extends HttpServlet {
	private static final long serialVersionUID = 4598035092703154800L;
	
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
	 * �иı�ֵ
	 */
	public static final short G_PLAYER_CHANGEVALUE = 364;
	/**
	 * ֪ͨ��Ϸ�����˸
	 */
	public static final short G_ICON_FLASH = 365;
	/**
	 * WEB��ȡ���
	 */
	public static final short G_WEB_GET_PLATFORMGIFT = 373;
	/**
	 * WEB��ȡ������
	 */
	public static final short G_WEB_TARGET_GETRANKING = 408;
	
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
			//String ip = IPAddressUtil.getIp(request);
			
			ReturnValue val = null;
			SqlString reqSqlStr = new SqlString();
			reqSqlStr.addDateTime("reqtime", Tools.getCurrentDateTimeStr());
			reqSqlStr.add("reqflow", buff.length);
			if (buff.length == 0) {
				val = new ReturnValue(false, "��Ч����");
			} else {
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
			if(!"��Ч����".equals(val.info)) {
				if(ConfigBAC.getBoolean("sts_http_log")) {
					if(ConfigBAC.getInt("logout_sts_http_threshold")<(t3-t2)) {
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
		if(act == M_USER_LOGOUT){
			int userid = dis.readInt();
			String reason = dis.readUTF();
			return UserBAC.getInstance().logout(userid, reason);
		} else 
		if(act == M_QUERY_ORDER) {
			String channel = dis.readUTF();
			String orderNo = dis.readUTF();
			JSONObject json = ChargeOrderBAC.getInstance().getJsonObj("channel='"+channel+"' and orderNo='"+orderNo+"'");
			if(json!=null) {
				return new ReturnValue(true, json.toString());
			} else {
				return new ReturnValue(false, "������"+orderNo+"������");
			}
		} else 
		if(act == M_GET_ORDERNO) {
			String channel = dis.readUTF();
			String extend = dis.readUTF();
			LogBAC.logout("charge/"+channel, "�յ������¶�������extend="+extend);
			return ChargeOrderBAC.getInstance().getChargeOrderno(channel,extend);			
		} else 
		if(act == M_JJC_REGISTER_PC){
			return UserBAC.getInstance().registerJJCUser();
		} else 
		if(act == M_FILE_CHECK){
			boolean del = dis.readBoolean();
			return FileMgrBAC.getInstance().checkFile(del);
		} else 
		if(act == M_GET_DBINFO){
			return new ReturnValue(true, DBHelper.getConnAmInfo());
		} else 
		if(act == M_TESTA){
			return DBPool.getInst().TestA();
		} else 
		if(act == M_TESTB){
			return DBPool.getInst().TestB();
		} else 
		if(act == M_GET_LISTPOOL){
			String tab = dis.readUTF();
			return DBPool.getInst().Test1(tab);
		} else 
		if(act == M_CLEAR_TABPOOL){
			String tab = dis.readUTF();
			return DBPool.getInst().Test2(tab);
		} else 
		if(act == M_GET_TXTPOOL){
			String key = dis.readUTF();
			return DBPool.getInst().Test3(key);
		} else 
		if(act == M_CLEAR_TXTPOOL){
			String key = dis.readUTF();
			return DBPool.getInst().Test4(key);
		} else 
		if(act == M_CLEAR_COLPOOL){
			String tab = dis.readUTF();
			DBUtil.clearColData(tab);
			return new ReturnValue(true);
		} else 
		if(act == M_INSERTLOG_GET_STATE){
			return DBHelper.getSaveLogPQState();
		} else 
		if(act == M_RESET_INSERTLOG_TIMEOUTAM){
			return DBHelper.resetInsertLogTimeoutAm();
		} else 
		if(act == M_GET_SERVER_RUN_STATE){
			return ServerBAC.getInstance().getRunState();
		} else 
		if(act == M_SERVER_UPDATE){
			String filename = dis.readUTF();
			int fileLen = dis.readInt();
			byte[] zipBytes = new byte[fileLen];
			dis.read(zipBytes);
			return SystemUpdateBAC.getInstance().updateSystem(filename, zipBytes);
		} else 
		{
			return new ReturnValue(false, "��Ч���� " + act);
		}
	}
}
