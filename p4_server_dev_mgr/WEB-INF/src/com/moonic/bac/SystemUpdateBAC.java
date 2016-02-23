package com.moonic.bac;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.BACException;
import com.moonic.util.DBHelper;

import conf.Conf;

import server.common.Tools;
import server.config.ServerConfig;

/**
 * ϵͳ����
 * @author 
 */
public class SystemUpdateBAC {
	public static String tb_system_update = "tb_system_update";
	
	/**
	 * ����ϵͳ
	 */
	public ReturnValue updateSystem(String filename, byte[] zipBytes) {
		DBHelper dbHelper = new DBHelper();
		try {
			if(!filename.toLowerCase().endsWith(".zip")){
				BACException.throwInstance("���ϴ�zip�ļ�");
			}
			update(zipBytes);
			dbHelper.openConnection();
			SqlString sqlStr = new SqlString();
			sqlStr.add("server", Conf.stsKey);
			sqlStr.add("updfile", filename);
			sqlStr.addDateTime("savetime", Tools.getCurrentDateTimeStr());
			sqlStr.add("filesize", zipBytes.length);
			dbHelper.insert(tb_system_update, sqlStr);
			return new ReturnValue(true, "���³ɹ�");
		} catch (Exception ex) {
			return new ReturnValue(false, "����ʧ��" + ex.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * �ͷ�zip�ļ����ݸ��Ǹ���ϵͳ
	 */
	private void update(byte[] zipBytes) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes);
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipEntry = null;
		byte[] buffer = new byte[4096];
		while ((zipEntry = zis.getNextEntry()) != null) {
			if (!zipEntry.isDirectory()) {
				String entryName = zipEntry.getName();
				File writeFile = new File(ServerConfig.getAppRootPath() + entryName);
				if (!writeFile.getParentFile().exists()) {
					writeFile.getParentFile().mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(writeFile);
				int len = 0;
				while ((len = zis.read(buffer)) != -1) {
					fos.write(buffer, 0, len);
				}
				fos.close();
			}
		}
		zis.close();
	}
	
	//----------------��̬��------------------
	
	private static SystemUpdateBAC instance = new SystemUpdateBAC();

	public static SystemUpdateBAC getInstance() {
		return instance;
	}
}
