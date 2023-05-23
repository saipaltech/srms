import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { ApiService } from '../api.service';
import { UsersService } from '../users/users.service';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit{
  UserImportForm: any;
  formLayout:any;
  constructor(private http:ApiService,private toastr: ToastrService, private fb: FormBuilder,private RS: UsersService){  
      this.formLayout = {
        userImport:[''],
      }
    this.UserImportForm =fb.group(this.formLayout);
  }
  ngOnInit(): void {

  }

  UserImportFormSubmit(){
    const fileInput = document.getElementById('formFileSm') as HTMLInputElement;
  
  if (fileInput && fileInput.files && fileInput.files.length > 0) {
    const file: File = fileInput.files[0];
    let formData: FormData = new FormData();
    formData.append('file', file, file.name);
    this.RS.uploadFile(formData).subscribe({
      next: (data:any) => {
        this.toastr.success(data.message, 'Success');
      }, error: error => {
        this.toastr.error(error.error.message, 'Error');
        // console.log(error);
      }
    });
    
  } else {
    console.log('No file selected.');
  }
  }
}
