#!/usr/bin/env python3
"""Fetch fixed Maven plugins+deps (transitive closure) into ~/workspace/m2repo.
Network via env proxy (urllib). Re-runnable: skips existing non-empty files."""
import os, re, sys, urllib.request

BASE = "https://repo.maven.apache.org/maven2"
REPO = os.path.expanduser("~/workspace/m2repo")
SEEN = set()

PLUGINS = [
    ("org.apache.maven.plugins", "maven-resources-plugin", "3.3.1"),
    ("org.apache.maven.plugins", "maven-compiler-plugin", "3.11.0"),
    ("org.apache.maven.plugins", "maven-surefire-plugin", "3.2.5"),
    ("org.apache.maven.plugins", "maven-war-plugin", "3.4.0"),
]
DEPS = [
    ("jakarta.servlet", "jakarta.servlet-api", "5.0.0"),
    ("jakarta.servlet.jsp.jstl", "jakarta.servlet.jsp.jstl-api", "2.0.0"),
    ("org.glassfish.web", "jakarta.servlet.jsp.jstl", "2.0.0"),
    ("com.mysql", "mysql-connector-j", "8.0.33"),
]

def fetch(url, dest):
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        return True
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=60) as r, open(dest, "wb") as f:
        f.write(r.read())
    print("got", url.split(BASE, 1)[1], flush=True)
    return True

def gpath(g, a, v):
    return f"{g.replace('.', '/')}/{a}/{v}"

def get_pom(g, a, v):
    gp = gpath(g, a, v)
    dest = f"{REPO}/{gp}/{a}-{v}.pom"
    if not os.path.exists(dest) or os.path.getsize(dest) == 0:
        fetch(f"{BASE}/{gp}/{a}-{v}.pom", dest)
    with open(dest) as f:
        return f.read()

def props_of(pom):
    props = {}
    m = re.search(r"<properties>(.*?)</properties>", pom, re.S)
    if m:
        for pm in re.finditer(r"<([\w.\-]+)>(.*?)</\1>", m.group(1), re.S):
            props[pm.group(1)] = pm.group(2).strip()
    return props

def resolve_str(s, props):
    def rep(m):
        return props.get(m.group(1), m.group(0))
    for _ in range(8):
        s2 = re.sub(r"\$\{([\w.\-]+)\}", rep, s)
        if s2 == s:
            break
        s = s2
    return s

def parse_deps(pom, props):
    deps = []
    for m in re.finditer(r"<dependency>(.*?)</dependency>", pom, re.S):
        block = m.group(1)
        def tag(t):
            mm = re.search(rf"<{t}>(.*?)</{t}>", block, re.S)
            return mm.group(1).strip() if mm else None
        scope = tag("scope") or "compile"
        if scope in ("test", "provided", "system"):
            continue
        if tag("optional") == "true":
            continue
        g, a, v = tag("groupId"), tag("artifactId"), tag("version")
        typ = tag("type") or "jar"
        if not (g and a and v):
            continue
        v = resolve_str(v, props)
        if "${" in v or not v:
            continue
        g = resolve_str(g, props); a = resolve_str(a, props)
        deps.append((g, a, v, typ))
    return deps

def ensure_parent(pom, props):
    m = re.search(r"<parent>(.*?)</parent>", pom, re.S)
    if not m:
        return props
    block = m.group(1)
    def tag(t):
        mm = re.search(rf"<{t}>(.*?)</{t}>", block, re.S)
        return mm.group(1).strip() if mm else None
    g, a, v = tag("groupId"), tag("artifactId"), tag("version")
    if g and a and v:
        v = resolve_str(v, props)
        pkey = (g, a, v)
        if pkey not in SEEN:
            SEEN.add(pkey)
            try:
                ppom = get_pom(g, a, v)
                pprops = props_of(ppom)
                merged = dict(pprops); merged.update(props)
                return ensure_parent(ppom, merged)
            except Exception as e:
                print(f"WARN parent {g}:{a}:{v}: {e}", file=sys.stderr)
    return props

def resolve(g, a, v, depth=0):
    key = (g, a, v)
    if key in SEEN or depth > 10:
        return
    SEEN.add(key)
    gp = gpath(g, a, v)
    try:
        pom = get_pom(g, a, v)
    except Exception as e:
        print(f"WARN no pom {g}:{a}:{v}: {e}", file=sys.stderr)
        return
    props = ensure_parent(pom, props_of(pom))
    jar_dest = f"{REPO}/{gp}/{a}-{v}.jar"
    if not os.path.exists(jar_dest) or os.path.getsize(jar_dest) == 0:
        try:
            fetch(f"{BASE}/{gp}/{a}-{v}.jar", jar_dest)
        except Exception as e:
            print(f"WARN no jar {g}:{a}:{v}: {e}", file=sys.stderr)
    for dg, da, dv, typ in parse_deps(pom, props):
        if typ == "pom":
            try:
                get_pom(dg, da, dv)
            except Exception as e:
                print(f"WARN bom {dg}:{da}:{dv}: {e}", file=sys.stderr)
            continue
        resolve(dg, da, dv, depth + 1)

for g, a, v in PLUGINS + DEPS:
    resolve(g, a, v)
print(f"DONE: {len(SEEN)} modules", flush=True)
