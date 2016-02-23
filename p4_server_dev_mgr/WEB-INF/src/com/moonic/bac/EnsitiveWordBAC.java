package com.moonic.bac;

import java.io.OutputStream;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.json.JSONArray;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.ehc.xml.FormXML;
import com.jspsmart.upload.SmartUpload;
import com.moonic.mgr.DBPoolMgr;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPsRs;
import com.moonic.util.MyTools;
import com.moonic.util.StreamHelper;

import conf.Conf;

/**
 * ���д�
 * @author John 
 */
public class EnsitiveWordBAC extends BaseActCtrl {
	public static String tab_ensitive_words = "tab_ensitive_words";
	
	/**
	 * ����
	 */
	public EnsitiveWordBAC() {
		super.setTbName(tab_ensitive_words);
		setDataBase(ServerConfig.getDataBase());
	}
	
	/**
	 * ����
	 */
	public ReturnValue dataImport(PageContext pageContext){
		SmartUpload smartUpload = new SmartUpload();
		smartUpload.setEncode("UTF-8");
		DBHelper dbHelper = new DBHelper();
		try {
			smartUpload.initialize(pageContext);
			smartUpload.upload();
			//com.jspsmart.upload.Request request = smartUpload.getRequest();
			
			String dir = Conf.logRoot+"ensitiveword/";
			ReturnValue rv = StreamHelper.getInstance().upload(smartUpload, dir, "importfile", "temp.txt");
			if(!rv.success){
				return rv;
			}
			
			String[][] temparr = Tools.getStrLineArrEx2(MyTools.readTxtFile(dir+"temp.txt"), "data:", "dataEnd");
			
			if(temparr == null || temparr.length <= 0){
				BACException.throwInstance("����Ϊ��");
			}
			JSONArray wordarr = new JSONArray();
			JSONArray typearr = new JSONArray();
			for(int i = 0; i < temparr.length; i++){
				temparr[i][0] = temparr[i][0].toUpperCase();
				DBPaRs rs = DBPool.getInst().pQueryA(tab_ensitive_words, "word="+temparr[i][0]);
				if(!rs.exist() && !wordarr.contains(temparr[i][0])){
					wordarr.add(temparr[i][0]);
					typearr.add(temparr[i][1]);
				}
			}
			if(wordarr.length() <= 0){
				BACException.throwInstance("��������");
			}
			dbHelper.openConnection();
			for(int i = 0; i < wordarr.length(); i++){
				SqlString sqlStr = new SqlString();
				sqlStr.add("word", wordarr.optString(i));
				sqlStr.add("processtype", typearr.optString(i));
				dbHelper.insert(tab_ensitive_words, sqlStr);
			}
			DBPoolMgr.getInstance().addClearTablePoolTask(tab_ensitive_words, null);
			return new ReturnValue(true, "����ɹ���������"+wordarr.length()+" �ظ���"+(temparr.length-wordarr.length()));
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	public static final String[] processtypeStr = {"���д��滻Ϊ��*��", "ȡ������"};
	
	/**
	 * ����
	 */
	public void dataExport(PageContext pageContext){
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\r\n");
			sb.append("data:\r\n");
			DBPsRs rs = DBPool.getInst().pQueryS(tab_ensitive_words);
			while(rs.next()){
				sb.append(rs.getString("word"));
				sb.append("\t");
				sb.append(rs.getInt("processtype"));
				sb.append("\r\n");
			}
			sb.append("dataEnd");
			byte[] outbytes = sb.toString().getBytes("UTF-8");
			
			HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
			
			response.reset();
			response.setContentType("application/octet-stream");
			response.setContentLength(outbytes.length);
			response.setHeader("Content-disposition", new String(("attachment;filename="+"ensitiveword.txt").getBytes(),"ISO-8859-1"));
			OutputStream os = response.getOutputStream();
			os.write(outbytes);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			String word=request.getParameter("word");
			int processtype=Tools.str2int(request.getParameter("processtype"));
			
			if(word == null || word.equals("")){
				BACException.throwInstance("���дʲ���Ϊ���ַ���");
			}
			
			word = word.toUpperCase();
			
			DBPaRs rs = DBPool.getInst().pQueryA(tab_ensitive_words, "word="+word);
			
			if(rs.exist()){
				BACException.throwInstance("���д��Ѵ���");
			}
			
			FormXML formXML = new FormXML();
			formXML.add("word", word);
			formXML.add("processtype", processtype);
			
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
		if(id > 0){
			return super.del("id=" + id);	
		} else {
			return super.del("id!=0");
		}
	}
	
	//--------------��̬��---------------
	
	private static EnsitiveWordBAC instance = new EnsitiveWordBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static EnsitiveWordBAC getInstance() {
		return instance;
	}
}
