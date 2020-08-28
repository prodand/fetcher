import {Component, OnInit} from '@angular/core';
import {ApiService} from './services/api.service';
import {Case} from './model/Case';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  cases: Case[];
  filteredCases: Case[];
  currentPage = 0;
  fNumber: string;
  fStatus: string;

  constructor(private api: ApiService) {
  }

  ngOnInit(): void {
    this.api.listAll().subscribe(result => this.filteredCases = this.cases = result)
  }

  pages() {
    const length = (this.filteredCases || []).length;
    let size = Math.round(length / 20) + (length % 20 == 0 ? 0 : 1);
    return Array(size).map((val, index) => index + 1);
  }

  goToPage(page: number) {
    this.currentPage = page;
  }

  filterByNumber() {
    this.filter();
  }

  filterByStatus() {
    this.filter();
  }

  private filter() {
    this.filteredCases = this.cases.filter(cs => !this.fNumber ? true : cs.number.includes(this.fNumber))
      .filter(cs => !this.fStatus ? true : cs.status.includes(this.fStatus));
    this.currentPage = 0;
  }

}
