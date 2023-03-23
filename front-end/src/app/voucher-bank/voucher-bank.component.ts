import { Component } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-voucher-bank',
  templateUrl: './voucher-bank.component.html',
  styleUrls: ['./voucher-bank.component.scss']
})
export class VoucherBankComponent {

  constructor(private toastr: ToastrService) {}

  showSuccess() {
    this.toastr.success('Hello world!', 'Toastr fun!');
  }

}
