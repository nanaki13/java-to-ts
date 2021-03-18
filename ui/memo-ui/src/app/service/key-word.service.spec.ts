/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { KeyWordService } from './key-word.service';

describe('Service: KeyWord', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [KeyWordService]
    });
  });

  it('should ...', inject([KeyWordService], (service: KeyWordService) => {
    expect(service).toBeTruthy();
  }));
});
