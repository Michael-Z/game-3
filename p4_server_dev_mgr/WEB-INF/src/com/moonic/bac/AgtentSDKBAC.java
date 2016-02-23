package com.moonic.bac;

import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.moonic.util.MyTools;

/**
 * 
 * @author 
 */
public class AgtentSDKBAC extends BaseActCtrl {
	public static String tbName = "infull_agent_analyse_sdk";
	
	/**
	 * ����
	 */
	public AgtentSDKBAC() {
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase_Report());
	}
	
	/**
	 * ����
	 */
	public ReturnValue save(PageContext pageContext) {
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			
			int id = Tools.str2int(request.getParameter("edit_iaa_id"));
			double agent_infull=Tools.str2double(request.getParameter("agent_infull"+id));
			double agent_rate=Tools.str2double(request.getParameter("agent_rate"+id));
			double suidian=Tools.str2double(request.getParameter("suidian"+id));
			double agent_cps=Tools.str2double(request.getParameter("agent_cps"+id));
			String operator_name=request.getParameter("operator_name");
			
			DecimalFormat df = new DecimalFormat("0.000");
			//��������̨���-������-˰�ѣ�*(1-�����ֳɱ�����
			double sy_infull = Double.valueOf(df.format((agent_infull - agent_rate - suidian) * (1 - agent_cps)));
			
			FormXML formXML = new FormXML();
			formXML.add("agent_infull", agent_infull);
			formXML.add("agent_rate",agent_rate);
			formXML.add("suidian",suidian);
			formXML.add("agent_cps",agent_cps);
			formXML.add("operator_name",operator_name);
			formXML.add("sy_infull", sy_infull);
			formXML.addDateTime("last_time", MyTools.getTimeStr());

			formXML.setAction(FormXML.ACTION_UPDATE);
			formXML.setWhereClause("iaa_id=" + id);
			ReturnValue rv = save(formXML);
			if (rv.success) {
				return new ReturnValue(true, "�޸ĳɹ�");
			} else {
				return new ReturnValue(false, "�޸�ʧ��");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.getMessage());
		}
	}
	
	//--------------��̬��---------------
	
	private static AgtentSDKBAC instance = new AgtentSDKBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static AgtentSDKBAC getInstance() {
		return instance;
	}
}
