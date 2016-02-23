package com.moonic.bac;

import java.sql.ResultSet;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.json.JSONArray;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.SqlString;
import com.moonic.util.DBHelper;
import com.moonic.util.DBUtil;
import com.moonic.util.FusionChartsCreator;
import com.moonic.util.JsonRs;
import com.moonic.util.MyTools;

/**
 * ��Ӫͳ��
 * @author John
 */
public class OperationStatBAC {
	//���շ�������
	public static String OPERATE_RETURN_REPORT = "OPERATE_RETURN_REPORT";
	//���շ�Χ����
	public static String OPERATE_RANGE_REPORT = "OPERATE_RANGE_REPORT";
	//��ʧ��
	public static String OPERATE_LOSE_REPORT = "OPERATE_LOSE_REPORT";
	//���ս�ɫ����
	public static String OPERATE_HOUR_REPORT = "OPERATE_HOUR_REPORT";
	
	/**
	 * ������ͳ��
	 */
	public JsonRs getRetentionData(PageContext pageContext) {
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Report());
		try {
			ServletRequest request = pageContext.getRequest();
			int stattype = Tools.str2int(request.getParameter("stattype"));
			String startdate = request.getParameter("startTime");
			String enddate = request.getParameter("endTime");
			String channel = request.getParameter("channel");
			String server = request.getParameter("server");
			int aitype = Tools.str2int(request.getParameter("aitype"));
			SqlString sqlStr = new SqlString();
			sqlStr.addDate("log_date", startdate, ">=");
			if(enddate!=null && !enddate.equals("")){
				sqlStr.addDate("log_date", MyTools.getDateStr(MyTools.getTimeLong(enddate)+MyTools.long_day), "<");
			}
			StringBuffer tgrSb = new StringBuffer();
			StringBuffer groupSb = new StringBuffer();
			StringBuffer orderSb = new StringBuffer();
			tgrSb.append("log_date");
			tgrSb.append(",sum(new_player) as total_new_player");
			tgrSb.append(",sum(stay_num1) as total_stay_num1");
			tgrSb.append(",sum(stay_num3) as total_stay_num3");
			tgrSb.append(",sum(stay_num7) as total_stay_num7");
			tgrSb.append(",sum(stay_num15) as total_stay_num15");
			tgrSb.append(",sum(stay_num30) as total_stay_num30");
			groupSb.append("log_date");
			orderSb.append(" order by log_date");
			if(channel!=null && !channel.equals("")){
				tgrSb.append(",channel");
				groupSb.append(",channel");
				orderSb.append(",channel");
			}
			if(server!=null && !server.equals("")){
				tgrSb.append(",serverid");
				groupSb.append(",serverid");
				orderSb.append(",serverid");				
			}
			dbHelper.openConnection();
			ResultSet reportRs = null;
			if(aitype == 0){
				sqlStr.add("type", stattype);
				reportRs = dbHelper.query(OPERATE_RETURN_REPORT, tgrSb.toString(), sqlStr.whereString(), null, groupSb.toString()+orderSb.toString());	
			} else {
				reportRs = dbHelper.query(OPERATE_RANGE_REPORT, tgrSb.toString(), sqlStr.whereString(), null, groupSb.toString()+orderSb.toString());	
			}
			return DBUtil.convertRsToJsonRs(reportRs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ʧ��ͳ��
	 */
	public JsonRs getTrunOverData(PageContext pageContext) {
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Report());
		try {
			ServletRequest request = pageContext.getRequest();
			int condtype = Tools.str2int(request.getParameter("condtype"));
			int serverid = Tools.str2int(request.getParameter("serverId"));
			String startdate = request.getParameter("startDate");
			SqlString sqlStr = new SqlString();
			if(serverid != 0){
				sqlStr.add("serverid", serverid);
			}
			sqlStr.addDate("log_date", startdate, ">=");
			sqlStr.addDate("log_date", MyTools.getDateStr(MyTools.getTimeLong(startdate)+MyTools.long_day), "<");
			sqlStr.add("type", condtype);
			dbHelper.openConnection();
			StringBuffer tgrSb = new StringBuffer();
			tgrSb.append("type_num");
			tgrSb.append(",sum(total_num) as sum_total_num");
			tgrSb.append(",sum(lose_num1) as sum_lose_num1");
			tgrSb.append(",sum(lose_num3) as sum_lose_num3");
			tgrSb.append(",sum(lose_num7) as sum_lose_num7");
			ResultSet reportRs = dbHelper.query(OPERATE_LOSE_REPORT, tgrSb.toString(), sqlStr.whereString(), null, "type_num order by type_num");
			return DBUtil.convertRsToJsonRs(reportRs);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ���ս�ɫ����
	 */
	public JSONArray getPlayerData(PageContext pageContext){
		DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Report());
		try {
			ServletRequest request = pageContext.getRequest();
			int serverid = Tools.str2int(request.getParameter("serverId"));
			String startdate = request.getParameter("date");
			
			SqlString sqlStr = new SqlString();
			if(serverid != 0){
				sqlStr.add("serverid", serverid);	
			}
			sqlStr.addDate("log_date", startdate, ">=");
			sqlStr.addDate("log_date", MyTools.getDateStr(MyTools.getTimeLong(startdate)+MyTools.long_day), "<");
			StringBuffer tgrSb = new StringBuffer();
			tgrSb.append("to_number(hour_num) as hour_num");
			tgrSb.append(",sum(new_player) as total_new_player");
			tgrSb.append(",sum(online_num) as total_online_num");
			tgrSb.append(",sum(online_time) as total_online_time");
			tgrSb.append(",sum(used_coin) as total_used_coin");
			tgrSb.append(",sum(login_num) as total_login_num");
			ResultSet reportRs = dbHelper.query(OPERATE_HOUR_REPORT, tgrSb.toString(), sqlStr.whereString(), null, "hour_num order by hour_num");
			
			StringBuffer lineSb = new StringBuffer();
			StringBuffer dataSb1 = new StringBuffer();
			StringBuffer dataSb2 = new StringBuffer();
			StringBuffer dataSb3 = new StringBuffer();
			StringBuffer dataSb4 = new StringBuffer();
			StringBuffer dataSb5 = new StringBuffer();
			
			while(reportRs.next()){
				lineSb.append("<category name='" + reportRs.getString("hour_num") + "'/>");
				dataSb1.append("<set value='" + reportRs.getInt("total_new_player") + "'/>");
				dataSb2.append("<set value='" + reportRs.getInt("total_online_num") + "'/>");
				dataSb3.append("<set value='" + reportRs.getInt("total_online_time") + "'/>");
				dataSb4.append("<set value='" + reportRs.getInt("total_used_coin") + "'/>");
				dataSb5.append("<set value='" + reportRs.getInt("total_login_num") + "'/>");
			}
			
			StringBuffer sb1 = new StringBuffer();
			sb1.append("<graph  caption='������ɫͳ��' baseFont='����' baseFontSize='12' yAxisMinValue='0' yAxisMaxValue='10' xaxisname='ʱ��' yaxisname='������ɫ��' hovercapbg='#87CEFF' hovercapborder='#8B0A50' formatNumberScale='0' decimalPrecision='0' showvalues='1' numdivlines='10' numVdivlines='0' shownames='1'  rotateNames='1' drawAnchors='1'  rotateLabels='1' showShadow='0' anchorSides='3'>");
			sb1.append("<categories>");
			sb1.append(lineSb.toString());
			sb1.append("</categories>");
			sb1.append("<dataset seriesName='������ɫ��' color='#FF0000' anchorBorderColor='#FF0000'>");
			sb1.append(dataSb1.toString());
			sb1.append("</dataset>");
			sb1.append("</graph>");
			String code1 = FusionChartsCreator.createChart("../Charts/MSLine.swf", "", sb1.toString(), "totalCreate", 950, 300, false, false);
			
			StringBuffer sb2 = new StringBuffer();
			sb2.append("<graph  caption='������ͳ��' baseFont='����' baseFontSize='12' yAxisMinValue='0' yAxisMaxValue='10' xaxisname='ʱ��' yaxisname='������' hovercapbg='#87CEFF' hovercapborder='#8B0A50' formatNumberScale='0' decimalPrecision='0' showvalues='1' numdivlines='10' numVdivlines='0' shownames='1'  rotateNames='1' drawAnchors='1'  rotateLabels='1' showShadow='0' anchorSides='3'>");
			sb2.append("<categories>");
			sb2.append(lineSb.toString());
			sb2.append("</categories>");
			sb2.append("<dataset seriesName='������' color='#FF0000' anchorBorderColor='#FF0000'>");
			sb2.append(dataSb2.toString());
			sb2.append("</dataset>");
			sb2.append("</graph>");
			String code2 = FusionChartsCreator.createChart("../Charts/MSLine.swf", "", sb2.toString(), "olam", 950, 300, false, false);
			
			StringBuffer sb3 = new StringBuffer();
			sb3.append("<graph  caption='����ʱ��ͳ��' baseFont='����' baseFontSize='12' yAxisMinValue='0' yAxisMaxValue='10' xaxisname='ʱ��' yaxisname='����ʱ��' hovercapbg='#87CEFF' hovercapborder='#8B0A50' formatNumberScale='0' decimalPrecision='0' showvalues='1' numdivlines='10' numVdivlines='0' shownames='1'  rotateNames='1' drawAnchors='1'  rotateLabels='1' showShadow='0' anchorSides='3'>");
			sb3.append("<categories>");
			sb3.append(lineSb.toString());
			sb3.append("</categories>");
			sb3.append("<dataset seriesName='����ʱ��' color='#FF0000' anchorBorderColor='#FF0000'>");
			sb3.append(dataSb3.toString());
			sb3.append("</dataset>");
			sb3.append("</graph>");
			String code3 = FusionChartsCreator.createChart("../Charts/MSLine.swf", "", sb3.toString(), "olTiemLen", 950, 300, false, false);
			
			StringBuffer sb4 = new StringBuffer();
			sb4.append("<graph  caption='��ʯʹ����ͳ��' baseFont='����' baseFontSize='12' yAxisMinValue='0' yAxisMaxValue='10' xaxisname='ʱ��' yaxisname='��ʯʹ����' hovercapbg='#87CEFF' hovercapborder='#8B0A50' formatNumberScale='0' decimalPrecision='0' showvalues='1' numdivlines='10' numVdivlines='0' shownames='1'  rotateNames='1' drawAnchors='1'  rotateLabels='1' showShadow='0' anchorSides='3'>");
			sb4.append("<categories>");
			sb4.append(lineSb.toString());
			sb4.append("</categories>");
			sb4.append("<dataset seriesName='��ʯʹ����' color='#FF0000' anchorBorderColor='#FF0000'>");
			sb4.append(dataSb4.toString());
			sb4.append("</dataset>");
			sb4.append("</graph>");
			String code4 = FusionChartsCreator.createChart("../Charts/MSLine.swf", "", sb4.toString(), "coinUse", 950, 300, false, false);
			
			StringBuffer sb5 = new StringBuffer();
			sb5.append("<graph  caption='��½����ͳ��' baseFont='����' baseFontSize='12' yAxisMinValue='0' yAxisMaxValue='10' xaxisname='ʱ��' yaxisname='��½����' hovercapbg='#87CEFF' hovercapborder='#8B0A50' formatNumberScale='0' decimalPrecision='0' showvalues='1' numdivlines='10' numVdivlines='0' shownames='1'  rotateNames='1' drawAnchors='1'  rotateLabels='1' showShadow='0' anchorSides='3'>");
			sb5.append("<categories>");
			sb5.append(lineSb.toString());
			sb5.append("</categories>");
			sb5.append("<dataset seriesName='��½����' color='#FF0000' anchorBorderColor='#FF0000'>");
			sb5.append(dataSb5.toString());
			sb5.append("</dataset>");
			sb5.append("</graph>");
			String code5 = FusionChartsCreator.createChart("../Charts/MSLine.swf", "", sb5.toString(), "loginAm", 950, 300, false, false);
			
			JSONArray returnarr = new JSONArray();
			returnarr.add(code1);
			returnarr.add(code2);
			returnarr.add(code3);
			returnarr.add(code4);
			returnarr.add(code5);
			return returnarr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	//--------------��̬��--------------
	
	private static OperationStatBAC instance = new OperationStatBAC();
	
	public static OperationStatBAC getInstance() {		
		return instance;
	}
}
