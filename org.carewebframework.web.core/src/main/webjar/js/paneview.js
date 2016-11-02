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
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
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
			return $(this.resolveEL('<div><div id="${id}-inner"/></div>'));
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
						handles: this._isHorizontal() ? 'e' : 's'
					});
				}
			}
		},
		
		/*------------------------------ State ------------------------------*/
		
		splittable: function(v) {
			this._updateSplitter();
		}
		
	});
		
	return cwf.widget;
});