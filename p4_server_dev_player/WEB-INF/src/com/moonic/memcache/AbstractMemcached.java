package com.moonic.memcache;

import java.util.Date;

import net.rubyeye.xmemcached.MemcachedClientStateListener;

public abstract class AbstractMemcached {

	private int connectPoolSize;        
	private boolean primitiveAsString;
	private int connectTimeout;
	
	/**
	 * ʵ����memcached����
	 * @param serverAddress
	 * @return
	 */
	public static AbstractMemcached getInstance(String serverAddress) {
		return new MemcachedImpl(serverAddress);
	}
	
	/**
	 * ���Memcached������
	 * @param listener
	 */
	public abstract void addStateListener(MemcachedClientStateListener listener);
	
	/**
	 * memcache��ʼ��
	 * 
	 */
	public abstract void init();
	
	/**
	 * ֹͣ
	 */
	public abstract void stop();
	
	/**
	 * ����memcache����
	 * @param key
	 * @param value
	 * @param expTime
	 */
	public abstract void set(String key, Object value, int expTime);
	
	/**
	 * ��ȡmemcached������Ϣ
	 * @param key
	 * @param opTimeout
	 * @return
	 */
	public abstract Object get(String key, int opTimeout);
	
	/**
	 * ɾ��memcached������Ϣ
	 * @param key
	 */
	public abstract void delete(String key);
	
	/**
	 * ����Memcachedֵ
	 * @param key
	 */
	public abstract void incr(String key);
	
	public abstract void add(String key, Object value, Date expiryDate);
	
	public int getConnectPoolSize() {
		return connectPoolSize;
	}

	public void setConnectPoolSize(int connectPoolSize) {
		this.connectPoolSize = connectPoolSize;
	}

	public boolean isPrimitiveAsString() {
		return primitiveAsString;
	}

	public void setPrimitiveAsString(boolean primitiveAsString) {
		this.primitiveAsString = primitiveAsString;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
}
