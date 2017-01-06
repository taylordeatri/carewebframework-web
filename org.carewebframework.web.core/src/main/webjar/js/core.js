'use strict';

define('cwf-core', ['jquery', 'jquery-ui', 'lodash'], function($) {
	var cwf;
	return cwf = {
	
	/*------------------------------ Initialization ------------------------------*/
			
	init: function(options) {
		window.onerror = function(error) {
			cwf.fatal(error);
		};
		
		window.onbeforeunload = function(event) {
			return cwf._canClose ? undefined : event.returnValue = true;
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
				cwf.fatal(e);
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
			cwf.log.debug('Processing: ', action);
			
			if (!action.fcn) {
				return;
			}
		
			var tgt = action.tgt;
		
			if (tgt) {
				if (tgt === 'dead') {
					return;
				}
			
				var i = tgt.indexOf('-'),
					sub = i == -1 ? null : tgt.substring(i + 1);
				
				tgt = cwf.widget.find(i > 0 ? tgt.substring(0, i) : tgt);
			
				if (!tgt) {
					throw new Error('No target matching ' + action.tgt);
				}
				
				tgt = sub ? tgt.sub$(sub) : tgt;
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
			var ele$ = this,
				wgt;

			while (!wgt && ele$.length) {
				wgt = ele$.data('cwf_widget');	
				ele$ = wgt ? null : ele$.parent();
			}
			 
			return wgt;
		},
		
		/**
		 * Notifies the caller (via move events) when the source element moves.  
		 * Note this uses background polling, so should be used sparingly.
		 */
		cwf$track: function(src$, untrack) {
			var lst = src$.data('cwf$track.ele') || [],
				poll = src$.data('cwf$track.poll');
			
			this.each(function() {
				var i = lst.indexOf(this);
				
				if (i == -1) {
					untrack ? null : lst.push(this);
				} else {
					untrack ? lst.splice(i, 1) : null;
				}
			});
			
			if (!lst.length) {
				if (poll) {
					clearInterval(poll);
					src$.data('cwf$track.poll', null);
				}
				
				src$.data('cwf$track.ele', null);
			} else if (!poll) {
				src$.data('cwf$track.ele', lst);
				src$.data('cwf$track.pos', src$.offset());
				poll = setInterval(_poll, 500);
				src$.data('cwf$track.poll', poll);
			}
			
			return this;
			
			function _poll() {
				var lastpos = src$.data('cwf$track.pos'),
					currpos = src$.offset();
				
				if (lastpos.left !== currpos.left || lastpos.top !== currpos.top) {
					lastpos.left = currpos.left;
					lastpos.top = currpos.top;
					var event = $.Event('move', {
						relatedTarget: src$, 
						position: currpos,
						pageX: currpos.left,
						pageY: currpos.top});
					$(src$.data('cwf$track.ele')).trigger(event);
				}
			}
		},
		
		cwf$attr: function(name, value) {
			return _.isNil(value) || value === '' ? this.removeAttr(name): this.attr(name, value);
		},
		
		cwf$show: function(visible) {
			return visible ? this.show() : this.hide();
		},
		
		cwf$on: function(name, handler) {
			return this.off(name).on(name, handler);
		},
		
		cwf$removeSubclass: function(base, sub) {
			if (_.isArray(sub)) {
				sub = sub.join(' ');
			}
			
			sub = sub.split(' ');
			
			for (var i in sub) {
				this.removeClass(base + sub[i]);
			}
			
			return this;
		},
		
		cwf$swapClasses: function(trueClass, falseClass, value) {
			this.removeClass(value ? falseClass : trueClass);
			this.addClass(value ? trueClass : falseClass);
			return this;
		},
		
		cwf$mask: function(zindex) {
			this.css('position', 'relative');
			
			return $('<div class="cwf_mask"/>')
				.css('z-index', zindex)
				.prependTo(this);
		},
		
		cwf$zindex: function() {
			var ele = this[0],
				zindex = null;
			
			while (ele && !_.isFinite(zindex)) {
				zindex = +getComputedStyle(ele).zIndex;
				ele = ele.parentElement;
			}
			
			return _.isFinite(zindex) ? zindex : 'auto';
		}
	},
	
	/*------------------------------ Websocket Support ------------------------------*/
	
	ws: {
		_init: function(wsurl) {
			this.socket = new WebSocket(wsurl);
			this.socket.onerror = this._onerror;
			this.socket.onmessage = this._onmessage;
			this.socket.onopen = this._onopen;
			this.socket.binaryType = 'blob';
		},
		
		_onopen: function() {
			var data = {
				requestURL: window.location.href,
				viewportHeight: $(window).height(),
				viewportWidth: $(window).width(),
				timezoneOffset: new Date().getTimezoneOffset()
			};
			
			cwf.flatten(screen, data, 'screen', 1);
			cwf.flatten(navigator, data, 'browser', 1);
			cwf.ws.sendData('init', data);
		},
	
		_onmessage: function(message) {
			var action = JSON.parse(message.data);
			cwf.log.debug('Received: ', action);
			cwf.action.queueAction(action);
		},
		
		_onerror: function(event) {
			cwf.log.error(event);
		},
		
		isConnected: function() {
			return this.socket && this.socket.readyState === WebSocket.OPEN;
		},
		
		sendData: function(type, data, nolog) {
			if (!this.isConnected()) {
				throw new Error('Communication with the server has been interrupted.');
			}
			
			var pkt = {type: type, pid: cwf.pid, data: data};
			
			if (data.blob) {
				var blob = data.blob;
				delete data.blob;
				this.socket.send(new Blob([JSON.stringify(pkt), '\n', blob]));
			} else {
				this.socket.send(JSON.stringify(pkt));
			}
			
			if (!nolog) {
				cwf.log.debug('Sent: ', pkt);
			}
		}
	},
	
	/*------------------------------ Event Support ------------------------------*/
		
	event: {
		postProcessors: {},
		
		_init: function() {
		    $('body').on('contextmenu', function(event) {
		    	cwf.debug ? null : event.preventDefault();
		    });
		    
		    $('body').on('keydown', function(event) {
		    	if (event.keyCode === 8) {
		    		var tp = event.srcElement || event.target;
		    		tp = tp.tagName.toLowerCase();

		    		if (tp !== 'input' && tp !== 'textarea')
		    			event.preventDefault();
		    	}
		    });
			
			this.registerPostProcessor('change', _ppChangeInput);
			this.registerPostProcessor('input', _ppChangeInput);
			this.registerPostProcessor('resize', _ppResize);
			
			function _ppChangeInput(event, data) {
				data.value = _.defaultTo(data.value, event.target.value);
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
			fn ? fn.apply(this, arguments) : null;
		},
		
		forward: function(event, type, target) {
			var target$ = cwf.$(target || event.target);
			event.type = type || event.type;
			target$ ? target$.triggerHandler(event) : null;
		},
		
		stop: function(event) {
			event.stopPropagation ? event.stopPropagation() : null;
			event.preventDefault ? event.preventDefault(): null;
			return event;
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

		toKeyCapture: function(event) {
			var value = '';
			value += event.ctrlKey ? '^' : '';
			value += event.altKey ? '@' : '';
			value += event.metaKey ? '~' : '';
			value += event.shiftKey ? '$' : '';
			value += "#" + event.keyCode;
			return value;
		},
		
		/**
		 * Event handler for constraining input.
		 */
		constrainInput: function(constraint, keyPressEvent) {
			var key = String.fromCharCode(keyPressEvent.which),
				test = constraint.test ? constraint.test(key) : constraint(key);
			
			test ? null : cwf.event.stop(keyPressEvent);
		},
		
		sendToServer: function(event, params) {
			var orig = event.originalEvent || event;
			
			if (orig.cwf_sent) {
				return;
			}
			
			var pkt = {};
			params ? _.assign(event, params) : null;
			
			_.forIn(event, function(value, pname) {
				value = pname === 'data' || pname === 'blob' || !_.isObject(value) ? value : cwf.id(value);
				
				if (value !== null) {
					pkt[pname] = value;
				}
			});
			
			orig.cwf_sent = true;
			cwf.event._postprocess(event, pkt);
			cwf.ws.sendData('event', pkt);
		}
	},
	
	/*------------------------------ Widget Support ------------------------------*/
	
	widget: {
		_registry: {},
		
		_zmodal: 999,
		
		_popup: null,
		
		_init: function() {
		    $.widget.bridge('uitooltip', $.ui.tooltip);
		    $.widget.bridge('uibutton', $.ui.button);
			$('body').on('click', function() {
				cwf.widget._popup ? cwf.widget._popup.close() : null;
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
		 * Send log message to console and/or to server.
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
		return _.isNil(object) ? null 
			: object.jquery ? object 
			: object.__cwf__ ? cwf.$(cwf.widget.find(object.__cwf__))
			: cwf.widget.isWidget(object) ? object.widget$ 
			: $(object);
	},
	
	/**
	 * Returns the widget associated with the object.
	 */
	wgt: function(object) {
		return _.isNil(object) ? null 
			: object.__cwf__ ? cwf.widget.find(object.__cwf__)
			: cwf.widget.isWidget(object) ? object 
			: this.$(object).cwf$widget();
	},	
	
	/**
	 * Returns the id associated with the object.
	 */
	id: function(object) {
		return _.isNil(object) ? null 
			: object.jquery ? object.attr('id') 
			: object.__cwf__ ? object.__cwf__
			: object.id;
	},
	
	/**
	 * Returns the DOM element associated with the object.
	 */
	ele: function(object) {
		return _.isNil(object) ? null 
			: _.isElement(object) ? object 
			: cwf.$(object)[0];
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
	 * Handler for fatal exceptions.
	 */
	fatal: function(error) {
		if (!(error instanceof Error)) {
			error = new Error(error);
		}
		
		var message = 'Fatal error:\n\n' + (error.stack ? error.stack : error);
		
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
		
		return cwf.widget.create(null, props, state);
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
	 * Performs an eval operation in the cwf context.
	 */
	eval: function(value) {
		return eval(value);
	},
	
	/**
	 * Return a unique identifier.
	 */
	uniqueId: function() {
		return _.uniqueId('_cwf__');
	},
	
	/**
	 * Resolve a hierarchical reference relative to the specified base.
	 */
	resolveReference: function(base, path) {
		var i = 0;
		path = path === null ? [] : path.split('.');
		
		if (!base) {
			base = eval(path[i++]);
		}
		
		var ref = base;
		
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
	 * Recursively copies members from source into destination in flattened form.
	 * 
	 * @param {object} source Source object to copy.
	 * @param {object} dest Destination of copy operation.
	 * @param {String} [prefix] Prefix to be prepended to key when writing to destination.
	 * @param {int} [maxdepth] Maximum recursion depth.  Defaults to no recursion.
	 */
	flatten: function(source, dest, prefix, maxdepth) {
		maxdepth = maxdepth ? maxdepth : 0;
		
		_.forIn(source, function(value, key) {
			if (!_.isNil(value) && !_.isFunction(value)) {
				key = _.isArray(source) ? '[' + key + ']' : key;
				key = prefix ? (prefix + _.upperFirst(key)) : key;
				
				if (_.isObject(value)) {
					maxdepth <= 0 ? null : cwf.flatten(value, dest, key, maxdepth - 1);
				} else {
					dest[key] = value;
				}
			}
		});
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
		clone.id = '';
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
	 * @return {object} A map with split values as keys.
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
	 * Converts a set to a delimited string.
	 * 
	 * @param {object} set Set whose values are to be concatenated.
	 * @param {string} dlm Delimiter for separating values.
	 * @return {string} The concatenated list of set values.
	 */
	setToString: function(set, dlm) {
		return _.keys(set).join(dlm);
	},
	
	/**
	 * Removes a set of values from another set.
	 * 
	 * @param {object} set Set whose values are to be removed.
	 * @param {object} values Set with values to be removed.
	 */
	removeFromSet: function(set, values) {
		_.forOwn(values, function(x, key) {
			delete set[key];
		});
		
		return set;
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
	
	saveToFile: function(content, mimetype, filename) {
		mimetype = !mimetype || navigator.userAgent.match(/Version\/[\d\.]+.*Safari/) ? 'application/octet-stream' : mimetype;
		require(['file-saver'], function() {
			var blob = new Blob([content], {type: mimetype});
			saveAs(blob, filename);
		});
	}
	
		
}});  // end module definition