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

  navcontent= [
    {
      name: 'SuperAdmin',
      icon: 'bi bi-person-circle',
      link:'user-profile'
    },
    {
      name: 'Bank',
      icon: 'bi bi-bank',
      link:'bank'
    },
    {
      name: 'Branch',
      icon: 'bi bi-diagram-3',
      link:'branch'
    },
    {
      name: 'Users',
      icon: 'bi bi-file-earmark-person-fill',
      link:'users'
    },
    {
      name: 'Voucher Entry',
      icon: 'bi bi-stickies',
      link:'voucher-bank'
    },
    {
      name: ' Voucher Entry II',
      icon: 'bi bi-stickies',
      link:'voucher-bank-off'
    },
    {
      name: ' Cheque Entry',
      icon: 'bi bi-stickies',
      link:'cheque-entry'
    },
    {
      name: 'Verify Voucher',
      icon: 'bi bi-file-earmark-medical',
      link:'verify-voucher'
    },
    {
      name: 'Report',
      icon: 'bi bi-newspaper',
      link:'report'
    },
    {
      name: 'Approve Voucher',
      icon: 'bi bi-file-text',
      link:'approve-voucher'
    },
 ]
  
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
