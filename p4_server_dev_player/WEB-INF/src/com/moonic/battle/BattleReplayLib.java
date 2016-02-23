package com.moonic.battle;

import java.util.Hashtable;

import server.config.ServerConfig;

import com.moonic.util.DBHelper;

public class BattleReplayLib 
{
	//public static String replayStrData;
	//public static Vector<String> replayCacheData;
	public static int maxCacheLen=1000; //��໺����
	public static Hashtable<String, String> replayCacheData = new Hashtable<String, String>(1000);
	
	/*static
	{
		readFixData();
	}*/
    

    /*public static void readFixData()
    {
        if (replayStrData != null) return;  //ֻ����һ��
        
        replayCacheData=new Vector<String>();
        
		try {
			replayStrData = DBPool.getInst().readTxtFromPool("battle_replay");
		} catch (Exception e) {		
			e.printStackTrace();
			return;
		}
		if(replayStrData==null)
		{
			System.out.println("tab_txt��battle_replay�����ڣ�");
			return;
		}
    }*/
	
	/**
	 * ����ս���طŻ���
	 * @param battleId
	 * @param replayData
	 */
	public static synchronized void addBattleReplayData(long battleId,String replayData)
	{
		if(!replayCacheData.containsKey(String.valueOf(battleId)))
		{
			replayCacheData.put(String.valueOf(battleId), replayData);		
		}
	}
    public static synchronized String getReplayStrData(long battleId)
    {
    	//�����ݿ⻺������
    	/*if(replayStrData != null)
    	{
    		String str = Tools.getSubString(replayStrData, "battle"+battleId+":", "battle"+battleId+"End");
    		if(str!=null)
    		{
    			//System.out.println("�����ݿ⻺���л�ȡbattleId="+battleId+"�Ļط�����");
    			return "data:"+str+"dataEnd";
    		}
    	}*/
    	
    	//���ڴ滺���������
    	if(replayCacheData!=null)
    	{
    		String cacheStr =replayCacheData.get(String.valueOf(battleId));
    		if(cacheStr!=null && !cacheStr.equals(""))
    		{
    			//System.out.println("�ӻطŻ�������л�ȡbattleId="+battleId+"�Ļط�����");
    			return cacheStr;
    		}
    	}
    	
    	//����־�ļ�Ŀ¼��Ѱ��
    	String logFileStr = null;
    	DBHelper dbHelper = new DBHelper(ServerConfig.getDataBase_Log());
    	try {
    		dbHelper.openConnection();
    		/*ResultSet logRs = dbHelper.query(LogTbName.TAB_BATTLE_RECORD(), "replaydata", "battleid="+battleId);
    		if(logRs.next()){
    			logFileStr = new String(logRs.getBytes("replaydata"), "UTF-8");
    		}*/
    		
    	} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbHelper.closeConnection();
		}
    	if(logFileStr!=null)
    	{
    		//System.out.println("����־�ļ��л�ȡbattleId="+battleId+"�Ļط�����");
    		
    		if(replayCacheData.size()>maxCacheLen)
    		{    		
    			replayCacheData.clear();    			
    			System.out.println("ս���طŻ�������ѳ���"+maxCacheLen+",����طŻ������");
    		}    		
    		replayCacheData.put(String.valueOf(battleId), logFileStr);    		
    		
    		//System.out.println("replayCacheData.size()="+replayCacheData.size());
    			
    		return logFileStr;
    	}
    	
        return null;
    }   
}
