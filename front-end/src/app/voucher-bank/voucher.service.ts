import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class VoucherService {
  getRevenue() {
    return this.http.get(this.url+"/get-revenue-list");
  }
  getPlaikaDetails(llgCode: any) {
    return this.http.get(this.url+"/llg-details?llgcode="+llgCode);
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
getEdit(id: string) {
  return this.http.get(this.url + '/' + id);

}
remove(id: string) {
  return this.http.delete(this.url + '/' + id);

}
}
