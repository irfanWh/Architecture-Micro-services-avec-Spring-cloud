#!/usr/bin/env python3
"""Start/stop the local demonstration without Docker. Run from any directory."""
import json, os, signal, socket, subprocess, sys, time
from pathlib import Path
from urllib.request import urlopen
ROOT = Path(__file__).resolve().parents[1]
SERVICES = [('config-service',9999),('discovery-service',8761),('customer-service',8081),('inventory-service',8082),('billing-service',8083),('gateway-service',8888)]
STATE = ROOT / '.pids'
def health(port):
    try:
        with urlopen(f'http://localhost:{port}/actuator/health',timeout=3) as response:
            return json.load(response)['status'] == 'UP'
    except Exception:
        return False
def stop():
    stopped_ports = []
    for name, port in reversed(SERVICES):
        file = STATE / (name+'.json')
        if not file.exists(): continue
        record = json.loads(file.read_text())
        command = subprocess.run(['ps','-p',str(record['pid']),'-o','command='],capture_output=True,text=True).stdout
        if record['jar'] in command:
            os.kill(record['pid'],signal.SIGTERM)
            stopped_ports.append(port)
            print('Arrêt :',name)
        file.unlink()
    deadline = time.monotonic()+30
    while stopped_ports and time.monotonic()<deadline:
        active = []
        for port in stopped_ports:
            with socket.socket() as sock:
                if sock.connect_ex(('localhost',port)) == 0: active.append(port)
        stopped_ports = active
        if stopped_ports: time.sleep(.5)
    if stopped_ports: raise SystemExit(f'Arrêt incomplet ; ports encore occupés : {stopped_ports}')
def start():
    STATE.mkdir(exist_ok=True)
    (ROOT/'logs').mkdir(exist_ok=True)
    for name, port in SERVICES:
        jar = ROOT/name/'target'/f'{name}-1.0.0-SNAPSHOT.jar'
        if not jar.exists(): raise SystemExit('Compiler d’abord : ./mvnw verify')
        with socket.socket() as sock:
            if sock.connect_ex(('localhost',port)) == 0:
                raise SystemExit(f'Port {port} occupé. Vérifier le service avant de relancer.')
    try:
        for name, port in SERVICES:
            jar = str(ROOT/name/'target'/f'{name}-1.0.0-SNAPSHOT.jar')
            env = dict(os.environ)
            env['CONFIG_REPO'] = str(ROOT/'config-repo')
            command = [str(Path(env['JAVA_HOME'])/'bin/java') if env.get('JAVA_HOME') else 'java','-Xms64m','-Xmx256m','-jar',jar]
            if name == 'gateway-service' and '--static' in sys.argv: command += ['--spring.profiles.active=static']
            with (ROOT/'logs'/f'{name}.log').open('a') as log:
                process = subprocess.Popen(command,cwd=ROOT,env=env,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
            (STATE/(name+'.json')).write_text(json.dumps({'pid':process.pid,'jar':jar}))
            deadline = time.monotonic()+120
            while not health(port):
                if process.poll() is not None or time.monotonic()>deadline:
                    raise RuntimeError(f'Échec {name}. Voir logs/{name}.log')
                time.sleep(1)
            print(f'Prêt : {name} → http://localhost:{port}',flush=True)
        print('Attente de la propagation Eureka…',flush=True)
        deadline=time.monotonic()+100
        while time.monotonic()<deadline:
            try:
                # Eureka and LoadBalancer caches may discover each service at different times.
                # Read-only probes ensure every route is ready before tests can create data.
                for path in ('customers','products','bills'):
                    with urlopen('http://localhost:8888/'+path,timeout=5) as response:
                        if response.status != 200: raise RuntimeError('Route indisponible : '+path)
                print('Passerelle prête : clients, produits et factures.',flush=True)
                return
            except Exception: pass
            time.sleep(2)
        raise RuntimeError('Routes non disponibles. Vérifier Eureka et les journaux.')
    except BaseException:
        stop()
        raise
if __name__ == '__main__':
    action = sys.argv[1] if len(sys.argv)>1 else 'status'
    if action == 'start': start()
    elif action == 'stop': stop()
    elif action == 'status':
        for name, port in SERVICES: print(name, 'UP' if health(port) else 'DOWN')
    else: raise SystemExit('Usage: python3 scripts/services.py start [--static] | stop | status')
