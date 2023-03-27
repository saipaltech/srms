import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class BankService {
  getBankFromSutra() {
    return this.api.get(this.url+'/banks-from-sutra');
  }
  url="bank";
  create(data: any) {
    // console.log(data);
    return this.api.post(this.url, data);

  }
  update(id: any, data: any) {
    return this.api.put(this.url + '/' + id, data);
    // return this.api.update(this.path,id,data);
  }

  constructor(private api: ApiService) {}

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
    return this.api.get(this.url + urlPart);

}
getEdit(id: string) {
  return this.api.get(this.url + '/' + id);

}
remove(id: string) {
  return this.api.delete(this.url + '/' + id);

}
getlist() {
  return this.api.get(this.url+'/get-list');
}
}
