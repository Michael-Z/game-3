//����ȫ�ֱ���
 //����ÿ���µ���Ӧ����
 var G_daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31,30, 31, 30, 31);
 //����������ѡ���б��ֵ
 var G_months = new Array("һ����","������","������","�ġ���","�塡��","������","�ߡ���","�ˡ���","�š���","ʮ����","ʮһ��","ʮ����")
 //��������titleӢ�����ڵ���ʾֵ
 var G_en_week= new Array("Su","Mo","Tu","We","Th","Fr","Sa");
 //��������title�������ڵ���ʾֵ
 var G_cn_week= new Array("��","һ","��","��","��","��","��");
 //����ȫ������������
 var G_Calendar = null;


//ʵ�����������Ա��������������Ĺ���
calendar.prototype.construct=construct;
calendar.prototype.initialCalendar=initialCalendar;
calendar.prototype.updateSelectedDay=updateSelectedDay;
calendar.prototype.getToday=getToday;
calendar.prototype.changeYear=changeYear;
calendar.prototype.refresh=refresh;
calendar.prototype.inchYear=inchYear;
calendar.prototype.selectMonth=selectMonth;
calendar.prototype.setDay=setDay;
calendar.prototype.makeDaysGrid=makeDaysGrid;
calendar.prototype.show=show;
calendar.prototype.hide=hide;
calendar.prototype.setTitleBgColor=setTitleBgColor;
calendar.prototype.setTitleTxtColor=setTitleTxtColor;
calendar.prototype.setBodyBgColor=setBodyBgColor;
calendar.prototype.setBodyTxtColor=setBodyTxtColor;
calendar.prototype.setSelectedDayBgColor=setSelectedDayBgColor;
calendar.prototype.setSelectedDayTxtColor=setSelectedDayTxtColor;
calendar.prototype.setBodyBgFile=setBodyBgFile;
calendar.prototype.setHeadBgColor=setHeadBgColor;
calendar.prototype.setLanguage=setLanguage;
calendar.prototype.analyseDate=analyseDate;
calendar.prototype.outputBind=outputBind;
calendar.prototype.setTxtSize=setTxtSize;
calendar.prototype.makeHeadGrid=makeHeadGrid;
calendar.prototype.makeTitleGrid=makeTitleGrid;
calendar.prototype.makeBodyGrid=makeBodyGrid;
calendar.prototype.refreshbody=refreshbody;
calendar.prototype.setOutType=setOutType;


//��������������
function calendar()
{
//������������
   //�ѵ�ǰ���������浽ȫ�ֱ�����
    G_Calendar=this;
   //������������Ĭ������ֵ
    this.constructed=false;
    this.stitlebgcolor ='\"#CCCCCC\"';
    this.sframecolor ='\"#000000\"';
    this.stitletxtcolor ='\"#000000\"';
    this.sheadbgcolor='\"CCCCCC\"'
    this.sbodytxtcolor ='\"#333333\"';
    this.sbodybgcolor='\"#FFFFFF\"';
    this.sSelecteddaytxtcolor ='\"#000000\"';
    this.sSelecteddaybgcolor ='\"#003366\"';
    this.slanguage='chinese';
    this.sbodybgfile='';
    this.outtype=0;
    this.TxtSize=1;
}

//���������Ա����
function construct(s_divname,s_boxname,arrhidedivs)
{  
//�������캯��
	//�ж��Ƿ��й����˵��������������ظ����� 
     if (this.constructed==false)
        this.constructed=true;
     else
        this.hide();
       //�������ص�����<div>����������д���
      if (arguments.length!=3 && arguments.length!=2)
    {
      alert("create calendar object error!");
      return false; 
    }
     if (arguments.length==3)
    this.arrhidediv=arrhidedivs;
     else
    this.arrhidediv=null;
      //��ʼ������
    this.initialCalendar(s_divname,s_boxname);
      //�����¹��������
    this.hide();
}

function initialCalendar(eltName,formElt)
{
//������ʼ������
	//��<div>����������<form>_<input>����ı���󶨵�ȫ������
 	//var x = formElt.indexOf('.');
  	//var formName = formElt.substring(0,x);
  	//var formEltName = formElt.substring(x+1);
  	this.outbox=formElt;  //document.forms[formName].elements[formEltName];
  	this.handle=document.all[eltName];
        this.handlename=eltName; 
  	this.styleOfHandleOfDiv=document.all[eltName].style;
}

