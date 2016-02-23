package com.moonic.bac;

import org.json.JSONArray;

import server.common.Tools;

import com.moonic.battle.Const;
import com.moonic.battle.SpriteBox;
import com.moonic.battle.TeamBox;
import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;

/**
 * �������͵���
 * @author wkc
 */
public class Enemy {
	public static final String tab_enemy = "tab_enemy";
	
	/**
	 * �������˶���TeamBox
	 */
	public TeamBox createTeamBox(String enemy) throws Exception{
		return createTeamBox(enemy, null);
	}
	
	/**
	 * �������˶���TeamBox
	 * @param enemy,���˱��String
	 * @param hparr,��Ӧ��ǰѪ��arr
	 */
	public TeamBox createTeamBox(String enemy, JSONArray hparr) throws Exception{
		TeamBox teamBox = new TeamBox();
		teamBox.teamType = 1;
		int[][] enemyarr = Tools.splitStrToIntArr2(enemy, "|", ",");
		byte pos = 1;
		boolean isNull = hparr == null ? true : false;
		for(int i = 0; i < enemyarr.length; i++){
			for(int j = 0; j < enemyarr[i].length; j++){
				if(enemyarr[i][j] != 0){
					if(isNull){
						SpriteBox spriteBox = createEnemyBox(enemyarr[i][j]);
						spriteBox.teamType = 1;
						spriteBox.posNum = pos;
						teamBox.sprites.add(spriteBox);
					} else{
						int currhp = hparr.optInt(pos-1);
						if(currhp > 0){
							SpriteBox spriteBox = createEnemyBox(enemyarr[i][j]);
							spriteBox.teamType = 1;
							spriteBox.posNum = pos;
							spriteBox.battle_prop[Const.PROP_HP] = currhp;
							teamBox.sprites.add(spriteBox);
						}
					}
				}
				pos++;
			}
		}
		return teamBox;
	}
	
	/**
	 * ��������SpriteBox
	 * @param enemynum
	 */
	public SpriteBox createEnemyBox(int enemynum) throws Exception{
		DBPaRs enemyListRs = DBPool.getInst().pQueryA(tab_enemy, "num="+enemynum);
		if(!enemyListRs.exist()){
			BACException.throwInstance("�����ڵĵ��˱��"+enemynum);
		}
		SpriteBox spriteBox = new SpriteBox();
		//��������
		spriteBox.type = 2;
		spriteBox.num = enemyListRs.getInt("num");
		spriteBox.name = enemyListRs.getString("name");
		spriteBox.level = enemyListRs.getShort("lv");
		spriteBox.battletype = enemyListRs.getByte("battletype");
		spriteBox.star = enemyListRs.getByte("star");
		spriteBox.phase = enemyListRs.getByte("quality");
		spriteBox.sex = enemyListRs.getByte("sex");
		//��������
		for(int i = 1; i <= 12; i++){
			spriteBox.addProp(1, i-1, 1, enemyListRs.getInt("prop"+i));
		}
		spriteBox.updateIngredientData("��������");
		//��ʼŭ��
		int initAnger = enemyListRs.getInt("prop13");
		if(initAnger > 0){
			spriteBox.addProp(1, Const.PROP_ANGER, 1, initAnger);
		}
		//����
		String skillStr = enemyListRs.getString("skill");
		if(!skillStr.equals("0")){
			int[][] skillarr = Tools.splitStrToIntArr2(skillStr, "|", ",");
			for(int i = 0; i < skillarr.length; i++){
				spriteBox.addSkill(skillarr[i][0], skillarr[i][1]);
			}
		}
		spriteBox.updateIngredientData("����");
		//���ʼ���
		String skillStrOdds = enemyListRs.getString("odds");
		if(!skillStrOdds.equals("0")){
			int[][] skillarr = Tools.splitStrToIntArr2(skillStrOdds, "|", ",");
			for(int i = 0; i < skillarr.length; i++){
				spriteBox.addSkill(skillarr[i][0], skillarr[i][1]);
			}
		}
		//��������
		String skillStrb = enemyListRs.getString("bskill");
		if(!skillStrb.equals("0")){
			int[][] skillarr = Tools.splitStrToIntArr2(skillStrb, "|", ",");
			for(int i = 0; i < skillarr.length; i++){
				spriteBox.addSkill(skillarr[i][0], skillarr[i][1]);
			}
		}
		//ת��
		spriteBox.conver();
		return spriteBox;
	}
	
	//--------------��̬��--------------
	
	private static Enemy instance = new Enemy();
	
	/**
	 * ��ȡʵ��
	 */
	public static Enemy getInstance(){
		return instance;
	}
}
