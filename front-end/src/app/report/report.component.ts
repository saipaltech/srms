import { Component } from '@angular/core';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.scss']
})
export class ReportComponent {
  bsInlineValue = new Date();
  bsInlineRangeValue: Date[];
  maxDate = new Date();

  constructor() {
    this.maxDate.setDate(this.maxDate.getDate() + 7);
    this.bsInlineRangeValue = [this.bsInlineValue, this.maxDate];
  }

  buttonClick(){
    console.log(this.bsInlineRangeValue);
    console.log("UP start value, below end date");
    console.log(this.maxDate);

  }

}
