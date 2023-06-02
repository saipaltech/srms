import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class RevenueReportService {

  url="report"; 

  constructor(private http: ApiService) { 
  }

  getBranches() {
    return this.http.get(this.url+"/get-branches");
  }
  getFy(){
    return this.http.get(this.url+"/get-fys")
  }
  getllgs(){
    return this.http.get(this.url+"/get-llgs")
  }

  getAccountNumbers(llgCode: any){
    return this.http.get(this.url+"/get-account-numbers?llgcode="+llgCode)
  }

  getUserList(){
    return this.http.get(this.url+"/get-user")
  }

  getLocalLevels(){
    return this.http.get(this.url+"/get-local-levels");
  }

}
