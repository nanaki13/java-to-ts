/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MemoService } from './memo.service';

describe('Service: Memo', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MemoService]
    });
  });

  it('should ...', inject([MemoService], (service: MemoService) => {
    expect(service).toBeTruthy();
  }));
});
