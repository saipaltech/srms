import { Component } from '@angular/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {

  buttonactive(e: any){
    var elem: HTMLElement = e;
    var all = document.getElementsByClassName('scrollto')


    for (let i= 0; i<=all.length; i++){
      if (all[i].classList.contains('active')){
        all[i].classList.remove('active');
      }
    }
    elem.classList.add('active');
    
  }

}
