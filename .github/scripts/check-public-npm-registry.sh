#!/usr/bin/env bash
# This repo is public. Clients must be able to `npm install` the examples
# without b.well credentials, so committed npm files may only resolve packages
# from the public npmjs registry and must not reference local yalc builds.
#
# b.well developers who install through the JFrog mirror (.npmrc) still work:
# npm swaps the registry.npmjs.org host for the configured registry at install
# time. But installing or bumping a package with that .npmrc writes mirror URLs
# into package-lock.json, which this check catches.
#
# Run locally from the repo root: .github/scripts/check-public-npm-registry.sh
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

PUBLIC_REGISTRY='https://registry.npmjs.org/'
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

# Prints file:line:content for matches in tracked files; no output if none.
grep_tracked() { # pattern, pathspec...
  local pattern="$1"; shift
  git ls-files -z -- "$@" | xargs -0 grep -nHE "$pattern" /dev/null || true
}

lockfile_count=$(git ls-files -- ':(glob)**/package-lock.json' | wc -l | tr -d ' ')

while IFS= read -r hit; do
  [[ -n "$hit" ]] || continue
  report "$hit" "Package resolved from a non-public registry. Rewrite \"resolved\" to ${PUBLIC_REGISTRY} (see root README section \"Contributing: keep lockfiles on public npmjs\")."
done <<< "$(grep_tracked '"resolved": "https?://' ':(glob)**/package-lock.json' | grep -vF "\"resolved\": \"${PUBLIC_REGISTRY}" || true)"

while IFS= read -r hit; do
  [[ -n "$hit" ]] || continue
  report "$hit" "Local yalc build referenced. Run 'yalc remove --all && npm install' before committing."
done <<< "$(grep_tracked 'file:\.yalc|/\.yalc/' ':(glob)**/package.json' ':(glob)**/package-lock.json')"

if ((failed)); then
  echo "Committed npm files must install from ${PUBLIC_REGISTRY} without credentials." >&2
  exit 1
fi
echo "OK: ${lockfile_count} lockfile(s) resolve only from ${PUBLIC_REGISTRY}"
