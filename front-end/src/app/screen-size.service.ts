import { Injectable, HostListener, EventEmitter } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ScreenSizeService {
  isSmallScreen = false;
  screenSizeChanged = new EventEmitter<boolean>();

  constructor() {
    this.checkScreenSize();
  }

  @HostListener('window:resize', [])
  private checkScreenSize() {
    const screenWidth = window.innerWidth;
    const isSmallScreen = screenWidth < 768; // adjust breakpoint as needed

    if (isSmallScreen !== this.isSmallScreen) {
      this.isSmallScreen = isSmallScreen;
      this.screenSizeChanged.emit(this.isSmallScreen);
    }
  }
}
