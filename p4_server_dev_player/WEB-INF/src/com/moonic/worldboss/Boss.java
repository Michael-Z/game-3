package com.moonic.worldboss;

import com.moonic.bac.Enemy;
import com.moonic.battle.TeamBox;

/**
 * Boss
 * @author wkc
 */
public class Boss {
	
	public TeamBox teamBox;//����TeamBox
	
	/**
	 * ����
	 * @throws Exception
	 */
	public Boss() throws Exception{
		this.teamBox = Enemy.getInstance().createTeamBox("9001");
	}
	
}
