package com.moonic.bac;

import com.moonic.mirror.MirrorOne;

/**
 * ��������λս
 * @author John
 */
public class JJCRankingBAC extends MirrorOne {
	
	/**
	 * ����
	 */
	public JJCRankingBAC() {
		super("tab_pla_jjcranking", "ranking");
		needcheck = false;
		serverWhere = true;
	}
	
	//------------------��̬��--------------------
	
	private static JJCRankingBAC instance = new JJCRankingBAC();

	public static JJCRankingBAC getInstance() {
		return instance;
	}
}
