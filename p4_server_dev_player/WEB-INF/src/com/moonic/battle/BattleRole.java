package com.moonic.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

import server.common.Tools;

import com.moonic.socket.PushData;

/**
 * ս����ɫ�߼�����
 * @author huangyan
 *
 */
public class BattleRole 
{	
	//public int id; //id����ǰս����Ψһ��ɫid��
	public SpriteBox spriteBox; //����	
	public TeamBox teamBox; //�����Ķ�
	public Battle battle; //������ս��
	
	public byte state;	
    

    public Command currCommand;//�����ж�ָ�����
    //public BattleSkill currBattleSkill; //��ǰʹ���еļ���   
    
    public ArrayList<BattleSkill> autoSkills; //�������   
    public ArrayList<BattleSkill> angrySkills; //ŭ����
    public ArrayList<BattleSkill> beSkills; //��������
    

    public ArrayList<Buff> buffArr; //�е���buff״̬    
    
    public int orderPriority; //�������ȼ�
    
    private Random ran;
    
    public int[] prop_change; //���Եı仯ֵ
    
    private static Object lock=new Object();
    private static int seed;
    
    private Vector<BattleRole> frontEnemy;
    private Vector<BattleRole> backEnemy;
    private Vector<BattleRole> allEnemy;
    private Vector<BattleRole> frontFriend;
    private Vector<BattleRole> backFriend;
    private Vector<BattleRole> allFriend;
    
    Vector<BattleRole> targetRoles;
    
    byte row;
    byte col;
    
    public Actor actor; //�ж���
    
    public boolean waitUseAngry; //�ȴ���ŭ����
    
    public boolean isChangeBody;
    
    //ս�����������ݱ仯�� �ͻ��˷���ã�
    public JSONArray removeBuffNumArr;  //������ȥ��buff��¼  [roleId,buffNum]
    public JSONArray blueBloodArr;  //���ƿ�Ѫ��¼  [roleId,shellHarm]
    public JSONArray addBuffNumArr;  //�������buff��¼  [roleId,buffNum]
    
    public BattleRole()
    {    	
		ran = new Random();
		synchronized (lock)  //ȷ��ͬʱ���ɵĽ�ɫҲ�в�ͬ�����������
		{
			seed++;
			ran.setSeed(seed);  
		}
		
		prop_change = new int[SpriteBox.BATTLE_PROP_LEN];
		
    } 
    
    public void init()
    {
    	removeBuffNumArr = new JSONArray();
    	blueBloodArr = new JSONArray();
    	addBuffNumArr = new JSONArray();
    }
    
    /**
     * �Ƿ�ǰ��
     * @return
     */
    public boolean isFront()
    {
    	if(row==0)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }    
   
    public int getId()
    {
    	if(spriteBox!=null)
    	{
    		return spriteBox.id;
    	}
    	return 0;
    }
    public int getLevel()
    {
    	return spriteBox.level;
    }  
    
    public void initSkills()
    {
    	autoSkills = new ArrayList<BattleSkill>();
    	angrySkills = new ArrayList<BattleSkill>();
    	beSkills = new ArrayList<BattleSkill>();
    	//BattlePrint.print("spriteBox.autoSkills.length()="+spriteBox.autoSkills.length());
    	//BattlePrint.print("spriteBox.angrySkills.length()="+spriteBox.angrySkills.length());
    	
    	if(spriteBox.autoSkills.length()>0)
    	{
    		for(int i=0;i<spriteBox.autoSkills.length();i++)
    		{
    			JSONArray arr = (JSONArray)spriteBox.autoSkills.opt(i);
    			BattleSkill autoSkill=BattleSkillLib.getSkill(arr.optInt(0), arr.optInt(1));
    	    	autoSkills.add(autoSkill);
    		}
    	}
    	
    	if(spriteBox.angrySkills.length()>0)
    	{
    		for(int i=0;i<spriteBox.angrySkills.length();i++)
    		{
    			JSONArray arr = (JSONArray)spriteBox.angrySkills.opt(i);
    			BattleSkill angrySkill=BattleSkillLib.getSkill(arr.optInt(0), arr.optInt(1));
    			angrySkills.add(angrySkill);
    		}
    	} 
    	
    	if(spriteBox.beSkills.length()>0)
    	{
    		for(int i=0;i<spriteBox.beSkills.length();i++)
    		{
    			JSONArray arr = (JSONArray)spriteBox.beSkills.opt(i);
    			BattleSkill beSkill=BattleSkillLib.getSkill(arr.optInt(0), arr.optInt(1));
    			beSkills.add(beSkill);
    		}
    	} 
    }
	
    public boolean canFight()
    {
    	if(getFinalProp(Const.PROP_HP)>0  && !isChangeBody)
    	{    		
    		return true;
    	}    	
    	else
    	{
    		return false;
    	}
    }
    
    public void addBuff(BattleRole fromRole,Buff addBuff)
    {
        if (buffArr == null)
        {
            buffArr = new ArrayList<Buff>();
        }            

        //������ͬ���buff����ȥ��
        for (int i = buffArr.size()-1;i >=0; i--)
        {
            Buff buff = (Buff)buffArr.get(i);
            if (buff.buffFixData.buffGroup == addBuff.buffFixData.buffGroup)
            {
                buffArr.remove(i);                    
            }
        }
        
        //�ͻ��˷������
        //BattlePrint.print(fromRole.getName()+".addBuffNumArr"+this.getId()+" "+addBuff.buffFixData.num);
        if(fromRole!=null)
        {
        	fromRole.addBuffNumArr.add(this.getId());
        	fromRole.addBuffNumArr.add(addBuff.buffFixData.num);
        }
        //BattlePrint.print(fromRole.getName()+".addBuffNumArr.length()="+fromRole.addBuffNumArr.length());
        //BattlePrint.print(this.getName()+"��"+fromRole.getName()+"��buff"+addBuff.buffFixData.name);
        buffArr.add(addBuff);        
    }
    /// <summary>
    /// ȥ��ȫ��buffЧ��
    /// </summary>
    public void removeAllBuff()
    {       
        if(buffArr != null)buffArr.clear();
    }
    public void removeBuff(Buff buff)
    {       
        if(buffArr != null)
        {
        	buffArr.remove(buff);
        }
    }
    /// <summary>
    /// buffÿ�ֶ����Եı仯�����ж�����Ѫ��ħ��
    /// </summary>
    /// <returns>��ʾbuff������Ч���������</returns>
    
    private void addBuffResult(JSONArray buffResult,int roleId,int type,int value)
    {
    	if(buffResult!=null)
    	{	    	
    		buffResult.add(roleId);
    		buffResult.add(type);
    		buffResult.add(value);	    	
    	}
    }
    /**
     * ÿ�غ�ǰִ�б�������
     * @param buffResult
     */
    public void execBeSkill(JSONArray changeResult)
    {
    	//todo buffResult�Ӵ�����
    	if(isLive())
    	{
    		for(int i=0;beSkills!=null && i<beSkills.size();i++)
	        {
	        	BattleSkill skill = beSkills.get(i);	
	           
	            int beSkillLevel = skill.level;
	            BattleSkillFixData beSkillData = skill.battleSkillFixData;
	           
                if(beSkillData.owner ==2) 
                {
                    for(int j=0;j<beSkillData.args.length;j++)
                    {
                        short[] arg = beSkillData.args[j];
                        short t = arg[0];

                        if(t == Const.BESKILL_TYPE9) //9.ÿ�غ��ж�ǰ�ָ�ָ��ֵ��%������������9,��������(0,�������ֵ|1,��ʧ����ֵ),�ָ��ٷֱ�,���������ٷֱ�
                        {
                            int baseV = 0;
                            if(arg[1] == 0)  //�������ֵ
                            {
                                baseV = this.getFinalProp(Const.PROP_MAXHP);
                            }
                            else if(arg[1] == 1)  //��ʧ����ֵ
                            {
                                baseV = this.getFinalProp(Const.PROP_MAXHP) - this.getFinalProp(Const.PROP_HP);
                            }
                            int addHP = (int)(baseV * (arg[2] + (beSkillLevel -1 ) * arg[3])/100);
                            if(addHP >0)
                            {
                                changeHP(addHP);
                                changeResult.add(this.getId());
                                changeResult.add(Const.EXEC_RESULT_TYPE_HP);
                                changeResult.add(addHP);	  
                            }
                        }
                        else if(t == Const.BESKILL_TYPE12)  //12.ÿ�غ��Զ��ָ�ŭ����������12,�ָ�ŭ��ֵ
                        {
                            changeAngry(arg[1]);
                            changeResult.add(this.getId());
                            changeResult.add(Const.EXEC_RESULT_TYPE_ANGRY);
                            changeResult.add(arg[1]);
                        }
                        else if(t == Const.BESKILL_TYPE13)  //13.�ض�ʱ����BUFF��������13,��0,�غ�ǰ|��0��������ֵ���ʾָ���غϼ��䱶����,BUFF���
                        {
                            if(arg[1]==0)
                            {
                                if(battle.turnCount==1)
                                {
                                    bindBuff(null,beSkillLevel,arg[2]);
                                    changeResult.add(this.getId());
                                    changeResult.add(Const.EXEC_RESULT_TYPE_ADDBUFF);
                                    changeResult.add(arg[2]);
                                }
                            }
                            else 
                            {
                                if(battle.turnCount % arg[1] ==0)
                                {
                                    bindBuff(null,beSkillLevel,arg[2]);
                                    changeResult.add(this.getId());
                                    changeResult.add(Const.EXEC_RESULT_TYPE_ADDBUFF);
                                    changeResult.add(arg[2]);
                                }
                            }
                        }                         
                	}
            	}  
        	}
        }    	
    }
    /**
     * ÿ�غ�ǰִ��buff
     */
    public void execBuff(JSONArray buffResult)
    {       
        //�����ж���Ѫ��buff
        for (int i = 0; buffArr != null && i < buffArr.size(); i++)
        {
            Buff buff = (Buff)buffArr.get(i);
            if(isLive())
            {            	
            	for(int j=0;j<buff.buffFixData.buffTypes.length;j++)
            	{
                    if(buff.buffFixData.buffTypes[j][0] == Const.BUFF_TYPE_LOSTHP)
                    {
                        int t = buff.buffFixData.args[j][0];
                        int v = buff.buffFixData.args[j][1];
                        int lvup = buff.buffFixData.args[j][2];
                        int harm=0;
                        if(t==0)
                        {
                            harm = (int)Math.ceil(buff.fromRoleAtk * (v + (buff.level - 1) * lvup)/100);
                        }
                        else
                        {
                            harm = v + (buff.level - 1) * lvup;
                        }

                        //�������ܸı��˺�
                        int[] harmTypes = new int[]{1,Const.HARM_TYPE_DEBUFF};                        
                        harm = getChangeHarmByBeSkill(null,harm,harmTypes);  

                        if(harm > 0)
                        {                        	
                        	changeHP(-harm);
                        	if(harm!=0)
                            {                        		
                        		BattlePrint.print("��"+battle.turnCount+"�غ�"+this.getName()+"�غ�ǰbuff��Ѫ"+harm);
                            	addBuffResult(buffResult,getId(),Const.EXEC_RESULT_TYPE_HP,-harm);
                            }                                                   
                        }
                    }
            	}            	           
            }            
            buff.haveExeced=true;
        }               
    }
    

