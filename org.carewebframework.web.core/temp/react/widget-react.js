define('cwf-widget', ['cwf-core', 'jquery', 'react', 'react-dom', 'lodash', 'css!bootstrap-css.css', 'css!cwf-css.css'], function(cwf, $, React, ReactDOM) { 

	/************************************************************
	 * Base extensible component class.
	 ************************************************************/ 
	cwf.widget.BaseExtensibleComponent = {
	
		abstract_: true,
		
		/*--------------- Inheritance ---------------*/

		_super: function() {
			console.log('Warning: call to dummy _super method was ignored.');
		},
		
		extend: function(subclass) {
			var superclass = this,
				fnTest = /xyz/.test(function(){xyz;}) ? /\b_super\b/ : /.*/;
			
			subclass = subclass || {};
			subclass.superclass_ = superclass;
			
			_.forIn(superclass, function(supervalue, name) {
				if (name.endsWith('_')) {
					return;
				}
				
				var subvalue = subclass[name];
				
				if (_.isUndefined(subvalue)) {
					subclass[name] = supervalue;
					return;
				}
				
				if (!_.isFunction(supervalue) || !_.isFunction(subvalue)) {
					return;
				}
				
				if (fnTest.test(subvalue)) {
			    	subclass[name] = (function(fn) {
		    			return function() {
		    				var ret, tmp = this._super;
		    				this._super = supervalue;
		    				
		    				try {
		    					ret = fn.apply(this, arguments);  
		    				} finally {
		    					this._super = tmp;
		    				}
		    				
		    				return ret;
		    			};
			    	})(subvalue);
				}
			});
			
			return subclass;
		},
		
		/*--------------- Lifecycle ---------------*/
		
		create: function(parent, props, state) {
			if (this.abstract_) {
				throw new Error('Cannot create instance of abstract class.');
			}
			
			props = props || {};
			state = state || {};
			this.beforeCreate(props, state);
			props.id = props.id || cwf.uniqueId();
			props.key = props.key || props.id;
			var wgt = this.factory(props, state);
			
			if (parent) {
				if (_.isElement(parent)) {
					ReactDOM.render(wgt, parent);
				} else if (parent.addChild) {
					parent.addChild(wgt);
				} else {
					throw new Error('Invalid parent specified.')
				}
			}
			
			return wgt;
		}, 
		
		beforeCreate: function(props, state) {
			// Override to perform additional setup
		},
		
		afterCreate: function() {
			// Override to perform additional setup
		},
		
		beforeDestroy() {
			// Override to perform additional cleanup
		},
		
		factory: function(props, state) {
			// Override to create component instances
		},
		
		destroy: function() {
			this.beforeDestroy();
			cwf.widget.unregister(this.props.id);
			this._destroyed = true;
			this._wrapper = null;
		},
		
		/*--------------- Widget Methods ---------------*/
		
		subId: function(sub, dlm) {
			return this.props.id + (dlm || '-') + sub;
		},
		
		getWidget: function(v) {
			return cwf.widget.find(v);
		},
		
		getPage: function() {
			var i = this.props.id.indexOf('_', 4),
				pageId = i === -1 ? this.props.id : this.props.id.substring(0, i);
			
			return this.getWidget(pageId);
		}
	};
	
	/************************************************************
	 * Base class for widgets that have components that must be 
	 * rendered to different parents within the DOM.
	 ************************************************************/ 
	cwf.widget.ComponentCollection = cwf.widget.BaseExtensibleComponent.extend({
	
		/*--------------- Lifecycle ---------------*/
		
		getMemberClasses: function() {
			return {}; // override to return member classes
		},
		
		factory: function(props, state) {
			var wgt = _.assign(new Object(), this);
			wgt.props = props;
			wgt.state = state;
			wgt.id = props.id;
			wgt.key = props.id;
			wgt.initMembers();
			cwf.widget.register(wgt.id, wgt);
			wgt.afterCreate();
			return wgt;
		},
		
		destroy: function() {
			var self = this;
			this._super();
			
			_.forOwn(this.members, function(member) {
				self.getWidget(member).destroy();
			});
			
			delete this.members;
		},
		
		initMembers: function() {
			var self = this;
			
			this.members = _.mapValues(this.getMemberClasses(), function(clazz, name) {
				var id = self.subId(name, '~'),
					wclass = self.props.wclass_ + '_' + name,
					props = cwf.combine([self.props, {id: id, key: id, wclass_: wclass}]),
					state = cwf.combine([self.state]),
					wgt = clazz.create(null, props, state);
				
				if (clazz.addChild) {
					self.anchor = id;
				}
				
				return wgt;
			});
		},
		
		/*--------------- State ---------------*/
		
		setState: function(state) {
			var self = this;
			
			_.forOwn(this.members, function(member) {
				self.getWidget(member).setState(state);
			});
		},
		
		getMember: function(name) {
			return this.members[name];
		},
		
		getAnchor: function() {
			return this.getWidget(this.anchor);
		},
		
		addChild: function(child, index) {
			return this.getAnchor().addChild(child, index);
		},
		
		removeChild: function(child) {
			return this.getAnchor().removeChild(child);
		},
		
		destroyChild: function(child) {
			return this.getAnchor().destroyChild(child);
		}
		
	});
	
	/************************************************************
	 * Base class for all React-based widgets.
	 ************************************************************/ 
	cwf.widget.BaseReactComponent = cwf.widget.BaseExtensibleComponent.extend({
	
		abstract_: true,
		
		propTypes: {id: React.PropTypes.string.isRequired},
		
		/*--------------- Inheritance ---------------*/

		extend: function(subclass, propTypes) {
			subclass = this._super(subclass);
			
			if (propTypes || subclass.propTypes) {
				subclass.propTypes = cwf.combine([subclass.propTypes, propTypes]);
			}
			
			return subclass;
		},
		
		/*--------------- Lifecycle ---------------*/
		
		factory: function(props, state) {
			props.initialState_ = state;
			
			if (!this.factory_) {
				this.factory_ = React.createClass(this);
			}
			
			return React.createElement(this.factory_, props);
		}, 
		
		afterCreate: function() {
			this.forwardToServer('stateChange');
		},
		
		componentWillMount: function() {
			this.id = this.props.id;
			this._wrapper = this._reactInternalInstance._currentElement;
			cwf.widget.register(this.props.id, this);
		},
		
		componentWillUnmount: function() {
			if (!this._destroyed) {
				_.assign(this.props.initialState_, this.state);
			}
		},
		
		/*--------------- State ---------------*/
		
		getInitialState: function() {
			this.afterCreate();
			return this.props.initialState_;
		},
		
		mergeState: function(superState, state) {
			return cwf.combine([state, superState]);
		},
		
		stateChanged: function(state, value) {
			this.trigger('stateChange', {data: {state: state, value: value}});
		},
		
		name: function(v) {
			this.setState({name: v});
		},

		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			return cwf.combine([this.state, this.props], function(value, key){
				return !key.endsWith('_');
			});
		},
		
		/*--------------- Events ---------------*/
		
		/**
		 * Turns on/off server forwarding of an event type.
		 * 
		 * @param eventTypes {string || string[]} Array or space-delimited list of event types (e.g., 'click change').
		 * @param noforward {boolean} If true, the event will not be forwarded to the server.
		 * 		If false, forwarding will occur.
		 */ 
		forwardToServer: function(eventTypes, noforward) {
			var self = this;
			
			if (!_.isArray(eventTypes)) {
				eventTypes = eventTypes.split(' ');
			}
			
			_.forEach(eventTypes, function(type) {
				self.registerEventHandler(type, cwf.event.sendToServer, noforward);
			});
		},
		
		registerEventHandler(eventType, handler, remove) {
			if (!handler) {
				throw new Error('Event handler not found.');
			}
			
			eventType = cwf.event.removeOn(eventType);
			
			if (!this._eventHandlers) {
				this._eventHandlers = {};
			}
			
			var handlers = this._eventHandlers[eventType],
				changed;
			
			if (!handlers) {
				if (remove) {
					return;
				}
				
				handlers = this._eventHandlers[eventType] = [];
			}
			
			var i = handlers.indexOf(handler);
			
			if (remove) {
				if (i >= 0) {
					handlers.splice(i, 1);
					changed = true;
				}
			} else {
				if (i === -1) {
					handlers.push(handler);
					changed = true;
				}
			}
			
			if (changed) {
				var newState = this.state ? {} : this.props.initialState_;
				newState[cwf.event.addOn(eventType)] = handler.length ? this.handleEvent : null;
				
				if (this.state) {
					this.setState(newState);
				}
			}
		},
		
		/**
		 * Handler for all events.
		 */ 
		handleEvent: function(event) {
			var type = cwf.event.removeOn(event.type),
				handlers = this._eventHandlers[type],
				self = this;
			
			if (handlers) {
				_.forEach(handlers, function(handler) {
					handler.call(self, event);	
				});
				
				event.stopPropagation();
			}
		},
		
		trigger: function(event, params) {
			if (_.isString(event)) {
				event = $.Event(event, {target: this});
			}
			
			if (params) {
				_.assign(event, params);
			}
			
			this.handleEvent(event);
		},
		
		/*--------------- Rendering ---------------*/
		
		renderHelper: function() {
			if (this._renderhelpers) {
				var self = this,
					args = arguments;
				
				_.forIn(this._renderhelpers, function(renderhelper) {
					renderhelper.apply(self, args);
				});
			}
		},
		
		registerRenderHelper: function(key, helper) {
			this._renderhelpers = this._renderhelpers || {};
			
			if (helper) {
				this._renderhelpers[key] = helper;
			} else {
				delete this._renderhelpers[key];
			}
			
			this.setState({ref: this.renderHelper});
		}
	});
	
	/************************************************************
	 * Base class for all UI widgets.
	 ************************************************************/ 
	cwf.widget.BaseUIComponent = cwf.widget.BaseReactComponent.extend({

		abstract_: true,
		
		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			var props = this._super();
			
			if (!this.state.visible_ || this.state.hidden_) {
				props.className += ' hidden';
			}
			
			return props;
		},
		
		/*--------------- State ---------------*/
		
		getInitialState: function() {
			return this.mergeState(this._super(), {visible_: true});
		},
		
		className: function(v) {
			this.setState({className: v});
		},
		
		style: function(v) {
			this.setState({style: v});
		},
		
		disabled: function(v) {
			this.setState({disabled: v});
		},
		
		title: function(v) {
			this.setState({title: v});
		},
		
		visible: function(v) {
			this.setState({visible_: v});
		},
		
		hidden: function(v) {
			this.setState({hidden_: v});
		},
		
		tabindex: function(v) {
			this.setState({tabIndex: v});
		},
		
		focus: function(v) {
			self = this;
			this.focusRequested_ = v;
			this.registerRenderHelper('focus', v ? _focus: null);
			
			function _focus(domElement) {
				if (domElement && self.focusRequested_) {
					self.focusRequested_ = false;
					domElement.focus();
				}
			}
		},
		
		/*--------------- Rendering ---------------*/
		
		renderIcon: function(type, handler) {
			return handler ? React.createElement('span', {className: 'glyphicon glyphicon-' + type, key: this.subId(type), onClick: handler.bind(this, type)}) : null;
		}
	});
	
	/************************************************************
	 * Base class for all widgets that may contain child widgets.
	 ************************************************************/ 
	cwf.widget.BaseContainerComponent = cwf.widget.BaseUIComponent.extend({

		abstract_: true,
				
		/*--------------- Lifecycle ---------------*/
		
		beforeCreate: function(props, state) {
			this._super(props, state);
			state.children_ = [];
		},		
	
		/*--------------- State ---------------*/
		
		getChildren: function() {
			return this.state.children_;
		},
		
		addChild: function(child, index) {
			if (cwf.ensureValue(index, -1) < 0) {
				index = this.state.children_.length;
			}
			
			return this._moveChild(child, index);
		},
		
		removeChild: function(child) {
			return this._moveChild(child, -1);
		},
		
		destroyChild: function(child) {
			return this._moveChild(child, -99);
		},
		
		_moveChild: function(child, newIndex) {
			child = child._wrapper || child;
			var children = cwf.insertIntoArray(this.state.children_, child, newIndex);
			
			if (children) {
				if (newIndex === -99) {
					this.getWidget(child).destroy();
				}
				
				this.setState({children_: children});
				return child;
			}
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('div', this.getMainProps(), this.getChildren());
		}
	});
	
	/************************************************************
	 * Base class for input components.
	 ************************************************************/ 
	cwf.widget.BaseInputComponent = cwf.widget.BaseUIComponent.extend({

		abstract_: true,

		/*--------------- State ---------------*/
		
		value: function(v) {
			this.setState({value: v});
		},
		
		placeholder: function(v) {
			this.setState({placeholder: v});
		},
		
		maxlength: function(v) {
			this.setState({maxlength: v});
		}
	});

	/************************************************************
	 * Base class for all widgets with a label.
	 ************************************************************/ 
	cwf.widget.BaseLabeledComponent = cwf.widget.BaseUIComponent.extend({

		abstract_: true,

		/*--------------- State ---------------*/
		
		label: function(v) {
			this.setState({label_: v});
		}
	});
	
	/************************************************************
	 * Widget container for text content.
	 ************************************************************/ 
	cwf.widget.Content = cwf.widget.BaseUIComponent.extend({
		
		/*--------------- State ---------------*/
		
		content: function(v) {
			this.setState({content_: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return this.state.content_ ? React.createElement('span', this.getMainProps(), this.state.content_) : null;
		}
	});
	
	/************************************************************
	 * The main page.
	 ************************************************************/ 
	cwf.widget.Page = cwf.widget.BaseContainerComponent.extend({
		
		/*--------------- State ---------------*/		
		
		title: function(v) {
			this.setState({title_: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			$('title').text(this.state.title_);
			return this._super();
		}
	});
	
	/************************************************************
	 * Serves as parent for all modal windows.
	 ************************************************************/ 
	cwf.widget.Modal_ = cwf.widget.BaseContainerComponent.extend({
		
		/*--------------- Lifecycle ---------------*/
		
		beforeCreate: function(props, state) {
			this._super(props, state);
			props.className = 'cwf_modal';
			props.id = 'cwf_modal';
			state.style = {};
		},
		
		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			var props = this._super(),
				count = this.getChildren().length;
			
			if (count) {
				props.style = cwf.combine(props.style, {zindex: cwf.widget._zmodal + count});
			} else {
				props.className += ' hidden';
			}
			
			return props;
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('div', this.getMainProps(), this.getChildren());
		}
	});
	
	/************************************************************
	 * A div widget.
	 ************************************************************/ 
	cwf.widget.Div = cwf.widget.BaseContainerComponent.extend({
	});
	
	
	/************************************************************
	 * A toolbar widget.
	 ************************************************************/ 
	cwf.widget.Toolbar = cwf.widget.BaseContainerComponent.extend({
	});
	
	/************************************************************
	 * A span widget.
	 ************************************************************/ 
	cwf.widget.Span = cwf.widget.BaseContainerComponent.extend({
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('span', this.getMainProps(), this.getChildren());
		}
	});
	
	/************************************************************
	 * A window widget.
	 ************************************************************/ 
	cwf.widget.Window = cwf.widget.BaseContainerComponent.extend({
		
		/*--------------- Lifecycle ---------------*/
		
		afterCreate: function() {
			this._super();
			this.forwardToServer('close');
		},		
	
		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			var props = this._super();
			
			if (this.state.mode_) {
				props.className += ' cwf_window-' + this.state.mode_.toLowerCase();
			}
			
			if (this.state.size_) {
				props.className += ' cwf_window-' + this.state.size_;
			}
			
			return props;
		},
		
		/*--------------- State ---------------*/
		
		title: function(v) {
			this.setState({title_: v});
		},
		
		closable: function(v) {
			this.setState({closable_: v});
		},
		
		sizable: function(v) {
			this.setState({sizable_: v});
		},
		
		minimizable: function(v) {
			this.setState({minimizable_: v});
		},
		
		maximizable: function(v) {
			this.setState({maximizable_: v});
		},
		
		mode: function(v) {
			this.setState({mode_: v});
		},
		
		size: function(v) {
			this.setState({size_: v});
		},
		
		/*--------------- Events ---------------*/
		
		closeHandler: function() {
			this.trigger('close');
		},
		
		maximizeHandler: function() {
			this.size(this.state.size_ === 'min' ? null : 'max');
		},
		
		minimizeHandler: function() {
			this.size(this.state.size_ === 'max' ? null : 'min');
		},
		
		mouseDownHandler: function(event) {
			if (event.button === 0) {
				this.coords = {x: event.pageX, y: event.pageY};
				this.position = ReactDOM.findDOMNode(this).getBoundingClientRect();
			}
		},
		
		mouseUpHandler: function(event) {
			this.coords = null;
			this.position = null;
		},
		
		mouseMoveHandler: function(event) {
			if (this.coords) {
				var dx = event.pageX - this.coords.x,
					dy = event.pageY - this.coords.y,
					pos = {left: this.position.left + dx, top: this.position.top + dy};
				
				this.setState({style: cwf.combine(this.state.style, pos)});
			}
		},
		
		/*--------------- Rendering ---------------*/
		
		renderIcons: function() {
			var icons = [];
			
			if ((this.state.minimizable_ && this.state.size_ !== 'min') || this.state.size_ === 'max') {
				icons.push(this.renderIcon('resize-small', this.minimizeHandler));
			}
			
			if ((this.state.maximizable_ && this.state.size_ !== 'max') || this.state.size === 'min') {
				icons.push(this.renderIcon('resize-full', this.maximizeHandler));
			}
			
			if (this.state.closable_) {
				icons.push(this.renderIcon('remove', this.closeHandler));
			}
			
			return icons;
		},
		
		render: function() {
			var headingProps = {
				key: this.subId('heading'), 
				className: 'panel-heading'
			}
			
			if (this.state.mode_ !== 'INLINE') {
				headingProps.onMouseDown = this.mouseDownHandler;
				headingProps.onMouseUp = this.mouseUpHandler;
				headingProps.onMouseMove = this.mouseMoveHandler;
			}
			
			var buttonHandler = this.state.mode_ === 'INLINE' ? null : this.mouseButtonHandler,
				titleElement = React.createElement('span', {key: this.subId('title')}, this.state.title_),
				iconElement = React.createElement('span', {key: this.subId('icons'), className: 'cwf_titled-icons'}, this.renderIcons()),
				title2Element = React.createElement('div', {className: 'panel-title'}, [titleElement, iconElement]),
				headingElement = React.createElement('div', headingProps, title2Element),
				bodyElement = React.createElement('div', {key: this.subId('body'), className: 'panel-body'}, this.getChildren()),
				panelElement = React.createElement('div', this.getMainProps(), [headingElement, bodyElement]);
			
			return panelElement;
		}
	});
	
	
	/************************************************************
	 * An alert widget
	 ************************************************************/
	cwf.widget.Alert_ = cwf.widget.Window.extend({
				
		/*--------------- Lifecycle ---------------*/
		
		beforeCreate: function(props, state) {
			this._super(props, state);
			state.style = {};
			state.style.maxHeight = '50%';
			state.style.maxWidth = '50%';
			state.closable_ = true;
			state.mode_ = 'modal';
			state.className = 'cwf_window panel panel-' + (state.type || 'primary')
		},
		
		/*--------------- State ---------------*/
		
		message: function() {
			setState({message_: message});
		},
		
		getChildren: function() {
			return this.state.message_;
		},
		
		/*--------------- Events ---------------*/
		
		closeHandler: function() {
			cwf.resolveId('cwf_modal').destroyChild(this);
		}
		
	});
	
	/************************************************************
	 * A tab box widget
	 ************************************************************/
	cwf.widget.Tabview = cwf.widget.BaseContainerComponent.extend({

		/*--------------- Rendering ---------------*/
		
		getChildren: function(type) {
			return _.map(this._super(), function(child) {
				return child.getMember(type);
			});
		},
		
		render: function() {
			var tabs = React.createElement('ul', {className: 'nav nav-tabs', key: this.subId('tabs')}, this.getChildren('header')),
				panels = React.createElement('div', {key: this.subId('panels')}, this.getChildren('panel'));
			
			return React.createElement('div', this.getMainProps(), [tabs, panels]);
		}
	});
	
	/************************************************************
	 * A tab widget
	 ************************************************************/
	cwf.widget.Tab = cwf.widget.ComponentCollection.extend({

		/*--------------- Lifecycle ---------------*/
		
		getMemberClasses: function() {
			return {
				header: cwf.widget.Tabheader_,
				panel: cwf.widget.Tabpanel_
			}
		},
		
		/*--------------- State ---------------*/
		
		label: function(v) {
			this.setState({label_: v})
		},
		
		active: function(v) {
			this.setState({active_: v});
		},
		
		closable: function(v) {
			this.setState({closable_: v});
		}
	});
	
	/************************************************************
	 * A tab header sub-widget
	 ************************************************************/
	cwf.widget.Tabheader_ = cwf.widget.BaseUIComponent.extend({

		/*--------------- Lifecycle ---------------*/
		
		beforeCreate: function(props, state) {
			this._super(props, state);
			props.role = 'presentation';
		},
		
		afterCreate: function() {
			this._super();
			this.forwardToServer(['close', 'click']);
		},
		
		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			var props = this._super();
			
			if (this.state.active_) {
				props.className += ' active';
			}
			
			return props;
		},
		
		/*--------------- Events ---------------*/
		
		closeHandler: function() {
			this.trigger('close');
		},
		
		/*--------------- Rendering ---------------*/
		
		renderIcons: function() {
			return this.state.closable_ ? this.renderIcon('remove', this.closeHandler) : null;
		},
		
		render: function() {
			var label = React.createElement('a', {href: 'javascript:'}, [this.state.label_, this.renderIcons()]);
			return React.createElement('li', this.getMainProps(), label);
		}
	});
	
	/************************************************************
	 * A tab panel sub-widget
	 ************************************************************/
	cwf.widget.Tabpanel_ = cwf.widget.BaseContainerComponent.extend({
		
		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			var props = this._super();
			
			if (!this.state.active_) {
				props.className += ' hidden';
			}
			
			return props;
		}
		
	});
	
	/************************************************************
	 * A raw HTML widget
	 ************************************************************/
	cwf.widget.Html = cwf.widget.BaseReactComponent.extend({
		
		/*--------------- Properties ---------------*/
		
		getMainProps: function() {
			var props = this._super();
			
			if (this.state.content_) {
				props.dangerouslySetInnerHTML = {__html: this.state.content_};
			}
			
			return props;
		},
		
		/*--------------- State ---------------*/
		
		content: function(v) {
			this.setState({content_: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('span', this.getMainProps());
		}
	});
	
	/************************************************************
	 * A label widget
	 ************************************************************/ 
	cwf.widget.Label = cwf.widget.BaseLabeledComponent.extend({

		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('label', this.getMainProps(), this.state.label_);
		}
	});
	
	/************************************************************
	 * A progress bar widget
	 ************************************************************/ 
	cwf.widget.Progressbar = cwf.widget.BaseLabeledComponent.extend({

		/*--------------- State ---------------*/
		
		getInitialState: function() {
			return this.mergeState(this._super(), {value_: 0, max_: 100});
		},		
	
		value: function(v) {
			this.setState({value_: v});
		},
		
		max: function(v) {
			this.setState({max_: v});
		},
		
		pct: function() {
			var value = this.state.value_;
			var max = this.state.max_;
			var pct = max <= 0 ? 0 : value / max * 100;
			return (pct > 100 ? 100 : pct) + '%';
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			var div1 = React.createElement('div', {key: this.subId('1')}, this.state.label_),
				div2 = React.createElement('div', {key: this.subId('2'), style: {width: this.pct()}});
			
			return React.createElement('div', this.getMainProps(), [div1, div2]);
		}
	});
	
	/************************************************************
	 * An image widget
	 ************************************************************/ 
	cwf.widget.Image = cwf.widget.BaseUIComponent.extend({

		/*--------------- State ---------------*/
		
		src: function(v) {
			this.setState({src: v});
		},
		
		alt: function(v) {
			this.setState({alt: v});
		},		
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('img', this.getMainProps());
		}
	});
	
	/************************************************************
	 * A button widget
	 ************************************************************/ 
	cwf.widget.Button = cwf.widget.BaseLabeledComponent.extend({

		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('button', this.getMainProps(), this.state.label_);
		}
	});
	
	/************************************************************
	 * A hyperlink widget
	 ************************************************************/ 
	cwf.widget.Hyperlink = cwf.widget.BaseLabeledComponent.extend({

		/*--------------- State ---------------*/
		
		getInitialState: function() {
			return this.mergeState(this._super(), {href: 'javascript:'});
		},		
			
		href: function(v) {
			this.setState({href: v || 'javascript:'});
		},
		
		target: function(v) {
			this.setState({target: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('a', this.getMainProps(), this.state.label_);
		}
	});
	
	/************************************************************
	 * An iframe
	 ************************************************************/ 
	cwf.widget.Iframe = cwf.widget.BaseUIComponent.extend({

		/*--------------- State ---------------*/
		
		src: function(v) {
			this.setState({src: v});
		},
		
		sandbox: function(v) {
			this.setState({sandbox: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('iframe', this.getMainProps());
		}
	});
	
	/************************************************************
	 * A text box widget
	 ************************************************************/ 
	cwf.widget.Textbox = cwf.widget.BaseInputComponent.extend({
		

		/*--------------- Lifecycle ---------------*/
		
		beforeCreate: function(props, state) {
			this._super(props, state);
			props.type = 'text';
		},	
		
		afterCreate: function() {
			this.registerEventHandler('change', this.handleChange);
			this.registerEventHandler('blur', this.handleBlur);
		},
	
		/*--------------- State ---------------*/
		
		synchronized: function(v) {
			this.setState({synchronized_: v});
		},
		
		multiline: function(v) {
			this.setState({multiline_: v})
		},
		
		/*--------------- Events ---------------*/
		
		handleChange: function(event) {
		    this.value(event.target.value);
		    
		    if (this.state.synchronized_) {
		    	cwf.event.sendToServer(event);
		    } else {
		    	event.persist();
		    	this.deferredEvent = event;
		    }
		},
		
		handleBlur: function(event) {
			if (this.deferredEvent) {
		    	cwf.event.sendToServer(this.deferredEvent);
				this.deferredEvent = null;
			}
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement(this.state.multiline_ ? 'textarea' : 'input', this.getMainProps());
		}
	});
	
	/************************************************************
	 * A list box widget
	 ************************************************************/ 
	cwf.widget.Listbox = cwf.widget.BaseContainerComponent.extend({

		/*--------------- Lifecycle ---------------*/
		
		afterCreate: function() {
			this._super();
			this.registerEventHandler('change', this.handleChange);
		},
		
		/*--------------- Events ---------------*/
		
		handleChange: function(event) {
			var self = this;
			
			_.forEach(this.getChildren(), function(child) {
				self.getWidget(child).syncSelected();
			});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('select', this.getMainProps(), this.getChildren());
		}
	});
	
	/************************************************************
	 * A list box item widget
	 ************************************************************/ 
	cwf.widget.Listitem = cwf.widget.BaseLabeledComponent.extend({
		
		/*--------------- Lifecycle ---------------*/
		
		beforeCreate: function(props, state) {
			this._super(props, state);
			props.role = 'presentation';
			props.value = props.id;
		},
		
		afterCreate: function() {
			this._super();
			this.forwardToServer('select');
		},
		
		/*--------------- State ---------------*/
		
		selected: function(v) {
			this.setState({selected: v});
		},
		
		syncSelected: function() {
			var selected = ReactDOM.findDOMNode(this).selected;
			
			if (selected != this.state.selected) {
				this.selected(selected);
				this.trigger('select', {selected: selected});
			}
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('option', this.getMainProps(), this.state.label_);
		}
	});
	
	/************************************************************
	 * A style tag for an embedded style.
	 ************************************************************/ 
	cwf.widget.Style = cwf.widget.BaseReactComponent.extend({
		
	/*--------------- State ---------------*/
		
		content: function(v) {
			this.setState({content_: v});
		},
		
	/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('style', this.getMainProps(), this.state.content_);
		}
	});
	
	/************************************************************
	 * A link tag for an external style sheet.
	 ************************************************************/
	cwf.widget.Stylesheet = cwf.widget.BaseReactComponent.extend({
		
		/*--------------- State ---------------*/
		
		href: function(v) {
			this.setState({href: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			$('#' + this.props.id).remove();
			
			if (this.state.href) {
				$('<link type="text/css" rel="stylesheet">')
					.attr('id', this.props.id)
					.attr('href', this.state.href)
					.appendTo('head');
			}
			
			return null;
		}
	});
	
	/************************************************************
	 * A script tag.
	 ************************************************************/ 
	cwf.widget.Script = cwf.widget.BaseReactComponent.extend({
		
		/*--------------- State ---------------*/
		
		type: function(v) {
			this.setState({type: v});
		},
		
		src: function(v) {
			this.setState({src: v});
		},
		
		content: function(v) {
			this.setState({content_: v});
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			return React.createElement('script', this.getMainProps(), this.state.content_);
		}
	});
	
	/************************************************************
	 * A timer.
	 ************************************************************/ 
	cwf.widget.Timer = cwf.widget.BaseReactComponent.extend({

		/*--------------- Lifecycle ---------------*/
		
		beforeDestroy: function() {
			this.stop();
			this._super();
		},
		
		/*--------------- State ---------------*/
		
		getInitialState: function() {
			return this.mergeState(this._super(), {repeat_: -1});
		},
		
		interval: function(v) {
			this.setState({interval_: v});
		},
		
		repeat: function(v) {
			this.setState({repeat_: v})
		},
		
		running: function(v) {
			this.setState({running_: v});
		},
		
		/*--------------- Timer Control ---------------*/
		
		start: function() {
			var self = this,
				repeat = this.state.repeat_;
			
			if (!this.timer && this.state.interval_ > 0) {
				this.timer = setInterval(_trigger, this.state.interval_);
				return true;
			}
			
			function _trigger() {
				if (!cwf.ws.isConnected()) {
					self.stop();
					return;
				}
				
				if (repeat-- === 0) {
					self.stop();
					self.running(false);
					self.stateChanged('running', false);
				}
				
				self.trigger('timer');
			}
		},
		
		stop: function() {
			if (this.timer) {
				clearInterval(this.timer);
				this.timer = null;
				return true;
			}
		},
		
		/*--------------- Rendering ---------------*/
		
		render: function() {
			this.stop();
			
			if (this.state.running_) {
				this.start();
			}
			
			return null;
		}
		
	});
	
	return cwf.widget;
});
