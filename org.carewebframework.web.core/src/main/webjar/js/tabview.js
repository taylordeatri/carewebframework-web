'use strict';

define('cwf-tabview', ['cwf-core', 'cwf-widget', 'cwf-tabview-css'], function(cwf) { 
	
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
				+   '<ul id="${id}-tabs" class="cwf_tabview-tabs"/>'
				+   '<div id ="${id}-panes" class="cwf_tabview-panes"/>'
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
		
		/*------------------------------ Events ------------------------------*/
		
		handleSelect: function(event) {
			this.trigger('change', {value: true});
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.forwardToServer('change close');
		},
				
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.sub$('tab').on('click', this.handleSelect.bind(this));
		},
				
		render$: function() {
			var dom = 
				  '<li role="presentation">'
				+   '<a id="${id}-tab">'
				+ this.getDOMTemplate(':image', 'badge', 'label', ':closable')
				+   '</a>'
				+ '</li>',
				self = this;
				
			if (!this._ancillaries.pane$) {
				var pane = '<div id="${id}-pane" class="cwf_tab-pane hidden"/>';
				this._ancillaries.pane$ = $(this.resolveEL(pane));
				this._ancillaries.pane$.data('attach', _attachPane);
				_attachPane();
			}
			
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