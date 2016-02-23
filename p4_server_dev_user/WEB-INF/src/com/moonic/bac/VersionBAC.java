package com.moonic.bac;

import org.json.JSONArray;
import org.json.JSONObject;

import server.common.Tools;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.moonic.util.BACException;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.DBPoolClearListener;
import com.moonic.util.DBPsRs;

import conf.Conf;

/**
 * �ϴ����°汾
 * @author alexhy
 */
public class VersionBAC {
	public static final String tab_version_apk = "tab_version_apk";	
	public static final String tab_version_res = "tab_version_res";
//	public static final String tab_version_patch = "tab_version_patch";
	public static final String tab_version_filelist = "tab_version_filelist";
	
	
	static
	{
		DBPool.getInst().addTxtClearListener(new TxtPoolClearListener());
	}
	/**
	 * ������汾
	 */
	public ReturnValue checkApkVer(int platform,String clientVer, String channel, String packageName, boolean isBigApk, boolean needPatch, String imei, String mac) {
		try {			
			//����Ƿ�����õİ汾
			String[] testVersionArr=null;
			String testVersions = ConfigBAC.getString("testVersion");
			if(testVersions!=null && !testVersions.equals("0"))
			{
				testVersionArr = Tools.splitStr(testVersions,",");
			}
			if(Conf.testRedir!=null && !Conf.testRedir.equals("") && testVersionArr!=null && Tools.strArrContain(testVersionArr, clientVer))
			{
				JSONArray arr = new JSONArray();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("redir", Conf.testRedir); //���ؿͻ�������ת���µ�ַ
				arr.add(jsonObj);
				
				return new ReturnValue(true, arr.toString());
			}
			else
			{
				DBPaRs channeListRs = ChannelBAC.getInstance().getChannelListRs(channel);
				if(!channeListRs.exist()){
					BACException.throwInstance("�����벻���� channel="+channel);
				}
				String platformFolder = ResFilelistBAC.getPlatformFolderByPlatformNum(platform);				
				
				String fileListCRC="";
				//��ȡ��Ӧƽ̨��filelist��crc
				DBPsRs filelistCRCRs = DBPool.getInst().pQueryS("tab_version_filelist", "platform="+platform+" and enable=1");
				
				if(filelistCRCRs!=null && filelistCRCRs.next())
				{
					fileListCRC = filelistCRCRs.getString("crc");
				}			
				
				String apkchannel = channeListRs.getString("apkchannel");
//				int updateType = channeListRs.getInt("updatetype"); //1:�ɴ򲹶� 2:ֻ������APK 3:�򿪸�����վ
				//String reschannel = channeListRs.getString("reschannel");
				DBPsRs apkRs = DBPool.getInst().pQueryS(tab_version_apk,"platform="+platform+" and version>'"+clientVer+"' and channel='"+apkchannel+"'", "version desc", 1);
				apkRs.next();
				
//				DBPsRs patchRs = null;		
				
				//if(!isBigApk && needPatch && platform==1)				
				
				
				//��ȡ��Դ��Ŀ¼
				String subfolder="";
				DBPsRs subfolderRs = DBPool.getInst().pQueryS(tab_version_filelist, "platform="+platform);
				if(subfolderRs!=null && subfolderRs.have())
				{
					subfolderRs.next();
					subfolder = Tools.strNull(subfolderRs.getString("subfolder"));
				}
				String web = getChannelWeb(apkchannel);	
				
				//TODO û�в�����
				/*if(updateType==1 || updateType==3) //�ɴ򲹶�
				{ 
					if(platform==1) //android�ͻ��˲������²���
					{
						if(apkRs.have()) 
						{					
							patchRs = DBPool.getInst().pQueryS(tab_version_patch, "fromversion='"+clientVer+"' and toversion='"+apkRs.getString("version")+"' and packagename='"+packageName+"' and channel='"+apkchannel+"'");
							if(!patchRs.have()) {
								patchRs = DBPool.getInst().pQueryS(tab_version_patch, "fromversion='"+clientVer+"' and toversion='"+apkRs.getString("version")+"' and packagename is null and channel='"+apkchannel+"'");
							}
						}
					}
					
					if(patchRs!=null && patchRs.have())  //�п��ò���
					{
						String preApkCRC="";
						//��ɰ汾apk��crcֵ
						DBPsRs preApkRs = DBPool.getInst().pQueryS(tab_version_apk,"platform="+platform+" and version='"+clientVer+"' and channel='"+apkchannel+"'", "version desc", 1);
						if(preApkRs.next())
						{
							preApkCRC = preApkRs.getString("crc");
						}
						
						patchRs.next();
						JSONArray arr = new JSONArray();
						JSONObject jsonObj = new JSONObject();
						String patchfile = patchRs.getString("patchfile");
						int filesize = patchRs.getInt("filesize");	
						String patchfilecrc = patchRs.getString("crc");
						
						String downPath = ServerConfig.dl_apk_url + platformFolder+"/"+ apkchannel + "/" + patchfile;
						jsonObj.put("updatetype",2);//�������� 1���ļ� 2������
						jsonObj.put("ver",apkRs.getString("version"));//�汾��
						jsonObj.put("patchfilename",patchfile);//�����ļ���
						jsonObj.put("patchfilesize",filesize);//�����ļ���С
						jsonObj.put("patchfilecrc",patchfilecrc);//�����ļ�crc
						jsonObj.put("patchdownpath",downPath);//����·��		
						jsonObj.put("apkfilename",apkRs.getString("updfile"));//�ļ���
						jsonObj.put("apkfilesize",apkRs.getInt("filesize"));
						jsonObj.put("apkfilecrc",apkRs.getString("crc")); //apk�ļ�crc
						jsonObj.put("mustupdate",apkRs.getInt("mustupdate"));//��Ҫ����	
						jsonObj.put("oldapkcrc",preApkCRC); //�ɰ汾APK��CRC,���ڴ򲹶�ǰ��У��
						
						String apkFileName = apkRs.getString("updfile");	
						String apkDownPath = ServerConfig.dl_apk_url + platformFolder+"/"+ apkchannel + "/" + apkFileName;
						jsonObj.put("apkdownpath",apkDownPath); //��apk����·��	
						jsonObj.put("web", web);
						
						arr.add(jsonObj);
						arr.add(ServerConfig.dl_res_url + platformFolder + (subfolder.equals("")?"/":"/"+subfolder+"/")); //��Դ����url	
						arr.add(fileListCRC); //��Դ�ļ��б�CRC					
						return new ReturnValue(true, arr.toString());
					} 					
				}		*/			
				
				if(apkRs.have()) //��APK
				{
					JSONArray arr = new JSONArray();
					JSONObject jsonObj = new JSONObject();
					String fileName = apkRs.getString("updfile");				
							
					String downPath = ServerConfig.dl_apk_url + platformFolder+"/"+ apkchannel + "/" + fileName;
					jsonObj.put("updatetype",1);//�������� 1���ļ� 2������
					jsonObj.put("ver",apkRs.getString("version"));//�汾��
					jsonObj.put("apkfilename",fileName);//�ļ���
					jsonObj.put("downpath",downPath);//����·��						
					jsonObj.put("apkfilesize",apkRs.getInt("filesize"));
					jsonObj.put("apkfilecrc",apkRs.getString("crc"));
					jsonObj.put("mustupdate",apkRs.getInt("mustupdate"));//��Ҫ����
					jsonObj.put("web", web);
					
					arr.add(jsonObj);
					
//					arr.add(ServerConfig.dl_res_url + platformFolder + (subfolder.equals("")?"/":"/"+subfolder+"/")); //��Դ����url		
//					arr.add(fileListCRC); //��Դ�ļ��б�crc
					return new ReturnValue(true, arr.toString());
				}
				else {	
					JSONArray arr = new JSONArray();
					JSONObject jsonObj = new JSONObject();
					
					arr.add(jsonObj);
					
//					arr.add(ServerConfig.dl_res_url + platformFolder + (subfolder.equals("")?"/":"/"+subfolder+"/")); //��Դ����url	
//					arr.add(fileListCRC); //��Դ�ļ��б�crc					
					return new ReturnValue(true, arr.toString());
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		}
	}
	
	/**
	 * �����Դ�汾
	 */
	public ReturnValue checkResVer(String clientVer, byte platform){
		try {
			DBPsRs resRs = DBPool.getInst().pQueryS(tab_version_res, "version>'"+clientVer+"' and platform="+platform, "version");
			JSONArray jsonarr = new JSONArray();
			while(resRs.next()){
				String fileName = resRs.getString("updfile");
				String subfolder = resRs.getString("subfolder");
				if(subfolder == null || subfolder.trim().length() == 0) {
					subfolder = "";
				} else {
					subfolder += "/";
				}
				String resPlatform = ResFilelistBAC.getPlatformFolderByPlatformNum(platform);
				String downPath = ServerConfig.dl_res_url + subfolder + resPlatform + "/" + fileName;
				JSONArray arr = new JSONArray();
				arr.add(resRs.getString("version"));//�汾��
				arr.add(fileName);//�ļ���
				arr.add(downPath);//����·��
				arr.add(resRs.getInt("filesize"));//�ļ���С
				arr.add(resRs.getInt("mustupdate"));//��Ҫ����
				jsonarr.add(arr);
			}
			return new ReturnValue(true, jsonarr.toString());
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} 
	}
	
	static String[][] channelWebData;
	
	/**
	 * ��ȡ����������ַ
	 * @param channel
	 * @return
	 */
	public String getChannelWeb(String channel)
    {
        if(channelWebData == null)
    	{
        	String txt=null;
        	try {
    			txt = DBPool.getInst().readTxtFromPool("channel_web");
    			channelWebData = Tools.getStrLineArrEx2(txt, "data:","dataEnd");
    		} catch (Exception e) {		
    			e.printStackTrace();    			
    		}
    	}
        
    	for(int i=0;channelWebData!=null && i<channelWebData.length;i++)
    	{
    		if(channelWebData[i][0].equals(channel))
    		{
    			return channelWebData[i][1];
    		}
    	}
            
        return "0";
    }
	
	/**
	 * ��ȡ��ԴCRC�б��ı�
	 * @param phonePlatform �ֻ�ƽ̨����1��׿2ios
	 * @param channel ����  ��ͬ������Դ�������ͬʱʹ��
	 * @param serverId ��Ϸ������id
	 * @return
	 */
	/*public ReturnValue getResCRCFileList(int phonePlatform,String channel)
	{
		try {
			//��serverId��Ӧ��server��reslv	
			long t1=System.currentTimeMillis();
			byte[] fileBytes = ResFilelistBAC.getInstance().getFileListStr(phonePlatform);
			long t2=System.currentTimeMillis();
			System.out.println("��ʱ"+(t2-t1)+"����");
			if(fileBytes!=null)
			{
				return new ReturnValue(true,fileBytes);
			}
			else
			{
				return new ReturnValue(false,"");
			}
						
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false,"");
		} 
	}*/
		
	//--------------��̬��--------------
	
	private static VersionBAC instance = new VersionBAC();
	
	/**
	 * ��ȡʵ��
	 */
	public static VersionBAC getInstance(){
		return instance;
	}
	static class TxtPoolClearListener implements DBPoolClearListener
	{
		public void callback(String key)
		{
			if(key!=null && key.toLowerCase().equals("channel_web"))
			{
				channelWebData=null;
			}
		}
	}
}
