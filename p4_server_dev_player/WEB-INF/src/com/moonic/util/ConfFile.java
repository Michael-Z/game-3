package com.moonic.util;

import java.io.File;
import java.util.Hashtable;

import server.config.ServerConfig;

/**
 * �����ļ�
 * @author John
 */
public class ConfFile {
	private static Hashtable<String, String> filenametab = new Hashtable<String, String>();
	
	/**
	 * ����������ʱ��ȡ����ֵ
	 */
	public static String getFileValueInStartServer(String filename, String defaultValue){
		String value = null;
		String path = getFilePath(filename);
		File file = new File(path);
		if(file.exists()){
			value = MyTools.readTxtFile(path);
		} else {
			value = defaultValue;
			(new FileUtil()).writeNewToTxt(getFilePath(filename), value);
		}
		filenametab.put(filename, value);
		return value;
	}
	
	/**
	 * ��ȡ����ֵ
	 */
	public static String getFileValue(String filename){
		if(!filenametab.containsKey(filename)){
			System.out.println("��ȡ����-�����ļ� "+filename+" δ������ʱ��ʼ��");
		}
		return filenametab.get(filename);
	}
	
	/**
	 * ��������ֵ
	 */
	public static void updateFileValue(String filename, String value){
		if(!filenametab.containsKey(filename)){
			System.out.println("��������-�����ļ� "+filename+" δ������ʱ��ʼ��");
		}
		(new FileUtil()).writeNewToTxt(getFilePath(filename), value);
		filenametab.put(filename, value);
	}
	
	/**
	 * ��ȡ�洢ʱ���ļ�·��
	 */
	private static String getFilePath(String filename){
		return ServerConfig.getWebInfPath()+"txt_conf/"+filename+".txt";
	}
}
