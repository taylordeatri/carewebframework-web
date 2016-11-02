'use strict';

define('cwf-core', ['jquery', 'jquery-ui', 'lodash'], function($) {
	var cwf;
	return cwf = {
	
	/*------------------------------ Initialization ------------------------------*/
			
	init: function(options) {
		window.onerror = function(error) {
			cwf.fatal(error);
		};
		
		window.onbeforeunload = function() {
			return cwf._canClose ? null : true;
		};
		
		this.debug = options.debug;
		this.pid = options.pid;
		this._canClose = true;
		this.log._init(options.logging);
		this.action._init();
		this.jquery._init();
		this.event._init();
		this.widget._init();
		this.ws._init(options.wsurl);
	},
	
	/*------------------------------ Request Processing ------------------------------*/
	
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
			
				tgt = cwf.widget.find(tgt);
			
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

	/*------------------------------ JQuery Extensions ------------------------------*/
	
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
			return _.isNil(value) || value === '' ? this.removeAttr(name): this.attr(name, value);
		},
		
		cwf$show: function(visible) {
			return visible ? this.show() : this.hide();
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
		
		cwf$mask: function() {
			return $('<div class="cwf_mask"/>')
				.css('z-index', ++cwf.widget._zmodal)
				.prependTo(this);
		}
	},
	
	/*------------------------------ Websocket Support ------------------------------*/
	
	ws: {
		_init: function(wsurl) {
			this.socket = new WebSocket(wsurl);
			this.socket.onerror = this._onerror;
			this.socket.onmessage = this._onmessage;
			this.socket.onopen = this._onopen;
		},
		
		_onopen: function() {
			var data = {
				requestURL: window.location.href,
				viewportHeight: $(window).height,
				viewportWidth: $(window).width,
				timezoneOffset: new Date().getTimezoneOffset()
			};
			
			_.forIn(navigator, function(value, key) {
				_.isObject(value) || _.isNil(value) ? null : data[key] = value;
			});
			
			cwf.ws.sendData('init', data);
		},
	
		_onmessage: function(message) {
			var action = JSON.parse(message.data);
			cwf.log.debug('Received: ', action);
			cwf.action.queueAction(action);
		},
		
		_onerror: function(event) {
			console.log.error(event);
		},
		
		isConnected: function() {
			return this.socket && this.socket.readyState === WebSocket.OPEN;
		},
		
		sendData: function(type, data, nolog) {
			if (!this.isConnected()) {
				throw new Error('Communication with the server has been interrupted.');
			}
			
			var pkt = {type: type, pid: cwf.pid, data: data};
			this.socket.send(JSON.stringify(pkt));
			
			if (!nolog) {
				cwf.log.debug('Sent: ', pkt);
			}
		}
	},
	
	/*------------------------------ Event Support ------------------------------*/
		
	event: {
		postProcessors: {},
		
		_init: function() {
			this.registerPostProcessor('change', _ppChangeInput);
			this.registerPostProcessor('input', _ppChangeInput);
			this.registerPostProcessor('resize', _ppResize);
			
			function _ppChangeInput(event, data) {
				data.value = data.value || event.target.value;
			}
			
			function _ppResize(event, data) {
				var rect = event.target.getBoundingClientRect();
				data.width = rect.width;
				data.height = rect.height;
				data.top = rect.top;
				data.bottom = rect.bottom;
			}
		},
		
		_postprocess: function(event, data) {
			var fn = this.postProcessors[event.type];
			
			if (fn) {
				fn.apply(this, arguments);
			}
		},
		
		forward: function(event, type, target) {
			var target$ = cwf.$(target || event.target);
			event.type = type || event.type;
			target$.triggerHandler(event);
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
		
		sendToServer: function(event, params) {
			var pkt = {};
			
			if (params) {
				_.assign(event, params);
			}
			
			_.forIn(event, function(value, pname) {
				value = pname === 'data' || !_.isObject(value) ? value : cwf.id(value);
				
				if (value !== null) {
					pkt[pname] = value;
				}
			});
			
			cwf.event._postprocess(event, pkt);
			cwf.ws.sendData('event', pkt);
		}
	},
	
	/*------------------------------ Widget Support ------------------------------*/
	
	widget: {
		_registry: {},
		
		_zmodal: 999,
		
		_init: function() {
		    $.widget.bridge('uitooltip', $.ui.tooltip);
		    $.widget.bridge('uibutton', $.ui.button);
		    cwf.debug ? null : $('body').on('contextmenu', function(event) {
		    	event.preventDefault();
		    });
		},
		
		/**
		 * Create a widget instance
		 * 
		 * @param {Widget} parent The parent widget
		 * @param {object} props Immutable widget properties.
		 * @param {object} [state] Initial state values.
		 * @return {Widget} The newly created widget.
		 */
		create: function(parent, props, state) {			
			var pkg;
			
			props.id = props.id || cwf.uniqueId();
			props.wpkg = props.wpkg || 'cwf-widget';
			
			try {
				pkg = require(props.wpkg);
			} catch (e) {
				return new Promise(function(resolve, reject) {
					require([props.wpkg], function(pkg) {
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
				var clazz = pkg[props.wclass];
				
				if (!clazz) { 
					throw new Error('Unrecognized widget class: ' + props.wpkg + '.' + props.wclass);
				}
				
				return cwf.widget.register(props.id, new clazz(parent, props, state));
			}
		},
		
		find: function(id) {
			return _.isNil(id) ? null : this._registry[id];
		},
		
		register: function(id, wgt) {
			this._registry[id] = wgt;
			return wgt;
		},
		
		unregister: function(id) {
			delete this._registry[id];
		},
		
		isWidget: function(object) {
			return object && object.constructor && object.constructor.name === 'Widget';
		}
		
	},
	
	log: {
		level: {},
		
		_init: function(settings) {
			this.level = settings;
		},

		debug: function() {
			cwf.log._log.call(this, 'debug', arguments);
		},
		
		error: function() {
			cwf.log._log.call(this, 'error', arguments);
		},
		
		fatal: function() {
			cwf.log._log.call(this, 'fatal', arguments);
		},
		
		info: function() {
			cwf.log._log.call(this, 'info', arguments);
		},
		
		trace: function() {
			cwf.log._log.call(this, 'trace', arguments);
		},
		
		warn: function() {
			cwf.log._log.call(this, 'warn', arguments);
		},
		
		/**
		 * Send log message to console and to server.
		 */
		_log: function(level, message) {
			var setting = cwf.log.level[level];
			
			if (setting & 2) {
				if (cwf.ws.isConnected()) {
					cwf.ws.sendData('log', {level: level, message: message}, true);
				} else {
					setting = 1;
				}
			}
			
			if (setting & 1) {
				var lvl = level === 'fatal' ? 'error' : level === 'trace' ? 'log' : level;
				console[lvl](level, ': ', message);
			}
		}
	},
	
	/*------------------------------ Utility Functions ------------------------------*/
	
	/**
	 * Returns the jquery object associated with the object.
	 */
	$: function(object) {
		return _.isNil(object) ? null : object.jquery ? object : 
			cwf.widget.isWidget(object) ? object.widget$ : $(object);
	},
	
	/**
	 * Returns the widget associated with the object.
	 */
	wgt: function(object) {
		return _.isNil(object) ? null : cwf.widget.isWidget(object) ? object : 
			this.$(object).cwf$widget();
	},	
	
	/**
	 * Returns the DOM element associated with the object.
	 */
	ele: function(object) {
		return _.isNil(object) ? null : _.isElement(object) ? object : cwf.$(object)[0];
	},
	
	/**
	 * Swaps positions of the two elements within the DOM.
	 */
	swap: function(ele1, ele2) {
		ele1 = cwf.ele(ele1);
		ele2 = cwf.ele(ele2);
		
		var parent1 = ele1.parentNode,
			next1 = ele1.nextSibling,
			parent2 = ele2.parentNode,
			next2 = ele2.nextSibling;

		parent1.insertBefore(ele2, next1);
		parent2.insertBefore(ele1, next2);
	},
	
	/**
	 * Returns the id associated with the object.
	 */
	id: function(object) {
		return _.isNil(object) ? null : object.jquery ? object.attr('id') : object.id;
	},
	
	/**
	 * Handler for fatal exceptions.
	 */
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
	
	/**
	 * Display an alert dialog.
	 */
	alert: function(message, title, flavor) {
		var props = {
				wclass: 'Alert'},
			state = {
				_clazz: 'panel-' + (flavor || 'primary'),
				title: title || 'Alert',
				text: message};
		
		return alert = cwf.widget.create(null, props, state);
	},
	
	/**
	 * Submits a form.
	 */
	submit: function(form) {
		cwf.$(form).submit();
	},
	
	/**
	 * Enables or disables close confirmation.
	 */
	canClose: function(value) {
		cwf._canClose = value;
	},
	
	/**
	 * Return a unique identifier.
	 */
	uniqueId: function() {
		return _.uniqueId('cwf__');
	},
	
	/**
	 * Resolve a hierarchical reference relative to the specified base.
	 */
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
	
	/**
	 * Resolve embedded Expression Language references.
	 */
	resolveEL: function(base, text, pfx) {
		var i = 0;
		base = base || this;
		pfx = (pfx || '$').charAt(0) + '{';
		
		while ((i = text.indexOf(pfx, i)) >= 0) {
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
	
	/**
	 * Escape reserved characters in text. 
	 */
	escapeHtml: function(text) {
		var div = document.createElement('div');
		div.appendChild(document.createTextNode(text));
		return div.innerHTML;
	},
	
	/**
	 * Replaces all occurrences of a substring.
	 * 
	 * @param {string} src The string to modify.
	 * @param {string} sub The substring to replace.
	 * @param {string} repl The replacement string.
	 * @return {string} The modified string.
	 */
	replaceAll: function(src, sub, repl) {
		return src.split(sub).join(repl);
	},
	
	/**
	 * Combine sources into a single object
	 * 
	 * @param {object[]} sources Array of source objects.
	 * @param {function} filter Optional filter that returns true for each element
	 * 		to be copied.
	 * @return {object} An object that is the result of the merging of all source
	 * 		objects.
	 */
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
	
	/**
	 * Clone a DOM element to the specified depth.
	 * 
	 * @param {Node|jquery|Widget} element A DOM element, jquery object, or widget.
	 * @param {int} [depth=0] Depth of clone operation (0 means original element only
	 * 		< 0 means infinite depth).
	 * @param {Node} [parent] The parent for the cloned element.
	 * @return {Node} The cloned element.
	 */
	clone: function(element, depth, parent) {
		element = cwf.ele(element);
		var clone = element.cloneNode(false);
		depth = depth || 0;
		
		if (parent) {
			parent.appendChild(clone);
		}
		
		if (depth--) {
			var childNodes = element.childNodes;
			
			for (var i = 0, j = childNodes.length; i < j; i++) {
				cwf.clone(childNodes.item(i), depth, clone);
			}
		}
		
		return clone;
	},
	
	/**
	 * Converts an input string (or array of strings) to a map consisting of
	 * the split values as keys and "true" as values.
	 * 
	 * @param {string|string[]} value The string or string array value.
	 * @param {string} [dlm] The delimiter used for split operation (required if value is a string).
	 * @result {object} A map with split values as keys.
	 */
	stringToSet: function(value, dlm) {
		if (!_.isArray(value)) {
			value = value.split(dlm);
		}
		
		var result = {};
		
		_.forEach(value, function(entry) {
			if (entry) {
				result[entry] = true;
			}
		});
		
		return result;
	},
	
	/**
	 * Inserts an element into the array at the specified position.
	 * If the element is already in the array, it is moved to the
	 * specified position.
	 * 
	 * @param {array} array The array to be modified.
	 * @param {object} element The element to be inserted.
	 * @position {integer} position The position within the array where the
	 * 		element will be inserted or moved.
	 */
	insertIntoArray: function(array, element, position) {
		var i = array.indexOf(element),
			changed = false;
	
		if (i >= 0 && i === position) {
			return;
		}
		
		if (i >= 0) {
			array.splice(i, 1);
			changed = true;
		}
		
		if (position >= 0) {
			array.splice(position, 0, element);
			changed = true;
		}
		
		return changed;
	},
	
	/**
	 * Returns the input value, or the default value if the input is undefined or null.
	 */
	ensureValue: function(value, dflt) {
		return _.isNil(value) ? dflt : value;
	},
	
	saveToFile: function(content, mimetype, filename) {
		require(['file-saver'], function() {
			var blob = new Blob([content], {type: mimetype});
			saveAs(blob, filename);
		});
	}
		
}});  // end module definition