function hide()
{
//��������
	  //��ʾ�����ص�����<div>������������
   if (this.arrhidediv!=null)
  {
   for(var i=0;i<this.arrhidediv.length;i++)
   {
    var theName = this.arrhidediv[i];
    var theElt = document.all[theName].style 
    if(theElt == null) break;
    theElt.visibility = 'visible';
   }
  }
   this.styleOfHandleOfDiv.visibility = 'hidden'; 
   
}


function show() 
{
//��ʾ����
  //������ı����н�������
  this.analyseDate();
    //�����������������Ϣ
     this.updateSelectedDay();
  //�ѵ�ǰ(��ȷ)�����ڰ󶨵�����ı���    
  this.outputBind();
  //ˢ������
  this.refresh();
  //���������ص�����<div>������ʾ����
  if (this.arrhidediv!=null)
  {
   for(var i=0;i<this.arrhidediv.length;i++)
   {
    var theName = this.arrhidediv[i];
    var theElt = document.all[theName].style 
    if(theElt == null) break;
    theElt.visibility = 'hidden';
   }
  }
   this.styleOfHandleOfDiv.visibility = 'visible'; 
}

function refresh()
{
//ˢ������
	//���������ַ���
	var daysGrid = this.makeDaysGrid();
	//�Ѹ��ַ�����������������innerHTML����
	this.handle.innerHTML=daysGrid;
	
}



function analyseDate()
{ 
//�����ı����е�����  
     //��������ı��ĸ�ʽ�������ı����е�����
     switch(this.outtype){
      case 0:
    var x = this.outbox.value.indexOf('\-');
	var year = this.outbox.value.substring(0,x);
	var substr = this.outbox.value.substring(x+1);
	x = substr.indexOf('\-');
   	var month = substr.substring(0,x);
    var day = substr.substring(x+1);
       break;
      default :
        var x = this.outbox.value.indexOf('\/');
	var month = this.outbox.value.substring(0,x);
	var substr = this.outbox.value.substring(x+1);
	x = substr.indexOf('\/');
   	var day = substr.substring(0,x);
        var year = substr.substring(x+1);
    }
        month=parseInt(month);
        month=month - 1;                       
        day=parseInt(day);
        year=parseInt(year); 
    //������ȷ��ѽ���ֵ����Ϊ������ǰ���ڣ�����ȡ����Ϊ������ǰ����    
   if ( month<=11 && month>=0 
      &&  day<=31 && day>=1 
      && year<2100 && year>1900)
   {  
     this.selectedDay=day;
     this.selectedMonth=month;
     this.selectedYear=year;
   }
   else
   {
     this.getToday();
   }
  
    return true;
}

function outputBind()
{
//��ѡ������ڵ�����ı���
   //���������ʽ����ѡ������ڵ�����ı���
   switch(this.outtype)
   {
   	case 0:
        this.outbox.value = this.selectedYear + '\-' + (this.selectedMonth+1) + '\-'  +  this.selectedDay;
        break;
        default:
        this.outbox.value = (this.selectedMonth+1) + '\/' + this.selectedDay + '\/'  +  this.selectedYear;
		
   }
}

function getToday()
{
//��ȡ���������
   //��ȡ���������(�꣬�£���)
   var now = new Date();
   this.selectedYear = now.getFullYear();
   this.selectedMonth = now.getMonth();
   this.selectedDay = now.getDate();
}

function updateSelectedDay()
{
// �����������������Ϣ
     // ���ѡ��������·�Ϊ���·ݣ��ж��Ƿ�Ϊ���꣬������Ӧ�ĸ����µ�������29��28�����Ƕ��·���������еõ����µ���Ӧ����
     if (this.selectedMonth == 1)
     {
	 this.daysOfSelectedMonth=((0 == this.selectedYear % 4) && (0 != (this.selectedYear % 100))) ||
            (0 == this.selectedYear % 400) ? 29 : 28;
	  }
     else
	 {
       this.daysOfSelectedMonth=G_daysInMonth[this.selectedMonth];
	  }
    // �ж�ѡ�е��գ�ע���ꡢ�¡����е��գ��Ƿ���ڸ��µ����������򽫸��µ���������ѡ�е���    
	if (this.selectedDay>this.daysOfSelectedMonth)
	   {
	    this.selectedDay=this.daysOfSelectedMonth;
	   }
	//��ȡ����1����һ���ڵĵڼ���  
	 var DayOfFirstMonth= new Date(this.selectedYear,this.selectedMonth,1);
	 var starDay=DayOfFirstMonth.getDay();
	 
	 this.selectedDayOfMonthOfFirstSunday=7-starDay+1;
}

