package com.moonic.bac;

import server.config.ServerConfig;

import com.ehc.dbc.BaseActCtrl;

/**
 * ���ʷ������쳣��־
 * @author John
 */
public class AccServerExcBAC extends BaseActCtrl {
	public static String tbName = "tab_access_server_exc_log";
	
	/**
	 * ����
	 */
	public AccServerExcBAC() {
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase_Log());
	}
	
	//--------------��̬��---------------
	
	private static AccServerExcBAC instance = new AccServerExcBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static AccServerExcBAC getInstance() {
		return instance;
	}
}
