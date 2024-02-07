import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class ChequeEntryService {
  getPanDetails(panno:any){
    return this.http.get(this.url+"/pan-details?panno="+panno);
  }
  getRevenue(bankorgid:any) {
    return this.http.get(this.url+"/get-revenue-list?bankorgid="+bankorgid);
  }

  getUsertype(){
    return this.http.get(this.url+"/get-usertype");
  }

  getdayclose(data:any){
    return this.http.post(this.url1+"/getdayclose", data);
  }

  vouchercancel(data:any){
    return this.http.post(this.url3+"/vouchercancel", data);
  }
  submitdayclose(data:any){
    return this.http.post(this.url1+"/submitdayclose", data);
  }

  getBank() {
    return this.http.get(this.url+"/get-banks-list");
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
  url1="dayclose";
  url="taxpayer-voucher"; 
  url3="bank-voucher";
  create(data: any) {
    // console.log(data);
    return this.http.post(this.url, data);

  }
  update(id: any, data: any) {
    return this.http.put(this.url + '/' + id, data);
    // return this.api.update(this.path,id,data);
  }

  getLocalLevels(){
    return this.http.get(this.url+"/get-local-levels-all-cheque");
  }

  getEdit(id: string) {
    return this.http.get(this.url + '/' + id);

  }

  constructor(private http: ApiService) { 
  }

  getDetails(id: string) {
    return this.http.get(this.url + '/get-specific/' + id);  
  }

  getDetailsOwn(id: string) {
    return this.http.get(this.url + '/get-specific-own/' + id);  
  }

  submitToPalika(id:any){
    return this.http.get(this.url3 + '/submitToPalika?id=' + id);
  }

  deleteVoucher(id:any){
    return this.http.get(this.url3 + '/deleteVoucher?id=' + id);
  }

  getDetailsSutra(id:any){
    return this.http.get(this.url3 + '/search-payment-sutra?transactionid=' + id); 
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
    return this.http.get(this.url+'/cheque' + urlPart);

}

getListVoucherCancel(perPage: string | number, page: string | number, searchTerm?: string, sortKey?: string, sortDir?: boolean, approve?: string) {

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
  //  urlPart += '&approve='+ approve;
  return this.http.get(this.url+'/vouchercancel' + urlPart);

}
// getEdit(id: string) {
//   return this.http.get(this.url + '/' + id);

// }
remove(id: string) {
  return this.http.delete(this.url + '/' + id);

}

clearCheque(id:any){
  return this.http.get(this.url+'/chequeclear?id=' + id);
}
}
