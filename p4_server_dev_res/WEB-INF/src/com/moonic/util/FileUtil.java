package com.moonic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * �ļ����߼�
 */
public class FileUtil {
	
	/**
	 * ׷�����ݵ�TXT
	 */
	public void addToTxt(String savepath, String content){
		writeToTxt(savepath, content, true);
	}
	
	/**
	 * д�������ݵ�TXT(ԭ���ݱ�ɾ��)
	 */
	public void writeNewToTxt(String savepath, String content){
		writeToTxt(savepath, content, false);
	}
	
	/**
	 * д�����ݵ�TXT
	 * @param addto �Ƿ�׷�����ݵ��ļ�
	 */
	private void writeToTxt(String savepath, String content, boolean addto){
		OutputStream os = null;
		try {
			String dirPath = savepath.substring(0, savepath.lastIndexOf("/")+1);
			File dir = new File(dirPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			boolean firstwrite = false;
			String filePath = savepath;
			File file = new File(filePath);
			if (!file.exists()) {
				file.createNewFile();
				firstwrite = true;
			}
			os = new FileOutputStream(file, addto);
			if(firstwrite || !addto){
				byte[] head = new byte[]{(byte)0xef, (byte)0xbb, (byte)0xbf};
				os.write(head);	
			}
			StringBuffer sb = new StringBuffer();
			sb.append(content);
			os.write(sb.toString().getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(os != null){
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ɾ���ļ���
	 */
	public boolean deleteDirectory(String sPath) {
		// ���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// ���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// ɾ���ļ����µ������ļ�(������Ŀ¼)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// ɾ�����ļ�
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // ɾ����Ŀ¼
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// ɾ����ǰĿ¼
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * ɾ���ļ�
	 */
	public boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}
	
	/**
	 * ���ļ�
	 */
	public void save(String savepath, byte[] filedata) throws Exception {
		File savedir = new File(savepath.substring(0, savepath.lastIndexOf("/")+1));
		if(!savedir.exists()){
			savedir.mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(savepath);
		fos.write(filedata);
		fos.close();
	}
	
	/**
	 * �ļ����Ϊ
	 */
	public void saveAs(String sourcepath, String savepath) {
		try {
			File savedir = new File(savepath.substring(0, savepath.lastIndexOf("/")+1));
			if(!savedir.exists()){
				savedir.mkdirs();
			}
			FileInputStream fis = new FileInputStream(sourcepath);
			FileOutputStream fos = new FileOutputStream(savepath);
			byte[] data = new byte[1024];
			int len = 0;
			while((len=fis.read(data))!=-1){
				fos.write(data, 0, len);
			}
			fis.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		FileUtil fileUtil = new FileUtil();
		fileUtil.saveAs("F:\\1\\a.jpg", "F:\\1\\b.jpg");
	}
}
