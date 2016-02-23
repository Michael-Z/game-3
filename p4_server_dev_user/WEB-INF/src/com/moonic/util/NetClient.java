package com.moonic.util;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.ehc.common.ReturnValue;


/**
 * �����ͻ���
 * @author alexhy
 *
 */
public class NetClient
{

	private boolean allowWatch;

	private static String proxyStr;
	private static int port;


	private String address;
	private int act; // ���Ͷ���
	private NetListener listener; // ������
	private byte[] sendBytes; // ����������
	private List<NameValuePair> params; //form
	private List<NameValuePair> httpHeadParams; //httpͷ�������
	
	private boolean success; //�����Ƿ�ɹ�
	private byte[] returnBytes;//�������ص�����
	private String contentType="application/octet-stream";

	public NetClient()
	{

	}

	/**
	 * ����http body��������
	 * @param type
	 */
	public void setContentType(String type)
	{
		contentType = type;
	}
	/**
	 * ����http�ύ��ַ
	 * @param theAddress
	 */
	public void setAddress(String theAddress)
	{
		address = theAddress;
	}	
	/**
	 * ����ҵ��act����
	 * @param act
	 */
	public void setAct(int act)
	{
		this.act = act;
	}	

	/**
	 * ����http body����������
	 * @param sendBytes
	 */
	public void setSendBytes(byte[] sendBytes)
	{
		this.sendBytes = sendBytes;
	}

	/**
	 * ���http body������
	 * @param name
	 * @param value
	 */
	public void addParameter(String name, String value)
	{
		if (params == null)
		{
			params = new ArrayList<NameValuePair>();
		}
		params.add(new BasicNameValuePair(name, value));
	}

