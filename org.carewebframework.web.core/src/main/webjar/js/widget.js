'use strict';

define('cwf-widget', ['cwf-core', 'bootstrap', 'jquery-scrollTo', 'css!balloon-css', 'css!jquery-ui', 'css!bootstrap-css', 'css!cwf-widget-css'], function(cwf) { 
	/* Widget support.  In the documentation, when we refer to 'widget' we mean an instance of the Widget
	 * class.  When we refer to 'widget$' (following the convention that a variable name ending in '$'
	 * is always a jquery object), we mean the jquery object contained by the widget.
	 */
	
	cwf.widget._fnTest = /xyz/.test(function(){xyz;}) ? /\b_super\b/ : /.*/;
	
	cwf.widget._domTemplates = {
			badge: '<span id="${id}-badge" class="badge" />',
			checkable: '<span id="${id}-chk" class="glyphicon"/>',
			closable: '<span id="${id}-cls" class="glyphicon glyphicon-remove"/>',
			image: '<img id="${id}-img" src="${_state.image}"/>',
			label: '<span id="${id}-lbl"/>',
			sortOrder: '<span id="${id}-dir" class="glyphicon"/>'
	};
	
	cwf.widget._radio = {};
	
	cwf.widget._popup = {};
	
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
	    			if (!cwf.widget._fnTest.test(subvalue)) {
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
			
			child = cwf.wgt(child);
			
			if (!child) {
				throw new Error('Child is not a valid widget.');
			}
			
			child._parent ? child._parent.removeChild(child) : null;
			index = _.isNil(index) || index >= this._children.length ? -1 : index;
			child._parent = this;
			child._attach(index);
			index < 0 ? this._children.push(child) : this._children.splice(index, 0, child);
			this.onAddChild(child);
		},
		
		anchor$: function() {
			return this.widget$;
		},
		
		getAncestor: function(wclass, wpkg) {
			var parent = this._parent;
			wpkg = wpkg || this.wpkg;
			
			while (parent && (parent.wclass !== wclass || parent.wpkg !== wpkg)) {
				parent = parent._parent;
			}
			
			return parent;
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
		 * Returns the level of a widget relative to its owning page.  If
		 * the widget is not attached to a page, returns -1.
		 * 
		 * @param {Object} [wgt] If specified, the widget associated with
		 * 		the parameter is considered.  Otherwise, this widget is used.
		 */
		getLevel: function(wgt) {
			var level = 0;
			wgt = wgt ? cwf.wgt(wgt) : this;
			
			while (wgt && wgt !== cwf.widget._page) {
				level++;
				wgt = wgt._parent;
			}
			
			return wgt ? level : -1;
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
			var currentIndex = this.getChildIndex(child);
			
			if (currentIndex >= 0) {
				var anchor$ = child.widget$.parent();
				this._children.splice(currentIndex, 1);
				child._detach(destroy);
				child._parent = null;
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
		 * @param {number} [index] Position of the widget relative to its siblings.
		 */
		_attach: function(index) {
			var ref = _.isNil(index) || index < 0 ? null : this._parent._children[index];
			this._attachWidgetAt(this.widget$, this._parent.anchor$(this.widget$), cwf.$(ref));
			this._attachAncillaries();
		},
		
		_attachAncillaries: function() {
			_.forOwn(this._ancillaries, function(ancillary) {
				var ancillary$ = cwf.$(ancillary),
					attach = ancillary$.data('attach') || _attach;
				
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
		 * @param {jquery} parent$ The widget to become the parent (if nil, must specify <code>ref$</code>).
		 * @param {jquery} ref$ The reference point for insertion (if nil, must specify <code>parent$</code>).
		 */
		_attachWidgetAt: function(widget$, parent$, ref$) {
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
			_.forOwn(this._ancillaries, function(ancillary, key, map) {
				var ancillary$ = cwf.$(ancillary),
					wgt = cwf.widget.isWidget(ancillary) ? ancillary : null;
				
				if (destroy) {
					delete map[key];
					wgt ? wgt.destroy() : ancillary$.remove();
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
			var fwd = this.getState('forwarding'),
				types = cwf.stringToSet(eventTypes, ' ');
			
			noforward ? cwf.removeFromSet(fwd, types) : _.assign(fwd, types);
			
			if (this.widget$) {
				this.widget$[noforward ? 'off' : 'on'](eventTypes, cwf.event.sendToServer);
			}
		},
		
		/**
		 * Notify server of a state change.
		 * 
		 * @param {string} state Name of the state.
		 * @param {*} value New value of the state.
		 */
		stateChanged: function(state, value) {
			this.trigger('statechange', {state: state, value: value});
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
			this.wclazz = this.wclazz || 'cwf_' + this.wclass.toLowerCase();
			this._rendering = false;
			this._state = {};
			this._state.forwarding = {statechange: true};
			this.initState(state, true);
			this.init();
			this.rerender();
		},
		
		/*------------------------------ Other ------------------------------*/
		
		/**
		 * Creates a translucent mask over this widget.
		 */
		addMask: function(label, popup) {
			this.removeMask();
			var zindex = this.widget$.cwf$zindex() + 1;
			this._mask$ = this.widget$.cwf$mask(zindex).append('<span>').css('display', 'flex');
			var span$ = this._mask$.children().first();
			span$.text(label).css('display', label ? '' : 'none');
			popup ? this.contextMenu(this._mask$, popup) : null;
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
			tgt$ ? tgt$.cwf$attr(attr, value) : null;
		},
		
		/**
		 * Locates the named widget within the namespace of this widget.
		 * 
		 * @param {string} name The name of the widget sought.
		 * @return {Widget} The widget sought, or nil result if not found.
		 */
		findByName: function(name) {
			return _find(this.getNamespace());
			
			function _find(wgt) {
				if (wgt && wgt.getState('name') !== name) {
					wgt.forEachChild(function (child) {
						var wgt2 = _find(child);
						
						if (wgt2) {
							wgt = wgt2;
							return false;
						}
					});
				}
				
				return wgt;
			}
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
		 * Returns the widget representing the enclosing namespace for this widget.
		 * 
		 * @return {Widget} The widget representing the enclosing namespace.
		 */
		getNamespace: function() {
			var wgt = this.nmsp ? this._parent : this,
				last = wgt;
			
			while (wgt && !wgt.nmsp) {
				last = wgt;
				wgt = wgt._parent;
			}
			
			return last;
		},
		
		/**
		 * Convenience method for setting a property value.
		 * 
		 * @param {string} prop The property name.
		 * @param value The new property value.
		 * @param {jquery} tgt$ The jquery object to receive the new property value.  If not
		 * 	specified, this widget's widget$ object will be used.
		 */
		prop: function(prop, value, tgt$) {
			tgt$ = tgt$ || this.widget$;
			tgt$ ? tgt$.cwf$prop(prop, value) : null;
		},
		
		/**
		 * Removes any existing mask;
		 */
		removeMask: function() {
			if (this._mask$) {
				this._mask$.remove();
				delete this._mask$;
			}
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
		 * Called after rendering is complete, including attachment to DOM.
		 * Override to apply event handlers, etc.
		 */
		afterRender: function() {
			// NOP
		},
		
		/**
		 * Called after the widget's DOM elements are constructed, but before
		 * applying state values or attaching to DOM.
		 */
		beforeRender: function() {
			// NOP
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
				
				this.beforeRender();
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
		 * Restore all server forwards.
		 */
		forwarding: function(v) {
			this.widget$.on(cwf.setToString(v, ' '), cwf.event.sendToServer);
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
		 * Assign name associated with the widget.
		 * 
		 * @param {string] v Name value.
		 */
		name: function(v) {
			this.attr('data-cwf-name', v);
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
		 * @return {boolean} True if the state value changed.
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
			
			return changed;
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
			this.initState({_clazz: this.wclazz, clazz: '', visible: true});
		},
				
		/*------------------------------ Other ------------------------------*/
		
		contextMenu: function(ele$, popup) {
			ele$.off('contextmenu.cwf');
			popup ? ele$.on('contextmenu.cwf', _showContextPopup) : null;
			
			function _showContextPopup(event) {
				var wgt = cwf.wgt(popup);
				
				if (!wgt) {
					ele$.off('contextmenu.cwf');
				} else {
					wgt.open({
						my: 'left top',
						at: 'right bottom',
						of: event
					});
				}
				
				return false;
			}
		},
		
		hoverPopup: function(ele$, popup) {
			ele$.off('mouseenter.cwf');
			ele$.off('mouseleave.cwf');
			popup ? ele$.on('mouseenter.cwf', _showHoverPopup) : null;
			popup ? ele$.on('mouseleave.cwf', _hideHoverPopup) : null;
			
			function _showHoverPopup(event) {
				var wgt = cwf.wgt(popup);
				
				if (!wgt) {
					ele$.off('mouseenter.cwf');
					ele$.off('mouseleave.cwf');
				} else {
					wgt.open({
						my: 'left top',
						at: 'right bottom',
						of: event
					});
				}
				
				return false;
			}
			
			function _hideHoverPopup() {
				var wgt = cwf.wgt(popup);
				wgt ? wgt.close() : null;
				return false;
			}
		},
		
		input$: function() {
			var input$ = this.sub$('inp');
			return input$.length ? input$ : this.widget$;
		},
		
		/**
		 * Replace one class with another.
		 * 
		 * @param {string} class1 The class to remove (may be nil).
		 * @param {string} class2 The class to add (may be nil).
		 * @param {boolean} [flip] If true, class1 and class2 meanings are flipped.
		 * @return {boolean} True if a class was added or removed.
		 */
		replaceClass: function(class1, class2, flip) {
			var result = class1 && this.toggleClass(class1, flip);
			result |= class2 && this.toggleClass(class2, !flip);
			return result;
		},
		
		scrollIntoView: function() {
			var sp$ = this.widget$.scrollParent();
			sp$ ? sp$.scrollTo(this.widget$) : null;
		},
		
		subclazz: function(sub, wclazz) {
			return sub ? (wclazz || this.wclazz) + '-' + sub.toLowerCase() : null;
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
		
		badge: function(v) {
			this.sub$('badge').text(v).cwf$swapClasses('badge-pos', 'badge-neg', v > 0).cwf$show(v);
		},
		
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
			this.contextMenu(this.widget$, v);
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
						stop: _stop,
						appendTo: 'body',
						iframeFix: true
					});
				}
			}
			
			function _helper() {
				var ele = self.getDragHelper();
				ele.className += ' cwf-dragging';
				return ele;
			}
			
			function _start(event, ui) {
				self._dragging = true;
				self.widget$.draggable('option', 'cursorAt', {
				    left: Math.floor(ui.helper.width() / 2),
				    top: Math.floor(ui.helper.height() / 2)
				});
			}
			
			function _stop() {
				self._dragging = false;
			}
		},
		
		dropid: function(v) {
			var self = this,
				active = !!this.widget$.droppable('instance'),
				newactive = !_.isNil(v);
			
			this._dropids = newactive ? cwf.stringToSet(v, ' ') : null;
			
			if (newactive !== active) {
				if (active) {
					this.widget$.droppable('destroy');
				} else {
					this.widget$.droppable({
						accept: _canDrop,
						tolerance: 'pointer',
						classes: {
							'ui-droppable-hover': 'cwf-droppable'
						}
					});
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
				this.widget$.on('keydown.cwf', _keyevent);
			} else {
				this._keycapture = null;
				this.widget$.off('keydown.cwf');
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
		
		popup: function(v) {
			this.hoverPopup(this.widget$, v);
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
	 * Connector widget.
	 ******************************************************************************************************************/
	
	cwf.widget.Connector = cwf.widget.BaseWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		onRemoveChild: function(child, destroyed, anchor$) {
			this._parent ? this._parent.removeChild(this, true) : null;
		},
		
		_detach: function(destroy) {
			if (destroy && this._children.length) {
				this.removeChild(this._children[0], destroy);
			}
			
			this._super(destroy);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $(this.resolveEL(this._dom));
		}
		
	});
	
	cwf.widget.Connector.create = function(dom, child) {
		var wgt = cwf.widget.create(null, {wclass: 'Connector', id: child.subId('cnc'), cntr: true, _dom: dom});
		wgt.addChild(child);
		return wgt;
	};
	
	/******************************************************************************************************************
	 * Base class for widgets wrapping input elements
	 ******************************************************************************************************************/ 
	
	cwf.widget.InputWidget = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({readonly: false});
		},
		
		/*------------------------------ Other ------------------------------*/
		
		selectAll: function() {
			this.selectRange(0, 99999);
		},
		
		selectRange: function(start, end) {
			try {
				this.input$()[0].setSelectionRange(start, end);
			} catch (e) {
			}
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
			this.trigger('change', {value: this._value()});
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
				ele.value = this.getState('value');
				ele.selectionStart = cpos;
				ele.selectionEnd = cpos;
				return;
			}
		    
			this.setState('value', value);
			
		    if (this._synchronized) {
		    	this.fireChanged();
		    } else {
		    	this._changed = true;
		    }
		},
		
		handleKeyup: function(event) {
			if (event.keyCode === 13) {
				this.trigger('enter');
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
				
		init: function() {
			this._super();
			this._synchronized = false;
			this._changed = false;
			this.forwardToServer('change');
			this.toggleClass('cwf_inputbox', true);
		},
		
		synced: function(v) {
			this._synchronized = v;
		},
		
		/*------------------------------ Other ------------------------------*/
		
		clear: function() {
			this.input$().val('');
		},
		
		_value: function() {
			return this.input$()[0].value;
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			var input$ = this.input$();
			input$.on('input propertychange', this.handleInput.bind(this));
			input$.on('blur', this.handleBlur.bind(this));
			input$.on('keyup', this.handleKeyup.bind(this));
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
			cwf.widget._page = this;
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
	 * Style widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Style = cwf.widget.MetaWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		realAnchor$: $('head'),
		
		renderReal$: function() {
			return $(this.getState('src') ? '<link type="text/css" rel="stylesheet">' : '<style>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		content: function(v) {
			this.rerender();
			this.real$.text(v);
		},
		
		src: function(v) {
			this.rerender();
			this.attr('href', v, this.real$);
		}
		
	});
	
	/******************************************************************************************************************
	 * Script widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Script = cwf.widget.MetaWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({async: false, defer: false});
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		realAnchor$: $('body'),
		
		renderReal$: function() {
			return $('<script>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		async: function(v) {
			this.attr('async', v, this.real$);
		},
		
		content: function(v) {
			this.real$.text(this.resolveEL(v, '#'));
		},
		
		defer: function(v) {
			this.attr('defer', v, this.real$);
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
		
		/*------------------------------ Other ------------------------------*/
		
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
			this.setState('src', null);
		},
		
		src: function(v) {
			var self = this;
			this.widget$.children().remove();
			this.setState('content', null);
			
			if (v) {
				require(['text!' + v], function(html) {
					self.widget$.append($(html));
				});
			}
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
	
	cwf.widget.Popup = cwf.widget.BaseWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.real$;
		},
		
		/*------------------------------ Events ------------------------------*/
		
		moveHandler: function(event) {
			this._options.of = event.relatedTarget;
			this.real$.position(this._options);
		},
		
		_trigger: function(which, notself) {
			var event = $.Event(which, {
				relatedTarget: this._related$ ? this._related$.cwf$widget() : null
			});
			
			notself ? null : this.trigger(event);
			event.relatedTarget ? event.relatedTarget.trigger(event) : null;
		},
		
		/*------------------------------ Other ------------------------------*/
		
		close: function(notself, notothers) {
			if (this.isOpen()) {
				cwf.widget.Popup.registerPopup(this, false);
				this.real$.hide().cwf$track(this._related$, true);
				this._trigger('close', notself);
				this._related$ = null;
				this._options = null;
			}
			
			notothers ? null : cwf.widget.Popup.closePopups(this.real$);
		},
		
		isOpen: function() {
			return cwf.widget._popup[this.id];
		},
		
		open: function(options, notself) {
			var related$;
			
			if (options.of.currentTarget) {
				related$ = cwf.$(options.of.currentTarget);
			} else {
				related$ = cwf.$(options.of);
				options.of = related$;
			}
			
			if (!this.isOpen() || (related$ && !related$.is(this._related$))) {
				cwf.widget.Popup.closePopups(related$);
				this._related$ = related$;
				this.real$.css('z-index', this._related$.cwf$zindex() + 1);
				this.real$.cwf$track(this._related$);
				options.collision = options.collision || 'flipfit';
				this._options = options;
				this.real$.show().position(options);
				cwf.widget.Popup.registerPopup(this, true);
				this._trigger('open', notself);
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.real$.on('move', this.moveHandler.bind(this));
			this._allowBubble ? null : this.real$.on('click', false);
		},
		
		render$: function() {
			if (!this.real$) {
				this.real$ = this.renderReal$()
					.appendTo('#cwf_root')
					.attr('id', this.subId('real'))
					.attr('data-cwf-popup', true)
					.hide()
					.addClass(this.wclazz);
				this._ancillaries.real$ = this.real$;
			}
			
			return $('<span>');
		},
		
		renderReal$: function() {
			return $('<div>')
		}
		
	});
	
	cwf.widget.Popup.registerPopup = function(wgt, v) {
		if (!wgt.close) {
			throw new Error('Widget must implement close method.');
		}
		
		if (v) {
			cwf.widget._popup[wgt.id] = wgt;
		} else {
			delete cwf.widget._popup[wgt.id];
		}
	};
	
	cwf.widget.Popup.closePopups = function(parent$) {
		var parent = parent$ ? parent$.closest('*[data-cwf-popup]')[0] : null;
		
		_.forOwn(cwf.widget._popup, function(popup) {
			if (!parent || $.contains(parent, popup._related$[0])) {
				popup.close(false, true);
			}
		});
	};
	
	
	/******************************************************************************************************************
	 * A toolbar widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Toolbar = cwf.widget.UIWidget.extend({

		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({alignment: 'START', orientation: 'HORIZONTAL'});
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<div/>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		alignment: function(v, old) {
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
		},
	
		position: function(v) {
			this.toggleClass('cwf_labeled-left', v === 'LEFT');
			this.toggleClass('cwf_labeled-right', v === 'RIGHT');
			this.toggleClass('cwf_labeled-top', v === 'TOP');
			this.toggleClass('cwf_labeled-bottom', v === 'BOTTOM');
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

		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.toggleClass('btn', true);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = '<button>'
				    + this.getDOMTemplate(':image', 'label')
					+ '</button>';
			
			return $(this.resolveEL(dom));
		}
	
	});
	
	/******************************************************************************************************************
	 * A hyperlink widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Hyperlink = cwf.widget.Button.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			 var dom = '<a>'
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
	 * A caption widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Caption = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
				
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({position: 'left', alignment: 'start', labelClass: 'label-default'})
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_updateClass: function(oldc, newc) {
			oldc = this.subclazz(oldc.toLowerCase());
			newc = this.subclazz(newc.toLowerCase());
			this.replaceClass(oldc, newc);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			 var dom = '<span>'
				    + 	this.getDOMTemplate('label')
				    + 	'<span id="${id}-inner"/>'
					+ '</span>';
 
			return $(this.resolveEL(dom));
		},
	
		/*------------------------------ State ------------------------------*/
		
		alignment: function(v, old) {
			this._updateClass(old || '', v || 'start');
		},
		
		labelClass: function(v) {
			this.attr('class', v, this.sub$('lbl'));
		},
		
		labelStyle: function(v) {
			this.attr('style', v, this.sub$('lbl'));
		},
		
		position: function(v, old) {
			this._updateClass(old || '', v || 'left');
		}
	});
	
	/******************************************************************************************************************
	 * A page control widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Paging = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ Events ------------------------------*/
		
		handleClick: function(event) {
			var page = this.getState('currentPage'),
				max = this.getState('maxPage');
			
			switch ($(event.target).data('cwf-pg')) {
				case -2: // Start
					page = 0;
					break;
					
				case -1: // Previous
					page = page - 1;
					break;
					
				case 1: // Next
					page = page + 1;
					break;
					
				case 2: // End
					page = max;
					break;
			}
			
			if (page >= 0 && page <= max) {
				this.setState('currentPage', page);
				this._update();
				this.trigger('change', {value: page});
			}
			
			return false;
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({currentPage: 0, maxPage: 0, pageSize: 0});
			this.forwardToServer('change');
			this.toggleClass('btn-toolbar', true);
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_update: function() {
			var label = this.getState('label') || '%c / %m',
				page = this.getState('currentPage'),
				max = this.getState('maxPage');
			
			label = label.replace('%c', page + 1).replace('%m', max + 1);
			this.sub$('lbl').text(label);
			this.widget$.find('a').each(function() {
				var a$ = $(this),
					i = a$.data('cwf-pg'),
					disabled = (i < 0 && page < 1) || (i > 0 && page >= max);
				
				a$.attr('disabled', disabled);
			});
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this.widget$.find('a').on('click.cwf', this.handleClick.bind(this));
		},
		
		render$: function() {
			var dom = 
				  '<span>'
				+   '<a data-cwf-pg="-2" class="glyphicon glyphicon-chevron-left-double"></a>'
				+   '<a data-cwf-pg="-1" class="glyphicon glyphicon-chevron-left"></a>'
				+ 	this.getDOMTemplate('label')
				+   '<a data-cwf-pg="1" class="glyphicon glyphicon-chevron-right"></a>'
				+   '<a data-cwf-pg="2" class="glyphicon glyphicon-chevron-right-double"></a>'
				+ '</span>'
			return $(this.resolveEL(dom));
		},
	
		/*------------------------------ State ------------------------------*/
		
		currentPage: function(v) {
			this._update();
		},
		
		label: function(v) {
			this._update();
		},
		
		maxPage: function(v) {
			this._update();
		},
		
		pageSize: function(v) {
			this._update();
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
		
		handleChange: function(event, params) {
			this._syncChecked(true);
			var target = event.target;
			target.value = target.checked;
			cwf.event.sendToServer(event, params);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('change', this.handleChange.bind(this));
		},
		
		render$: function() {
			var dom =
				'<div>'
			  +   '<input id="${id}-real" type="checkbox">'
			  +   '<label id="${id}-lbl" for="${id}-real"/>'
			  + '</div>';
			
			return $(this.resolveEL(dom));
		},
	
		/*------------------------------ State ------------------------------*/
		
		checked: function(v) {
			this.sub$('real').prop('checked', v);
			this._syncChecked(v);
		},
		
		_syncChecked: function(checked) {
			// NOP
		}
		
	});
	
	/******************************************************************************************************************
	 * A radio button widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Radiobutton = cwf.widget.Checkbox.extend({
	
		/*------------------------------ Other ------------------------------*/
		
		getGroup: function() {
			var wgt = this._parent;
			
			while (wgt && wgt.wclass !== 'Radiogroup') {
				wgt = wgt._parent;
			}
			
			return wgt ? wgt.id : null;
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom =
				'<div>'
			  +   '<input id="${id}-real" type="radio" name="${getGroup}">'
			  +   '<label id="${id}-lbl" for="${id}-real"/>'
			  + '</div>';
			
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		_syncChecked: function(checked) {
			var group = this.getGroup();
			
			if (group) {
				var previous = cwf.widget._radio[group];
				previous = previous ? cwf.widget.find(previous) : null;
				
				if (checked) {
					if (previous && previous !== this) {
						previous.trigger('change', {value: false});
					}
					
					cwf.widget._radio[group] = this.id;
				} else if (previous === this) {
					delete cwf.widget._radio[group];
				}
			}
		}
		
	});
	
	/******************************************************************************************************************
	 * Widget for grouping radio buttons
	 ******************************************************************************************************************/ 
	
	cwf.widget.Radiogroup = cwf.widget.Span.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({orientation: 'HORIZONTAL'});
		},		
		
		/*------------------------------ State ------------------------------*/
		
		orientation: function(v) {
			this.toggleClass(this.subclazz(v.toLowerCase()), true);
			this.toggleClass(this.subclazz(v === 'VERTICAL' ? 'horizontal' : 'vertical'), false);
		}
	});
	
	/******************************************************************************************************************
	 * A menu popup widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menupopup = cwf.widget.Popup.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this._allowBubble = true;
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		renderReal$: function() {
			return $('<ul role="menu" class="dropdown-menu multi-level" />');
		}
		
	});
	
	/******************************************************************************************************************
	 * A menu widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Menu = cwf.widget.LabeledImageWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this._ancillaries.popup ? this._ancillaries.popup.anchor$() : this._super();
		},
						
		onAddChild: function() {
			this._childrenUpdated();
		},
		
		onRemoveChild: function() {
			this._childrenUpdated();
		},
		
		/*------------------------------ Events ------------------------------*/
			
		handleClick: function(event) {
			this._children.length ? this.toggle() : null;
			return false;
		},
		
		handleOpenClose: function(event) {
			this.setState('_open', event.type === 'open');
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({_open: false});
			this.forwardToServer('open close');
		},
		
		/*------------------------------ Other ------------------------------*/
		
		open: function() {
			if (this._ancillaries.popup) {
				this.close();

				this._ancillaries.popup.open({
					my: 'left top',
					at: 'left bottom',
					of: this.widget$
				});
				
				this.setState('_open', true);
			}
		},
		
		close: function() {
			if (this._ancillaries.popup) {
				this._ancillaries.popup.close();
				this.setState('_open', false);
			}
		},
		
		toggle: function() {
			this.getState('_open') ? this.close() : this.open();
		},
		
		_childrenUpdated: function() {
			this.toggleClass(this.subclazz('nochildren'), !this._children.length);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('open close', this.handleOpenClose.bind(this));
			this.widget$.on('click', this.handleClick.bind(this));
			this._childrenUpdated();

			if (!this._ancillaries.popup) {
				this._ancillaries.popup = cwf.widget.create(null, {wclass: 'Menupopup'});
				this.widget$.append(this._ancillaries.popup.widget$);
			} else if (this.getState('_open')) {
				this.open();
			}
		},
		
		render$: function() {
			var dom = 
				  '<span>'
				+   '<div class="dropdown" style="display: inline-block" role="presentation">'
				+     '<a id="${id}-btn" role="button" aria-haspopup="true" aria-expanded="false">'
				+ 		 this.getDOMTemplate(':image', 'label')
				+       '<span class="caret"></span>'
				+     '</a>'
				+   '</div>'
				+ '</span>';
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		clazz: function(v) {
			this._super();
			this.attr('class', v, this.sub$('btn'));
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
			this.trigger(event);
			return false;
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
					+   '<a>'
					+ this.getDOMTemplate(':image', ':checkable', 'label')
					+   '</a>'
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
			this.attr('type', v ? 'password' : this._type, this.input$());
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
				
		/*------------------------------ Other ------------------------------*/
		
		scrollToBottom: function() {
			var input$ = this.input$();
			input$.scrollTop(input$[0].scrollHeight);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $(this.resolveEL('<span><textarea id="${id}-inp"/></span>'));
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
	
	cwf.widget.Popupbox = cwf.widget.Textbox.extend({
		
		/*------------------------------ Events ------------------------------*/
		
		handleOpenClose: function(event) {
			this.setState('_open', event.type === 'open');
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({_open: false});
		},
		
		/*------------------------------ Other ------------------------------*/
		
		open: function() {
			this.close();
			var popup = this._popup();
			
			if (popup) {
				popup.open({
					my: 'left top',
					at: 'left bottom',
					of: this.widget$
				});
				
				this.setState('_open', true);
			}
		},
		
		close: function() {
			var popup = this._popup();
			
			if (popup) {
				popup.close();
				this.setState('_open', false);
			}
		},
		
		_popup: function() {
			return cwf.wgt(this.getState('popup'));
		},
		
		toggle: function() {
			this.getState('_open') ? this.close() : this.open();
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('open close', this.handleOpenClose.bind(this));
		},
		
		render$: function() {
			var dom =
				'<span>'
			  +   '<input id="${id}-inp" type="text">'
			  +   '<span id="${id}-btn" class="glyphicon glyphicon-triangle-bottom" />'
			  + '</span>';
			
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		popup: function(v) {
			var btn$ = this.sub$('btn'),
				self = this;
			
			btn$.off('click.cwf');
			v ? btn$.on('click.cwf', _showPopup) : null;
			
			function _showPopup(event) {
				self.toggle();
				return false;
			}
		},
		
		readonly: function(v) {
			this._super.apply(this, arguments);
			var self = this,
				inp$ = this.input$();
			
			inp$.off('click.cwf');
			v ? inp$.on('click.cwf', _click) : null;
			
			function _click() {
				self.sub$('btn').triggerHandler('click');
				return false;
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
	 * A time box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Timebox = cwf.widget.InputboxWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._type = 'time';
			this._super();
		}
		
	});
	
	/******************************************************************************************************************
	 * A list box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Listbox = cwf.widget.UIWidget.extend({

		/*------------------------------ Events ------------------------------*/

		handleChange: function(event) {
			this.forEachChild(function(child) {
				child.syncSelected();
			});
			
			event.cwf_nosend = true;
			return false;
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('change', this.handleChange.bind(this));
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
		
		/*------------------------------ Events ------------------------------*/
		
		handleChange: function(event) {
			this.syncSelected(true);
		},
		
		handleClick: function(event) {
			if (!this._dragging) {
				this.selected(true);
				this._parent.handleChange();
				this._parent.focus();
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({selected: false});
			this.forwardToServer('change');
		},
		
		/*------------------------------ Other ------------------------------*/
		
		syncSelected: function(noevent) {
			var selected = this.widget$.is(':selected');
			
			if (this.setState('selected', selected) && !noevent) {
				this.trigger('change', {value: selected});
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.widget$.on('change', this.handleChange.bind(this));
		},
		
		render$: function() {
			return $('<option role="presentation">');
		},
		
		/*------------------------------ State ------------------------------*/
		
		dragid: function(v, old) {
			this._super.apply(this, arguments);
			this.widget$.off('mouseup.cwf');
			
			if (v) {
				this.widget$.on('mouseup.cwf', this.handleClick.bind(this));
			}
		},
		
		selected: function(v) {
			this.prop('selected', v);
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
			if (event.target.tagName === 'LI') {
				return;
			}
			
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
		
		handleChange: function(event, ui) {
			var wgt = ui.item ? cwf.widget.find(ui.item.id) : null;
			
			if (wgt && wgt.setState('selected', true)) {
				wgt.trigger('change', {value: true});
			}
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
				change: this.handleChange.bind(this),
				select: this.handleChange.bind(this)
			});
			
			inp$.data('ui-autocomplete')._renderItem = this.renderItem$.bind(this);
			inp$.on('blur', this.handleBlur.bind(this));
		},
		
		render$: function() {
			var dom =
				'<span>'
			  +   '<input id="${id}-inp" type="text">'
			  +   '<span id="${id}-btn" class="glyphicon glyphicon-triangle-bottom" />'
			  +   '<select id="${id}-inner" class="hidden" />'
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
		},
		
		readonly: function(v) {
			this._super.apply(this, arguments);
			var self = this,
				btn$ = this.sub$('btn');
			
			this.widget$.off('click.cwf');
			btn$.off('click.cwf');
			v ? this.widget$.on('click.cwf', _dropdown) : null;
			v ? null : btn$.on('click.cwf', _dropdown);
			
			function _dropdown(event) {
				self.handleClick(event);
			}
		}
		
	});
	
	/******************************************************************************************************************
	 * A combo box item widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Comboitem = cwf.widget.LabeledWidget.extend({		
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.forwardToServer('change');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
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
				
		/*------------------------------ Events ------------------------------*/
		
		handleLoad: function(event) {
			event.src = '';
			
			try {
				var src = event.target.contentWindow.location.href;
				this.setState('src', src);
				event.src = src;
			} catch(e) {};
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.forwardToServer('load');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this.widget$.on('load', this.handleLoad.bind(this));
		},
		
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
	 * A group box widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Groupbox = cwf.widget.UIWidget.extend({
				
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = 
				'<div>'
			  +   '<fieldset>'
			  +     '<legend id="${id}-title"/>'
			  +     '<span id="${id}-inner"/>'
			  +   '</fieldset>'
			  + '</div>';
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		title: function(v) {
			this.sub$('title').text(v);
		}
		
	});
	
	/******************************************************************************************************************
	 * A progress bar widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Progressbar = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({maxValue: 100, value: 0})
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_pct: function() {
			var value = this.getState('value'),
				max = this.getState('maxValue'),
				pct = max <= 0 ? 0 : value / max * 100;
			
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
			this._adjust();
		},
		
		value: function(v) {
			this._adjust();
		}		
		
	});
		
	/******************************************************************************************************************
	 * A slider widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Slider = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Events ------------------------------*/
		
		handleChange: function(event, ui) {
			var value = ui.value;
			
			if (this.setState('value', value)) {
				this.trigger('change', {value: value});
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({value: 0, maxValue: 100, minValue: 0, step: 1, orientation: 'HORIZONTAL', synced: false});
			this.forwardToServer('change');
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_slider: function(opt, value) {
			if (!this.widget$.slider('instance')) {
				this.widget$.slider({});
			}
			
			this.widget$.slider('option', opt, value);
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<div/>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		maxValue: function(v) {
			this._slider('max', v);
		},
		
		minValue: function(v) {
			this._slider('min', v);
		},
		
		orientation: function(v) {
			this._slider('orientation', v.toUpperCase());
		},
		
		step: function(v) {
			this._slider('step', v);
		},
		
		synced: function(v) {
			this.widget$.off('slidechange slide');
			this.widget$.on(v ? 'slide' : 'slidechange', this.handleChange.bind(this));
		},
		
		value: function(v) {
			this._slider('value', v);
		}
		
	});
	
		
	/******************************************************************************************************************
	 * A slide-down message window.
	 ******************************************************************************************************************/ 
	
	cwf.widget.Messagewindow = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		onAddChild: function(child) {
			this.widget$.show();
			child._slide(true, 'fast', function() {
				child.widget$.height('auto');
			});
		},
		
		onRemoveChild: function(child) {
			!this.getChildCount() ? this.widget$.hide() : null;
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = '<div>'
					+   '<div id="${id}-inner"/>'
					+ '</div>';
			return $(this.resolveEL(dom));
		}
		
	});
	
	/******************************************************************************************************************
	 * Message pane for slide-down message window.
	 ******************************************************************************************************************/ 
	
	cwf.widget.Messagepane = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		_detach: function(destroy) {
			destroy ? this._clearTimeout() : null;
			this._super(destroy);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({duration: 8000});
			this.toggleClass('alert', true);
			this.forwardToServer('close');
		},
		
		/*------------------------------ Other ------------------------------*/
		
		_slide : function(down, duration, complete) {
			var w$ = this.widget$,
				start = down ? 0 : w$.outerHeight(),
				end = down ? w$.outerHeight() : 0;
			w$.outerHeight(start);
			w$.animate({height: end}, duration || 'slow', complete);
		},
		
		_clearTimeout: function() {
			if (this._timeout) {
				clearTimeout(this._timeout);
				delete this._timeout;
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this._buttonAdd('remove', 'close');
		},
		
		render$: function() {
			var dom = '<div>'
				    +   '<div>'
				    +     '<span id="${id}-title" class="${wclazz}-title"/>'
				    +     '<span id="${id}-icons" class="cwf_titled-icons"/>'
				    +   '</div>'
				    +	'<div id="${id}-inner"/>'
				    + '</div>';
			return $(this.resolveEL(dom));
		},
		
		_buttonAdd: function(type, forward) {
			var btn$ = $('<span>').addClass('glyphicon glyphicon-' + type).appendTo(this.sub$('icons'));
			this.forward(btn$, 'click', forward);
		},
		
		/*------------------------------ State ------------------------------*/
		
		actionable: function(v) {
			v ? this._buttonAdd('flash', 'action') : null;
		},
		
		duration: function(v) {
			var self = this;
			this._clearTimeout();
			
			if (v > 0) {
				this._timeout = setTimeout(_timeout, v);
			}
			
			function _timeout() {
				delete self._timeout;
				self._slide(false, null, function() {
					self.trigger('close');
				});
			}
		},
		
		title: function(v) {
			this.sub$('title').text(v);
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
		
		handleMaximize: function(event) {
			var size = this._buttonState('maximize') ? 'NORMAL' : 'MAXIMIZED';
			this.updateState('size', size);
		}, 
		
		handleMinimize: function(event) {
			var size = this._buttonState('minimize') ? 'NORMAL' : 'MINIMIZED';
			this.updateState('size', size);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({mode: 'INLINE', size: 'NORMAL'});
			this.toggleClass('cwf_titled panel', true);
			this.forwardToServer('close');
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
			this.widget$.on('minimize', this.handleMinimize.bind(this));
			this.widget$.on('maximize', this.handleMaximize.bind(this));
		},
		
		getDragHelper: function() {
			return cwf.clone(this.sub$('titlebar'), -1);
		},
		
		render$: function() {
			var dom =
				  '<div>'
				+   '<div class="panel-heading">'
				+     '<div id="${id}-titlebar" class="panel-title">'
				+       '<img id="${id}-image"/>'
				+       '<span id="${id}-title"/>'
				+       '<span id="${id}-icons" class="cwf_titled-icons"/>'
				+     '</div>'
				+   '</div>'
				+   '<div id="${id}-inner" class="panel-body"/>'
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
			this.widget$.removeAttr('data-cwf-popup');
			
			if (v === 'MODAL') {
				this.widget$.attr('data-cwf-popup', true);
				mask$ = mask$ || $('body').cwf$mask(++cwf.widget._zmodal);
				mask$.cwf$show(this.getState('visible'));
				this.widget$.css('z-index', mask$.css('z-index'));
				this._ancillaries.mask$ = mask$;
			} else if (mask$) {
				mask$.remove();
				delete this._ancillaries.mask$;
			}
			
			if (v !== 'MODAL') {
				this.widget$.css('z-index', v === 'POPUP' ? ++cwf.widget._zmodal : null);
			}
			
			if (v !== 'INLINE') {
				this.widget$.draggable({
					containment: '#cwf_root'})
				.position({
					my: 'center',
					at: 'center',
					of: '#cwf_root'});
				this.scrollIntoView();
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