    /**
     * �������ܶ��˺��ĸı�
     * @param attackRole
     * @param harm
     * @param harmTypes
     * @return
     */
    public int getChangeHarmByBeSkill(BattleRole attackRole,int harm ,int[] harmTypes)
    {
	    //harmTypes : 0,ȫ���˺�|1,ָ�����˺�|2,DEBUFF�˺�|3,�����˺�|4,�����˺�
	
	    //�����ߵı������ܶ��˺��ĸı�
	    if(attackRole !=null && attackRole.beSkills!=null)
	    {
	        for(int i=0;i<attackRole.beSkills.size();i++)
	        {
	        	BattleSkill skill = attackRole.beSkills.get(i);	  
	        	int beSkillLevel = skill.level;
	            BattleSkillFixData beSkillData = skill.battleSkillFixData;
	           
	            if(beSkillData.owner ==2)  //���˳�����ʱ�����ı�������
	            {  
	                for(int j=0;j<beSkillData.args.length;j++)
	                {
	                    short[] arg = beSkillData.args[j];
	                    short t = arg[0];
	
	                    if(t == Const.BESKILL_TYPE3)  //3.��ָ��ְҵ�����˺�ֵ�仯��������3,ְҵ���,(1,����|-1,����),��ʼЧ������ֵ,����Ч������ֵ
	                    {	                    	
	                        if(arg[1] == this.getSeries())
	                        {
	                            short updown = arg[2];
	                            short v = arg[3];
	                            short lvup = arg[4];
	                            harm = (int)(harm + harm * (float)(updown * (v + (beSkillLevel -1) * lvup))/100);
	                        }
	                    }
                        else if(t == Const.BESKILL_TYPE4)  //4.�Ե�ָ���Ա������˺�ֵ�仯��������4,�Ա���,(1,����|-1,����),��ʼЧ������ֵ,����Ч������ֵ
                        {
	                        if(arg[1] == getSex())
	                        {
	                        	short updown = arg[2];
	                        	short v = arg[3];
	                        	short lvup = arg[4];
	                            harm = (int)(harm + harm * (float)(updown * (v + (beSkillLevel -1) * lvup))/100);
	                        }                            
                        }
                        else if(t == Const.BESKILL_TYPE6)   //6.�Ե�ǰ����ֵָ���ٷֱ����ϵ�λ�ӳ������˺���������6,��������,�ӳɾ���ֵ,������������ֵ (�˺�ֵ + �ӳ�=�����˺�)
                        {
                        	short percent = arg[1];
                        	short v = arg[2];
                        	short lvup = arg[3];
	                        if((float)getFinalProp(Const.PROP_HP) / getFinalProp(Const.PROP_MAXHP) >= (float)percent/100)
	                        {
	                            harm = (int)(harm + harm * (float)(v + (beSkillLevel -1) * lvup)/100);
	                        }
                        }
	                }            
	            }	            
	        }
	    }
	    //�����߱�������
	    if(beSkills!=null)
	    {
	        for(int i=0;i<beSkills.size();i++)
	        {
	        	BattleSkill skill = beSkills.get(i);	  
	        	int beSkillLevel = skill.level;
	            BattleSkillFixData beSkillData = skill.battleSkillFixData;
	           
	            
	            if(beSkillData.owner ==1) //���˳�����ʱ�����ı������� //�����ߵı������ܶ��˺��ĸı�
	            {
	            	for(int j=0;j<beSkillData.args.length;j++)
	            	{
		                short[] arg = beSkillData.args[j];
		                short t = arg[0];
		
		                if(t == Const.BESKILL_TYPE3) //3.��ָ��ְҵ�����˺�ֵ�仯��������3,ְҵ���,(1,����|-1,����),��ʼЧ������ֵ,����Ч������ֵ
		                {
		                    if(attackRole!=null && arg[1] == attackRole.getSeries())
		                    {
		                        short updown = arg[2];
		                        short v = arg[3];
		                        short lvup = arg[4];
		                        harm = (int)(harm + harm * (float)(updown * (v + (beSkillLevel -1) * lvup))/100);
		                    }
		                }
		                else if(t == Const.BESKILL_TYPE4) //4.�Ե�ָ���Ա������˺�ֵ�仯��������4,�Ա���,(1,����|-1,����),��ʼЧ������ֵ,����Ч������ֵ
		                {
		                    if(attackRole!=null && arg[1] == attackRole.getSex())
		                    {
		                        short updown = arg[2];
		                        short v = arg[3];
		                        short lvup = arg[4];
		                        harm = (int)(harm + harm * (float)(updown * (v + (beSkillLevel -1) * lvup))/100);
		                    }
		                }
		                else if(t == Const.BESKILL_TYPE5)  //5.�����ܵ�ָ�������˺�ֵ�仯��������5,��������,�ȼ�����ֵ,�˺�����(0,ȫ���˺�|1,ָ�����˺�|2,DEBUFF�˺�|3,�����˺�|4,�����˺�),��������(1,����|-1,����),��ʼ����ֵ,������������ֵ
		                {		                	
		                    short rate = (short)(arg[1] + (beSkillLevel - 1) * arg[2]);
		                    short rnd = (short)Tools.getRandomNumber(1,100);		                    
		                    
		                    if(rnd <= rate)
		                    {
		                        short harmType = arg[3];
		                        boolean check = false;
		                        if(harmType ==0)
		                        {
		                            check = true;
		                        }
		                        else
		                        {
		                            check = Tools.intArrContain(harmTypes,harmType);
		                        }
		                        if(check)
		                        {
		                            short updown = arg[4];
		                            short v = arg[5];
		                            short lvup = arg[6];
		                            
		                            harm = (int)(harm + harm * (float)(updown * (v + (beSkillLevel -1) * lvup))/100);
		                        }
		                    }
		                }
		                else if(t == Const.BESKILL_TYPE7)   //7.����ֵ����ָ���ٷֱ�ʱ�����ܵ��˺���������7,��������,����ٷֱ�,���������ٷֱ�
		                {
		                    short percent = arg[1];
		                    short v = arg[2];
		                    short lvup = arg[3];
		                    if((float)getFinalProp(Const.PROP_HP) / getFinalProp(Const.PROP_MAXHP) <= (float)percent/100)
		                    {
		                        harm = (int)(harm - harm * (float)(v + (beSkillLevel -1) * lvup)/100);
		                    }                    
		                }
	            	} 
	            }
	        }
	    }
	
	    if(harm <0)
	    {
	        harm=0;
	    }
	
	    return harm;
    }
   
    /**
     * ÿ�غϽ������buffʣ�����
     */
    public void reduceBuffTurns(JSONArray array)
    {
    	for (int i = 0; buffArr != null && i < buffArr.size(); i++)
        {
            Buff buff = (Buff)buffArr.get(i);
            if(buff.haveExeced)
            {
            	buff.turns--;  //ʣ��غ�����һ
            	//BattlePrint.print(getName()+"��buff "+buff.buffFixData.name+" ʣ��غ���="+buff.turns);
            	//battle.addDeailStrData(getName()+"("+id+")"+"��"+buff.buffFixData.name+"BUFFʣ��غ���="+buff.turns);
            }
        }
    	clearBuff(array);
    }
    private void clearBuff(JSONArray array)
    {
    	//ʣ��غ���Ϊ0��ȥ��
        if (buffArr != null && buffArr.size() > 0)
        { 
            for (int i = buffArr.size()-1; buffArr != null && i>=0; i--)
            {
                Buff buff = (Buff)buffArr.get(i);
                if (buff.turns == 0)
                {                	
					array.add(getId());
					array.add(buff.buffFixData.num);	
					if(buff.isChangeBuff)  //�Ǳ�ɫbuff�軹ԭ״̬
					{
						this.isChangeBody=false;
					}
					//BattlePrint.print(getName()+"���Ƴ�buff"+buff.buffFixData.name);
                    buffArr.remove(i);                   
                }        
            }
        } 
    }
    /**
     * ����ɫ�Ƿ�����ĳ�쳣״̬��
     * @param buffType
     * @return
     */
    public boolean isInAbnormalStatus(int buffType)
    {
    	for (int i = 0; buffArr != null && i < buffArr.size(); i++)
        {
            Buff buff = (Buff)buffArr.get(i);
            for(int j=0;j<buff.buffFixData.buffTypes.length;j++)
            {
                if(buff.buffFixData.buffTypes[j][0] == buffType)
                {
                    return true;
                }
            }            
        }
        return false;
    }
    /**
     * ����ɫ�Ƿ�����ĳ��ŵ�buff
     * @param buffNum
     * @return
     */
    public boolean isInBuff(int buffNum)
    {
    	for (int i = 0; buffArr != null && i < buffArr.size(); i++)
        {
            Buff buff = (Buff)buffArr.get(i);
            if(buff.buffFixData.num == buffNum)
            {
            	return true;
            }
        }
        return false;
    }   

