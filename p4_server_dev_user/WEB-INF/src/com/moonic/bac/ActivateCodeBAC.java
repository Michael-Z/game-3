package com.moonic.bac;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

public class ActivateCodeBAC extends BaseActCtrl//TODO ȥ��BAC
{
	public static String tbName = "TAB_ACTIVATE_CODE";
	
	private static ActivateCodeBAC self = new ActivateCodeBAC();
	
	private static Object syncLock = new Object();
	
	public static ActivateCodeBAC getInstance() {
		return self;
	}
	
	public ActivateCodeBAC()
	{
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}
	
	//��ϲ���ɹ���ȡ�����룺GHFDN,��½�ڴ�����OL�ͻ��˼�����Ϸ�ʺţ������������н����������ע�ٷ���̳��www.xianmobbs.com�����˳��С�

	/**
	 * �μӼ�����齱
	 * @param userName �û���
	 * @return
	 */
	public ReturnValue joinLottery(String userName)
	{
		JSONObject returnJsonObj = new JSONObject();
		
		
		//���γ齱��Ѿ����������ྫ�����ע���ڴ�����OL���ٷ���̳
		returnJsonObj.put("note", "���γ齱��Ѿ����������ྫ�����ע���ڴ�����OL���ٷ���̳");
		return new ReturnValue(false,returnJsonObj.toString());
		/*
		if(userName==null || userName.equals(""))
		{
			returnJsonObj.put("note", "��Ч���û�����");
			return new ReturnValue(false,returnJsonObj.toString());
		}
		synchronized (syncLock) 
		{			
			if(checkActivateTmp(null,userName))
			{
				int lottery = getIntValue("lottery", "ACTIVATE_USER='"+userName+"'");
				if(lottery==1)
				{
					returnJsonObj.put("note", "���Ѳμӹ����γ齱��������ظ��ύ��");	
				}
				else				
				{
					update("lottery=1,lottery_time="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr()), "ACTIVATE_USER='"+userName+"'");
					returnJsonObj.put("note", "��ϲ����Ϸ�ʺ��ύ�ɹ��������������ע�ٷ���̳���ٷ�΢����");	
				}
				
				return new ReturnValue(true,returnJsonObj.toString());
			}
			else
			{
				returnJsonObj.put("note", "����δ�����ʺţ�����������Ϸ������Ϸ�м����ʺţ����ܲμӱ��γ齱���");
				return new ReturnValue(false,returnJsonObj.toString());
			}
		}	*/	
	}
	
