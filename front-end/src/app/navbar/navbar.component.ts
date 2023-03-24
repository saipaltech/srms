import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {

  buttonactive(e: any){
    var elem: HTMLElement = e.target;
    var all : any = document.getElementsByClassName('scrollto')


    for (let a of all){
      if (a.classList.contains('active')){
        a.classList.remove('active');
      }
    }
    
    elem.classList.add('active');
    
  }

}
