'use strict';

define('cwf-codemirror', ['cwf-core', 'cwf-widget', 'codemirror', 'css!cwf-codemirror-css.css', 'css!codemirror.css',
	'codemirror/addon/display/placeholder',
	'codemirror/addon/edit/closebrackets',
	'codemirror/addon/edit/closetag',
	'codemirror/addon/edit/matchbrackets',
	'codemirror/addon/edit/matchtags',
	'codemirror/addon/fold/xml-fold',
	'codemirror/addon/fold/brace-fold'], function(cwf, Widget, CodeMirror) { 
	
	/**
	 * Wrapper for CodeMirror
	 */
	Widget.CodeMirror = Widget.UIWidget.extend({
	
		/*------------------------------ Containment ------------------------------*/

		_detach: function(destroy) {
			if (destroy) {
				this._cm = null;
			}
			
			this._super(destroy);
		},
		
		/*------------------------------ Lifecycle ------------------------------*/

		init: function() {
			this._super();
			this.forwardToServer('change');
			this._cm = null;
		},
		
		/*------------------------------ Other ------------------------------*/
	
		format: function() {
		    var cm = this._cm,
		    	from = cm.getCursor('from'),
		    	to = cm.getCursor('to');
		    
		    if (from.line == to.line && from.ch == to.ch) {
		    	from.line = 0;
		    	to.line = cm.lastLine();
		    }
		        
    		cm.operation(function() {
				for (var i = from.line; i <= to.line; i++) {
					cm.indentLine(i);
			    }
			});
    		
    		from.ch = 0;
    		cm.setSelection(from, from);
    		cm.focus();
		},
		
		load: function(path, callback) {
			return cwf.load('codemirror/' + path, callback);
		},
		
		clear: function() {
		    this._cm.setValue('');
		    this._cm.focus();
		},
		
		/*------------------------------ Rendering ------------------------------*/

		afterRender: function() {
			var cm = this._cm;
			
			setTimeout(function() {
			    cm.refresh();
			}, 1);

		},
		
		beforeRender: function() {
			var self = this;
			this._super();
			this._cm = CodeMirror(this.widget$[0], {
				autoCloseTags: true,
				matchTags: {bothTags: true},
				extraKeys: {
					'Alt-F': self.format.bind(self),
					'Alt-J': 'toMatchingTag'
				}
			});
			
			this._cm.on('changes', function() {
				var v = self._cm.getValue();
				
				if (self.setState('value', v)) {
					self.trigger('change', {value: v});
				}
			});
			
			this.widget$.find('.CodeMirror textarea').on('change', false);
			this._cm.setSize('100%', '100%');
		},
		
		render$: function() {
			return $('<span/>');
		},
		
		/*------------------------------ State ------------------------------*/
		
		focus: function(v) {
			v ? this._cm.focus() : null;    
		},
		
		lineNumbers: function(v) {
			this._cm.setOption('lineNumbers', v);
		},
		
		mode: function(v) {
			var self = this;
			v = v ? v.toLowerCase() : null;
			
			if (!v) {
				_mode();
			} else {
				this.load('mode/' + v + '/' + v, _mode);
			}
			
			function _mode() {
				self._cm.setOption('mode', v);
			}
		},
		
		placeholder: function(v) {
			this._cm.setOption('placeholder', v);
		},
		
		readonly: function(v) {
			this._cm.setOption('readOnly', v);
		},
		
		value: function(v) {
			this._cm.setValue(v || '');
		}
	});

	return cwf.widget;
});