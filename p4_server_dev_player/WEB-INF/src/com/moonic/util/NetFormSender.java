package com.moonic.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.ehc.common.ReturnValue;
import com.ehc.common.SqlString;

import conf.Conf;
import conf.LogTbName;

/**
 * ������
 * @author alexhy,John
 */
public class NetFormSender {
	private String address;
	private List<NameValuePair> params;
	
	public ReturnValue rv;
	
	public static MyLog log = new MyLog(MyLog.NAME_DATE, "log_nfs", "NFS", true, false, true, null);
	
	/**
	 * ����
	 */
	public NetFormSender(String address) {
		this.address = address;
		
		params = new ArrayList<NameValuePair>();
	}
	
	/**
	 * �������
	 */
	public void addParameter(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}

	/**
	 * �������
	 */
	public void addParameter(String name, int value) {
		params.add(new BasicNameValuePair(name, String.valueOf(value)));
	}
	
	/**
	 * ��������
	 */
	public NetFormSender send() {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		HttpConnectionParams.setSoTimeout(httpParams, 10000);

		HttpClient httpClient = new DefaultHttpClient(httpParams);

		log.d("��������" + address);
		HttpPost post = new HttpPost(address);
		try {
			post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		HttpResponse response = null;
		try {
			response = httpClient.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			log.d("״̬�룺" + statusCode);
			if(statusCode != 200){
				BACException.throwInstance("����" + address + "�쳣statusCode=" + statusCode);
			}
			byte[] buff = EntityUtils.toByteArray(response.getEntity());
			if (buff != null) {
				String strData = new String(buff, "UTF-8");
				rv = new ReturnValue(true, strData);
			} else {
				rv = new ReturnValue(false, "û������");
			}
			log.d("���ؽ����" + rv.info);
		} catch (Exception e) {
			System.out.println("����"+address+"���쳣");
			SqlString sqlStr = new SqlString();
			sqlStr.add("sendreqserver", Conf.stsKey);
			sqlStr.add("accessurl", address);
			sqlStr.add("reqinfo", "-");
			sqlStr.add("excinfo", e.toString());
			sqlStr.addDateTime("createtime", MyTools.getTimeStr());
			DBHelper.logInsert(LogTbName.TAB_ACCESS_SERVER_EXC_LOG(), sqlStr);
			e.printStackTrace();
			rv = new ReturnValue(false, e.toString());
		}
		return this;
	}
	
	/**
	 * ���ؽ�����
	 */
	public void check() throws Exception {
		if(rv!=null && !rv.success){
			BACException.throwInstance(rv.info);
		}
	}
	
	/**
	 * ����
	 */
	public static void main(String[] args) {
		NetFormSender sender = new NetFormSender("http://test1.gc73.com.cn/register/quickRegister.do");
		sender.addParameter("rUser.agentId", "091");
		sender.addParameter("rUser.userName", "h576497619m01");
		sender.addParameter("rUser.password", "h576497619m01");
		sender.addParameter("rUser.rePassword", "h576497619m01");

		sender.send();
	}
}
