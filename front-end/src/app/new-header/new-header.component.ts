import { Component, Inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-new-header',
  templateUrl: './new-header.component.html',
  styleUrls: ['./new-header.component.scss']
})
export class NewHeaderComponent {
  
  branch = "";
  bank = "";
  username="";
  name="";

  constructor(private auth: AuthService, @Inject(DOCUMENT) private document: Document, private router: Router) {
    const details = auth.getUserDetails();
    if (details) {
      this.bank = details.bank;
      this.branch = details.branch;
      this.username = details.username;
      this.name = details.name;
    }
  }


  toggleSidebar() {
    const kl = "toggle-sidebar";
    // console.log('SideBar')
    if (this.document.body.classList.contains(kl)) {
      this.document.body.classList.remove(kl);
    } else {
      this.document.body.classList.add(kl);
    }
  }

  
  logout() {
    if (confirm("Are you sure you want to logout?")) {
      this.auth.logout();
      this.router.navigate(['/login']);
    }
  }
  
  

}
