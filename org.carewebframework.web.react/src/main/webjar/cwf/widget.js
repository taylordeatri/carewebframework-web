'use strict';

define('cwf-react', ['cwf-core', 'cwf-widget', 'react', 'react-dom'], 
	function(cwf, wgt, React, ReactDOM) { 

	return {
		
	ReactWidget: cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		destroy: function() {
			this._destroy();
			this._super();
		},
		
		init: function() {
			this._super();
			this._rxInvoke = [];
		},
		
		_destroy: function () {
			this.widget$ ? ReactDOM.unmountComponentAtNode(this.widget$[0]) : null;
			this._rxComponent = null;
		},
		
		/*------------------------------ Other ------------------------------*/
		
		isLoaded: function() {
			return !!this._rxComponent;
		},
		
		loaded: function(component) {
			this._rxComponent = component;
			
			while (this._rxInvoke.length) {
				var invk = this._rxInvoke.shift();
				this.rxInvoke(invk.functionName, invk.args);
			}
		},
		
		rxInvoke: function(functionName, args) {
			if (this.isLoaded()) {
				return this._rxComponent[functionName].apply(this._rxComponent, args);
			} else {
				this._rxInvoke.push({functionName: functionName, args: args});
			}
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			
			var src = this.getState('src'),
				self = this;
			
			if (src) {
				System.import(src).then(function(componentClass) {
					var element = React.createElement(componentClass);
					
					ReactDOM.render(element, self.widget$[0], function() {
						self.loaded(this);
					});
				});
			}
		},
		
		beforeRender: function() {
			this._destroy();
			this._super();
		},
		
		render$: function() {
			return $('<div />');
		},
		
		/*------------------------------ State ------------------------------*/
		
		src: function(v) {
			this.rerender();
		}
	
	})};
});