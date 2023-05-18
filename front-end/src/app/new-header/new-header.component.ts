import { Component, Inject, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';
import { ChequeEntryService } from '../cheque-entry/cheque-entry.service';
import { TranslateService } from '@ngx-translate/core';
import { AppConfig } from '../app.config';

@Component({
  selector: 'app-new-header',
  templateUrl: './new-header.component.html',
  styleUrls: ['./new-header.component.scss']
})
export class NewHeaderComponent implements OnInit{
  
  branch = "";
  bank = "";
  username="";
  name="";

  constructor(private auth: AuthService, @Inject(DOCUMENT) private document: Document, private router: Router,private bvs:ChequeEntryService,public appConfig:AppConfig,  private translateService: TranslateService) {
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

  usertype:any
  ngOnInit(): void {
    this.bvs.getUsertype().subscribe({next:(dt)=>{
      this.usertype = dt.usertype;
      // this.voucherBankForm.patchValue({"lgid":this.dlgid});
    },error:err=>{

    }});
    // const lang = localStorage.getItem("lang") || "np-Np";
    const lang = "en-EN";
    this.changeLang(lang, null);
  }

  changeLang(lan: string, e:any) {
    localStorage.setItem("lang",lan);
    this.translateService.use(lan);
    var element = e.target;
    var ele: any= this.document.getElementsByClassName("langHighlight")

    for (const i of ele){
      i.classList.remove('langHighlight');
    }

    element.classList.add("langHighlight");
  }
  
  logout() {
    if (confirm("Are you sure you want to logout?")) {
      this.auth.logout();
      this.router.navigate(['/login']);
    }
  }
  
  

}
