package com.moonic.platform;

import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.UserBAC;
import com.moonic.mgr.PookNet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.MD5;
import com.moonic.util.MyTools;
import com.moonic.util.NetFormSender;

/**
 * ���˳���
 * @author 
 */
public class P001 extends P {
	
	/**
	 * �ֻ��һ�����
	 */
	public ReturnValue mobileFindPwd(String username, String phone,String ip) {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet dataRs = dbHelper.query(UserBAC.tab_user, "pook", "username='"+username+"' and platform='001'");
			if(!dataRs.next()){
				BACException.throwInstance("�Ҳ����û���"+username);
			}
			String tstamp = String.valueOf(System.currentTimeMillis());
			StringBuffer ticketSb = new StringBuffer();
			ticketSb.append(username);
			ticketSb.append("_");
			ticketSb.append(phone);
			ticketSb.append("_");
			ticketSb.append(tstamp);
			ticketSb.append("_");
			ticketSb.append(ip);
			ticketSb.append("_");						
			ticketSb.append(PookNet.screctKey);
			dbHelper.closeConnection();
			NetFormSender sender = new NetFormSender(PookNet.mobilefindpwd_do);
			sender.addParameter("userName", username);
			sender.addParameter("mobilePhone", phone);
			sender.addParameter("tstamp", tstamp);
			sender.addParameter("ipString", ip);
			sender.addParameter("ticket", MD5.encode(ticketSb.toString()));
			sender.send().check();
			boolean result = false;
			String returnStr = null;
			if(sender.rv.success) {
				JSONObject resultJson = new JSONObject(sender.rv.info);
				String resultStr = resultJson.optString("result");
				String message = resultJson.optString("message");
				result = resultStr.equals("S");
				returnStr = message;
			} else {
				returnStr = sender.rv.info;
			}
			return new ReturnValue(result, returnStr);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �����һ�����
	 */
	public ReturnValue emailFindPwd(String username, String email,String ip){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			ResultSet dataRs = dbHelper.query(UserBAC.tab_user, "pook,channel", "username='"+username+"' and platform='001'");
			if(!dataRs.next()){
				BACException.throwInstance("�Ҳ����û���"+username);
			}
			String tstamp = String.valueOf(System.currentTimeMillis());
			StringBuffer ticketSb = new StringBuffer();
			ticketSb.append(username);
			ticketSb.append("_");
			ticketSb.append(email);
			ticketSb.append("_");
			ticketSb.append(tstamp);
			ticketSb.append("_");			
			ticketSb.append(ip);
			ticketSb.append("_");
			ticketSb.append(PookNet.screctKey);
			dbHelper.closeConnection();
			NetFormSender sender = new NetFormSender(PookNet.emailfindpwd_do);
			sender.addParameter("userName", username);
			sender.addParameter("email", email);
			sender.addParameter("tstamp", tstamp);
			sender.addParameter("ipString", ip);
			sender.addParameter("ticket", MD5.encode(ticketSb.toString()));
			sender.send().check();
			boolean result = false;
			String returnStr = null;
			if(sender.rv.success) {
				JSONObject resultJson = new JSONObject(sender.rv.info);
				String resultStr = resultJson.optString("result");
				String message = resultJson.optString("message");
				result = resultStr.equals("S");
				returnStr = message;
			} else {
				returnStr = sender.rv.info;
			}
			return new ReturnValue(result, returnStr);
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	public void register(DBHelper dbHelper, String username, String password, String rePassword, String ip, String channel, JSONArray logdata) throws Exception {
		username = username.toLowerCase(); //�����û���ǿ��תСд
		NetFormSender sender = new NetFormSender(PookNet.register_do);
		sender.addParameter("rUser.agentId", "094");
		//sender.addParameter("rUser.agentId", "10490912"); //�´�ά��ʱ����
		sender.addParameter("rUser.userName", username);
		sender.addParameter("rUser.password", password);
		sender.addParameter("rUser.rePassword", rePassword);
		sender.addParameter("rUser.validCode", "webLobby");
		sender.addParameter("ipString", ip);
		StringBuffer ticket = new StringBuffer();
		ticket.append("094");
		ticket.append("_");
		ticket.append(username);
		ticket.append("_");
		ticket.append(password);
		ticket.append("_");
		ticket.append(password);
		ticket.append("_");
		ticket.append("webLobby");
		ticket.append("_");
		ticket.append(ip);
		ticket.append("_");
		ticket.append(PookNet.screctKey);
		sender.addParameter("ticket", MD5.encode(ticket.toString()));
		
		sender.send().check();
		//System.out.println("ע�᷵��"+sender.rv.info);
		JSONObject pokerobj = new JSONObject(sender.rv.info);
		String result = pokerobj.getString("result");
		if(result.equals("S")){
		} else 
		if(result.equals("E")){
			BACException.throwInstance(pokerobj.getString("message"));
		} else 
		{
			BACException.throwInstance("����ʧ��");
		}
		UserBAC.getInstance().insert(dbHelper, username, "", channel, platform, ip, logdata);
	}

	public JSONObject login(String channel, String extend, String username, String password, String ip, String imei, String mac, int loginport, SqlString userSqlStr) throws Exception {
		NetFormSender sender = null;
		if(channel.equals("042")){
			String tstamp = String.valueOf(System.currentTimeMillis());
			sender = new NetFormSender(PookNet.login042_do);
			sender.addParameter("gameId",20);
			sender.addParameter("distinctId",1);
			sender.addParameter("userId",username);//USERNAMEʵ��ΪuserId
			sender.addParameter("tstamp",tstamp);
			
			StringBuffer uattest = new StringBuffer();
			uattest.append("userId");
			uattest.append(username);
			uattest.append("time");
			uattest.append(tstamp);
			uattest.append("token");
			uattest.append(password);
			uattest.append(PookNet.key_53wan);
			sender.addParameter("uattest",MD5.encode(uattest.toString()));
			
			sender.addParameter("userToken",password);//PASSWORDʵ��Ϊtoken
		} else {
			username = username.toLowerCase(); //�����û���ǿ��תСд
			//+��=dsfesdffeasd54f64*FEDF::DFEsdf;eWEJDGLURYD>FJE��
			//String md5password = MD5.encode(MD5.encode(password)+"=dsfesdffeasd54f64*FEDF::DFEsdf;eWEJDGLURYD>FJE").toUpperCase();
			String md5password = MD5.encode(password).toUpperCase();
			//NetFormSender sender = new NetFormSender("http://www.pook.com/commLogin.do");
			sender = new NetFormSender(PookNet.login_do);
			//System.out.println("��"+NetFormSender.login_do+"���͵�¼��֤����,uname="+username+"&upwd="+md5password+"&loginType=13");
			String loginType="13";
			sender.addParameter("uname",username);
			sender.addParameter("upwd",md5password);
			sender.addParameter("loginType",loginType);
			sender.addParameter("imeiString",imei);
			sender.addParameter("macString",mac);
			
			sender.addParameter("ipString",ip);
			
			StringBuffer ticket = new StringBuffer();
			ticket.append(username);
			ticket.append("_");
			ticket.append(md5password);
			ticket.append("_");
			ticket.append(loginType);
			ticket.append("_");			
			ticket.append(mac);
			ticket.append("_");
			ticket.append(imei);
			ticket.append("_");			
			ticket.append(ip);
			ticket.append("_");
			ticket.append(PookNet.screctKey);
			sender.addParameter("ticket", MD5.encode(ticket.toString()));
		}
		
		long t1 = System.currentTimeMillis();
		//System.out.println("IP="+ip+",username="+username+"���Ե�¼");
		sender.send().check();
		
		long t2 = System.currentTimeMillis();
		if(t2-t1>500)
		{
			//LogBAC.logout("login/"+channel, username+"��¼���˺�ʱ="+(t2-t1));
		}
		//System.out.println("��¼��������="+sender.rv.info);
		//LogBAC.logout("login/"+channel, "��¼��������="+sender.rv.info);	
		if(!sender.rv.success){
			LogBAC.logout("login_error", "channel="+channel+",�û���֤ʧ��,���˷���:"+sender.rv.info);
			BACException.throwInstance("���˷���:"+sender.rv.info);
		}
		String info = sender.rv.info;
		JSONObject json = new JSONObject(info);
		if(channel.equals("042")){
			json.put("result", json.optString("key"));
			json.put("message", json.optString("msg"));
			json.put("userToken", password);
			json.put("userId", username);
			json.put("userName", json.optString("msg"));
		}
		String result=json.optString("result");
		String message=json.optString("message");
		String userToken=json.optString("userToken");
		String userId = json.optString("userId");
		username = json.optString("userName");
		if(!result.equals("S")){
			BACException.throwInstance("���˷���:"+message);
		}
		if(channel.equals("042")){
			JSONObject returnobj = new JSONObject();
			returnobj.put("username", username);
			returnobj.put("channeldata", username);
			return returnobj;
		} else {
			JSONObject pookJson = new JSONObject();//��ȡ�û���չ��Ϣ
			pookJson.put("uid", userId);
			pookJson.put("token", userToken);
			JSONObject safeJson = getSafetyInfo(pookJson.toString(),ip);
			if(safeJson == null){
				BACException.throwInstance("���˷���:getSafetyInfo==null");
			}
			safeJson.put("username", username);
			userSqlStr.add("pook", safeJson.toString());
			userSqlStr.add("pookid", userId);
			String card = safeJson.getString("card");
			String mobile = safeJson.getString("mobile");
			String email = safeJson.getString("email");
			JSONArray channeldata = new JSONArray();
			channeldata.add(MyTools.getEncrypeStr(card, 6, card.length()-2));//�Ƿ��Ѱ����֤
			channeldata.add(MyTools.getEncrypeStr(mobile, mobile.length()/2, mobile.length()));//�Ƿ��Ѱ󶨵绰
			channeldata.add(MyTools.getEncrypeStr(email, 3, 6));//�Ƿ��Ѱ�����
			JSONObject returnobj = new JSONObject();
			returnobj.put("username", username);
			returnobj.put("channeldata", channeldata);
			return returnobj;
		}
	}

	public ReturnValue checkLogin(String username, String extend, String ip) throws Exception {
		DBHelper dbHelper = new DBHelper();
		JSONObject userJson = dbHelper.queryJsonObj(UserBAC.tab_user, "*", "username='"+username+"' and platform='"+platform+"'");
		if(userJson == null) {
			BACException.throwInstance("�û���������");
		}
		if(userJson.getInt("onlinestate")==0){
			BACException.throwInstance("��δ��¼");
		}
		return new ReturnValue(true, username);
	}
	
	/**
	 * ��ȡ�û���ȫ��Ϣ
	 */
	public JSONObject getSafetyInfo(String str,String ip) throws Exception {
		JSONObject pookObj = new JSONObject(str);
		String tstamp = String.valueOf(System.currentTimeMillis());
		StringBuffer ticket = new StringBuffer();
		ticket.append(pookObj.getInt("uid"));
		ticket.append("_");
		ticket.append(pookObj.getString("token"));
		ticket.append("_");
		ticket.append(tstamp);
		ticket.append("_");
		ticket.append(ip);
		ticket.append("_");	
		ticket.append(PookNet.screctKey);
		NetFormSender sender = new NetFormSender(PookNet.getsafety_do);
		sender.addParameter("userId", pookObj.getInt("uid"));
		sender.addParameter("userToken", pookObj.getString("token"));
		sender.addParameter("tstamp", tstamp);
		sender.addParameter("ipString", ip);
		sender.addParameter("ticket", MD5.encode(ticket.toString()));
		sender.send().check();
		
		JSONObject resultJson = new JSONObject(sender.rv.info);
		String result = resultJson.optString("result");
		String message = resultJson.optString("message");
		if(!result.equals("S")) {
			BACException.throwInstance("���˷��أ�"+message);
		}
		pookObj.put("card", resultJson.optString("cardNo"));
		pookObj.put("mobile", resultJson.optString("mobilePhone"));
		pookObj.put("email", resultJson.optString("email"));
		return pookObj;
	}
}
