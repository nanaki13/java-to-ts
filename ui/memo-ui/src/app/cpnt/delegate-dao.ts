import { Component, OnInit } from "@angular/core";
import { Dao } from "../service/Dao";

export abstract class DelegateDao<T,ID>  {

  abstract get editing() : T;
  abstract get listEntities() : T[];
  abstract set listEntities(e:  T[]);
  constructor(protected service : Dao<T,ID> ){}

  _ngOnInit() {
    this.service.readAll().subscribe(ms => {
      this.listEntities = ms
    },e =>[])
  }
  add(){
    this.service.create(this.editing).subscribe(ms =>{
      this.listEntities.push(ms);
    })
  }

  remove(a : T){
    this.service.delete(a).subscribe(ms =>{
     this.listEntities.splice(this.listEntities.indexOf(a),1)
    })
  }
}
