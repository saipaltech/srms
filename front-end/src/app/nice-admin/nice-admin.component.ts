import { DOCUMENT } from '@angular/common';
import { Component, Inject } from '@angular/core';

@Component({
  selector: 'app-nice-admin',
  templateUrl: './nice-admin.component.html',
  styleUrls: ['./nice-admin.component.scss']
})
export class NiceAdminComponent {
  constructor(@Inject(DOCUMENT) private document: Document) { }

  menuItems: any = [];
ngOnInit(): void {
    this.menuItems = [
      {
        title: "Dashboard",
        link: '/dashboard',
        icon: "bi bi-grid",
      },
      {
        title: "Profile",
        link: '/user-profile',
        icon: "bi bi-person",
      },
      {
        title: "Blank Page",
        link: 'blank-page',
        icon: "bi bi-file-earmark",
      },
      {
        title: "Login",
        link: '/login',
        icon: "bi bi-box-arrow-in-right",
      },
      {
        title:"Submenu Demo",
        link:"",
        icon:"bi bi-subtract",
        childs:[
          {
            title: "Dashboard",
            link: '/dashboard',
            icon: "bi bi-circle",
          },
          {
            title: "Blank Page",
            link: 'blank-page',
            icon: "bi bi-circle",
          },
          {
            title: "Login",
            link: '/login',
            icon: "bi bi-circle",
          },
        ]
      }
    ];
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
}
