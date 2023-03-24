import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor() { }

    getToken(): string | boolean {
        const userDetails = localStorage.getItem('currentUser');
        if(userDetails){
            const currentUser = JSON.parse(userDetails);
            return currentUser && currentUser.token;
        }
        return false;
    }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (this.getToken()) {
            req = req.clone({
                setHeaders: {
                    Authorization: 'Bearer ' + this.getToken()
                }
            });
        }
        return next.handle(req);
    }
}