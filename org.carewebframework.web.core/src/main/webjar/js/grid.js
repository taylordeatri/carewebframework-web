'use strict';

define('cwf-grid', ['cwf-core', 'cwf-widget', 'cwf-grid-css'], function(cwf) { 
	/******************************************************************************************************************
	 * Grid widget
	 ******************************************************************************************************************/
	
	cwf.widget.Grid = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			return $('<table><caption/></table>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		title: function(v) {
			this.widget$.children('caption').text(v);
		}
	
	});
	
	/******************************************************************************************************************
	 * Grid columns widget
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
	 * Grid column widget
	 ******************************************************************************************************************/ 
	
	cwf.widget.Column = cwf.widget.LabeledImageWidget.extend({		

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
			var dom = '<th>' 
				+ this.getDOMTemplate(':image', 'label', ':sortOrder') 
				+ '</th>';
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
	 * Grid rows widget
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
			            cancel: 'input,textarea,button,select,option,.glyphicon',
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
				
				if (w.updateState('selected', selected, true)) {
					w.trigger('change', {value: selected});
				}
			}
		}
	
	});
	
	/******************************************************************************************************************
	 * Grid row widget
	 ******************************************************************************************************************/
	
	cwf.widget.Row = cwf.widget.UIWidget.extend({
		
		/*------------------------------ Containment ------------------------------*/
		
		addChild: function(child, index) {
			if (child.wclass !== 'Rowcell') {
				child = cwf.widget.Connector.create('<td>', child);
			}
			
			this._super(child, index);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/
		
		init: function() {
			this._super();
			this.initState({selected: false});
			this.forwardToServer('change');
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

	/******************************************************************************************************************
	 * Grid row cell widget
	 ******************************************************************************************************************/
	
	cwf.widget.Rowcell = cwf.widget.LabeledWidget.extend({
		
		/*------------------------------ Rendering ------------------------------*/
		
		render$: function() {
			var dom = 
				'<td>'
			  + this.getDOMTemplate(':label')
			  + '</td>';
			
			return $(this.resolveEL(dom));
		},
		
		/*------------------------------ State ------------------------------*/
		
		colspan: function(v) {
			this.attr('colspan', v);
		},
		
		
		label: function(v, old) {
			if (!!old !== !!v) {
				this.rerender();
			}
			
			this._super(v, old);
		},
		
		rowspan: function(v) {
			this.attr('rowspan', v);
		}
		
	});

	return cwf.widget;
});