var PatternsDict = new Object();

PatternsDict.L=/^\D{1}$/;   //ƥ��L
PatternsDict.B_Z=/^[b-zB-Z]$/;   //ƥ��L
PatternsDict.Sn=/^(\W|\w|\s)+$/;   //ƥ�����ɸ��ǿհ��ַ�
PatternsDict.Dn=/^\d+$/;   //ƥ�����ɸ�D
PatternsDict.LDD=/^\D\d{2}$/;   //ƥ��LDD
PatternsDict.Num=/(^\d+\.\d+$)|(^\d*$)/;   //ƥ������
PatternsDict.numeric =/(^\d+\.\d+$)|(^\d*$)|(^-(\d+\.\d+$))|(^-(\d*$))/;   //ƥ����ֵ
PatternsDict.P0Int=/^[0-9]\d*$/;   //ƥ��Ǹ�����
PatternsDict.PInt=/^[1-9]\d*$/;   //ƥ��������
PatternsDict.PInt_4=/^[1-9]\d{3}$/;   //ƥ��4λ������
PatternsDict.Dnz=/^[1-9]\d*$/;

PatternsDict.L2D6L = /^\D{2}\d{6}\D$/;	// ƥ��LLDDDDDDL, added by whj, for mi_cover
PatternsDict.L2D6LD = /^\D{2}\d{6}\D\d$/;	// ƥ��LLDDDDDDLD, ����������䣬 added by whj, for mi_cover
PatternsDict.L2D6L2 = /^\D{2}\d{6}\D{2}$/;	// ƥ��LLDDDDDDLL, ����������䣬 added by whj, for ����ָʾ���
PatternsDict.L02D6LD = /^\D{0,2}\d{6}\D\d$/;	// ƥ��LLDDDDDDLD, ��������Բ��䣬 added by whj, for ����ָʾ��Ų�ѯ

PatternsDict.D1_1=/(^\d{1}\.\d{1}$)|(^\d{1}$)/;  //ƥ��D.D
PatternsDict.D1_2=/(^\d{1}\.(\d{1}|\d{2})$)|(^\d{1}$)/;  //ƥ��D.DD
PatternsDict.D1_3=/(^\d{1}\.(\d{1}|\d{2}|\d{3})$)|(^\d{1}$)/;  //ƥ��D.DDD

PatternsDict.D2_1=/(^(\d{1}|\d{2})\.\d{1}$)|(^(\d{1}|\d{2})$)/;   //ƥ��DD.D
PatternsDict.D2_2=/(^(\d{1}|\d{2})\.(\d{1}|\d{2})$)|(^(\d{1}|\d{2})$)/;	  //ƥ��DD.DD
PatternsDict.D2_3=/(^(\d{1}|\d{2})\.(\d{1}|\d{2}|\d{3})$)|(^(\d{1}|\d{2})$)/;  //ƥ��DD.DDD

PatternsDict.D3_1=/(^(\d{1}|\d{2}|\d{3})\.\d{1}$)|(^(\d{1}|\d{2}|\d{3})$)/;	//ƥ��DDD.D
PatternsDict.D3_2=/(^(\d{1}|\d{2}|\d{3})\.(\d{1}|\d{2})$)|(^(\d{1}|\d{2}|\d{3})$)/;   //ƥ��DDD.DD
PatternsDict.D3_3=/(^(\d{1}|\d{2}|\d{3})\.(\d{1}|\d{2}|\d{3})$)|(^(\d{1}|\d{2}|\d{3})$)/;	//ƥ��DDD.DDD

PatternsDict.D2=/(^(\d{2})$)/;   //ƥ��DD
PatternsDict.D3=/(^(\d{3})$)/;   //ƥ��DDD
PatternsDict.D4_1=/(^(\d{1}|\d{2}|\d{3}|\d{4})\.\d{1}$)|(^(\d{1}|\d{2}|\d{3}|\d{4})$)/;   //ƥ��DDDD.D
PatternsDict.D4=/(^(\d{4})$)/;   //ƥ��DDDD

PatternsDict.D1=/^\d{1}$/;	//ƥ��D
PatternsDict.D1_D2=/^(\d{1}|\d{2})$/;//ƥ��DD
PatternsDict.D1_D3=/^(\d{1}|\d{2}|\d{3})$/;	//ƥ��DDD
PatternsDict.D1_D4=/^(\d{1}|\d{2}|\d{3}|\d{4})$/;   //ƥ��D��DDDD
PatternsDict.D1_D5=/^(\d{1}|\d{2}|\d{3}|\d{4}|\d{5})$/;   //ƥ��D��DDDDD

PatternsDict.fD3_2=/(^-(\d{1}|\d{2}|\d{3})\.(\d{1}|\d{2})$)|(^-(\d{1}|\d{2}|\d{3})$)/;   //ƥ��-DDD.DD
PatternsDict.fD3_4=/(^-(\d{1}|\d{2}|\d{3})\.(\d{1}|\d{2}|\d{3}|\d{4})$)|(^-(\d{1}|\d{2}|\d{3})$)/;   //ƥ��-DDD.DDDD

PatternsDict.C08=/^(\w|\W){0,8}$/;	//ƥ�����8��C, added by whj, for mi_cover
PatternsDict.C02=/^\w{0,2}$/;		//ƥ�����2��C, added by whj, for mi_cover
PatternsDict.email=/^[_a-zA-Z0-9]+@([_a-zA-Z0-9]+\.)+[a-zA-Z0-9]{2,3}$/

PatternsDict.C1=/^\w{1}$/;		//ƥ��C
PatternsDict.C1_C4=/^\w{1,4}$/;		//ƥ��C��CCCC
PatternsDict.C2=/^\w{2}$/;		//ƥ��CC
PatternsDict.C9=/^\w{9}$/;		//ƥ��CCCCCCCCC
PatternsDict.C1_C12=/^(\w|\W){1,12}$/;		//ƥ��C��CCCCCCCCCCCC
PatternsDict.LD=/^\D\d{1}$/;	//ƥ��LD
PatternsDict.D6 = /^\d{6}/;	// ƥ��D6

PatternsDict.T10=/^(\W|\w|\s){1,10}$/;   //ƥ��10���ǿհ��ַ�
PatternsDict.T12=/^(\W{1,12}|\w{1,12}|\s{1,12})$/;   //ƥ��12���ǿհ��ַ�
PatternsDict.DateTime=/^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)\s+([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$/