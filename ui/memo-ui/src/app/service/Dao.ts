import { HttpClient } from "@angular/common/http";
import { Observable, of, throwError } from "rxjs";
import { catchError, map, } from 'rxjs/operators';
export interface Dao<T,ID> {

  read(id : ID):Observable<T>
  readAll():Observable<T[]>
  create(t : T):Observable<T>
  update(t : T) : Observable<T>
  delete(t : T) : Observable<Boolean>

}

export abstract class DaoImpl<T,ID>  implements Dao<T,ID>{

  abstract readId(t : T) : ID
  handleError<A>(obs : Observable<A>,error : (error :any) => void ) : Observable<A> {
    return  obs.pipe(catchError((err )=> {
      error(err);
      return throwError(err);
    } ))
  }

  constructor(private httpClient : HttpClient,private url : string,private error : (error :any) => void  ){}
  readAll(): Observable<T[]>{
    return this.handleError(this.httpClient.get<T[]>(this.url), this.error);

  }
  read(id: ID): Observable<T> {
    return  this.handleError(this.httpClient.get<T>(`${this.url}/${id}`), this.error);
  }
  create(t: T): Observable<T> {
    return this.handleError(this.httpClient.post<T>(this.url,t), this.error);
  }
  update(t: T): Observable<T> {
    return this.handleError(this.httpClient.patch<T>(this.url,t), this.error);
  }
  delete(t: T): Observable<Boolean> {
    return this.handleError(this.httpClient.delete(`${this.url}/${this.readId(t)}`,{}).pipe(map(() => true)), this.error);
  }

}
