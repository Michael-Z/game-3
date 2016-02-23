package com.moonic.bac;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import server.common.Tools;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;
import com.moonic.util.DBHelper;
import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;

public class ScheduleTask
{
	public static final byte STATE_WAIT=0; //�ȴ���
	public static final byte STATE_TIMER=1; //��ʱ��
	public static final byte STATE_RUN=2; //ִ����
	public static final byte STATE_COMPLETE=3; //��ִ�����
	
	private int id;
	private String name;
	private int type;
	private int theState; //0�ȴ��� 
	private ScheduledExecutorService timer;
	private MyTimerTask timerTask;
	private int executeTimes; //ִ�д���
	private long period; //ִ�м��������Ϊ��λ
	private Date startTime;
	
	public int getExecuteTimes()
	{
		return executeTimes;
	}
	public int getState()
	{
		return theState;
	}
	public int getId()
	{
		return id;
	}
	public String getName()
	{
		return name;
	}
	public int getType()
	{
		return type;
	}
	public String getStartTime()
	{
		return Tools.date2str(startTime);
	}
	public long getPeriod()
	{
		return period/1000;
	}
	public static String getTypeName(int type)
	{
		if(type==1) //�ط�
		{
			return "�ط�";
		}
		else
		if(type==2) //����
		{
			return "����";
		}
		else
		if(type==3) //ִ��SQL
		{
			return "ִ��SQL";
		}
		return "δ����";
	}
	
