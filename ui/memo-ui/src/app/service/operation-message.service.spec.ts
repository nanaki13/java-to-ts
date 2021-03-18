/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { OperationMessageService } from './operation-message.service';

describe('Service: OperationMessage', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OperationMessageService]
    });
  });

  it('should ...', inject([OperationMessageService], (service: OperationMessageService) => {
    expect(service).toBeTruthy();
  }));
});
