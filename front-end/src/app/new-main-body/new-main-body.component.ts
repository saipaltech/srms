import { Component, OnDestroy, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';

@Component({
  selector: 'app-new-main-body',
  templateUrl: './new-main-body.component.html',
  styleUrls: ['./new-main-body.component.scss']
})
export class NewMainBodyComponent implements OnInit,OnDestroy{
  showSpinner!: Boolean

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