	/**
	 * ���http body������
	 * @param name
	 * @param value
	 */
	public void addParameter(String name, int value)
	{
		if (params == null)
		{
			params = new ArrayList<NameValuePair>();
		}
		params.add(new BasicNameValuePair(name, String.valueOf(value)));
	}
	/**
	 * ���http head����
	 * @param name
	 * @param value
	 */
	public void addHttpHead(String name, String value)
	{
		if (httpHeadParams == null)
		{
			httpHeadParams = new ArrayList<NameValuePair>();
		}
		httpHeadParams.add(new BasicNameValuePair(name, value));
	}
	/**
	 * ���http head����
	 * @param name
	 * @param value
	 */
	public void addHttpHead(String name, int value)
	{
		if (httpHeadParams == null)
		{
			httpHeadParams = new ArrayList<NameValuePair>();
		}
		httpHeadParams.add(new BasicNameValuePair(name, String.valueOf(value)));
	}
	/**
	 * ͬ�����ͷ�ʽ
	 * @return
	 */
	public ReturnValue send()
	{
		if (sendBytes != null)
		{				
			HttpURLConnection httpPost=null;
			try {
				
				ByteArrayOutputStream byteArrayOut = null;
				URL url = new URL(address);
				httpPost = (HttpURLConnection)url.openConnection();
				httpPost.setReadTimeout(10000);
				httpPost.setConnectTimeout(10000);
				OutputStream out = null;
				InputStream in = null;
				
				httpPost.setRequestMethod("POST");
				httpPost.setDoInput(true);
				httpPost.setDoOutput(true);
				httpPost.setUseCaches(false);
				
				//httpPost.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//httpPost.setRequestProperty("Content-Type", "application/octet-stream");
				httpPost.setRequestProperty("Content-Type", contentType);
				for(int i=0;httpHeadParams!=null && i<httpHeadParams.size();i++)
				{
					NameValuePair nameValue = httpHeadParams.get(i);
					httpPost.setRequestProperty(nameValue.getName(), nameValue.getValue());
				}
				
				out = httpPost.getOutputStream();
				out.write(sendBytes);
				out.flush();
				out.close();	
				int statusCode = httpPost.getResponseCode();
				if(statusCode==200)
				{
					in = httpPost.getInputStream();
					byteArrayOut = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int len = 0;
					while ((len = in.read(buf)) != -1) 
					{
						byteArrayOut.write(buf, 0, len);
					}
					byte[] bytes = byteArrayOut.toByteArray();
					in.close();
					if(bytes!=null && bytes.length>0)
					{
						return new ReturnValue(true,bytes);
					}
					else
					{
						return new ReturnValue(false,"û������");
					}
				}
				else
				{
					return new ReturnValue(false,"����ʧ��,statusCode="+statusCode);					
				}					
			} 
			catch (Exception e) {
				//e.printStackTrace();
				System.out.println("����"+address+"�����쳣"+e.toString());
				return new ReturnValue(false,"����ʧ��"+e.toString());
			} 
			finally 
			{					
				if (httpPost != null) 
				{
					httpPost.disconnect();
				}
			}				
		}
		else
		if (params != null)
		{
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
			HttpConnectionParams.setSoTimeout(httpParams, 10000);
			
			// �½�HttpClient����
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			
			if (proxyStr != null && port > 0)
			{
				HttpHost proxy = new HttpHost(proxyStr, port);
				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
			//System.out.println("��"+address+"��������");
			HttpPost post = new HttpPost(address);
			/*if (sendBytes != null)
			{
				ContentProducer cp = new ContentProducer()
				{
					public void writeTo(OutputStream outstream) throws IOException
					{
						outstream.write(sendBytes);
						//System.out.println("��������"+new String(sendBytes,"UTF-8"));
					}
				};
				HttpEntity entity = new EntityTemplate(cp);
				post.setEntity(entity);			
			}
			else if (params != null)*/
			{
				try
				{
					post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
					post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
			for(int i=0;httpHeadParams!=null && i<httpHeadParams.size();i++)
			{
				NameValuePair nameValue = httpHeadParams.get(i);
				post.addHeader(nameValue.getName(), nameValue.getValue());
			}

			HttpResponse response = null;

			try
			{
				response = httpClient.execute(post);
				int statusCode = response.getStatusLine().getStatusCode();
				//System.out.println("statusCode="+statusCode);
				if (statusCode == 200)
				{
					/** 
					 * ��Ϊֱ�ӵ���toString���ܻᵼ��ĳЩ�����ַ������������������Դ˴�ʹ��toByteArray 
					 * �����Ҫת��String���󣬿����ȵ���EntityUtils.toByteArray()��������Ϣʵ��ת��byte���飬 
					 * ����new String(byte[] bArray)ת�����ַ����� 
					 */
					byte[] buff = EntityUtils.toByteArray(response.getEntity());
					if (buff != null)
					{					
						return new ReturnValue(true,buff);					
					}
					else
					{
						return new ReturnValue(false,"����Ч����");		
					}
				}
				else
				{	
					return new ReturnValue(false,"����ʧ��,statusCode="+statusCode);
				}
			}		
			catch (Exception e)
			{				
				//e.printStackTrace();
				System.out.println("����"+address+"�����쳣"+e.toString());
				return new ReturnValue(false,"����ʧ��"+e.toString());
			}	
		}
		else //get���ͷ�ʽ
		{
			HttpURLConnection httpPost=null;
			try {
				ByteArrayOutputStream byteArrayOut = null;
				URL url = new URL(address);
				httpPost = (HttpURLConnection)url.openConnection();
				httpPost.setReadTimeout(10000);
				httpPost.setConnectTimeout(10000);
				InputStream in = null;
				
				httpPost.setRequestMethod("GET");
				httpPost.setDoInput(true);
				httpPost.setDoOutput(false);
				httpPost.setUseCaches(false);
				
				//httpPost.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//httpPost.setRequestProperty("Content-Type", "application/octet-stream");
				httpPost.setRequestProperty("Content-Type", contentType);
				for(int i=0;httpHeadParams!=null && i<httpHeadParams.size();i++)
				{
					NameValuePair nameValue = httpHeadParams.get(i);
					httpPost.setRequestProperty(nameValue.getName(), nameValue.getValue());
				}
				
				int statusCode = httpPost.getResponseCode();
				if(statusCode==200)
				{
					in = httpPost.getInputStream();
					byteArrayOut = new ByteArrayOutputStream();
					byte[] buf = new byte[4096];
					int len = 0;
					while ((len = in.read(buf)) != -1) 
					{
						byteArrayOut.write(buf, 0, len);
					}
					byte[] bytes = byteArrayOut.toByteArray();
					in.close();
					if(bytes!=null && bytes.length>0)
					{
						return new ReturnValue(true,bytes);
					}
					else
					{
						return new ReturnValue(false,"û������");
					}
				}
				else
				{
					return new ReturnValue(false,"����ʧ��,statusCode="+statusCode);					
				}					
			} 
			catch (Exception e) {
				//e.printStackTrace();
				System.out.println("����"+address+"�����쳣"+e.toString());
				return new ReturnValue(false,"����ʧ��"+e.toString());
			} 
			finally 
			{					
				if (httpPost != null) 
				{
					httpPost.disconnect();
				}
			}
		}
	}
	/**
	 * �첽���ͷ�ʽ
	 * @param listener ������
	 */
	public void send(NetListener listener)
	{
		this.listener = listener;
		(new Thread(new NetSender())).start();
	}
	class NetSender implements Runnable
	{
		public void run()
		{
			if (sendBytes != null)
			{				
				HttpURLConnection httpPost=null;
				try {
					ByteArrayOutputStream byteArrayOut = null;
					URL url = new URL(address);
					httpPost = (HttpURLConnection)url.openConnection();
					httpPost.setConnectTimeout(10000);
					httpPost.setReadTimeout(10000);
					OutputStream out = null;
					InputStream in = null;
					
					httpPost.setRequestMethod("POST");
					httpPost.setDoInput(true);
					httpPost.setDoOutput(true);
					httpPost.setUseCaches(false);
					
					//httpPost.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					//httpPost.setRequestProperty("Content-Type", "application/octet-stream");
					httpPost.setRequestProperty("Content-Type", contentType);
					for(int i=0;httpHeadParams!=null && i<httpHeadParams.size();i++)
					{
						NameValuePair nameValue = httpHeadParams.get(i);
						httpPost.setRequestProperty(nameValue.getName(), nameValue.getValue());
					}
					
					out = httpPost.getOutputStream();
					out.write(sendBytes);
					out.flush();
					out.close();	
					int statusCode = httpPost.getResponseCode();
					if(statusCode==200)
					{
						in = httpPost.getInputStream();
						byteArrayOut = new ByteArrayOutputStream();
						byte[] buf = new byte[4096];
						int len = 0;
						while ((len = in.read(buf)) != -1) 
						{
							byteArrayOut.write(buf, 0, len);
						}
						byte[] bytes = byteArrayOut.toByteArray();
						in.close();
						if(bytes!=null && bytes.length>0)
						{
							listener.callBack(act, NetListener.RESULT_SUCCESS, bytes);
						}
						else
						{
							listener.callBack(act, NetListener.RESULT_FAIL, "û������");
						}
					}
					else
					{
						listener.callBack(act, NetListener.RESULT_NETFAILURE, "����ʧ��,statusCode="+statusCode);
					}					
				} 
				catch (Exception e) {
					e.printStackTrace();
					listener.callBack(act, NetListener.RESULT_NETFAILURE, "����ʧ��"+e.toString());
				} 
				finally 
				{					
					if (httpPost != null) 
					{
						httpPost.disconnect();
					}
				}				
			}
			else
			if (params != null)
			{
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
				HttpConnectionParams.setSoTimeout(httpParams, 10000);
				
				// �½�HttpClient����
				HttpClient httpClient = new DefaultHttpClient(httpParams);
				
				if (proxyStr != null && port > 0)
				{
					HttpHost proxy = new HttpHost(proxyStr, port);
					httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
				}
				
				HttpPost post = new HttpPost(address);
				/*if (sendBytes != null)
				{
					ContentProducer cp = new ContentProducer()
					{
						public void writeTo(OutputStream outstream) throws IOException
						{
							outstream.write(sendBytes);
						}
					};
					HttpEntity entity = new EntityTemplate(cp);
					post.setEntity(entity);
				}
				else 
				if (params != null)*/
				{
					try
					{
						post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
						post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
				}
				for(int i=0;httpHeadParams!=null && i<httpHeadParams.size();i++)
				{
					NameValuePair nameValue = httpHeadParams.get(i);
					post.addHeader(nameValue.getName(), nameValue.getValue());
				}
				
				HttpResponse response = null;

				try
				{
					response = httpClient.execute(post);
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == 200)
					{
						/** 
						 * ��Ϊֱ�ӵ���toString���ܻᵼ��ĳЩ�����ַ������������������Դ˴�ʹ��toByteArray 
						 * �����Ҫת��String���󣬿����ȵ���EntityUtils.toByteArray()��������Ϣʵ��ת��byte���飬 
						 * ����new String(byte[] bArray)ת�����ַ����� 
						 */
						byte[] buff = EntityUtils.toByteArray(response.getEntity());
						if (buff != null)
						{						
							listener.callBack(act, NetListener.RESULT_SUCCESS,buff);
						}
						else
						{
							listener.callBack(act, NetListener.RESULT_FAIL, "û������");
						}					
						
					}
					else
					{						
						listener.callBack(act, NetListener.RESULT_NETFAILURE, "����ʧ��statusCode="+statusCode);
					}
				}			
				catch (Exception e)
				{
					e.printStackTrace();
					listener.callBack(act, NetListener.RESULT_NETFAILURE, "����ʧ��"+e.toString());
				}	
			}
			else
			{
				HttpURLConnection httpPost=null;
				try {
					ByteArrayOutputStream byteArrayOut = null;
					URL url = new URL(address);
					httpPost = (HttpURLConnection)url.openConnection();
					httpPost.setConnectTimeout(10000);
					httpPost.setReadTimeout(10000);
					
					InputStream in = null;
					
					httpPost.setRequestMethod("GET");
					httpPost.setDoInput(true);
					httpPost.setDoOutput(false);
					httpPost.setUseCaches(false);
					
					//httpPost.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					//httpPost.setRequestProperty("Content-Type", "application/octet-stream");
					httpPost.setRequestProperty("Content-Type", contentType);
					for(int i=0;httpHeadParams!=null && i<httpHeadParams.size();i++)
					{
						NameValuePair nameValue = httpHeadParams.get(i);
						httpPost.setRequestProperty(nameValue.getName(), nameValue.getValue());
					}
					
					int statusCode = httpPost.getResponseCode();
					if(statusCode==200)
					{
						in = httpPost.getInputStream();
						byteArrayOut = new ByteArrayOutputStream();
						byte[] buf = new byte[4096];
						int len = 0;
						while ((len = in.read(buf)) != -1) 
						{
							byteArrayOut.write(buf, 0, len);
						}
						byte[] bytes = byteArrayOut.toByteArray();
						in.close();
						if(bytes!=null && bytes.length>0)
						{
							listener.callBack(act, NetListener.RESULT_SUCCESS, bytes);
						}
						else
						{
							listener.callBack(act, NetListener.RESULT_FAIL, "û������");
						}
					}
					else
					{
						listener.callBack(act, NetListener.RESULT_NETFAILURE, "����ʧ��,statusCode="+statusCode);
					}					
				} 
				catch (Exception e) {
					e.printStackTrace();
					listener.callBack(act, NetListener.RESULT_NETFAILURE, "����ʧ��"+e.toString());
				} 
				finally 
				{					
					if (httpPost != null) 
					{
						httpPost.disconnect();
					}
				}	
			}
		}
	}

	
	public void ignoreSSL()
	{			
		try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}
			} };
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String string, SSLSession ssls) {
							return true;
						}
					});				
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	public static void main(String[] args)
	{
		/*NetClient.getGiftList(new NetListener()
		{
			public void callBack(int act, int result, String strData)
			{
				System.out.println(strData);
			}
		});*/
		/*NetClient.orderCallBack(new NetListener()
		{
			public void callBack(int act, int result, String strData)
			{
				System.out.println(strData);
			}
		});*/
		/*try {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}
			} };
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String string, SSLSession ssls) {
							return true;
						}
					});
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			URL url = new URL(createSessionURL);
			HttpURLConnection connect = (HttpURLConnection) url .openConnection();
			connect.setDoOutput(true);
			DataOutputStream out = new DataOutputStream(connect .getOutputStream());
			out.writeBytes(postData);
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader( connect.getInputStream()));

			String line;

			while ((line = in.readLine()) != null) {
				// sessionId += "n" + line;
				System.out.println("page info ===> " + line);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
}
