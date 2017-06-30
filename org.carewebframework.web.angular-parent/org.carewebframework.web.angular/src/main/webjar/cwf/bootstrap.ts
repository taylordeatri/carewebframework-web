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

  var appContext = this;
  
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
      ) {
          appContext.zone = ngZone;
      }

      ngDoBootstrap(appRef : ApplicationRef) {
          const factory = this.resolver.resolveComponentFactory(App);
          appContext.componentRef = appRef.bootstrap(factory, selector);
      }
  }

  AppContext.prototype.isLoaded = function() : boolean {
    return !!this.moduleRef;
  }
  
  AppContext.prototype.bootstrap = function(compilerOptions?) : Promise<NgModuleRef<AppModule>> {  
    const platform = platformBrowserDynamic();
    return platform.bootstrapModule(AppModule, compilerOptions).then(
      ref => this.moduleRef = ref);
  }
  
  AppContext.prototype.destroy = function() : void {
    this.moduleRef ? this.moduleRef.destroy() : null;
    this.moduleRef = null; 
  }
  
  AppContext.prototype.invoke = function(functionName: string, args: any[]) : any {
    return this.zone.run(() => {
      let instance = this.componentRef.instance;
      instance[functionName].apply(instance, args)
    })
  }
  
}