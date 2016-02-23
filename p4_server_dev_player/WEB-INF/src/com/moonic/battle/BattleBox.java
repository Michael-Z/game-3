package com.moonic.battle;

import java.util.ArrayList;

import org.json.JSONArray;

import conf.Conf;

/**
 * ս��������
 * @author John
 */
public class BattleBox 
{	
	public int bgnum = 1;//ս������
	
	public boolean mustSaveLog;//�Ƿ�ض���ս����־
	
	public ArrayList<TeamBox>[] teamArr;//����˫������,���TeamBox[0],����TeamBox[1] ����ĳ���ж�����飬��Ҫȫ�������Ҳ������Ѫ
	
	public JSONArray parameterarr;//�Զ������
	
	//---����Ϊս����������Ҫ��ֵ������---
	
	public long battleId = createBattleId();//ս��ID
	
	public byte winTeam;//ʤ���Ķ���
	
	public JSONArray replayData;//¼������ �ṹ��JSONARRAY[��һ��¼�񣬵ڶ���¼��...��N��¼��]
	
	private static long startBattleId = Conf.sid * 10000000000000L + System.currentTimeMillis(); //��ʼս��id
	
	/**
	 * ����BATTLEID
	 */
	private static synchronized long createBattleId(){
		return startBattleId++;
	}
	
	/**
	 * ����
	 */
	public BattleBox(){
		teamArr = new ArrayList[2];
		teamArr[0] = new ArrayList<TeamBox>();
		teamArr[1] = new ArrayList<TeamBox>();
	}
	
	/**
	 * ��ȡ����
	 */
	public JSONArray getJSONArray(){		
		createSpriteIds(); //����ÿ��sprite��ɫ��Ψһid
		
		JSONArray teamsarr = new JSONArray();
		for(int i = 0; i < teamArr.length; i++){
			JSONArray tarr = new JSONArray();
			for(int k = 0; k < teamArr[i].size(); k++){
				tarr.add(teamArr[i].get(k).getJSONArray());
			}
			teamsarr.add(tarr);
		}
		JSONArray dataarr = new JSONArray();
		dataarr.add(bgnum);
		dataarr.add(teamsarr);
		return dataarr;
	}
	
	/**
	 * ����˫����ɫ��Ψһid����
	 */
	public void createSpriteIds()
	{
		int id=1;
		
		for (int i = 0; teamArr != null && i < teamArr.length; i++)   //����AB����
		{
			ArrayList<TeamBox> teamBoxArr = teamArr[i];
			for(int j=0;j<teamBoxArr.size();j++)  //����A��B��ĳС�ӣ�P4����ֻ��һ��С��
			{
				TeamBox team = teamBoxArr.get(j);
				
				for (int k = 0; team.sprites != null && k < team.sprites.size(); k++)  //������ɫ 
				{
					SpriteBox sprite = team.sprites.get(k);
					sprite.id = id;
					id++;
				}				
			}			
		}				
	}
}
