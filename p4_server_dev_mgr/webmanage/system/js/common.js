// ���е��ļ��е��ԶԻ����Ƿ���ʾ��=true����ʾ
var LPiRelase=true;
// ��ǰ�ļ��е��ԶԻ����Ƿ���ʾ��=true��ʾ
var LPiFiledDebug=false;

/**
 * �ڵ��Թ����е������Կ�.
 * ֻ�е�������Ŀû�з������ҵ�ǰ�ļ��е��Կ��ش�ʱ���ŵ���alert��
 */
function LPiAlert(strPrompt)
{
	if(!LPiRelase && LPiFiledDebug)
	{
		alert(strPrompt);
	}
}

function openWindow(url,wName,w,h,scrollbars,bResizable){   //�򿪲����д���
var newwindow;
if(w==null || w==0){w=400}
if(h==null || h==0){h=370}
var feature="width="+w+",height="+h;
if(scrollbars!=null){feature+=",scrollbars=yes"}
if(bResizable)
{
	feature+=",resizable=yes"
}
else
{
	feature+=",resizable=no"
}

newwindow=window.open(url,wName,feature);
newwindow.moveTo((window.screen.width-w)/2,(window.screen.height-h)/2);
return newwindow;
}

var cname_receiver=null;
var cid_receiver=null;
function chooseCustomer(nameObj,idObj)
{
	cname_receiver=nameObj;
	cid_receiver=idObj;
	openWindow("../customer/customer_list_forchoose.jsp","ѡ��ͻ�",600,400,true);
}
var pname_receiver=null;
var pid_receiver=null;
function chooseCargo(nameObj,idObj)
{
	pname_receiver=nameObj;
	pid_receiver=idObj;
	openWindow("../cargo/cargo_list_forchoose.jsp","ѡ����Ʒ",600,400,true);
}


function changechoose(selectobj,targetName,customValue,copy){   //selectѡ���ֵ���������
var selectname=selectobj.name;
var targetObj=eval("form1."+targetName);
var selectvalue=selectobj.options[selectobj.selectedIndex].value;
if(selectvalue!=customValue){
	targetObj.value=selectvalue;
	targetObj.readOnly=true;
	}else
	{
	targetObj.readOnly=false;
	if(copy==true){
	  targetObj.value=customValue
	}else{
	targetObj.value="";
	}	
	targetObj.focus();
	}
}
function customValue(obj,cTag,initValue){
if(cTag==obj.options[obj.selectedIndex].value){
	var newOption=document.createElement("option");
	if(initValue==null){
	cValue=prompt("�������Զ����ֵ","");}
	else{
	cValue=prompt("�������Զ����ֵ",initValue);
	}
	cValue=trim(cValue);
	if(cValue!=null && cValue!=""){
		if(obj.maxLen!=null && obj.maxLen!="" && cValue.length>obj.maxLen*1){
			alert("�����ֵ�ĳ��Ȳ��ܳ��� " + obj.maxLen + " ���ַ�");
			if(!isNaN(obj.defaultIndex)){
				obj.selectedIndex=obj.defaultIndex-1;
			}
			else{
			 	obj.selectedIndex=0;
			}
		}
		else{
			newOption.text=cValue;
			newOption.value=cValue;
			obj.options.add(newOption);
			obj.selectedIndex=obj.length-1;
		}
	}else{
		if(cValue==""){
			alert("����ֵ����Ϊ�գ�");
			  if(!isNaN(obj.defaultIndex)){
				obj.selectedIndex=obj.defaultIndex-1;
			  }else{
			 	obj.selectedIndex=0;
			  }
			
			}
			if(cValue==null){
			if(!isNaN(obj.defaultIndex)){
				obj.selectedIndex=obj.defaultIndex-1;
			}else{
				obj.selectedIndex=0;
			}
		}
	}
 }
}
function mmtoinch(num,decimal){   //����ת����Ӣ��
if(num==null || isNaN(num) || trim(num)==""){
	if(trim(num)!="")alert("����������");
	return "";	
	}else{
	if(decimal==null){decimal=0;}
	n=Math.pow(10,decimal);
	return Math.round((num/25.4)*n)/n;
	}
}
function inchtomm(num,decimal){   //Ӣ��ת��������
if(num==null || isNaN(num) || trim(num)==""){
	if(trim(num)!="")alert("����������");
	return "";
	}else{
	if(decimal==null){decimal=0;}
	n=Math.pow(10,decimal);
	return Math.round((num*25.4)*n)/n;
	}
}

