import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../api.service';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-new-sidebar',
  templateUrl: './new-sidebar.component.html',
  styleUrls: ['./new-sidebar.component.scss']
})
export class NewSidebarComponent {
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

  openDropdown(e:any){
    var element: HTMLElement =  e.target;
    element.nextElementSibling?.classList.toggle('show')
  }

  logout(){
    if (confirm("Are you sure you want to logout?")){
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  }
}
