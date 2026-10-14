"""Compile/test current source using dependencies in an existing fxMR fat JAR.

No original optimizer is substituted. Maven remains the normal clean build.
"""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument("dependencies", type=Path)
parser.add_argument("--output", type=Path)
args = parser.parse_args()
here = Path(__file__).resolve().parent
root = here.parent
source = here / "java source"
dependencies = args.dependencies.resolve()
output = args.output or root / "STACMR-R/java/fxMR-0.3.45-jar-with-dependencies.jar"
env = os.environ.copy()
env.pop("_JAVA_OPTIONS", None)
with tempfile.TemporaryDirectory(prefix="sta-correctness-build-") as tmp:
    classes = Path(tmp)
    main = sorted((source / "src/main/java").rglob("*.java"))
    tests = sorted((source / "src/test/java").rglob("*.java"))
    subprocess.run(["javac", "--release", "8", "-cp", str(dependencies), "-d", str(classes),
                    *map(str, main)], env=env, check=True)
    cp = os.pathsep.join((str(classes), str(dependencies)))
    subprocess.run(["javac", "--release", "8", "-cp", cp, "-d", str(classes),
                    *map(str, tests)], env=env, check=True)
    names = ["InitialOrderTests", "MRTests", "StatsTests", "CMRxTests", "BinSingleTests", "BinCSingleTests", "BinTests"]
    subprocess.run(["java", "-Djava.awt.headless=true", "-cp", cp, "org.junit.runner.JUnitCore",
                    *["au.edu.adelaide.fxmr.model." + n for n in names],
                    "au.edu.adelaide.fxmr.om.OMUtilTests"], cwd=source, env=env, check=True)
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(dependencies) as old, zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as new:
        for name in old.namelist():
            if name.endswith(".class") and name.startswith("au/edu/adelaide/"):
                owner = name.split("$")[0].removesuffix(".class") + ".java"
                if (source / "src/main/java" / owner).exists():
                    continue
            if name.startswith("META-INF/maven/fxMR/fxMR/"):
                continue
            data = old.read(name)
            if name == "META-INF/MANIFEST.MF":
                data = data.replace(b"0.3.44", b"0.3.45")
            new.writestr(name, data)
        written = set()
        for path in main:
            relative = path.relative_to(source / "src/main/java")
            base = classes / relative.parent
            for compiled in [base / (path.stem + ".class"), *base.glob(path.stem + "$*.class")]:
                name = str(compiled.relative_to(classes))
                if compiled.exists() and name not in written:
                    new.write(compiled, name)
                    written.add(name)
        new.writestr("META-INF/maven/fxMR/fxMR/pom.xml", (source / "pom.xml").read_text())
        new.writestr("META-INF/maven/fxMR/fxMR/pom.properties", "groupId=fxMR\nartifactId=fxMR\nversion=0.3.45\n")
print("Built and tested", output)
