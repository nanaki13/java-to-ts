import { Component, OnInit } from '@angular/core';
import { of } from 'rxjs';
import { Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DelegateDao } from '../cpnt/delegate-dao';
import { Memo } from '../model/Memo';
import { MemoService } from '../service/memo.service';

@Component({
  selector: 'app-memo',
  templateUrl: './memo.component.html',
  styleUrls: ['./memo.component.scss']
})
export class MemoComponent extends  DelegateDao<Memo,number> implements OnInit {
  get editing(): Memo {
    return this.memoContent;
  }
  get listEntities(): Memo[] {
    return this.memos;
  }
  set listEntities(e: Memo[]) {
    this.memos = e;
  }
  memos: Memo[] = []
  memoContent : Memo = {} as Memo

  constructor(private memoService : MemoService)  {
      super(memoService)
   }
  ngOnInit(): void {
   this._ngOnInit();
  }





}
