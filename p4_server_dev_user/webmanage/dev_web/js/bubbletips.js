/***
 * 气泡信息提示Jquery插件
***/

var inShow;//是否在显示中

function showHelp(obj,note){
	if(inShow){
		hideHelp();
	}
	inShow = true;
	$("#"+obj.id+"").bubbletips({	position : "l",value : 35,content : "说明：<br>"+note});
}

function hideHelp(){
	inShow = false;
	$("#btip").hide();
}

$(function() {
	$.fn.bubbletips = function(options) {
		var defaults = {			
			position : "t",			//箭头指向上(t)、箭头指向下(b)、箭头指向左(l)、箭头指向右(r)
			value : 15,				//小箭头偏离左边和上边的位置
			content : ""			//内容
			
		};
		var options = $.extend(defaults,options);		
		var offset = $(this).offset();
		$("body").prepend('<div class="docBubble" id="btip"><i class="triangle-'+options.position+'"></i><div class="tl"><div class="inner"><div class="cont">'+options.content+'</div></div></div><div class="tr"></div><div class="bl"></div></div>');
		var $btip = $("#btip");
		var h = $(this).height();
		var w = $(this).width();	
		switch(options.position){
			case "t" ://当它是上面的时候
				$(".triangle-t").css('left',options.value);
				$btip.css({ "left":offset.left+w/2-options.value  ,  "top":offset.top+h+14  });
				break;
			case "b" ://当它是下面的时候
				$(".triangle-b").css('left',options.value);
				$btip.css({ "left":offset.left+w/2-options.value  ,  "top":offset.top-h-7-$btip.height()  });
				break;
			case "l" ://当它是左边的时候		
				$(".triangle-l").css('top',options.value);
				$btip.css({ "left":offset.left+w+10  ,  "top":offset.top+h/2-7-options.value });
				break;
			case "r" ://当它是右边的时候			
				$(".triangle-r").css('top',options.value);
				$btip.css({ "left":offset.left-w+25-$btip.width()  ,  "top":offset.top+h/2-7-options.value });
				break;
		}
	}
});