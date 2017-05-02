'use strict';

define('cwf-react-pomodoro', [ 'react', 'react-dom' ],
	function(React, ReactDOM) {

		var ImageClass = React.createClass({
			render : function() {
				return React.createElement('img', {
					src : 'webjars/cwf-react-example/assets/img/pomodoro.png',
					alt : 'Pomodoro'
				});
			}
		});

		var CounterClass = React.createClass({
			formatTime : function() {
				return format(this.props.minutes) + ':' + format(this.props.seconds);
				
				function format(value) {
					return (value + 100).toString().substring(1);
				}
			},

			render : function() {
				return React.createElement('h1', {}, this.formatTime());
			}
		});

		var ButtonClass = React.createClass({
			render : function() {
				return React.createElement('button', {
					className : 'btn btn-danger',
					onClick : this.props.onClick
				}, this.props.buttonLabel);
			}
		});

		// Return the class for the top level component.
		
		return React.createClass({
			getInitialState : function() {
				return {
					isPaused : true,
					minutes : 24,
					seconds : 59,
					buttonLabel : 'Start'
				}
			},

			componentDidMount : function() {
				var self = this;

				this.timer = setInterval(function() {
					self.tick();
				}, 1000);
			},

			componentWillUnmount : function() {
				clearInterval(this.timer);
				delete this.timer;
			},

			resetPomodoro : function() {
				this.setState(this.getInitialState());
			},

			tick : function() {
				if (!this.state.isPaused) {
					var newState = {};

					newState.buttonLabel = 'Pause';
					newState.seconds = this.state.seconds - 1;

					if (newState.seconds < 0) {
						newState.seconds = 59;
						newState.minutes = this.state.minutes - 1;

						if (newState.minutes < 0) {
							return this.resetPomodoro();
						}
					}

					this.setState(newState);
				}
			},

			togglePause : function() {
				var newState = {};

				newState.isPaused = !this.state.isPaused;

				if (this.state.minutes < 24 || this.state.seconds < 59) {
					newState.buttonLabel = newState.isPaused ? 'Resume' : 'Pause';
				}

				this.setState(newState);
			},

			render : function() {
				return React.createElement('div', {
					className : 'text-center'
				}, [
					React.createElement(ImageClass, {
						key: 'image'
					}),
					React.createElement(CounterClass, {
						key : 'counter',
						minutes : this.state.minutes,
						seconds : this.state.seconds
					}),
					React.createElement(ButtonClass, {
						key : 'button',
						buttonLabel : this.state.buttonLabel,
						onClick : this.togglePause
					})
				]);
			}
		});
	});