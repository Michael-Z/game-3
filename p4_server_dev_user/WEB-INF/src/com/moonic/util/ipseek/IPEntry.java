package com.moonic.util.ipseek;
//һ��IP��Χ��¼�������������Һ�����Ҳ������ʼIP�ͽ���IP
public class IPEntry {
	public String beginIp;
	public String endIp;
	public String country;
	public String area;
	public IPEntry() {
		beginIp = "";
		endIp = "";
		country = "";
		area = "";
	}

	@Override
	public String toString() {
		return this.area + "  " + this.country + "  IP��Χ:" + this.beginIp + "-"
				+ this.endIp;
	}
}
