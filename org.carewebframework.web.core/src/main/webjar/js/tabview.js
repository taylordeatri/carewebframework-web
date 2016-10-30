'use strict';

define('cwf-tabview', ['cwf-core', 'cwf-widget', 'css!cwf-tabview-css.css'], function(cwf) { 
	
	/******************************************************************************************************************
	 * A tab box widget
	 ******************************************************************************************************************/
	
	cwf.widget.Tabview = cwf.widget.UIWidget.extend({
				
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('tabs');
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({tabPosition: 'TOP'});
		},		
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = 
				  '<div>'
				+ '  <ul id="${id}-tabs" class="cwf_tabview-tabs"/>'
				+ '  <div id ="${id}-panes" class="cwf_tabview-panes"/>'
				+ '</div>';
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		tabPosition: function(v, old) {
			v = 'cwf_tabview-' + (v ? v.toLowerCase() : 'top');
			old = old ? 'cwf_tabview-' + old : null;
			this.replaceClass(old, v);
		}
		
	});
	
	/******************************************************************************************************************
	 * A tab widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Tab = cwf.widget.LabeledImageWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('pane');
		},
		
		_attach: function(index) {
			this.sub$('pane').appendTo(this._parent.sub$('panes'));
			this._super(index);
		},	
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.forwardToServer('select close');
			this.forward(this.sub$('tab'), 'click', 'select');
		},
				
		render$: function() {
			var dom = 
				  '<li role="presentation">'
				+ '  <a id="${id}-tab" href="javascript:">'
				+ this.getDOMTemplate(':image', 'label', ':closable')
				+ '  </a>'
				+ '	 <div id="${id}-pane" class="hidden"/>'
				+ '</li>';
			
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/		
		
		closable: function(v) {
			this.rerender();
			this.toggleClass('cwf_tab-closable', v);
			
			if (v) {
				this.forward(this.sub$('cls'), 'click', 'close');
			}
		},
		
		selected: function(v) {
			this.toggleClass('cwf_tab-selected', v);
			this.sub$('pane').toggleClass('hidden', !v);
			this.widget$.children().blur();
		}
		
	});
	
	return cwf.widget;
});