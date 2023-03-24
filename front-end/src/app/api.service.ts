import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfig } from './app.config';

@Injectable({
    providedIn:'root'
})
export class ApiService {
    baseUrl = "";

    constructor(private http: HttpClient, private appConfig:AppConfig) {
        this.baseUrl = appConfig.baseUrl;
    }

    // returns instance of the httpclient
    getHttpClient(): HttpClient {
        return this.http;
    }

    /*
     * @General Methods for api operation
     */

    // get for our Httpclient
    get(path: string): Observable<any> {
        return this.http.get(this.baseUrl + path);
    }
    // post for our httpclient
    post(path: string, data: any): Observable<any> {
        return this.http.post(this.baseUrl + path, data);
    }
    // put for httpclient
    put(path: string, data: any): Observable<any> {
        return this.http.put(this.baseUrl + path, data);
    }
    // delete for our htttpclient
    delete(path: string): Observable<any> {
        return this.http.delete(this.baseUrl + path);
    }

    /*
     * Other application specific methods
     */
    getList(path: string, perPage:Number, page:Number, searchTerm?:string, sortKey?:string, sortDir?:string): Observable<any> {
        let qryStarter = "?";
        if(path.includes("?")){
            qryStarter="&";
        }
        let urlPart = qryStarter+'perPage=' + perPage + '&page=' + page;
        if (searchTerm) {
            urlPart += '&searchOption=all&searchTerm=' + searchTerm;
        }
        if (sortKey) {
            urlPart += '&sortKey=' + sortKey;
        }
        if (sortDir) {
            if (sortDir) {
                urlPart += '&sortDir=desc';
            } else {
                urlPart += '&sortDir=asc';
            }
        }
        return this.http.get(this.baseUrl + path + urlPart);
    }
    create(path: string, data: any): Observable<any> {
        return this.http.post(this.baseUrl + path, data);
    }
    update(path: string, id: any, data: any): Observable<any> {
        return this.http.put(this.baseUrl + path + '/' + id, data);
    }
    getOne(path: string, id: any): Observable<any> {
        return this.http.get(this.baseUrl + path + '/' + id);
    }
    remove(path: string, id: any): Observable<any> {
        return this.http.delete(this.baseUrl + path + '/' + id);
    }
}
