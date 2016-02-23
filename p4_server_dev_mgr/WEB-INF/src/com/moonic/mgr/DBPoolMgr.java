package com.moonic.mgr;

import com.ehc.dbc.BACListener;
import com.ehc.dbc.BaseActCtrl;
import com.moonic.bac.ServerBAC;
import com.moonic.servlet.STSServlet;
import com.moonic.util.DBPool;
import com.moonic.util.ProcessQueue;
import com.moonic.util.ProcessQueueTask;
import com.moonic.util.STSNetSender;

/**
 * �������
 * @author John
 */
public class DBPoolMgr {
	
	/**
	 * ��ʼ���建��ص�
	 */
	public void initClearPoolListener(){
		BaseActCtrl.setBACListener(new BACListener() {
			public void onSave(String table) {
				try {
					addClearTablePoolTask(table, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});			
	}
	
	/**
	 * �������б�������
	 */
	public void addClearTablePoolTask(String table, DBPoolMgrListener listener){
		pq.addTask(new ClearServerPoolTask(STSServlet.G_CLEAR_TABPOOL, table, listener));
		DBPool.getInst().clearTableFromPool(table);
	}
	
	/**
	 * �������ı���������
	 */
	public void addClearTxtPoolTask(String filename, DBPoolMgrListener listener){
		pq.addTask(new ClearServerPoolTask(STSServlet.G_CLEAR_TXTPOOL, filename, listener));
		DBPool.getInst().clearTxtFromPool(filename);
	}
	
	//--------------�ڲ���--------------
	
	private ProcessQueue pq = new ProcessQueue();
	
	/**
	 * ֪ͨ�����洦��
	 * @author John
	 */
	private class ClearServerPoolTask implements ProcessQueueTask {
		public short act;
		public String key;
		public DBPoolMgrListener listener;
		public ClearServerPoolTask(short act, String key, DBPoolMgrListener listener){
			this.act = act;
			this.key = key;
			this.listener = listener;
		}
		public void execute(){
			try {
				STSNetSender sender = new STSNetSender(act);
				sender.dos.writeUTF(key);
				ServerBAC.getInstance().sendReqToAll(ServerBAC.STS_USER_SERVER, sender);
				ServerBAC.getInstance().sendReqToAll(ServerBAC.STS_GAME_SERVER, sender);
				if(listener != null){
					listener.callback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//--------------��̬��--------------

	private static DBPoolMgr instance = new DBPoolMgr();
	
	/**
	 * ��ȡʵ��
	 */
	public static DBPoolMgr getInstance() {
		return instance;
	}
}
