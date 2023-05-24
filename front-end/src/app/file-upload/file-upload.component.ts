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
    const fileName: string = file.name;
    const fileExtension: string = fileName.substring(fileName.lastIndexOf('.') + 1);
    if(fileExtension=="xlsx"){
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
  }else{
    this.toastr.error("Invalid file format. Please upload xlsx file type.",'Error');
  }
    
  } else {
    this.toastr.error("No file Selected",'Error');
  }
  }
}
