import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { ApplicationRef, ComponentFactory, ComponentFactoryResolver } from '@angular/core';

export function AppContext(module: any, selector: string) {
  var App = module.AngularComponent;

  var module_decorations = {
    imports: [ BrowserModule ],
    declarations: [ App ],
    entryComponents: [ App ]
  }

  module.decorations ? Object.assign(module_decorations, module.decorations) : null;

  @NgModule(module_decorations)
  class AppModule {
      constructor(
          private resolver : ComponentFactoryResolver
      ) {}

      ngDoBootstrap(appRef : ApplicationRef) {
          const factory = this.resolver.resolveComponentFactory(App);
          factory.selector = selector;
          appRef.bootstrap(factory);
      }
  }

  this.bootstrap = function bootstrap() {  
    const platform = platformBrowserDynamic();
    platform.bootstrapModule(AppModule);  
  }
  
}