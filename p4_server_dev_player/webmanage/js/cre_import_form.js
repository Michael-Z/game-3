
	//version: 1.0
	//Time   : 2002-11-19
	//Author : zjq
	//Object : ��������ҳ��
	//Usages :  
	//       obj         : ���ñ������Ķ���
	//       iNoUseLine  : �ڱ�Table�в�����ѭ���ļ�¼��
	//       sFile       : �ļ���(�������ʸ�ҳ��ȫ·��,ip��ַ�Ͷ˿ںŲ�д)
	//       sTitle      : ��ʾ�Ի���ı���
	//       iWidth      : ��ʾ�Ի���Ŀ��
	//       iHeight     : ��ʾ�Ի���ĸ߶�
	//       sControl    : ��ʾ�Ի����Ƿ���Ҫ������,yes:��Ҫ no:����Ҫ
	//       sFormName   : ��������Ҫ������Form��Name����
	//       sInputs     : ��������Ҫ������Form�ķ���INPUT ����,����ж���ֶ�,����","�ָ�
	//       iMultiSelect: ��ʾ���ڵ�Ԫ���ܷ��ѡ,����������,1:���Զ�ѡ(ѡ����Ԫ�صķ���ֵ�ö��ŷָ�),��1:���ܶ�ѡ
	//       chSplit     : ѡ����Ԫ�صķ���ֵ�ķָ�����,����Ϊ���ַ����߶���ַ�,example:",","","|||",etc.
	//       sParam      : ������������,����ж��ֵ,�ö��ŷָ�,string
	//       sDefault    : ��ѡ����ĳ�ʼֵ,string
	//       sDefaultSplit    : ��ѡ����ĳ�ʼֵ֮��ķָ�����,string
	//       iList       : �Ƿ�Ҫ��������Ѿ�ѡ���ѡ��ֵ�����г���,������int,1=��Ҫ�г���,��1=����Ҫ�г���
	//       sHasSelected: �Ѿ�ѡ���ѡ��ֵ,string
	//       sSelectedSplit: �Ѿ�ѡ���ѡ��ֵ֮��ķָ�����,string
	//       iMaxNum     : ���ѡ�������,������int,���Ϊ0,��û������
	//       sIndexValue : ������������Form�еļ�¼������±�,string����,���뵱iNoUseLine=-1ʱ����Ч
	function createImportForm(obj,iNoUseLine,sFile,sTitle,iWidth,iHeight,sControl,sFormName,sInputs,iMultiSelect,chSplit,sParam,sDefault,sDefaultSplit,iList,sHasSelected,sSelectedSplit,iMaxNum,sIndexValue)
	{
		if(obj!=null)
		{
   		var sindex="";
   		if(iNoUseLine==-1)
   		{
   		   sindex = sIndexValue;
   		}else
   		{
      		var rowCount = obj.parentElement.parentElement.parentElement.parentElement.rows.length-iNoUseLine;
      		var rowNum=obj.parentElement.parentElement.rowIndex-1;
      		if(rowCount>1){sindex = "[" + rowNum + "]";}	
   	   }
		   ModalDialog(sFile + '?imaxnum='+ iMaxNum +'&sssplit='+ sSelectedSplit +'&sselected='+ sHasSelected +'&ilist='+ iList +'&sdsplit='+sDefaultSplit+'&sdefault='+ sDefault +'&param='+ sParam +'&sindex='+ sindex +'&split='+ chSplit +'&multiselect='+ iMultiSelect +'&formname='+sFormName+'&inputs='+sInputs,sTitle,iWidth,iHeight,sControl);
		}
		else
		{alert("������Ϊ�գ�");}
	}




	//version: 1.0
	//Time   : 2002-11-19
	//Author : zjq
	//Object : ������ͨģ̬ҳ��
	//Usages :  
	//       sFile       : �ļ���(�������ʸ�ҳ��ȫ·��,ip��ַ�Ͷ˿ںŲ�д)
	//       sTitle      : ��ʾ�Ի���ı���
	//       iWidth      : ��ʾ�Ի���Ŀ��
	//       iHeight     : ��ʾ�Ի���ĸ߶�
	//       sControl    : ��ʾ�Ի����Ƿ���Ҫ������,yes:��Ҫ no:����Ҫ
	//       sParam      : ������������,����ж��ֵ,�ö��ŷָ�,string
	
	
	function createNormalForm(sFile,sTitle,iWidth,iHeight,sControl,sParam)
	{
      ModalDialog(sFile + '?param=' + sParam,sTitle,iWidth,iHeight,sControl);
	}

	//version: 1.0
	//Time   : 2002-11-19
	//Author : zjq
	//Object : ������ͨģ̬ҳ��
	//Usages :  
	//       sFile       : �ļ���(�������ʸ�ҳ��ȫ·��,ip��ַ�Ͷ˿ںŲ�д)
	//       sTitle      : ��ʾ�Ի���ı���
	//       iWidth      : ��ʾ�Ի���Ŀ��
	//       iHeight     : ��ʾ�Ի���ĸ߶�
	//       sControl    : ��ʾ�Ի����Ƿ���Ҫ������,yes:��Ҫ no:����Ҫ
	//       sParam      : �������������������ļ���֮������в���
	
	
	function createModelForm(sFile,sTitle,iWidth,iHeight,sControl,sParam)
	{
      ModalDialog(sFile + sParam,sTitle,iWidth,iHeight,sControl);
	}	

	//version: 1.0
	//Time   : 2002-11-19	
	//Author : zjq
	//Object : ��������ҳ��
	//Usages :  
	function createImportFormNoInput(sFile,sTitle,iWidth,iHeight,sControl)
	{
		ModalDialog(sFile,sTitle,iWidth,iHeight,sControl);
	}


	//version: 1.0
	//Time   : 2002-11-19
	//Author : zjq
	//Object : �жϴ���Ķ����Ƿ����ɶ����validatorָ��������
	//Usages :  
	//       obj      : �ȴ��жϵĶ���
	//Return :
	//       true     : ��
	//       false    : ��
	function isType(obj)
	{
       var thePat=null;
       var v = obj.validator;    
       if(v!=null)
       {
         thePat = PatternsDict[v];
       }
       var gotIt = thePat.exec(obj.value); 
       if(gotIt){
         return (true);
       }else{
         return (false);
       }	
		
	}	
	