	/**
	 * ��������
	 * @param type ��������
	 * @param startTime ִ�п�ʼʱ��
	 * @param period ʱ����(��Ϊ��λ)
	 * @param sql ִ�е�sql���
	 * @return
	 */
	public boolean setTask(final int id,String name,int state,int type,Date startTime,final long period,final String sql,final String word1,final String word2,final String word3)
	{
		timerTask =null;
		if(type==1) //�ط�
		{
			this.id = id;
			this.name = name;
			this.type = type;
			this.theState = state;
			this.startTime = startTime;
			this.period = period * 1000;
			timerTask = new MyTimerTask()
			{					
				public void run2() 
				{
					System.out.println(Tools.getCurrentDateTimeStr()+"--"+"�ط�");
					
					if(word1!=null && !word1.equals(""))
					{
						System.out.println("����������䣺"+word1);
						ServerBAC.getInstance().maintain(0, word2, word1, word3, 0);
					}
					else
					{
						ServerBAC.getInstance().maintain(0, word2, word1, word3, 0);
					}
					
					/*
					if(word2!=null && !word2.equals(""))
					{
						System.out.println("����ά������䣺"+word2);
						ServerBAC.getInstance().closeMainServerLogin(word2);
					}
					else
					{
						ServerBAC.getInstance().closeMainServerLogin("ϵͳά����");
					}
					*/
					
					if(period>0)
					{
						theState=STATE_RUN;								
					}
					else
					{
						theState=STATE_COMPLETE;	
						ScheduleBAC.getInstance().stopTaskById(id);
					}
					executeTimes++;
					updateTimes(id,executeTimes);
					//�������ݿ��е�״̬
					updateState(id,theState);					
				}
			};	
			return true;
		}
		else
		if(type==2) //����
		{
			this.id = id;
			this.name = name;
			this.type = type;
			this.theState = state;
			this.startTime = startTime;
			this.period = period * 1000;
			
			timerTask = new MyTimerTask()
			{					
				public void run2() {
					ServerBAC.getInstance().openGameServer(0);
					/*
					ServerBAC.getInstance().openMainServerLogin();
					*/
					System.out.println(Tools.getCurrentDateTimeStr()+"--"+"����");
					if(period>0)
					{
						theState=STATE_RUN;								
					}
					else
					{
						theState=STATE_COMPLETE;	
						ScheduleBAC.getInstance().stopTaskById(id);
					}
					executeTimes++;
					updateTimes(id,executeTimes);
					//�������ݿ��е�״̬
					updateState(id,theState);	
				}
			};	
			return true;
		}
		else
		if(type==3) //SQL
		{
			this.id = id;
			this.name = name;
			this.type = type;
			this.theState = state;
			this.startTime = startTime;
			this.period = period * 1000;
			
			timerTask = new MyTimerTask()
			{					
				public void run2() {
					DBHelper dbHelper = new DBHelper();
					try {
						dbHelper.execute(sql);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally
					{
						dbHelper.closeConnection();
					}
					System.out.println(Tools.getCurrentDateTimeStr()+"--"+"ִ��SQL��"+sql);
					if(period>0)
					{
						theState=STATE_RUN;								
					}
					else
					{
						theState=STATE_COMPLETE;
						ScheduleBAC.getInstance().stopTaskById(id);
					}
					executeTimes++;
					updateTimes(id,executeTimes);
					//�������ݿ��е�״̬
					updateState(id,theState);	
				}
			};	
			return true;
		}
		return false;
	}
	/**
	 * �����������ݿ��е�״̬
	 * @param taskId
	 * @param state
	 */
	public static void updateState(int taskId,int state)
	{
		//�������ݿ��е�״̬
		DBHelper dbHelper = new DBHelper();
		SqlString sqlS = new SqlString();
		sqlS.add("state", state);
		try {
			dbHelper.update("TAB_SCHEDULE", sqlS, "id="+taskId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally					
		{
			dbHelper.closeConnection();
		}		
	}
	public static void updateTimes(int taskId,int times)
	{
		//�������ݿ��е�״̬
		DBHelper dbHelper = new DBHelper();
		SqlString sqlS = new SqlString();
		sqlS.add("exectimes", times);
		try {
			dbHelper.update("TAB_SCHEDULE", sqlS, "id="+taskId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally					
		{
			dbHelper.closeConnection();
		}
	}

	/**
	 * ֹͣ����
	 */
	public ReturnValue stop()
	{
		if(theState!=STATE_RUN && theState!=STATE_TIMER)
		{
			return new ReturnValue(false,"ֻ�м�ʱ�л�ִ���е��������ֹͣ");
		}
		
		if(timer!=null)
		{
			MyTools.cancelTimer(timer);
		}
		timer=null;
		theState=STATE_WAIT;
		executeTimes=0;		
		updateTimes(id,executeTimes);
		updateState(id, getState());
		return new ReturnValue(true,"�ƻ�����["+name+"]ֹͣ�ɹ�");
	}
	/**
	 * ��������
	 * @param type
	 * @param startTime
	 * @param period
	 * @param sql
	 */
	public ReturnValue start()
	{
		if(theState!=STATE_WAIT && theState!=STATE_COMPLETE)
		{
			return new ReturnValue(false,"ֻ�еȴ��л�����ɵ������������");
		}
		
		if(Tools.compareStrDate(Tools.getCurrentDateTimeStr(), Tools.date2str(startTime))>0) //��ǰ���ڿ�ʼʱ��
		{
			return new ReturnValue(false,"�ƻ�����["+name+"]��ʼʱ���ѹ���");
		}
		
		if(timer!=null)
		{
			MyTools.cancelTimer(timer);			
		}				
		timer = MyTools.createTimer(3);
		executeTimes=0;
		
		if(period>0)
		{
			theState = STATE_TIMER;
			timer.scheduleAtFixedRate(timerTask, Math.max(startTime.getTime()-System.currentTimeMillis(), 0), period, TimeUnit.MILLISECONDS);
		}
		else
		{
			theState = STATE_TIMER;
			timer.schedule(timerTask, Math.max(startTime.getTime()-System.currentTimeMillis(), 0), TimeUnit.MILLISECONDS);
		}
		updateTimes(id,executeTimes);
		updateState(id, getState());
		
		return new ReturnValue(true,"�ƻ�����["+name+"]�����ɹ�");
	}
}
