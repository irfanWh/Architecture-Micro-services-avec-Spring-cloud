#!/usr/bin/env python3
"""Checks real HTTP traffic through Gateway, Eureka, Config and OpenFeign."""
import json, time
from urllib.request import Request, urlopen
from urllib.error import HTTPError
BASE='http://localhost:8888'
def call(path,method='GET',data=None,status=200):
    request=Request(BASE+path,method=method,data=json.dumps(data).encode() if data is not None else None,headers={'Content-Type':'application/json'})
    try:
        with urlopen(request,timeout=15) as response:
            assert response.status==status,(path,response.status)
            raw=response.read()
            return json.loads(raw) if raw else None
    except HTTPError as error:
        assert error.code==status,(path,error.code,error.read())
customers=[]; products=[]; bills=[]
try:
    c=call('/customers','POST',{'name':'Smoke Test','email':'smoke@example.com'},201);customers.append(c['id'])
    p=call('/products','POST',{'name':'Smoke Product','price':125.50,'quantity':10},201);products.append(p['id'])
    call('/customers/'+str(c['id']),'PUT',{'name':'Updated Smoke','email':'smoke@example.com'})
    request={'customerId':c['id'],'items':[{'productId':p['id'],'quantity':3}]}
    # Eureka registry propagation for billing and Feign can lag behind direct readiness.
    for attempt in range(30):
        try:
            b=call('/bills','POST',request,201);break
        except AssertionError:
            if attempt==29: raise
            time.sleep(2)
    bills.append(b['id']);assert b['total']==376.5 and b['customerName']=='Updated Smoke'
    call('/products/'+str(p['id']),'PUT',{'name':'Changed Product','price':999,'quantity':10})
    snapshot=call('/bills/'+str(b['id'])+'/full');assert snapshot['total']==376.5 and snapshot['items'][0]['productName']=='Smoke Product'
    call('/customers','POST',{'name':'','email':'invalid'},400)
    call('/products','POST',{'name':'Bad price','price':-1,'quantity':-1},400)
    call('/bills','POST',{'customerId':c['id'],'items':[]},400)
    call('/bills','POST',{'customerId':c['id'],'items':[{'productId':p['id'],'quantity':11}]},400)
    call('/bills','POST',{'customerId':c['id'],'items':[{'productId':p['id'],'quantity':1}]*2},400)
    call('/customers/999999999',status=404)
    with urlopen('http://localhost:9999/customer-service/default') as response:
        config=json.load(response);assert any(s['source'].get('info.project') for s in config['propertySources'])
    expected={'CUSTOMER-SERVICE','INVENTORY-SERVICE','BILLING-SERVICE','GATEWAY-SERVICE'}
    for attempt in range(30):
        with urlopen(Request('http://localhost:8761/eureka/apps',headers={'Accept':'application/json'})) as response:
            names={a['name'] for a in json.load(response)['applications']['application']}
        if expected<=names: break
        time.sleep(2)
    assert expected<=names,names
    print('PASS : CRUD, validation, Gateway, Eureka, OpenFeign, instantanés, totaux et Config.')
finally:
    for kind,ids in [('bills',bills),('products',products),('customers',customers)]:
        for id in ids: call('/'+kind+'/'+str(id),'DELETE',status=204)