function chooseDefault(){   //����select��defaultIndex����ָ����ѡ��
var formEle;

for(var i=0;i<document.forms.length;i++){
 formEle = document.forms[i].elements;
 
 for(var j=0;j<formEle.length;j++)
	{
	  if(formEle[j].tagName=="SELECT"){
		  if(!isNaN(formEle[j].defaultIndex)){
		    formEle[j].selectedIndex=formEle[j].defaultIndex-1;
		  }		
	  }	 
	}
 }
}
function chooseOption(selectObj,chooseValue){
var haveFound=0;
for(i=0;i<selectObj.options.length;i++){
if(selectObj.options[i].value==chooseValue){
	selectObj.selectedIndex=i;
	haveFound=1;
	}
}
if(haveFound!=1){
 newOption=document.createElement("OPTION");
 newOption.text=chooseValue;
 newOption.value=chooseValue;
 selectObj.options.add(newOption);
 selectObj.selectedIndex=selectObj.options.length-1;
}
}

//����allValue���������ֵ�Զ���дҳ���϶�Ӧ���ֵ������������˵��ȿؼ�
function autoChoose(allValue){
var eleArr = document.forms[0].elements;    // �����е�����Ԫ�ط�������
var selectValue,checkValue,inputValue,strArr;
for(var i = 0; i < eleArr.length; i++){
 if(eleArr[i].autoInput!=null && eleArr[i].autoInput=="false")continue;

//�Զ�ѡ�������˵�
 if(eleArr[i].tagName=="SELECT"){    
 	 selectValue=allValue[eleArr[i].name];
	 if(selectValue!=null){
	if(eval("document.forms[0]."+eleArr[i].name).item(0).name!=null){ //�������ֵĶ����ж��
		strArr="";
		if(selectValue!=null){
		strArr=selectValue.split("|");   //���ַ����������
		}
	
	objArr=eval("document.forms[0]."+eleArr[i].name);
	for(j=0;j<objArr.length;j++){
		if(eleArr[i]==objArr.item(j)){   //�Ƚ϶���������ÿ������͵�ǰ�����Ƿ���ͬ
		if(strArr[j]!=null){
			if(strArr[j]=="_"){
				chooseOption(eleArr[i],"");
				}else{
					chooseOption(eleArr[i],strArr[j]);				
					}
			}else
			{
				chooseOption(eleArr[i],"");	
			}
		}
	}

	}else{
		if(selectValue!=null){		
			chooseOption(eleArr[i],selectValue);		  
		}
	  }
	 }
	}

//�Զ�ѡ��checkbox��radio
 if(eleArr[i].tagName=="INPUT" && (eleArr[i].type=="checkbox" || eleArr[i].type=="radio"))
{
	checkValue=allValue[eleArr[i].name];	
	
	if(checkValue!=null && checkValue!="")
	{
		checkValueArr=checkValue.split("|");  //���ַ����������
		for(var j=0;j<checkValueArr.length;j++)
		{
			if(checkValueArr[j]==eleArr[i].value)
			{
				eleArr[i].checked=true;
				break;  //ƥ��Ļ�����ѭ��
			}			
			else
			{
				eleArr[i].checked=false;
			}			
		}
	}
	else
	{
		if(checkValue=="")eleArr[i].checked=false;
	}
	 
}

//�Զ���дtext��hidden��password
  if(eleArr[i].tagName=="INPUT" && (eleArr[i].type=="text" || eleArr[i].type=="hidden" || eleArr[i].type=="password")){
	inputValue=allValue[eleArr[i].name];  
	if(inputValue!=null){
	if((count=eval("document.forms[0]."+eleArr[i].name).length)!=null){ //�������ֵĶ����ж��	
	strArr="";
	if(inputValue!=null){
	strArr=inputValue.split("|");   //���ַ����������
	}
	
	objArr=eval("document.forms[0]."+eleArr[i].name);
	for(j=0;j<objArr.length;j++){
		if(eleArr[i]==objArr.item(j)){   //�Ƚ϶���������ÿ������͵�ǰ�����Ƿ���ͬ
		if(strArr[j]!=null){
			if(strArr[j]=="_"){
				eleArr[i].value="";
				}else{
				eleArr[i].value=strArr[j];
				}
			}else
			{
			eleArr[i].value="";
			}
		}
	}

	}else{
		if(inputValue!=null){
			if(inputValue!="_"){
			eleArr[i].value=inputValue;
			}else{
			eleArr[i].value="";
			}
			}
		}
	}
	}

  if(eleArr[i].tagName=="TEXTAREA"){
	inputValue=allValue[eleArr[i].name];
	if(inputValue!=null){
		eleArr[i].value=inputValue;
		}
	}


 } //��дһ���ؼ�����
}

