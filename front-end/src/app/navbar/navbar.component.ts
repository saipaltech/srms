import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  constructor(private router:Router ,private authService:AuthService){}

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

  logout(){
    this.authService.logout();
    this.router.navigate(['/login']);
  }

}
