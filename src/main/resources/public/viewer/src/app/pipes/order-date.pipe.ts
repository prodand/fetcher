import {Pipe, PipeTransform} from '@angular/core';
import {Case} from '../model/Case';

@Pipe({
  name: 'orderDate'
})
export class OrderDatePipe implements PipeTransform {

  transform(values: Case[], args?: any): any {
    return (values || []).sort((a, b) => {
      if (a.lastUpdate > b.lastUpdate) {
        return -1;
      }
      if (a.lastUpdate < b.lastUpdate) {
        return 1;
      }
      if (!a.lastUpdate && !!b.lastUpdate) {
        return 1;
      }
      if (!!a.lastUpdate && !b.lastUpdate) {
        return -1;
      }
      return a.number.localeCompare(b.number);
    });
  }

}
