import { Component, Inject, Renderer2 } from '@angular/core';
import { LoaderService } from './loader.service';
import { DOCUMENT } from '@angular/common';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'SRMS-bank';

    constructor(@Inject(DOCUMENT) document: Document ,private loaderService: LoaderService, private renderer: Renderer2) {
      this.loaderService.httpProgress().subscribe((status: boolean) => {
        if (status) {
  
        document.getElementById('myOverLay')!.classList.toggle('showOverlay');
        } else {
          document.getElementById('myOverLay')!.classList.toggle('showOverlay');

        }
      });
    }
     }

  // ngAfterViewInit() {
  //   this.loaderService.httpProgress().subscribe((status: boolean) => {
  //     if (status) {
  //     //   const myNav = this.renderer.selectRootElement('#myOverLay');
  //     // this.renderer.addClass(myNav, 'showOverlay');
  //     this.document 
  //     } else {
  //       const myNav = this.renderer.selectRootElement('#myOverLay');
  //     this.renderer.addClass(myNav, 'showOverlay');
  //     }
  //   });
  // }
