package com.moonic.bac;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;

/**
 * ��Ʒ�һ���
 * @author huangyan
 *
 */
public class ExchangeCodeBAC extends BaseActCtrl
{
	public static String tbName = "TAB_EXCHANGE_CODE";
	
	private static ExchangeCodeBAC self = new ExchangeCodeBAC();
	
	private static Object syncLock = new Object();
	
	public static ExchangeCodeBAC getInstance() {
		return self;
	}
	
	public ExchangeCodeBAC()
	{
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}	
	
	/**
	 * ���öһ���
	 * @param code
	 * @return
	 */
	public ReturnValue exchangeCode(String code)
	{			
		String[] codeInfo = getValues(new String[]{"published","exchanged"}, "code='"+code+"'");
		if(codeInfo!=null)
		{
			if(codeInfo[0].equals("1"))
			{
				if(codeInfo[1].equals("0"))
				{
					update("exchanged=1,exchange_time="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr()), "code='"+code+"'");
					return new ReturnValue(true);
				}
				else
				{
					JSONObject returnJsonObj = new JSONObject();
					returnJsonObj.put("note", code+"�Ѷһ�����");		
					return new ReturnValue(false,returnJsonObj.toString());
				}
			}
			else
			{
				JSONObject returnJsonObj = new JSONObject();
				returnJsonObj.put("note", "δ�ַ��Ķһ���");				
				return new ReturnValue(false,returnJsonObj.toString());
			}
		}
		else
		{
			JSONObject returnJsonObj = new JSONObject();
			returnJsonObj.put("note", "�һ��벻����");		
			return new ReturnValue(false,returnJsonObj.toString());
		}				
	}
	/**
	 * ����һ���
	 * @param code
	 * @return
	 */
	public ReturnValue checkCode(String code,String phone)
	{			
		String[] codeInfo = getValues(new String[]{"published","exchanged"}, "code='"+code+"' and phone='"+phone+"'");
		if(codeInfo!=null)
		{
			if(codeInfo[0].equals("1"))
			{
				/*if(codeInfo[1].equals("0"))
				{
					update("exchanged=1,exchange_time="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr()), "code='"+code+"'");
				}*/
				return new ReturnValue(true);
			}
			else
			{
				JSONObject returnJsonObj = new JSONObject();
				returnJsonObj.put("note", "δ�ַ��Ķһ���");				
				return new ReturnValue(false,returnJsonObj.toString());
			}
		}
		else
		{
			JSONObject returnJsonObj = new JSONObject();
			returnJsonObj.put("note", "�ֻ�������һ��벻ƥ��");		
			return new ReturnValue(false,returnJsonObj.toString());
		}				
	}
	
	
	public ReturnValue getExchangeCode(String phone)
	{	
		JSONObject returnJsonObj = new JSONObject();
		synchronized (syncLock) 
		{
			if(phone==null || phone.equals(""))
			{
				returnJsonObj.put("note", "���ṩ�ֻ��š�");
				return new ReturnValue(false,returnJsonObj.toString());
			}
			if(phone.length()!=11)
			{
				returnJsonObj.put("note", "�ֻ��ű���11λ");
				return new ReturnValue(false,returnJsonObj.toString());
			}
			//�����ֻ������Ƿ�����֤������ȡ��
			String[] codeInfo = getValues(new String[]{"code","exchanged"}, "published=1 and phone='"+phone+"'");
			if(codeInfo!=null)
			{				
				if(codeInfo[1]!=null && codeInfo[1].equals("1")) //�Ѷһ���
				{			
					String msg = "�����ֻ�����"+phone+"�Ѷһ�����Ʒ,�����ظ���ȡ";
					returnJsonObj.put("note", msg);
					LogBAC.logout("exchange", "���ţ�"+msg);
					return new ReturnValue(false,returnJsonObj.toString());					
				}
				else	 //����ȡ������δ�һ�
				{		
					String msg = "���Ѿ���ȡ�������ܶһ���"+codeInfo[0]+",���ע�ڴ����޹ٷ�΢��kdhsol������·��һ�����ȡ��ť����ɶҽ���Ϣ��";					
					returnJsonObj.put("note", msg);
					LogBAC.logout("exchange", "���ţ�"+msg);
					return new ReturnValue(true,returnJsonObj.toString());		
				}
			}
			
			SqlString sqlStr = new SqlString();
			//�ҿ������ڵ�
			sqlStr.add("exchanged", 0);
			sqlStr.add("published", 0);
			sqlStr.addWhere("(open_date is null or (to_char(sysdate,'yyyy')= to_char(open_date,'yyyy') and to_char(sysdate,'mm')= to_char(open_date,'mm') and to_char(sysdate,'dd')=to_char(open_date,'dd')))");
			JSONObject jsonObj = getTopJsonList("code", 1, sqlStr.whereString(), "id ASC");
			
			if(jsonObj!=null)
			{
				JSONArray arr = jsonObj.optJSONArray("list");
				if(arr.length()>0)
				{
					JSONObject line = arr.optJSONObject(0);
					String code = line.optString("code");
					update("phone='"+phone+"',PUBLISHED=1,publish_time="+Tools.getOracleDateTimeStr(Tools.getCurrentDateTimeStr()), "code='"+code+"'");
					String msg = "��ϲ���������ܶһ���Ϊ"+code+",���ע�ڴ����޹ٷ�΢��kdhsol������·��һ�����ȡ��ť����ɶҽ���Ϣ��";
					returnJsonObj.put("note", msg);
					LogBAC.logout("activate", "phone="+phone+",���ţ�"+msg);
					return new ReturnValue(true,returnJsonObj.toString());
				}
			}
			
			String msg = null;	
			
			if(dateAfter("2014-8-3")) //���һ��
			{
				msg = "��ϧ���������Ѿ�������������,��ע�ٷ�΢��kdhsol�����и��ྫ�ʻŶ��";	
			}	
			else
			{
				msg = "�͹٣������Ѿ�����⣬���ո��硣";	
			}
			returnJsonObj.put("note", msg);
			LogBAC.logout("exchange", "���ţ�"+msg);
			return new ReturnValue(false,returnJsonObj.toString());
		}		
	}
	private boolean dateAfter(String compareDateStr)
	{
		if(compareDateStr==null || compareDateStr.equals(""))return false;
		Date compareDate = Tools.str2date(compareDateStr);
		Date now = new Date();
		Calendar cal = Calendar.getInstance();		
		cal.setTime(compareDate);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(now);
		if(cal2.after(cal))
		{
			return true;			
		}
		else
		{
			return false;
		}			
	}
}
