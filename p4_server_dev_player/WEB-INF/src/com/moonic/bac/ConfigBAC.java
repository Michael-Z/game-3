package com.moonic.bac;

import server.common.Tools;

import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * ϵͳ����
 * @author John
 */
public class ConfigBAC {
	public static String tb_config = "tb_config";
	
	//----------------��̬��----------------
	
	/**
	 * ��ȡֵ
	 */
	public static boolean getBoolean(String name) {
		return Tools.str2boolean(getString(name));
	}
	
	/**
	 * ��ȡֵ
	 */
	public static int getInt(String name) {
		return Tools.str2int(getString(name));
	}
	
	/**
	 * ��ȡֵ
	 */
	public static String getString(String name) {
		try {
			DBPaRs confRs = DBPool.getInst().pQueryA(tb_config, "name='"+name+"'");
			if(!confRs.exist()){
				BACException.throwAndOutInstance("ȱ��CONFIG������"+name);
			}
			return confRs.getString("value");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static ConfigBAC instance = new ConfigBAC();

	public static ConfigBAC getInstance() {
		return instance;
	}
}
