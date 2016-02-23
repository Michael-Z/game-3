package com.moonic.bac;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.moonic.mgr.TabStor;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.MyTools;
import com.moonic.util.StreamHelper;

import conf.Conf;
import conf.LogTbName;

/**
 * �����¼
 * @author 
 */
public class MsgLogBAC extends BaseActCtrl{
	public static final String[] CHA_NAME = {"���", "����", "����", "ϵͳ", "����", "����", "����", "����", "����ս����", "����սս��", "ƥ��սս��", "����"};
	
	public MsgLogBAC() {
		super.setTbName(LogTbName.TAB_MSG_LOG());
		setDataBase(ServerConfig.getDataBase_Backup());
	}

	public JSONObject getPageList(PageContext pageContext) {
		ServletRequest request = pageContext.getRequest();
		int page = Tools.str2int(request.getParameter("page"));
		if (page == 0) {
			page = 1;
		}
		int rpp = Tools.str2int(request.getParameter("rpp"));
		if (rpp == 0) {
			rpp = 10;
		}
		String showorder = request.getParameter("showorder");
		if (showorder == null || showorder.equals("")) {
			showorder = "id";
		}
		String ordertype = request.getParameter("ordertype");
		if (ordertype == null || ordertype.equals("")) {
			ordertype = "desc";
		}
		String colname = request.getParameter("colname");
		String colvalue = request.getParameter("colvalue");
		String channel = request.getParameter("channel");
		String operator = request.getParameter("operator");

		String serverId = request.getParameter("serverId");

		String startTime = request.getParameter("startTime");
		String endTime = request.getParameter("endTime");

		SqlString sqlS = new SqlString();
		String orderClause = showorder + " " + ordertype;

		if (channel != null && channel.equals("2")) {
			orderClause = "factionid," + orderClause;
		}
		
		if (colvalue != null && !colvalue.equals("")) {
			if(colname.equals("playerid")){
				colvalue = TabStor.getDataVal(PlayerBAC.tab_player, "name='"+colvalue+"'", "id");
			} else 
			if(colname.equals("factionid")){
				colvalue = TabStor.getDataVal(TabStor.tab_faction_stor, "name='"+colvalue+"'", "id");
			}
			if (operator.equals("����")) {
				sqlS.add(colname, colvalue, "like");
			} else {
				sqlS.add(colname, colvalue);
			}
		}
		if (channel != null && !channel.equals("")) {
			sqlS.add("channel", Tools.str2int(channel));
		}
		if (serverId != null && !serverId.equals("")) {
			sqlS.add("serverid", Tools.str2int(serverId));
		}
		if (startTime == null) {
			startTime = MyTools.getDateStr();
		}
		if (!startTime.equals("")) {
			sqlS.addDate("savetime", startTime, ">=");
		}
		if (endTime != null && !endTime.equals("")) {
			if (endTime.indexOf(":") == -1) {
				endTime = endTime+" 23:59:59";
			}
			sqlS.addDate("savetime", endTime, "<=");
		}
		String sql = "select * from " + LogTbName.TAB_MSG_LOG() + " " + sqlS.whereStringEx() + " order by " + orderClause;
		return getJsonPageListBySQL(sql, page, rpp);
	}
	
	
	public String replaceEmotionTagToImg(String str) {
		String[][] data = getEmotionData();
		if(data != null) {
			for(int i=0; i<emotionData.length; i++) {
				str = Tools.replace(str, emotionData[i][1], "<img src='../chat_emotion/"+emotionData[i][0]+"'>");
			}
			return str;
		} else {
			return str;
		}
	}
	
	private static String[][] emotionData;
	private String[][] getEmotionData(){
		if(emotionData == null){
			try {
				String fileStr = DBPool.getInst().readTxtFromPool("chat_emotion");
				emotionData = Tools.getStrLineArrEx2(fileStr, "emotion:", "end");
				DBPool.getInst().addTxtClearListener(new DBPoolClearListener() {
					public void callback(String key) {
						if(key.equals("chat_emotion")){
							emotionData = null;
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return emotionData;
	}
	
	public void getVoiceData(PageContext pageContext) {
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response =(HttpServletResponse)pageContext.getResponse();
		int playerId = Tools.str2int(request.getParameter("playerId"));
		String filename = request.getParameter("filename");
		DBHelper dbHelper = new DBHelper(getDataBase());
		try {
			ResultSet plaRs = dbHelper.query("tab_player", "vsid", "id="+playerId);
			if(!plaRs.next()){
				response.sendRedirect("msglog_voicefile_notfound.jsp");
				return ;
			}
			byte[] binaryData = StreamHelper.getInstance().downLoadFromUrl(Conf.res_url+"voice/"+plaRs.getInt("vsid")+"/"+filename+".dat");
			//��Ƶ��ѹ
			//String contenttype="application/octet-stream";
			String contenttype="audio/wav";
			response.reset() ;
			response.setContentType(contenttype);
			response.setHeader("Content-Disposition", new String(("attachment;Filename="+filename+".wav").getBytes("GBK"),"ISO8859-1"));
			OutputStream os = response.getOutputStream();
			os.write(Tools.getWavFileBytes(decode(binaryData)));
			os.close();		
		} catch (Exception e) {
			e.printStackTrace();
			try {
				response.sendRedirect("msglog_voicefile_notfound.jsp");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	public static byte[] decode(byte[] encoded) {
		if(encoded==null)return null;
		//���ݸ�ʽ�������㷨�汾(һ��byte),����...
		if(encoded[0]==1) {
			//��ѹ8λ��Ƶ,�����㷨�汾1
			byte[] decompress = new byte[(encoded.length-1)*2];
			for(int i=1;i<encoded.length;i++) {
				//short decompressShort = (short)(encoded[i]<<8);
				//decompress[i-1] = (short)(encoded[i]<<8);
				decompress[(i-1)*2+1]=encoded[i];					
			}
			return decompress;
		} else {
			//���������㷨
			return null;
		}
		
		//��ѹ8λ��Ƶ,�����㷨�汾1
		/*byte[] decompress = new byte[(encoded.length-1)*2];
		for(int i=1;i<encoded.length;i++) {
			//short decompressShort = (short)(encoded[i]<<8);
			//decompress[i-1] = (short)(encoded[i]<<8);
			decompress[(i-1)*2+1]=encoded[i];					
		}
		return decompress;*/
	}
	
	//--------------��̬��--------------
	
	private static MsgLogBAC instance = new MsgLogBAC();
		
	public static MsgLogBAC getInstance() {
		return instance;
	}
}
