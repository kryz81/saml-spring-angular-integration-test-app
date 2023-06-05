import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent {
  authUser = '';

  constructor(private readonly http: HttpClient) {}

  fetch() {
    this.http
      .get('http://localhost:8080/authenticated', { withCredentials: true })
      .subscribe((data) => {
        if (data === false) {
          window.location.href = 'http://localhost:8080/saml2/authenticate';
        } else {
          this.http
            .get('http://localhost:8080', { withCredentials: true })
            .subscribe((data: Record<string, any>) => {
              console.log(data);
              this.authUser = data['emailAddress'];
            });
        }
      });
  }

  logout() {
    window.location.href = 'http://localhost:8080/logout';
  }
}
