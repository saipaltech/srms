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
        if(this.navcontent){
          this.navcontent.forEach((mnu:any) => {
            if(mnu.link=='report'){
              mnu.childs = [
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
                ,{
                  name: "Summary Report",
                  link: '/report',
                  queryparameter:{ type: 'sr' },
                  icon: "bi bi-circle",
                },
                {
                  name: "Detail Report",
                  link: '/report',
                  queryparameter:{ type: 'bsr' },
                  icon: "bi bi-circle",
                }
              ]
            }

            if(mnu.link=='revenue-report'){
              mnu.childs = [
                {
                  name: "Default Branch revenue account collection detail report",
                  link: '/revenue-report',
                  queryparameter:{ type: 'dbracdr' },
                  icon: "bi bi-circle",
                }, {
                  name: "Default Branch revenue account collection report",
                  link: '/revenue-report',
                  queryparameter:{ type: 'dbracr' },
                  icon: "bi bi-circle",
                },{
                  name: "Off branch Collection report",
                  link: '/revenue-report',
                  queryparameter:{ type: 'obcr' },
                  icon: "bi bi-circle",
                }
                ,{
                  name: "Off branch Collection report Summary",
                  link: '/revenue-report',
                  queryparameter:{ type: 'obcrs' },
                  icon: "bi bi-circle",
                }
                ,{
                  name: "Day close report",
                  link: '/revenue-report',
                  queryparameter:{ type: 'dcr' },
                  icon: "bi bi-circle",
                },{
                  name: "Outside branch Collection for own branch",
                  link: '/revenue-report',
                  queryparameter:{ type: 'obcfob' },
                  icon: "bi bi-circle",
                },{
                  name: "Outside branch Collection for own branch Summary",
                  link: '/revenue-report',
                  queryparameter:{ type: 'obcfobs' },
                  icon: "bi bi-circle",
                },{
                  name: "Local Level Revenue Collection Report",
                  link: '/revenue-report',
                  queryparameter:{ type: 'llrcr' },
                  icon: "bi bi-circle",
                }
              ]
            }
          });
        }
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
