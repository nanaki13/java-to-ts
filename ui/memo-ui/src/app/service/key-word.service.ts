import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { KeyWord } from '../model/KeyWord';
import { DaoImpl } from './Dao';
import { OperationMessageService } from './operation-message.service';

@Injectable({
  providedIn: 'root'
})
export class KeyWordService extends DaoImpl<KeyWord,number> {
  readId(t: KeyWord): number {
    return t.id
  }

  constructor(httpClient : HttpClient,mService: OperationMessageService) {
      super(httpClient,"http://localhost:8080/keyword",(err) => {
        if(err instanceof HttpErrorResponse){
          const erTyped = err as HttpErrorResponse
          mService.message.emit(`${erTyped.error}`)
        }

      })
   }

  }