	/**
	 * �û�����
	 * @param channel ����
	 * @param userName �û���־��
	 * @param code ������
	 * @param ip 
	 */
	public ReturnValue activate(String channel,String userName,String code,String ip) 
	{
		DBHelper dbHelper = new DBHelper();
		try {
			//�жϸ��û��Ƿ��Ѽ���
			if(checkActivate(channel,userName)) {
				return new ReturnValue(true,"���û��Ѿ�������ˡ�");
			}
			//�жϼ������Ƿ����
			JSONObject jsonObj = getJsonObj("code='"+code.toUpperCase()+"'");
			if(jsonObj==null) {
				return new ReturnValue(false,"�ü����벻���ڡ�");
			}
			//�жϸü������Ƿ���ʹ�ù�
			if(jsonObj.optInt("ACTIVATED")==1) {
				return new ReturnValue(false,"�ü������Ѿ������������ʹ���ˡ�");
			}
			//�жϼ������Ƿ񷢲���
			if(jsonObj.optInt("publish")==0) {
				return new ReturnValue(false,"δ�ַ����Ĳ����ü����롣");
			}
			//�жϼ������Ƿ���Ч��
			String startTime = jsonObj.optString("startTime");
			if(startTime!=null && !startTime.equals("")) {
				Date currentDate = Tools.str2date(Tools.getCurrentDateTimeStr());
				Date codeStartDate = Tools.str2date(startTime);
				if(currentDate.before(codeStartDate)) {
					return new ReturnValue(false,"�ü�����Ҫ��"+Tools.strdate2str(startTime,"yyyy-MM-dd")+"����ʹ�á�");
				}
			}
			DBPaRs channelRs = DBPool.getInst().pQueryA(ChannelBAC.tab_channel, "code="+channel);
			SqlString sqlS = new SqlString();
			sqlS.add("ACTIVATED", 1);
			sqlS.add("ACTIVATE_USER", userName);
			sqlS.addDateTime("ACTIVATE_TIME", Tools.getCurrentDateTimeStr());
			//tab_activate_code�е�channelʵ������Ϊplatform
			sqlS.add("CHANNEL", channelRs.getString("platform"));
			sqlS.add("ip", ip);			
			dbHelper.update(tbName, sqlS, "code='"+code.toUpperCase()+"'");
			//update(sqlS.updateString(), "code='"+code.toUpperCase()+"'");
			return new ReturnValue(true);			
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
		finally
		{	
			dbHelper.closeConnection();
		}
	}
	
	private static String[] exemptActivateChannel = {"003"};
	
	/**
	 * ����û��Ƿ��Ѽ���
	 * @param channel ����
	 * @param userName �û���־��
	 */
	public boolean checkActivate(String channel, String userName) throws Exception {
		if(!ConfigBAC.getBoolean("needactivate")){
			return true;
		}
		if(Tools.contain(exemptActivateChannel, channel)){
			return true;
		}
		DBPaRs channelRs = DBPool.getInst().pQueryA(ChannelBAC.tab_channel, "code="+channel);
		//tab_activate_code�е�channelʵ������Ϊplatform
		return getJsonObj("channel='"+channelRs.getString("platform")+"' and activate_user='"+userName+"' and activated=1")!=null;
	}
	
	public ReturnValue haveActivateCode()
	{
		JSONObject returnJsonObj = new JSONObject();
		int count = getCount("publish=0");
		if(count>0)
		{
			returnJsonObj.put("note", "");			
			return new ReturnValue(true,returnJsonObj.toString());
		}
		else
		{
			returnJsonObj.put("note", "���γ齱��Ѿ����������ྫ�����ע���ڴ�����OL���ٷ���վhttp://kd.pook.com/��");			
			return new ReturnValue(false,returnJsonObj.toString());
		}
		
		/*returnJsonObj.put("note", "����������ȡ�꣬��ȴ��ٷ����·����¼����롣");			
		return new ReturnValue(false,returnJsonObj.toString());*/
	}
	
	public ReturnValue getActivateCode(String phone,String ip,int method,String publishUser)
	{
		//System.out.println(phone+"����ȡ������");
		JSONObject returnJsonObj = new JSONObject();
		DBHelper dbHelper = new DBHelper();
		synchronized (syncLock) 
		{
			try
			{
				//System.out.println("��ȡ������ phone="+phone);
				if(phone==null || phone.equals(""))
				{
					returnJsonObj.put("note", "���ṩ�ֻ��š�");
					//writeLog(Tools.getCurrentDateTimeStr()+ "�����ṩ�ֻ���ip="+ip);
					return new ReturnValue(false,returnJsonObj.toString());
				}
				//�����ֻ������Ƿ�����֤������ȡ��
				String[] codeInfo = getValues(new String[]{"code","activated","starttime"}, "publish=1 and phone='"+phone+"'");
				//System.out.println("codeInfo="+Tools.strArr2Str(codeInfo));
				if(codeInfo!=null)
				{
					//System.out.println("codeInfo[2]="+codeInfo[2]);
					if(codeInfo[1]!=null && codeInfo[1].equals("1")) //��ʹ�ù�������
					{			
						String msg = "�����ֻ���"+phone+"����ȡ�������룺"+codeInfo[0]+"���Ͽ��½�ڴ�����OL�ͻ��˼�����Ϸ�ʺŰɣ����ص�ַ��http://kd.pook.com";
						returnJsonObj.put("note", msg);
						LogBAC.logout("activate", "phone="+phone+",from="+method+",user="+publishUser+",���ţ�"+msg);
						//writeLog("�����ֻ�����"+phone+"��ʹ�ù�������"+codeInfo[0]+"������Ϸ�ʺţ���������뵽�ٷ���̳www.xianmobbs.com��ѯ��");
					}
					else	 //����ȡ������δ����
					{
						//System.out.println("����ȡ������δ����");
						int compare=0;
						String startdate = codeInfo[2];
						if(startdate!=null)
						{
							startdate = Tools.strdate2str(startdate, "yyyy-M-d");
							compare=Tools.compareStrDate(Tools.getCurrentDateTimeStr(),startdate);
						}
						else
						{
							compare=1;
						}
						if(compare>0) //�ѵ��ɼ�������
						{		
							String msg ="�����ֻ���"+phone+"����ȡ�������룺"+codeInfo[0]+"���Ͽ��½�ڴ�����OL�ͻ��˼�����Ϸ�ʺŰɣ����ص�ַ��http://kd.pook.com";
							returnJsonObj.put("note", msg);
							//writeLog("�����ֻ���"+phone+"����ȡ�������룺"+codeInfo[0]+"���Ͽ��½�ڴ�����OL�ͻ��˼�����Ϸ�ʺŰɣ����ص�ַ��wap.xm.pook.com");
							LogBAC.logout("activate", "phone="+phone+",from="+method+",user="+publishUser+",���ţ�"+msg);
						}
						else //δ���ɼ�������
						{	
							String msg ="�����ֻ���"+phone+"����ȡ��ԤԼ�����룺"+codeInfo[0]+"����Ҫ�ȵ�"+startdate+"�Ų��ܵ�½�ڴ�����OL�ͻ��˼����Լ�����Ϸ�ʺţ����ص�ַ��http://kd.pook.com";
							returnJsonObj.put("note", msg);
							LogBAC.logout("activate", "phone="+phone+",from="+method+",user="+publishUser+",���ţ�"+msg);
						}					
					}								
					return new ReturnValue(true,returnJsonObj.toString());
				}
				
				SqlString sqlStr = new SqlString();
				//����Ч���ڵ�
				sqlStr.add("activated", 0);
				sqlStr.add("PUBLISH", 0);
				sqlStr.addWhere("(STARTTIME is null or STARTTIME<="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr())+")");
				JSONObject jsonObj = getTopJsonList("code", 1, sqlStr.whereString(), "id ASC");
				//System.out.println("��ȡ����="+jsonObj.toString());
				if(jsonObj!=null)
				{
					JSONArray arr = jsonObj.optJSONArray("list");
					if(arr.length()>0)
					{
						JSONObject line = arr.optJSONObject(0);
						String code = line.optString("code");
						SqlString updateSqlS = new SqlString();
						updateSqlS.add("method", method);
						updateSqlS.add("phone", phone);
						updateSqlS.add("publish_user", publishUser);
						updateSqlS.add("publish", 1);
						updateSqlS.addDateTime("publish_time", Tools.getCurrentDateTimeStr());
						updateSqlS.add("ip", ip);
						dbHelper.update(tbName, updateSqlS, "code='"+code+"'");
						//update("method="+method+",phone='"+phone+"',publish_user='"+publishUser+"',PUBLISH=1,publish_time="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr())+",ip='"+ip+"'", "code='"+code+"'");
						//��ϲ���ɹ���ȡ�����룺GHFDN,��½�ڴ�����OL�ͻ��˼�����Ϸ�ʺţ������������н����������ע�ٷ���̳��www.xianmobbs.com�����˳��С�
						String msg = "��ϲ���ɹ���ȡ�����룺"+code+"����½�ڴ�����OL�ͻ��˼�����Ϸ�ʺţ������������н������ص�ַ��http://kd.pook.com";
						returnJsonObj.put("note", msg);
						//writeLog("�ֻ�����"+phone+":"+"��ϲ���ɹ���ȡ�����룺"+code+"����½�ڴ�����OL�ͻ��˼�����Ϸ�ʺţ������������н������ص�ַ��wap.xm.pook.com");
						LogBAC.logout("activate", "phone="+phone+",from="+method+",user="+publishUser+",���ţ�"+msg);
						return new ReturnValue(true,returnJsonObj.toString());
					}			
				}
				else //����ԤԼ��
				{
					sqlStr.clear();
					sqlStr.add("activated", 0);
					sqlStr.add("PUBLISH", 0);
					sqlStr.addWhere("(STARTTIME is not null and STARTTIME>"+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr())+")");
					jsonObj = getTopJsonList("code,STARTTIME", 1, sqlStr.whereString(), "id ASC");
					if(jsonObj!=null)
					{
						JSONArray arr = jsonObj.optJSONArray("list");
						if(arr.length()>0)
						{
							JSONObject line = arr.optJSONObject(0);
							String code = line.optString("code");
							String startdate = Tools.strdate2str(line.optString("STARTTIME"),"yyyy-M-d");
							SqlString updateSqlS = new SqlString();
							updateSqlS.add("method", method);
							updateSqlS.add("phone", phone);
							updateSqlS.add("publish_user", publishUser);
							updateSqlS.add("publish", 1);
							updateSqlS.addDateTime("publish_time", Tools.getCurrentDateTimeStr());
							updateSqlS.add("ip", ip);
							dbHelper.update(tbName, updateSqlS, "code='"+code+"'");
							
							//update("method="+method+",phone='"+phone+"',publish_user='"+publishUser+"',PUBLISH=1,publish_time="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr())+",ip='"+ip+"'", "code='"+code+"'");
							//returnJsonObj.put("code", code);
							//returnJsonObj.put("startdate", startdate);
							String msg = "���ѳɹ���ȡԤԼ�����룺"+code+"����Ҫ�ȵ�"+startdate+"�Ų��ܵ�½�ڴ�����OL�ͻ��˼�����Ϸ�ʺš����ص�ַ��http://kd.pook.com";
							returnJsonObj.put("note", msg);
							//System.out.println("���ѳɹ���ȡԤԼ�����룺"+code+"�����Եȵ�"+startdate+"�ŵ�¼�ڴ�����OL��Ϸ�����Լ�����Ϸ�ʺš����ע�ٷ���̳��http://xianmobbs.0211.com/��");
							//writeLog("�ֻ�����"+phone+":"+"���ѳɹ���ȡԤԼ�����룺"+code+"����Ҫ�ȵ�"+startdate+"�Ų��ܵ�½�ڴ�����OL�ͻ��˼�����Ϸ�ʺš����ص�ַ��wap.xm.pook.com");
							LogBAC.logout("activate", "phone="+phone+",from="+method+",user="+publishUser+",���ţ�"+msg);
							
							return new ReturnValue(true,returnJsonObj.toString());
						}
					}
				}
				//һ�������Ѿ�����������4��9��11ʱ׼ʱ�����ڶ�������
				//returnJsonObj.put("note", "�������ѷַ����,��ȴ��´λ��ᣬ�������ע�ٷ���̳��www.xianmobbs.com ");
				//writeLog("�ֻ�����"+phone+":"+"�������ѷַ����,��ȴ��´λ��ᣬ�������ע�ٷ���̳��www.xianmobbs.com ");
				
				String msg = "���������ѷ��꣬���ע���ڴ�����OL���ٷ���վhttp://kd.pook.com";
				returnJsonObj.put("note", msg);
				//returnJsonObj.put("note", "���������Ѿ���������ȴ������������");
				LogBAC.logout("activate", "phone="+phone+",from="+method+",user="+publishUser+",ҳ����ʾ��"+msg);
				//writeLog("�ֻ�����"+phone+":"+"���γ齱��Ѿ����������ྫ�����ע���ڴ�����OL���ٷ���̳��");
				//System.out.println("�������ѷַ����,��ȴ��´λ��ᣬ���ȹ�ע�ڴ�����OL�ٷ���̳��http://xianmobbs.0211.com/��");
				//DataBase.setUseDebug(false);
				return new ReturnValue(false,returnJsonObj.toString());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				returnJsonObj.put("note",ex.toString());
				return new ReturnValue(false,returnJsonObj.toString());
			}
			finally
			{
				dbHelper.closeConnection();
			}
		}		
	}	
}
