# Checks upstream repositories and warns if UniMixins is out of sync with them.
#
# A GitHub API token can be provided either in the GITHUB_TOKEN environmental
# variable, or the ~/.tokens/github file.
# Using one is not necessary, but doing so will greatly increase rate limits.

import requests
import re
import os
from pathlib import Path
import sys

cachedGithubToken = None
foundUpdates = 0

if not Path("module-mixin").is_dir():
    sys.exit('''
    This script needs to be run from the root of the repository, e.g.:
        python3 scripts/check_upstream.py
''')

def compareVersions(rootName, comparisons):
    for comparison in comparisons:
        name, a, b = comparison
        compareVersion(f"{rootName} ({name})", a, b)

def compareVersion(name, a, b):
    global foundUpdates
    
    upToDate = a == b
    judgement = "Up to date" if upToDate else "Out of sync!"
    details = f"({a} << {b})" 
    if not upToDate:
        print(f"{name}: {judgement} {details}")
        foundUpdates += 1

def property(text, propName):
    props = dict([line.split("=") for line in text.splitlines() if line])
    return props[propName]

def localFile(path):
    return open(path, "r", encoding="utf8").read()

def githubFile(ownerAndRepo, branch, path):
    return requests.get(f"https://raw.githubusercontent.com/{ownerAndRepo}/{branch}/{path}").text

def stripBuildIdentifiers(ver):
    return ver.split("+")[0]

def lastReleaseTag(ownerAndRepo):
    return githubApi(f"/repos/{ownerAndRepo}/releases/latest")["tag_name"]

def lastVersionTag(ownerAndRepo):
    # some repos are missing releases, so we have to check tags. we'll have to guess which tags are versions.
    tags = [tag["ref"][len("refs/tags/"):] for tag in githubApi(f"/repos/{ownerAndRepo}/git/refs/tags")]
    return [tag for tag in tags if isProbablyAVersion(tag)][-1]

def isProbablyAVersion(tag):
    return bool(re.match("(v|V)?\d.*", tag))

def creditsSources(moduleName):
    sources = [line.split(": ")[-1] for line in localFile(moduleName + "/CREDITS").splitlines() if line.startswith("Source: ")]
    out = []
    
    for source in sources:
        pattern = re.compile("https://github.com/(.+?)/(.+?)/tree/(.+)")
        match = pattern.match(source)
        if match:
            owner, repo, commitish = match.groups()
            ownerAndRepo = f"{owner}/{repo}"
            out.append([ownerAndRepo, commitish, lastVersionTag(ownerAndRepo)])
        else:
            raise Exception("Unknown source: " + source)
    
    return out

def githubMainBranch(ownerAndRepo):
    return githubApi(f"/repos/{ownerAndRepo}")["default_branch"]

def githubHash(ownerAndRepo, commitish):
    return githubApi(f"/repos/{ownerAndRepo}/commits/{commitish}")["commit"]["tree"]["sha"]

def githubApi(path):
    headers = {}
    if githubToken():
        headers["Authorization"] = "Bearer " + githubToken()
    return requests.get(f"https://api.github.com{path}", headers=headers).json()

def githubToken():
    global cachedGithubToken
    if not cachedGithubToken:
        tokenFile = Path.home() / ".tokens" / "github"
        if tokenFile.is_file():
            cachedGithubToken = open(tokenFile, "r", encoding="utf8").read().strip()
        else:
            cachedGithubToken = os.environ.get("GITHUB_TOKEN") or ""
    return cachedGithubToken

compareVersion("Mixin (SpongePowered)",
    property(localFile("module-mixin/gradle.properties"), "spongepoweredMixinVersion"),
    property(githubFile("SpongePowered/Mixin", "master", "gradle.properties"), "buildVersion"))

compareVersion("Mixin (Fabric)",
    stripBuildIdentifiers(property(localFile("module-mixin/gradle.properties"), "fabricMixinVersion")),
    property(githubFile("FabricMC/Mixin", "main", "gradle.properties"), "buildVersion"))

compareVersion("Mixin (UniMix)",
    stripBuildIdentifiers(property(localFile("module-mixin/gradle.properties"), "unimixMixinVersion")),
    property(githubFile("LegacyModdingMC", "UniMix/main", "gradle.properties"), "buildVersion"))

compareVersions("GTNHMixins",
    creditsSources("module-gtnhmixins"))

compareVersions("MixinBooterLegacy",
    creditsSources("module-mixinbooterlegacy"))

compareVersion("MixinExtras",
    property(localFile("module-mixinextras/gradle.properties"), "mixinExtrasVersion"),
    lastVersionTag("LlamaLad7/MixinExtras"))

compareVersions("Mixingasm",
    creditsSources("module-mixingasm"))

compareVersions("SpongeMixins",
    creditsSources("module-spongemixins"))

if not foundUpdates:
    print("All modules up to date.")
else:
    print()
    print(foundUpdates, f"repositor{'y is' if foundUpdates == 1 else 'ies are'} out of sync.")
    sys.exit(1)
