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
 * �������쳣�ʼ����͵�ַ
 * @author John
 */
public class ServerExcMailBAC extends BaseActCtrl {
	public static String tbName = "tab_server_exc_mail_addr";
	
	/**
	 * ����
	 */
	public ServerExcMailBAC() {
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}
	
	/**
	 * ���/����
	 */
	public ReturnValue save(PageContext pageContext) {
		SmartUpload smartUpload = new SmartUpload();
		smartUpload.setEncode("UTF-8");
		try {
			smartUpload.initialize(pageContext);
			smartUpload.upload();
			com.jspsmart.upload.Request request = smartUpload.getRequest();
			
			int id = Tools.str2int(request.getParameter("id"));
			String name=request.getParameter("name");
			String mailaddr=request.getParameter("mailaddr");

			FormXML formXML = new FormXML();
			formXML.add("name",name);
			formXML.add("mailaddr",mailaddr);
			
			if (id > 0) // �޸�
			{
				formXML.setAction(FormXML.ACTION_UPDATE);
				formXML.setWhereClause("id=" + id);
				ReturnValue rv = save(formXML);
				if (rv.success) {
					return new ReturnValue(true, "�޸ĳɹ�");
				} else {
					return new ReturnValue(false, "�޸�ʧ��");
				}
			} else // ���
			{
				formXML.setAction(FormXML.ACTION_INSERT);
				ReturnValue rv = save(formXML);
				if (rv.success) {
					return new ReturnValue(true, "����ɹ�");
				} else {
					return new ReturnValue(false, "����ʧ��");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.getMessage());
		}
	}
	
	/**
	 * ɾ����¼
	 */
	public ReturnValue del(PageContext pageContext) {
		ServletRequest req = pageContext.getRequest();
		int id = Tools.str2int(req.getParameter("id"));
		ReturnValue rv = super.del("id=" + id);
		return rv;
	}
	
	//--------------��̬��---------------
	
	private static ServerExcMailBAC instance = new ServerExcMailBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static ServerExcMailBAC getInstance() {
		return instance;
	}
}
