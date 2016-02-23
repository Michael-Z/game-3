package com.moonic.bac;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.moonic.util.FileUtil;
import com.moonic.util.MyTools;

/**
 * 
 * @author 
 */
public class AgtentNSDKBAC extends BaseActCtrl {
	public static String tbName = "infull_agent_analyse_nsdk";
	
	/**
	 * ����
	 */
	public AgtentNSDKBAC() {
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase_Report());
	}
	
	/**
	 * ���/����
	 */
	public ReturnValue save(PageContext pageContext) {
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			
			int id = Tools.str2int(request.getParameter("edit_iaa_id"));
			double rate=Tools.str2double(request.getParameter("rate"+id));
			double suidian=Tools.str2double(request.getParameter("suidian"+id));
			double agent_rate=Tools.str2double(request.getParameter("agent_rate"+id));
			String operator_name=request.getParameter("operator_name");
			
			
			JSONObject obj = getJsonObj("iaa_id="+id);
			
			double total_infull=obj.getDouble("total_infull");
			
			double agent_rate_money = total_infull * rate;
			/*
			3����������=���ܳ�ֵ���-�����ѣ�*�����ֳɱ���*��1-˰�㣩
			4����������=���ܳ�ֵ���-�����ѣ�*��1-�����ֳɱ�����
			*/
			double agent_infull = (obj.getDouble("total_infull") - agent_rate_money) * agent_rate * (1 - suidian);
			double sy_infull = (obj.getDouble("total_infull") - agent_rate_money) * (1 - agent_rate);
			//System.out.println("operator_name:"+operator_name);
			
			DecimalFormat df = new DecimalFormat("0.000");
			
			FormXML formXML = new FormXML();
			formXML.add("rate",rate);
			formXML.add("suidian",suidian);
			formXML.add("agent_rate",agent_rate);
			formXML.add("operator_name",operator_name);
			formXML.add("agent_rate_money", Double.valueOf(df.format(agent_rate_money)));
			formXML.add("agent_infull", Double.valueOf(df.format(agent_infull)));
			formXML.add("sy_infull", Double.valueOf(df.format(sy_infull)));
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
	
	/**
	 * ����
	 */
	public void export(PageContext pageContext){
		try {
			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
			
			String sel_yearStr = request.getParameter("yearlist");
			String sel_moonthStr = request.getParameter("moonthlist");
			String sel_channel = request.getParameter("exportchannel");
			
			String where = "infull_date='"+sel_yearStr+"-"+sel_moonthStr+"'";
			if(!sel_channel.equals("")){
				where += " and agent_name='"+sel_channel+"'";
			}
			
			JSONArray jsonarr = getJsonList(null, where, null);
			
			//System.out.println("where:"+where);
			//System.out.println("jsonarr:"+jsonarr);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
			
			for(int i = 0; jsonarr != null && i < jsonarr.length(); i++){
				JSONObject channelobj = jsonarr.optJSONObject(i);
				
				double total_infull=channelobj.getDouble("total_infull");
				
				double rate=channelobj.getDouble("rate");
				
				double channelrate = channelobj.getDouble("agent_rate");
				
				String channelName = channelobj.optString("agent_name");
				String sourcepath = ServerConfig.getWebInfPath()+"/res2/cps_model.xlsx";
				String savepath = ServerConfig.getWebInfPath()+"/logs/rate/"+channelName+"-�ڴ�����CPS.xlsx";
				String moonth = sel_yearStr+"-"+sel_moonthStr;
				
				double[] money = new double[]{total_infull};//����
				double[] chargerate = new double[]{rate};//�����ֳ�
				//System.out.println("savepath:"+savepath);
				exportExcel(channelName, moonth, money, chargerate, channelrate, sourcepath, savepath);
				
				File excelfile = new File(savepath);
				byte[] fileBytes = Tools.getBytesFromFile(excelfile);
				ZipEntry zipEntry = new ZipEntry(excelfile.getName());
				zipEntry.setSize(fileBytes.length);
				zipOutputStream.putNextEntry(zipEntry);
				zipOutputStream.write(fileBytes);
				zipOutputStream.closeEntry();
			}
			
			zipOutputStream.close();
			baos.close();
			
			HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();
			
			response.reset();
			response.setContentType("application/zip");
			response.setContentLength(baos.size());
			response.setHeader("Content-disposition", new String(("attachment;filename=�ڴ�����CPS.zip").getBytes("GBK"),"ISO-8859-1"));
			
			OutputStream os = response.getOutputStream();
			os.write(baos.toByteArray());
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ����EXCEL
	 */
	public static void exportExcel(String channelName, String moonth, double[] money, double[] chargerate, double channelrate, String sourcepath, String savepath){
		try {
			File del = new File(savepath);
			if(del.exists()){
				del.delete();
			}
			FileUtil fileutil = new FileUtil();
			fileutil.saveAs(sourcepath, savepath);
			FileInputStream fis = new FileInputStream(savepath);
			Workbook wb = new XSSFWorkbook(fis);
			wb.setSheetName(0, channelName+moonth+"�¶��˵� ");
			//String sheetName = wb.getSheetName(0);//����
			//System.out.println(sheetName);
			Sheet sheet = wb.getSheetAt(0);
			Row channelRow = sheet.getRow(1);
			Cell channelCell = channelRow.getCell(1);
			channelCell.setCellValue("������飺 "+channelName);
			//System.out.println(channelCell.getStringCellValue());
			Row moonthRow = sheet.getRow(5);
			Cell moonthCell = moonthRow.getCell(2);
			moonthCell.setCellValue(moonth+"��");
			//System.out.println(moonthCell.getStringCellValue());
			double totalmoney = 0;//������
			double totalchargemoney = 0;//�ܳ�ֵ�����ֳ�
			double totalratemoney = 0;//�ֳܷ�����
			for(int i = 0; i < money.length; i++){
				Row shouruRow = sheet.getRow(5+i);
				Cell moneyCell = shouruRow.getCell(3);//��ֵ����
				moneyCell.setCellValue(money[i]);
				//System.out.println(shouruCell.getNumericCellValue());
				Cell channelrateCell = shouruRow.getCell(7);//�����ֳɱ���
				channelrateCell.setCellValue(channelrate);
				Cell chargemoneyCell = shouruRow.getCell(6);//���������ֳ�����
				shouruRow.getCell(5).setCellValue(chargerate[i]);//���������ֳɱ���
				double chargemoney = money[i]*chargerate[i];
				//System.out.println("money[i]:"+money[i]+" chargerate[i]:"+chargerate[i]+" chargemoney:"+chargemoney);
				chargemoneyCell.setCellValue(chargemoney);
				Cell ratemoneyCell = shouruRow.getCell(8);//�ֳ�����
				double ratemoney = (money[i]-chargemoney)*channelrate;
				ratemoneyCell.setCellValue(ratemoney);
				//System.out.println(rateCell.getNumericCellValue());
				totalmoney += money[i];
				totalchargemoney += chargemoney;
				totalratemoney += ratemoney;
			}
			Row totalRow = sheet.getRow(11);
			totalRow.getCell(3).setCellValue(totalmoney);
			totalRow.getCell(6).setCellValue(totalchargemoney);
			totalRow.getCell(8).setCellValue(totalratemoney);
			FileOutputStream fos = new FileOutputStream(new File(savepath));
			wb.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		String channelName = "���˳���";
		String sourcepath = "E:/xianmo_server/xianmo_server_manage/WEB-INF/res/������-�ڴ�����CPS.xlsx";
		String savepath = "E:/xianmo_server/xianmo_server_manage/WEB-INF/logs/rate/"+channelName+"-�ڴ�����CPS.xlsx";
		String moonth = "2014-05";
		double[] shouru = new double[]{10000, 10000, 10000, 10000, 10000};//����
		double[] rate = new double[]{0.4, 0.4, 0.4, 0.4, 0.4};//�����ֳ�
		exportExcel(channelName, moonth, shouru, rate, 0.5, sourcepath, savepath);
	}
	
	//--------------��̬��---------------
	
	private static AgtentNSDKBAC instance = new AgtentNSDKBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static AgtentNSDKBAC getInstance() {
		return instance;
	}
}
