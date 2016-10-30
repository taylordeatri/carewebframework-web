define('cwf-core', ['jquery', 'lodash'], function($) {
	var cwf;
	return cwf = {
	
	init: function(options) {
		window.onerror = function(error) {
			cwf.fatal(error);
		};
		
		this.debug = options.debug;
		this.pid = options.pid;
		this.action._init();
		this.jquery._init();
		this.event._init();
		this.widget._init();
		this.ws._init(options.wsurl);
	},
	
	action: {
		_init: function() {
			this.queue = [];
			this.processing = false;
		},
		
		processQueue: function() {
			if (this.processing) {
				return;
			}
			
			var action,
				self = this;
			
			this.processing = true;
			
			try {
				while (action = this.queue.shift()) {
					var result = this._processAction(action);
					
					if (result instanceof Promise) {
						return result.then(function() {
							self.processing = false;
							self.processQueue();
						}, function(error) {
							self.processing = false;
							throw error;
						})
					}
				}
			} catch (e) {
				this.processing = false;
				throw e;
			}
			
			this.processing = false;
		},
		
		queueAction: function(action) {
			if (_.isArray(action)) {
				Array.prototype.push.apply(this.queue, action);
			} else {
				this.queue.push(action);
			}
			
			this.processQueue();
		},
		
		_processAction: function(action) {
			if (!action.fcn) {
				return;
			}
			
			var tgt = action.tgt;
			
			if (tgt) {
				if (tgt === 'dead') {
					return;
				}
				
				tgt = cwf.resolveId(tgt);
				
				if (!tgt) {
					throw new Error('No target matching ' + action.tgt);
				}
			} else {
				tgt = cwf;
				action.tgt = 'cwf';
			}
			
			var fcn = cwf.resolveReference(tgt, action.fcn);
			
			if (!fcn) {
				throw new Error('Unknown action (' + action.tgt + ').' + action.fcn);
			}
			
			return fcn.ref.apply(fcn.base, _processArgs(action.arg));
			
			function _processArgs(args) {
				args = args || [];
				
				if (!_.isArray(args)) {
					args = [args];
				}
	
				return _.map(args, function(value) {
					return value === null ? null : value.__cwf__ ? cwf.widget.find(value.__cwf__) : value;
				});
			}
		}
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
		
		cwf$attr: function(name, value) {
			if (value === null || value === '') {
				this.removeAttr(name);
			} else {
				this.attr(name, value);
			}
		},
		
		cwf$removeSubclass: function(base, sub) {
			if (_.isArray(sub)) {
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
				if (mask.length === 0) {
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
			var action = JSON.parse(message.data);
			cwf.log(action, 'Received');
			cwf.action.queueAction(action);
		},
		
		_onerror: function(event) {
			console.log(event, 'Error');
		},
		
		isConnected: function() {
			return this.socket.readyState === WebSocket.OPEN;
		},
		
		sendData: function(type, data) {
			if (!this.isConnected()) {
				throw new Error('Communication with the server has been interrupted.');
			}
			
			var pkt = {type: type, pid: cwf.pid, data: data};
			this.socket.send(JSON.stringify(pkt));
			cwf.log(pkt, 'Sent');
		}
	},
	
	// Event support
	
	event: {
		postProcessors: {},
		
		_init: function() {
			var fn = function(event, data) {
				data.value = event.target.value;
			};
			
			this.registerPostProcessor('change', fn);
			this.registerPostProcessor('input', fn);
		},
		
		_postprocess: function(event, data) {
			var fn = this.postProcessors[event.type];
			
			if (fn) {
				if (event.persist) {
					event.persist();
				}
				
				fn.apply(this, arguments);
			}
		},
		
		removeOn: function(type) {
			return type.startsWith('on') ? _.lowerFirst(type.substring(2)) : type;
		},
		
		addOn: function(type) {
			return type.startsWith('on') ? type : ('on' + _.upperFirst(type));
		},
		
		registerPostProcessor: function(type, fn) {
			this.postProcessors[type] = fn;
		},
		
		sendToServer: function(event) {
			var data = {};
			
			_.forIn(event, function(value, pname) {
				value = pname === 'data' || !_.isObject(value) ? value : value.id;
				
				if (value !== null) {
					data[pname] = value;
				}
			});
			
			cwf.event._postprocess(event, data);
			cwf.ws.sendData('event', data);
		}
	},
	
	// Widget support
	
	widget: {
		_registry: {},
		
		_zmodal: 999,
		
		_init: function() {
			this.create('#cwf_modal', {wclass_: 'Modal_'});
		},
		
		/**
		 * Create a widget instance
		 * 
		 * @param parent {selector} Selector for the parent element.  If not specified, the page root is assumed.
		 * @param props {object} The fixed properties.
		 * @param state {object} The initial state.
		 * @return {ReactElement} The newly created widget.
		 */
		create: function(parent, props, state) {
			var wpkg = props.wpkg_ || 'cwf-widget',
				pkg;
			
			try {
				pkg = require(wpkg);
			} catch (e) {
				return new Promise(function(resolve, reject) {
					require([wpkg], function(pkg) {
						try {
							resolve(_create(pkg));
						} catch(e) {
							reject(e);
						}
					}, function(e) {
						reject(e);
					});
				});
			};
			
			return _create(pkg);
			
			function _create(pkg) {
				var clazz = pkg[props.wclass_];
				
				if (!clazz) { 
					throw new Error('Unrecognized widget class: ' + wpkg + '.' + props.wclass_);
				}
				
				parent = cwf.resolveId(parent || '#cwf_root');
				return clazz.create ? clazz.create(parent, props, state) : new clazz(parent, props, state);
			}
		},
		
		find: function(id) {
			id = id.key || id.id || id;
			return this._registry[id];
		},
		
		register: function(id, wgt) {
			return this._registry[id] = wgt;
		},
		
		unregister: function(id) {
			delete this._registry[id];
		}
		
	},
	
	// Utilities
	
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
	
	log: function(message, label) {
		if (cwf.debug) {
			console.log((label ? label + ': ' : ''), message);
		}
	},
	
	alert: function(message, title, type) {
		var props = {
				wclass_: 'Alert_'},
			state = {
				type: type,
				title_: title || 'Alert',
				message_: message},
			alert = cwf.widget.create(cwf.resolveId('cwf_modal'), props, state);
		
		return alert;
	},
	
	uniqueId: function() {
		return _.uniqueId('cwf$');
	},
	
	resolveId: function(id) {
		var result = cwf.widget.find(id);
		return result || $(id)[0];
	},
	
	resolveReference: function(base, path) {
		var ref = base;
		var i = 0;
		path = path === null ? [] : path.split('.');
		
		while (i < path.length && ref) {
			base = ref;
			var name = path[i++];
			ref = ref[name];
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
			var ref = exp.length === 0 ? null : this.resolveReference(base, exp);
			ref = ref === null ? '' 
				: _.isFunction(ref.ref) ? ref.ref.apply(base)
				: _.isObject(ref.ref) ? ''
				: ref.ref;
			text = text.substring(0, i) + ref + text.substring(j + 1);
			i += exp.length - ref.length;
		}
		
		return text;
	},
	
	parseStyle: function(style) {
		var result = {};
		
		style.split(';').forEach(function (value){
			var i = value.indexOf(':');
			result[_.trim(value.substring(0, i))] = _.trim(value.substring(i + 1));
		});
		
		return result;
	},
	
	combine: function(sources, filter) {
		target = {};
		
		for (var i = 0; i < sources.length; i++) {
			if (sources[i]) {
				_.forOwn(sources[i], function(value, key, object) {
					if (!filter || filter.apply(object, arguments)) {
						target[key] = value;
					}
				});
			}
		}
		
		return target;
	},
	
	insertIntoArray: function(array, element, position) {
		var i = array.indexOf(element),
			changed = false;
	
		if (i >= 0 && i === position) {
			return;
		}
		
		if (i >= 0) {
			array = array.slice();
			array.splice(i, 1);
			changed = true;
		}
		
		if (position >= 0) {
			array = changed ? array : array.slice();
			array.splice(position, 0, element);
			changed = true;
		}
		
		return changed ? array : undefined;
	},
	
	ensureValue: function(value, dflt) {
		return _.isNil(value) ? dflt : value;
	}
		
}});  // end module definition