function checkedIndex(obj){
if(obj.tagName=="SELECT"){
for(var i=0;i<obj.length;i++){
	if(obj[i].selected==true){
		return i;
		}
	}
return -1;
}
if(obj[0].tagName=="INPUT" && (obj[0].type=="radio" || obj[0].type=="checkbox")){
for(var i=0;i<obj.length;i++){
	if(obj[i].checked==true){
		return i;
		}
	}
 return -1;
}
}
//��ָ��С��λ����������
function dataFormat(col_value,precision){
	if(precision<0){precision=0;}
	var addvalue=5* Math.pow(10,-1*(precision+1));
	if(col_value>0){
	var value=col_value*1+addvalue;
	}else{
	var value=col_value*1-addvalue;
	}	
	var value_str=value+"";
	var pointIndex=value_str.indexOf(".");
	if(precision==0){pointIndex--;}
	var toIndex=pointIndex+precision*1+1;
	if(toIndex>value_str.length){
		toIndex=value_str.length-1;
	}
	value_str_final=value_str.substr(0,toIndex);
    

	while(value_str_final.lastIndexOf(".")==value_str_final.length-1){
	  value_str_final=value_str_final.substr(0,value_str_final.length-1);
	}
	if(value_str_final*1==0 && value_str_final.charAt(0)=="-"){
	 value_str_final=value_str_final.substr(1,value_str_final.length);
	}

	
	return value_str_final;		
}



/**����һ��HTML����obj��������ֵ��С��λ����digit(��������)  -ty*/
function setDecimal(obj, digit){
	var num;
	if (obj.value != ""){
		var pointIndex = obj.value.indexOf(".");
		if(pointIndex >= 0){
			if(obj.value.length > (pointIndex + digit + 1)){
				num = obj.value * Math.pow(10,digit);
				num = Math.round(num) / Math.pow(10,digit);
				obj.value = num;
			}
		}
		else
			//���������,���С����
			obj.value = obj.value + ".0";
	}
}


/** 
 * �жϲ���myV�Ƿ���������obj������ֵ���У������������,������ʾ������ͬ��ֵ,
 * ���myText����Ϊ�գ���text��value��ͬ��
 *
 * @author: ty
 * @ver 1.0
 */
function insertValue(obj, myV, myText){
	var index = -1;
	var stat = true;
	
	for(i=0; i<obj.length; i++){
		if(obj.options[i].value==myV){
			index = i;
			stat = false;
			break;
		}
		else
			continue;
	}
	if(stat==true){
		var newOption=document.createElement("option");
		if(myText != null) newOption.text = myText;
		else newOption.text = myV;
		newOption.value=myV;
		obj.options.add(newOption);
		obj.selectedIndex=obj.length-1;
	}
	else{		
		if(index>0)
			obj.selectedIndex=index;
	}
}

