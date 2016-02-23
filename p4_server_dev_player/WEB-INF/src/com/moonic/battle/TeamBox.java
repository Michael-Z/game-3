package com.moonic.battle;

import java.util.ArrayList;

import org.json.JSONArray;

import com.moonic.bac.*;

/**
 * һ�����С�ӵ�������
 * @author John
 */
public class TeamBox 
{
	public byte teamType;//��������
	
	public int playerid;
	public String pname;
	public int pnum;//ͷ����
	
	//��ս��[NPC���ͣ��ȼ���ͷ���ţ�ƣ�Ͷȣ�ս�����ۼƼ������ٷֱȣ�����ID�����ڳ��б�ţ�������]
	public JSONArray parameterarr;//�Զ������
	
	public ArrayList<SpriteBox> sprites = new ArrayList<SpriteBox>();//��ս��鼯��
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getJSONArray(){
		JSONArray spritesarr = new JSONArray();
		for(int i = 0; i < sprites.size(); i++){
			spritesarr.add(sprites.get(i).getJSONArray());
		}
		int power=0;
		try
		{
			power = PartnerBAC.getInstance().getTeamBoxBattlePower(this);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		JSONArray dataarr = new JSONArray();
		dataarr.add(teamType);
		dataarr.add(playerid);
		dataarr.add(pname);
		dataarr.add(power);
		dataarr.add(spritesarr);
		dataarr.add(pnum);
		return dataarr;
	}
	
	/**
	 * ��ȡ�����Ҫ�����ַ���
	 */
	public String getTeamDataStr(){
		StringBuffer sb = new StringBuffer();
		sb.append(pname+"("+playerid+")");
		sb.append("[");
		for(int i = 0; i < sprites.size(); i++){
			SpriteBox spritebox = sprites.get(i);
			sb.append(spritebox.name+"("+spritebox.partnerId+"),");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * ��ȡ���ָ��������ֵ
	 */
	public int getTotalPropValue(byte type){
		int value = 0;
		for(int i = 0; i < sprites.size(); i++){
			SpriteBox spritebox = sprites.get(i);
			value += spritebox.battle_prop[type];
		}
		return value;
	}
	
	/**
	 * ��ȡ�������������
	 */
	public JSONArray getDiedPartner(){
		JSONArray arr = new JSONArray();
		for(int i = 0; i < sprites.size(); i++){
			SpriteBox spritebox = sprites.get(i);
			if(spritebox.battle_prop[Const.PROP_HP] <= 0){
				arr.add(spritebox.partnerId);		
			}
		}
		return arr;
	}
	
	/**
	 * ��ȡ�������վλ
	 */
	public JSONArray getNumFormaction() throws Exception {
		JSONArray arr = new JSONArray();
		for(int i = 0; i < sprites.size(); i++){
			SpriteBox spritebox = sprites.get(i);
			arr.put(spritebox.posNum-1, spritebox.num);
			//System.out.println(spritebox.name+" "+spritebox.posNum+" "+spritebox.num);
		}
		//System.out.println("-------------------");
		return arr;
	}
}
