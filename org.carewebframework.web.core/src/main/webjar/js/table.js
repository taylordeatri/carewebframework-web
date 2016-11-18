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

		/*------------------------------ Rendering ------------------------------*/
		
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
			!v !== !old ? this.rerender() : null;
			
			if (v) {
				this.sub$('dir')
					.toggleClass('glyphicon-chevron-up', v === 'ASCENDING')
					.toggleClass('glyphicon-chevron-down', v === 'DESCENDING')
					.toggleClass('glyphicon-sort', v === 'UNSORTED')
					.on('click', _sort);
			}
			
			function _sort() {
				self.trigger('sort');
			}
		}
	
	});
	
	/******************************************************************************************************************
	 * Table rows widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Rows = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<tbody>');
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
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<tr>');
		}
		
	});
	
	return cwf.widget;
});