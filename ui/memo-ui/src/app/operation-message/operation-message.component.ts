import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { OperationMessageService } from '../service/operation-message.service';

@Component({
  selector: 'app-operation-message',
  templateUrl: './operation-message.component.html',
  styleUrls: ['./operation-message.component.scss']
})
export class OperationMessageComponent implements OnInit {
  message: String[] = []

  constructor(private service : OperationMessageService) {
   service.message.subscribe(m => {
    this.message .push(m);
    if(this.message.length>10){
      this.message.shift()
    }
   });
  }

  ngOnInit() {
  }

}
