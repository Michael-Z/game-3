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

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ActivateCodeBAC;
import com.moonic.bac.ChannelBAC;
import com.moonic.bac.ChargeOrderBAC;
import com.moonic.bac.ConfigBAC;
import com.moonic.bac.PlatformBAC;
import com.moonic.bac.RanNameBAC;
import com.moonic.bac.ServerBAC;
import com.moonic.bac.SysNoticeBAC;
import com.moonic.bac.UserBAC;
import com.moonic.bac.VersionBAC;
import com.moonic.mode.User;
import com.moonic.platform.P;
import com.moonic.platform.P001;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.EncryptionUtil;
import com.moonic.util.MyTools;

import conf.LogTbName;

public class GameServlet extends HttpServlet {
	private static final long serialVersionUID = 4598035092703154800L;
	
	public static boolean useHTTPEncrypt=true; //��http���ݽ��м��ܵĹ��ܿ���
	
	/**
	 * ������汾
	 */
	public static final short ACT_VER_APK_CHECK = 101;	
	/**
	 * �����Դ�汾
	 */
	public static final short ACT_VER_RES_CHECK = 102;
	
	/**
	 * ����Ϸ
	 */
	public static final short ACT_PLATFROM_OPENGAME = 104;
	/**
	 * ��ȡϵͳ����
	 */
	public static final short ACT_GET_SYS_NOTICE = 122;
	/**
	 * ע��
	 */
	public static final short ACT_USER_REG = 141;
	/**
	 * ��¼
	 */
	public static final short ACT_USER_LOGIN = 142;
	/**
	 * ������Ϸ
	 */
	public static final short ACT_SHORTCUT_GAME = 143;
	/**
	 * �ֻ��һ�����
	 */
	public static final short ACT_USER_MOBILE_FIND_PWD = 144;
	/**
	 * �����һ�����
	 */
	public static final short ACT_USER_EMAIL_FIND_PWD = 145;
	/**
	 * ��������ά�����
	 */
	public static final short ACT_JUMP_CHECK = 146;
	/**
	 * ������߻�ȡ�����б�
	 */
	public static final short ACT_GET_CHANNLE_LIST = 174;
	/**
	 * �����ʺ�
	 */
	public static final short ACT_USER_ACTIVATE = 201;
	/**
	 * ע��
	 */
	public static final short ACT_USER_LOGOUT = 202;
	/**
	 * ��ȡ�������б�
	 */
	public static final short ACT_SERVER_LIST = 203;
	/**
	 * ��ȡ�����ɫ��
	 */
	public static final short ACT_PLAYER_RANNAME = 204;
	/**
	 * ��ȡ���˿����
	 */
	public static final short ACT_GET_CARDVALUE= 208;
	/**
	 * ������ֵ����
	 */
	public static final short ACT_CREATE_ORDER = 301;
	
	/**
	 * service
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long t1= System.currentTimeMillis();
		/*Enumeration enu = request.getHeaderNames();
		System.out.println("http����head-----------------");
		while(enu.hasMoreElements())
		{
			String key = (String)enu.nextElement();
			System.out.println(key+"="+request.getHeader(key));
		}*/
		//System.out.println(Tools.getCurrentDateTimeStr()+"--����");
		String ip = IPAddressUtil.getIp(request);
		//System.out.println("ip="+ip);
		//���˻���֩�����
		String agent = request.getHeader("User-Agent");
		if(agent!=null && (agent.indexOf("spider")!=-1
		|| agent.indexOf("roboo")!=-1			
		|| agent.toLowerCase().indexOf("bot")!=-1			
		))
		{
			return;
		}
		InputStream is = request.getInputStream();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[4096];
		int readLen = -1;
		while ((readLen = is.read(buff)) != -1) {
			baos.write(buff, 0, readLen);
		}
		buff = baos.toByteArray();
		
		if(useHTTPEncrypt)
		{			
			buff = EncryptionUtil.RC4(buff);  //���ݽ���
		}
		/*if(buff.length==0)
		{
			System.out.println(Tools.getCurrentDateTimeStr()+"--���յ�����"+ip+"��http����,���ݳ���Ϊ0");
		}*/
		
