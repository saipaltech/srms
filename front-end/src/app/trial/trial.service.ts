import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class TrialService {
    constructor(private http: ApiService) { 
    }
  
    getReport(voucherno:any, palika: any){
        return this.http.get(this.url+ '/generate-report?voucherno='+voucherno+'&palika='+palika);
    }

    getRevenueDetails(voucherno:any, palika: any){
      return this.http.get(this.url+ '/getRevenueDetails?voucherno='+voucherno+'&palika='+palika);
    }

  url="taxpayer-voucher"; 
}
