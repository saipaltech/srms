import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Observable, catchError, filter, of, tap, throwError } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    public jwtHelper = new JwtHelperService();
    constructor(private router:Router) { }

    getToken(): string | boolean {
        const userDetails = localStorage.getItem('currentUser');
        if(userDetails){
            const currentUser = JSON.parse(userDetails);
            if(currentUser && currentUser.token){
                if(!this.jwtHelper.isTokenExpired(currentUser.token)){
                    return currentUser.token;
                }
            } 
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
        // return next.handle(req).pipe(
        //     filter((event: HttpEvent<any>) => event instanceof HttpErrorResponse && (event.status === 401 || event.status === 403)),
        //     tap(() => {
        //       this.router.navigate(['login']);
        //     })
        //   );
        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
              if (error.status === 401 || error.status === 403) {
                this.router.navigate(['login']);
                return of(new HttpResponse({
                  status: error.status,
                  statusText: error.statusText,
                  body: null
                }));
              } else {
                return throwError(() => error);
              }
            })
          );
    }
}