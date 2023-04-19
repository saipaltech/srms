import { Injectable } from '@angular/core';
import {LoaderService} from './loader.service'
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
} from '@angular/common/http';
import { Observable, finalize } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HttpInterceptorService implements HttpInterceptor {

  private count = 0;

  constructor(private loaderService: LoaderService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    if (this.count === 0) {
      this.loaderService.setHttpProgressStatus(true);
    }
    this.count++;
    return next.handle(req).pipe(
      finalize(() => {
    this.count--;
    if (this.count === 0) {
      this.loaderService.setHttpProgressStatus(false);
    }
      })
    );
  }
}
