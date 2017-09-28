var countdown = 60;
function settime(obj) {
	if (countdown == 0) {
		obj.attr('disabled', false);
		obj.html('发送短信');
		countdown = 60;
		return;
	} else {
		obj.attr('disabled', true);
		obj.html('重新发送' + countdown + 's');
		countdown--;
	}
	setTimeout(function() {
		settime(obj);
	}, 1000);
}

var timeout = 30000;
var t = null;
function showMask(){
	$('#loading').fakeLoader({
		timeToHide:timeout,
		spinner:'spinner2',
		bgColor:'#'
	});
	t = setTimeout(function() {
		hideMask();
	}, timeout);
	$('div.login').find("*").each(function() { 
		$(this).attr("disabled", "disabled"); 
	});
}

function hideMask(){
	$('div.login').find("*").each(function() { 
		$(this).removeAttr("disabled"); 
	});
	$('#loading').remove();
	$('body').append('<div id="loading"></div>');
	if(t!=null){
		clearTimeout(t);
		t = null;
	}
}


var authUrl = '/api/operator/auth/v1';
function interact() {
	
	$('#error').hide();
	showMask();

	var tel = $('#tel').val().trim();
	var password = $('#password').val().trim();
	var step = $('#step').val();
	var code = $('#code').val().trim();
	var captcha = $('#captcha').val().trim();
	var operator = $('#operator').val();
	var province = $('#province').val();

	var sendData = {
		'tel' : tel,
		'password' : password,
		'code' : code,
		'captcha' : captcha,
		'step' : step,
		'operator' : operator,
		'province' : province,
		'service_code' : 'operator',
	}

	$.ajax({
		'url' : authUrl,
		'type' : 'POST',
		'contentType' : 'application/json;charset=UTF-8',
		'dataType' : 'json',
		'data' : JSON.stringify(sendData),
		'success' : function(data, status, xhr) {
			hideMask();
			var code = data.code;
			$('#status').val(code);
			if (code == 100) {// 需要验证码
				$('#capDiv').show();
				$('#captchaImg').attr('src',
						'data:image/jpeg;base64,' + data.img);
			} else if (code == 101) {// 不需要图片验证码
				$('#capDiv').hide();
				$('#captcha').val('');
			} else if (code == 102) {// 需要身份验证
				$('#error').html('需要身份验证,请发送短信进行身份验证');
				$('#error').show();
				$('#code').val('');
				$('#codeDiv').show();
				//$('#sendCode').click();
			} else if (code == 103) {// 需要短信验证(移动)
				$('#code').val('');
				$('#codeDiv').show();
				//$('#sendCode').click();
			} else if (code == 104) {// 不需要短信验证(移动)
				$('#code').val('');
				$('#codeDiv').hide();
			} else if (code == 203) {// 刷新验证码成功
				$('#capDiv').show();
				$('#captchaImg').attr('src',
						'data:image/jpeg;base64,' + data.img);
			} else if (code == 303) {// 刷新验证码失败
				$('#capDiv').show();
				$('#captchaImg').attr('src', '#');
			} else if (code == 204) {// 发送短信成功
				settime($('#sendCode'));
			} else if (code == 209) {// 发送身份验证短信成功
				$('#step').val('valid');
				settime($('#sendCode'));
			} else if (code == 304 || code==309) {// 发送短信失败
				$('#error').html('发送短信失败，请重试');
				$('#error').show();
			} else if (code == 305) {// 发送短信次数超过当日上限
				$('#sendCode').attr('disabled', 'disabled');
				$('#error').html(data.msg);
				$('#error').show();
			} else if (code == 206) {// 验证码正确
				//$('#error').html(data.msg);
				//$('#error').show();
			} else if (code == 306) {// 验证码错误
				$('#error').html(data.msg);
				$('#error').show();
			} else if (code == 201) {// 登录成功
				window.location.href='/success.html';
			} else if (code == 301) {// 登录失败
				$('#error').html(data.msg);
				$('#error').show();
			} else if (code == 302) {// 身份验证失败
				$('#error').html(data.msg);
				$('#error').show();
			} else if (code == 401) {// 短信验证码错误
				$('#error').html(data.msg);
				$('#error').show();
			} else {// 未知异常
				$('#error').html('未知异常，请刷新页面重试');
				$('#error').show();
			}
		},
		'error' : function(data, status, xhr) {
			hideMask();
			console.log('data:' + data + ';status=' + status);
		}
	});

}

function locTel(tel) {
	$('#step').val('init');
	
	var queryUrl = 'http://v.showji.com/Locating/showji.com2016234999234.aspx?m='
			+ tel + '&output=json&timestamp=' + new Date().getMilliseconds();
	console.log(queryUrl)
	$.ajax({
		url : queryUrl,
		type : 'GET',
		async : false,
		dataType : 'JSONP',
		success : function(data, status) {
			console.log(data);
			if (data.QueryResult == 'True') {
				$('#province').val(data.Province);
				var opt = data.Corp;
				if (opt.indexOf('移动') != -1) {
					$('#operator').val('移动');
					interact();
				} else if (opt.indexOf('联通') != -1) {
					$('#operator').val('联通');
					interact();
				}else if (opt.indexOf('电信') != -1) {
					$('#error').html('暂不支持电信手机号');
					$('#error').show();
					//$('#operator').val('电信');
					//interact();
				} else {
					$('#error').html('暂不支持该手机号');
					$('#error').show();
				}
			} else {
				$('#error').html('请输入正确的手机号');
				$('#error').show();
			}
		}
	});
}

$(function() {

	$('#capDiv').hide();
	$('#sendValidCode').hide();

	$('#tel').on('input',function() {
		if (/\d{11}/.test(this.value)) {
			locTel(this.value);
		}
	});
	
	$('#tel').blur(function(){
		if (!/\d{11}/.test(this.value)) {
			$('#error').html('请输入正确的手机号');
			$('#error').show();
		}
	});

	$('#captchaImg').click(function() {
		$('#step').val('sendcaptcha');
		interact();
	});

	$('#captcha').blur(function() {
		$('#step').val('validcaptcha');
		interact();
	});

	$('#sendCode').click(function() {
		if (!/\d{11}/.test($('#tel').val())) {
			$('#error').html('请输入正确的手机号');
			$('#error').show();
			return;
		}
		if($('#status').val()=='103'){
			$('#step').val('sendsms');
		}else{
			$('#step').val('sendvalidsms');
		}
		interact();
	});
	
	$('#submit').click(function() {
		if (!/\d{11}/.test($('#tel').val())) {
			$('#error').html('请输入正确的手机号');
			$('#error').show();
			return;
		}
		if (!/\d{6}/.test($('#password').val())) {
			$('#error').html('服务密码为6位数字');
			$('#error').show();
			return;
		}
		if(!$('#capDiv').is(":hidden") && !/\w{4}/.test($('#captcha'))){
			$('#error').html('验证码错误');
			$('#error').show();
			return;
		}
		if($('#step').val()!='valid'){
			$('#step').val('login');
		}
		interact();
	});

});