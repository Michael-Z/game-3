package com.moonic.bac;

import java.io.File;

import com.ehc.common.ReturnValue;
import com.moonic.util.MyTools;

import server.common.Tools;
import server.config.ServerConfig;


/**
 * �ļ�����
 */
public class FileMgrBAC {
	
	/**
	 * �ļ����
	 */
	public ReturnValue checkFile(boolean del){
		try {
			String rootpath = ServerConfig.getAppRootPath();
			rootpath = rootpath.substring(0, rootpath.length()-1);
			String str = check(rootpath, rootpath+"/"+"filelist.txt", del);
			return new ReturnValue(true, str);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * ����ָ��·����·���б��ļ�����ļ�
	 * @param rootpath ��Ŀ��Ŀ¼
	 * @param listpath �ļ���ַ
	 * @param del �Ƿ�ɾ�������ļ�
	 */
	public String check(String rootpath, String listpath, boolean del){
		StringBuffer sb = new StringBuffer();
		File listFile = new File(listpath);
		if(listFile.exists()){
			sb.append("\r\nfilelist.txt ����ʱ�䣺"+MyTools.getTimeStr(listFile.lastModified())+"\r\n");
			rootpath = rootpath.replace('\\', '/');
			sb.append("�������\r\n");
			String filetext = MyTools.readTxtFile(listpath);
			String[] list = Tools.splitStr(Tools.getSubString(filetext, "path:", "pathEnd"), "\r\n");
			for(int i = 0; list != null && i < list.length; i++){
				String[] data = Tools.splitStr(list[i], "|");
				//System.out.println(list[i]);
				File file = new File(rootpath+"/"+data[0]);
				if(!file.exists()){
					sb.append("ȱ���ļ���"+data[0]+"\r\n");
				} else 
				if(file.length()!=Integer.valueOf(data[1])){
					sb.append("�ļ���С��һ�£�"+data[0]+"\r\n");
				}
				list[i] = data[0];
			}
			String[] dispath = Tools.splitStr(Tools.getSubString(filetext, "dir:", "dirEnd"), "\r\n");
			String[] list2 = Tools.splitStr(getPath(rootpath, dispath), "\r\n");
			for(int i = 0; i < list2.length; i++){
				String[] data = Tools.splitStr(list2[i], "|");
				if(!MyTools.checkInStrArr(list, data[0])){
					sb.append("�����ļ���"+data[0]);
					if(del){
						File file = new File(rootpath+"/"+data[0]);
						if(file.exists()){
							file.delete();
							sb.append("(��ɾ��)");
						}
					}
					sb.append("\r\n");
				}
			}	
		} else {
			sb.append("filelist.txt δ�ҵ���");
		}
		return sb.toString();
	}
	
	/**
	 * ��ȡָ��Ŀ¼���������·��
	 * @param rootpath ��Ŀ��Ŀ¼ĩβ��"/"
	 * @param dirpath �����ļ���
	 */
	private String getPath(String rootpath, String[] dirpath){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; dirpath != null && i < dirpath.length; i++){
			File root = new File(rootpath+"/"+dirpath[i]);
			if(!root.exists()){
				System.out.println("ָ���ļ�·�������� "+rootpath+"/"+dirpath[i]);
				continue;
			}
			if(root.isDirectory()){
				ergodic(root.listFiles(), sb);		
			} else {
				ergodic(new File[]{root}, sb);
			}
		}
		//System.out.println(sb.toString());
		//System.out.println(rootpath+"/");
		return sb.toString().replace(rootpath+"/", "");
	}
	
	/**
	 * ����
	 */
	private void ergodic(File[] files, StringBuffer sb){
		for(int i = 0; i < files.length; i++){
			if(files[i].isDirectory()){
				ergodic(files[i].listFiles(), sb);
			} else {
				sb.append(files[i].getPath().replace('\\', '/')+"|"+files[i].length()+"\r\n");
			}
		}
	}
	
	//--------------��̬��--------------
	
	private static FileMgrBAC instance = new FileMgrBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static FileMgrBAC getInstance(){
		return instance;
	}
}
