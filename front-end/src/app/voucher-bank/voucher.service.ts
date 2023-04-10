import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class VoucherService {
  getPanDetails(panno:any){
    return this.http.get(this.url+"/pan-details?panno="+panno);
  }
  getRevenue(bankorgid:any) {
    return this.http.get(this.url+"/get-revenue-list?bankorgid="+bankorgid);
  }
  getPlaikaDetails(llgCode: any) {
    return this.http.get(this.url+"/llg-details?llgcode="+llgCode);
  }
  getCostCentres(llgCode: any) {
    return this.http.get(this.url+"/get-cost-centres?llgcode="+llgCode);
  }
  getBankAccounts(llgCode: any) {
    return this.http.get(this.url+"/get-bank-accounts?llgcode="+llgCode);
  }
  url="taxpayer-voucher"; 
  create(data: any) {
    // console.log(data);
    return this.http.post(this.url, data);

  }
  update(id: any, data: any) {
    return this.http.put(this.url + '/' + id, data);
    // return this.api.update(this.path,id,data);
  }

  getLocalLevels(){
    return this.http.get(this.url+"/get-local-levels");
  }

  constructor(private http: ApiService) { 
  }

  approveVoucher(id: any){
    return this.http.post(this.url + '/approve/'+ id, {id:id})
  }

  getDetails(id: string) {
    return this.http.get(this.url + '/get-specific/' + id);  
  }

  getList(perPage: string | number, page: string | number, searchTerm?: string, sortKey?: string, sortDir?: boolean, approve?: string) {

    let urlPart = '?perPage=' + perPage + '&page=' + page;
    if (typeof searchTerm !== 'undefined' || searchTerm !== '') {
        urlPart += '&searchOption=all&searchTerm=' + searchTerm;
    }
    if (typeof sortKey !== 'undefined' || sortKey !== '') {
        urlPart += '&sortKey=' + sortKey;
    }
    if (typeof sortDir !== 'undefined' && sortKey !== '') {
        if (sortDir) {
            urlPart += '&sortDir=desc';
        } else {
            urlPart += '&sortDir=asc';
        }
     }
     urlPart += '&approve='+ approve;
    return this.http.get(this.url + urlPart);

}
// getEdit(id: string) {
//   return this.http.get(this.url + '/' + id);

// }
remove(id: string) {
  return this.http.delete(this.url + '/' + id);

}
}