    /// <summary>
    ///  ��ȡ����buff������ӳɺ����������ֵ
    /// </summary>
    /// <param name="propIndex"></param>
    /// <returns></returns>
    public int getFinalProp(byte propIndex)
    {
        int orgProp = getProp(propIndex); 
        int addProp = getBuffAddProp(propIndex);
        if(addProp!=0)
        {
        	//BattlePrint.print(this.getName()+"������"+propIndex+"��buff�仯"+addProp);
        }
        //updatePropChange();
        int addProp2 = prop_change[propIndex];
        //BattlePrint.print(getName()+"������"+propIndex+" BuffAddProp="+addProp);
        int finalProp = orgProp + addProp+addProp2;
        if (finalProp < 0) finalProp = 0;
        return finalProp;
    }  
    
	
	//���buff�ı���˺�,���ؽ�ɫ���˺����ܵ��˺�
	public int getChangeHarmByBuff(BattleRole attackRole,int harm)
	{
	    if(buffArr == null)
	    {
	        return harm;
	    }
	    int roleHarm=harm;
	    int shellHarm=0;
	
	    ArrayList<Buff> removeBuffList = new ArrayList<Buff>();    
	
	    for(int i = 0;i<buffArr.size();i++)
	    {
	       Buff buff = buffArr.get(i);
	       for(int j=0;j<buff.buffFixData.buffTypes.length;j++)
	       {
	            if(buff.buffFixData.buffTypes[j][0] ==Const.BUFF_TYPE_SHELL)
	            {	                
	                int hp = buff.shellHP;
	                //BattlePrint.print(this.getName()+"�Ķ���Ѫ="+buff.shellHP);
	                if(hp>0)
	                {
	                   if(buff.shellHP >= roleHarm)
	                   {
	                        buff.shellHP = buff.shellHP - roleHarm;
	                        if(buff.shellHP<=0)
	                        {
	                        	removeBuffList.add(buff);	                            
	                        }
	                        shellHarm = roleHarm;
	                        attackRole.blueBloodArr.add(this.getId());
	                        attackRole.blueBloodArr.add(shellHarm);
	                        roleHarm =0;
	                        //BattlePrint.print(getName()+"�Ķ��Ʊ�"+attackRole.getName()+"���"+shellHarm+"Ѫ,��ʣ"+buff.shellHP);
	                   }
	                   else
	                   {
	                        roleHarm = roleHarm - hp;
	                        shellHarm = hp;
	                        attackRole.blueBloodArr.add(this.getId());
	                        attackRole.blueBloodArr.add(shellHarm);
	                        
	                        buff.shellHP = 0;
	                        removeBuffList.add(buff);
	                        //BattlePrint.print(getName()+"�Ķ��Ʊ�"+attackRole.getName()+"��");
	                   }
	                }
	            }
	            else if( buff.buffFixData.buffTypes[j][0] ==Const.BUFF_TYPE_CHANGEHARM)
	            {
	                int updown = buff.buffFixData.updown;
	                int t = buff.buffFixData.args[j][0];
	                int v = buff.buffFixData.args[j][1];
	                int lvup = buff.buffFixData.args[j][2];
	                //BattlePrint.print(this.getName()+"�ı��˺�buff t="+t+",v="+v+",lvup="+lvup+",buff.level="+buff.level);
	                //BattlePrint.print("ԭroleHarm="+roleHarm);
	                if(t==0)  //�ٷֱ�
	                {
	                    roleHarm = (int)Math.ceil(roleHarm + updown * roleHarm * (v + (buff.level-1) * lvup)/100);
	                }
                    else  //����ֵ
                    {
	                    roleHarm = (int)Math.ceil(roleHarm + updown * (v + (buff.level-1) * lvup));
	                    if(roleHarm <0)
	                    {
	                        roleHarm=0;
	                    }
                    }
	                //BattlePrint.print("�仯���roleHarm="+roleHarm);
	            }
	       }
	    }
	    
	    for(int i=0;i<removeBuffList.size();i++)
	    {	    	
	    	//��¼��json���ͻ���
	    	attackRole.removeBuffNumArr.add(this.getId());
	    	attackRole.removeBuffNumArr.add(((Buff)removeBuffList.get(i)).buffFixData.num);
            
            removeBuff((Buff)removeBuffList.get(i));            
	    }
	
	    return roleHarm;	    
	}
   
    
    /// <summary>
    /// ��ȡ����buff�ӳɵ�����addֵ
    /// </summary>
    /// <param name="propIndex">��0��ʼ����������</param>
    /// <returns></returns>
    public int getBuffAddProp(int propIndex)
    {
    	int add = 0;  
    	
        if (propIndex >= Const.PROP_MAXHP && propIndex <= Const.PROP_BECRIT_SUB_DAMAGE)
        {                      
            for (int i = 0; buffArr != null && i < buffArr.size(); i++)
            {
                Buff buff = (Buff)buffArr.get(i);
                
                for(int j=0;j<buff.buffFixData.buffTypes.length;j++)
                {
                    if(buff.buffFixData.buffTypes[j][0] == Const.BUFF_TYPE_PROP)
                    {
                        if(buff.buffFixData.buffTypes[j][1] == propIndex)
                        {                         
                            byte updown = buff.buffFixData.updown;                        
                            int[] arg = buff.buffFixData.args[j];
                            int t=arg[0];
                            int v = arg[1];
                            int lvup = arg[2];
                            if(t==0) //���ٷֱ���
                            {
                                add = add + getProp(propIndex) * updown * (v +(buff.level - 1) * lvup)/100;
                            }
                            else  //������ֵ��
                            {
                                add = add + updown * (v +(buff.level - 1) * lvup);
                            }
                        }
                    }
                }  
            }               
        }
        if(add!=0)
        {
        	//battle.addDeailStrData(getName()+"("+id+")��"+propNames[propIndex-2]+"�����ձ仯ֵΪ"+getProp(propIndex)+"+("+add+")"+"->"+(getProp(propIndex)+add));
        	//BattlePrint.print(getName()+"("+id+")��"+propNames[propIndex-2]+"�����ձ仯ֵΪ"+getProp(propIndex)+"+("+add+")"+"->"+(getProp(propIndex)+add));
        }    	
        return add;
    }
   
    public void setState(int theState)
    {
    	this.state = (byte)theState;
    }
    public byte getTeamType()
    {
    	return spriteBox.teamType;
    }   
    
    public int getSpeed()
    {
    	return getFinalProp(Const.PROP_SPEED);
    } 
    
    /**
     * �ռ�ǰ���ŵ��ҽ�ɫ����
     */
    public void collectEnemys()
    {
    	frontEnemy = new Vector<BattleRole>();
        backEnemy = new Vector<BattleRole>();
        allEnemy = new Vector<BattleRole>();
        frontFriend = new Vector<BattleRole>();
        backFriend = new Vector<BattleRole>();
        allFriend = new Vector<BattleRole>();
    	
        for(int i=0;i<battle.battleRoleArr.size();i++)
        {
        	BattleRole battleRole = battle.battleRoleArr.get(i);
        	if(battleRole.getFinalProp(Const.PROP_HP)>0)
        	{
        		if(battleRole.getTeamType() != this.getTeamType())
        		{
        			if(battleRole.isFront())
        			{
        				frontEnemy.add(battleRole);
        			}
        			else
        			{
        				backEnemy.add(battleRole);
        			}
        			allEnemy.add(battleRole);
        		}
        		else
        		{
        			if(battleRole.isFront())
        			{
        				frontFriend.add(battleRole);
        			}
        			else
        			{
        				backFriend.add(battleRole);
        			}
        			allFriend.add(battleRole);
        		}
        	}
        }
    } 

	/**
	 * ���л���������Ŀ��
	 * @param team
	 * @return
	 */
	public Vector<BattleRole> findColTargets(int team)
	{
		Vector<BattleRole> tmp = new Vector<BattleRole>();
		
	    BattleRole[][] grid=new BattleRole[2][3];
	    
	    for(int i=0;i<battle.battleRoleArr.size();i++)
	    {
	    	BattleRole role = battle.battleRoleArr.get(i);
	        if(role.getTeamType() == team)
	        {
	            if(role.getFinalProp(Const.PROP_HP)>0)
	            {
	                grid[role.row][role.col] = role;           
	            }
	        }
	    }

	    //������ǰ��λ��  
	    int targetCol = 2 - col;
	    if(grid[0][targetCol]!=null && grid[1][targetCol]!=null)
	    {
	    	tmp.add(grid[0][targetCol]);
	    	tmp.add(grid[1][targetCol]);
	        return tmp;
	    }

	    if(grid[0][targetCol]!=null)
	    {
	    	tmp.add(grid[0][targetCol]);
	        return tmp;
	    }

	    if(grid[1][targetCol]!=null)
	    {
	    	tmp.add(grid[1][targetCol]);
	        return tmp;
	    }
	    
	    //������2���˵���
	    for(int col=0;col<3;col++)
	    {
	        if(grid[0][col]!=null && grid[1][col]!=null)
        	{
	        	tmp.add(grid[0][col]);
	        	tmp.add(grid[1][col]);
	        	return tmp;        	   
        	}
	    }
	    
	    //��ǰ��
	    for(int col=0;col<3;col++)
	    {
	        if(grid[0][col]!=null)
	        {
	        	tmp.add(grid[0][col]);	       
	            return tmp;
	        }        
	    }
	    //�Һ���
	    for(int col=0;col<3;col++)
	    {
	        if(grid[1][col]!=null)
	        {
	        	tmp.add(grid[1][col]);	       
	            return tmp;
	        }        
	    }	   
	    return null;
	}
    
