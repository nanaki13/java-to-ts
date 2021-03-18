import { EventEmitter, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OperationMessageService {

  message: EventEmitter<String> = new EventEmitter<String>()
constructor() { }

}
