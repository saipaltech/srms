import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppConfig } from '../app.config';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class AllUsersService {
  baseurl = "";
  url="";
  constructor(private http: HttpClient,appCnfig:AppConfig,private api: ApiService) { 
    this.baseurl = appCnfig.baseUrl;
    this.url = this.baseurl + 'users';
  }
  url1="bank";
  getBanks(){
    return this.api.get(this.url1+'/get-bank-list');
  }

  getBranches(bankid:any){
    return this.api.get(this.url1+'/get-Branches?bid='+bankid);
  }

  getList(perPage: string | number, page: string | number, searchTerm?: string, sortKey?: string, sortDir?: boolean,bankid?:string,branchid?:string) {

    let urlPart = '?perPage=' + perPage + '&page=' + page;
    if (typeof searchTerm !== 'undefined' || searchTerm !== '') {
        urlPart += '&searchOption=all&searchTerm=' + searchTerm;
    }

    if (typeof bankid !== 'undefined' || bankid !== '') {
      urlPart += '&bankid=' + bankid;
  }

  if (typeof branchid !== 'undefined' || branchid !== '') {
    urlPart += '&branchid=' + branchid;
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
    return this.http.get(this.url+'/all' + urlPart);

}
}
