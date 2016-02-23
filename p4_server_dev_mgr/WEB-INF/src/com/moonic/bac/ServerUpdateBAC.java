package com.moonic.bac;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.dbc.BaseActCtrl;
import com.jspsmart.upload.SmartUpload;
import com.moonic.servlet.STSServlet;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTools;
import com.moonic.util.STSNetSender;

/**
 * ϵͳ����
 * @author 
 */
public class ServerUpdateBAC extends BaseActCtrl{
	public static String tbName = "tb_system_update";
	
	/**
	 * ����
	 */
	public ServerUpdateBAC() {
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}
	
	/**
	 * ����
	 */
	public ReturnValue update(PageContext pageContext) {
		SmartUpload smartUpload = new SmartUpload();
		smartUpload.setEncode("UTF-8");
		try {
			smartUpload.initialize(pageContext);
			smartUpload.upload();
			com.jspsmart.upload.File file = smartUpload.getFile("file");
			int fileLen = file.getSize();
			if(!file.getFileName().toLowerCase().endsWith(".zip")){
				return new ReturnValue(false, "���ϴ�zip�ļ�");
			}
			byte[] fileBytes = new byte[fileLen];
			for(int i=0;i<fileBytes.length;i++) {
				fileBytes[i] = file.getBinaryData(i);
			}
			int type = Tools.str2int(smartUpload.getRequest().getParameter("updtype"));
			if(type == 0){//��̨�����
				return SystemUpdateBAC.getInstance().updateSystem(file.getFileName(), fileBytes);
			} else 
			if(type == 1 || type == 2){//�û���|��Ϸ��
				String[] serverIds = smartUpload.getRequest().getParameterValues("serverId");
				if(serverIds == null){
					return new ReturnValue(false, "��ѡ��Ҫ���µ���Ϸ������");
				}
				short act = 0;
				byte servertype = 0;
				if(type == 1){
					act = STSServlet.M_SERVER_UPDATE;
					servertype = ServerBAC.STS_USER_SERVER;
				} else 
				{
					act = STSServlet.G_SERVER_UPDATE;
					servertype = ServerBAC.STS_GAME_SERVER;
				}
				STSNetSender sender = new STSNetSender(act);
				sender.dos.writeUTF(file.getFileName());
				sender.dos.writeInt(fileBytes.length);
				sender.dos.write(fileBytes);
				String where = MyTools.converWhere("or", "id", "=", serverIds);
				String resultStr = ServerBAC.getInstance().converNrsToPromptStr(ServerBAC.getInstance().sendReq(servertype, where, sender));
				//��¼������־
				String ip = IPAddressUtil.getIp((HttpServletRequest)pageContext.getRequest());
				String username = (String)pageContext.getSession().getAttribute("username");
				LogBAC.logout("gameupdate", "����"+ip+"��"+username+"�û��ύ�˷�����("+type+")("+Tools.strArr2Str(serverIds)+")���°�"+file.getFileName());
				return new ReturnValue(true, resultStr);
			} else 
			{
				return new ReturnValue(false, "�������ʹ��� type="+type);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false,e.toString());
		} 		
	}
	
	/**
	 * ɾ��ָ����¼
	 */
	public ReturnValue del(PageContext pageContext) {	
		ServletRequest req = pageContext.getRequest();
		int id = Tools.str2int(req.getParameter("id"));
		ReturnValue rv = super.del("id="+ id);			
		return rv;
	}
	
	/**
	 * ������м�¼
	 */
	public ReturnValue clear() {
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.execute("delete from "+tbName);	
			return new ReturnValue(true,"������־����ɹ�");
		} catch(Exception ex) {
			ex.printStackTrace();
			return new ReturnValue(false,ex.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��---------------
	
	private static ServerUpdateBAC instance = new ServerUpdateBAC();
		
	public static ServerUpdateBAC getInstance() {
		return instance;
	}
}
