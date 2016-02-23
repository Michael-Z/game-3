/*
 * Created on 2005-12-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ehc.system;


import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import server.config.LogBAC;
import server.config.ServerConfig;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.common.ToolFunc;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.dbc.DataXmlAdapter;
import com.ehc.xml.AimXML;
import com.moonic.util.DBHelper;



/**
 * �û��飨��ɫ���������
 *
 */
public class RoleBAC extends BaseActCtrl
{
    public static final String tbName = "tb_baRole";
	
	public RoleBAC()
	{		
		super.setTbName(tbName);	
		setDataBase(ServerConfig.getDataBase());
	}
	

    /**
     * �������ʱ���������Ψһ��
     * @param id int:role��id
     * @param roleName String: ����
     * @return ReturnValue:���ؽ��,true����������
     */
    public ReturnValue checkRoleId(int id,String roleName)
    {
    	DataXmlAdapter adapter = new DataXmlAdapter(ServerConfig.getDataBase());
    	
    	
    	AimXML xml=null;
      
    	String sql=null;
    	if(id==0)
        {
        	sql="select id from "+ tbName + " where roleName = '"+ roleName +"'";
        }
        else
        {
        	sql="select id from "+ tbName + " where id<> "+ id +" and roleName='"+ roleName +"'";
        }        	
        xml = adapter.getRsPageToXML(sql, 1, 1);
            
        if(xml!=null)
        {
        	return new ReturnValue(false,"�������Ѵ���");	
        }
        else
        {
        	return new ReturnValue(true,"��������ʹ��");
        }
       
    }
    
    /**
     * ȡ��ָ����ɫ��Ȩ�޶�������
     * @param roleId int:��ɫid
     * @return Perm[]:����Ȩ�޶�������
     */
    public Perm[] getPermissions(int roleId)
    {
    	RolePermissionBAC PermBAC = new RolePermissionBAC();
    	AimXML xml=PermBAC.getXMLObjs("roleId="+ roleId,"id");
    	Vector vc=new Vector();
    	if(xml!=null)
    	{
    		xml.openRs(RolePermissionBAC.tbName);
    		while(xml.next())
    		{
    			String moudleStr=xml.getRsValue("ModuleId");
    			String permStr=xml.getRsValue("Permission");
    			vc.add(new Perm(moudleStr,permStr));
    		}
    	}
    	if(vc.size()>0)
    	{
    		Perm[] perms=new Perm[vc.size()];
    		vc.toArray(perms);
    		return perms;
    	}
    	else
    	{    		
    		return null;
    	}    		
    }
    
    public ReturnValue save(PageContext pageContext)
	{				
    	HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();    	
	
		int id=ToolFunc.str2int(request.getParameter("id"));
		
		String roleName=request.getParameter("roleName");
		int roleType=ToolFunc.str2int(request.getParameter("roleType"));		
		
		//���Ψһ��
		ReturnValue rv = checkRoleId(id,roleName);
		if(!rv.success)
		{
			return rv;
		}
		DBHelper dbHelper = new DBHelper();
		
		try {
			dbHelper.openConnection();
			SqlString sqlS = new SqlString();
			
			sqlS.add("roleName",roleName);
			sqlS.add("roleType",roleType);
			sqlS.add("isEnable",1);
			if(id>0)
			{				
				dbHelper.update(tbName, sqlS, "id="+id);
			}
			else
			{				
				dbHelper.insert(tbName, sqlS);
				String opusername = (String)pageContext.getSession().getAttribute("username");
				TBLogParameter  parameter=TBLogParameter.getInstance();
				parameter.addParameter("note", "�����飺"+roleName);
				LogBAC.addLog(opusername,"Ȩ�޷���",parameter.toString(),IPAddressUtil.getIp(request));
			}
			return new ReturnValue(true,"����ɹ�");
		} catch (Exception e) {
			// TODO �Զ����� catch ��
			e.printStackTrace();
			return new ReturnValue(true,"����ʧ�ܣ�"+e.toString());
		}
		finally
		{
			dbHelper.closeConnection();
		}					
	}

}
