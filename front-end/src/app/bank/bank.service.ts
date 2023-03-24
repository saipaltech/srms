import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppConfig } from '../app.config';


@Injectable({
  providedIn: 'root'
})
export class BankService {
  baseurl = "";
  url="";
  create(data: any) {
    // console.log(data);
    return this.http.post(this.url, data);

  }
  update(id: any, data: any) {
    return this.http.put(this.url + '/' + id, data);
    // return this.api.update(this.path,id,data);
  }

  constructor(private http: HttpClient,appCnfig:AppConfig) { 
    this.baseurl = appCnfig.baseUrl;
    this.url = this.baseurl + 'post-setup';
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
remove(id: string) {
  return this.http.delete(this.url + '/' + id);

}
}
