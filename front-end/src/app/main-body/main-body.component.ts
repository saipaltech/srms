import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-main-body',
  templateUrl: './main-body.component.html',
  styleUrls: ['./main-body.component.scss']
})
export class MainBodyComponent implements OnInit,OnDestroy {
  reloginPeriod:any;
  constructor(private auth:AuthService){

  }
  ngOnInit(): void {
    this.auth.getFreshToken();
    this.reloginPeriod = setInterval(()=>{
      this.auth.getFreshToken();
    },8000);
    
  }
  ngOnDestroy(): void {
    clearInterval(this.reloginPeriod);
  }
}
