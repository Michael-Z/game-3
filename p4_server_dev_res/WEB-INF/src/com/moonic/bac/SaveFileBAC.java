package com.moonic.bac;

import com.ehc.common.ReturnValue;
import com.moonic.util.FileUtil;

import conf.Conf;

public class SaveFileBAC {
	
	/**
	 * ���ļ�
	 */
	public ReturnValue saveFile(String savepath, byte[] filedata){
		try {
			FileUtil fileutil = new FileUtil();
			fileutil.save(Conf.savepath+savepath, filedata);
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	//--------��̬��----------
	
	private static SaveFileBAC instance = new SaveFileBAC();
	
	public static SaveFileBAC getInstance(){
		return instance;
	}
}
