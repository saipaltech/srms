import { Component } from '@angular/core';

@Component({
  selector: 'app-new-design',
  templateUrl: './new-design.component.html',
  styleUrls: ['./new-design.component.scss']
})
export class NewDesignComponent {
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
