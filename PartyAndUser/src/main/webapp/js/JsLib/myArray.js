/**
 * 维护cookie
 * 使用方法:
 * var cf=new window.cookiesFactory();    
 * cf.createCookies(?,?,?,?);常量变量时间路径 存
 * cf.getCookies(?);取 key
 * cf.deleteCookies(?);删
 */
(function (root) {
	
	root.arrayFactory=function(parm){
		this.array=parm;/*默认*/
	};
	
	arrayFactory.prototype.removeType = function(obj) {
		for (var i = 0; i < this.array.length; i++) {
			var temp = this.array[i];
			if (!isNaN(obj)) {
				temp = i;
			}
			if (temp.type == obj) {
				for (var j = i; j < this.array.length; j++) {
					this.array[j] = this.array[j + 1];
				}
				this.array.pop();
			}
		}
		return this.array;
	};
	arrayFactory.prototype.remove = function(obj) {
		for (var i = 0; i < this.array.length; i++) {
			var temp = this.array[i];
			if (!isNaN(obj)) {
				temp = i;
			}
			if (temp == obj || temp.name == obj) {
				if (temp.name) {
					console.log(temp);
					if (temp.isBrand == "brandImg") {
						var divs = document.getElementById("divId" + temp.modelId);
						divs.style.borderColor = "gray";
						divs.style.backgroundImage = "url(../../../media/images/white.png)";
						divs=document.getElementById(temp.isBrand+"s"+temp.modelId);
						divs.checked = (divs.checked?false:true);
					} else if (temp.isBrand == "model"
							|| temp.isBrand == "standard") {
						var divs = document.getElementById(temp.isBrand
								+ temp.modelId);
						divs.checked = (divs.checked ? false : true);
					}
				}
				for (var j = i; j < this.array.length; j++) {
					this.array[j] = this.array[j + 1];
				}
				this.array.pop();
			}
		}
		return this.array;
	};
	
	arrayFactory.prototype.removeIndex=function(obj){
		for(var i =0;i <this.array.length;i++){
		var temp = this.array[i];
		if(!isNaN(obj)){
		temp=i;
		}
		if(temp == obj){
		for(var j = i;j <this.array.length;j++){
		this.array[j]=this.array[j+1];
		}
		this.array.length = this.array.length-1;
		}
		}
		return this.array;
		}

	arrayFactory.prototype.removeAll = function() {
		while (this.array.length > 0) {
			if (!isNaN(this.array.name)) {
				this.remove(this.array[0].name);
			} else {
				this.remove(this.array[0]);
			}
		}
		return this.array;
	};
})(this);