		long t2= System.currentTimeMillis();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buff));
		DataOutputStream dos = new DataOutputStream(response.getOutputStream());		
		try {
			ReturnValue val = null;
			SqlString reqSqlStr = null;
			if(ConfigBAC.getBoolean("http_log"))
			{
				reqSqlStr = new SqlString();
				reqSqlStr.addDateTime("reqtime", Tools.getCurrentDateTimeStr());
				reqSqlStr.add("reqflow", buff.length);	
			}
			JSONArray processarr = new JSONArray();
			
			int currentAct=0;
			
			if (buff.length == 0) {
				val = new ReturnValue(false, "��Ч����");
			} 
			/*
			else if(DBHelper.connectionAmount >= 100){
				val = new ReturnValue(false, "��������æ");
			}
			*/
			else {				
				
				if(ConfigBAC.getBoolean("logout_http_ex"))
				{
					DataInputStream edis = new DataInputStream(new ByteArrayInputStream(buff));
					currentAct = edis.readShort();
					LogBAC.logout("http", request.getSession().getId()+"\tip="+ip+"\tact="+currentAct+"\tstart\tactive="+ServerConfig.getDataBase().getNumActive()+"\tidle="+ServerConfig.getDataBase().getNumIdle());
				}
				
				
				try {
					val = processingReq(request, response, dis, dos, reqSqlStr, processarr);					
				} catch (EOFException e) {
					DataInputStream edis = new DataInputStream(new ByteArrayInputStream(buff));
					int act = edis.readShort();
					System.out.println(e.toString()+"(act="+act+")");
					if(act <= 10000){//TODO ��ʾ�������ų����˷�ΧʱӦ��ʱ����
						e.printStackTrace();	
					}
					val = new ReturnValue(false, e.toString());
				} catch (Exception ex1) {
					ex1.printStackTrace();
					val = new ReturnValue(false, ex1.toString());
				}
				if(processarr.length() >= 2){
					short act = (short)processarr.getInt(0);
					User user = (User)processarr.get(1);
					user.removeReqing(act, val);
				}
			}
			dis.close();
			byte[] responseData = null;
			if(val.getDataType()==ReturnValue.TYPE_STR) 
			{				
				responseData = Tools.strNull(val.info).getBytes("UTF-8");
			} 
			else 
			{
				responseData = val.binaryData;
			}
			long t3= System.currentTimeMillis();
			
			//��ü��ܺ���ֽ���
			byte[] outputBytes = getOutputBytes(val.success,responseData);
			if(outputBytes!=null)
			{				
				dos.write(outputBytes);
			}	
			
			long t4= System.currentTimeMillis();
						
			if(!"��Ч����".equals(val.info))
			{
				if(ConfigBAC.getBoolean("logout_http_ex"))
				{
					LogBAC.logout("http", request.getSession().getId()+"\tip="+ip+"\tact="+currentAct+"\tend\tactive="+ServerConfig.getDataBase().getNumActive()+"\tidle="+ServerConfig.getDataBase().getNumIdle()+"\t"+(t3-t2)+"ms");
				}
				
				if(ConfigBAC.getBoolean("http_log"))
				{
					if(ConfigBAC.getInt("logout_http_threshold")<(t3-t2))
					{
						reqSqlStr.addDateTime("resptime", Tools.getCurrentDateTimeStr());
						reqSqlStr.add("respflow", responseData.length);
						reqSqlStr.add("respresult", val.success ? 1 : 0);
						reqSqlStr.add("respdatatype", val.getDataType());
						reqSqlStr.add("usedtime", t3-t2);
						reqSqlStr.add("uploadtime", t2-t1);
						reqSqlStr.add("downloadtime", t4 - t3);
						reqSqlStr.add("ip", ip);
						DBHelper.logInsert(LogTbName.TAB_HTTP_LOG(), reqSqlStr);
					}						
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			byte[] outputBytes = getOutputBytes(false,e.toString().getBytes("UTF-8"));
			if(outputBytes!=null)
			{
				dos.write(outputBytes);
			}
		}
		finally
		{
			dos.close();
		}
	}
	
	/**
	 * ��÷��ص�����bytes
	 * @param success �Ƿ�ɹ��Ľ��
	 * @param dataBytes ԭʼ����bytes
	 * @return
	 */
	private static byte[] getOutputBytes(boolean success,byte[] dataBytes)
	{
		try 
		{
			ByteArrayOutputStream outputBaos = new ByteArrayOutputStream();	
			DataOutputStream outputDos = new DataOutputStream(outputBaos);
			if(success)
			{
				outputDos.writeByte(1);				
			}
			else
			{
				outputDos.writeByte(0);
			}
			outputDos.write(dataBytes);
			outputDos.close();
			
			if(useHTTPEncrypt)  //ʹ�ü��ܻ���
			{	
				dataBytes = EncryptionUtil.RC4(outputBaos.toByteArray());  //���ݼ���
				return dataBytes;			
			}
			else
			{
				return outputBaos.toByteArray();
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static String[] jump_check_ip;
	
	public static short ACT_APPDATA = -101;
	
	/**
	 * ��������
	 */
	private ReturnValue processingReq(HttpServletRequest request, HttpServletResponse response, DataInputStream dis, DataOutputStream dos, SqlString reqSqlStr, JSONArray processarr) throws Exception{
		short act = dis.readShort();	
		processarr.add(act);
		
		long time = dis.readLong();
		String ip = IPAddressUtil.getIp(request);		
		
		//��ȡϵͳ����
		if(act == ACT_GET_SYS_NOTICE){
			long tagtime = dis.readLong();
			String channel = "001";
			try {
				channel = dis.readUTF();	
			} catch (Exception e) {
			}
			return SysNoticeBAC.getInstance().getSysNotice(channel, tagtime);
		}
		//��������ά�����
		if(act == ACT_JUMP_CHECK){
			if(!MyTools.checkInStrArr(jump_check_ip, ip)){
				jump_check_ip = Tools.addToStrArr(jump_check_ip, ip);
			}
			return new ReturnValue(true);
		}
		//��ȡ���Զ���Ϣ
		if(act == ACT_APPDATA){
			JSONArray actarr = new JSONArray();
			actarr.add(ACT_USER_LOGIN);
			actarr.add(ACT_SERVER_LIST);
			actarr.add(302);
			actarr.add(304);
			actarr.add(1);
			actarr.add(1);
			return new ReturnValue(true, actarr.toString());
		}
		//�ж�����ά�����
		if(!ConfigBAC.getBoolean("openlogin")) {
			if(!MyTools.checkInStrArr(jump_check_ip,ip)) {
				return new ReturnValue(false, ConfigBAC.getString("closeloginnote")+"#1");
			}
		}
		if(reqSqlStr!=null)reqSqlStr.add("act", act);
		
		if(act == ACT_VER_APK_CHECK)
		{
			byte platform = dis.readByte(); //1��׿2ios
			String ver = dis.readUTF();
			String channel = dis.readUTF();	
					
			String packageName = dis.readUTF();	
			boolean isBigApk =  dis.readBoolean();
			boolean needPatch = dis.readBoolean();
			String imei = dis.readUTF();
			String mac = dis.readUTF();			
			
			/*if(Conf.ms_url.equals("http://xmlogin.pook.com/xianmo_user/") || Conf.ms_url.equals("http://192.168.1.29:82/xianmo_user/"))
			{
				needPatch = true;  //�����ǿ��ʹ�ò���
			}*/
			return VersionBAC.getInstance().checkApkVer(platform,ver, channel,packageName,isBigApk,needPatch,imei,mac);
		} 
		/*else 
		if(act == ACT_GET_RES_CRC_FILELIST){
			byte phonePlatform = dis.readByte(); //�ֻ�ƽ̨����1��׿2ios
			String channel = dis.readUTF();			
			return VersionBAC.getInstance().getResCRCFileList(phonePlatform,channel);
		} */
		else if(act == ACT_VER_RES_CHECK){
			String ver = dis.readUTF();
			byte platform = dis.readByte();
			return VersionBAC.getInstance().checkResVer(ver, platform);
		} else 
		if(act == ACT_PLATFROM_OPENGAME){
			String data = dis.readUTF();
			return PlatformBAC.getInstance().createtOpenGameLog(data);
		} else 
		if(act == ACT_USER_REG){
			String usernmae = dis.readUTF();
			String password = dis.readUTF();
			String channel = dis.readUTF();
			String logdata = dis.readUTF();
			return UserBAC.getInstance().register(usernmae, password, password, ip, channel, new JSONArray(logdata));//String userAgent = request.getHeader("User-Agent");
		} else 
		if(act == ACT_USER_LOGIN){
			String username = dis.readUTF();
			String password = dis.readUTF();
			String logdata = dis.readUTF();
			String channel = dis.readUTF();
			String extend = dis.readUTF();//��չ������jsonObject��ʽ			
			return UserBAC.getInstance().login(username, password, ip, 0, new JSONArray(logdata), channel, extend);
		} else 
		if(act == ACT_SHORTCUT_GAME){
			String channel = dis.readUTF();
			String logdata = dis.readUTF();
			return UserBAC.getInstance().shortcutGame(ip, channel, new JSONArray(logdata));
		} else 
		if(act == ACT_USER_MOBILE_FIND_PWD){
			String username = dis.readUTF();
			String phone = dis.readUTF();
			return ((P001)P.getInstance("001")).mobileFindPwd(username, phone, ip);
		} else 
		if(act == ACT_USER_EMAIL_FIND_PWD){
			String username = dis.readUTF();
			String email = dis.readUTF();
			return ((P001)P.getInstance("001")).emailFindPwd(username, email, ip);
		} else 
		if(act == ACT_GET_CHANNLE_LIST) {
			JSONArray array = DBPool.getInst().pQueryS(ChannelBAC.tab_channel, null, "code").getJsonarr();
			return new ReturnValue(true, array.toString());
		}
		
		String sessionid = dis.readUTF();
		User user = UserBAC.getInstance().loadUser(sessionid);
		if(user == null){
			return new ReturnValue(false, "��δ��¼�ʺ�");
		}
		ReturnValue addReqRv = user.addReqing(act, time);
		//����ʷ���
		if(addReqRv.parameter==null && !addReqRv.success){
			return addReqRv;
		}
		processarr.add(user);
		//��ʷ���
		if(addReqRv.parameter!=null){
			return addReqRv;
		}
		int uid = user.uid;
		if(reqSqlStr!=null)reqSqlStr.add("userid", uid);
		if(act == ACT_USER_ACTIVATE){
			String code = dis.readUTF();
			return ActivateCodeBAC.getInstance().activate(user.channel, user.username, code, ip);
		} else 
		if(act == ACT_USER_LOGOUT){
			return UserBAC.getInstance().logout(uid, "HTTP�û�ע��");
		} else 
		if(act == ACT_SERVER_LIST){
			return ServerBAC.getInstance().getServerList(uid, user.channel);
		} else 
		if(act == ACT_PLAYER_RANNAME){
			int serverid = dis.readInt();
			byte amount = dis.readByte();
			return RanNameBAC.getInstance().getRandomName(serverid, amount);
		} else 
		if(act == ACT_GET_CARDVALUE) {
			String cardNum = dis.readUTF();			
			return ChargeOrderBAC.getInstance().getCardValue(cardNum);
		} else 
		if(act == ACT_CREATE_ORDER)
		{
			String channel = dis.readUTF();
			String extend = dis.readUTF();
			LogBAC.logout("charge/"+channel, "�յ������¶�������extend="+extend);
			return ChargeOrderBAC.getInstance().getChargeOrderno(channel,extend);
		} else 
		{
			return new ReturnValue(false, "��Ч����");
		}
	}
}
