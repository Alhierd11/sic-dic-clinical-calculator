"""Offline specification checks and independent Decimal reference tests (not native build tests)."""
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path
import json
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RULES = json.loads((ROOT/'clinical-spec/clinical-rules.json').read_text())
def validate(value, schema):
    """Validate only the schema keywords used by this repository, rejecting unsupported keywords."""
    assert not set(schema) - {'$schema','type','required','additionalProperties','properties','items','prefixItems','minItems','maxItems','minimum','maximum','minLength','pattern','enum'}
    if 'enum' in schema: assert value in schema['enum']; return
    kind=schema.get('type')
    types={'object':dict,'array':list,'string':str,'integer':int,'boolean':bool}
    if kind: assert type(value) is types[kind], (value,kind)
    if kind=='object':
        assert set(schema.get('required',[])) <= value.keys()
        if schema.get('additionalProperties') is False: assert value.keys() <= schema['properties'].keys()
        for k,v in value.items(): validate(v,schema['properties'][k])
    if kind=='array':
        assert schema.get('minItems',0)<=len(value)<=schema.get('maxItems',10**9)
        for i,v in enumerate(value): validate(v,schema['prefixItems'][i] if 'prefixItems' in schema else schema['items'])
    if kind=='string':
        assert len(value)>=schema.get('minLength',0)
        if 'pattern' in schema: assert re.fullmatch(schema['pattern'],value)
    if kind=='integer': assert schema.get('minimum',value)<=value<=schema.get('maximum',value)

def num(raw, positive=False):
    if not re.fullmatch(r'[0-9]{1,9}(?:[.,][0-9]{1,6})?',raw.strip()): raise ValueError('number')
    n=Decimal(raw.strip().replace(',','.'))
    if positive and n<=0: raise ValueError('positive')
    return n
def flag(raw):
    if raw not in ['true','false']: raise ValueError('boolean')
    return raw=='true'
def band(name,value,scale=Decimal(1)):
    for op,t,p in RULES['bands'][name]:
        boundary=Decimal(t)*scale
        if op=='always' or {'lt':value<boundary,'le':value<=boundary,'gt':value>boundary,'ge':value>=boundary}.get(op,False): return p
    raise AssertionError('No terminal band')
def vector(op,a):
    try:
        if op=='sic':
            p,i,s=num(a[0]),num(a[1],True),num(a[2])
            if s!=s.to_integral() or s>16: raise ValueError('sofa')
            pts=[band('sicPlatelets',p),band('sicInr',i),band('sicSofa',s)]
            result=pts+[sum(pts),sum(pts)>=4 and sum(pts[:2])>2]
        elif op=='dic':
            if not flag(a[7]): raise ValueError('comparison')
            p,d,u,pt,control,f=num(a[0]),num(a[1]),num(a[2],True),num(a[3],True),num(a[4],True),num(a[5])
            if a[6]=='mg/dL': f/=100
            elif a[6]!='g/L': raise ValueError('unit')
            pts=[band('dicPlatelets',p),band('dicDimer',d,u),band('dicPt',pt-control),band('dicFibrinogen',f)]
            result=pts+[sum(pts),sum(pts)>=5,pt<control]
        elif op=='convert':
            divisor={'fibrinogen':'100','bilirubin':'17.1','creatinine':'88.4'}[a[1]]
            return format((num(a[0])/Decimal(divisor)).quantize(Decimal('0.000001'),rounding=ROUND_HALF_UP),'f').rstrip('0').rstrip('.') or '0'
        elif op=='sofa':
            if not flag(a[11]): raise ValueError('duration')
            if a[4] not in ['mg/dL','umol/L']: raise ValueError('unit')
            hs,ks=(Decimal('17.1'),Decimal('88.4')) if a[4]=='umol/L' else (Decimal(1),Decimal(1))
            r=band('respiratorySupported' if flag(a[1]) else 'respiratoryUnsupported',num(a[0]))
            c=max(band('map',num(a[6],True)),band('dopamine',num(a[7])),band('epinephrine',num(a[8])),band('epinephrine',num(a[9])),band('dobutamine',num(a[10])))
            h=band('hepaticMgDl',num(a[2]),hs); k=max(band('renalMgDl',num(a[3]),ks),band('urine24h',num(a[5])))
            result=[r,c,h,k,r+c+h+k]
        else: raise ValueError('operation')
        return ','.join(str(x).lower() for x in result)
    except ValueError: return 'ERROR'

def main():
    validate(RULES,json.loads((ROOT/'clinical-spec/clinical-rules.schema.json').read_text()))
    assert (ROOT/'clinical-spec/VERSION').read_text().strip()==RULES['version']
    subprocess.run([sys.executable,str(ROOT/'tools/generate.py'),'--check'],check=True)
    ids=set()
    for p in sorted((ROOT/'shared-test-vectors').glob('*-tests.json')):
        for t in json.loads(p.read_text()):
            assert t['id'] not in ids; ids.add(t['id'])
            actual=vector(t['operation'],t['input'])
            assert actual==t['expected'],(t['id'],actual,t['expected'])
    ru=json.loads((ROOT/'localization/ru.json').read_text());en=json.loads((ROOT/'localization/en.json').read_text())
    assert ru.keys()==en.keys()
    for lang,folder in [('ru','values'),('en','values-en')]:
        xml=ET.parse(ROOT/f'android/app/src/main/res/{folder}/strings.xml')
        assert {e.attrib['name'] for e in xml.getroot()}==ru.keys()
    for p in list(ROOT.rglob('*.xml'))+list(ROOT.rglob('*.xaml'))+list(ROOT.rglob('*.csproj')): ET.parse(p)
    manifest=(ROOT/'android/app/src/main/AndroidManifest.xml').read_text()
    assert 'android.permission.INTERNET' not in manifest and 'android:allowBackup="false"' in manifest
    for folder in ['android','windows']:
        for ext in ['*.java','*.kt','*.cs']:
            for p in (ROOT/folder).rglob(ext):
                assert not re.search(r'WebView|HttpClient|HttpURLConnection|Firebase|Sentry|Socket\(',p.read_text()),p
    print(f'PASS: schema shape, generated consistency, {len(ids)} Decimal vectors, localization keys, XML, offline source guardrails')
    print('Native compilation and runtime UX are separate gates; this result does not certify them.')

if __name__=='__main__': main()
