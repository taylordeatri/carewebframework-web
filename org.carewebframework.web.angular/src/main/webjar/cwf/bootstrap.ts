import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { ApplicationRef, ComponentFactory, ComponentFactoryResolver, NgModuleRef, NgZone, ComponentRef } from '@angular/core';

export function AppContext(module: any, selector: string) {
  var App = module.AngularComponent;

  var zone : NgZone;
  
  var componentRef : ComponentRef<any>;
  
  var module_decorations = {
    imports: [ BrowserModule ],
    declarations: [ App ],
    entryComponents: [ App ]
  }

  module.decorations ? Object.assign(module_decorations, module.decorations) : null;

  @NgModule(module_decorations)
  class AppModule {
      constructor(
          private resolver : ComponentFactoryResolver,
          private ngZone: NgZone
      ) {zone = ngZone}

      ngDoBootstrap(appRef : ApplicationRef) {
          const factory = this.resolver.resolveComponentFactory(App);
          factory.selector = selector;
          componentRef = appRef.bootstrap(factory);
      }
  }

  AppContext.prototype.bootstrap = function(compilerOptions?) : Promise<NgModuleRef<AppModule>> {  
    const platform = platformBrowserDynamic();
    return platform.bootstrapModule(AppModule, compilerOptions);
  }
  
  AppContext.prototype.invoke = function(functionName: string, ...args) : any {
    return zone.run(() => {
      let instance = componentRef.instance;
      instance[functionName].apply(instance, args)
    })
  }
  
}