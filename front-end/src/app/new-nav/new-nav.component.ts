import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-new-nav',
  templateUrl: './new-nav.component.html',
  styleUrls: ['./new-nav.component.scss']
})
export class NewNavComponent {

  constructor(private router:Router ,private authService:AuthService){}
  
  buttonactive(e: any){
    var elem: HTMLElement = e.target;
    var all : any = document.getElementsByClassName('active')


    for (let a of all){
      if (a.classList.contains('active')){
        a.classList.remove('active');
        a.classList.add('text-white');

      }
    }
    
    elem.classList.remove('text-white');
    elem.classList.add('active');
    
  }

  logout(){
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  closeSideBar(){
    var sb = document.getElementsByClassName('sidebar')[0];
    var buttooon: any = document.getElementsByClassName('buttooon')[0];
    

    if (sb.classList.contains('hidebar')){
      sb.classList.remove('hidebar');
      sb.classList.add('showbar');
      buttooon.style.left='250px';
    }
    else{
      sb.classList.remove('showbar');
      sb.classList.add('hidebar');
      buttooon.style.left='0';
    }
    
  }
}
