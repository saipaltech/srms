import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable()
export class AuthGuard implements CanActivate {
	public jwtHelper = new JwtHelperService();
    constructor(private router: Router) { }

    canActivate():boolean { 
        const currentUser = localStorage.getItem('currentUser');
        if (currentUser){
            if(!this.jwtHelper.isTokenExpired(JSON.parse(currentUser).token)) 
            return true;
        }
        this.router.navigate(['/login']);
        return false;
    }
}

@Injectable()
export class LoginGuard implements CanActivate {
    public jwtHelper = new JwtHelperService();
    constructor(private router: Router) { }

    canActivate():boolean {
        const currentUser = localStorage.getItem('currentUser');
        if (currentUser){
            if(!this.jwtHelper.isTokenExpired(JSON.parse(currentUser).token)) 
            return false;
        }
        return true;
    }
}

@Injectable()
export class AuthorizeGuard implements CanActivate {
    constructor(private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot):boolean {
      //  if (route.data.perm === 'abcd') {
         //   return true;
      //  }
        return true;
    }
}
