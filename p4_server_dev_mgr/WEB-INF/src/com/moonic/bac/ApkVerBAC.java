package com.moonic.bac;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.jspsmart.upload.SmartUpload;

/**
 * APK�汾
 * @author John
 */
public class ApkVerBAC extends BaseActCtrl{
	public static String tbName = "TAB_VERSION_APK";
	private static ApkVerBAC self;
	  		
	public static ApkVerBAC getInstance()
	{
		if(self==null)
		{
			self = new ApkVerBAC();
		}			
		return self;
	}
	
	public ApkVerBAC()
	{			
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
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
			String version=request.getParameter("version");
			String updfile=request.getParameter("updfile");
			String savetime=request.getParameter("savetime");
			int mustupdate=Tools.str2int(request.getParameter("mustupdate"));
			int filesize=Tools.str2int(request.getParameter("filesize"));
			String channel=request.getParameter("channel");
			String subfolder="";
			String platform=request.getParameter("platform");
			String crc=request.getParameter("crc");			
			if(crc!=null)crc=crc.toUpperCase();
			
			FormXML formXML = new FormXML();
			formXML.add("version",version);
			formXML.add("updfile",updfile);
			formXML.addDateTime("savetime",savetime);
			formXML.add("mustupdate",mustupdate);
			formXML.add("filesize",filesize);
			formXML.add("channel",channel);
			formXML.add("subfolder",subfolder);
			formXML.add("platform",platform);
			formXML.add("crc",crc);			

			if(id>0)
			{
				int count = getCount("version='"+version+"' and platform='"+platform+"' and id <>"+id);
				if(count>0)
				{
						return new ReturnValue(false,"�汾�ظ�");
				}
			}
			else
			{
				int count = getCount("version='"+version+"' and platform='"+platform+"'");
				if(count>0)
				{
						return new ReturnValue(false,"�汾�ظ�");
				}
			}
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
			return new ReturnValue(false,e.toString());
		} 		
	}
	
	public ReturnValue del(PageContext pageContext)
	{	
		ServletRequest req = pageContext.getRequest();
		int id = Tools.str2int(req.getParameter("id"));
		ReturnValue rv = super.del("id="+ id);			
		return rv;
	}
}
