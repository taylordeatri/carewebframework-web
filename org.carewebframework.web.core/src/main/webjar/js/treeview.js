'use strict';

define('cwf-treeview', ['cwf-core', 'cwf-widget', 'cwf-treeview-css'], function(cwf) { 
	
	/******************************************************************************************************************
	 * Tree view widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Treeview = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({showRoot: false, showLines: true, showToggles: true});
		},		
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $(this.resolveEL('<div><ul id="${id}-inner"></div>'));
		},
		
		/*------------------------------ State ------------------------------*/
		
		showLines: function(v) {
			this.toggleClass('cwf_treeview-nolines', !v);
		},
		
		showRoot: function(v) {
			this.toggleClass('cwf_treeview-noroot', !v);
		},
		
		showToggles: function(v) {
			this.toggleClass('cwf_treeview-notoggles', !v);
		}
		
	});
	
	/******************************************************************************************************************
	 * Tree node widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Treenode = cwf.widget.LabeledImageWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		onAddChild: function() {
			if (this.getChildCount() === 1) {
				this._updateToggle();
			}
		},
		
		onRemoveChild: function() {
			if (!this.getChildCount()) {
				this._updateToggle();
			}
		},
		
		/*------------------------------ Events ------------------------------*/
		
		handleClick: function(event) {
			var collapsed = !this.getState('collapsed');
			this.updateState('collapsed', collapsed);
			this.trigger('toggle', {collapsed: collapsed});
			return false;
		},
		
		handleSelect: function(event) {
			if (this.updateState('selected', true, true)) {
				this.trigger('change', {value: true});
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({collapsed: false});
			this.forwardToServer('change toggle');
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this.widget$.find('>a').on('click', this.handleSelect.bind(this));
			this.sub$('ctl').on('click', this.handleClick.bind(this));
			this._updateToggle();
		},
		
		getDragHelper: function() {
			return cwf.clone(this.sub$('lbl'), -1);
		},
		
		render$: function() {
			var dom = 
				  '<li>'
				+ ' <span id="${id}-ctl" class="glyphicon"/>'
				+ ' <a>'
				+ this.getDOMTemplate(':image', 'badge', 'label')
				+ ' </a>'
				+ ' <ul id="${id}-inner"/>'
				+ '</li>';
			return $(this.resolveEL(dom));
		},
		
		_updateToggle: function() {
			this.sub$('ctl').toggleClass('cwf_treenode-nochildren', !this.getChildCount());
		},
		
		/*------------------------------ State ------------------------------*/
		
		collapsed: function(v) {
			this.sub$('ctl').toggleClass('glyphicon-expand', v)
				.toggleClass('glyphicon-collapse-down', !v);
			this.toggleClass('cwf_treenode-collapsed', v);
		},
		
		selected: function(v) {
			this.toggleClass('cwf_treenode-selected', v);
		}
		
	});
		
	return cwf.widget;
});