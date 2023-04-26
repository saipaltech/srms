import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class ReportService {

  url="taxpayer-voucher"; 

  constructor(private http: ApiService) { 
  }

  getBranches() {
    return this.http.get(this.url+"/get-branch-report");
  }

}