function changeYear(year) 
{
//��ı��ˢ������
   //���õ�ǰ��
   this.selectedYear=parseInt(year);
   //�󶨵�������������������Ϣ����ˢ������
   this.outputBind();
   this.updateSelectedDay();
   this.refresh();
}

function inchYear(delta)
{
//�������ˢ������
   //�����µ����
    this.selectedYear =this.selectedYear + delta;
    //�󶨵�������������������Ϣ����ˢ������
    this.outputBind();
    this.updateSelectedDay();	
    this.refresh();
}

function selectMonth(cdmonth)
{
//�·ݵ�����ˢ������
   //�����µ��·�
   this.selectedMonth=parseInt(cdmonth);
   //�󶨵�������������������Ϣ����ˢ������
   this.outputBind();
   this.updateSelectedDay();	
   this.refresh();
}

function setDay(day) 
{
//����ѡ�е��գ����󶨵����
   //����ѡ�е��գ����󶨵����
   this.selectedDay=day;
   this.outputBind();
   
      //�������������Ϣ��ˢ������
      //this.updateSelectedDay();
      this.refreshbody();
      //this.refresh();
   //this.hide();
   self.close();
}

	
function setTitleBgColor(color)
{  
//����title������ɫ
   //����title������ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.stitlebgcolor= '\"'+ color + '\"';
   	 return true;
	 }
  
   return false ;
}

function setBodyBgColor(color)
{  
//����body������ɫ
	//����body������ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.sbodybgcolor= '\"'+ color + '\"';
   	 return true;
    }
  
   return false ;
}

function setTitleTxtColor(color)
{ 
//����titli�ı���ɫ 
	//����titli�ı���ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.stitletxtcolor= '\"'+ color + '\"';
   	 return true;
    }
  
   return false ;
}

function setBodyTxtColor(color)
{  
//����body�ı���ɫ
	//����body�ı���ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.sbodytxtcolor= '\"'+ color + '\"';
   	 return true;
	 }
  
   return false ;
}

function setSelectedDayBgColor(color)
{
//���õ�ǰ�յı�����ɫ  
	//���õ�ǰ�յı�����ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.sSelecteddaybgcolor= '\"'+ color + '\"';
   	 return true;
	 }
  
   return false ;
}

function setSelectedDayTxtColor(color)
{
//���õ�ǰ�յ��ı���ɫ 
	//���õ�ǰ�յ��ı���ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.sSelecteddaytxtcolor= '\"'+ color + '\"';
   	 return true;
	 }
  
   return false ;
}

function setHeadBgColor(color)
{
//����head�ı�����ɫ
	//����head�ı�����ɫ
   if (color.length==7 && color.substring(0,1)=='#')
    {
	 this.sheadbgcolor= '\"'+ color + '\"';
   	 return true;
	 }
  
   return false ;
}

function setTxtSize(size)
{
//���������ı��������С
	//���������ı��������С
   if (size>=1 && size<=10)
   { 
     this.TxtSize=size;
     return true;
   }
   return false;
}
   

function setBodyBgFile(file)
{ 
//���ñ���ͼƬ�ļ� 
	//���ñ���ͼƬ�ļ�
    this.sbodybgfile= file;
}

function setOutType(type)
{
//����������ڵĸ�ʽ  
	//����������ڵĸ�ʽ
   if (type==1)
    this.outtype = 1;
   else
    this.outtype = 0;
}


function setLanguage(language)
{
//����title��������ʾ��Ӣ����ʾ
	//����title��������ʾ��Ӣ����ʾ
    if (language=='english'||language=='e'||language=='E'||language=='ENGLISH'||language=='English')
     {  this.slanguage='english'; }
	else
	 {  this.slanguage='chinese'; }
}

