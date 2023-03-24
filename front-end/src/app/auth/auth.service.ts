import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { AppConfig } from '../app.config';

@Injectable()
export class AuthService {
    private path = "";
    public token: string | null = null;
    public currentUser;
    public user: any;
    public permissions: any;

    constructor(private http: HttpClient, private appConfig: AppConfig) {
        this.path = appConfig.baseUrl + "auth";
        // set token if saved in local storage
        const cu = localStorage.getItem('currentUser');
        if (cu) {
            const currentUser = JSON.parse(cu);
            this.currentUser = currentUser;
            this.token = currentUser.token;
        }
    }

    getUserDetails(){
        const cu = localStorage.getItem('currentUser');
        if (cu) {
            return JSON.parse(cu);
        }
        return null;
    }

    public login(username: string, password: string): Observable<any> {
        return new Observable(subscriber => {
            this.http.post(this.path + "/login", { username: username, password: password })
                .subscribe({next:(response:any) => {
                    const resp = JSON.parse(JSON.stringify(response));
                    const isToken = resp && resp.data && resp.data.token;
                    const data = resp.data;
                    if (isToken) {
                        this.token = data.token;
                        localStorage.setItem('currentUser', JSON.stringify({ username: username, name: data.name, token: data.token, orgName: data.orgname }));
                    }
                    subscriber.next(data);
                    subscriber.complete();
                },error: (err) => {
                    subscriber.error(err.error);
                }});
        });
    }

    public geFrontPermissions() {
        this.http.get('/front-menu').subscribe({next:response => {
            this.permissions = response;
        }, error: error => {
            console.log('Error on fetching permissions.');
        }});
    }

    public logout() {
        // clear token remove user from local storage to log user out
        this.token = null;
        localStorage.removeItem('currentUser');
    }

    public getUserInfo() {
        this.http.get(this.path + '/user').subscribe({next:result => {
            this.user = result;
        }, error: error => {
            console.log('Error on Fetching userdata.');
        }});
    }

    public getAdminInfo(){
        return this.http.get(this.appConfig.baseUrl+"admin-info").pipe(map((result:any)=>{
            result.adminlogo = this.appConfig.baseUrl+result.adminlogo;
            return result;
        }));
    }
}