    public void searchTarget(int skillTargetType,int rangeType)
    {
    	targetRoles = new Vector<BattleRole>();
    	collectEnemys();
    	Vector<BattleRole> tmp=null;
    	
    	if(skillTargetType == Const.TARGET_ENEMY)  //�Եз�����
		{
	        if(rangeType == Const.RANGE_FRONT_SINGLE || rangeType == Const.RANGE_BACK_SINGLE)  //����ǰ�ŵ���,���Ⱥ��ŵ���
        	{
	        	if(rangeType == Const.RANGE_FRONT_SINGLE)
	        	{
		            //��ǰ����ֻ��ѡǰ��Ŀ�꣬ûǰ�ſ�ѡ����Ŀ��
		            if(frontEnemy.size() > 0)
	            	{
		                tmp = frontEnemy;
	            	}
	        	}
	        	else if(rangeType == Const.RANGE_BACK_SINGLE)
	        	{
	        		if(backEnemy.size() > 0)
	            	{
		                tmp = backEnemy;
	            	}
	        	}
	            if(tmp==null)	            
	            {
	                tmp = allEnemy;
	            }		            
	            
	            if(tmp.size()>0)
            	{
	            	BattleRole target=null;
	            	
	            	byte[] searchCols = new byte[3];
	            	searchCols[0] = (byte)(2 - col); //������ǰ��
	            	searchCols[1] = (byte)(3- col); //��ǰ��
	            	searchCols[2] = (byte)(1- col); //��ǰ��	                

	                for(int i=0;i<searchCols.length;i++)
	                {
	                	for(int j=0;j<tmp.size();j++)
		            	{
		                    if(tmp.get(j).col == searchCols[i])
	                    	{
		                    	target = tmp.get(j);	                    	
		                    	break;
	                    	}
		            	}
	                }
	              
	            	if(target==null) 
	            	{
	            		int index = getRandomNumber(0,tmp.size()-1);  //�����һ��
	            		target = tmp.get(index);
	            	}
	            	if(actor.battleSkill!=null)
	            	{
	            		if(actor.battleSkill.battleSkillFixData.maxUseTimes>1)  //������
	            		{
	            			for(int i=0;i<actor.battleSkill.battleSkillFixData.maxUseTimes;i++)
	            			{
	            				targetRoles.add(target);
	            			}
	            		}
	            		else
	            		{
	            			targetRoles.add(target);	
	            		}
	            	}
	            	else
	            	{
	            		targetRoles.add(target);	
	            	}
	            }
        	}
	        else if(rangeType == Const.RANGE_FRONT_LINE) //���ȴ�ǰ��          
	        {
	            if(frontEnemy.size()>0)
            	{
	                targetRoles = frontEnemy;
            	}
	            else if(backEnemy.size()>0)
            	{
	                targetRoles = backEnemy; 
            	}
	        }
	        else if(rangeType == Const.RANGE_COL)  //������
	        {
	            targetRoles = findColTargets(1 - getTeamType());
	        }	        
	        else if(rangeType == Const.RANGE_N_HIGH)
        	{
	            tmp = allEnemy;
	            if(tmp.size() > 0)
	            {
	                final byte propIndex = (byte)actor.battleSkill.battleSkillFixData.rangeArgs[0];
	                int amount = actor.battleSkill.battleSkillFixData.rangeArgs[1];
	                if(amount > tmp.size())
	                {
	                    amount = tmp.size();
	                }
	                Collections.sort(tmp, new Comparator<BattleRole>() 
            		{
            			public int compare(BattleRole role1,BattleRole role2)
            			{
            				return role2.getFinalProp(propIndex) - role1.getFinalProp(propIndex);
            			}				
            		});	                
	                
	                Vector<BattleRole> finalTargets = new Vector<BattleRole>();
	                for(int i=0;i<amount;i++)
	                {
	                	finalTargets.add(tmp.get(i));	                    
	                }
	                targetRoles = finalTargets;
	            }
        	}
	        else if(rangeType == Const.RANGE_N_LOW)
	        {
	        	tmp = allEnemy;
	            if(tmp.size() > 0)
	            {
	                final byte propIndex = (byte)actor.battleSkill.battleSkillFixData.rangeArgs[0];
	                int amount = actor.battleSkill.battleSkillFixData.rangeArgs[1];
	                if(amount > tmp.size())
	                {
	                    amount = tmp.size();
	                }
	               
	                Collections.sort(tmp, new Comparator<BattleRole>() 
            		{
            			public int compare(BattleRole role1,BattleRole role2)
            			{
            				return role1.getFinalProp(propIndex) - role2.getFinalProp(propIndex) ;
            			}				
            		});	                
	                
	                Vector<BattleRole> finalTargets = new Vector<BattleRole>();
	                for(int i=0;i<amount;i++)
	                {
	                	finalTargets.add(tmp.get(i));	                    
	                }
	                targetRoles = finalTargets;
	            }
	        }
	        else if(rangeType == Const.RANGE_ALL)
	        {
	            if(allEnemy.size() > 0)
	            {
	                targetRoles = allEnemy;
	            }
	        }
	        else if(rangeType == Const.RANGE_SINGLE) //���ⵥ��
	        {
	            tmp = allEnemy;
	            if(tmp.size()>0)
	            {
	                int index = getRandomNumber(0,tmp.size()-1);	                
	                targetRoles.add(tmp.get(index));        
	            } 
	        }
	        else if(rangeType == Const.RANGE_BACK_LINE)  //���ȴ����
	        {
	            if(backEnemy.size() > 0)
            	{
	                targetRoles = backEnemy;  
            	}
	            else if(frontEnemy.size() > 0)
            	{
	                targetRoles = frontEnemy;  
            	}
	        }
	        else if(rangeType == Const.RANGE_SPLIT)  //ɢ��
	        {
	        	if(allEnemy.size()>0)
	        	{
	        		targetRoles = getRandomAmountTargets(allEnemy,actor.battleSkill.battleSkillFixData.maxUseTimes);
	        	}
	        }
	        else
	        {
	            //BattlePrint.print("targetType="+skillTargetType+",δ֪range����="+rangeType);
	        }   
		}
    	else if(skillTargetType == Const.TARGET_FRIEND)  //���ѷ�����
    	{
	        if(rangeType == Const.RANGE_COL)  //����
	        {
	            targetRoles = findColTargets(getTeamType());
	        }
	        else if(rangeType == Const.RANGE_SELF)   //�Լ�
	        {
	        	targetRoles.add(this);
	        }
	        else if(rangeType == Const.RANGE_N_HIGH)  
	        {
	            tmp = allFriend;
	            if(tmp.size() > 0)
	            {
	                final byte propIndex = (byte)actor.battleSkill.battleSkillFixData.rangeArgs[0];
	                int amount = actor.battleSkill.battleSkillFixData.rangeArgs[1];
	                if(amount > tmp.size())
	                {
	                    amount = tmp.size();
	                }
	                Collections.sort(tmp, new Comparator<BattleRole>() 
            		{
            			public int compare(BattleRole role1,BattleRole role2)
            			{
            				return role2.getFinalProp(propIndex) - role1.getFinalProp(propIndex);
            			}				
            		});	                
	                
	                Vector<BattleRole> finalTargets = new Vector<BattleRole>();
	                for(int i=0;i<amount;i++)
	                {
	                	finalTargets.add(tmp.get(i));	                    
	                }
	                targetRoles = finalTargets;
	            }
	        }
	        else if(rangeType == Const.RANGE_N_LOW)
	        {
	            tmp = allFriend;
	            if(tmp.size() > 0)
	            {
	                final byte propIndex = (byte)actor.battleSkill.battleSkillFixData.rangeArgs[0];
	                int amount = actor.battleSkill.battleSkillFixData.rangeArgs[1];
	                if(amount > tmp.size())
	                {
	                    amount = tmp.size();
	                }
	               
	                Collections.sort(tmp, new Comparator<BattleRole>() 
            		{
            			public int compare(BattleRole role1,BattleRole role2)
            			{
            				return role1.getFinalProp(propIndex) - role2.getFinalProp(propIndex) ;
            			}				
            		});	                
	                
	                Vector<BattleRole> finalTargets = new Vector<BattleRole>();
	                for(int i=0;i<amount;i++)
	                {
	                	finalTargets.add(tmp.get(i));	                    
	                }
	                targetRoles = finalTargets;
	            }
	        }
	        else if(rangeType == Const.RANGE_ALL)  //ȫ��
	        {
	            targetRoles = allFriend;
	        }
	        else if(rangeType == Const.RANGE_SINGLE)  //���ⵥ��
	        {
	            tmp = allFriend;
	            if(tmp.size()>0)
            	{
	                int index = getRandomNumber(0,tmp.size()-1);	                
	                targetRoles.add(tmp.get(index));        
            	} 
	        }
	        else if(rangeType== Const.RANGE_LINE)  //����
	        {
	            if(frontFriend.size() >0)
	            {
	                targetRoles = frontFriend;
	            }
	            else if(backFriend.size() >0)
            	{
	                targetRoles = backFriend;
            	}
	        }
	        else
	        {
	           // BattlePrint.print("targetType="+skillTargetType+",δ֪range����="+rangeType);
	        } 
    	} 
    }
    
    //��������N��Ŀ��
    public Vector<BattleRole> getRandomAmountTargets(Vector<BattleRole> allTarget,int amount)
    {
        if(allTarget==null)
    	{
            return null;
    	}
        else
        {
            if(allTarget.size() <= amount)
            {
                return allTarget;
            }
            else
            {
            	Vector<BattleRole> targets = new Vector<BattleRole>();
            	Vector<BattleRole> tmp = allTarget;
                for(int i=0;i<amount;i++)
                {
                    int num = Tools.getRandomNumber(0,tmp.size()-1);
                    targets.add(tmp.get(num));
                    tmp.remove(num);                    
                }
                return targets;
            }
        }
    }
	
    
    /**
     * ��ȡ�������
     * @param num
     * @return
     */
    public BattleSkill getAutoSkill(int num)
    {
    	for(int i=0;autoSkills!=null && i<autoSkills.size();i++)
		{
    		if(autoSkills.get(i).battleSkillFixData.num==num)
    		{
    			return autoSkills.get(i);
    		}
		}    
    	return null;
    }
    
    /**
     * ��ȡŭ����
     * @param num
     * @return
     */
    public BattleSkill getAngrySkill(int num)
    {
    	for(int i=0;angrySkills!=null && i<angrySkills.size();i++)
		{
    		if(angrySkills.get(i).battleSkillFixData.num==num)
    		{
    			return angrySkills.get(i);
    		}
		}    
    	return null;
    }

    /**
     * һ���ж����������֮ǰָ��
     */
    public void clearCommand()
    {
        currCommand = null;                
    }
   
	/**
	 * �Ƿ���
	 * @return
	 */
	public boolean isLive()
    {
		return getFinalProp(Const.PROP_HP)>0?true:false;
    }
	public int getPlayerId()
    {
		return spriteBox.playerId;
    }
	public int getPartnerId()
    {
		return spriteBox.partnerId;
    }
	
	public int getProp(int index)
	{
		return spriteBox.battle_prop[index];
	}
	public void changeHP(int changeValue)
	{
		if(changeValue!=0)
		{
			if(changeValue >0)
			{
		        //���Ƿ��в��ܱ���Ѫ��buff
		        if(inBuffStatus(Const.BUFF_TYPE_UNCUREABLE))
        		{
		            return;
        		}
			}
			int result = spriteBox.battle_prop[Const.PROP_HP] + changeValue;
			if(result<0)
			{
				result=0;
			}
			else
			if(result>spriteBox.battle_prop[Const.PROP_MAXHP])
			{
				result = spriteBox.battle_prop[Const.PROP_MAXHP];
			}
			spriteBox.battle_prop[Const.PROP_HP] = result;
			if(spriteBox.battle_prop[Const.PROP_HP]<=0)
			{
				removeAllBuff();				
			}
		}		
	}
	