function makeHeadGrid()
{
 
   var daysGrid;

 //��������head��<table>���󣬲���������head�ı�����ɫ
    daysGrid='<table><tr>';
    //�����·�ѡ���б�
    daysGrid=daysGrid + '<td>';
     var monthString='<select name="monthselect"   size="1" onchange=" ' + 'G_Calendar.selectMonth(this.options[this.selectedIndex].value)">';
	 for (var i=0;i<12;++i)
	 {
	 //�����������·ݵĵ�ǰֵ���������·�ѡ���б��ѡ��ֵ
	    if  (i==this.selectedMonth)
	    { monthString=monthString + '<option value=' + i + ' Selected> ' + G_months[i] + '</option>';}
	    else 
	    { monthString=monthString + '<option value=' + i + '  > ' + G_months[i] + '</option>';}
	 }
        monthString=monthString + '</Selected>'
	daysGrid=daysGrid + monthString;
	daysGrid=daysGrid + '</td>'
	
	//��������΢����ť
	  daysGrid=daysGrid + '<td height="28" width="16"> '
    daysGrid=daysGrid + '<img src="../images/btn_yearsub.gif" style="cursor:hand"  align="absmiddle" alt="��������" onclick=\'' + 'G_Calendar.inchYear(' +  -1  + ')\'>';
    daysGrid=daysGrid + '</td>'

    //��������ʾ�����
    daysGrid=daysGrid + '<td height="28" width="58"> '
    daysGrid=daysGrid + '<input type="text" name="yearinput" maxlength="4" size="8" value=' + this.selectedYear + ' onchange="' + 'G_Calendar.changeYear(this.value)">'
    daysGrid=daysGrid + '</td>'

    //��������΢����ť
    daysGrid=daysGrid + ' <td height="16" width="9"> '
    daysGrid=daysGrid + ' <img src="../images/btn_yearadd.gif" style="cursor:hand" class="hand" align="absmiddle" alt="��������" onclick=\'' + 'G_Calendar.inchYear( ' + 1  + ') \' >';
    daysGrid=daysGrid + ' </td>'

    //�����������ذ�ť
    daysGrid=daysGrid + ' <td height="16" width="9"><img onclick =\'' + 'self.close()\' src="../images/x.gif" style="cursor:hand" alt="�ر�" align="absmiddle" class="hand" alt="�ر�">'
    daysGrid=daysGrid + ' </td></tr></table>' 
   
    return daysGrid;	
}

function makeTitleGrid()
{
   /////////////////////
     //��������title��<table>���󣬲���������title�ı�����ɫ
   
   var myDaysGird = '';
   
    myDaysGird=myDaysGird +   '<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%"><tr bgcolor=' + this.stitlebgcolor + '>';
    //��������������ֵ��������������title��Ӣ������title
   var weekString='';
    if (this.slanguage=='chinese')
	{
	  for (var i=0;i<7;++i)
	  {
	   weekString=weekString + '<td height="21">';
           weekString=weekString + '<div align="center"><font color='+ this.stitletxtcolor +'size=' + this.TxtSize + '>' +G_cn_week[i]+ '</font>';
           weekString=weekString + '</td>';
          }
    
         }
    else
	{      
	  for (var i=0;i<7;++i)
	  {
	    weekString=weekString + '<td height="21">';
            weekString=weekString + '<div align="center"><font color='+ this.stitletxtcolor +'size=' + this.TxtSize + '>' +G_en_week[i]+ '</font>';
            weekString=weekString + '</td>';
          }
	}
 
  myDaysGird = myDaysGird + weekString;
  myDaysGird = myDaysGird + '</tr></table>';
  
   return myDaysGird;
}


