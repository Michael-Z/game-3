/*
 * Created on 2005-12-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ehc.system;


import server.config.ServerConfig;

import com.ehc.dbc.BaseActCtrl;



/**
 * �û����Ȩ�޶�Ӧ��ϵ�������
 *
 */
public class RolePermissionBAC extends BaseActCtrl
{    
    public static String tbName = "tb_baRolePermission";
    
   
	public RolePermissionBAC()
	{
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}

}
