import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { ApplicationRef, ComponentFactory, ComponentFactoryResolver, NgModuleRef, NgZone, ComponentRef } from '@angular/core';

export interface IComponentModule {
  AngularComponent: any;
  ngModule?: NgModule;
}

export function AppContext(componentModule: IComponentModule, selector: string) {
  var App = componentModule.AngularComponent;

  var zone : NgZone;
  
  var componentRef : ComponentRef<any>;
  
  var moduleRef : NgModuleRef<AppModule>;
  
  var ngModule : NgModule = {
    imports: [ BrowserModule ],
    declarations: [ App ],
    entryComponents: [ App ]
  }

  componentModule.ngModule ? Object.assign(ngModule, componentModule.ngModule) : null;

  @NgModule(ngModule)
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

  AppContext.prototype.isLoaded = function() : boolean {
    return !!moduleRef;
  }
  
  AppContext.prototype.bootstrap = function(compilerOptions?) : Promise<NgModuleRef<AppModule>> {  
    const platform = platformBrowserDynamic();
    return platform.bootstrapModule(AppModule, compilerOptions).then(
      ref => moduleRef = ref);
  }
  
  AppContext.prototype.destroy = function() : void {
    moduleRef ? moduleRef.destroy() : null;
    moduleRef = null; 
  }
  
  AppContext.prototype.invoke = function(functionName: string, args: any[]) : any {
    return zone.run(() => {
      let instance = componentRef.instance;
      instance[functionName].apply(instance, args)
    })
  }
  
}