function makeBodyGrid()
{
   var daysGrid='';
   
       //���������������ͼƬ�ļ�������������������ͼƬ��������������body�ı�����ɫ
        if (this.sbodybgfile=='')
		{daysGrid='<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%"  bgcolor='  +  this.sbodybgcolor + '  >';}
		else
		{daysGrid='<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%"  background=" ' + this.sbodybgfile + ' ">';}
		
		var dayOfMonth=0;
       for (var intWeek = 0; intWeek < 6; ++intWeek) 
	{
        //���һ������
           daysGrid=daysGrid + ' <tr>'
        for (var intDay = 0; intDay < 7; ++intDay) 
		{
	   //���һ������
	       //���㵱ǰ�������
            dayOfMonth = (intWeek * 7) + intDay + this.selectedDayOfMonthOfFirstSunday - 7;
    	       //�������dayOfMonthС���㣬������ո�
	       //�������dayOfMonth�����㣬����С�ڸ��µ������������dayOfMonth������û�����
	       //�������dayOfMonth���ڱ�ѡ�е�����selectedday���������䱳����ɫ�����dayOfMonth
    	       
    	       //�������dayOfMonth���ڱ�ѡ�е�����selectedday���������䱳����ɫ
    	      
    	       if ( this.selectedDay == dayOfMonth) 
	          { daysGrid=daysGrid + ' <td bgcolor='+ this.sSelecteddaybgcolor +'>';}
		    else 
			  { daysGrid=daysGrid + '<td>' ; }
		    if (dayOfMonth <= 0) 
		    //�������dayOfMonthС�ڵ����㣬������ո�
		      { daysGrid += "&nbsp;&nbsp;"; }
		      //�������dayOfMonth�����㣬����С�ڸ��µ������������dayOfMonth������û�����
		      else if (dayOfMonth <= this.daysOfSelectedMonth)
			  {
		         //����������ڵĳ����ӣ�����ȫ��������setDay()������
	                 //�����������dayOfMonth��������ɫ����С
		         if ( this.selectedDay == dayOfMonth)
				   {
		              daysGrid += '<a  href="javascript:G_Calendar.setDay(';
		              daysGrid += dayOfMonth + ' )\" style="text-decoration:none" > ' ;
		              daysGrid += '<div align="center"><font color=' + this.sSelecteddaytxtcolor + 'size=' + this.TxtSize + '>' + dayOfMonth + '</font></div></a>';
		            } 
				 else
				   { 
		              daysGrid += '<a href="javascript:G_Calendar.setDay(';
		              daysGrid += dayOfMonth + ' )\" style="text-decoration:none"> ' ;
		              daysGrid += '<div align="center"><font color=' + this.sbodytxtcolor + 'size=' + this.TxtSize + '>' + dayOfMonth + '</font></div></a>';
					}
	           }//elseif
		    daysGrid += '</td>';  
        }//forloop
        if (dayOfMonth <= this.daysOfSelectedMonth)
		{  daysGrid += "</tr>" }
    }//forloop                

    daysGrid=daysGrid + '              </table>'
	
   
    return daysGrid ;	
}

function makeDaysGrid()
{
   var daysGrid='';
   //����Ϊ���ÿ����ɫ��<table>����
    daysGrid=daysGrid + '<table border=0 width="75" border="0"  cellspacing="0" bgcolor='+ this.sframecolor +'align="center">'
    daysGrid=daysGrid + '<tr><td >'
	
	//���ɴ��������head��title��body��<table>����
    daysGrid=daysGrid + '<table border=0 width="75" border="0"  cellspacing="0" bgcolor='  +  this.sheadbgcolor + '  align="center"> '
	
	//��������head
   	daysGrid=daysGrid + '<tr ><td>'
    daysGrid=daysGrid + this.makeHeadGrid();
	daysGrid=daysGrid + '</td></tr>'
	
	//��������title
    daysGrid=daysGrid + '<tr ><td>'
    daysGrid=daysGrid + this.makeTitleGrid();
	daysGrid=daysGrid + '</td></tr>'
	
	//��������body
	daysGrid=daysGrid + '<tr ><td><div id="CB_' + this.handlename + '">'
    daysGrid=daysGrid + this.makeBodyGrid();
    daysGrid=daysGrid + '</div></td></tr>'
	daysGrid=daysGrid + '</table>'
	
    daysGrid=daysGrid + '</td></tr>'
	daysGrid=daysGrid + '</table>'

    
    return daysGrid;
  
}

function refreshbody()
{
//ˢ������body
   var strbody = 'CB_'+ this.handlename;
   document.all[strbody].innerHTML=this.makeBodyGrid();
   G_Calendar.hide();
}

var a= new calendar();
function getdate(obj)
{
     if (G_Calendar==null)
    { return false; }
  
        G_Calendar.construct('daysOfMonth',obj);
        G_Calendar.setTitleBgColor('#E3F7EB');
    	G_Calendar.setTitleTxtColor('#000000');
    	G_Calendar.setSelectedDayTxtColor('#FFFFFF');
    	G_Calendar.setSelectedDayBgColor('#224033');
	    G_Calendar.setBodyBgColor('#8DB597');
	    G_Calendar.setBodyTxtColor('#F4F4DC');
        G_Calendar.setHeadBgColor('#B6D5BE');
                
	    G_Calendar.setLanguage('chinese');
	    G_Calendar.setTxtSize(2);
	    G_Calendar.setBodyBgFile('../images/calendar_back.gif');

        G_Calendar.show();
}