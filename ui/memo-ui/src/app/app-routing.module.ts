import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { KeyWordComponent } from './key-word/key-word.component';
import { MemoComponent } from './memo/memo.component';

const routes: Routes = [
  { path: 'memo', component: MemoComponent },
  { path: 'keyword', component: KeyWordComponent }

];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
