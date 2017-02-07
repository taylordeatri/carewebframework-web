'use strict';

define('cwf-paneview', ['cwf-core', 'cwf-widget', 'css!cwf-paneview-css.css'], function(cwf) { 
	
	/******************************************************************************************************************
	 * Pane view widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Paneview = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		onAddChild: function(child) {
			child._updateSplitter();
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({orientation: 'HORIZONTAL'});
		},		
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<div/>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		orientation: function(v, old) {
			old ? this.toggleClass('cwf_paneview-' + old.toLowerCase(), false) : null;
			this.toggleClass('cwf_paneview-' + v.toLowerCase(), true);
			this.forEachChild(function(child) {
				child._updateSplitter();
			});
		}
		
	});
	
	/******************************************************************************************************************
	 * Pane widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Pane = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({splittable: false});
		},
				
		/*------------------------------ Other ------------------------------*/
		
		_isHorizontal: function() {
			return !this._parent || this._parent.getState('orientation') === 'HORIZONTAL';
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = '<div>'
					+    '<span id="${id}-title"/>'
					+ '</div>';
			return $(this.resolveEL(dom));
		},
		
		_updateSplitter: function() {
			var spl$ = this.widget$,
				active = !!spl$.resizable('instance'),
				splittable = this.getState('splittable');
			
			if (active === !splittable) {
				if (active) {
					spl$.resizable('destroy');
				} else {
					spl$.resizable({
						containment: 'parent',
						handles: this._isHorizontal() ? 'e' : 's'
					});
				}
			}
		},
		
		/*------------------------------ State ------------------------------*/
		
		splittable: function(v) {
			this._updateSplitter();
		},
		
		title: function(v) {
			this.sub$('title').text(v);
		}
		
	});
		
	return cwf.widget;
});