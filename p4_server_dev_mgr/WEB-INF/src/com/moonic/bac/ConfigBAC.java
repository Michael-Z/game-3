package com.moonic.bac;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.jspsmart.upload.SmartUpload;
import com.moonic.mgr.DBPoolMgr;
import com.moonic.mgr.DBPoolMgrListener;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ��Ϸ����
 * @author John
 */
public class ConfigBAC extends BaseActCtrl {
	public static String tb_config = "tb_config";
	
	/**
	 * ����
	 */
	public ConfigBAC() {
		super.setTbName(tb_config);
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
			String name = request.getParameter("name");
			String value = request.getParameter("value");
			String memo = request.getParameter("memo");

			FormXML formXML = new FormXML();
			formXML.add("name", name);
			formXML.add("value", value);
			formXML.add("memo", memo);

			if (id > 0) {
				int count = getCount("name='" + name + "' and id <>" + id);
				if (count > 0) {
					return new ReturnValue(false, "�������ظ�");
				}
			} else {
				int count = getCount("name='" + name + "'");
				if (count > 0) {
					return new ReturnValue(false, "�������ظ�");
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
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ɾ��
	 */
	public ReturnValue del(PageContext pageContext) {
		ServletRequest req = pageContext.getRequest();
		int id = Tools.str2int(req.getParameter("id"));
		ReturnValue rv = super.del("id=" + id);
		return rv;
	}
	
	/**
	 * ����ֵ
	 */
	public void setValue(String name, String value, DBPoolMgrListener listener){
		setValue(new String[]{name}, new String[]{value}, listener);
	}
	
	/**
	 * ����ֵ
	 */
	public void setValue(String[] names, String[] values, DBPoolMgrListener listener){
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			for(int i = 0; i < names.length; i++){
				SqlString sqlStr = new SqlString();
				sqlStr.add("value", values[i]);
				dbHelper.update(tb_config, sqlStr, "name='"+names[i]+"'");
				DBPoolMgr.getInstance().addClearTablePoolTask(tb_config, i>=names.length-1?listener:null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//------------------��̬��-------------------
	
	/**
	 * ��ȡֵ
	 */
	public static boolean getBoolean(String name) {
		return Tools.str2boolean(getString(name));
	}
	
	/**
	 * ��ȡֵ
	 */
	public static int getInt(String name) {
		return Tools.str2int(getString(name));
	}
	
	/**
	 * ��ȡֵ
	 */
	public static String getString(String name) {
		try {
			DBPaRs confRs = DBPool.getInst().pQueryA(tb_config, "name='"+name+"'");
			if(!confRs.exist()){
				BACException.throwAndOutInstance("ȱ��CONFIG������"+name);
			}
			return confRs.getString("value");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static ConfigBAC instance = new ConfigBAC();
	
	/**
	 * ��ȡ��������
	 */
	public static ConfigBAC getInstance() {
		return instance;
	}
}
