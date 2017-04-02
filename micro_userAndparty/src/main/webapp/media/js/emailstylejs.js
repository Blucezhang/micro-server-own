	

    //邮箱后缀自动显示
	function getStyle(obj, att) {
		if (window.getComputedStyle) {
			return getComputedStyle(obj, false)[att];
		} else {
			return obj.currentStyle[att];
		}
	}
	/*window.onload = function() {
		var s1 = new Suggest();
		s1.init(); //执行初始化函数

	};*/
	function Suggest(inputParm,ulParm) {
		if(inputParm){
			this.oInput = document.getElementById(inputParm);
		}else{
			this.oInput = document.getElementById('electronicMail');
		}
		if(ulParm){
			this.oUl = document.getElementById(ulParm);
		}else{
			this.oUl = document.getElementById('suggest');
		}
		this.aLi = this.oUl.getElementsByTagName('li');
		this.index = 1;
		this.aLi_index = []; //利用数组保存Li；用于后面的而键盘选取Li元素
	}//构造函数
	Suggest.prototype = {
		init : function() {
			this.toChange();
			this.toBlur();
			this.keyNext();
			this.saveLi();
			this.mouseEvent();
		},//初始化
		toChange : function() {
			var ie = !-[ 1, ];
			var _this = this;

			if (ie) {
				this.oInput.onpropertychange = function() {
					_this.tips();
					if (_this.oInput.value == '')
						return '';
					_this.thowUl();
					_this.toggle_Class(1);
					_this.index = 1;
				};
				//onpropertychange 这事件上来就会触发;正常时输入的时候触发才
			} else {
				this.oInput.oninput = function() {
					_this.thowUl();
					_this.tips();
					_this.toggle_Class(1);
					_this.index = 1;
				};
			}
		},//事件发生时
		thowUl : function() {
			this.oUl.style.display = 'block';
		},//显示框
		toBlur : function() {
			var This = this;
			this.oInput.onblur = function() {
				This.oUl.style.display = 'none';
			};
		},//失去焦点hidden框
		tips : function() {
			var value = this.oInput.value;
			if (value == '') {
				this.oUl.style.display = 'none';
			}

			for (var i = 1; i < this.aLi.length; i++) {
				this.aLi[i].style.display = 'block'; //全部显示出来
				var oEmail = this.aLi[i].getAttribute('email');
				if (value.lastIndexOf('@') == -1) //当没输入@时候
				{
					if (oEmail) {
						this.aLi[i].innerHTML = value + oEmail;
					} else {
						this.aLi[1].innerHTML = value;
					}
				} else //当输入@了之后
				{
					var str = value.substring(value.lastIndexOf('@') + 1);
					//截取@后面的字符串
					var te = new RegExp('^@' + str);
					//匹配正则'@'后面的字符串
					if (i == 1) {
						this.aLi[1].innerHTML = value;
					} else if (te.test(oEmail)) {
						this.aLi[i].style.display = 'block';
					} else {
						this.aLi[i].style.display = 'none';
					}
				}
			}
			this.saveLi();
			//这里回修改Li的display所以保存
		},
		keyNext : function() {
			var _this = this;
			this.oInput.onkeydown = function(e) {
				var e = e || event;
				var len = _this.aLi_index;
				if (e.keyCode == 40) {
					if (_this.index == len.length - 1) {
						_this.index = 0;
					}
					_this.toggle_Class(len[++_this.index]);
					//下键
				} else if (e.keyCode == 38) {
					if (_this.index == 1) {
						_this.index = len.length;
					}
					_this.toggle_Class(len[--_this.index]);
					//上键
				} else if (e.keyCode == 13) {
					_this.oInput.value = _this.aLi[len[_this.index]].innerHTML;
					_this.oInput.blur();
				}//回车
			};

		},
		mouseEvent : function() {
			var _this = this;
			for (var i = 1; i < this.aLi.length; i++) {
				(function(i) {
					_this.aLi[i].onmouseover = function() {
						_this.toggle_Class(i);
						for (var b = 0; b < _this.aLi_index.length; b++) {
							if (_this.aLi_index[b] == i) {
								_this.index = b;
							}
						}
						; // 用枚举找到索引在数组中的索引(长度);
						/*
						 1.数组保存了当前'block'(显示)了几个Li元素
						 2.数组里面的值代表了Li的索引
						 PS: 鼠标转按键：  _this.toggle_Class( len[++_this.index] );(按键模式);
						 如果是常规的 _this.index = i; 那么假如当前是'block'的aLi的长度是5（也就是数组长度是5）;
						 但 i 可能 是 14（aLi元素集合的长度）中的任何值;这就导致了 如果i=14 '数组【14】错误'；	 
						 */

					};
				})(i);//为了传递 i 定义一个自执行；
				this.aLi[i].onmousedown = function() {
					_this.oInput.value = this.innerHTML;
					var showValue = _this.oInput.value;
				};
			}
		},
		toggle_Class : function(oIndex) {
			for (var i = 0; i < this.aLi.length; i++) {
				this.aLi[i].className = 'item';
			}
			;
			if (oIndex) {
				this.aLi[oIndex].className = 'active';
			}
		},
		saveLi : function() {
			this.aLi_index = [];
			for (var i = 0; i < this.aLi.length; i++) {
				if (getStyle(this.aLi[i], 'display') != 'none') {
					this.aLi_index.push(i);
				}
			}
		}
	}