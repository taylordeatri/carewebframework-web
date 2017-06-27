"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var platform_browser_1 = require("@angular/platform-browser");
var platform_browser_dynamic_1 = require("@angular/platform-browser-dynamic");
var core_2 = require("@angular/core");
function AppContext(componentModule, selector) {
    var App = componentModule.AngularComponent;
    var zone;
    var componentRef;
    var moduleRef;
    var ngModule = {
        imports: [platform_browser_1.BrowserModule],
        declarations: [App],
        entryComponents: [App]
    };
    componentModule.ngModule ? Object.assign(ngModule, componentModule.ngModule) : null;
    var AppModule = (function () {
        function AppModule(resolver, ngZone) {
            this.resolver = resolver;
            this.ngZone = ngZone;
            zone = ngZone;
        }
        AppModule.prototype.ngDoBootstrap = function (appRef) {
            var factory = this.resolver.resolveComponentFactory(App);
            componentRef = appRef.bootstrap(factory, selector);
        };
        return AppModule;
    }());
    AppModule = __decorate([
        core_1.NgModule(ngModule),
        __metadata("design:paramtypes", [core_2.ComponentFactoryResolver,
            core_2.NgZone])
    ], AppModule);
    AppContext.prototype.isLoaded = function () {
        return !!moduleRef;
    };
    AppContext.prototype.bootstrap = function (compilerOptions) {
        var platform = platform_browser_dynamic_1.platformBrowserDynamic();
        return platform.bootstrapModule(AppModule, compilerOptions).then(function (ref) { return moduleRef = ref; });
    };
    AppContext.prototype.destroy = function () {
        moduleRef ? moduleRef.destroy() : null;
        moduleRef = null;
    };
    AppContext.prototype.invoke = function (functionName, args) {
        return zone.run(function () {
            var instance = componentRef.instance;
            instance[functionName].apply(instance, args);
        });
    };
}
exports.AppContext = AppContext;
