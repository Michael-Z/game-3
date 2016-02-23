package com.moonic.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.bac.ConfigBAC;
import com.moonic.servlet.GameServlet;
import com.moonic.socket.PushData;
import com.moonic.util.DBHelper;
import com.moonic.util.MyRunnable;
import com.moonic.util.MyTools;

import conf.Conf;
import conf.LogTbName;

/**
 * һ��ս���Ĺ������
 * 
 * @author huangyan
 * 
 */
public class Battle {
	public byte state;

	long battleId;
	BattleBox battleBox; // ����AB����	
	//public int enemyBatch=0;  //��������
	public Vector<Actor> actorArr; //�ж��߶��У������ظ���ɫ
	public Vector<BattleRole> battleRoleArr; //ս��˫����ɫȫ�弯�ϣ����ظ���ɫ

	public int turnCount; // �غ�������
	public int[] playerIds; // ˫�����id
	public Vector<BattleRole> sortedFighterlist; // ÿ�غϸ����ٶ������ս���Ľ�ɫ	
	long battleStartTime;// ս�������ʱ��
	int actorIndex;
	
	/**
	 * ս����ʼ��
	 */
	public void init(BattleBox battleBox) {
		this.battleBox = battleBox;
		battleStartTime = System.currentTimeMillis();
		
		this.battleBox.createSpriteIds();
		createBattleRoleArrByBattleBox(battleBox);
		
		//���ٶ�����
		Collections.sort(battleRoleArr, new Comparator<BattleRole>() 
		{
			public int compare(BattleRole role1,BattleRole role2)
			{
				return role2.getSpeed() - role1.getSpeed();
			}				
		});
		
		//BattlePrint.print("�����Ľ�ɫ");
		for (int i = 0; i < battleRoleArr.size(); i++) 
		{
			BattleRole role = battleRoleArr.get(i);
			//BattlePrint.print("�����Ľ�ɫ"+role.getId()+" name="+role.getName()+" speed="+role.getSpeed());
			battleRoleArr.get(i).initSkills(); // ��������תΪ����
		}
	}
	
