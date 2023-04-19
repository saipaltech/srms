import { Component } from '@angular/core';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-new-header',
  templateUrl: './new-header.component.html',
  styleUrls: ['./new-header.component.scss']
})
export class NewHeaderComponent {
  
  branch = ""
  bank = ""
  username=""

  constructor(private auth: AuthService) {
    const details = auth.getUserDetails();
    if (details) {
      this.bank = details.bank;
      this.branch = details.branch;
      this.username = details.username;
    }
  }

  
  
  sideBar(){

    var li = document.getElementById("sidebar");
  
  
  
    if (li!.classList.contains('sidebarDisplay')){
      li!.classList.remove('sidebarDisplay');
      li!.classList.add('sidebarHide')
    }
  
    else if (li!.classList.contains('sidebarHide')){
  
      li!.classList.remove('sidebarHide');
      li!.classList.add('sidebarDisplay');
    }
  

}
}
