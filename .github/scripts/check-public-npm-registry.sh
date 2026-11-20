#!/usr/bin/env bash
# This repo is public. Clients must be able to `npm install` the examples
# without b.well credentials, so committed npm files may only resolve packages
# from the public npmjs registry and must not reference local yalc builds,
# local paths or git repos. Registry config (.npmrc) must not be committed.
#
# b.well developers who install through the JFrog mirror (.npmrc) still work:
# npm swaps the registry.npmjs.org host for the configured registry at install
# time. But installing or bumping a package with that .npmrc writes mirror URLs
# into package-lock.json, which this check catches.
#
# Run locally from the repo root: .github/scripts/check-public-npm-registry.sh
# Tests: .github/scripts/test-check-public-npm-registry.sh
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

PUBLIC_REGISTRY='https://registry.npmjs.org/'
README_REF='see root README section "Contributing: keep lockfiles on public npmjs"'
YALC_MSG="Local yalc build referenced. Run 'yalc remove --all && npm install' before committing."
LOCKFILES=(':(glob)**/package-lock.json' ':(glob)**/npm-shrinkwrap.json')
failed=0

report() { # file:line:content, message
  local file="${1%%:*}" rest="${1#*:}"
  local line="${rest%%:*}"
  failed=1
  if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
    echo "::error file=${file},line=${line}::$2"
  else
    echo "${file}:${line}: $2"
  fi
}

# The yalc message is more actionable than the generic one, so prefer it.
message_for() { # hit, default message
  if [[ "$1" == *.yalc* ]]; then echo "$YALC_MSG"; else echo "$2"; fi
}

# Prints file:line:content for matches in tracked files; no output if none.
grep_tracked() { # pattern, pathspec...
  local pattern="$1"; shift
  git ls-files -z -- "$@" | xargs -0 grep -nHE "$pattern" /dev/null || true
}

# Prints file:line:content for each dependency spec in tracked package.json
# files that isn't a registry version range or npm: alias, so file:, link:,
# git+ssh:, github:, owner/repo shorthand and tarball URLs. Only looks inside
# dependency sections, so fields like "homepage" URLs aren't flagged.
non_registry_dependency_specs() {
  git ls-files -z -- ':(glob)**/package.json' | xargs -0 awk '
    FNR == 1 { depth = 0 }
    depth == 0 && /^[[:space:]]*"(dependencies|devDependencies|optionalDependencies|peerDependencies|overrides|resolutions)"[[:space:]]*:[[:space:]]*\{/ {
      depth = gsub(/\{/, "{") - gsub(/\}/, "}")
      next
    }
    depth > 0 {
      n = split($0, part, "\"")
      if (n >= 5 && part[3] ~ /^[[:space:]]*:[[:space:]]*$/) {
        spec = part[4]
        if (spec !~ /^npm:/ && spec ~ /[:\/]/) print FILENAME ":" FNR ":" $0
      }
      depth += gsub(/\{/, "{") - gsub(/\}/, "}")
    }
  ' /dev/null
}

lockfile_count=$(git ls-files -- "${LOCKFILES[@]}" | wc -l | tr -d ' ')
if ((lockfile_count == 0)); then
  failed=1
  echo "No tracked package-lock.json or npm-shrinkwrap.json found. If a lockfile moved, check that its new path matches this script's patterns." >&2
fi

# Flag every "resolved" not on public npmjs, whatever the scheme: https
# mirrors, git+ssh/git+https/github: repos and file: paths all fail for clients.
while IFS= read -r hit; do
  [[ -n "$hit" ]] || continue
  report "$hit" "$(message_for "$hit" "Package not resolved from ${PUBLIC_REGISTRY} (private registry, git repo or local file). Clients can't install it (${README_REF}).")"
done <<< "$(grep_tracked '"resolved": "' "${LOCKFILES[@]}" | grep -vF "\"resolved\": \"${PUBLIC_REGISTRY}" || true)"

while IFS= read -r hit; do
  [[ -n "$hit" ]] || continue
  report "$hit" "$(message_for "$hit" "Dependency doesn't install from the npm registry (local path, link, git repo or URL). Clients can't install it (${README_REF}).")"
done <<< "$(non_registry_dependency_specs)"

while IFS= read -r npmrc; do
  [[ -n "$npmrc" ]] || continue
  report "${npmrc}:1:" "Committed .npmrc. It may hold registry credentials; remove it with 'git rm --cached ${npmrc}' and rotate any token it contained."
done <<< "$(git ls-files -- ':(glob)**/.npmrc')"

if ((failed)); then
  echo "Committed npm files must install from ${PUBLIC_REGISTRY} without credentials." >&2
  exit 1
fi
echo "OK: ${lockfile_count} lockfile(s) resolve only from ${PUBLIC_REGISTRY}"
