package com.moonic.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * ZIP����
 * 
 * @author John
 */
public class ZipUtil {
	
	/**
	 * ��ѹZIP�ļ�
	 * @param filePath Ŀ���ļ�·��
	 * @param folderPath ��ѹ�����ļ���·��
	 */
	public static void upZipFile(String filePath, String folderPath) {
		try {
			ZipFile zipFile = new ZipFile(new File(filePath));
			Enumeration<?> zipList = zipFile.entries();// ����������Ŀ
			ZipEntry zipEntry = null;
			byte[] buffer = new byte[1024 * 4];
			while (zipList.hasMoreElements()) {
				zipEntry = (ZipEntry) zipList.nextElement();
				if (!zipEntry.isDirectory()) {// ����Ŀ¼
					String dirPath = folderPath + "/" + zipEntry.getName();
					File file = new File(dirPath);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					if(file.exists()){
						file.delete();
					}
					OutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
					InputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
					int length = 0;
					while ((length = bis.read(buffer)) != -1) {
						bos.write(buffer, 0, length);
					}
					bis.close();
					bos.close();
				}
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		upZipFile("C:\\Users\\huangminglong\\Desktop\\1.zip", "C:\\Users\\huangminglong\\Desktop\\1");
	}
}
