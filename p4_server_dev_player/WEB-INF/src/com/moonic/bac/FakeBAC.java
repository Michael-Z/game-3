package com.moonic.bac;

import server.common.Tools;

import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * �ٸ���
 * @author John
 */
public class FakeBAC {
	public static String tab_fakeodds_item = "tab_fakeodds_item";
	
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
	public static String getString(String confkey) {
		try {
			DBPaRs confRs = DBPool.getInst().pQueryA(tab_fakeodds_item, "confkey='"+confkey+"'");
			if(!confRs.exist()){
				BACException.throwAndOutInstance("ȱ�ټٸ��ʲ�����"+confkey);
			}
			return confRs.getString("confvalue");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static FakeBAC instance = new FakeBAC();

	public static FakeBAC getInstance() {
		return instance;
	}
}
