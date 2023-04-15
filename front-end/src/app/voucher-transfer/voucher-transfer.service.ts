import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppConfig } from '../app.config';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class VoucherTransferService {
  url="taxpayer-voucher";
  getTranactionData(srch_term: any) {
    return this.http.get(this.url+'/search-voucher?voucherno='+srch_term);
  }
  create(data: any) {
    // console.log(data);
    return this.http.post(this.url, data);

  }
  update(id: any, data: any) {
    return this.http.put(this.url + '/' + id, data);
    // return this.api.update(this.path,id,data);
  }

  constructor(private http: ApiService) { 
  }

  getList(perPage: string | number, page: string | number, searchTerm?: string, sortKey?: string, sortDir?: boolean) {

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
    return this.http.get(this.url + urlPart);

}
getEdit(id: string) {
  return this.http.get(this.url + '/' + id);

}

getBankAccounts(llgCode: any) {
  return this.http.get(this.url+"/get-bank-accounts?llgcode="+llgCode);
}

getRevenue(bankorgid:any) {
  return this.http.get(this.url+"/get-revenue-list?bankorgid="+bankorgid);
}
remove(id: string) {
  return this.http.delete(this.url + '/' + id);

}
}
