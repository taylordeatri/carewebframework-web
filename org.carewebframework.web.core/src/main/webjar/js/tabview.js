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
			return this._ancillaries.pane$;
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
				+ '  <a id="${id}-tab">'
				+ this.getDOMTemplate(':image', 'label', ':closable')
				+ '  </a>'
				+ '</li>',
				pane = '<div id="${id}-pane" class="cwf_tab-pane hidden"/>',
				self = this;
				
			this._ancillaries.pane$ = $(this.resolveEL(pane));
			_attachPane();
			this._ancillaries.pane$.data('attach', _attachPane);
			return $(this.resolveEL(dom));
			
			function _attachPane() {
				self._ancillaries.pane$.appendTo(self._parent.sub$('panes'));
			}
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