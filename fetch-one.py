#!/usr/bin/env python3
"""Fetch a single artifact (pom + jar + parent poms) into /root/.m2/repository.
Usage: fetch-one.py group:artifact:version [pom|jar]"""
import os, re, sys, urllib.request

BASE = "https://repo.maven.apache.org/maven2"
REPO = os.path.expanduser("~/workspace/m2repo")

def fetch(url, dest):
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        return
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=60) as r, open(dest, "wb") as f:
        f.write(r.read())
    print("got", url.split(BASE, 1)[1], flush=True)

def get_pom(g, a, v, seen):
    if (g, a, v) in seen:
        return
    seen.add((g, a, v))
    gp = f"{g.replace('.', '/')}/{a}/{v}"
    dest = f"{REPO}/{gp}/{a}-{v}.pom"
    try:
        fetch(f"{BASE}/{gp}/{a}-{v}.pom", dest)
    except Exception as e:
        print(f"WARN {e}", file=sys.stderr)
        return
    with open(dest) as f:
        pom = f.read()
    m = re.search(r"<parent>(.*?)</parent>", pom, re.S)
    if m:
        def tag(t):
            mm = re.search(rf"<{t}>(.*?)</{t}>", m.group(1), re.S)
            return mm.group(1).strip() if mm else None
        pg, pa, pv = tag("groupId"), tag("artifactId"), tag("version")
        if pg and pa and pv:
            get_pom(pg, pa, pv, seen)

def main():
    g, a, v = sys.argv[1].split(":")
    typ = sys.argv[2] if len(sys.argv) > 2 else "jar"
    seen = set()
    get_pom(g, a, v, seen)
    if typ == "jar":
        gp = f"{g.replace('.', '/')}/{a}/{v}"
        dest = f"{REPO}/{gp}/{a}-{v}.jar"
        try:
            fetch(f"{BASE}/{gp}/{a}-{v}.jar", dest)
        except Exception as e:
            print(f"WARN {e}", file=sys.stderr)

if __name__ == "__main__":
    main()
