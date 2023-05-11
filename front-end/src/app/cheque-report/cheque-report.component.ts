import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {TrialService} from '../trial/trial.service'


@Component({
  selector: 'app-cheque-report',
  templateUrl: './cheque-report.component.html',
  styleUrls: ['./cheque-report.component.scss']
})
export class ChequeReportComponent {

  voucherno: any;
  palika: any;

  fromChequeClear=false;

  constructor(private route: ActivatedRoute, private RS: TrialService) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.voucherno = params['voucherno'];
      this.palika = params['palika'];
      this.fromChequeClear = params['formvalue'];
      console.log(this.fromChequeClear, this.voucherno);
    });
  
    this.getData(this.voucherno, this.palika);
    this.getRevenueDetails(this.voucherno, this.palika);
    // console.log(this.myData)
  }
  reportData: any;
  rdetails:any;
  

  getData(voucherno: any, palika: any){
    this.RS.getReport(voucherno, palika).subscribe({next:(dt)=>{
      this.reportData = dt;
      // console.log(this.reportData);
    },error:err=>{
      
    }});
  }

  getRevenueDetails(voucherno: any, palika: any){
    this.RS.getRevenueDetails(voucherno, palika).subscribe({next:(dt)=>{
      this.rdetails = dt.data;
      // console.log(this.rdetails);
    },error:err=>{
      
    }});
  }

  hi = 0;

}
