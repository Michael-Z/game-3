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
 * �û�������
 * @author 
 */
public class UserServerBAC extends BaseActCtrl {
	public static String tab_user_server = "tab_user_server";
	
	/**
	 * ����
	 */
	public UserServerBAC() {
		super.setTbName(tab_user_server);
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
			String http=request.getParameter("http");

			FormXML formXML = new FormXML();
			formXML.add("name",name);
			formXML.add("http",http);

			if(id>0)
			{
				int count = getCount("http='"+http+"' and id <>"+id);
				if(count>0)
				{
						return new ReturnValue(false,"��ַ�ظ�");
				}
			}
			else
			{
				int count = getCount("http='"+http+"'");
				if(count>0)
				{
						return new ReturnValue(false,"��ַ�ظ�");
				}
			}
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
	
	private static UserServerBAC instance = new UserServerBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static UserServerBAC getInstance() {
		return instance;
	}
}
