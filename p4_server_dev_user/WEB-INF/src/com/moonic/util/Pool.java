package com.moonic.util;

import java.util.Vector;

public class Pool 
{
	private static Vector<PoolObj> vc;
	
	/**
	 * �������Pool��Ĭ�Ϲ���ʱ��30��
	 * @param id ����id
	 * @param obj Ҫ����Ķ���
	 */
	public static synchronized void addObjectToPool(String id,Object obj)
	{
		addObjectToPool(id,30,obj);
	}
	
	/**
	 * �������Pool
	 * @param id ����id
	 * @param expireSecond ����ʱ�����룩-1 ������
	 * @param obj Ҫ����Ķ���
	 */
	public static synchronized void addObjectToPool(String id,int expireSecond,Object obj)
	{
		if(obj==null)
		{
			return;
		}
		if(vc==null)		
		{
			vc = new Vector<PoolObj>();
		}	
		
		PoolObj poolObj = new PoolObj();
		poolObj.id = id;
		poolObj.time = System.currentTimeMillis();
		poolObj.expireSecond = expireSecond;
		poolObj.obj = obj;
		
		//���������滻ԭ����
		for(int i=0;i<vc.size();i++)
		{
			PoolObj oldPoolObj = vc.elementAt(i);
			if(oldPoolObj.id.equals(id))
			{
				vc.remove(i);
				vc.add(poolObj);
				return;
			}
		}
		//�����µ�
		vc.add(poolObj);		
	}
	/**
	 * �ӻ������Ƴ�
	 * @param id
	 */
	public static synchronized void removeObjectFromPoolById(String id)
	{
		for(int i=0;i<vc.size();i++)
		{
			PoolObj poolObj = vc.elementAt(i);
			if(poolObj.id.equals(id))
			{	
				vc.remove(poolObj);
				return;
			}
		}
	}
	/**
	 * ��Pool�л�ȡ����Ķ������Ϊ�ձ�ʾ�޴˶��������ѹ���
	 * @param id ����id
	 * @return
	 */
	public static synchronized Object getObjectFromPoolById(String id)
	{
		if(vc!=null)
		{
			for(int i=0;i<vc.size();i++)
			{
				PoolObj poolObj = vc.elementAt(i);
				if(poolObj.id.equals(id))
				{
					//�ж�ʱ��
					if(System.currentTimeMillis()-poolObj.time < poolObj.expireSecond * 1000 || poolObj.expireSecond==-1)
					{
						return poolObj.obj;
					}
					else
					{
						//�������
						vc.remove(poolObj);
						return null;
					}
				}
			}
			return null;
		}
		else
		{
			return null;
		}
	}
	/**
	 * �������
	 */
	static class PoolObj
	{
		String id;
		long time; //�洢ʱ�ĺ�����
		int expireSecond; //Ĭ��30�� 
		Object obj;
	}
}

