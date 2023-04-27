import { HttpBackend, HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";


@Injectable({
    providedIn: 'root',
})
export class AppConfig {

    public baseUrl: string = "";
    private httpClient: HttpClient;

    constructor(private direct: HttpBackend) { 
        this.httpClient = new HttpClient(direct);
    }

    loadConfig() {
        return new Promise((resolve, reject) => {
            this.httpClient.get('/assets/config.json').subscribe({
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
