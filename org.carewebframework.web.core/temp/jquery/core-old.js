define(['jquery', 'jquery-ui', 'bootstrap', 'lodash'], function($) {
	var cwf;
	return cwf = {
	
	init: function(options) {
		window.onerror = function(error) {
			cwf.fatal(error);
		}
		
		this.debug = options.debug;
		this.pid = options.pid;
		this.jquery._init();
		this.ws._init(options.wsurl);
		this.event._init();
	},
	
	_doInvocation: function(data) {
		if ($.isArray(data)) {
			for (var i = 0; i < data.length; i++) {
				this._doInvocation(data[i]);
			}
			
			return;
		}
		
		if (!data.fcn) {
			return;
		}
		
		var tgt = data.tgt;
		
		if (tgt) {
			if (tgt === '#dead')
				return;
			
			tgt = $(tgt).cwf$widget();
			
			if (!tgt) {
				throw new Error('No widget matching ' + data.tgt);
			}
		} else {
			tgt = this;
			data.tgt = 'cwf';
		}
		
		var fcn = this.resolveReference(tgt, data.fcn);
		
		if (!fcn) {
			throw new Error('Unknown function ' + data.tgt + '.' + data.fcn);
		}
		
		var arg = data.arg || [];
		
		if (!$.isArray(arg)) {
			arg = [arg];
		}

		fcn.ref.apply(fcn.base, arg);
	},

	// JQuery extensions
	jquery: {
		_init: function() {
			for (var fn in this) {
				if (fn != '_init') {
					$.fn[fn] = this[fn];
				}
			}
		},
		
		cwf$widget: function() {
			return this.data('cwf_widget');
		},
		
		cwf$attr: function(name, value) {
			if (value == null || value === '') {
				this.removeAttr(name);
			} else {
				this.attr(name, value);
			}
		},
		
		cwf$removeSubclass: function(base, sub) {
			if (cwf.isArray(sub)) {
				sub = sub.join(' ');
			}
			
			sub = sub.split(' ');
			
			for (var i in sub) {
				this.removeClass(base + sub[i]);
			}
		},
		
		cwf$mask: function(option, id) {
			if (this.css('display') !== 'block') {
				return;
			}
			
			id = id || 'cwf-mask';
			var mask = $('#' + id);
			var _this = this;
			
			function ensureMask() {
				if (mask.length == 0) {
					var zmodal = ++cwf.widget._zmodal;
					mask = $('<div id="' + id + '" class="cwf_mask"/>')
						.prependTo(_this)
						.data('z-index', [])
						.zIndex(zmodal);
				}
				return mask;
			}
			
			switch (option || 'show') {
				case 'remove':
					mask.remove();
					break;
					
				case 'hide':
					mask.hide();
					break;
					
				case 'show':
					ensureMask().show();
					break;
					
				case 'front':
					ensureMask();
					mask.data('z-index').push(mask.zIndex());
					mask.zIndex(++cwf.widget._zmodal);
					break;
					
				case 'back':
					var stack = ensureMask().data('z-index');
					
					if (stack.length > 0) {
						mask.zIndex(stack.pop());
					} else {
						mask.hide();
					}
					
					break;
			}
			
			return mask;
		}
	},
	
	// Websocket support
	ws: {
		_init: function(wsurl) {
			this.socket = new WebSocket(wsurl);
			this.socket.onerror = this._onerror;
			this.socket.onmessage = this._onmessage;
			this.socket.onopen = this._onopen;
		},
		
		_onopen: function() {
			cwf.ws.sendData('init');
		},
	
		_onmessage: function(message) {
			var data = JSON.parse(message.data);
			cwf.log(data);
			cwf._doInvocation(data);
		},
		
		_onerror: function(event) {
			console.log(event);
		},
		
		isConnected() {
			return this.socket.readyState === WebSocket.OPEN;
		},
		
		sendData: function(type, data) {
			if (!this.isConnected()) {
				throw new Error('Communication with the server has been interrupted.');
			}
			
			this.socket.send(JSON.stringify({type: type, pid: cwf.pid, data: data}));
		}
	},
	
	// Event support
	event: {
		postProcessors: {},
		
		_init: function() {
			var fn = function(event, data) {
				data.value = event.target.value;
			}
			
			this.registerPostProcessor('change', fn);
			this.registerPostProcessor('input', fn);
		},
		
		_postprocess: function(event, data) {
			var fn = this.postProcessors[event.type];
			
			if (fn) {
				fn.apply(this, arguments);
			}
		},
		
		forward: function(event, type, target) {
			target = cwf.$(target || event.target);
			event.type = type || event.type;
			target.trigger(event);
		},
		
		registerPostProcessor: function(type, fn) {
			this.postProcessors[type] = fn;
		},
		
		sendToServer: function(event) {
			var data = {};
			
			for (var pname in event) {
				var value = event[pname];
				value = pname === 'data' || cwf.isScalar(value) ? value : value.id;
				
				if (value != null) {
					data[pname] = value;
				}
			}
			
			this._postprocess(event, data);
			cwf.ws.sendData('event', data);
		}
	},
	
	
	// Utilities
	$: function(object) {
		return object == null ? null : object.jquery ? object : 
			object.widget$ ? object.widget$ : $(object);
	},
	
	wgt: function(object) {
		return object == null ? null : this.$(object).cwf$widget();
	},
	
	fatal: function(error) {
		$('#' + this.pid).hide();
		
		if (!(error instanceof Error)) {
			error = new Error(error);
		}
		
		var message = 'Fatal error:\n\n' + error.message;
		
		if (error.lineNumber) {
			message += ' at line ' + error.lineNumber;
		}
		
		if (error.fileName) {
			message += ' in ' + error.fileName;
		}
		
		alert(message);
	},
	
	log: function(message) {
		if (cwf.debug) {
			console.log(message);
		}
	},
	
	alert: function(message, title, type) {
		var alert = cwf.widget.create('Alert');
		alert.widget$.addClass('panel-' + (type || 'primary'));
		alert.closable(true);
		alert.title(title || 'Alert');
		alert.text(message);
		alert.widget$.find('.glyphicon-remove').on('click', function() {
			alert.destroy();
		})
		
		return alert;
	},
	
	resolveReference: function(base, path) {
		var ref = base;
		var i = 0;
		path = path == null ? [] : path.split('.');
		
		while (i < path.length && ref) {
			base = ref;
			ref = ref[path[i++]];
		}
		
		return ref ? {base: base, ref: ref} : null;
	},
	
	resolveEL: function(base, text) {
		var i = 0;
		base = base || this;
		
		while ((i = text.indexOf('${', i)) >= 0) {
			var j = text.indexOf('}', i + 2);
			j = j == -1 ? text.length : j;
			var exp = text.substring(i + 2, j);
			var ref = exp.length == 0 ? null : this.resolveReference(base, exp);
			ref = ref == null ? '' 
				: this.isFunction(ref.ref) ? ref.ref.apply(base)
				: !this.isScalar(ref.ref) ? ''
				: ref.ref;
			text = text.substring(0, i) + ref + text.substring(j + 1);
			i += exp.length - ref.length;
		}
		
		return text;
	},
	
	escapeHtml: function(text) {
		var div = document.createElement('div');
		div.appendChild(document.createTextNode(text));
		return div.innerHTML;
	},
	
	isObject: function(x) {
		return x != null && typeof x === 'object';
	},
	
	isFunction: function(x) {
		return x != null && typeof x === 'function';
	},
	
	isScalar: function(x) {
		return !this.isObject(x) && !this.isFunction(x);
	}
	
}});  // end module definition