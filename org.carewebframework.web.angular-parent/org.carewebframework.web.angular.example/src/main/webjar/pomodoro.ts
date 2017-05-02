import { Component } from '@angular/core';

// Pomodoro timer component
// Note: bootstrapping is handled automatically.
// Note: that we do not specify a selector as it will be assigned during bootstrapping.
@Component({
  template: `
    <div class="text-center">
      <img src="webjars/cwf-angular-example/assets/img/pomodoro.png" alt="Pomodoro">
      <h1> {{ minutes }}:{{ seconds | number: '2.0' }} </h1>
      <p>
        <button (click)="togglePause()"
          class="btn btn-danger">
          {{ buttonLabel }}
        </button>
      </p>
    </div>
`
})
class PomodoroComponent {
  minutes: number;
  seconds: number;
  isPaused: boolean;
  buttonLabel: string;

  constructor() {
    this.resetPomodoro();
    setInterval(() => this.tick(), 1000);
  }

  resetPomodoro(): void {
    this.isPaused = true;
    this.minutes = 24;
    this.seconds = 59;
    this.buttonLabel = 'Start';
  }

  private tick(): void {
    if (!this.isPaused) {
      this.buttonLabel = 'Pause';

      if (--this.seconds < 0) {
        this.seconds = 59;
        if (--this.minutes < 0) {
          this.resetPomodoro();
        }
      }
    }
  }

  togglePause(): void {
    this.isPaused = !this.isPaused;
    if (this.minutes < 24 || this.seconds < 59) {
      this.buttonLabel = this.isPaused ? 'Resume' : 'Pause';
    }
  }
}

// Must export component to be bootstrapped as AngularComponent

let AngularComponent = PomodoroComponent;
export { AngularComponent };
  
/* Any additional metadata to be passed to the application module during bootstrapping
 * may be exported as in the following example:
 * 
 *      let ngModule : NgModule = { providers: [ AService ] };
 *      export { ngModule };
 * 
 * This will be merged with the default metadata prior to bootstrapping.
*/