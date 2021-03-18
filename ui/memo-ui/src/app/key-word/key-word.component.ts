import { Component, OnInit } from '@angular/core';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DelegateDao } from '../cpnt/delegate-dao';
import { KeyWord } from '../model/KeyWord';
import { KeyWordService } from '../service/key-word.service';

@Component({
  selector: 'app-key-word',
  templateUrl: './key-word.component.html',
  styleUrls: ['./key-word.component.scss']
})
export class KeyWordComponent extends  DelegateDao<KeyWord,number> implements OnInit {
  get editing(): KeyWord {
    return this.keywWordEdit;
  }
  get listEntities(): KeyWord[] {
    return this.keywords;
  }
  set listEntities(e: KeyWord[]) {
    this.keywords = e;
  }
  keywords: KeyWord[] = []
  keywWordEdit : KeyWord = {} as KeyWord

  constructor(protected service : KeyWordService) {
      super(service)
   }

  ngOnInit() {
    this._ngOnInit();
  }

}

