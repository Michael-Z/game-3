package com.ehc.system;

import java.util.Calendar;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.jspsmart.upload.SmartUpload;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTools;


public class IPDenyBAC extends BaseActCtrl
{	
	public static String tbName = "tb_ipdeny";	 
	private static IPDenyBAC self;	 
	  		
		public static IPDenyBAC getInstance()
		{						
			if(self==null)
			{
				self = new IPDenyBAC();
			}
			return self;
		}
		public IPDenyBAC()
		{			
			super.setTbName(tbName);
			setDataBase(ServerConfig.getDataBase());
		}
		public boolean addDenyIP(String username,String ip)
		{
			DBHelper dbHelper = new DBHelper();
			try
			{
				dbHelper.openConnection();
				SqlString sqlS = new SqlString();
				sqlS.add("username", username);
				sqlS.add("ip", ip);
				sqlS.addDateTime("savetime", Tools.getCurrentDateTimeStr());
				dbHelper.insert(tbName, sqlS);				
				return true;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			finally
			{
				dbHelper.closeConnection();
			}
		}
		public ReturnValue isHacker(String username,String ip)
		{
			DBHelper dbHelper = new DBHelper();
			try
			{
				dbHelper.openConnection();
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR_OF_DAY, -6);
				int successCount = dbHelper.queryCount("tb_log", "act='��¼�ɹ�' and username='"+username+"' and ip='"+ip+"' and savedate>="+MyTools.getTimeStr(cal.getTimeInMillis()));
				int failCount = dbHelper.queryCount("tb_log", "act='��¼ʧ��' and username='"+username+"' and ip='"+ip+"' and savedate>="+MyTools.getTimeStr(cal.getTimeInMillis()));
				//System.out.println("successCount="+successCount);
				//System.out.println("failCount="+failCount);
				//if(failCount>=4 && successCount==0)
				if(failCount>=50 && successCount==0)
				{
					//System.out.println("�������5��");
					return new ReturnValue(false,"�������50��");
				}
				return new ReturnValue(true);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return new ReturnValue(true);
			}
			finally
			{
				dbHelper.closeConnection();
			}
		}
		public boolean isDenyIP(String ip)
		{
			DBHelper dbHelper = new DBHelper();
			try {
				dbHelper.openConnection();
				boolean deny = dbHelper.queryExist(tbName, "ip='"+ip+"'");
				if(deny)
				{
					return true;
				}
				else
				{
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			finally
			{
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
				String ip=request.getParameter("ip");
				String savetime=request.getParameter("savetime");
				String username=request.getParameter("username");

				
				FormXML formXML = new FormXML();
				formXML.add("ip",ip);
				formXML.addDateTime("savetime",savetime);
				formXML.add("username",username);

				
				if(id>0)  //�޸�
				{	
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
				return new ReturnValue(false,e.getMessage());
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
	
}
