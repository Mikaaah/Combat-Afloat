#!/usr/bin/env python3
"""Download unmodified upstream dependencies; none are bundled in Combat Afloat."""
import argparse
import hashlib
from pathlib import Path
import urllib.request
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
DEPENDENCIES = [
    ("libs/epic-fight-21.17.3.1-mc1.21.1-neoforge.jar", "https://mediafilez.forgecdn.net/files/8175/609/epic-fight-21.17.3.1-mc1.21.1-neoforge.jar", "8b882554cf10086398340fbdc741819ee72a801a3adce516c7f4768326a39526"),
    ("libs/sable-neoforge-1.21.1-2.0.5.jar", "https://mediafilez.forgecdn.net/files/8673/825/sable-neoforge-1.21.1-2.0.5.jar", "c8710f85bc780bbf523e6726ceaaf76dd0b8c61479db497b382c6bb34317e7ab"),
]
EXTRAS = [
    ("test-libs/create-1.21.1-6.0.10.jar", "https://mediafilez.forgecdn.net/files/7963/363/create-1.21.1-6.0.10.jar", "ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a"),
    ("test-libs/aeronautics-1.3.2.jar", "https://mediafilez.forgecdn.net/files/8763/471/create-aeronautics-bundled-1.21.1-1.3.2.jar", "a3f330d4757640f8715d991a43eedb826e4d0e51558d859c843ae6693bd35f1e"),
]
SODIUM = ("test-libs/sodium-neoforge-0.8.13+mc1.21.1.jar", "https://mediafilez.forgecdn.net/files/8756/580/sodium-neoforge-0.8.13%2Bmc1.21.1.jar", "575d187b15328d7bddd8a9b1bee64333c22c14735f5780ec49eac0cd5503c08d")

def fetch(name, url, expected):
    path = ROOT / name
    path.parent.mkdir(parents=True, exist_ok=True)
    if not path.exists() or hashlib.sha256(path.read_bytes()).hexdigest() != expected:
        print("Downloading", path.name)
        request = urllib.request.Request(url, headers={"User-Agent": "CombatAfloat-DependencyBootstrap/0.1"})
        with urllib.request.urlopen(request, timeout=120) as response:
            data = response.read()
        if hashlib.sha256(data).hexdigest() != expected:
            raise RuntimeError("Checksum mismatch: " + name)
        path.write_bytes(data)
    print("Verified", path.name)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--with-aeronautics", action="store_true")
    parser.add_argument("--with-sodium", action="store_true")
    args = parser.parse_args()
    for entry in DEPENDENCIES:
        fetch(*entry)
    with ZipFile(ROOT / "libs/sable-neoforge-1.21.1-2.0.5.jar") as jar:
        for name in ["sable-companion-common-1.21.1-1.6.0.jar", "veil-neoforge-1.21.1-4.3.2.jar"]:
            (ROOT / "libs" / name).write_bytes(jar.read("META-INF/jarjar/" + name))
    if args.with_aeronautics:
        for entry in EXTRAS:
            fetch(*entry)
    if args.with_sodium:
        fetch(*SODIUM)

if __name__ == "__main__":
    main()
