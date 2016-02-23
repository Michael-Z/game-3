package com.moonic.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.ehc.common.ReturnValue;
import com.jspsmart.upload.File;
import com.jspsmart.upload.Files;
import com.jspsmart.upload.SmartUpload;

/**
 * �ϴ�����������
 * @author John
 */
public class StreamHelper {
	
	/**
	 * �ϴ�
	 */
	public ReturnValue upload(SmartUpload smartUpload, String path, String tag) throws Exception {
		File file = smartUpload.getFile(tag);
		return upload(smartUpload, path, tag, file.getFileName());
	}
	
	/**
	 * �ϴ�
	 */
	public ReturnValue upload(SmartUpload smartUpload, String path, String tag, String filename) throws Exception {
		File file = smartUpload.getFile(tag);
		if(file.getSize() > 0) {
			java.io.File folder = new java.io.File(path);//Ŀ¼�������򴴽�
			if(!folder.exists()) {
				folder.mkdirs();
			}
			java.io.File myfile = new java.io.File(path + filename);
			if(myfile.exists()){
				myfile.delete();
			}
			file.saveAs(path + filename);
		}
		return new ReturnValue(true, filename);
	}
	
	/**
	 * �ϴ�����ļ�
	 */
	public ReturnValue uploadFiles(SmartUpload smartUpload, String path, String tag) throws Exception {
		Files files = smartUpload.getFiles();
		for (int i = 0; i < files.getCount(); i++) {
			File file = files.getFile(i);
			if (file.getFieldName().equals(tag)) {
				if (file.getSize() > 0) {
					java.io.File folder = new java.io.File(path);// Ŀ¼�������򴴽�
					if (!folder.exists()) {
						folder.mkdirs();
					}
					String filename = file.getFileName();
					java.io.File myfile = new java.io.File(path + filename);
					if(myfile.exists()){
						myfile.delete();
					}
					file.saveAs(path + filename);
				}
			}
		}
		return new ReturnValue(true);
	}
	
	/**
	 * �ϴ�����ļ�|��ZIP��ѹ
	 */
	public ReturnValue uploadFiles2(SmartUpload smartUpload, String path, String tag) throws Exception {
		Files files = smartUpload.getFiles();
		for (int i = 0; i < files.getCount(); i++) {
			File file = files.getFile(i);
			if (file.getFieldName().equals(tag)) {
				if (file.getSize() > 0) {
					java.io.File folder = new java.io.File(path);// Ŀ¼�������򴴽�
					if (!folder.exists()) {
						folder.mkdirs();
					}
					String filename = file.getFileName();
					java.io.File myfile = new java.io.File(path + filename);
					if(myfile.exists()){
						myfile.delete();
					}
					file.saveAs(path + filename);
					if(file.getFileName().toLowerCase().endsWith(".zip")){
						ZipUtil.upZipFile(path + file.getFileName(), path);
						java.io.File delFile = new java.io.File((path + file.getFileName()));
						delFile.delete();
					}
				}
			}
		}
		return new ReturnValue(true);
	}
	
	/**
	 * ��������·�����ļ�
	 */
	public ReturnValue download(HttpServletResponse response, String path) throws Exception {
		java.io.File file = new java.io.File(path);
		ServletOutputStream os = response.getOutputStream();
		if (file.exists()) 
		{
			if(file.length()>50*1024) //����50k�Ĵ��zip����
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zos = new ZipOutputStream(baos);
				ZipEntry entry = new ZipEntry(file.getName());
				entry.setMethod(ZipEntry.DEFLATED);
				entry.setSize(file.length());
				zos.putNextEntry(entry);
				FileInputStream fis = new FileInputStream(file);
				byte[] buffer = new byte[4096];
				int len=0;
				while((len=fis.read(buffer))!=-1)
				{
					zos.write(buffer, 0, len);
				}
				zos.close();
				byte[] outbytes = baos.toByteArray();
				if(outbytes!=null && outbytes.length>0)
				{
					response.reset();
					response.addHeader("Content-Disposition", "attachment;filename=" + new String((file.getName()+".zip").getBytes(), "ISO-8859-1"));
					response.addHeader("Content-Length", "" + file.length());
					response.setContentLength(outbytes.length);
					response.setContentType("application/zip");// ֱ������	
					
					os.write(outbytes);
					os.flush();
					os.close();
					return new ReturnValue(true);
				}
				else
				{
					return new ReturnValue(false);
				}
			}
			else
			{
				FileInputStream fis = new FileInputStream(file);
				response.reset();
				response.addHeader("Content-Disposition", "attachment;filename=" + new String(file.getName().getBytes(), "ISO-8859-1"));
				response.addHeader("Content-Length", "" + file.length());
				response.setContentLength((int)file.length());
				response.setContentType("application/octet-stream");// ֱ������
				
				byte[] buffer = new byte[4096];
				int readlen=0;
				while((readlen = fis.read(buffer))!=-1)
				{
					os.write(buffer, 0,readlen);
				}
				os.flush();
				os.close();
				return new ReturnValue(true);
			}			
		} else {
			return new ReturnValue(false);
		}
	}
	
	/**
	 * ����WEB-INF����ļ�
	 */
	public ReturnValue downloadFile(String path){
		String str = "<script language='javascript' type='text/javascript'>" +
					 "window.location.href='" + path + "';" +
					 "</script>";
		return new ReturnValue(true, str);
	}
	
	/**
	 * ������URL�������ļ�
	 */
	public byte[] downLoadFromUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3 * 1000);//���ó�ʱ��Ϊ3��
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");//��ֹ���γ���ץȡ������403����
		InputStream inputStream = conn.getInputStream();//�õ�������
		return readInputStream(inputStream);//��ȡ�Լ�����
	}
	
	/**
	 * ���������л�ȡ�ֽ�����
	 */
	public byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}
		bos.close();
		return bos.toByteArray();
	}
	
	//--------------��̬��--------------
	
	/**
	 * ��ȡʵ��
	 */
	public static StreamHelper getInstance(){
		return new StreamHelper();
	}
	
	//context.getServletContext().getRealPath("/")
	/*try {
		InputStream is = request.getInputStream();
		byte[] data = new byte[4*1024];int len = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while((len = is.read(data)) > 0){
			baos.write(data, 0, len);
		}
		is.close();
		File file = new File(ServerConfig.getAppRootPath()+"/res/test_stream/upload.txt");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(baos.toByteArray());
		fos.close();
		return new ReturnValue(true, "�������");
	} catch (IOException e) {
		e.printStackTrace();
		return new ReturnValue(false, e.toString());
	}*/
}