	public byte changeAngry(int changeValue)
	{
		if(changeValue!=0)
		{
			//���Ƿ��в��ܼ�ŭ����buff
            if(changeValue>0 && inBuffStatus(Const.BUFF_TYPE_NOTADDANGRY))
            {
                return 0; 
            }
            
			int result = spriteBox.battle_prop[Const.PROP_ANGER] + changeValue;
			if(result<0)
			{
				result=0;
			}
			else
			if(result> BattleConfig.angryMaxValue)
			{
				result = BattleConfig.angryMaxValue;
			}
			spriteBox.battle_prop[Const.PROP_ANGER] = result;
			//todo  ��100��ӵ���ŭ���ܺ���
			if(spriteBox.battle_prop[Const.PROP_ANGER] >= BattleConfig.angryMaxValue && !waitUseAngry)
			{
				waitUseAngry=true;
				this.battle.insertActor(this);				
			}
		}	
		
		return (byte)spriteBox.battle_prop[Const.PROP_ANGER];
	}
	
	public void setAngry(int setValue)
	{
		spriteBox.battle_prop[Const.PROP_ANGER] = setValue;
	}
	
	
	public boolean calcDodge(BattleRole attackRole)
	{
	    //ʵ��������=����������-�з����У�/(���ȼ�*mod10+mod11*����������-�з����У�)
	    int hit = attackRole.getFinalProp(Const.PROP_HIT);
	    int dodge = getFinalProp(Const.PROP_DODGE);
	    if(dodge > hit)
	    {
	        double dodgeRate = (dodge - hit)/(getLevel() * BattleData.mod10 + BattleData.mod11 * (dodge - hit));
	        double rnd=getRandomFloatNumber(0, 1);
	        
	        if(rnd<dodgeRate)
	        {	            
	            return true;                      
	        }
	        else
	        {
	        	return false;
	        }
	    }
	    else
	    {
	    	return false;
	    }
	}
	
	public int calcCritical(BattleResult br,BattleRole attackRole,int baseHarm)
	{
	    //1.�����
	    //ʵ�ʸ���=�������-�з��ƻ���/(���ȼ�*mod13+mod14*�������-�з��ƻ���)
	    int bk = attackRole.getFinalProp(Const.PROP_BREAK);
	    int block = getFinalProp(Const.PROP_BLOCK);
	    if(block > bk)
	    {
	        double blockRate = (block - bk)/(getLevel() * BattleData.mod13 + BattleData.mod14 * (block - bk));
	        
	        double rnd= getRandomFloatNumber(0, 1);
	        
	        if(rnd<blockRate)
	        {
	        	br.isBlock=true;
	            //�񵲺��γɵ��˺�ֵ=�����˺�ֵ*mod16
	            return (int)Math.ceil(baseHarm * BattleData.mod16);
	        }
	    }
	    
	    //2.���㱩��    
	    //������=(������������-�з�����)/(���ȼ�*mod5+mod6*(������������-�з�����))	
	    int critical = attackRole.getFinalProp(Const.PROP_CRITICAL);
	    int toughness = getFinalProp(Const.PROP_TOUGHNESS);
	    if(critical > toughness)
	    {
	        double criticalRate = (critical - toughness)/(getLevel() * BattleData.mod5 + BattleData.mod6 * (critical - toughness));
	        
	        double rnd= getRandomFloatNumber(0, 1);	
	        if(rnd<criticalRate)
	        {
	        	br.isCriticalAtk = true;
	            return (int)Math.ceil(baseHarm * (BattleData.mod8 + attackRole.getFinalProp(Const.PROP_CRITICAL_MUL) / BattleData.mod9));
	        }
	        else
	        {
	            return baseHarm;
	        }
	    }
	
	    return baseHarm;
	}

	public int getSeries()
	{
		return spriteBox.battletype;
	}
	
	public int getSex()
	{
		return spriteBox.sex;
	}
	
	
	/**
	 * ��������˺����չ��ͼ��ܹ���
	 * @param attackRole
	 * @return
	 */
	private int calcBaseHarm(BattleRole attackRole)
	{
		int harm=0;	
		int defence = getFinalProp(Const.PROP_DEFENCE);
		
		if(attackRole.getSeries() == Const.SERIES_MAGIC || attackRole.getSeries() == Const.SERIES_HELP)
		{
            defence = getFinalProp(Const.PROP_MAGICDEF);
		}
		
		//�������ܸı����
		defence = attackRole.getDefChangeByBeSkill(defence);
		if(defence<0)
        {
            defence=0;
        }
		
        harm = (int)((attackRole.getFinalProp(Const.PROP_ATTACK) * BattleConfig.atkFactor - defence * BattleConfig.defFactor) * BattleConfig.harmFactor);

        if(harm<=0)
        {
            harm = 1;
        }
        return harm;
	}
	//���㼼���˺�
	private int calcBaseSkillHarm(BattleResult br,BattleRole attackRole,BattleSkill skill,int harm) 
	{
	    //�Ӽ����˺�
	    //��ʼ�˺��ٷֱ�,�����˺��ٷֱ�����,��ʼ�˺��̶�ֵ,�����˺��̶�ֵ����
	    //�����˺����㹫ʽ=������/�����������˺�*[��ʼ�˺��ٷֱ�+�����ܵȼ�-1��*�����˺��ٷֱ�����]+��ʼ�˺��̶�ֵ+�����ܵȼ�-1��*�����˺��̶�ֵ����
	    short[] harmArgs = skill.battleSkillFixData.harmArgs;
	    //int attackerHPChange=0;
	    int skillLevel = skill.level;
	    BattleSkillFixData skillData = skill.battleSkillFixData;

	    if(harmArgs !=null)
	    {
	        if(harmArgs[0] > 0 || harmArgs[1] > 0 || harmArgs[2] > 0 || harmArgs[3] > 0)
	        {
	            harm = harm * (harmArgs[0]+(skillLevel-1)*harmArgs[1])/100 + harmArgs[2] + (skillLevel-1)*harmArgs[3];
	            if(harm<=0)
	            {
	                harm = 1;
	            }
	        }
	        else
	        {
	            harm = 0;
	        }
	    } 

	    if(skillData.otherArgs!=null)
	    {
	        if(skillData.otherArgs[0]==1)   //��Ѫ��������1,�˺��ĳ�ʼ�ٷֱȣ���������
	        {
	            int percent = skillData.otherArgs[1];
	            int levelUP = skillData.otherArgs[2];
	          //����Ƿ��ڲ��ܼ�Ѫ��buff��
	            if(!attackRole.inBuffStatus(Const.BUFF_TYPE_UNCUREABLE))
	            {
	            	br.fromRoleHpChange += (int)Math.ceil(harm * (percent + levelUP * (skillLevel-1)) / 100);	            	
	            }	            
	        }
	        else if(skillData.otherArgs[0]==2)   //ŭ���仯,������2,��0,����|1,���ӣ�����ŭ��ֵ����������       
	        {
	        	
	        }
	    }
	    
	    if(harm<0)
	    {
	        harm=0;
	    }

	    return harm;
	}
	//����buff�ͱ��������˺�
	private int calcOtherHarm(BattleResult br,BattleRole attackRole,int harm)
	{
	    //buff�ı��˺�	   
	    harm = getChangeHarmByBuff(attackRole,harm);	   

	    //�������ܸı��˺�  harmType:1,ָ�����˺�|2,DEBUFF�˺�|3,�����˺�|4,�����˺�	    
	    int[] harmTypes = new int[]{1};
        if(isPhy())
        {
        	harmTypes = Tools.addToIntArr(harmTypes, Const.HARM_TYPE_PHY);            
        }
        else
        {
        	harmTypes = Tools.addToIntArr(harmTypes, Const.HARM_TYPE_MAGIC);
        }        

        harm = getChangeHarmByBeSkill(attackRole ,harm,harmTypes);
	   
      //����������
        int bounceHarm = getBounceHarmByBeSkill(harm);
        if(bounceHarm > 0)
        {
        	int attackRoleHarm =0;
            if(bounceHarm >= attackRole.getFinalProp(Const.PROP_HP)) //����������һ��Ѫ
            {
            	attackRoleHarm = -(attackRole.getFinalProp(Const.PROP_HP)-1);
            	//attackRole.changeHP(attackRoleHarm);                
            }
            else
            {
            	attackRoleHarm = -bounceHarm;
            	//attackRole.changeHP(attackRoleHarm);                
            }             
            br.fromRoleHpChange += attackRoleHarm;            
        } 
        
	    //���㱩���͸�
	    harm = calcCritical(br,attackRole,harm);

	    if(harm<0)
	    {
	        harm=0;
	    }

	    return harm;
	}
	/**
	 * �����չ��˺�
	 * @param attackRole ʩ����
	 */
	public BattleResult calcHarm(BattleRole attackRole)
	{		
		BattleResult br = new BattleResult();
		boolean dodge = calcDodge(attackRole);
		if(dodge)
		{				
			br.isDodge=true;
			br.toRoleHpChange = 0;
		    return br;
		}
		
		int harm=0;		
		
		int hpStartPercent = getFinalProp(Const.PROP_HP) / getFinalProp(Const.PROP_MAXHP) * 100;    
		
		//��1����������˺�
		harm = calcBaseHarm(attackRole);
		
		//��2������buff,�������������񵲵ȶ��˺��ĸı�		
		harm = calcOtherHarm(br,attackRole,harm);	
        
        harm = (int)Math.ceil(harm);
        
        //�鹥�����Ƿ������ŭ������
        boolean notAngry = attackRole.haveBeSkill(14);

        if(isLive() && !notAngry)  //�����߼�ŭ
        {   
	    	br.angryValueChanged = true;
	    	br.angryValue = changeAngry((int)(BattleConfig.beHitAddAngry +  harm / this.getFinalProp(Const.PROP_MAXHP) * BattleData.mod21));		    
        }
        
        changeHP(-harm);

        if(isLive())
        {
            int hpEndPercent = getFinalProp(Const.PROP_HP) / getFinalProp(Const.PROP_MAXHP) * 100;
            addBuffByHarm(attackRole,hpStartPercent,hpEndPercent);
        }
		
		br.toRoleHpChange = -harm;
		return br;
	}
	

