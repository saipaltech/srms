import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {TrialService} from './trial.service'


@Component({
  selector: 'app-trial',
  templateUrl: './trial.component.html',
  styleUrls: ['./trial.component.scss']
})
export class TrialComponent implements OnInit{
  voucherno: any;
  palika: any;

  constructor(private route: ActivatedRoute, private RS: TrialService) { }

  ngOnInit(): void {
    // this.myData = this.route.snapshot.data['voucherno'];
    this.route.queryParams.subscribe(params => {
      this.voucherno = params['voucherno'];
      this.palika = params['palika'];

    });
  
    this.getData(this.voucherno, this.palika);
    // console.log(this.myData)
  }
  reportData: any;

  getData(voucherno: any, palika: any){
    this.RS.getReport(voucherno, palika).subscribe({next:(dt)=>{
      this.reportData = dt;
      console.log(this.reportData);
    },error:err=>{
      
    }});
  }

}
