'use strict';

define('cwf-widget', ['cwf-core', 'bootstrap', 'css!balloon-css.css', 'css!jquery-ui.css', 'css!bootstrap-css.css', 'css!cwf-widget-css.css'], function(cwf) { 
	/* Widget support.  In the documentation, when we refer to 'widget' we mean an instance of the Widget
	 * class.  When we refer to 'widget$' (following the convention that a variable name ending in '$'
	 * is always a jquery object), we mean the jquery object contained by the widget.
	 */
	
	cwf.widget.fnTest = /xyz/.test(function(){xyz;}) ? /\b_super\b/ : /.*/;
	
	cwf.widget._domTemplates = {
			checkable: '<span id="${id}-chk" class="glyphicon"/>',
			closable: '<span id="${id}-cls" class="glyphicon glyphicon-remove"/>',
			image: '<img id="${id}-img" src="${_state.image}"/>',
			label: '<span id="${id}-lbl"/>',
			sortdir: '<span id="${id}-dir" class="glyphicon"/>'
	};
	
	cwf.widget._radio = {};
	
	/******************************************************************************************************************
	 * Base class providing simulated inheritance.
	 ******************************************************************************************************************/ 
	
	cwf.widget.Widget = function(){};
	
	/**
	 * Simulates inheritance by copying methods and properties to
	 * the designated subclass prototype.
	 * 
	 * @param {object} The subclass.
	 */
	cwf.widget.Widget.extend = function(subclass) {
	    var _super = this.prototype,
	    	prototype = new this();
	   
	    subclass = subclass || {};
	    
	    for (var name in subclass) {
	    	if (!name.endsWith('_')) {
	    		var subvalue = subclass[name],
	    			supervalue = _super[name];
	    		
	    		if (_.isFunction(subvalue) && _.isFunction(supervalue)) {
	    			if (!cwf.widget.fnTest.test(subvalue)) {
	    				cwf.debug ? cwf.log.warn('_super method not called for ', name) : null;
	    				prototype[name] = subvalue;
	    			} else {
	    				prototype[name] = 
	    			    		(function(name, fn) {
	    			    			return function() {
	    			    				var tmp = this._super, ret;
	    			    				this._super = _super[name];
	    			    				try {
	    			    					ret = fn.apply(this, arguments);       
	    			    				} finally {
	    			    					this._super = tmp;
	    			    				}
	    			    				return ret;
	    			    			};
	    			    		})(name, subvalue);
	    			}
	    		} else {
	    			prototype[name] = subvalue;
	    		}
	    	}
	    }

	    function Widget() {
	    	if (arguments.length && this._init) {
	    		this._init.apply(this, arguments);
	    	}
	    }
	   
	    Widget.prototype = prototype;
	    Widget.prototype.constructor = Widget;
	    Widget.extend = this.extend;
	    return Widget;
	};
			
	/******************************************************************************************************************
	 * Base class for all widget implementations.
	 ******************************************************************************************************************/ 
	
	cwf.widget.BaseWidget = cwf.widget.Widget.extend({

		/*------------------------------ Containment ------------------------------*/
		
		addChild: function(child, index) {
			if (!this.isContainer()) {
				throw new Error('Not a container widget: ' + this.wclass)
			}
			
			var wgt = cwf.wgt(child);
			
			if (!wgt) {
				throw new Error('Child is not a valid widget.');
			}
			
			child = wgt;
			var currentIndex = child._parent === this ? child.getIndex() : -1;
			index = _.isNil(index) || index < -1 ? -1 : index;
			
			if (currentIndex >= 0 && currentIndex === index) {
				return;
			}
			
			var maxIndex = currentIndex >= 0 ? this._children.length - 1 : this._children.length;
			
			if (index > maxIndex) {
				throw new Error('Index out of range: ' + index);
			} else if (index === maxIndex) {
				index = -1;
			}
			
			child._parent ? child._parent.removeChild(child) : null;
			index < 0 ? this._children.push(child) : this._children.splice(index, 0, child);
			child._parent = this;
			child._attach(index);
			this.onAddChild(child);
		},
		
		anchor$: function() {
			return this.widget$;
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
		 * Invokes a callback on each child widget.
		 * 
		 * @param {callback} callback A callback function following the forEach convention.
		 */
		forEachChild: function(callback) {
			if (this._children) {
				this._children.forEach(callback, this);
			}
		},
				
		/**
		 * Returns true if this widget may contain other widgets.
		 * 
		 * @return {boolean} True if widget is a container.
		 */
		isContainer: function() {
			return this._children;
		},
		
		onAddChild: function(child) {
			// Does nothing by default
		},
		
		onRemoveChild: function(child, destroyed, anchor$) {
			// Does nothing by default
		},
		
		removeChild: function(child, destroy) {
			var currentIndex = child.getIndex();
			
			if (currentIndex >= 0) {
				var anchor$ = child.widget$.parent();
				this._children.splice(currentIndex, 1);
				child._detach(destroy);
				this.onRemoveChild(child, destroy, anchor$);
				return true;
			}
		},
		
		swapChildren: function(index1, index2) {
			var child1 = this._children[index1],
				child2 = this._children[index2];
			
			this._children[index1] = child2;
			this._children[index2] = child1;
			cwf.swap(child1.widget$, child2.widget$);
		},
		
		/**
		 * Attaches a widget to the DOM at the specified position.
		 * 
		 * @param {number} Position of the widget relative to its siblings.
		 */
		_attach: function(index) {
			this._attachWidgetAt(this.widget$, this._parent.anchor$(), this._parent._children[index])
			this._attachAncillaries();
		},
		
		_attachAncillaries: function() {
			_.forOwn(this._ancillaries, function(ancillary$) {
				var attach = ancillary$.data('attach') || _attach;
				attach(ancillary$);
			});
			
			function _attach(ancillary$) {
				var oldparent$ = ancillary$.data('oldparent');
				oldparent$ ? oldparent$.append(ancillary$) : null;
			}
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
			
			this._detachAncillaries(destroy);
		},
		
		_detachAncillaries: function(destroy) {
			_.forOwn(this._ancillaries, function(ancillary$, key, map) {
				if (destroy) {
					ancillary$.remove();
					delete map[key];
				} else {
					ancillary$.data('oldparent', ancillary$.parent());
					ancillary$.detach();
				}
			});
		},
		
		/*------------------------------ Events ------------------------------*/
		
		/**
		 * Establish forwarding of an event from its source to this widget.
		 * 
		 * @param {jquery} source$ The source of the event.
		 * @param {string} sourceEvent The name of the source event.
		 * @param {string} [forwardEvent] The name of the forwarded event 
		 * 	(defaults to same as sourceEvent).
		 */
		forward: function(source$, sourceEvent, forwardEvent) {
			var widget$ = this.widget$;
			
			source$.on(sourceEvent, function (event) {
				var forward = $.extend({}, event);
				forward.type = forwardEvent || sourceEvent;
				widget$.triggerHandler(forward);
			})
		},
		
		/**
		 * Turns on/off server forwarding of one or more event types.
		 * 
		 * @param {string} eventTypes Space-delimited list of event types.
		 * @param {boolean} noforward If true, forwarding is enabled; if false, disabled.
		 */ 
		forwardToServer: function(eventTypes, noforward) {
			this.widget$[noforward ? 'off' : 'on'](eventTypes, cwf.event.sendToServer);
		},
		
		/**
		 * Notify server of a state change.
		 * 
		 * @param {string} state Name of the state.
		 * @param {*} value New value of the state.
		 */
		stateChanged: function(state, value) {
			this.trigger('stateChange', {data: {state: state, value: value}});
		},
		
		/**
		 * Trigger an event on the widget.
		 * 
		 * @param {string || Event} event The event to be triggered.
		 * @param {object} [params] Additional params to be included.
		 */
		trigger: function(event, params) {
			this.widget$.triggerHandler(event, params);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		/**
		 * Removes this widget from its parent widget and destroys the associated widget$.
		 */
		destroy: function() {
			if (this._parent) {
				this._parent.removeChild(this, true);
			} else {
				this._detach(true);
			}
			
			cwf.widget.unregister(this.id);
		},
		
		init: function() {
			// Override to perform additional initializations
		},
		
		_init: function(parent, props, state) {
			_.assign(this, props);
			this._parent = parent;
			this._children = this.cntr ? [] : null;
			this._ancillaries = {};
			this.widget$ = null;
			this._rendering = false;
			this._state = {};
			this._forwarding = {};
			this.initState(state, true);
			this.init();
			this.rerender();
		},
		
		/*------------------------------ Other ------------------------------*/
		
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
		
		/**
		 * Returns the index position of this child widget within its parent widget.
		 * 
		 * @return {number} The index of this child widget, or -1 if it has no parent widget.
		 */
		getIndex: function() {
			return this._parent ? this._parent.getChildIndex(this) : -1;
		},
		
		/**
		 * Returns the page to which this widget belongs.
		 * 
		 * @return {Page} The owning page.
		 */
		page: function() {
			return cwf.widget.find(this.pageId());
		},
		
		/**
		 * Returns the page$ to which this widget belongs.
		 * 
		 * @return {jquery} The owning page.
		 */
		page$: function() {
			return $('#' + this.pageId());
		},
		
		/**
		 * Returns the id of the page to which this widget belongs.
		 */
		pageId: function() {
			return '_cwf_' + this.id.split('_')[2];
		},
		
		/**
		 * Convenience method for resolving embedded EL references.
		 * 
		 * @param {string} v Value containing EL references.
		 * @param {string} [pfx] Optional EL prefix (defaults to '$').
		 * @return {string} Input value with EL references resolved.
		 */
		resolveEL: function(v, pfx) {
			return cwf.resolveEL(this, v, pfx);
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
		
		/*------------------------------ Rendering ------------------------------*/
		
		/**
		 * Override to apply event handlers, etc.
		 */
		afterRender: function() {
			this.forwardToServer('stateChange');
		},
		
		rerender: function() {
			if (this._rendering) {
				return;
			}
			
			try {
				this._rendering = true;
				
				if (this.isContainer()) {
					this.forEachChild(function(child){child._detach();});
				}
				
				var old$ = this.widget$;
				
				this.widget$ = this.render$()
					.data('cwf_widget', this)
					.data('cwf_wclass', this.wclass)
					.first()
					.attr('id', this.id);
				
				this.syncState();
				
				if (old$) {
					old$.replaceWith(this.widget$);
				} else if (this._parent) {
					this._parent.addChild(this);
				}
				
				if (this.isContainer()) {
					this.forEachChild(function(child){child._attach(-1);});
				}
			} finally {
				this._rendering = false;
			}
			
			this.afterRender();
		},

		/**
		 * Returns the jquery object representing the rendered DOM for
		 * this widget.
		 */
		render$: function() {
			throw new Error('No rendering logic supplied for ' + this.wclass);
		},
		
		/**
		 * Returns the DOM template(s) associated with the specified key(s).
		 * 
		 * @param {string...} keys One or more keys.  A key may be prefixed with
		 * 		a state name followed by a colon to indicate that the template
		 *      should be included only if the state has a truthy value.  If no
		 *      state name precedes the colon, the key name is used (i.e.,
		 *      ":xxx" is a shortcut for "xxx:xxx").
		 * @return {string} A concatenation of the DOM templates associated 
		 * 		with the specified keys.
		 */
		getDOMTemplate: function() {
			var result = '';
			
			for (var i = 0; i < arguments.length; i++) {
				var key = arguments[i],
					j = key.indexOf(':');
				
				if (j >= 0) {
					var state = key.substring(0, j);
					key = key.substring(j + 1);
					
					if (!this.getState(state.length ? state : key)) {
						continue;
					}
				}
				
				var tmpl = cwf.widget._domTemplates[key];
				
				if (!_.isNil(tmpl)) {
					result += tmpl;
				}
			}
			
			return result;
		},
		
		/*------------------------------ State ------------------------------*/
		
		/**
		 * Invoke a state's setter function.
		 * 
		 * @param {string} key The name of the state.
		 * @param {*} old The previous value of the state.
		 */
		applyState: function(key, old) {
			if (!key.startsWith('_')) {
				var fn = this[key],
					value = this._state[key];
				
				if (!fn || !_.isFunction(fn)) {
					throw new Error('Unrecognized state for ' + this.wclass + ': ' + key);
				}

				fn.call(this, value, old);
			}
		},
		
		/**
		 * Add / remove text content to / from widget.
		 * 
		 * @param {string} Text content to add (or nil to remove).
		 */
		content: function(v) {
			var span$ = this.sub$('content');
			
			if (!v) {
				span$.remove();
			} else {
				if (span$.length === 0) {
					var dom = this.resolveEL('<span id="${id}-content"/>');
					span$ = $(dom).appendTo(this.widget$);
				}
				
				span$.text(v);
			}
		},

		/**
		 * Returns the current state for the specified key.
		 * 
		 * @param {string} key The key for the requested state.
		 * @return {*} The value of the requested state.
		 */
		getState: function(key) {
			return this._state[key];
		},
		
		/**
		 * Returns true if there is a state associated with the specified key.
		 * 
		 * @param {string} key The key for the state of interest.
		 * @return {boolean} True if a state is present for the key.
		 */
		hasState: function(key) {
			var value = this._state[key];
			return !_.isNil(value) && value !== '';
		},
		
		/**
		 * Initializes the widget state to the specified values.  If defaults is
		 * true, the supplied values will not overwrite existing values in the
		 * current state.  Otherwise, any existing values in the current state
		 * will be replaced by those supplied.
		 * 
		 * @param {object} state A map of name-value pairs.
		 * @param {boolean} [overwrite] If true, existing state values will be
		 * 		overwritten.
		 */
		initState: function(state, overwrite) {
			if (state) {
				return overwrite ? _.assign(this._state, state) : _.defaults(this._state, state);
			}
		},
		
		/**
		 * Creates/removes a translucent mask over this widget.
		 */
		mask: function(v) {
			var destroy = v === false;
			
			if (!this._mask$ === !destroy) {
				if (destroy) {
					this._mask$.remove();
					delete this._mask$;
				} else {
					this._mask$ = this.widget$.cwf$mask().append('<span>').css('display', 'flex');
				}
			}
			
			if (!destroy) {
				var span$ = this._mask$.children().first();
				span$.text(v).css('display', v ? '' : 'none');
			}
		},
		
		/**
		 * Assign name associated with the widget.
		 * 
		 * @param {string] v Name value.
		 */
		name: function(v) {
			this.attr('cwf_name', v);
		},
		
		/**
		 * Updates the saved state for the specified function and arguments.
		 * 
		 * @param {string} key The name of the setter function.
		 * @param {*} value The value of the last setter invocation.
		 * @return {boolean} True if the state changed.
		 */
		setState: function(key, value) {
			var oldValue = this._state[key];
			
			if (!_.isEqual(value, oldValue)) {
				delete this._state[key];
				this._state[key] = value;
				return true;
			}
		},

		/**
		 * Calls setters for all saved states.
		 */
		syncState: function() {
			var self = this;

			_.forOwn(this._state, function(value, key) {
				self.applyState(key);
			});
		},
		
		/**
		 * Updates the value for the specified state, and invokes its setter
		 * function if the value changed from the previous value or if rendering
		 * is active.
		 * 
		 * @param {string} key The name of the state.
		 * @param {boolean} [fromServer] If true, do not sync state back to server..
		 * @param {*} value The new value for the state.
		 */
		updateState: function(key, value, fromServer) {
			var old = this._state[key],
				changed = this.setState(key, value);
			
			if (changed || this._rendering) {
				this.applyState(key, old)
				
				if (changed && !fromServer) {
					this.stateChanged(key, value);
				}
			}
		}

	});
	
	/******************************************************************************************************************
	 * Non-UI widget base class
	 ******************************************************************************************************************/ 
	
	cwf.widget.MetaWidget = cwf.widget.BaseWidget.extend({

		/*------------------------------ Rendering ------------------------------*/
		
		/**
		 * Extend render$ to also render the real widget.  The widget$ returned
		 * is really just a NOP placeholder.
		 */
		render$: function() {
			this._detachAncillaries(true);
			this.real$ = this.renderReal$();
			
			if (this.real$) {
				this.real$
					.appendTo(this.realAnchor$)
					.attr('id', this.subId('real'));
				this._ancillaries.real$ = this.real$;
			}
			
			return $('<!-- ' + this.id + ' -->');
		},
		
		/**
		 * Return rendering for the real widget.
		 */
		renderReal$: function() {
			return null;
		}
		
	});
	
	/******************************************************************************************************************
	 * UI widget base class
	 ******************************************************************************************************************/ 
	
	cwf.widget.UIWidget = cwf.widget.BaseWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.wclazz = 'cwf_' + this.wclass.toLowerCase();
			this.initState({_clazz: this.wclazz, clazz: '', visible: true});
		},
				
		/*------------------------------ Other ------------------------------*/
		
		input$: function() {
			var input$ = this.sub$('inp');
			return input$.length ? input$ : this.widget$;
		},
		
		/**
		 * Replace one class with another.
		 * 
		 * @param {string} oldcls The class to remove (may be nil).
		 * @param {string} newcls The class to add (may be nil).
		 * @return {boolean} True if a class was added or removed.
		 */
		replaceClass: function(oldcls, newcls) {
			var result = oldcls && this.toggleClass(oldcls, false);
			result |= newcls && this.toggleClass(newcls, true);
			return result;
		},
		
		scrollIntoView: function(alignToTop) {
			this.widget$[0].scrollIntoView(alignToTop);
		},
		
		subclazz: function(sub, wclazz) {
			return sub ? (wclazz ? wclazz : this.wclazz) + '-' + sub.toLowerCase() : null;
		},
		
		/**
		 * Use this method to add or remove fixed classes.  Fixed classes will always be added 
		 * to the element regardless of the classes specified via the class property.
		 * 
		 * @param {string} cls The classes to toggle.
		 * @param {boolean} [add] If true, add the class; if false, remove it; if missing, toggle it.
		 * @return {boolean} True if any class was added or removed.
		 */
		toggleClass: function(cls, add) {
			var _clazz = this.getState('_clazz').split(' '),
				cls = cls.split(' '),
				w$ = this.widget$,
				changed = false;
			
			_.forEach(cls, _toggle);
			
			if (changed) {
				this.setState('_clazz', _clazz.join(' '));
			}
			
			return changed;
			
			function _toggle(cls) {
				var i = _clazz.indexOf(cls),
					exists = i !== -1,
					remove = _.isNil(add) ? exists : !add;
				
				if (exists === remove) {
					w$ ? w$.toggleClass(cls, !remove) : null;
					remove ? _clazz.splice(i, 1) : _clazz.push(cls);
					changed = true;
				}
			}
		},
		
		/*------------------------------ State ------------------------------*/
		
		balloon: function(v) {
			if (v) {
				this.widget$.attr('data-balloon', v)
					.attr('data-balloon-pos', 'right')
					.attr('data-balloon-visible', true)
					.attr('data-balloon-length', 'fit');
			} else {
				this.widget$.removeAttr('data-balloon data-balloon-pos data-balloon-visible data-balloon-length');
			}
		},
		
		clazz: function(v) {
			var clazz = this.getState('_clazz') + (v ? ' ' + v : '');
			this.attr('class', clazz);
		},
		
		context: function(v) {
			if (v) {
				this.widget$.on('contextmenu', _showContextPopup);
			} else {
				this.widget$.off('contextmenu', _showContextPopup);
			}
			
			function _showContextPopup(event) {
				cwf.event.stop(event);
				cwf.wgt(v).open({
					my: 'left top',
					at: 'right bottom',
					of: event
				});
			}
		},
		
		css: function(v) {
			var inline$ = this._ancillaries.inline$;
			
			if (v) {
				if (!inline$) {
					this._ancillaries.inline$ = inline$ = 
						$('<style>').appendTo('head').attr('id', this.subId('inline'));
				}
				inline$.text(this.resolveEL(v, '#'));
			} else if (inline$) {
				inline$.destroy();
				delete this._ancillaries.inline$;
			}
		},
		
		disabled: function(v) {
			this.attr('disabled', v, this.input$());
		},
		
		dragid: function(v) {
			var self = this,
				active = !!this.widget$.draggable('instance'),
				newactive = !_.isNil(v);
			
			this._dragids = newactive ? cwf.stringToSet(v, ' ') : null;
			
			if (newactive !== active) {
				if (active) {
					this.widget$.draggable('destroy');
				} else {
					this.widget$.draggable({
						cancel: null,
						helper: _helper,
						start: _start,
						appendTo: 'body',
						iframeFix: true
					});
				}
			}
			
			function _helper() {
				var ele = self.getDragHelper();
				ele.className += ' cwf_dragging';
				return ele;
			}
			
			function _start(event, ui) {
				self.widget$.draggable('option', 'cursorAt', {
				    left: Math.floor(ui.helper.width() / 2),
				    top: Math.floor(ui.helper.height() / 2)
				});
			}
		},
		
		dropid: function(v) {
			var self = this,
				active = !!this.widget$.droppable('instance'),
				newactive = !_.isNil(v);
			
			this._dropids = newactive ? cwf.stringToSet(v, ' ') : null;
			
			if (newactive !== active) {
				if (active) {
					this.widget$.off('drop', _dropped);
					this.widget$.droppable('destroy');
				} else {
					this.widget$.droppable({
						accept: _canDrop,
						tolerance: 'pointer',
						classes: {
							'ui-droppable-hover': 'cwf_droppable'
						}
					});
					
					this.widget$.on('drop', _dropped);
				}
			}
			
			function _canDrop(draggable$) {
				var wgt = cwf.wgt(draggable$),
					dragids = wgt ? wgt._dragids : null,
					dropids = self._dropids,
					result = false;
				
				if (dragids && dropids) {
					if (dragids['*'] || dropids['*']) {
						return true;
					}
					
					_.forOwn(dropids, function(x, dropid) {
						if (dragids[dropid]) {
							result = true;
							return false;
						}
					});
				}
				
				return result;
			}
			
			function _dropped(event, ui) {
				event.relatedTarget = ui.draggable;
			}
		},
		
		focus: function(v) {
			this.input$()[v ? 'focus' : 'blur']();
		},
		
		hint: function(v) {
			this.attr('title', v, this.input$());
		},
		
		keycapture: function(v) {
			var	self = this;
			
			if (v) {
				this._keycapture = v.split(' ');
				this.widget$.on('keydown', _keyevent);
			} else {
				this._keycapture = null;
				this.widget$.off('keydown', _keyevent);
			}
			
			function _keyevent(event) {
				var val = cwf.event.toKeyCapture(event);
				
				if (self._keycapture.indexOf(val) >= 0) {
					cwf.event.stop(event);
					event.type = 'keycapture';
					cwf.event.sendToServer(event);
				}
			}
		},
		
		style: function(v) {
			this.attr('style', v);
		},
		
		tabindex: function(v) {
			this.attr('tabindex', v, this.input$());
		},
		
		visible: function(v) {
			this.toggleClass('hidden', !v);
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		getDragHelper: function() {
			return cwf.clone(this.widget$, this.isContainer() ? 0 : -1);
		}
		
	});
	
	/******************************************************************************************************************
	 * Base class for widgets wrapping input elements
	 ******************************************************************************************************************/ 
	
	cwf.widget.InputWidget = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Other ------------------------------*/
		
		selectAll: function() {
			this.selectRange(0, 99999);
		},
		
		selectRange: function(start, end) {
			this.input$()[0].setSelectionRange(start, end);
		},
		
		/*------------------------------ State ------------------------------*/
		
		maxlength: function(v) {
			this.attr('maxlength', v, this.input$());
		},
		
		maxvalue: function(v) {
			this.attr('max', v, this.input$());
		},
		
		minvalue: function(v) {
			this.attr('min', v, this.input$());
		},
		
		pattern: function(v) {
			this.attr('pattern', v, this.input$());
		},
		
		placeholder: function(v) {
			this.attr('placeholder', v, this.input$());
		},
		
		readonly: function(v) {
			this.attr('readonly', v, this.input$());
		},
		
		required: function(v) {
			this.attr('required', v, this.input$());
		},
		
		value: function(v) {
			this.input$().val(v);
		}
		
	});

	/******************************************************************************************************************
	 * Base class for widgets the allow text entry.
	 ******************************************************************************************************************/ 
	
	cwf.widget.InputboxWidget = cwf.widget.InputWidget.extend({
		
		/*------------------------------ Events ------------------------------*/
		
		fireChanged: function() {
			this._changed = false;
			this.trigger('change', {value: this.input$()[0].value});
		},
				
		handleBlur: function(event) {
			var msg = event.target.validationMessage;
			cwf.wgt(event.target).updateState('balloon', msg ? msg : null);
			
			if (this._changed && !msg) {
		    	this.fireChanged();
			}
		},
		
		handleInput: function(event) {
			var ele = this.input$()[0],
				value = ele.value;
			
			if (value.length && this.validate && !this.validate(value)) {
				cwf.event.stop(event);
				var cpos = ele.selectionStart - 1;
				ele.value = this._previous;
				ele.selectionStart = cpos;
				ele.selectionEnd = cpos;
				return;
			}
		    
		    this._previous = value;
			
		    if (this._synchronized) {
		    	this.fireChanged();
		    } else {
		    	this._changed = true;
		    }
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
				
		init: function() {
			this._super();
			this._synchronized = false;
			this._changed = false;
			this._previous = '';
		},
		
		synced: function(v) {
			this._synchronized = v;
		},
		
		/*------------------------------ Other ------------------------------*/
		
		clear: function() {
			this.input$().val('');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.toggleClass('cwf_inputbox', true);
			this.forwardToServer('change');
			var input$ = this.input$();
			input$.on('input propertychange', this.handleInput.bind(this));
			input$.on('blur', this.handleBlur.bind(this));
			this._constraint ? input$.on('keypress', cwf.event.constrainInput.bind(this, this._constraint)) : null;
		},
		
		render$: function() {
			return $(this.resolveEL('<span><input id="${id}-inp" type="${_type}"></span'));
		}
		
	});
	
	/******************************************************************************************************************
	 * An integer input box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.NumberboxWidget = cwf.widget.InputboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._type = 'text';
			this._constraint = /[\d+-]/;
			this._partial = /^[+-]?$/;
			this._super();
		},
		
		/*------------------------------ Other ------------------------------*/
		
		validate: function(value, full) {
			var partial = !full && this._partial.test(value);
			value = partial ? 0 : _.toNumber(value);
			return partial || (!_.isNaN(value) && this.validateRange(value, full)); 
		},
		
		validateRange: function(value, full) {
			var min = full ? _.defaultTo(this.getState('min'), this._min) : this._min,
				max = full ? _.defaultTo(this.getState('max'), this._max) : this._max;
			
			value = value === undefined ? +this.input$().val() : +value;
			return value >= min && value <= max;
		},
		
		/*------------------------------ State ------------------------------*/
		
		minValue: function(v) {
			this.attr('min', v, this.input$());
		},
		
		maxValue: function(v) {
			this.attr('max', v, this.input$());
		}
	});
	
	/******************************************************************************************************************
	 * Main page widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Page = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			if (this._parent) {
				throw new Error('Page may not have a parent.')
			}
			
			this._super();
		},
			
		afterInitialize: function() {
			$('#cwf_root').css('visibility', 'visible');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<div>').appendTo('#cwf_root');
		},
		
		/*------------------------------ State ------------------------------*/
		
		title: function(v) {
			$('head>title').text(v);
		}
		
	});

	/******************************************************************************************************************
	 * Inline style widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Style = cwf.widget.MetaWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		realAnchor$: $('head'),
		
		renderReal$: function() {
			return $('<style>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		content: function(v) {
			this.real$.text(v);
		}
		
	});
	
	/******************************************************************************************************************
	 * Stylesheet link widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Stylesheet = cwf.widget.MetaWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		realAnchor$: $('head'),
		
		renderReal$: function() {
			return $('<link type="text/css" rel="stylesheet">');
		},
		
		/*------------------------------ State ------------------------------*/
		
		href: function(v) {
			this.attr('href', v, this.real$);
		}
		
	});
	
	/******************************************************************************************************************
	 * Embedded javascript widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Script = cwf.widget.MetaWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		realAnchor$: $('body'),
		
		renderReal$: function() {
			return $('<script>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		content: function(v) {
			this.real$.text(v);
		},
		
		src: function(v) {
			this.attr('src', v, this.real$);
		},
		
		type: function(v) {
			this.attr('type', v, this.real$);
		}
		
	});
	
	/******************************************************************************************************************
	 * A timer widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Timer = cwf.widget.BaseWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._timer = null;
			this._interval = 0;
			this._repeat = -1;
		},
		
		_detach: function(destroy) {
			this.stop();
			this._super(destroy);
		},
		
		/*------------------------------ Operations ------------------------------*/
		
		start: function() {
			var self = this,
				count = 0;
			
			if (!this.timer && this._interval > 0) {
				this.timer = setInterval(_trigger, this._interval);
				return true;
			}
			
			function _trigger() {
				if (!cwf.ws.isConnected()) {
					self.stop();
					return;
				}
				
				count++;
				
				if (self._repeat >= 0 && count > self._repeat) {
					self.updateState('running', false);
				}
				
				self.trigger('timer', {count: count, running: self.timer !== null});
			}
		},
		
		stop: function() {
			if (this.timer) {
				clearInterval(this.timer);
				this.timer = null;
				return true;
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<span>');
		},
		
		/*------------------------------ State ------------------------------*/
		
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
		}
		
	});
	
	/******************************************************************************************************************
	 * Widget wrapping text content
	 ******************************************************************************************************************/ 
	
	cwf.widget.Content = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<span>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		content: function(v) {
			this.widget$.text(v);
		}
		
	});
	
	/******************************************************************************************************************
	 * Widget wrapping html content
	 ******************************************************************************************************************/ 
	
	cwf.widget.Html = cwf.widget.UIWidget.extend( {
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<span>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		content: function(v) {
			this.widget$.children().remove();
			this.widget$.append(cwf.$(v));
		}
		
	});
	
	/******************************************************************************************************************
	 * A div widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Div = cwf.widget.UIWidget.extend({

		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<div>');
		}
	
	});

	/******************************************************************************************************************
	 * A span widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Span = cwf.widget.UIWidget.extend({

		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<span>');
		}
	
	});

	/******************************************************************************************************************
	 * A popup widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Popup = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		_trigger: function(which) {
			var event = $.Event(which, {
				relatedTarget: !this._related ? null : this._related.target ? this._related.target : this._related
			});
			
			this.trigger(event);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._allowClickBubble = false;
		},

		/*------------------------------ Other ------------------------------*/
		
		close: function() {
			this.anchor$().hide().appendTo(this.widget$);
			this._trigger('close');
			this._related = null;
		},
		
		isOpen: function() {
			return this.anchor$().css('display') !== 'none';
		},
		
		open: function(position) {
			$('body').one('click', this.close.bind(this));
			this._related = position.of;
			
			this.anchor$()
				.appendTo(position.anchor || '#cwf_root')
				.show()
				.position(position);
			
			this._trigger('open');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.toggleClass('hidden', true);
			this.anchor$().addClass(this.wclazz).hide();
			this._allowClickBubble ? null : this.widget$.on('click', function(event) {
				event.stopPropagation();
			});
		},
		
		render$: function() {
			return $(this.resolveEL('<span><div id="${id}-inner"></span'));
		}
		
	});
	
	/******************************************************************************************************************
	 * A toolbar widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Toolbar = cwf.widget.UIWidget.extend({

		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({align: 'START', orientation: 'HORIZONTAL'});
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<div/>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		align: function(v, old) {
			v = this.subclazz(v ? v : 'start');
			old = old ? this.subclazz(old) : null;
			this.replaceClass(old, v);
		},
		
		orientation: function(v, old) {
			v = this.subclazz(v ? v : 'horizontal');
			old = old ? this.subclazz(old) : null;
			this.replaceClass(old, v);
		}
		
	});
	
	/******************************************************************************************************************
	 * Base class for widgets with a label
	 ******************************************************************************************************************/ 
	
	cwf.widget.LabeledWidget = cwf.widget.UIWidget.extend({
		
		/*------------------------------ State ------------------------------*/
		
		label: function(v) {
			var lbl$ = this.sub$('lbl');
			(lbl$.length ? lbl$ : this.widget$).text(v);
		}
	
	});
	
	/******************************************************************************************************************
	 * Base class for widgets with a label and an image
	 ******************************************************************************************************************/ 
	
	cwf.widget.LabeledImageWidget = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ State ------------------------------*/
		
		image: function(v) {
			this.rerender();
		}
		
	});
	
	/******************************************************************************************************************
	 * A button widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Button = cwf.widget.LabeledImageWidget.extend({

		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = '<button class="btn btn-sm btn-success">'
				    + this.getDOMTemplate(':image', 'label')
					+ '</button>';
			
			return $(this.resolveEL(dom));
		}
	
	});
	
	/******************************************************************************************************************
	 * A hyperlink widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Hyperlink = cwf.widget.LabeledImageWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			 var dom = '<a class="btn-link">'
				    + this.getDOMTemplate(':image', 'label')
					+ '</a>';
			 
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		href: function(v) {
			this.attr('href', v);
		},
		
		target: function(v) {
			this.attr('target', v);
		}
		
	});
	
	/******************************************************************************************************************
	 * A label widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Label = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<label class="label-default">');
		}
	
	});
	
	/******************************************************************************************************************
	 * A cell widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Cell = cwf.widget.LabeledWidget.extend({		
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = 
				'<div>'
			  + this.getDOMTemplate(':label')
			  + '</div>';
			
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		label: function(v, old) {
			if (!!old !== !!v) {
				this.rerender();
			}
			
			this._super(v, old);
		}
		
	});

	/******************************************************************************************************************
	 * A checkbox widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Checkbox = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ Events ------------------------------*/
		
		handleChange: function(event) {
			this._syncChecked(true);
			var target = event.target;
			target.value = target.checked;
			cwf.event.sendToServer(event);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.type = 'checkbox';
			this.group = null;
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('change', this.handleChange.bind(this));
		},
		
		render$: function() {
			var dom =
				'<div>'
			  + '  <input id="${id}-real" type="${type}" name="${group}">'
			  + '  <label id="${id}-lbl" for="${id}-real"/>'
			  + '</div>';
			
			return $(this.resolveEL(dom));
		},
	
		/*------------------------------ State ------------------------------*/
		
		checked: function(v) {
			this.sub$('real').prop('checked', v);
			this._syncChecked(v);
		},
		
		position: function(v) {
			this.toggleClass('cwf_labeled-left', v === 'LEFT');
			this.toggleClass('cwf_labeled-right', v !== 'LEFT');
		},

		_syncChecked: function(checked) {
			// NOP
		}
		
	});
	
	/******************************************************************************************************************
	 * A radio button widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Radiobutton = cwf.widget.Checkbox.extend({
	
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.type = 'radio';
		},		
		
		/*------------------------------ Rendering ------------------------------*/
		
		getGroup: function() {
			var wgt = this._parent;
			
			while (wgt && wgt.wclass !== 'Radiogroup') {
				wgt = wgt._parent;
			}
			
			return wgt ? wgt.id : null;
		},
		
		render$: function() {
			this.group = this.getGroup();
			return this._super();
		},
		
		/*------------------------------ State ------------------------------*/
		
		_syncChecked: function(checked) {
			if (this.group) {
				var previous = cwf.widget._radio[this.group];
				previous = previous ? cwf.widget.find(previous) : null;
				
				if (checked) {
					if (previous && previous !== this) {
						previous.trigger('change');
					}
					
					cwf.widget._radio[this.group] = this.id;
				} else if (previous === this) {
					cwf.widget._radio[this.group] = null;
				}
			}
		}
		
	});
	
	/******************************************************************************************************************
	 * Widget for grouping radio buttons
	 ******************************************************************************************************************/ 
	
	cwf.widget.Radiogroup = cwf.widget.Span.extend();
	
	/******************************************************************************************************************
	 * A menu popup widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menupopup = cwf.widget.Popup.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._allowClickBubble = true;
		},		
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = 
				'<span>'
			  +   '<ul id="${id}-inner" role="menu" class="dropdown-menu multi-level" />'
			  + '</span>';
			return $(this.resolveEL(dom));
		}
		
	});
	
	/******************************************************************************************************************
	 * A menu widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menu = cwf.widget.LabeledImageWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
						
		/*------------------------------ Events ------------------------------*/
		
		handleClose: function(event) {
			this._open(false);
		},
		
		handleOpen: function(event) {
			this._open(true);
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_open: function(v) {
			this.setState('open', v);
			this.stateChanged('open', v);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this.widget$.on('show.bs.dropdown', this.handleOpen.bind(this));
			this.widget$.on('hide.bs.dropdown', this.handleClose.bind(this));
		},
		
		render$: function() {
			var dom = 
				  '<span>'
				+ '  <div class="dropdown" style="display: inline-block" role="presentation">'
				+ '    <a id="${id}-btn" data-toggle="dropdown"'
				+ '      role="button" aria-haspopup="true" aria-expanded="false">'
				+ 		 this.getDOMTemplate(':image', 'label')
				+ '      <span class="caret"></span>'
				+ '    </a>'
				+ '    <ul id="${id}-inner" class="dropdown-menu multi-level" '
				+ '      role="menu" aria-labelledby="${id}-lbl"></ul>'
				+ '  </div>'
				+ '</span>';
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		clazz: function(v) {
			this._super();
			this.attr('class', v, this.sub$('btn'));
		},
		
		open: function(v) {
			var dd$ = this.widget$.children().first(),
				open = dd$.hasClass('open');
			
			if (!open !== !v) {
				dd$.children().first().dropdown('toggle');
			}
		}
		
	});
	
	/******************************************************************************************************************
	 * A menu item widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menuitem = cwf.widget.LabeledImageWidget.extend({

		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
				
		onAddChild: function() {
			this._childrenUpdated();
		},
		
		onRemoveChild: function() {
			this._childrenUpdated();
		},
		
		/*------------------------------ Events ------------------------------*/
		
		handleCheck: function(event) {
			cwf.event.stop(event);
			this.trigger('click');
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({_submenu: false, checked: false, checkable: false});
		},
		
		
		/*------------------------------ Other ------------------------------*/
		
		_childrenUpdated: function() {
			if (this.setState('_submenu', !!this._children.length)) {
				this.rerender();
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this.sub$('chk').on('click', this.handleCheck.bind(this));
		},
		
		render$: function() {
			var submenu = this.getState('_submenu'),
				dom = '<li>'
					+ '  <a>'
					+ this.getDOMTemplate(':image', ':checkable', 'label')
					+ '  </a>'
					+ (submenu ? '<ul id="${id}-inner" class="dropdown-menu">' : '')
					+ '</li>';
			
			this.toggleClass('dropdown', !submenu);
			this.toggleClass('dropdown-submenu', submenu);
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		checkable: function(v) {
			this.rerender();
		},
		
		checked: function(v) {
			this.sub$('chk').cwf$swapClasses('glyphicon-check', 'glyphicon-unchecked', v);
		}
	});
	
	/******************************************************************************************************************
	 * A menu header widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menuheader = cwf.widget.LabeledImageWidget.extend({

		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.toggleClass('dropdown-header', true);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = 
				  '<li>'
			    + this.getDOMTemplate(':image', 'label')
				+ '</li>';
					
			return $(this.resolveEL(dom));
		}

	});
	
	/******************************************************************************************************************
	 * A menu separator widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menuseparator = cwf.widget.UIWidget.extend({

		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.toggleClass('divider', true);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<li role="separator"></li>');
		}
		
	});
	
	/******************************************************************************************************************
	 * A standalone image widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Image = cwf.widget.UIWidget.extend({
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<img>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		alt: function(v) {
			this.attr('alt', v);
		},
		
		src: function(v) {
			this.attr('src', v);
		}
		
	});
	
	/******************************************************************************************************************
	 * A text box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Textbox = cwf.widget.InputboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
				
		init: function() {
			this._type = 'text';
			this._super();
		},
		
		/*------------------------------ State ------------------------------*/
		
		masked: function(v) {
			this.attr('type', v ? 'password' : this._type);
		}
		
	});
	
	/******************************************************************************************************************
	 * An integer input box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Integerbox = cwf.widget.NumberboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._max = 2147483647;
			this._min = -2147483648;
		},
		
		/*------------------------------ Other ------------------------------*/
		
		validate: function(value) {
			return this._super(value);
		}
		
	});
	
	/******************************************************************************************************************
	 * An integer input box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Longbox = cwf.widget.Integerbox.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._max = 9223372036854775807;
			this._min = -9223372036854775808;
		}
		
	});
	
	/******************************************************************************************************************
	 * A double float point input box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Doublebox = cwf.widget.NumberboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._constraint = /[\d+-.]/;
			this._partial = /^[+-]?[.]?$/;
			this._max = Number.MAX_VALUE;
			this._min = -Number.MAX_VALUE;
		}
		
	});
	
	/******************************************************************************************************************
	 * A multi-line text box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Memobox = cwf.widget.InputboxWidget.extend({
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $(this.resolveEL('<span><textarea id="${id}-inp"/></span>'));
		},
		
		scrollToBottom: function() {
			var input$ = this.input$();
			input$.scrollTop(input$[0].scrollHeight);
		},
		
		/*------------------------------ State ------------------------------*/
		
		autoScroll: function(v) {
			if (v) {
				this.scrollToBottom();
			}
		},
		
		value: function(v) {
			this._super(v);
			
			if (this.getState('autoScroll')) {
				this.scrollToBottom();
			}
		}
		
	});
	
	/******************************************************************************************************************
	 * A popup box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Popupbox = cwf.widget.InputboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
				
		init: function() {
			this._type = 'text';
			this._super();
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom =
				'<span>'
			  + '  <input id="${id}-inp" type="text">'
			  + '  <span id="${id}-btn" class="glyphicon glyphicon-triangle-bottom" />'
			  + '</span>';
			
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ Other ------------------------------*/
		
		open: function() {
			if (this._popup && !this.isOpen()) {
				this._popup.open({
					my: 'right top',
					at: 'right bottom',
					of: this.widget$,
					anchor: this.widget$
				});
			}
		},
		
		close: function() {
			if (this.isOpen()) {
				this._popup.close();
			}
		},
		
		toggle: function() {
			this.isOpen() ? this.close() : this.open();
		},
		
		isOpen: function() {
			return this._popup && this._popup.isOpen();
		},
		
		/*------------------------------ State ------------------------------*/
		
		popup: function(v) {
			this._popup = v;
			
			var btn$ = this.sub$('btn'),
				self = this;
			
			if (v) {
				btn$.on('click', _showPopup);
			} else {
				btn$.off('click', _showPopup);
			}
			
			function _showPopup(event) {
				cwf.event.stop(event);
				self.toggle();
			}
		}		
		
	});
	
	/******************************************************************************************************************
	 * A date box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Datebox = cwf.widget.InputboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._type = 'date';
			this._super();
		}
		
	});
	
	/******************************************************************************************************************
	 * A list box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Listbox = cwf.widget.UIWidget.extend({

		/*------------------------------ Events ------------------------------*/

		handleSelect: function(event) {
			this.forEachChild(function(child) {
				child.syncSelected();
			});
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('change', this.handleSelect.bind(this));
		},
		
		render$: function() {
			return $('<select>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		multiple: function(v) {
			this.attr('multiple', v);
		},
		
		size: function(v) {
			this.attr('size', v);
		}
		
	});
	
	/******************************************************************************************************************
	 * A list box item widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Listitem = cwf.widget.LabeledWidget.extend({		
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.forwardToServer('select');
		},
		
		render$: function() {
			return $('<option role="presentation">');
		},
		
		/*------------------------------ State ------------------------------*/
		
		selected: function(v) {
			this.attr('selected', v);
		},
		
		syncSelected: function() {
			var selected = this.widget$.is(':selected');
			
			if (this.setState('selected', selected)) {
				this.trigger('select', {selected: selected});
			}
		},
		
		value: function(v) {
			this.attr('value', v);
		}
		
	});
	
	/******************************************************************************************************************
	 * A combo box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Combobox = cwf.widget.InputboxWidget.extend({

		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		source: function(request, response) {
			var term = request.term.toLowerCase(),
				len = term.length,
				items = [],
				filter = this.getState('autoFilter');
			
			this.forEachChild(function(child) {
				var label = child.getState('label') || '',
					matched = len > 0 && label.substring(0, len).toLowerCase() === term;
				
				if (!len || !filter || matched) {
					items.push({label: label, id: child.id, matched: matched, selected: !!child.getState('selected')});
				}
			});
			
			response(items);
		},
				
		/*------------------------------ Events ------------------------------*/

		handleBlur: function(event) {
			this.input$().autocomplete('close');
		},
		
		handleClick: function(event) {
			var inp$ = this.input$();
			
			if (inp$.is(':disabled')) {
				return;
			}
			
			var open = $(inp$.autocomplete('widget')).is(':visible');
			
			if (!open) {
				inp$.autocomplete('search', inp$.attr('value'));
				inp$.focus();
			} else {
				inp$.autocomplete('close');
			}
		},
		
		handleSelect: function(event, ui) {
			var wgt = cwf.widget.find(ui.item.id);
			wgt.selected(true);
			wgt.trigger('select', {selected: true});
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			
			var inp$ = this.input$();
			
			inp$.autocomplete({
	            delay: 50,
	            minLength: 0,
	            autoFocus: false,
	            appendTo: this.widget$,
	            source: this.source.bind(this),
				select: this.handleSelect.bind(this)
			});
			
			inp$.data('ui-autocomplete')._renderItem = this.renderItem$.bind(this);
			this.sub$('btn').on('click', this.handleClick.bind(this));
			inp$.on('blur', this.handleBlur.bind(this));
		},
		
		render$: function() {
			var dom =
				'<span>'
			  + '  <input id="${id}-inp" type="text">'
			  + '  <span id="${id}-btn" class="glyphicon glyphicon-triangle-bottom" />'
			  + '  <select id="${id}-inner" class="hidden" />'
			  + '</span>';
			
			return $(this.resolveEL(dom));
		},
		
		renderItem$: function(ul, item) {
			return $('<li>')
				.text(item.label)
				.toggleClass(this.subclazz('matched'), item.matched)
				.toggleClass(this.subclazz('selected'), item.selected)
				.appendTo(ul);
		},
		
		/*------------------------------ State ------------------------------*/

		autoFilter: function(v) {
			// NOP
		}	
		
	});
	
	/******************************************************************************************************************
	 * A combo box item widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Comboitem = cwf.widget.LabeledWidget.extend({		
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.forwardToServer('select');
		},
		
		render$: function() {
			return $('<option>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		selected: function(v) {
			this.attr('selected', v);
		},
		
		value: function(v) {
			this.attr('value', v);
		}
		
	});
	
	/******************************************************************************************************************
	 * An iframe widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Iframe = cwf.widget.UIWidget.extend({
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<iframe>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		sandbox: function(v) {
			this.attr('sandbox', v);
		},
		
		src: function(v) {
			this.attr('src', v);
		}	
		
	});
	
	/******************************************************************************************************************
	 * A progress bar widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Progressbar = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._value = 0;
			this._max = 100;
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_pct: function() {
			var value = this._value || 0;
			var max = this._max || 100;
			var pct = max <= 0 ? 0 : value / max * 100;
			return pct > 100 ? 100 : pct;
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this._adjust();
		},
		
		render$: function() {
			return $('<div><div/><div/></div>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		_adjust: function(v) {
			v = v || this.widget$;
			v.children().last().width(this._pct() + '%');
		},
		
		label: function(v) {
			this.widget$.children().first().text(v);
		},
		
		maxValue: function(v) {
			this._max = v;
			this._adjust();
		},
		
		value: function(v) {
			this._value = v;
			this._adjust();
		}		
		
	});
	
	
	/******************************************************************************************************************
	 * A window widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Window = cwf.widget.UIWidget.extend({

		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		/*------------------------------ Events ------------------------------*/
		
		_onmaximize: function(event) {
			var size = this._buttonState('maximize') ? 'NORMAL' : 'MAXIMIZED';
			this.updateState('size', size);
		}, 
		
		_onminimize: function(event) {
			var size = this._buttonState('minimize') ? 'NORMAL' : 'MINIMIZED';
			this.updateState('size', size);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({mode: 'INLINE', size: 'NORMAL'});
			this.toggleClass('cwf_titled panel', true);
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_updateSizable : function() {
			var canResize = this.getState('sizable')
				&& this.getState('mode') !== 'INLINE'
				&& this.getState('size') === 'NORMAL',
				active = this.widget$.resizable('instance');
			
			if (!canResize !== !active) {
				if (canResize) {
					this.widget$.resizable({
						minHeight: 50,
						minWidth: 100,
						handles: 'all'});
				} else {
					this.widget$.resizable('destroy');
				}
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('minimize', this._onminimize.bind(this));
			this.widget$.on('maximize', this._onmaximize.bind(this));
		},
		
		getDragHelper: function() {
			return cwf.clone(this.sub$('titlebar'), -1);
		},
		
		render$: function() {
			var dom =
				  '<div>'
				+ '  <div class="panel-heading">'
				+ '    <div id="${id}-titlebar" class="panel-title">'
				+ '      <img id="${id}-image"/>'
				+ '      <span id="${id}-title"/>'
				+ '      <span id="${id}-icons" class="cwf_titled-icons"/>'
				+ '    </div>'
				+ '  </div>'
				+ '  <div id="${id}-inner" class="panel-body"/>'
				+ '</div>';
			return $(this.resolveEL(dom));
		},
				
		_buttonAdd: function(type, icons, position) {
			var id = this.subId(type);
			var btn = $('#' + id);
			
			if (btn.length) {
				return btn;
			}
			
			icons = icons.split(' ');
			btn = $('<span class="glyphicon glyphicon-' + icons[0] + '"/>')
				.attr('id', id)
				.data('position', position)
				.data('icons', icons)
				.data('state', 0);
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
			
			this.forward(btn, 'click', type);
			return btn;
		},
		
		_buttonRemove: function(type) {
			this.sub$(type).remove();
		},
		
		_buttonState: function(type, newState) {
			var btn = this.sub$(type),
				icons = btn.data('icons'),
				oldState = btn.data('state');
			
			if (!icons) {
				newState = 0;
			} else if (_.isNil(newState)) {
				newState = oldState;
			} else if (newState !== oldState) {
				btn.data('state', newState).removeClass('glyphicon-' + icons[oldState]).addClass('glyphicon-' + icons[newState]);
			}
			
			return newState;
		},

		/*------------------------------ State ------------------------------*/
		
		closable: function(v) {
			this[v ? '_buttonAdd' : '_buttonRemove']('close', 'remove', 9999);
			this.forwardToServer('close', !v);
		},
		
		dragid: function(v) {
			if (this.getState('mode') === 'INLINE') {
				this._super(v);
			} else {
				this._dragids = null;
			}
		},		
		
		image: function(v) {
			this.sub$('image').attr('src', v);
		},
		
		maximizable: function(v) {
			this[v ? '_buttonAdd' : '_buttonRemove']('maximize', 'resize-full resize-small', 10);
		},
		
		minimizable: function(v) {
			this[v ? '_buttonAdd' : '_buttonRemove']('minimize', 'chevron-down chevron-up', 20);
		},
		
		mode: function(v, oldmode) {
			var self = this,
				mask$ = this._ancillaries.mask$;
			
			v = v || 'INLINE';
			_mode(oldmode, true);
			_mode(v, false);
			this._updateSizable();
			this.widget$.draggable('instance') ? this.widget$.draggable('destroy') : null;
			this.applyState('dragid');
			
			if (v === 'MODAL') {
				mask$ = mask$ || $('#cwf_root').cwf$mask();
				mask$.cwf$show(this.getState('visible'));
				this.widget$.css('z-index', mask$.css('z-index'));
				this._ancillaries.mask$ = mask$;
			} else if (mask$) {
				mask$.remove();
				delete this._ancillaries.mask$;
				this.widget$.css('z-index', '');
			}
			
			if (v !== 'INLINE') {
				this.widget$.draggable({
					containment: 'window'})
				.position({
					my: 'center',
					at: 'center',
					of: 'body'});
			}
			
			function _mode(mode, remove) {
				mode ? self.toggleClass(self.subclazz(mode), !remove) : null;
			}
		},
		
		sizable: function(v) {
			this._updateSizable();
		},
		
		size: function(v)	 {
			var inline = 'INLINE' === this.getState('mode'),
				saved = this.getState('_savedState'),
				self = this,
				w$ = this.widget$;
			
			this._updateSizable();
			
			switch (v) {
				case 'NORMAL':
					this._buttonState('minimize', 0);
					this._buttonState('maximize', 0);
					this.sub$('inner').hide();
					_modifyState(saved);
					this.setState('_savedState', null);
					this.sub$('inner').show();
					break;
					
				case 'MAXIMIZED':
					_saveState();
					this._buttonState('minimize', 0);
					this._buttonState('maximize', 1);
					
					if (!inline) {
						_modifyState({
							left: 0,
							right: 0,
							top: 0,
							bottom: 0,
							height: null,
							width: null
						});
					}
					
					this.sub$('inner').show();
					break;
				
				case 'MINIMIZED':
					_saveState();
					this.sub$('inner').hide();
					this._buttonState('minimize', 1);
					this._buttonState('maximize', 0);
					var tbheight = this.widget$.children().first().css('height');
					
					if (inline) {
						_modifyState({
							height: tbheight
						});
					} else {
						_modifyState({
							left: 0,
							right: 'auto',
							top: null,
							bottom: 0,
							height: tbheight,
							width: null
						});
					}
					
					break;
			}
			
			function _modifyState(state) {
				if (state) {
					var s = w$[0].style;
					
					_.forOwn(state, function(value, key) {
						s[key] = value;
					});
				}
			}
			function _saveState() {
				if (!saved) {
					var s = w$[0].style;
					
					saved = {
					    left: s.left,
						right: s.right,
						top: s.top,
						bottom: s.bottom,
						height: s.height,
						width: s.width
					};
					
					self.setState('_savedState', saved);
				}
			}
		},
		
		title: function(v) {
			this.sub$('title').text(v);
		},
		
		visible: function(v) {
			this._super(v);
			var mask$ = this._ancillaries.mask$;
			
			if (mask$) {
				mask$.cwf$show(v);
				v ? this.widget$.css('z-index', mask$.css('z-index')) : null;
			}
		}
		
	});
	
	/******************************************************************************************************************
	 * A widget for displaying alerts (client side only)
	 ******************************************************************************************************************/ 
	
	cwf.widget.Alert = cwf.widget.Window.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.wclazz = 'cwf_window';
			this.initState({mode: 'MODAL', closable: true, sizable: true}, true);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.find('.glyphicon-remove').on('click', this.destroy.bind(this));
		},

		render$: function() {
			return this._super().appendTo('body');
		},
		
		/*------------------------------ State ------------------------------*/
		
		text: function(v) {
			this.widget$.find('.panel-body').text(v);
		}
		
	});
	
	
	return cwf.widget;
});