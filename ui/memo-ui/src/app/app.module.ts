import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MemoComponent } from './memo/memo.component';
import { OperationMessageComponent } from './operation-message/operation-message.component';
import { KeyWordComponent } from './key-word/key-word.component';
import { DelegateDao } from './cpnt/delegate-dao';

@NgModule({
  declarations: [
    AppComponent,
      MemoComponent,
      OperationMessageComponent,
      KeyWordComponent
   ],
  imports: [
    HttpClientModule,
    FormsModule,
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
