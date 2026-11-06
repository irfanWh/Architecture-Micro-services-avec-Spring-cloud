import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
export interface Customer {id:number;name:string;email:string}
export interface Product {id:number;name:string;price:number;quantity:number}
export interface Item {productId:number;productName:string;unitPrice:number;quantity:number;total:number}
export interface Bill {id:number;createdAt:string;customerId:number;customerName:string;customerEmail:string;items:Item[];total:number}
@Injectable({providedIn:'root'}) export class Api {
 private http=inject(HttpClient);
 customers(){return this.http.get<Customer[]>('/api/customers')}
 products(){return this.http.get<Product[]>('/api/products')}
 bills(){return this.http.get<Bill[]>('/api/bills')}
 bill(id:number){return this.http.get<Bill>('/api/bills/'+id+'/full')}
 saveCustomer(c:Partial<Customer>){return c.id?this.http.put<Customer>('/api/customers/'+c.id,c):this.http.post<Customer>('/api/customers',c)}
 saveProduct(p:Partial<Product>){return p.id?this.http.put<Product>('/api/products/'+p.id,p):this.http.post<Product>('/api/products',p)}
 delete(kind:'customers'|'products'|'bills',id:number){return this.http.delete('/api/'+kind+'/'+id)}
 createBill(customerId:number,items:{productId:number;quantity:number}[]){return this.http.post<Bill>('/api/bills',{customerId,items})}
}
