package com.moonic.battle;

/**
 * ս���ж��� 
 *
 */
public class Actor {
	public BattleRole battleRole;  //��Ӧ�Ľ�ɫ
	public int cmdType;	
	public BattleSkill battleSkill;
	
	public JSONWrap doBattle()
	{
	    battleRole.actor = this;	    
	    return battleRole.doBattle();
	}
}