	public void initActors(JSONObject turnJson)
	{
	    actorArr = new Vector<Actor>();
	   
	    JSONArray addBuffData=new JSONArray(); //װսǰ�ӵ�buff��[id,buffnum,id,buffnum]
	    
	    for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	if(battleRole.isLive())
    		{	    		
	    		//BattlePrint.print(battleRole.getId()+"."+battleRole.getName()+" HP="+battleRole.getFinalProp(Const.PROP_HP));
	    				       
		        //BattlePrint.print(battleRole.getName()+".autoSkills.size()="+actor.battleRole.autoSkills.size());
		        if(battleRole.autoSkills.size() > 0)
		        {        		        	
	                boolean haveUseSkill = false;
	                for(int j=0;j<battleRole.autoSkills.size();j++)
	                {    
	                	boolean allowUseSkill=true;
	                	BattleSkill skill = battleRole.autoSkills.get(j);
	                    BattleSkillFixData skillData = skill.battleSkillFixData;	                   
	                    
	                    if(skillData.useTurn!=null)
	                    {
	                    	if(skillData.useTurn[0]==2)  //��һ�غ�ִ��
	                    	{
	                    		allowUseSkill=false;
	                    		if(turnCount==1)
	                    		{
                                    //BattlePrint.print("ִ��"+actor.battleRole.getName()+"��һ�غ�buff����");
                                    int targetType = skillData.targetType;
                                    int rangeType = skillData.range;
                                    if(targetType==Const.TARGET_FRIEND)
                                    {
                                        if(rangeType == Const.RANGE_SELF) //���Լ�
                                        {
                                            for(int k=0;k<skillData.buffs.length;k++)
                                            {
                                            	battleRole.bindBuff(battleRole, skill.level, skillData.buffs[k]);
                                            	addBuffData.add(battleRole.getId());
                                            	addBuffData.add(skill.level);
                                            	addBuffData.add(skillData.buffs[k]);                                            	
                                            }
                                        }
                                    }
                                    else if(targetType==Const.TARGET_ENEMY)
                                    {
                                        if(rangeType == Const.RANGE_FRONT_SINGLE)  //������ĵ���
                                        {
                                        	battleRole.searchTarget(targetType, rangeType);  
                                            if(battleRole.targetRoles!=null)
                                            {
                                                for(int k=0;k<battleRole.targetRoles.size();i++)
                                                {
                                                	for(int m=0;m<skillData.buffs.length;m++)
                                                	{
                                                		battleRole.targetRoles.get(k).bindBuff(battleRole,skill.level,skillData.buffs[m]);
                                                		addBuffData.add(battleRole.targetRoles.get(k).getId());
                                                		addBuffData.add(skill.level);
                                                    	addBuffData.add(skillData.buffs[m]); 
                                                	}                                                	                                                  
                                                }
                                            }
                                        }
                                    }
	                    		}
	                    	}
	                    	else if(skillData.useTurn[0]==3)  //�޶��غ�ִ��
	                    	{
	                    		if(turnCount % skillData.useTurn[1] !=0)
	                    		{
	                    			allowUseSkill=false;
	                    		}	                    		
	                    	}
	                    }
	                    
	                    if(allowUseSkill)
	                    {
		                    int rate = skillData.rate;
		                    //BattlePrint.print(battleRole.getName()+"����"+skill.battleSkillFixData.name+"����="+rate);
		                    int rnd = battleRole.getRandomNumber(1, 100); //���
		                    //BattlePrint.print("�����="+rnd);
		                    if(rnd<=rate)
		                	{     
		                    	Actor actor = new Actor();
		        		        actor.battleRole = battleRole;	
		                    	//BattlePrint.print(battleRole.getName()+"ʹ�ü���"+skill.battleSkillFixData.name);
		                        actor.cmdType = Command.CMD_AUTOSKILL;
		                        actor.battleSkill = skill;                                           
		                        haveUseSkill=true;  
		                        actorArr.add(actor);	
		                	}  
	                    }
	                }
	                
	                //���addbuff
	                if(addBuffData.length()>0)
	                {
	                	JSONObject addBuffJson = new JSONObject();
	                	addBuffJson.put(JSONWrap.KEY.ADD_BUFF, addBuffData);
	                	turnJson.put(JSONWrap.KEY.PRE, addBuffJson);
	                }

	                if(!haveUseSkill)  //û�浽����
	                {
	                	if(inAbnormal(battleRole))
            			{
			            	Actor actor = new Actor();
					        actor.battleRole = battleRole;	
			            	//BattlePrint.print(battleRole.getName()+"û�������������ܣ�ʹ���չ�");                    
			                actor.cmdType = Command.CMD_ATTACK;  
			                actorArr.add(actor);
            			}
	                }
		        }
		        else
		        {
		        	Actor actor = new Actor();
			        actor.battleRole = battleRole;	
	                //print(role.name,"û��������ܣ�ʹ���չ�");
	                actor.cmdType = Command.CMD_ATTACK;
	                actorArr.add(actor);	
		        }
    		}	        
    	}	
	}
	
	/**
	 * ŭ������
	 * @param battleRole
	 */
	public void insertActor(BattleRole battleRole)
	{
		Actor actor = new Actor();
	    actor.battleRole = battleRole;
	    actor.cmdType = Command.CMD_ANGRYSKILL;
	    if(actor.battleRole.angrySkills!=null && actor.battleRole.angrySkills.size()>0)
	    {	    	
	        actor.battleSkill = actor.battleRole.angrySkills.get(0);
		    //���뵽�����ŭ����λ��
		    int insertIndex = actorIndex+1;
		    while(true)
		    {
		        if(insertIndex >= actorArr.size() || actorArr.get(insertIndex).cmdType != Command.CMD_ANGRYSKILL)
		        {
		        	//BattlePrint.print(battleRole.getName()+"ŭ�����ˣ����뵽����"+insertIndex+"λ��");
		        	actorArr.insertElementAt(actor, insertIndex);
		        	break;
		        }  
		        else
		        {
		            insertIndex = insertIndex + 1;
		        }
		    }
	    }	    
	}
	public void printLeftHP()
	{
		//BattlePrint.print("----------��"+turnCount+"�غ�ʣ��Ѫ��-----------");
		for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	//if(battleRole.isLive())
    		{
	    		BattlePrint.print(battleRole.getId()+"."+battleRole.getName()+" HP="+battleRole.getFinalProp(Const.PROP_HP));
    		}
    	}
	}
	public int[] getLeftHPData()
	{		
		int[] leftHP=null;
		for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	leftHP = Tools.addToIntArr(leftHP, battleRole.getId());	    	
	    	leftHP = Tools.addToIntArr(leftHP, battleRole.getFinalProp(Const.PROP_HP));	    	
    	}
		return leftHP;
	}
	
	public JSONObject doPVPBattle() 
	{		
		JSONObject resultJson = new JSONObject();
		try
		{
			JSONArray replayJsonArray = new JSONArray();
			JSONWrap jsonWrap=null;
			turnCount=0;
			while (true) 
			{	
				turnCount++;
				//BattlePrint.print("--------��"+turnCount+"�غ�--------");
				JSONObject turnJson = new JSONObject();
				
				initAllBattleRole();
				//1.�غ�ǰִ�б�������
				JSONArray beSkillResult = new JSONArray();
				execAllBeSkill(beSkillResult);
				if(beSkillResult.length()>0)
				{					
					turnJson.put(JSONWrap.KEY.EXECBESKILL, beSkillResult);
				}
				//BattlePrint.print("JSONWrap.KEY.EXECBESKILL="+turnJson.toString());
				//2.�غ�ǰִ��ȫ����ɫ��buff�����ж���
				JSONArray buffResult = new JSONArray();
				execAllBuff(buffResult);
				
				if(buffResult.length()>0)
				{					
					turnJson.put(JSONWrap.KEY.EXECBUFF, buffResult);
				}
				//BattlePrint.print("JSONWrap.KEY.EXECBUFF="+turnJson.toString());
				
				//3.����ս���Ŷ�
				//BattlePrint.print("ս���Ŷ�");
				initActors(turnJson);	
				
				//BattlePrint.print("----------��"+turnCount+"�غϿ�ʼѪ��-----------");
				//printLeftHP();
				//4.������ִ�б��غϵ�ս����������				
				JSONArray queue = new JSONArray();
				for(int i=0;i<actorArr.size();i++)
				{					
					actorIndex=i;
					Actor actor = actorArr.get(i);		
					//BattlePrint.print(actor.battleRole.getName()+"�ж�,cmdType="+actor.cmdType);
					jsonWrap = actor.doBattle();
					if(jsonWrap!=null)
					{												
						//BattlePrint.print(jsonWrap.getJsonObj().toString());
						queue.add(jsonWrap.getJsonObj());						
					}
				}	
				turnJson.put(JSONWrap.KEY.LEFTHP,getLeftHPData());
				
				//BattlePrint.print("----------��"+turnCount+"�غϽ���Ѫ��-----------");
				//printLeftHP();
				turnJson.put(JSONWrap.KEY.QUEUE,queue);
				
				//5.�غϽ���ִ��buff��ʣ��غ���������
				JSONArray removeBuffResult = new JSONArray();
				reduceAllBuff(removeBuffResult);
				if(removeBuffResult.length()>0)
				{					
					turnJson.put(JSONWrap.KEY.REMOVE_BUFF, removeBuffResult);					
				}				
				
				replayJsonArray.add(turnJson);
				
				int winTeam = isEnd();
				if (winTeam>=0) 
				{	
					turnJson = new JSONObject();
					turnJson.put(JSONWrap.KEY.WIN_TEAM, winTeam);
					replayJsonArray.add(turnJson);
					//sb.append("winTeam="+winTeam+"\r\n");
					//BattlePrint.print("ս������");
					battleBox.winTeam = (byte)winTeam;
					break;
				}
				else 
				if(turnCount>=BattleConfig.maxTurns)
				{
					jsonWrap = new JSONWrap();
					jsonWrap.put(JSONWrap.KEY.WIN_TEAM, Const.teamB);  //���˻غ�������δ��ʤ�������ط�Ӯ
					replayJsonArray.add(jsonWrap.getJsonObj());

					battleBox.winTeam = Const.teamB; 
					break;
				}
			}
			resultJson.put("winTeam", battleBox.winTeam);
			resultJson.put("replay", replayJsonArray);
			//BattlePrint.print(replayJsonArray.toString());
			return resultJson;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * �غ�ǰִ�б�������
	 * @param beSkillResult
	 */
	public void execAllBeSkill(JSONArray beSkillResult)
	{
		for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	battleRole.execBeSkill(beSkillResult);
    	}
	}
	
	public void initAllBattleRole()
	{
		for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	battleRole.init();
    	}
	}
	
	/**
	 * �غ�ǰִ��buff
	 * @param buffResult
	 */
	public void execAllBuff(JSONArray buffResult)
	{
		for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	battleRole.execBuff(buffResult);
    	}
	}
	
	/**
	 * ÿ�غϼ�ʣ��غ���
	 * @param reduceResult
	 */
	public void reduceAllBuff(JSONArray reduceResult)
	{
		for(int i=0;i<battleRoleArr.size();i++)
    	{
	    	BattleRole battleRole = battleRoleArr.get(i);
	    	battleRole.reduceBuffTurns(reduceResult);
    	}
	}
	
	public int isEnd()
	{
	    int[] liveAmount = new int[2];
	    liveAmount[Const.teamA]=0;
	    liveAmount[Const.teamB]=0;
	
	    for(int i=0;i<battleRoleArr.size();i++)
	    {
	        BattleRole battleRole = battleRoleArr.get(i);
	        if(battleRole.isLive())
	        {
	        	liveAmount[battleRole.getTeamType()] = liveAmount[battleRole.getTeamType()]+1;
	        }
	    }
	    if(liveAmount[Const.teamA]>0 && liveAmount[Const.teamB]==0)
	    {
	        return Const.teamA; //A��Ӯ
	    }
	    else if(liveAmount[Const.teamA]==0 && liveAmount[Const.teamB]>0)
	    {
	        return Const.teamB; //B��Ӯ
	    }
	    else
	    {
	        return -1; //��ʤ��
	    }
	}
	
	public String getBattleReplay()
	{
		return null;
	}
	
	public void createBattleRoleArrByBattleBox(BattleBox battleBox) {
		battleRoleArr = new Vector<BattleRole>();
		createBattleRoleArrByBattleTeam(battleRoleArr, battleBox.teamArr[Const.teamA], Const.teamA);
		createBattleRoleArrByBattleTeam(battleRoleArr, battleBox.teamArr[Const.teamB], Const.teamB);		
	}
	
	private void createBattleRoleArrByBattleTeam(Vector<BattleRole> battleRoleList, ArrayList<TeamBox> teamBoxArr, byte teamType) {
		for (int i = 0; teamBoxArr != null && i < teamBoxArr.size(); i++) {
			TeamBox team = teamBoxArr.get(i); //����һ��С�ӣ�P4����ֻ��һ��С��		
			
			for (int k = 0; team.sprites != null && k < team.sprites.size(); k++) {
				SpriteBox sprite = team.sprites.get(k);

				sprite.teamType = teamType;				
				if (sprite != null) {
					BattleRole battleRole = createBattleRole(team, sprite);
					battleRoleList.add(battleRole);					
				}
			}				
		}
	}
	private BattleRole createBattleRole(TeamBox team, SpriteBox sprite) {
		BattleRole battleRole = new BattleRole();
		battleRole.battle = this;		
		
		//sprite.battleRole = battleRole;
		battleRole.spriteBox = sprite;
		battleRole.teamBox = team;		
		battleRole.row = (byte)((sprite.posNum-1) / 3);
		battleRole.col = (byte)((sprite.posNum-1) % 3);
		return battleRole;
	}
	//���쳣״̬��
	public boolean inAbnormal(BattleRole role)
	{
	    if(role.inBuffStatus(Const.BUFF_TYPE_DIZZY) || role.inBuffStatus(Const.BUFF_TYPE_SILENCE))
	    {
	        return true;
	    }
	    else
	    {
	        return false;
	    }	    
	}
	public static void main(String[] args)
	{
		Vector testVC = new Vector();
		for(int i=0;i<5;i++)
		{
			testVC.add(i);
		}
		for(int i=0;i<testVC.size();i++)
		{
			if(i==3)
			{
				testVC.add(5, "a");
			}
			//BattlePrint.print(testVC.get(i));
		}
	}

}
