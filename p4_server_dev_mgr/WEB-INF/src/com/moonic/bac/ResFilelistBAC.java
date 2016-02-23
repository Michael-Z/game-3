package com.moonic.bac;

import javax.servlet.*;
import javax.servlet.jsp.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.sql.ResultSet;

import server.common.*;
import server.config.*;
import com.ehc.common.*;
import com.ehc.dbc.*;
import com.ehc.xml.*;
import com.jspsmart.upload.*;
import com.moonic.mgr.DBPoolMgr;
import com.moonic.mgr.LockStor;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTools;
import com.ehc.common.*;

import conf.Conf;


public class ResFilelistBAC extends BaseActCtrl
{	
	public static String tbName = "tab_version_filelist";	 
	private static ResFilelistBAC self;	 
	
	  		
	public static ResFilelistBAC getInstance()
	{						
		if(self==null)
		{
			self = new ResFilelistBAC();
		}
		return self;
	}
	public ResFilelistBAC()
	{			
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}

	public ReturnValue save(PageContext pageContext)
	{
		ServletRequest request = pageContext.getRequest();
		int id=Tools.str2int(request.getParameter("id"));
		int platform=Tools.str2int(request.getParameter("platform"));
		String crc=request.getParameter("crc");				
		int enable=Tools.str2int(request.getParameter("enable"));
		String savetime=request.getParameter("savetime");			
		String subfolder=request.getParameter("subfolder");
	
		DBHelper dbhelper = new DBHelper();
		SqlString sqlStr = new SqlString();
		sqlStr.add("platform", platform);
		sqlStr.add("crc", crc.toUpperCase());	
		sqlStr.add("enable", enable);		
		sqlStr.add("subfolder", subfolder);
		sqlStr.addDateTime("savetime", savetime);
		
		if(id>0)  //�޸�
		{					
			try {
				dbhelper.openConnection();
				ResultSet rs = dbhelper.query(tbName, "id","platform="+platform+" and id<>"+id);
				if(rs!=null && rs.next())
				{
					return new ReturnValue(false,"��ƽ̨����Դ�б�CRC�Ѵ���");
				}				
				dbhelper.update(tbName, sqlStr, "id="+id);
				DBPoolMgr.getInstance().addClearTablePoolTask(tbName, null);
				return new ReturnValue(true,"�޸ĳɹ�");
			} catch (Exception e) {
				e.printStackTrace();
				return new ReturnValue(false,"�޸�ʧ��");
			} finally {
				dbhelper.closeConnection();
			}								
		}
		else  //���
		{
			try {
				dbhelper.openConnection();
				ResultSet rs = dbhelper.query(tbName, "id","platform="+platform);
				if(rs!=null && rs.next())
				{
					return new ReturnValue(false,"��ƽ̨����Դ�б�CRC�Ѵ���");
				}					
				dbhelper.insert(tbName, sqlStr);	
				DBPoolMgr.getInstance().addClearTablePoolTask(tbName, null);
				return new ReturnValue(true,"����ɹ�");
			} catch (Exception e) {
				e.printStackTrace();
				return new ReturnValue(false,"����ʧ��");
			} finally {
				dbhelper.closeConnection();
			}						
		}
				
	}
	public ReturnValue del(PageContext pageContext)
	{	
		ServletRequest req = pageContext.getRequest();
		int id = Tools.str2int(req.getParameter("id"));			
		
		ReturnValue rv = super.del("id="+ id);		
		//todo ɾ�������������еļ�¼
		return rv;
	}
	public static String getPlatformNameByNum(int num)
	{
		if(num==1)
		{
			return "��׿("+num+")";
		}
		else
		if(num==2)
		{
			return "IOS("+num+")";
		}			
		else
		if(num==3)
		{
			return "PC("+num+")";
		}else
		{
			return "δ֪("+num+")";
		}
	}
	public static String getPlatformFolderByPlatformNum(int platform)
	{
		if(platform==1)
		{
			return "android";			
		}
		else
		if(platform==2)
		{
			return "ios";
		}
		else
		if(platform==3)
		{
			return "pc";
		}
		else
		{
			return "android";		
		}			
	}
}
