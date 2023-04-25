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
  name = "";
  navcontent: any = [];
  constructor(private router: Router, private authService: AuthService, private http: ApiService) { }
  ngOnInit(): void {
    this.name = this.authService.getUserDetails()?.name;
    this.http.get("users/get-front-menu").subscribe({
      next: (dt) => {
        this.navcontent = dt;
        this.navcontent.push({
          name: "Report",
          link: '/report',
          icon: "bi bi-newspaper ico stopClick",
          childs:[
            {
              name: "Cash Deposit",
              link: '/report',
              queryparameter:{ type: 'cad' },
              icon: "bi bi-circle",
            }, {
              name: "Cheque Deposit",
              link: '/report',
              queryparameter:{ type: 'chd' },
              icon: "bi bi-circle",
            },{
              name: "Verified Voucher",
              link: '/report',
              queryparameter:{ type: 'vv' },
              icon: "bi bi-circle",
            }
            ,{
              name: "Day Close",
              link: '/report',
              queryparameter:{ type: 'dc' },
              icon: "bi bi-circle",
            }
          ]
        });
      }, error: err => {
        console.log(err);
      }
    });
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

  menuItems: any = [];


  openDropdown(e: any) {
    var element: HTMLElement = e.target;
    element.nextElementSibling?.classList.toggle('show')
  }


  logout() {
    if (confirm("Are you sure you want to logout?")) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }
}
