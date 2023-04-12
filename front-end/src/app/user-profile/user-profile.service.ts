import { Injectable } from '@angular/core';
import { ApiService } from '../api.service';


@Injectable({
  providedIn: 'root'
})
export class UserProfileService {
  
  url="users";

  update(id: any, data: any) {
    return this.http.put(this.url + '/' + id, data);
  }

  constructor(private http: ApiService) { 
  }

getDetails(username: string) {
  return this.http.get(this.url + '/get-user-details');

}

changePassword(data:any){
  return this.http.post(this.url+'/change-password', data);
}

}
