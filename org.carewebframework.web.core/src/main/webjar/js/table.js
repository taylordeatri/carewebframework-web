'use strict';

define('cwf-table', ['cwf-core', 'cwf-widget', 'css!cwf-table-css.css'], function(cwf) { 
	/******************************************************************************************************************
	 * Table widget
	 ******************************************************************************************************************/
	
	cwf.widget.Table = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<table>');
		}
		
	});
	
	/******************************************************************************************************************
	 * Table columns widget
	 ******************************************************************************************************************/
	
	cwf.widget.Columns = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return this.sub$('inner');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $(this.resolveEL('<thead><tr id="${id}-inner"/></thead>'));
		}
		
	});
	
	/******************************************************************************************************************
	 * Table column widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Column = cwf.widget.LabeledWidget.extend({		

		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.forwardToServer('sort');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		afterRender: function() {
			this._super();
			this.forward(this.sub$('dir'), 'click', 'sort');
		},
		
		render$: function() {
			var dom = '<th>' + this.getDOMTemplate('label', ':sortOrder') + '</th>';
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		label: function(v) {
			this.sub$('lbl').text(v);
		},
		
		sortOrder: function(v, old) {
			var self = this;
			
			if (!v !== !old) {
				this.rerender();
			}
			
			if (v) {
				this.sub$('dir')
					.toggleClass('glyphicon-chevron-up', v === 'ASCENDING')
					.toggleClass('glyphicon-chevron-down', v === 'DESCENDING')
					.toggleClass('glyphicon-sort', v === 'UNSORTED' || v === 'NATIVE');
			}
		}
	
	});
	
	/******************************************************************************************************************
	 * Table rows widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Rows = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Lifecycle ------------------------------*/
		init: function() {
			this._super();
			this.initState({selectable: 'NO'});
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<tbody>');
		},
	
		/*------------------------------ State ------------------------------*/
		
		selectable: function(v) {
			var self = this,
				active = !!this.widget$.selectable('instance');
			
			_selectable(v !== 'NO');
			
			function _selectable(selectable) {
				if (selectable && !active) {
					self.widget$.selectable({
						appendTo: '#cwf_root',
						filter: 'tr',
						selected: _select,
						unselected: _unselect
					})
				} else if (!selectable && active){
					self.widget$.selectable('destroy');
				}
			};
			
			function _select(event, ui) {
				_doSelect(ui.selected, true);
			}
			
			function _unselect(event, ui) {
				_doSelect(ui.unselected, false);
			}
			
			function _doSelect(target, selected) {
				var w = cwf.wgt(target);
				w.updateState('selected', selected, true);
				w.trigger('select', {selected: selected});
			}
		}
	
	});
	
	/******************************************************************************************************************
	 * Table row widget
	 ******************************************************************************************************************/
	
	cwf.widget.Row = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		anchor$: function() {
			return $('<td>').appendTo(this.widget$);
		},
		
		onRemoveChild: function(child, destroyed, anchor$) {
			this._super(child, destroyed, anchor$);
			
			if (anchor$) {
				anchor$.remove();
			}
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({selected: false});
			this.forwardToServer('select');
		},
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<tr>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		selected: function(v) {
			this.toggleClass('ui-selected', v);
		}
	});
	
	return cwf.widget;
});