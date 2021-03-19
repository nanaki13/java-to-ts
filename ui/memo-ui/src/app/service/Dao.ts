import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Observable, of, throwError } from "rxjs";
import { catchError, map, } from 'rxjs/operators';
import { OperationMessageService } from "./operation-message.service";
export interface Dao<T,ID> {

  read(id : ID):Observable<T>
  readAll():Observable<T[]>
  create(t : T):Observable<T>
  update(t : T) : Observable<T>
  delete(t : T) : Observable<Boolean>

}

export abstract class DaoImpl<T,ID>  implements Dao<T,ID>{

  abstract readId(t : T) : ID

  erreur(mService: OperationMessageService,err : any)  {
      if(err instanceof HttpErrorResponse){
        const erTyped = err as HttpErrorResponse
        console.log(err)
        mService.message.emit(`erreur avec le back`)
      }


 }
  handleError<A>(obs : Observable<A>,mService: OperationMessageService ) : Observable<A> {
    return  obs.pipe(catchError((err )=> {
      this.erreur(mService,err);
      return throwError(err);
    } ))
  }

  constructor(private httpClient : HttpClient,private url : string,private mService: OperationMessageService  ){}
  readAll(): Observable<T[]>{
    return this.handleError(this.httpClient.get<T[]>(this.url), this.mService);

  }
  read(id: ID): Observable<T> {
    return  this.handleError(this.httpClient.get<T>(`${this.url}/${id}`), this.mService);
  }
  create(t: T): Observable<T> {
    return this.handleError(this.httpClient.post<T>(this.url,t), this.mService);
  }
  update(t: T): Observable<T> {
    return this.handleError(this.httpClient.patch<T>(this.url,t), this.mService);
  }
  delete(t: T): Observable<Boolean> {
    return this.handleError(this.httpClient.delete(`${this.url}/${this.readId(t)}`,{}).pipe(map(() => true)), this.mService);
  }

}