	/**
	 * 
	 * @param attackRole
	 */
	public BattleResult calcSkillHarm(BattleRole attackRole,boolean isAngry)
	{
		BattleResult br = new BattleResult();
		boolean dodge = calcDodge(attackRole);
		if(dodge)
		{				
			br.isDodge=true;
			br.toRoleHpChange = 0;
		    return br;
		}
		
		int hpStartPercent = getFinalProp(Const.PROP_HP) / getFinalProp(Const.PROP_MAXHP) * 100;
				
	    int harm=0;
	    
	    //��1����������˺�
	    harm = calcBaseHarm(attackRole);
	    
	    //��2�����㼼�ܶ��˺��ĸı�
	    harm = calcBaseSkillHarm(br,attackRole,attackRole.actor.battleSkill,harm);	   
	    
	    //��3������buff,�������������񵲵ȶ��˺��ĸı�
        harm = calcOtherHarm(br,attackRole,harm);	    
        
	    harm = (int)Math.ceil(harm);	
	    
	    br.toRoleHpChange = -harm;
	    
	    //�鹥�����Ƿ������ŭ������
        boolean notAngry = attackRole.haveBeSkill(14);

        if(isLive() && !notAngry)
        {   
	    	br.angryValueChanged = true;
	    	if(isAngry)
	    	{
	    		br.angryValue = changeAngry((int)(BattleConfig.beAngrySkillHitAddAngry + harm / this.getFinalProp(Const.PROP_MAXHP) * BattleData.mod21));
	    	}
	    	else
	    	{
	    		br.angryValue = changeAngry((int)(BattleConfig.beAutoSkillHitAddAngry+ harm / this.getFinalProp(Const.PROP_MAXHP) * BattleData.mod21));	    		
	    	}
            //�ӿ����е�buff
		    hitBuff(br,attackRole);
        }

        changeHP(-harm);
        
        if(isLive())
        {
	        int hpEndPercent = getFinalProp(Const.PROP_HP) / getFinalProp(Const.PROP_MAXHP) * 100;
	        addBuffByHarm(attackRole,hpStartPercent,hpEndPercent);
        }
        
	    return br; 
	}

	public Buff bindBuff(BattleRole fromRole,int level,int buffNum)
	{
	    BuffFixData buffData = BuffLib.getBuffFixDataByNum(buffNum);
	    if(buffData==null)
	    {
	    	BattlePrint.print("δ�ҵ�buff="+buffNum+"������");
	    }
	    //���쳣״̬buff���Ƿ�������buff
	    if(buffData.goodbad == 2) //����buff
	    {
	        if(inBuffStatus(Const.BUFF_TYPE_ABNORMAL_SHELL))
	        {
	            Buff shellBuff = getBuffByType(Const.BUFF_TYPE_ABNORMAL_SHELL);
	            if(shellBuff!=null && shellBuff.shellAbnormal >0)
	            {	            	
	                shellBuff.shellAbnormal = (byte)(shellBuff.shellAbnormal -1);
	                BattlePrint.print(this.getIdName()+"���߶���ʣ�����="+shellBuff.shellAbnormal+",����һ��debuff");
	                if(shellBuff.shellAbnormal <=0)
	                {
	                	//��¼��json���ͻ���
	                	fromRole.removeBuffNumArr.add(this.getId());
	                	fromRole.removeBuffNumArr.add(shellBuff.buffFixData.num);
	        	    	
	                    removeBuff(shellBuff);
	                }
	                return null;
	            }
	        }
	    }
	
	    if(buffData !=null)
	    {
	        if(buffData.buffTypes!=null)
	        {
	            Buff buffObj = BuffLib.createBuff(buffNum, level);
	
	            for(int i=0;i<buffData.buffTypes.length;i++)
	            {
	                int num = buffData.buffTypes[i][0];	                
	                    
	                if(num== Const.BUFF_TYPE_SHELL) //���ӵֵ�ָ���˺��Ļ���
	                {
	                    buffObj.shellHP = buffData.args[i][1] + (level-1) * buffData.args[i][2];	                   
	                    
	                    //BattlePrint.print("����Ѫ����ֵ"+buffObj.shellHP);
	                }
	                else if(num== Const.BUFF_TYPE_ABNORMAL_SHELL) //��������ָ�������쳣״̬�Ļ���
	                {
	                    buffObj.shellAbnormal = (byte)(buffData.args[i][1] + (level-1) * buffData.args[i][2]);
	                }
	                else if(num== Const.BUFF_TYPE_CHANGEBODY)  //����
	                {
	                    if(!isChangeBody)
	                    {
	                        isChangeBody=true;	                        
	                        BattlePrint.print(this.getIdName()+"������");
	                    }   
	                    buffObj.isChangeBuff=true;
	                }
	            }	           
	           
	            if(fromRole !=null)
	            {
	                buffObj.fromRoleAtk = (short)fromRole.getFinalProp(Const.PROP_ATTACK);
	            }
	            addBuff(fromRole,buffObj);
	            //BattlePrint.print(getName()+"("+getId()+")����buff:"+buffData.name+"turns="+buffObj.turns);
	            return buffObj;
	        }
	    }
	    return null;
	}
	
	//��ñ������ܸı�Է��ķ����� 11.���ӶԷ���������������11,��������ֵ,������������ֵ
	public int getDefChangeByBeSkill(int defence)
	{
	    if(isLive())
	    {
	        for(int i=0;i<beSkills.size();i++)
	        {
	        	BattleSkill skill = beSkills.get(i);	            
	            int beSkillLevel = skill.level;
	            BattleSkillFixData beSkillData = skill.battleSkillFixData;
	            if(beSkillData.owner ==2)
	            {
	                for(int j=0;j<beSkillData.args.length;j++)
	                {
	                    short[] arg = beSkillData.args[j];
	                    short t = arg[0];

	                    if(t == Const.BESKILL_TYPE11) //11.���ӶԷ���������������11,��������ֵ,������������ֵ
	                    {	                     
	                        return defence * (100-(arg[1] + arg[2] * (beSkillLevel - 1)))/100;
	                    }	                    
	                }  
	            }       
	        }
	    }	    
	    return defence;
	}
	
	//����ֵ����ָ���ٷֱ�ʱ���BUFF��������8,��������,BUFF���
	public void addBuffByHarm(BattleRole attackRole,int hpStartPercent,int hpEndPercent)
	{
	    //�����ߵı������ܶ��˺��ĸı�
	    for(int i=0;i<beSkills.size();i++)
	    {
	    	BattleSkill skill = beSkills.get(i);	        
	        int beSkillLevel = skill.level;
	        BattleSkillFixData beSkillData = skill.battleSkillFixData;
	        if(beSkillData.owner ==1)  //���˳�����ʱ�����ı�������
	        {
	            for(int j=0;j<beSkillData.args.length;j++)
	            {
	                short[] arg = beSkillData.args[j];
	                short t = arg[0];

	                if(t == Const.BESKILL_TYPE8)  //8.����ֵ����ָ���ٷֱ�ʱ���BUFF��������8,��������,BUFF���
	                {
	                    if(hpStartPercent>= arg[1] && hpEndPercent <arg[1])
	                    {	
	                    	//BattlePrint.print(this.getName()+"��"+attackRole.getName()+"���buff"+arg[2]);
	                    	bindBuff(attackRole, beSkillLevel, arg[2]);	                    	                   
	                    }                    
	                }
	            }  
	        }       
	    }
	}
	
	public boolean isPhy()
	{
	    if(getSeries() == Const.SERIES_NEAR || getSeries() == Const.SERIES_DEF || getSeries() == Const.SERIES_KILL)
	    {
	        return true;
	    }
	    else
	    {
	        return false;
	    }	    
	}
	
	
	/**
	 * ���ݸ��ʼ����е�buff
	 * @param br
	 * @param attackRole
	 */
	public void hitBuff(BattleResult br,BattleRole attackRole)
	{			
		short[] buffsNum = attackRole.actor.battleSkill.battleSkillFixData.buffs;
		int skillLevel = attackRole.actor.battleSkill.level;
		short[][] buffsRate = attackRole.actor.battleSkill.battleSkillFixData.buffsRate;
		
		
	    
	    
		for(int i=0;buffsNum!=null && i<buffsNum.length;i++)
		{			
			int rate = buffsRate[i][0] + (skillLevel-1)*buffsRate[i][1];
			int rnd = getRandomNumber(0, 100);
			if(rnd<=rate)
			{
				this.bindBuff(attackRole, skillLevel, buffsNum[i]);				
			}			
		}		
	}
	public Buff getBuffByType(int buffType)
	{
	    if(buffArr==null)
	    {
	        return null;
	    }
	
	    for(int i=0;i<buffArr.size();i++)
	    {
	        Buff buff = buffArr.get(i);
	
	        for(int j=0;j<buff.buffFixData.buffTypes.length;j++)
	        {
	            if(buff.buffFixData.buffTypes[j][0] == buffType)
	            {
	                return buff;
	            }
	        }
	    }
	    return null;
	}
	
	public BattleResult calcRecover(BattleRole fromRole)
	{
		BattleResult br = new BattleResult();
        BattleSkillFixData skillData = fromRole.actor.battleSkill.battleSkillFixData;
        int skillLevel = fromRole.actor.battleSkill.level;

        int recover = fromRole.getFinalProp(Const.PROP_ATTACK);

        //�Ӽ����˺�
        //��ʼ�˺��ٷֱ�,�����˺��ٷֱ�����,��ʼ�˺��̶�ֵ,�����˺��̶�ֵ����
        //�����˺����㹫ʽ=������/�����������˺�*[��ʼ�˺��ٷֱ�+�����ܵȼ�-1��*�����˺��ٷֱ�����]+��ʼ�˺��̶�ֵ+�����ܵȼ�-1��*�����˺��̶�ֵ����
        short[] harmArgs = skillData.harmArgs;

        if(harmArgs !=null)
        {
            if(harmArgs[0] > 0 || harmArgs[1] > 0 || harmArgs[2] > 0 || harmArgs[3] > 0)
        	{
                recover = (int)((recover * (harmArgs[0]+(skillLevel-1)*harmArgs[1])/100 + harmArgs[2] + (skillLevel-1)*harmArgs[3]) * BattleData.mod20);
                if(recover<=0)
                {
                    recover = 1;
                }        
        	}
        } 

        recover = (int)Math.ceil(recover);

        changeHP(recover);
        br.toRoleHpChange = recover;
        
        return br;
	}    
	public String getIdName()
	{
		return this.getId() +"."+spriteBox.name;
	}
	
