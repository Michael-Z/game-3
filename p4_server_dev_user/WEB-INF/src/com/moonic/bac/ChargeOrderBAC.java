package com.moonic.bac;

import java.net.URLDecoder;
import java.sql.ResultSet;

import org.json.JSONException;
import org.json.JSONObject;

import server.common.Tools;
import server.config.LogBAC;
import server.config.ServerConfig;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.ehc.dbc.BaseActCtrl;
import com.moonic.chargecenter.OrderCenter;
import com.moonic.mgr.PookNet;
import com.moonic.servlet.STSServlet;
import com.moonic.util.DBHelper;
import com.moonic.util.DBPaRs;
import com.moonic.util.DBPool;
import com.moonic.util.MD5;
import com.moonic.util.MyTools;
import com.moonic.util.NetClient;
import com.moonic.util.NetFormSender;
import com.moonic.util.STSNetSender;

/**
 * ��ֵ����֧���ӿ�
 * @author ����
 *
 */
public class ChargeOrderBAC extends BaseActCtrl 
{
	public static String tbName = "tab_charge_order";
	
	private ChargeOrderBAC()
	{
		super.setTbName(tbName);
		setDataBase(ServerConfig.getDataBase());
	}		
	
	
	public ReturnValue updateCenterOrderNo(String orderNo, String cOrderNo)
	{
		SqlString sqlStr = new SqlString();
		sqlStr.add("corderNo", cOrderNo);		
		DBHelper dbHelper = new DBHelper();
		try
		{
			dbHelper.update(tbName, sqlStr, "orderNo='"+orderNo+"'");
			return new ReturnValue(true);
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false,e.toString());
		}
		finally
		{
			dbHelper.closeConnection();
		}
	}
	
	public ReturnValue createCenterNewOrderWithoutCOrder(String orderNo, int orderType,int price,String username,String extend,String ordertime,String ip,int userSource)
	{
		if(extend==null || extend.equals(""))
		{
			return new ReturnValue(false,"ȱ����չ����extend");	
		}		
		
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			
			String channel="001"; //��������Ĭ��Ϊ��������			
			
			//String extendStr = URLDecoder.decode(extend,"UTF-8");
			int serverId=0;
			int playerId=0;
			int buyType=1;
			int systemtype=1; //Ĭ�ϰ�׿
			
			//ChannelChargeTypeBAC.getInstance().getCount("channel='"+channel+"' and chargetype="+orderType)
			
			String platform=null;
			int power = 0; //��Ȩ
			try
			{		
				JSONObject json = new JSONObject(extend);
				
				int vsid = json.optInt("serverId");
				DBPaRs channelServerRs = DBPool.getInst().pQueryA(ServerBAC.tab_channel_server, "vsid="+vsid);
				if(!channelServerRs.exist()){
					LogBAC.logout("chargecenter/"+channel,"������δ�ҵ� vsid="+vsid);
					return new ReturnValue(false,"������δ�ҵ� vsid="+vsid);	
				}
				serverId = channelServerRs.getInt("serverid");
				playerId = json.optInt("playerId");				
				buyType = json.optInt("buytype");
				power = json.optInt("power");
				channel = json.optString("channel");
				systemtype = json.optInt("system");
				if(systemtype==0)systemtype=1; //�޲���Ĭ�ϰ�׿
				
				if(channel==null || channel.equals(""))
				{
					channel = "001";
				}
				if(buyType==0)
				{
					LogBAC.logout("chargecenter/"+channel,"��չ����ȱ��buyType");
					return new ReturnValue(false,"����ѡ��������");	
				}
				if(serverId==0)
				{
					LogBAC.logout("chargecenter/"+channel,"��չ����ȱ��serverId");
					return new ReturnValue(false,"����ѡ����Ϸ��");	
				}
				if(playerId==0)
				{
					LogBAC.logout("chargecenter/"+channel,"��չ����ȱ��playerId");
					return new ReturnValue(false,"��Ϸ��ɫδѡ��");	
				}	
				/*try
				{
					platform= json.getString("platform");
				}
				catch(Exception ex)
				{
					
				}*/
				
			}
			catch(Exception ex)
			{
				LogBAC.logout("chargecenter/"+channel,channel+"�����Ķ���"+orderNo+"��չ��������json��ʽ"+extend);
				//System.out.println("����"+orderNo+"��չ��������json��ʽ"+extendStr);
			}				
			platform = ChannelBAC.getInstance().getChannelListRs(channel).getString("platform");
			
			/*if(platform==null || platform.equals(""))
			{
				platform=channel;
			}*/
			
			boolean exist = dbHelper.queryExist(tbName, "orderno='"+orderNo+"'");
			
			if(!exist)
			{		
				if(buyType==1)
				{
					power = 0; //�����ͻ��˴�����power
				}
				//У��
				if(!ConfigBAC.getBoolean("chargetest"))
				{
					if(buyType!=1 && buyType!=2)
					{
						LogBAC.logout("chargecenter/"+channel,"buytype="+buyType+",�������ͷǷ�");
						return new ReturnValue(false,"�������ͷǷ�");	
					}
					if(buyType==1)
					{
						if(orderType==OrderCenter.iosInfullType)
						{
							if(price<6)
							{
								LogBAC.logout("chargecenter/"+channel,"buytype=1 price="+price+",����������6Ԫ");
								return new ReturnValue(false,"����������6Ԫ");
							}
						}
						else
						{
							if(price<10)
							{
								LogBAC.logout("chargecenter/"+channel,"buytype=1 price="+price+",����������10Ԫ");
								return new ReturnValue(false,"����������10Ԫ");
							}
						}
					}
					else
					if(buyType==2)
					{
						if(price>0)
						{							
							DBPaRs dbPaRs= DBPool.getInst().pQueryA("tab_prerogative", "price="+price);
							
							if(!dbPaRs.exist())
							{
								LogBAC.logout("chargecenter/"+channel,"buytype=2 price="+price+",��Ȩ������Ƿ�");
								return new ReturnValue(false,"��Ȩ������Ƿ�");
							}
							power = dbPaRs.getInt("num");

							if(power<=0)
							{
								LogBAC.logout("chargecenter/"+channel,"buytype=2 price="+price+",��Ȩ������Ƿ�");
								return new ReturnValue(false,"��Ȩ������Ƿ�");
							}			
						}
						else
						{
							LogBAC.logout("chargecenter/"+channel,"buytype=2 price="+price+",��Ȩ������������0Ԫ");
							return new ReturnValue(false,"��Ȩ������������0Ԫ");
						}
					}
				}
				
				int from=userSource; //1�ͻ��� 2 ��վ 
				/*if(orderType==1 || orderType==2 || orderType==3 || orderType==4 || orderType==5 || orderType==14 || orderType==99)
				{
					from=1;
				}
				else
				{
					from=2;
				}*/
				SqlString sqlStr = new SqlString();
				sqlStr.add("orderNo", orderNo);
				sqlStr.add("orderType", orderType);				
				sqlStr.add("fromWhere", from);
				sqlStr.add("buyType",buyType);
				sqlStr.add("price", price);
				sqlStr.add("serverid", serverId);
				sqlStr.add("playerId", playerId);
				sqlStr.add("channel", channel);
				sqlStr.add("platform", platform);
				sqlStr.add("corderType", orderType);
				sqlStr.add("chargecenter", 1);
				sqlStr.add("systemtype",systemtype);
				/*if(Tools.str2int(channel)<=100)
				{
					sqlStr.add("platform", channel); //channel����100�Ķ��������˺�����
				}
				else
				{
					sqlStr.add("platform", "001");  //channel����100�Ķ��㲨���˺�����
				}*/
				
				sqlStr.add("username", username);
				sqlStr.add("extend", extend);
				sqlStr.add("getpower", power);
				sqlStr.add("ip", ip);
				sqlStr.addDateTime("ordertime", ordertime);
				sqlStr.addDateTime("savetime", MyTools.getTimeStr());
				sqlStr.add("result", 0);
				sqlStr.add("gived", 0);
				
				dbHelper.insert(tbName, sqlStr);
				//System.out.println("����"+System.currentTimeMillis()+"�����¶���"+orderNo);
				
				LogBAC.logout("chargecenter/"+channel,"����"+channel+"�����µĶ���"+orderNo+",serverId="+serverId+",username="+username+",playerId="+playerId+",orderType="+orderType+",buyType="+buyType+",price="+price+",platform="+platform+",extend="+extend+",power="+power);
				return new ReturnValue(true,"");	
			}
			else
			{				
				LogBAC.logout("chargecenter/"+channel,channel+"�����Ķ�����"+orderNo+"�Ѵ���");
				return new ReturnValue(false,"�������Ѵ���");
				//return new ReturnValue(false,"same orderno is exist!");
			}
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	
	String spchannels="010,013,021,022,023"; //֧���ɹ����л�Ϊ���ע���������������
	
	/**
	 * ����ר�÷��ض������
	 * @param orderNo ������
	 * @param result ���1�ɹ� 0ʧ��
	 */
	public synchronized ReturnValue orderCallback(String channel,String orderNo, int result,String note,String ip,int realPayMoney)
	{
		DBHelper dbHelper = new DBHelper();
		try {
			JSONObject jsonObj=null;
			
			if(channel!=null && !channel.equals("")) //���������Լ�sdk������
			{
				jsonObj = dbHelper.queryJsonObj("tab_charge_order", null, "orderno='"+orderNo+"' and channel='"+channel+"'");
			}
			else //��������
			{				
				jsonObj = dbHelper.queryJsonObj("tab_charge_order", null, "orderno='"+orderNo+"'");						
			}
			if(jsonObj!=null)
			{				
				int orderResult =  jsonObj.optInt("result");
				int serverId = jsonObj.optInt("serverId");
				int playerId= jsonObj.optInt("playerId");
				short chargeType=(short)jsonObj.optInt("ordertype"); //1:֧���� 2:����
				int money = jsonObj.optInt("price"); //�����
				int buytype = jsonObj.optInt("buytype");
				byte powernum = (byte)jsonObj.optInt("getpower");
				int gived = jsonObj.optInt("gived"); //�Ƿ��ѷ���
				int from=jsonObj.optInt("fromWhere"); //1�ͻ��� 2 ��վ 
				String sourceChannel = jsonObj.optString("channel"); //֧������ʱ������
				String centerOrderNo = jsonObj.optString("corderno");//��ֵ���Ķ�����
				
				if(realPayMoney < money)
				{
					return new ReturnValue(false,"�۸�ƥ��,�����۸�"+money+"ʵ��ֻ֧����"+realPayMoney);
				}
				
				
				if(channel==null || channel.equals("")) //���������ص���channel
				{
					if(sourceChannel==null || sourceChannel.equals("") || spchannels.indexOf(sourceChannel)==-1) //���������������
					{
						try
						{
							JSONObject json = dbHelper.queryJsonObj("tab_player", "userid", "id="+playerId);
							int userid = json.optInt("userid");
							json = dbHelper.queryJsonObj("tab_user", "channel", "id="+userid);				
							channel = json.optString("channel"); //������������
							/*if(( channel.equals("009") || channel.equals("010") || channel.equals("018")) && chargeType!=99) //�����г���Ӧ�û㣬���壬���ǲ�������ר����ֵ�滻�ɲ���
							{
								channel ="001";
							}*/
							LogBAC.logout("charge/"+channel,"�յ��������֪ͨorderNo="+orderNo+",result="+result);
							SqlString sqlStr = new SqlString();
							sqlStr.add("channel", channel);
							dbHelper.openConnection();
							dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"'"); //���¶���������Ϊ��ҵ�����
							dbHelper.closeConnection();
							LogBAC.logout("charge/","���¶�������Ϊ��ҵ�����"+ channel);
						}
						catch(Exception ex)
						{
							System.out.println("channel="+channel+",playerId="+playerId+",orderNo="+orderNo+",result="+result);
							ex.printStackTrace();
							return new ReturnValue(false,"���¶�������ʧ��:"+ex.toString());
						}
					}
					else
					{
						channel = sourceChannel;
					}
				}
				
				if(orderResult==0 || (ConfigBAC.getBoolean("chargetest") && orderResult==-1) || (channel.equals("003") && orderResult==-1)) //δ����Ķ���,003�������λ���ʧ�ܱ�ɹ��Ķ��������⴦��
				{
					SqlString sqlStr = new SqlString();
					if(result==1)
					{	
						if(buytype==1)
						{
							sqlStr.add("result", 1);
							sqlStr.add("gived", 2); //������
							sqlStr.add("getcoin", getCoinByRMB(chargeType, money));
							if(ip!=null)sqlStr.add("ip",ip);
							sqlStr.addDateTime("savetime", Tools.getCurrentDateTimeStr()); //����ʱ��Ϊ����ʱ��
							dbHelper.openConnection();
							int updateReslut = dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"' and channel='"+channel+"' and result=0");
							dbHelper.closeConnection();							
							
							if(updateReslut>0) //��������result=0 �и��¼�¼ʱ��֪ͨ��Ǯ
							{
								//�������ʯ����Ȩ
								LogBAC.logout("charge/"+channel,channel+"�����Ķ���"+orderNo+"֧���ɹ�,֧�������="+money+",playerId="+playerId+",chargeType="+chargeType+",buytype="+buytype+",����Ϸ��"+serverId+"������Ϣ");
								STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_RECHARGE);
								sender.dos.writeByte(1);
								sender.dos.writeUTF(""); //�ɹ�����
								sender.dos.writeByte(from); //��Դ
								sender.dos.writeUTF(channel); //��������
								sender.dos.writeUTF(orderNo); //������
								sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
								sender.dos.writeInt(playerId);
								sender.dos.writeByte(0);//TODO ��ֵ�㣺��Ҫ�ͻ��˷��ͣ����ڼ�¼��ֵ��־�ͱ���ͳ��
								sender.dos.writeShort(chargeType);
								sender.dos.writeInt(money);
								ServerBAC.getInstance().sendReqToOne(sender, serverId);
							}
						}
						else
						if(buytype==2)
						{		
							if(ConfigBAC.getBoolean("chargetest"))
							{
								//�����ڴ���
								sqlStr.add("result", 1);
								sqlStr.add("gived", 2); //������
								sqlStr.add("getpower", powernum);
								if(ip!=null)sqlStr.add("ip",ip);
								sqlStr.addDateTime("savetime", Tools.getCurrentDateTimeStr()); //����ʱ��Ϊ����ʱ��
								dbHelper.openConnection();
								int updateReslut = dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"' and channel='"+channel+"' and result=0");
								dbHelper.closeConnection();								
								
								if(updateReslut>0) //��������result=0 �и��¼�¼ʱ��֪ͨ��Ǯ
								{
									//�������ʯ����Ȩ
									STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_BUY_TQ);
									sender.dos.writeByte(1);
									sender.dos.writeUTF(""); //�ɹ�����
									sender.dos.writeByte(from); //��Դ
									sender.dos.writeUTF(channel); //��������
									sender.dos.writeUTF(orderNo); //������
									sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
									sender.dos.writeInt(playerId);
									sender.dos.writeByte(powernum);
									ServerBAC.getInstance().sendReqToOne(sender, serverId);
									LogBAC.logout("charge/"+channel,channel+"�����Ķ���"+orderNo+"֧���ɹ�,֧�������="+money+",playerId="+playerId+",buytype="+buytype+",�����Ȩ="+powernum+",����Ϸ��"+serverId+"������Ϣ");
								}
								
							}
							else
							{
								//��ʽ����
								dbHelper.openConnection();
								DBPaRs dbPaRs= DBPool.getInst().pQueryA("tab_prerogative", "price="+money);
								dbHelper.closeConnection();
								if(dbPaRs.exist())
								{
									sqlStr.add("result", 1);
									sqlStr.add("gived", 2); //������
									powernum = (byte)dbPaRs.getInt("num");
									sqlStr.add("getpower", powernum);
									if(ip!=null)sqlStr.add("ip",ip);
									sqlStr.addDateTime("savetime", Tools.getCurrentDateTimeStr()); //����ʱ��Ϊ����ʱ��
									dbHelper.openConnection();
									int updateReslut = dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"' and channel='"+channel+"' and result=0");
									dbHelper.closeConnection();									
									
									if(updateReslut>0) //��������result=0 �и��¼�¼ʱ��֪ͨ��Ǯ
									{
										//�������ʯ����Ȩ
										STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_BUY_TQ);
										sender.dos.writeByte(1);
										sender.dos.writeUTF(""); //�ɹ�����
										sender.dos.writeByte(from); //��Դ
										sender.dos.writeUTF(channel); //��������
										sender.dos.writeUTF(orderNo); //������
										sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
										sender.dos.writeInt(playerId);
										sender.dos.writeByte(powernum);
										ServerBAC.getInstance().sendReqToOne(sender, serverId);
										LogBAC.logout("charge/"+channel,channel+"�����Ķ���"+orderNo+"֧���ɹ�,֧�������="+money+",playerId="+playerId+",buytype="+buytype+",�����Ȩ="+powernum+",����Ϸ��"+serverId+"������Ϣ");
									}
								}
								else
								{
									//System.out.println("����������Ȩʧ��money="+money);
									LogBAC.logout("charge/"+channel,channel+"����������Ȩʧ��money="+money);
									
									sqlStr.add("result", -1);
									dbHelper.openConnection();
									int updateReslut = dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"' and channel='"+channel+"' and result=0");
									dbHelper.closeConnection();									
									
									if(updateReslut>0) //��������result=0 �и��¼�¼ʱ��֪ͨ��Ǯ
									{									
										STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_BUY_TQ);
										sender.dos.writeByte(0);
										sender.dos.writeUTF("�۸����"); //ʧ������
										sender.dos.writeByte(from); //��Դ
										sender.dos.writeUTF(channel); //��������
										sender.dos.writeUTF(orderNo); //������
										sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
										sender.dos.writeInt(playerId);
										ServerBAC.getInstance().sendReqToOne(sender, serverId);
									}
								}
							}							
						}
						else
						{
							LogBAC.logout("charge/"+channel,channel+"�����Ķ���"+orderNo+"��buytype="+buytype+"��֧��,playerId="+playerId);
						}
					}
					else
					{
						sqlStr.add("result", -1);
						sqlStr.add("note", note);
						dbHelper.openConnection();
						dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"' and channel='"+channel+"'");
						dbHelper.closeConnection();
						if(buytype==1)
						{
							LogBAC.logout("charge/"+channel,channel+"��������ʯ����"+orderNo+"֧��ʧ��,֧�������="+money+",playerId="+playerId+",����Ϸ��"+serverId+"������Ϣ");	
						}
						else
						if(buytype==2)
						{
							LogBAC.logout("charge/"+channel,channel+"��������Ȩ����"+orderNo+"֧��ʧ��,֧�������="+money+",playerId="+playerId+",����Ϸ��"+serverId+"������Ϣ");	
						}
						else						
						{
							LogBAC.logout("charge/"+channel,channel+"�����Ķ���"+orderNo+"֧��ʧ��,֧�������="+money+",playerId="+playerId+",����buytype="+buytype+"��֧��");
						}
						if(buytype==2)
						{
							STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_BUY_TQ);
							sender.dos.writeByte(0);
							sender.dos.writeUTF("����֧��ʧ��:"+note); //ʧ������
							sender.dos.writeByte(from); //��Դ
							sender.dos.writeUTF(channel); //��������
							sender.dos.writeUTF(orderNo); //������
							sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
							sender.dos.writeInt(playerId);
							ServerBAC.getInstance().sendReqToOne(sender, serverId);
						}
						else
						{
							STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_RECHARGE);
							sender.dos.writeByte(0);
							sender.dos.writeUTF("����֧��ʧ��:"+note); //ʧ������
							sender.dos.writeByte(from); //��Դ
							sender.dos.writeUTF(channel); //��������
							sender.dos.writeUTF(orderNo); //������
							sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
							sender.dos.writeInt(playerId);
							ServerBAC.getInstance().sendReqToOne(sender, serverId);
						}						
					}
					//dbHelper.update(tbName, sqlStr, "orderno='"+orderNo+"'");
					
					return new ReturnValue(true,"");
				}
				else
				{
					//return new ReturnValue(false,"�ö����Ѵ����");
					/*STSNetSender sender = new STSNetSender(STSServlet.G_PLAYER_RECHARGE);
					sender.dos.writeByte(0);
					sender.dos.writeUTF("�ö����Ѵ����"); //ʧ������
					sender.dos.writeByte(from); //��Դ
					sender.dos.writeUTF(channel); //��������
					sender.dos.writeUTF(orderNo); //������
					sender.dos.writeUTF(centerOrderNo);//��ֵ���Ķ�����
					sender.dos.writeInt(playerId);
					ServerBAC.getInstance().sendReqToOne(sender, serverId);*/
					//System.out.println("������"+orderNo+"�Ѵ����");
					LogBAC.logout("charge/"+channel,channel+"�����Ķ�����"+orderNo+"�Ѵ����");
					//return new ReturnValue(false,"this order has been processed");
					return new ReturnValue(true,"������"+orderNo+"�Ѵ����");
				}				
			}
			else
			{				
				LogBAC.logout("charge/"+channel,channel+"�����Ķ�����"+orderNo+"������");
				return new ReturnValue(false,"orderno not exist");
			}
			
		} catch(Exception e){
			e.printStackTrace();
			return new ReturnValue(false, e.toString());
		} finally {
			dbHelper.closeConnection();
		}		
	}
	
	private static Object ordernoLock; //��ȡ�����ŵ���
	static
	{
		ordernoLock = new Object();
	}
	
	/**
	 * ��ȡ��һ�������
	 */
	public static String getNextOrderNo()
	{
		synchronized(ordernoLock) 
		{
			String orderno = String.valueOf(System.currentTimeMillis());
			try 
			{
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return orderno;
		}
	}
	
	/**
	 * ��ȡ��ֵ������
	 */
	public ReturnValue getChargeOrderno(String channel,String extend)
	{
		if(channel==null || channel.equals(""))
		{
			return new ReturnValue(false,"ȱ��������");
		}
		String token=null;
		String refresh_token=null;
		if(channel.equals("008")) //360��������ˢ��token
		{
			String appKey = "531ca7e66f8743fde3ea7b7cd68b52f4";
			String appSecret = "129d4c155bce4a5f6cc9a397014d9451";
			JSONObject json;
			try {
				json = new JSONObject(extend);
				refresh_token = json.optString("refresh_token");
				//https://openapi.360.cn/oauth2/access_token?grant_type=refresh_token&refresh_token=12065961868762ec8ab911a3089a7ebdf11f8264d5836fd41&client_id=0fb2676d5007f123756d1c1b4b5968bc&client_secret=8d9e3305c1ab18384f56.....&scope=basic
				String url = "https://openapi.360.cn/oauth2/access_token";
				//LogBAC.logout("charge/"+channel, "�û���֤url="+url);
				NetClient netClient = new NetClient();
				netClient.setAddress(url);
				netClient.setContentType("application/x-www-form-urlencoded");
				String sendStr = "grant_type=refresh_token"
						+"&refresh_token="+refresh_token
						+"&client_id="+appKey
						+"&client_secret="+appSecret
						+"&scope=basic";
				
				netClient.ignoreSSL();
				netClient.setSendBytes(sendStr.getBytes());
				ReturnValue rv = netClient.send();
				if(rv.success)
				{
					try
					{
						String result = new String(rv.binaryData,"UTF-8");
						//{"access_token":"51274456c5021375a1d60d37e50d389efda2ebd703684fae","expires_in":"36000","scope":"basic","refresh_token":"512744561753b0725722cac52304a60b6bc5413e38708b53"}
						JSONObject resultJson = new JSONObject(result);
						token = resultJson.optString("access_token");
						refresh_token= resultJson.optString("refresh_token");
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						return new ReturnValue(false,"ˢ��toeknʧ��"+ex.toString());
					}
					//{ "access_token":"120652e586871bb6bbcd1c7b77818fb9c95d92f9e0b735873", "expires_in":"36000", "scope":"basic", "refresh_token":"12065961868762ec8ab911a3089a7ebdf11f8264d5836fd41 " }
				}
				else
				{
					return new ReturnValue(false,"ˢ��toeknʧ��"+rv.info);
				}
			} catch (JSONException e) {				
				e.printStackTrace();
				return new ReturnValue(false,"ˢ��toeknʧ��"+e.toString());
			}
		}
		
		String orderno = getNextOrderNo();
		JSONObject json;
		try {
			json = new JSONObject(extend);
			int price = json.getInt("price");
			String username = json.optString("username");
			//orderno ="ON"+orderno;
			
			ReturnValue rv=null;
			
			//String centerChannel = ConfigBAC.getString("centerChannel");
			
			//if(centerChannel!=null && !centerChannel.equals("") && centerChannel.indexOf(channel)!=-1)
			{
				//���󷢵���ֵ����
				rv = OrderCenter.getInstance().sendToCenter(99, orderno, price, "0",username, 1, "", "0.0.0.0", "", "", "", "", "", extend,null);
				if(rv.success)
				{
					JSONObject orderJson = new JSONObject(rv.info);
					LogBAC.logout("sdk", "���������ɹ�,���ķ����ִ�="+rv.info);
					orderno = orderJson.optString("orderId"); //��ȡ���Ķ�����
					
					LogBAC.logout("sdk", "���������ɹ�,���Ķ�����="+orderno);
				}
				else
				{
					LogBAC.logout("sdk", "��������ʧ��,ԭ��="+rv.info);
				}
			}
			/*else
			{
				rv = ChargeOrderBAC.getInstance().createNewOrder(channel,orderno, 99, price,username, extend, Tools.getCurrentDateTimeStr(),"0.0.0.0");
			}*/
			
			if(rv.success)
			{				
				if(channel.equals("008")) //360��������ˢ��token
				{
					//{"orderno":"33333":"token":"dddddd","refresh_token":"ddddd"}
					JSONObject returnJson = new JSONObject();
					returnJson.put("orderno", orderno);
					returnJson.put("token", token);
					returnJson.put("refresh_token", refresh_token);
					return new ReturnValue(true,returnJson.toString());
				}
				else
				{
					JSONObject returnJson = new JSONObject();
					returnJson.put("orderno", orderno);
					return new ReturnValue(true,returnJson.toString());
				}					
			}
			else
			{
				return new ReturnValue(false,"���ɶ���ʧ��"+rv.info);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false,"���ɶ���ʧ��"+e.toString());
		}
	}
	
	/**
	 * ��������ҿɹ������ʯ
	 * @param rechargetype ֧����ʽ
	 * @param rmb �����
	 */
	public int getCoinByRMB(int rechargetype, int rmb) 
	{
		DBHelper dbHelper = new DBHelper();
		try {
			dbHelper.openConnection();
			int buycoin = rmb * 10; //��ֵ��õ���ʯ
			int rebatecoin = 0; //�����͵���ʯ
			//if(rechargetype==1 || rechargetype==2) //֧���� | ����
			/*{
				ResultSet rebateRs = dbHelper.query("TAB_RECHARGE_REBATE", "rebateam", "rechargeam<="+rmb, "rechargeam desc", 1);
				if(rebateRs.next()){
					rebatecoin = rebateRs.getInt("rebateam");
				}
			}*/
			int count_temp = rmb;
			while(true){
				ResultSet rebateRs = dbHelper.query("tab_recharge_rebate", "rechargeam,rebateam", "rechargeam<="+count_temp, "rechargeam desc", 1);
				if(!rebateRs.next()){
					break;
				}
				rebatecoin += rebateRs.getInt("rebateam");
				count_temp -= rebateRs.getInt("rechargeam");
			}
			return buycoin + rebatecoin;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			dbHelper.closeConnection();
		}
	}
	
	/**
	 * ��ȡ���˳�ֵ�����
	 */
	public ReturnValue getCardValue(String cardNum)
	{
		NetFormSender sender = new NetFormSender(PookNet.getcardvalue_do);

		sender.addParameter("platformType","6");
		sender.addParameter("cardNum",cardNum);
		String key = "x#2xilnx0t9x0opsr8";
		String sign = MD5.encode("6"+cardNum+key);
		sender.addParameter("signature",sign);
		try {
			sender.send().check();			
			 //{"ret":"S","msg":"","cardValue":10000}
			JSONObject json = new JSONObject(sender.rv.info);
			//System.out.println("���˵㿨"+cardNum+"���ز�ѯ���ԭʼ���="+sender.rv.info);
			json.setForceLowerCase(false);
			if(json.optString("ret").equals("S"))
			{
				int cardValue = json.optInt("cardvalue");
				//���˵㵥λ/1000תΪԪ
				int price = cardValue / 1000;
				json.remove("cardvalue");
				json.put("cardValue", price);
				return new ReturnValue(true,json.toString());
			}
			else
			{
				return new ReturnValue(false,sender.rv.info);
			}
			//System.out.println("���˵㿨"+cardNum+"���ز�ѯ�����="+json.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnValue(false,e.toString());
		}		
	}
	/**
	 * ���������Ӧ��֧�������Ƿ����
	 * @param channel
	 * @param chargeType
	 * @return
	 */
	public boolean checkChannelChargeType(String channel,int chargeType)
	{
		try
		{
			DBPaRs channelServerRs = DBPool.getInst().pQueryA("tab_channel_charge_type", "channel='"+channel+"' and chargeType="+chargeType);
			if(channelServerRs.exist())
			{
				return true;
			}
			else
			{
				return false;
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	//--------------��̬��--------------
	
	private static ChargeOrderBAC self = new ChargeOrderBAC();
	
	public static ChargeOrderBAC getInstance()
	{
		return self;
	}	
}
