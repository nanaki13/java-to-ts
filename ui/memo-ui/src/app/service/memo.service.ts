import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { DaoImpl } from './Dao';
import { Memo } from '../model/Memo';
import { OperationMessageService } from './operation-message.service';
@Injectable({
  providedIn: 'root'
})
export class MemoService extends DaoImpl<Memo,number> {
readId(t: Memo): number {
  return t.id;
}

constructor(httpClient : HttpClient,mService: OperationMessageService) {
    super(httpClient,"http://localhost:8080/memo",mService);
 }

}
