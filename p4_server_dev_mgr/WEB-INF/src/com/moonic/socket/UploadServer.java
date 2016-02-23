package com.moonic.socket;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import server.common.Tools;
import server.config.ServerConfig;

import com.moonic.util.MyTimerTask;
import com.moonic.util.MyTools;

import conf.Conf;

/**
 * �ϴ�����
 * @author John
 */
public class UploadServer {
	private ServerSocket serversocket;
	private boolean isRun;

	private int port = Conf.uploadServerPort;
	private ScheduledExecutorService timer;
	/**
	 * ����
	 */
	private UploadServer() {}
	
	/**
	 * ��������
	 */
	public void start() {
		if (!isRun) {
			try {
				isRun = true;
				(new UploadThread()).start();
				System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+"�����ϴ��������,��ʼ����" + port + "�˿�");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+"��" + port + "�˿ڵ�TCP�ϴ����������������У�����ֹͣ��");
		}
	}
	
	/**
	 * ֹͣ����
	 */
	public void stop() {
		isRun = false;
		MyTools.cancelTimer(timer);
		timer=null;
		if (serversocket != null) {
			try {				
				serversocket.close();
				serversocket=null;
			} catch (Exception e) {
				e.printStackTrace();
			}
			instance=null;
		}
	}
	
	//--------------�ڲ���--------------
	
	/**
	 * �����ֳ�
	 */
	class UploadThread extends Thread {
		public void run() {
			try {
				serversocket = new ServerSocket(port);
				while (isRun) {
					try {
						Socket socket = serversocket.accept();
						System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+"�ɹ���������" + socket.getRemoteSocketAddress() + "���ϴ�TCP����");
						DoUploadThread doUploadThread = new DoUploadThread(socket);
						(new Thread(doUploadThread)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				UploadServer.this.stop();
				System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+"�˿�" + port + "���ϴ�������ֹͣ");
			}
		}
	}
	
	/**
	 * �ϴ��߳�
	 */
	class DoUploadThread implements Runnable {
		private Socket socket;
		

		public DoUploadThread(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				SessionidTT sessionidTT = new SessionidTT(dis);
				
				timer.schedule(sessionidTT, 10 * 60 * 1000, TimeUnit.MILLISECONDS);
				byte[] uploadBytes = Tools.getBytesFromInputstream(dis);
				sessionidTT.cancel();
				int splitIndex = -1;
				for (int i = 0; i < uploadBytes.length; i++) {
					if (uploadBytes[i] == ';') {
						splitIndex = i;
						break;
					}
				}
				if (splitIndex > 0) {
					String fileInfo = new String(uploadBytes, 0, splitIndex);
					int pathSplit = fileInfo.indexOf('#');
					if (pathSplit != -1) {
						String subpath = fileInfo.substring(0, pathSplit);
						String filename = fileInfo.substring(pathSplit + 1);
						String fullPath = ServerConfig.getAppRootPath() + "download/" + subpath;
						File folder = new File(fullPath);
						if (!folder.exists()) {
							folder.mkdirs();
						}
						File file = new File(fullPath + filename);
						System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey + "д�ļ�·��=" + (fullPath + filename));
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(uploadBytes, splitIndex + 1, uploadBytes.length - (splitIndex + 1));
						fos.close();
						System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+ "�ϴ��ļ��ɹ�,д��" + (uploadBytes.length - (splitIndex + 1)) + "�ֽ�");
					}
				} else {
					System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+ "δ�ҵ��ļ��ָ���;");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * ��ʱ����
	 */
	class SessionidTT extends MyTimerTask {
		public DataInputStream dis;

		public SessionidTT(DataInputStream dis) {
			this.dis = dis;
		}

		public void run2() {
			try {
				System.out.println(Tools.getCurrentDateTimeStr()+"--" +Conf.stsKey+ "�ϴ���ʱ10���ӣ��Զ��Ͽ�");
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//--------------��̬��--------------
	
	private static UploadServer instance;
	
	/**
	 * ��ȡʵ��
	 */
	public static UploadServer getInstance() 
	{
		if(instance==null)
		{
			instance = new UploadServer();
			instance.timer = MyTools.createTimer(1);
		}
		return instance;
	}
}
