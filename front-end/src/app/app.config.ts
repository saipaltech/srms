import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";


@Injectable({
    providedIn: 'root',
})
export class AppConfig {

    public baseUrl: string = "";

    constructor(private httpClient: HttpClient) { }

    loadConfig() {
        return new Promise((resolve, reject) => {
            this.httpClient.get('assets/config.json').subscribe({
                next: (resp: any) => {
                    this.baseUrl = resp.baseUrl;
                    resolve(true);
                }, error: (err: any) => {
                    console.log(err.error);
                    reject();
                }
            });
        });
    }
};
