import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-new-nav',
  templateUrl: './new-nav.component.html',
  styleUrls: ['./new-nav.component.scss']
})
export class NewNavComponent implements OnInit{

  name="";
  navcontent:any= [];
  constructor(private router:Router ,private authService:AuthService,private http:ApiService){}
  ngOnInit(): void {
    this.name = this.authService.getUserDetails()?.name;
    this.http.get("users/get-front-menu").subscribe({next:(dt)=>{
      this.navcontent = dt;
    },error:err=>{
      console.log(err);
    }});
  }

  
  
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
    if (confirm("Are you sure you want to logout?")){
    this.authService.logout();
    this.router.navigate(['/login']);
  }
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