	public String getName()
	{
		return spriteBox.name;
	}
	
	public int getRandomNumber(int start, int end) 
    {
    	if(start==end || end - start + 1==0)    	
    	{
    		return start;
    	}
	    int rnd = (Math.abs(ran.nextInt()) % (end - start + 1)) + start;
	    return rnd;
	}
	public double getRandomFloatNumber(float start, float end) 
    {
    	if(start==end)    	
    	{
    		return start;
    	}
    	double r = start + ran.nextDouble() * (end - start);	    
	    return r;
	}
	
	private static int[] getIdsByTargetRoles(Vector<BattleRole> targetVC)
	{
		int[] ids = null;
		for(int i=0;targetVC!=null && i<targetVC.size();i++)
		{
			BattleRole role = targetVC.get(i);
			ids = Tools.addToIntArr(ids, role.getId());
		}
		return ids;
	}
	

	//��÷����˺�ֵ
	public int getBounceHarmByBeSkill(int harm)
	{
	    if(getFinalProp(Const.PROP_HP) > 0)
	    {
	        for(int i=0;i<beSkills.size();i++)
	        {
	        	BattleSkill skill = beSkills.get(i);	            
	            int beSkillLevel = skill.level;
	            BattleSkillFixData beSkillData = skill.battleSkillFixData;
	            if(beSkillData.owner ==1)
	            {
	                for(int j=0;j<beSkillData.args.length;j++)
	                {
	                    short[] arg = beSkillData.args[j];
	                    short t = arg[0];
	                    if(t == Const.BESKILL_TYPE17)  //17.�����ܵ����˺����������������˺�������ɶԷ������������˺����ᳬ�������������ޣ�������17,�����˺��ٷֱ�,���������ٷֱȣ���λ0.1��
	                    {
	                        return (int)(harm * (arg[1] + arg[2]* 0.1 * (beSkillLevel - 1))/100);
	                    }	                    
	                }  
	            }       
	        }
	    }
	    return 0;
	}
	
