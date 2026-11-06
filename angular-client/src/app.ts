import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Api, Customer, Product, Bill } from './api';
@Component({selector:'app-root',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./app.html'})
export class AppComponent {
 private api=inject(Api);
 tab=signal<'overview'|'customers'|'products'|'bills'>('overview'); customers=signal<Customer[]>([]); products=signal<Product[]>([]); bills=signal<Bill[]>([]);
 busy=signal(false);error=signal('');notice=signal(''); detail=signal<Bill|null>(null);
 customer:Partial<Customer>={};product:Partial<Product>={}; customerId=0; lines=[{productId:0,quantity:1}];
 constructor(){this.load()}
 load(){this.busy.set(true);this.error.set('');forkJoin({customers:this.api.customers(),products:this.api.products(),bills:this.api.bills()}).subscribe({next:r=>{this.customers.set(r.customers);this.products.set(r.products);this.bills.set(r.bills);this.busy.set(false)},error:e=>this.fail(e)})}
 fail(e:any){this.busy.set(false);this.error.set(e.error?.message?.toString()||'Connexion impossible. Vérifiez le démarrage des services, puis réessayez.');}
 saved(message:string){this.notice.set(message);this.load()}
 saveCustomer(){this.busy.set(true);this.api.saveCustomer(this.customer).subscribe({next:()=>{this.customer={};this.saved('Client enregistré.')},error:e=>this.fail(e)})}
 saveProduct(){this.busy.set(true);this.api.saveProduct(this.product).subscribe({next:()=>{this.product={};this.saved('Produit enregistré.')},error:e=>this.fail(e)})}
 remove(kind:'customers'|'products'|'bills',id:number){if(!confirm('Confirmer la suppression ?'))return;this.busy.set(true);this.api.delete(kind,id).subscribe({next:()=>{this.detail.set(null);this.saved('Suppression effectuée.')},error:e=>this.fail(e)})}
 addLine(){this.lines.push({productId:0,quantity:1})}
 removeLine(i:number){this.lines.splice(i,1)}
 total(){return this.lines.reduce((s,l)=>s+(this.products().find(p=>p.id===Number(l.productId))?.price||0)*l.quantity,0)}
 createBill(){this.busy.set(true);this.api.createBill(Number(this.customerId),this.lines.map(l=>({productId:Number(l.productId),quantity:Number(l.quantity)}))).subscribe({next:b=>{this.lines=[{productId:0,quantity:1}];this.customerId=0;this.detail.set(b);this.saved('Facture créée.')},error:e=>this.fail(e)})}
 viewBill(id:number){this.api.bill(id).subscribe({next:b=>this.detail.set(b),error:e=>this.fail(e)})}
 revenue(){return this.bills().reduce((s,b)=>s+b.total,0)}
 print(){window.print()}
}
