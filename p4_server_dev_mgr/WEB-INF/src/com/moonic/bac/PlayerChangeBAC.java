package com.moonic.bac;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import server.common.Tools;
import util.IPAddressUtil;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.mgr.TabStor;
import com.moonic.servlet.STSServlet;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.NetResult;
import com.moonic.util.STSNetSender;

import conf.LogTbName;

/**
 * ��ɫ���ݲ���
 * @author John
 */
public class PlayerChangeBAC {
	
	/**
	 * ��Ҵ���
	 */
	public ReturnValue operate(PageContext pageContext) {
		try {
			HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
			int serverId = Tools.str2int(request.getParameter("serverId"));
			int playerId = Tools.str2int(request.getParameter("playerId"));
			String columnname =  request.getParameter("columnname");
			DBPaRs changeRs = DBPool.getInst().pQueryA(TabStor.tab_player_change_type, "columnname='"+columnname+"'");
			if(!changeRs.exist()){
				BACException.throwInstance("���ʹ���");
			}
			String changeValue = request.getParameter("changeValue");
			int type = changeRs.getInt("type");
			String reason = request.getParameter("reason");
			STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_CHANGEVALUE);
			sender.dos.writeInt(playerId);
			sender.dos.writeByte(type);
			sender.dos.writeUTF(changeValue);
			sender.dos.writeUTF("��Ҵ���");
			NetResult nr = ServerBAC.getInstance().sendReqToOne(ServerBAC.STS_GAME_SERVER, sender, serverId);
			if(!nr.rv.success){
				return nr.rv;
			}
			SqlString sqlStr = new SqlString();
			sqlStr.add("serverid", serverId);
			sqlStr.add("playerid", playerId);
			sqlStr.add("columnname", changeRs.getString("columnname"));
			sqlStr.add("changeValue", changeValue);
			sqlStr.addDateTime("savedate", Tools.getCurrentDateTimeStr());
			HttpSession session = pageContext.getSession();
			String operatername = (String)session.getAttribute("username");
			sqlStr.add("operatername", operatername);
			sqlStr.add("ip", IPAddressUtil.getIp(request));
			sqlStr.add("reason", reason);
			DBHelper.logInsert(LogTbName.TAB_PLAYER_CHANGELOG(), sqlStr);
			return new ReturnValue(true, "����ɹ�");
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	//--------------��̬��--------------
	
	private static PlayerChangeBAC instance = new PlayerChangeBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static PlayerChangeBAC getInstance(){
		return instance;
	}
}
