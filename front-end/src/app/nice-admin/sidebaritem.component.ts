import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-sidebaritem',
  template: `<li class="nav-item" *ngFor="let mit of menuItems">
    <app-sidebaritem-inner *ngIf="hasSubMenu(mit)" [menuItem]="mit"></app-sidebaritem-inner>
    <a (click)="activeButton($event)" [ngClass]="!isChildLink?'nav-link collapsed':''" [routerLink]="mit.link" [queryParams]="mit.queryparameter?.type ? {type: mit.queryparameter.type} : null" *ngIf="!hasSubMenu(mit)">
          <i class="{{mit.icon}} ico stopClick"></i>
          <span class="stopClick">{{mit.name}}</span>
        </a>
</li>`,
})
export class SidebaritemComponent {
  @Input("menuItems") menuItems:any;
  @Input("isChildLink") isChildLink?=false;
  active = false;
  constructor() { }

  activeButton(e: any) {
    var element: HTMLElement = e.target;
    var el: any = document.getElementsByClassName('nav-link')

    for (let i of el) {
      if (!i.classList.contains('collapsed')){
        i.classList.add('collapsed')
      }
    }
    // console.log(element.children[1])
    element.classList.remove('collapsed');
  }


  hasSubMenu(mit:any){
    if(mit.hasOwnProperty('childs')){
      if(mit.childs.length){
        return true;
      }
    }
    return false;
  }

}

@Component({
  selector: 'app-sidebaritem-inner',
  template: `
  <a class="nav-link collapsed" (click)="isCollapsed = !isCollapsed" href="javascript:void(0)">
      <i class="{{menuItem.icon}} ico"></i>
      <span>{{menuItem.name}}</span>
      <i class="bi bi-chevron-down ms-auto" [ngClass]="isCollapsed?'bi-chevron-down':'bi-chevron-up'"></i>
    </a>
    <ul class="nav-content" [collapse]="isCollapsed" [isAnimated]="true">
      <app-sidebaritem [menuItems]="menuItem.childs" [isChildLink]="true"></app-sidebaritem>
    </ul>`,
})
export class SidebaritemInnerComponent {
  @Input("menuItem") menuItem:any;
  @Input("isChildLink") isChildLink?=false;
  isCollapsed = true;
  active = false;
  constructor() { }
}