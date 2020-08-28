import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Case} from '../model/Case';

@Injectable()
export class ApiService {

  constructor(private http: HttpClient) {
  }

  listAll(): Observable<Case[]> {
    return this.http.get<Case[]>('api/cases');
  }
}