	private static JSONArray getJsonArrByIntArr(int[] arr)
	{
		try {
			JSONArray jsonArr = new JSONArray(arr);
			return jsonArr;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	//���Ƿ��б�������
	public boolean haveBeSkill(int typenum)
	{
	    if(getFinalProp(Const.PROP_HP) > 0)
	    {
	        for(int i=0;i<beSkills.size();i++)
	        {
	        	BattleSkill skill = beSkills.get(i);	            
	            int beSkillLevel = skill.level;
	            BattleSkillFixData beSkillData = skill.battleSkillFixData;
	           
	            for(int j=0;j<beSkillData.args.length;j++)
	            {
	                short[] arg = beSkillData.args[j];
	                short t = arg[0];
	                if(t == typenum)
	                {
	                    return true;
	                }                    
	            }    
	        }
	    }

	    return false;
	}
	
	public boolean inBuffStatus(int buffType)
	{
	    if(buffArr==null)
	    {
	        return false;
	    }
	
	    for(int i=0;i<buffArr.size();i++)
	    {
	        Buff buff = buffArr.get(i);
	
	        for(int j=0;j<buff.buffFixData.buffTypes.length;j++)
	        {
	            if(buff.buffFixData.buffTypes[j][0] == buffType)
	            {
	                return true;
	            }
	        }
	    }
	    return false;
	}
	
	/**
	 *ִ��ս���߼� 
	 */
	public JSONWrap doBattle()
	{	 	   
        if(isLive())
        {            
            if(actor!=null)
            {               	
            	if(actor.cmdType == Command.CMD_NONE)
            	{
            		//todo��AI��ָ��
            		
                    //A���ò���ָ��
                    if(getTeamType() == Const.teamA)
                    {
                        actor.cmdType = Command.CMD_ATTACK;
                    }
                    else
                    {
                    	actor.cmdType = Command.CMD_ATTACK;
                    }
            	}

            
                if(actor.cmdType == Command.CMD_WAIT || actor.cmdType == Command.CMD_NONE)
                {                   
                    return null;
                }
              
                
                //ѣ������
                if(inBuffStatus(Const.BUFF_TYPE_DIZZY))
                {
                	return null;
                } 
                //��������
                if(isChangeBody)
                {
                	return null;
                }

                //������޼���
                if(actor.cmdType == Command.CMD_AUTOSKILL)
                {
                    if(autoSkills.size()==0)   
                    {
                        actor.cmdType = Command.CMD_ATTACK;
                	}
                }

                if(actor.cmdType == Command.CMD_ANGRYSKILL)
                {
                    if(angrySkills.size() == 0)
                    {
                       actor.cmdType = Command.CMD_ATTACK;
                    }
                }   
                
                if(inBuffStatus(Const.BUFF_TYPE_SILENCE))  //��Ĭ
                {
                	actor.cmdType = Command.CMD_ATTACK;
                }

                if(actor.cmdType == Command.CMD_ATTACK)  //�չ�
                {
                    //Ѱ��Ŀ��       
                    searchTarget(Const.TARGET_ENEMY,Const.RANGE_FRONT_SINGLE);
                    
                    if(targetRoles!=null && targetRoles.size()>0)
                    {                    	
                    	JSONWrap jsonWrap = new JSONWrap();
                        
                        jsonWrap.put(JSONWrap.KEY.ID, getId());
                        jsonWrap.put(JSONWrap.KEY.CMD, actor.cmdType);
                        
                    	//BattlePrint.print(this.getName()+"ʹ���չ�");
                    	jsonWrap.put(JSONWrap.KEY.TARGET_IDS, getJsonArrByIntArr(getIdsByTargetRoles(targetRoles)));
                    	//todo �����չ��˺�
                    	int[] hpChanges=null;
                    	int[] dodgeIds=null;
                    	int[] criticalIds = null;
                    	int[] blockIds = null;
                    	int[] angryChangeData=null;
                    	int totalHarm=0;
                    	int killAmount=0;
                    	int fromRoleHpChange=0;
                    	
                    	for(int i=0;i<targetRoles.size();i++)
                    	{
                    		BattleResult br = targetRoles.get(i).calcHarm(this);
                    		totalHarm += br.toRoleHpChange;
                    		if(br.toRoleHpChange!=0)BattlePrint.print(targetRoles.get(i).getIdName()+"��"+this.getIdName()+"�չ����"+br.toRoleHpChange+"Ѫ,ʣ��Ѫ="+targetRoles.get(i).getFinalProp(Const.PROP_HP));
                    		if(!targetRoles.get(i).isLive())  //����
                    		{
                    			killAmount++;	
                    		}
                    		
                    		hpChanges = Tools.addToIntArr(hpChanges, br.toRoleHpChange);
                    		fromRoleHpChange += br.fromRoleHpChange;
                    		
                    		if(br.fromRoleHpChange!=0)BattlePrint.print(this.getIdName()+"Ѫ�ı�"+br.fromRoleHpChange+",ʣ��Ѫ="+this.getFinalProp(Const.PROP_HP));
                    		
                    		if(br.isBlock)
                    		{
                    			//BattlePrint.print(targetRoles.get(i).getName()+"�񵲳ɹ�");
                    			blockIds = Tools.addToIntArr(blockIds, targetRoles.get(i).getId());
                    		}
                    		if(br.isCriticalAtk)
                    		{
                    			//BattlePrint.print(targetRoles.get(i).getName()+"�����ɹ�");
                    			criticalIds = Tools.addToIntArr(criticalIds, targetRoles.get(i).getId());
                    		}
                    		if(br.isDodge)
                    		{
                    			//BattlePrint.print(targetRoles.get(i).getName()+"���ܳɹ�");
                    			dodgeIds = Tools.addToIntArr(dodgeIds, targetRoles.get(i).getId());
                    		}
                    		if(br.angryValueChanged)  //�����߼�ŭ
                    		{
                    			angryChangeData = Tools.addToIntArr(angryChangeData, targetRoles.get(i).getId());
                    			angryChangeData = Tools.addToIntArr(angryChangeData, br.angryValue);
                    		}
                    	}
                    	if(totalHarm != 0) //�����߼�ŭ
                		{
                    		int angry = this.changeAngry(BattleConfig.hitAddAngry + killAmount * 20);       
                    		angryChangeData = Tools.addToIntArr(angryChangeData, this.getId());
                			angryChangeData = Tools.addToIntArr(angryChangeData, angry);
                		}
                            
                    	jsonWrap.put(JSONWrap.KEY.TARGET_HP_CHANGE, hpChanges);
                    	
                    	
                    	if(fromRoleHpChange!=0)
	                	{
                    		BattlePrint.print(this.getIdName()+"������"+fromRoleHpChange);
                    		changeHP(fromRoleHpChange);
	                		jsonWrap.put(JSONWrap.KEY.SOURCE_HP_CHANGE,fromRoleHpChange);
	                	}
                    	if(blockIds!=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.BLOCK, blockIds);
                    	}
                    	if(criticalIds!=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.CRITICAL, criticalIds);
                    	}
                    	if(dodgeIds!=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.DODGE, dodgeIds);
                    	}
                    	if(angryChangeData !=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.ANGRY, angryChangeData);
                    	}                      	
                    	if(addBuffNumArr.length()>0)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.ADD_BUFF, addBuffNumArr);
                    	}
                    	if(removeBuffNumArr.length()>0)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.REMOVE_BUFF, removeBuffNumArr);
                    	}
                    	if(blueBloodArr.length()>0)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.BLUE_BLOOD, blueBloodArr);
                    	}                    	
                    	return jsonWrap;
                    }
                }             
                else if(actor.cmdType == Command.CMD_AUTOSKILL || actor.cmdType == Command.CMD_ANGRYSKILL)  //������ܻ�ŭ����
                {     
                	int rangeType = actor.battleSkill.battleSkillFixData.range;
                    searchTarget(actor.battleSkill.battleSkillFixData.targetType,rangeType );  
                    
                    if(targetRoles!=null && targetRoles.size()>0)
	                {	                
	                	JSONWrap jsonWrap = new JSONWrap();
	                    
	                    jsonWrap.put(JSONWrap.KEY.ID, getId());
	                    jsonWrap.put(JSONWrap.KEY.CMD, actor.cmdType);
	                    
	                	int[] hpChanges=null;
	                	int[] angryChangeData=null;
	                	int[] dodgeIds=null;
                    	int[] criticalIds = null;
                    	int[] blockIds = null;
                    	//JSONArray addBuffs = null;
                    	int fromRoleHpChange=0;
                    	int killAmount=0;
                    	
	                	if(actor.cmdType == Command.CMD_ANGRYSKILL)
	                	{
	                		BattlePrint.print(this.getName()+"ʹ��ŭ����"+actor.battleSkill.battleSkillFixData.num+"."+actor.battleSkill.battleSkillFixData.name);
	                		//ŭ����0
	                		waitUseAngry=false;
	                		setAngry(0);
	                		angryChangeData = Tools.addToIntArr(angryChangeData, this.getId());
                			angryChangeData = Tools.addToIntArr(angryChangeData, 0);
	                	}
	                	else
	                	{
	                		BattlePrint.print(this.getName()+"ʹ���������"+actor.battleSkill.battleSkillFixData.num+"."+actor.battleSkill.battleSkillFixData.name);
	                	}
	                	jsonWrap.put(JSONWrap.KEY.SKILL_NUM,actor.battleSkill.battleSkillFixData.num);
	                	
	                	
	                	BattleResult br =null;
	                	if(rangeType==Const.RANGE_SPLIT)  //�������⴦��
	                    {	                		
	                		int[] targetIds = null;
	                		int harmIndex=0; //Ŀ��ָ��	                		
	                    	int hitTimes = actor.battleSkill.battleSkillFixData.maxUseTimes;  //�����������
	                    	for(int i=0;i<hitTimes;i++)
	                    	{
	                    		int fromIndex = harmIndex;  //�����ҵ����
	                    		while(true)  //��һ�����Ŀ�깥��
	                    		{
	                    			if(targetRoles.get(harmIndex).isLive())
		                    		{
		                    			br = targetRoles.get(harmIndex).calcSkillHarm(this,actor.cmdType == Command.CMD_ANGRYSKILL);
		                    			targetIds = Tools.addToIntArr(targetIds, targetRoles.get(harmIndex).getId());
		                    			if(!targetRoles.get(harmIndex).isLive())
			                			{
			                				killAmount++;
			                			}
		                    			BattlePrint.print(this.getName()+"��"+targetRoles.get(harmIndex).getName()+"��"+(i+1)+"����������˺�"+br.toRoleHpChange+",ʣ��Ѫ="+targetRoles.get(harmIndex).getFinalProp(Const.PROP_HP));
		                    			hpChanges = Tools.addToIntArr(hpChanges, br.toRoleHpChange);
				                		fromRoleHpChange += br.fromRoleHpChange;
				                		if(br.isBlock)
			                    		{
			                    			//BattlePrint.print(targetRoles.get(i).getName()+"�񵲳ɹ�");
			                    			blockIds = Tools.addToIntArr(blockIds, targetRoles.get(harmIndex).getId());
			                    		}
			                    		if(br.isCriticalAtk)
			                    		{
			                    			//BattlePrint.print(targetRoles.get(i).getName()+"�����ɹ�");
			                    			criticalIds = Tools.addToIntArr(criticalIds, targetRoles.get(harmIndex).getId());
			                    		}
			                    		if(br.isDodge)
			                    		{
			                    			//BattlePrint.print(targetRoles.get(i).getName()+"���ܳɹ�");
			                    			dodgeIds = Tools.addToIntArr(dodgeIds, targetRoles.get(harmIndex).getId());
			                    		}
			                    		if(br.angryValueChanged) //�����߼�ŭ
			                    		{
			                    			angryChangeData = Tools.addToIntArr(angryChangeData, targetRoles.get(harmIndex).getId());
			                    			angryChangeData = Tools.addToIntArr(angryChangeData, br.angryValue);
			                    		}
		                    			
		                    			harmIndex = harmIndex +1;
		                                if(harmIndex >= targetRoles.size())
		                                {
		                                    harmIndex =0;
		                                }
		                                break;
		                    		}
		                    		else
		                    		{
			                    		harmIndex = harmIndex +1;  //����һ��
			                            if(harmIndex >= targetRoles.size())
			                            {
			                                harmIndex =0;
			                            }
			                            if(harmIndex == fromIndex)  //����һ�ֲ������
			                            {
			                                break;
			                            }
		                    		}	
	                    		}	                    		
	                    	}
	                    	jsonWrap.put(JSONWrap.KEY.TARGET_IDS, targetIds);
	                    }
	                    else  //��ͨ���ܴ��
	                    {
	                    	jsonWrap.put(JSONWrap.KEY.TARGET_IDS, getIdsByTargetRoles(targetRoles));
	                    	for(int i=0;i<targetRoles.size();i++)
	                    	{	                		
		                		if(actor.battleSkill.battleSkillFixData.type == Const.SKILLTYPE_HARM)
		                		{
		                			br = targetRoles.get(i).calcSkillHarm(this,actor.cmdType == Command.CMD_ANGRYSKILL);
		                			if(actor.cmdType == Command.CMD_ANGRYSKILL)
		                			{
		                				if(br.toRoleHpChange!=0)BattlePrint.print(targetRoles.get(i).getIdName()+"��"+this.getIdName()+"ŭ���ܴ��"+br.toRoleHpChange+"Ѫ,ʣ��Ѫ="+targetRoles.get(i).getFinalProp(Const.PROP_HP));	
		                			}
		                			else
		                			{
		                				if(br.toRoleHpChange!=0)BattlePrint.print(targetRoles.get(i).getIdName()+"��"+this.getIdName()+"������ܴ��"+br.toRoleHpChange+"Ѫ,ʣ��Ѫ="+targetRoles.get(i).getFinalProp(Const.PROP_HP));
		                			}
		                    		
		                			if(!targetRoles.get(i).isLive())
		                			{
		                				killAmount++;
		                			}
		                		}
		                		else if(actor.battleSkill.battleSkillFixData.type == Const.SKILLTYPE_RECOVER)
		                		{
		                			br = targetRoles.get(i).calcRecover(this);
		                		}
		                		else if(actor.battleSkill.battleSkillFixData.type == Const.SKILLTYPE_BUFF)
		                		{
		                			br = new BattleResult();
		                			targetRoles.get(i).hitBuff(br,this);
		                		}
		                		hpChanges = Tools.addToIntArr(hpChanges, br.toRoleHpChange);
		                		fromRoleHpChange += br.fromRoleHpChange;
		                		
		                		if(br.isBlock)
	                    		{
	                    			//BattlePrint.print(targetRoles.get(i).getName()+"�񵲳ɹ�");
	                    			blockIds = Tools.addToIntArr(blockIds, targetRoles.get(i).getId());
	                    		}
	                    		if(br.isCriticalAtk)
	                    		{
	                    			//BattlePrint.print(targetRoles.get(i).getName()+"�����ɹ�");
	                    			criticalIds = Tools.addToIntArr(criticalIds, targetRoles.get(i).getId());
	                    		}
	                    		if(br.isDodge)
	                    		{
	                    			//BattlePrint.print(targetRoles.get(i).getName()+"���ܳɹ�");
	                    			dodgeIds = Tools.addToIntArr(dodgeIds, targetRoles.get(i).getId());
	                    		}
	                    		if(br.angryValueChanged) //�����߼�ŭ
	                    		{
	                    			angryChangeData = Tools.addToIntArr(angryChangeData, targetRoles.get(i).getId());
	                    			angryChangeData = Tools.addToIntArr(angryChangeData, br.angryValue);
	                    		}
	                    		/*short[] buffIds = br.getBuffIds();
	                    		if(buffIds !=null)
	                    		{
	                    			addBuffs = new JSONArray();
	                    			addBuffs.add(targetRoles.get(i).getId());
	                    			addBuffs.add(buffIds);
	                    		}*/
	                    	}
	                    }
	                	
	                	//������buff
	                    if(actor.battleSkill.battleSkillFixData.selfBuff > 0)
	                    {
	                        bindBuff(this,actor.battleSkill.level,actor.battleSkill.battleSkillFixData.selfBuff); 
	                    }
	                	
	                	if(actor.cmdType != Command.CMD_ANGRYSKILL)  //�����߼�ŭ
	                	{
	                		//if(totalHarm != 0)
	                		{
	                    		int angry = this.changeAngry(BattleConfig.autoSkillAddAngry + killAmount * 20);       
	                    		angryChangeData = Tools.addToIntArr(angryChangeData, this.getId());
	                			angryChangeData = Tools.addToIntArr(angryChangeData, angry);
	                		}
	                	}	                	
	                	else  //ŭ��������Ҳ��ŭ
	                	{
	                		int angry = this.changeAngry(killAmount * 20);       
                    		angryChangeData = Tools.addToIntArr(angryChangeData, this.getId());
                			angryChangeData = Tools.addToIntArr(angryChangeData, angry);
	                	}
	                	
	                	jsonWrap.put(JSONWrap.KEY.TARGET_HP_CHANGE, hpChanges);
	                	if(fromRoleHpChange!=0)
	                	{
	                		BattlePrint.print(this.getIdName()+"������"+fromRoleHpChange);
	                		changeHP(fromRoleHpChange);
	                		jsonWrap.put(JSONWrap.KEY.SOURCE_HP_CHANGE,fromRoleHpChange);
	                	}
	                	if(blockIds!=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.BLOCK, blockIds);
                    	}
                    	if(criticalIds!=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.CRITICAL, criticalIds);
                    	}
                    	if(dodgeIds!=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.DODGE, dodgeIds);
                    	}
                    	if(angryChangeData !=null)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.ANGRY, angryChangeData);
                    	}
                    	
                    	if(addBuffNumArr.length()>0)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.ADD_BUFF, addBuffNumArr);
                    	}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
                    	if(removeBuffNumArr.length()>0)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
                    	{
                    		jsonWrap.put(JSONWrap.KEY.REMOVE_BUFF, removeBuffNumArr);
                    	}
                    	if(blueBloodArr.length()>0)
                    	{
                    		jsonWrap.put(JSONWrap.KEY.BLUE_BLOOD, blueBloodArr);
                    	}
                    	return jsonWrap;
	                }                                    
                }                
                return null;
        	}
        }        
        return null;        
	}
}