/**ȥ���ַ������˵Ŀո�   -ty*/
function trim(objValue){
	if(objValue!="" && objValue!=null){
		while((objValue.indexOf(" ")==0 || objValue.indexOf("��")==0 || objValue.lastIndexOf(" ")==objValue.length-1 || objValue.lastIndexOf("��")==objValue.length-1) && objValue.length>0){
			if(objValue.indexOf(" ")==0)
				objValue = objValue.substr(1);
			if(objValue.indexOf("��")==0)
				objValue = objValue.substr(1);
			if(objValue.lastIndexOf(" ")==objValue.length-1)
				objValue = objValue.substr(0,objValue.length-1);
			if(objValue.lastIndexOf("��")==objValue.length-1)
				objValue = objValue.substr(0,objValue.length-1);
		}
	}
	return objValue;
}
function showError(errorValue){	
errorName=errorValue.name.split("|");
errorOrder=errorValue.order.split("|");
errorTip=errorValue.tip.split("|");
errorValue=errorValue.value.split("|");

theForm=document.forms[0];

for(var i=0;i<errorName.length;i++){
 formObj=eval("theForm."+errorName[i]);
 if(errorOrder[i]!="-1"){    //���ͬ���ؼ������
	if(formObj[errorOrder[i]].tagName=="INPUT" && formObj[errorOrder[i]].type=="text"){
		formObj[errorOrder[i]].style.border="1px #FF0000 solid";
		if(errorTip[i]!="_"){
			formObj[errorOrder[i]].title=errorTip[i];
			}
		if(errorValue[i]!="_"){
			formObj[errorOrder[i]].value=errorValue[i];
			}
		}	
	if(formObj[errorOrder[i]].tagName=="INPUT" && (formObj[errorOrder[i]].type=="radio" || formObj[errorOrder[i]].type=="checkbox")){
		for(var j=0;j<formObj.length;j++){
			formObj[j].style.border="1px #FF0000 solid";
			formObj[j].title=errorTip[i];
			}
		}
	if(formObj[errorOrder[i]].tagName=="SELECT"){
		formObj[errorOrder[i]].style.border="1px #FF0000 solid";
		formObj[errorOrder[i]].title=errorTip[i];		
		}
 }else{
	if(formObj.tagName=="INPUT" && formObj.type=="text"){
		formObj.style.border="1px #FF0000 solid";
		if(errorTip[i]!="_"){
			formObj.title=errorTip[i];
			}
		if(errorValue[i]!="_"){
			formObj.value=errorValue[i];
			}
		}
	if(formObj.tagName=="INPUT" && (formObj.type=="radio" || formObj.type=="checkbox")){
		formObj.style.border="1px #FF0000 solid";
		formObj.title=errorTip[i];		
		}
	if(formObj.tagName=="SELECT"){
		formObj.style.border="1px #FF0000 solid";
		formObj.title=errorTip[i];		
		}
	}
 }
}

function isDate(dateValue){
var tian=new Array();
var splitStr=null;
if(dateValue.indexOf("-")!=-1){
	splitStr="-";
}else if(dateValue.indexOf("/")!=-1){
	splitStr="/";
}else if(dateValue.indexOf(".")!=-1){
	splitStr=".";
}else{
return false;
}
var nianIndex=dateValue.indexOf(splitStr);
var nianStr=dateValue.substring(0,nianIndex);
if(nianStr.length!=4 || isNaN(nianStr)){return false;}
var yueIndex=dateValue.indexOf(splitStr,nianIndex+1);
var yueStr=dateValue.substring(nianIndex+1,yueIndex);
if((yueStr.length!=1 && yueStr.length!=2) || isNaN(yueStr)){return false;}

var dayStr=dateValue.substring(yueIndex+1,dateValue.length);
if((dayStr.length!=1 && dayStr.length!=2) || isNaN(dayStr)){return false;}

var nian=nianStr*1;
var yue=yueStr*1;
var day=dayStr*1;
//alert("��="+nian+"\r\n"+"��="+"\r\n"+yue+"��="+"\r\n"+day);
if(nian % 100==0){
 if(nian % 400==0){
  tian[2]=29;
  niantag="����";}
 else
  {tian[2]=28;
  niantag="ƽ��";}
 }else{
 if(nian % 4==0){
  tian[2]=29;
  niantag="����";}
 else
  {tian[2]=28;
  niantag="ƽ��";}
 }
tian[1]=31;
tian[3]=31;
tian[4]=30;
tian[5]=31;
tian[6]=30;
tian[7]=31;
tian[8]=31;
tian[9]=30;
tian[10]=31;
tian[11]=30;
tian[12]=31;
if(nian<1){return false;}
if(yue>12 || yue<1){return false;}
if(day>tian[yue] || day<1){return false;}
return true;
}

