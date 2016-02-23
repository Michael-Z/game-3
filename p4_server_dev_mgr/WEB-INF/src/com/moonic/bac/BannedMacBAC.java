package com.moonic.bac;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.jspsmart.upload.SmartUpload;

/**
 * ��MAC
 * @author John
 */
public class BannedMacBAC extends BaseActCtrl {	
	public static String tbName = "tab_banned_mac";	 
	
	public BannedMacBAC() {			
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}
	
	public ReturnValue save(PageContext pageContext) {
		SmartUpload smartUpload = new SmartUpload();
		smartUpload.setEncode("UTF-8");
		try {
			smartUpload.initialize(pageContext);
			smartUpload.upload();
			com.jspsmart.upload.Request request = smartUpload.getRequest();				
			
			int id=Tools.str2int(request.getParameter("id"));
			String mac=request.getParameter("mac");
			String imei=request.getParameter("imei");
			String savetime=request.getParameter("savetime");
			String reason=request.getParameter("reason");
			
			HttpSession session = pageContext.getSession();
			JSONObject userObj=(JSONObject)session.getAttribute("user");
			String opuser = userObj.optString("username");
			if(mac!=null)
			{
				mac = mac.toUpperCase();
			}
			
			FormXML formXML = new FormXML();
			formXML.add("mac",mac);
			formXML.add("imei",imei);
			formXML.add("reason",reason);
			formXML.add("opuser",opuser);
			formXML.addDateTime("savetime",savetime);
			

			if(id>0)  //�޸�
			{	
				if(mac!=null && !mac.equals(""))
				{
					if(getCount("mac='"+mac+"' and id<>"+id)>0)
					{
						return new ReturnValue(false,"mac��ַ"+mac+"�Ѵ���");						
					}
				}
				if(imei!=null && !imei.equals(""))
				{
					if(getCount("imei='"+imei+"' and id<>"+id)>0)
					{
						return new ReturnValue(false,"imei����"+imei+"�Ѵ���");	
					}
				}
				
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
				//���mac��imei�Ƿ��Ѵ���
				if(mac!=null && !mac.equals(""))
				{
					if(getCount("mac='"+mac+"'")>0)
					{
						return new ReturnValue(false,"mac��ַ"+mac+"�Ѵ���");						
					}
				}
				if(imei!=null && !imei.equals(""))
				{
					if(getCount("imei='"+imei+"'")>0)
					{
						return new ReturnValue(false,"imei����"+imei+"�Ѵ���");						
					}
				}
				
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
	
	public ReturnValue del(PageContext pageContext) {
		ServletRequest req = pageContext.getRequest();
		int id = Tools.str2int(req.getParameter("id"));
		ReturnValue rv = super.del("id="+ id);			
		return rv;
	}
	
	//--------------��̬��--------------
	
	private static BannedMacBAC instance = new BannedMacBAC();	 
		
	public static BannedMacBAC getInstance() {
		return instance;
	}
}
