import { Pipe, PipeTransform } from '@angular/core';
import {Case} from '../model/Case';

@Pipe({
  name: 'pager'
})
export class PagerPipe implements PipeTransform {

  transform(value: Case[], page: number, size: number): any {
    const start = Math.round(page * size);
    const end = Math.min(Math.round((page + 1) * size), value.length);
    console.info(start, end)
    return (value || []).slice(start, end);
  }

}