//ʹ��ǰҳ��Ĺ���ɵȴ�״̬
function wait(){
for(var i=0;i<document.all.length;i++){		
		document.all[i].org_cursor=document.all[i].style.cursor;
		document.all[i].style.cursor="wait";
	}
}

//ʹָ�����ڶ���ҳ��Ĺ��ظ��ȴ�ǰ��״̬
function wait_end(obj){
for(var i=0;i<obj.document.all.length;i++){
	if(obj.document.all[i].org_cursor!=null){
		obj.document.all[i].style.cursor=obj.document.all[i].org_cursor;
		}		
	}
}

// ����bShow���������Ƿ���ʾ
function showIt(theObj, bShow)
{
	if(theObj == null || theObj.style == null || bShow == null) return;
	if(bShow)
	{
		theObj.style.display = "";
	}
	else
	{
		theObj.style.display = "none";
	}
}

//���ָ�����ַ����Ƿ���ָ���������б���
function textInSelect(selObj,textValue)
{	
	for(var i=0;selObj!=null && i<selObj.length;i++)
	{
		if(selObj.options[i].text==textValue)return true;
	}
	return false;
}

//���ָ����ֵ�Ƿ���ָ���������б���
function valueInSelect(selObj,value)
{	
	for(var i=0;selObj!=null && i<selObj.length;i++)
	{
		if(selObj.options[i].value==value)return true;
	}
	return false;
}

//��ָ�����ģ��������ݲ���һ��
//opTbl:Ҫ�����еı�����
//modelTbl:ģ����
//index:Ҫ�����λ�ã�-1��ʾ��ӵ����λ��
function insertRow(opTbl,modelTbl,index)
{
	var rowObj;
	var rowAlign;
	var cellHtml;
	var cellClass;
	var cellAlign;
	var cellNoWarp;
	var cellObj;
	for(var i=0;modelTbl!=null && i<modelTbl.rows.length;i++)
	{
		if(index!=null && index>=0)
		{
			rowObj=opTbl.insertRow(index);
			
		}else
		{
			rowObj=opTbl.insertRow();
		}
		rowObj.className=modelTbl.rows[i].className;
		rowObj.align=modelTbl.rows[i].align;
		for(var j=0;modelTbl.rows[i].cells!=null && j<modelTbl.rows[i].cells.length;j++)
		{			
			cellHtml=modelTbl.rows[i].cells[j].innerHTML;
			cellClass=modelTbl.rows[i].cells[j].className;
			cellAlign=modelTbl.rows[i].cells[j].align;
			cellNoWarp=modelTbl.rows[i].cells[j].noWarp;
			cellObj=rowObj.insertCell();
			cellObj.align=cellAlign;
			cellObj.className=cellClass;
			cellObj.noWarp=cellNoWarp;
			cellObj.innerHTML=cellHtml;
		}		
	}
}

//ɾ��ָ������һ��
//opTbl:Ҫ�����ı�����
//index:Ҫɾ������λ��,-1��ʾɾ���һ��
function deleteRow(opTbl,index)
{
	if(opTbl==null)return;
	if(index==-1)
	{
		if(opTbl.rows.length>1){opTbl.deleteRow(opTbl.rows.length-1);}
	}
	else
	{
		opTbl.deleteRow(index);
	}
}

//�滻����Ԫ�ص�valueֵ������replaceAll(theForm,""," ")���Խ��java��set��ֵ������
function replaceAll(theForm,compareStr,desStr)
{
	var eleArr = theForm.elements;
	for(var i=0;eleArr!=null && i<eleArr.length;i++)
	{
		if(eleArr[i].tagName=="INPUT" && eleArr[i].type=="text")
		{
			if(eleArr[i].value==compareStr)
			{
				eleArr[i].value=desStr;
			}
		}
		if(eleArr[i].tagName=="TEXTAREA")
		{
			if(eleArr[i].value==compareStr)
			{
				eleArr[i].value=desStr;
			}
		}
	}
}

//�Զ�����ҳ��������չ���ڴ�С
function autoResize()
{
window.resizeTo(document.body.scrollWidth+40,document.body.scrollHeight+30);
}
