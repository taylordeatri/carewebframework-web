define(['core', 'bootstrap', 'spectrum', 'css!spectrum.css', 'css!jquery-ui.css', 'css!bootstrap-css.css', 'css!widget.css'], function(cwf) { 
	/* Widget support.  In the documentation, when we refer to 'widget' we mean an instance of the Widget
	 * class.  When we refer to 'widget$' (following the convention that a variable name ending in '$'
	 * is always a jquery object), we mean the jquery object contained by the widget.
	 */
	
	cwf.widget = {
		_zmodal: 999,
		
		/**
		 * Create a widget instance
		 * 
		 * @param wclass {string} The full name of the widget class.
		 * @param id {string} The element id to be assigned.  If not specified, a unique id is assigned.
		 * @param parent {selector} Selector for the parent element.  If not specified, the document body is assumed.
		 * @return {Widget} The newly created widget.
		 */
		create: function(wclass, id, parent) {
			var ctor = this[wclass];
			
			if (!ctor) {
				throw new Error('Unrecognized widget class: ' + wclass);
			}
			
			id = id || $('<div>').uniqueId().attr('id');
			parent = parent || 'body';
			return new ctor(wclass, id, parent);
		}
	};
		
	(function() {
		var initializing = false; 
		var fnTest = /xyz/.test(function(){xyz;}) ? /\b_super\b/ : /.*/;
		cwf.widget.Widget = function(){};
		  
		/**
		 * Simulates inheritance by copying methods and properties to
		 * the designated subclass prototype.
		 * 
		 * @param {object} The subclass.
		 */
		cwf.widget.Widget.extend = function(subclass) {
		    var _super = this.prototype;
		    initializing = true;
		    var prototype = new this();
		    initializing = false;
		   
		    for (var name in subclass) {
		    	prototype[name] = typeof subclass[name] === "function" 
		    		&& typeof _super[name] === "function" 
		    		&& fnTest.test(subclass[name]) ?
		    		(function(name, fn) {
		    			return function() {
		    				var tmp = this._super;
		    				this._super = _super[name];
		    				var ret = fn.apply(this, arguments);        
		    				this._super = tmp;
		    				return ret;
		    			};
		    		})(name, subclass[name]) : subclass[name];
		    }
		   
		    function Widget() {
		    	if (!initializing && this.init)
		    		this.init.apply(this, arguments);
		    }
		   
		    Widget.prototype = prototype;
		    Widget.prototype.constructor = Widget;
		    Widget.extend = arguments.callee;
		    return Widget;
		};
	})();
	
	/**
	 * Base component for all widgets.
	 */
	cwf.widget.BaseComponent = cwf.widget.Widget.extend({
		widget$: null,
		wclass: null,
		id: null,
		_rendering: null,
		_children: null,
		_parent: null,

		init: function(wclass, id, parent) {
			this._forwarding = {};
			this._state = {};
			this.wclass = wclass;
			this.id = id;
			this.rerender();
			this.toServer('propertyChange', true);
			
			if (parent) {
				var parent$ = cwf.$(parent);
				var w = parent$.cwf$widget();
				
				if (w) {
					w.addChild(this);
				} else {
					parent$.append(this.widget$);
				}
			}
		},
		
		rerender: function() {
			if (this._rendering) {
				return;
			}
			
			try {
				this._rendering = true;
				this._forEachChild(function(child){child._detach();});
				this._detach(true);
				
				this.widget$ = this.render$()
					.data('cwf_widget', this)
					.data('cwf_wclass', this.wclass);
				
				this.widget$.eq(0)
					.attr('id', this.id);
	
				this._applyState();
				this._forEachChild(function(child){child._attach(-1);});
				
				if (this._parent) {
					this._attach(this.getIndex());
				}
			} finally {
				this._rendering = false;
			}
		},

		/**
		 * @return The count of children belonging to this parent.
		 */
		getChildCount: function() {
			return this._children ? this._children.length : 0;
		},
		
		/**
		 * Returns the index position of the specified child widget within its parent widget.
		 * 
		 * @param {Widget} child The child widget.
		 * @return {number} The index of the child widget, or -1 if not found.
		 */
		getChildIndex: function(child) {
			return this._children ? this._children.indexOf(child) : -1;
		},
		
		/**
		 * Returns the index position of this child widget within its parent widget.
		 * 
		 * @return {number} The index of this child widget, or -1 if it has no parent widget.
		 */
		getIndex: function() {
			return this._parent ? this._parent.getChildIndex(this) : -1;
		},
		
		/**
		 * Invokes a callback on each child widget.
		 * 
		 * @param {callback} callback A callback function following the forEach convention.
		 */
		_forEachChild: function(callback) {
			if (this._children) {
				this._children.forEach(callback, this);
			}
		},
		
		
		/**
		 * Re-applies the saved state for a single or all entries.
		 * 
		 *  @param {string} key The name of the setter function to be invoked
		 *  	with arguments from its saved state.  If not specified, all
		 *  	saved state entries will be processed.
		 */
		_applyState: function(key) {
			var keys = key ? _.pick(this._state, key) : this._state,
				self = this;

			_.forOwn(keys, function(value, key) {
				var fn = self[key];
				
				if (!fn) {
					throw new Error('Unrecognized state for ' + self.wclass + ': ' + key);
				}

				fn.call(self, value);
			});
		},
		
		/**
		 * Updates the saved state for the specified function and arguments.
		 * 
		 * @param {string} key The name of the setter function.
		 * @param {arguments} value The value of the last setter invocation.
		 * @return {boolean} True if the state changed or if actively rendering.
		 */
		_updateState: function(key, value) {
			if (!_.isEqual(value, this._state[key])) {
				delete this._state[key];
				this._state[key] = value;
				return true;
			}
			
			return this._rendering;
		},

		/**
		 * Returns the anchor point for child widgets.  Non-container widgets
		 * should return null.
		 * 
		 * @return {jquery}
		 */
		anchor$: function() {
			return null;
		},
		
		/**
		 * Returns the jquery object representing the rendered DOM for
		 * this widget.
		 */
		render$: function() {
			throw new Error('No rendering logic supplied for ' + this.wclass);
		},
		
		/**
		 * Convenience method for resolving embedded EL references.
		 * 
		 * @param {string} v Value containing EL references.
		 * @return {string} Input value with EL references resolved.
		 */
		resolveEL: function(v) {
			return cwf.resolveEL(this, v);
		},
		
		/**
		 * Convenience method for setting an attribute value.
		 * 
		 * @param {string} attr The attribute name.
		 * @param value The new attribute value.
		 * @param {jquery} tgt$ The jquery object to receive the new attribute value.  If not
		 * 	specified, this widget's widget$ object will be used.
		 */
		attr: function(attr, value, tgt$) {
			tgt$ = tgt$ || this.widget$;
			tgt$.cwf$attr(attr, value);
		},
		
		addChild: function(child, index) {
			if (!this._children) {
				throw new Error('Not a container widget: ' + this.wclass)
			}
			
			child = cwf.wgt(child);
			
			if (!child) {
				throw new Error('Child is not a valid widget.');
			}
			
			var currentIndex = child.getIndex();
			index = typeof index === 'undefined' || index < -1 ? -1 : index;
			
			if (currentIndex >= 0 && currentIndex === index) {
				return;
			}
			
			var maxIndex = currentIndex >= 0 ? this._children.length - 1 : this._children.length;
			
			if (index > maxIndex) {
				throw new Error('Index out of range: ' + index);
			} else if (index === maxIndex) {
				index = -1;
			}
			
			if (currentIndex >= 0) {
				this._children.splice(currentIndex, 1);
				child._detach();
			}
			
			this._children.splice(index, 0, child);
			child._parent = this;
			child._attach(index);
			this.onAddChild(child);
		},
		
		onAddChild: function(child) {
			// Does nothing by default
		},
		
		removeChild: function(child, destroy) {
			var currentIndex = child.getIndex();
			
			if (currentIndex >= 0) {
				this._children.splice(currentIndex, 1);
				child._detach(destroy);
				this.onRemoveChild(child, destroy);
				return true;
			}
		},
		
		onRemoveChild: function(child, destroyed) {
			// Does nothing by default
		},
		
		_attach: function(index) {
			this._attachWidgetAt(this.widget$, this._parent.anchor$(), this._parent._children[index])
		},
		
		/**
		 * Attaches a widget to the DOM at the specified reference point.
		 * 
		 * @param {jquery} widget$ The widget to attach.
		 * @param {jquery} parent$ The widget to become the parent (if nil, must specify <code>ref</code>).
		 * @param {number | jq | Widget} ref The reference point for insertion (if nil, must specify <code>parent$</code>).
		 */
		_attachWidgetAt: function(widget$, parent$, ref) {
			var ref$ = _.isNil(ref) ? null 
					: _.isObject(ref) ? cwf.$(ref) 
					: ref < 0 ? null 
					: parent$.children().eq(ref)[0];
			
			if (ref$) {
				ref$.before(widget$);
			} else {
				parent$.append(widget$);
			}
		},
		
		/**
		 * Detaches or removes the associated widget$.
		 * 
		 * @param {boolean} destroy If true, the widget$ is removed from the DOM and set to null.
		 * 		If false, the widget$ is just detached from the DOM and may be reattached later.
		 */
		_detach: function(destroy) {
			if (this.widget$) {
				if (destroy) {
					this.widget$.remove();
					this.widget$ = null;
				} else {
					this.widget$.detach();
				}
			}
		},
		
		/**
		 * Removes this widget from its parent widget and destroys the associated widget$.
		 */
		destroy: function() {
			if (this._parent) {
				this._parent.removeChild(this, true);
			} else {
				this._detach(true);
			}
		},
		
		/**
		 * Returns the identifier for the specified subcomponent.
		 * 
		 * @param {string} sub The subcomponent name.
		 * @return {string} The subcomponent's identifier.
		 */
		subId: function(sub) {
			return this.id + '-' + sub;
		},
		
		/**
		 * Returns the subcomponent for the specified identifier.
		 * 
		 * @param {string} sub The subcomponent name.
		 * @return {jquery} The subcomponent (never null).
		 */
		sub$: function(sub) {
			var id = '#' + this.subId(sub);
			sub = this.widget$ ? this.widget$.find(id) : null;
			return sub && sub.length > 0 ? sub : $(id);
		},
		
		/**
		 * Returns the page to which this widget belongs.
		 * 
		 * @return {Page} The owning page.
		 */
		page: function() {
			var page = this.page$();
			return page ? page.cwf$widget() : null;
		},
		
		page$: function() {
			var pcs = this.id.split('_');
			return $('#cwf_' + pcs[1]);
		},
		
		/**
		 * Turns on/off server forwarding of an event type.
		 * 
		 * @param eventNames {string} The event's name (e.g., 'click').
		 * @param forward {boolean} If true, the event will be forwarded to the server.
		 * 		If false, forwarding will not occur.
		 */
		toServer: function(eventNames, forward) {
			eventNames = eventNames.split(' ');
			
			for (var i in eventNames) {
				var eventName = eventNames[i];
				
				if (this._forwarding[eventName] !== forward) {
					if (forward) {
						this.widget$.on(eventName, this._serverHandler);
					} else {
						this.widget$.off(eventName, this._serverHandler);
					}
					this._forwarding[eventName] = forward;
				}
			}
		},
		
		/**
		 * Establishing forwarding of an event from its source to this widget.
		 * 
		 * @param source$ {jquery} The source of the event.
		 * @param sourceEvent The name of the source event.
		 * @param forwardEvent The name of the forwarded event (defaults to
		 * 	same as sourceEvent).
		 */
		toWidget: function(source$, sourceEvent, forwardEvent) {
			var widget$ = this.widget$;
			
			source$.on(sourceEvent, function (event) {
				var forward = $.extend({}, event);
				forward.type = forwardEvent || sourceEvent;
				widget$.trigger(forward);
			})
		},
		
		/**
		 * Handler for forwarding events to the server.
		 */
		_serverHandler: function(event, data) {
			if (data) {
				event.data = data;
			}
			
			cwf.event.sendToServer(event);
			event.stopPropagation();
		},
		
		_propertyChange: function(property, value) {
			this.widget$.trigger('propertyChange', {property: property, value: value});
		},
		
		/**
		 * Moves the widget to the parking lot (hidden div).
		 */
		park: function(v) {
			v = v || this.widget$;
			this.page().sub$('parked').append(v);
		},
		
		/**
		 * Creates a translucent mask over this widget.
		 */
		mask: function(option) {
			var id = this.widget$.attr('id') + '-mask';
			this.widget$.cwf$mask(option, id);
		},
		
		name: function(v) {
			if (this._updateState('name', v)) {
				this.attr('cwf_name', v);
			}
		},
		
		content: function(v) {
			if (!this._updateState('content', v)) {
				return;
			}
			
			var span$ = this.sub$('content');
			
			if (!v) {
				span$.remove();
			} else {
				if (span$.length == 0) {
					var dom = this.resolveEL('<span id="${id}-content"/>');
					span$ = $(dom).appendTo(this.widget$);
				}
				
				span$.text(v);
			}
		}

	});
	
	cwf.widget.NonUIComponent = cwf.widget.BaseComponent.extend({
		real$: null,
		_anchor: null,
		
		init: function(wclass, id, parent) {
			this.real$ = this.renderReal$();
			this._super(wclass, id, parent);
			
			if (this.real$) {
				this.real$
					.appendTo(this._anchor)
					.attr('id', this.subId('real'));
			}
		},
		
		_detach: function(destroy) {
			if (this.real$) {
				this.real$[destroy ? 'remove' : 'detach']();
			}
			
			this._super(destroy);
		},
	
		renderReal$: function() {
			return null;
		},
		
		render$: function() {
			return $('<span>');
		}
	});
	
	cwf.widget.UiComponent = cwf.widget.BaseComponent.extend({
		init: function(wclass, id, parent) {
			this._super(wclass, id, parent);
			this.widget$.addClass('cwf_' + wclass.toLowerCase())
		},
		
		style: function(v) {
			if (this._updateState('style', v)) {
				this.attr('style', v);
			}
		},
		
		css: function(n, v) {
			this.widget$.css(n, v);
			this._updateState('style', this.widget$.attr('style'));
		},
		
		clazz: function(v) {
			if (this._updateState('clazz', v)) {
				this.attr('class', v);
			}
		},
		
		visible: function(v) {
			if (this._updateState('visible', v)) {
				this.widget$[v ? 'show' : 'hide']();
			}
		},
		
		focus: function(v) {
			if (this._updateState('focus', v)) {
				this.widget$[v ? 'focus' : 'blur']();
			}
		},
		
		hint: function(v) {
			if (this._updateState('hint', v)) {
				this.attr('title', v);
			}
		},
		
		disabled: function(v) {
			if (this._updateState('disabled', v)) {
				this.attr('disabled', v);
			}
		},
		
		tabindex: function(v) {
			if (this._updateState('tabindex', v)) {
				this.attr('tabindex', v);
			}
		}
	});
	
	cwf.widget.ContainerComponent = cwf.widget.UiComponent.extend({
		_children: [],

		anchor$: function() {
			return this.widget$;
		}
	});

	cwf.widget.InputComponent = cwf.widget.UiComponent.extend({
		placeholder: function(v) {
			if (this._updateState('placeholder', v)) {
				this.attr('placeholder', v);
			}
		},
		
		maxlength: function(v) {
			if (this._updateState('maxlength', v)) {
				this.attr('maxlength', v);
			}
		}
	});

	cwf.widget.Style = cwf.widget.NonUIComponent.extend({
		_anchor: 'head',
		
		content: function(v) {
			if (this._updateState('content', v)) {
				this.real$.text(v);
			}
		},
		
		renderReal$: function() {
			return $('<style>');
		}
	});
	
	cwf.widget.Stylesheet = cwf.widget.NonUIComponent.extend({
		_anchor: 'head',
		
		href: function(v) {
			if (this._updateState('href', v)) {
				this.attr('href', v, this.real$);
			}
		},
		
		renderReal$: function() {
			return $('<link type="text/css" rel="stylesheet">');
		}
	});
	
	cwf.widget.Script = cwf.widget.NonUIComponent.extend({
		_anchor: 'body',
		
		content: function(v) {
			if (this._updateState('content', v)) {
				this.real$.text(v);
			}
		},
		
		type: function(v) {
			if (this._updateState('type', v)) {
				this.attr('type', v, this.real$);
			}
		},
		
		src: function(v) {
			if (this._updateState('src', v)) {
				this.attr('src', v, this.real$);
			}
		},
		
		renderReal$: function() {
			return $('<script>');
		}
	});
	
	cwf.widget.Timer = cwf.widget.BaseComponent.extend({
		_timer: null,
		_interval: 0,
		_repeat: true,
		
		_detach: function(destroy) {
			this.stop();
			this._super(destroy);
		},
		
		start: function() {
			if (!this._timer && this._interval > 0) {
				var self = this;
				this._timer = setInterval(function () {
					self._trigger();
				}, this._interval);
				return true;
			}
		},
		
		_trigger: function() {
			if (!cwf.ws.isConnected()) {
				this.stop();
				return;
			}
			
			if (!this._repeat) {
				this.stop();
				this._propertyChange('running', false);
			}
			
			this.widget$.trigger('timer');
		},
		
		stop: function() {
			if (this._timer) {
				clearInterval(this._timer);
				this._timer = null;
				return true;
			}
		},
		
		interval: function(v) {
			if (v !== this._interval) {
				this._interval = v;
				
				if (this.stop()) {
					this.start();
				}
			}
		},
		
		repeat: function(v) {
			this._repeat = v;
		},
		
		running: function(v) {
			this[v ? 'start' : 'stop']();
		},
		
		render$: function() {
			return $('<span>');
		}
		
	});
	
	cwf.widget.Html = cwf.widget.UiComponent.extend( {
		render$: function() {
			return $('<span>');
		},
		
		content: function(v) {
			if (this._updateState('content', v)) {
				this.widget$.children().remove();
				this.widget$.append(cwf.$(v));
			}
		}
	});
	
	cwf.widget.Div = cwf.widget.ContainerComponent.extend({
		render$: function() {
			return $('<div>');
		}
	});

	cwf.widget.Span = cwf.widget.ContainerComponent.extend({
		render$: function() {
			return $('<span>');
		}
	});

	cwf.widget.Toolbar = cwf.widget.ContainerComponent.extend({
		render$: function() {
			return $('<div class="btn-toolbar">');
		}
	});
	
	cwf.widget.Page = cwf.widget.ContainerComponent.extend({
		render$: function() {
			return $(this.resolveEL('<div><div id="${id}-parked" style="display:none"/></div>'));
		},
		
		title: function(v) {
			if (this._updateState('title', v)) {
				$('head>title').text(v);
			}
		}
	});

	cwf.widget.LabeledComponent = cwf.widget.UiComponent.extend({
		label: function(v) {
			if (this._updateState('label', v)) {
				this.widget$.text(v);
			}
		}
	});
	
	cwf.widget.Button = cwf.widget.LabeledComponent.extend({
		render$: function() {
			return $('<button class="btn btn-sm btn-success">');
		}
	});
	
	cwf.widget.Hyperlink = cwf.widget.LabeledComponent.extend({
		render$: function() {
			return $('<a class="btn-link" href="javascript:">');
		},

		href: function(v) {
			v = v || 'javascript:';
			
			if (this._updateState('href', v)) {
				this.attr('href', v);
			}
		},
		
		target: function(v) {
			if (this._updateState('target', v)) {
				this.attr('target', v);
			}
		}
	});
	
	cwf.widget.Label = cwf.widget.LabeledComponent.extend({
		render$: function() {
			return $('<label class="label-default">');
		}
	});
	
	cwf.widget.Menu = cwf.widget.ContainerComponent.extend({
		label: function(v) {
			if (this._updateState('label', v)) {
				this.sub$('lbl').text(v);
			}
		},
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		render$: function() {
			var dom = 
				  '<div class="dropdown">'
				+ '  <button id="${id}-btn" class="btn btn-default dropdown-toggle" type="button"'
				+ '    data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">'
				+ '    <span id="${id}-lbl"></span>'
				+ '    <span class="caret"></span>'
				+ '  </button>'
				+ '  <ul id="${id}-inner" class="dropdown-menu" aria-labelledby="${id}-btn" />'
				+ '</div>';
			return $(this.resolveEL(dom));
		}
	});
	
	cwf.widget.Menuitem = cwf.widget.UiComponent.extend({
		_type: null,
		
		label: function(v) {
			if (this._updateState('label', v)) {
				this.sub$('lbl').text(v);
			}
		},
		
		type: function(v) {
			if (this._updateState('type', v)) {
				this._type = v;
				this.rerender();
			}
		},
		
		render$: function() {
			var dom;
			
			switch(this._type) {
				case 'SEPARATOR':
					dom = '<li class="separator" role="divider"></li>';
					break;
					
				case 'HEADER': {
					dom = '<li class="dropdown-header"><span id="${id}-lbl"/></li>';
					break;
				}
				
				default:
					dom = '<li><a id="${id}-lbl" tabindex="-1" href="javascript:"></a></li>';
			}
			
			return $(this.resolveEL(dom));
		}
	});
	
	cwf.widget.Image = cwf.widget.UiComponent.extend({
		render$: function() {
			return $('<img>');
		},
		
		src: function(v) {
			if (this._updateState('src', v)) {
				this.attr('src', v);
			}
		},
		
		alt: function(v) {
			if (this._updateState('alt', v)) {
				this.attr('alt', v);
			}
		}
	});
	
	cwf.widget.Textbox = cwf.widget.InputComponent.extend({
		render$: function() {
			return $('<input type="text">');
		}
	});
	
	cwf.widget.Iframe = cwf.widget.UiComponent.extend({
		render$: function() {
			return $('<iframe>');
		},
		
		src: function(v) {
			if (this._updateState('src', v)) {
				this.attr('src', v);
			}
		},
		
		sandbox: function(v) {
			if (this._updateState('sandbox', v)) {
				this.attr('sandbox', v);
			}
		}
	});
	
	cwf.widget.Progressbar = cwf.widget.UiComponent.extend({
		_value: 0,
		_max: 100,
		
		render$: function() {
			var w$ = $('<div><div/><div/></div>');
			this._adjust(w$);
			return w$;
		},
		
		value: function(v) {
			if (this._updateState('value', v)) {
				this._value = v;
				this._adjust();
			}
		},
		
		max: function(v) {
			if (this._updateState('max', v)) {
				this._max = v;
				this._adjust();
			}
		},
		
		label: function(v) {
			if (this._updateState('label', v)) {
				this.widget$.children().first().text(v);
			}
		},
		
		_pct: function() {
			var value = this._value || 0;
			var max = this._max || 100;
			var pct = max <= 0 ? 0 : value / max * 100;
			return pct > 100 ? 100 : pct;
		},
		
		_adjust: function(v) {
			v = v || this.widget$;
			v.children().last().width(this._pct() + '%');
		}
	});
	
	cwf.widget.Colorpicker = cwf.widget.UiComponent.extend({
		value: function(v) {
			if (this._updateState('value', v)) {
				this.widget$.find('input').spectrum('set', v);
				//this._spectrum('option', 'color', v);
			}
		},
		
		_spectrum: function() {
			this.widget$.find('input').spectrum(arguments);
		},
		
		render$: function() {
			var w$ = $('<span><input type="text"></span>'),
				self = this;
			
			w$.find('input').spectrum({
				allowEmpty: true,
				showInput: true,
				showInitial: true,
				className: 'cwf-spectrum',
				preferredFormat: 'name',
				change: function(v) {
					self._updateState('value', v.toName());
				}
			});
			
			return w$;
		}
	}),
	
	cwf.widget.Panel = cwf.widget.ContainerComponent.extend({
		_closable: false,
		
		render$: function() {
			var dom =
				  '<div class="cwf_titled panel panel-default">'
				+ '  <div class="panel-heading">'
				+ '    <div class="panel-title">'
				+ '      <span id="${id}-title"/>'
				+ '      <span id="${id}-icons" class="cwf_titled-icons"/>'
				+ '    </div>'
				+ '  </div>'
				+ '  <div id="${id}-inner" class="panel-body"/>'
				+ '</div>';
			return $(this.resolveEL(dom));
		},
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		title: function(v) {
			if (this._updateState('title', v)) {
				this.sub$('title').text(v);
			}
		},
		
		closable: function(v) {
			if (this._updateState('closable', v)) {
				this._closable = v;
				this[v ? '_addButton' : '_removeButton']('close', 'remove', 9999);
			}
		},
		
		sizable: function(v) {
			if (this._updateState('sizable', v)) {
				this._sizable = v;
			}
		},
		
		_getButtonId: function(type) {
			return this.id + '-' + type;
		},
		
		_addButton: function(type, icons, position, handler) {
			var id = this._getButtonId(type);
			var btn = $('#' + id);
			
			if (btn.length) {
				return btn;
			}
			
			icons = icons.split(' ');
			btn = $('<a class="glyphicon glyphicon-' + icons[0] + '"/>')
				.attr('id', id)
				.data('position', position)
				.data('icons', icons)
				.data('state', 0)
				.data('context', this);
			var icons = this.sub$('icons');
			icons.append(btn);
			icons.children().sort(function(a, b) {
				a = $(a), b = $(b);
				var x = a.data('position') - b.data('position');
				
				if (x > 0) {
					a.before(b);
				}
				
				return x;
			});
			
			if (handler) {
				btn.on('click', handler);
			}
			
			this.toWidget(btn, 'click', type);
			return btn;
		},
		
		_removeButton: function(type) {
			$('#' + this._getButtonId(type)).remove();
		},
		
		_toggleButton: function(type) {
			var btn = $('#' + this._getButtonId(type))
			var icons = btn.data('icons');
			var oldState = btn.data('state');
			var newState = (oldState + 1) % icons.length;
			btn.data('state', newState).removeClass('glyphicon-' + icons[oldState]).addClass('glyphicon-' + icons[newState]);
			return newState;
		}
	});
	
	cwf.widget.Window = cwf.widget.Panel.extend({
		_maximizable: false,
		_minimizable: false,
		_sizable: false,
		_modal: false,
		_autoClose: false,
		_maximized: false,
		_minimized: false,
		
		render$: function() {
			var w = this._super();

			w.draggable({
				containment: 'window',
				handle: w.find('.panel-heading')})
			.position({
				my: 'center',
				at: 'center',
				of: 'body'})
			.resizable({
				handles: 'all',
				alsoResize: w.find('.panel-body')});
			
			return w;
		},
		
		minimizable: function(v) {
			if (this._updateState('minimizable', v)) {
				this._minimizable = v;
				this[v ? '_addButton' : '_removeButton']('minimize', 'chevron-down chevron-up', 20, this._onminimize);
			}
		},
		
		maximizable: function(v) {
			if (this._updateState('maximizable', v)) {
				this._maximizable = v;
				this[v ? '_addButton' : '_removeButton']('maximize', 'resize-full resize-small', 10, this._onmaximize);
			}
		},
		
		sizable: function(v) {
			if (this._updateState('sizable', v)) {
				this._sizable = v;
			}
		},
		
		state: function(v) {
			v = v || 'normal';
			v = v.toLowerCase();
			
			if (~this.states.indexOf(v) && this._updateState('state', v)) {
				this._state = v;
				this._updateState();
			}
		},
		
		modal: function(v) {
			if (this._updateState('modal', v)) {
				this._modal = v;
				this.widget$[v ? 'addClass' : 'removeClass']('cwf-window-modal');
			}
		},
		
		_onmaximize: function(event) {
			var context = $(event.target).data('context');
			event.state = context._toggleButton('maximize');
		}, 
		
		_onminimize: function(event) {
			var context = $(event.target).data('context');
			event.state = context._toggleButton('minimize');
		}
	});
	
	cwf.widget.Alert = cwf.widget.Window.extend({
		text: function(v) {
			if (this._updateState('text', v)) {
				this.widget$.find('.panel-body').text(v);
			}
		}
	});
	
	cwf.widget.Tabview = cwf.widget.ContainerComponent.extend({
		_activeTab: null,
		
		render$: function() {
			var dom = 
				  '<div>'
				+ '  <ul id="${id}-tabs" class="nav nav-tabs"/>'
				+ '  <div id ="${id}-panes" class="cwf_tabview-panes"/>'
				+ '</div>';
			return $(this.resolveEL(dom));
		},
		
		anchor$: function() {
			return this.sub$('tabs');
		},
		
		onRemoveChild: function(child) {
			if (child === this._activeTab) {
				this._activeTab = null;
			}
		},
		
		activeTab: function(tab) {
			if (this._activeTab) {
				this._activeTab.active(false);
			}

			if (tab) {
				this._activeTab = cwf.$(tab).cwf$widget();
				this._activeTab.active(true);
			} else {
				this._activeTab = null;
			}
		}
	});
	
	cwf.widget.Tab = cwf.widget.ContainerComponent.extend({
		_closable: false,
		
		init: function(wclass, id, parent) {
			this._super(wclass, id, parent);
			this.toWidget(this.sub$('tab'), 'click', 'select');
		},
		
		render$: function() {
			var dom = 
				  '<li role="presentation">'
				+ '  <a id="${id}-tab" href="javascript:">'
				+ '    <span/>'
				+ '  </a>'
				+ '</li>'
				+ '<div id="${id}-pane" style="display:none"/>';
			
			return $(this.resolveEL(dom));
		},
		
		anchor$: function() {
			return this.sub$('pane');
		},
		
		_attach: function(index) {
			this._super(index);
			this.sub$('pane').appendTo(this._parent.sub$('panes'));
		},
		
		active: function(v) {
			if (this._updateState('active', v)) {
				this.widget$[v ? 'addClass' : 'removeClass']('active');
				this.sub$('pane')[v ? 'show' : 'hide']();
			}
		},
		
		park: function(v) {
			this._super(v);
			this.active(false);
		},
		
		label: function(v) {
			if (this._updateState('label', v)) {
				this.sub$('tab').first().text(v);
			}
		},
		
		closable: function(v) {
			if (this._updateState('closable', v)) {
				this._closable = v;
				
				if (v) {
					var btn = $('<span class="glyphicon glyphicon-remove"/>').appendTo(this.sub$('tab'));
					this.toWidget(btn, 'click', 'close');
				} else {
					this.sub$('tab').find('glyphicon-remove').remove();
				}
			}
		}
	});
	
	return {};
});