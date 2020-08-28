import { OrderDatePipe } from './order-date.pipe';

describe('OrderDatePipe', () => {
  it('create an instance', () => {
    const pipe = new OrderDatePipe();
    expect(pipe).toBeTruthy();
  });
});
