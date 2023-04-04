import { Component } from '@angular/core';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot, ActivatedRoute } from '@angular/router';


@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss']
})
export class ReportComponent {
  // data = this.route.snapshot.data;  


  myVariable: any;
constructor(private router: ActivatedRoute) {
  this.router.queryParams.subscribe(params => {
    // Retrieve the 'result' query parameter value
    this.myVariable = params['result'];
    console.log(this.myVariable); // Log the retrieved value to the console
  });
  // this.myVariable = this.router.snapshot.queryParamMap.get('result');
  // const myVariable = this.router.snapshot.queryParamMap.get('result');

  // console.log(this.myVariable)
}



  

}
