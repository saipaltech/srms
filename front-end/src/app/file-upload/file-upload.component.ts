import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit{
  UserImportForm: any;
  formLayout:any;
  constructor(private http:ApiService,private toastr: ToastrService, private fb: FormBuilder){  
      this.formLayout = {
        userImport:[''],
      }
    this.UserImportForm =fb.group(this.formLayout);
  }
  ngOnInit(): void {

  }

  UserImportFormSubmit(){

  }
}
