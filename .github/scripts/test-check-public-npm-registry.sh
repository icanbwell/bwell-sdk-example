#!/usr/bin/env bash
# Runs check-public-npm-registry.sh against fixture repos and asserts on its
# exit code and output. Fixtures are generated here rather than committed,
# because committed fixture lockfiles would fail the real check.
#
# Run locally from the repo root: .github/scripts/test-check-public-npm-registry.sh
set -euo pipefail

CHECK="$(cd "$(dirname "$0")" && pwd)/check-public-npm-registry.sh"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

NPMJS='https://registry.npmjs.org/left-pad/-/left-pad-1.3.0.tgz'
CLEAN_PACKAGE_JSON='{
  "name": "app",
  "homepage": "https://example.com/app",
  "dependencies": {
    "left-pad": "^1.3.0",
    "aliased": "npm:left-pad@1.3.0"
  },
  "overrides": {
    "left-pad": {
      ".": "1.3.0"
    }
  }
}'

lockfile_resolving() { # resolved URL
  printf '{\n  "packages": {\n    "node_modules/left-pad": {\n      "version": "1.3.0",\n      "resolved": "%s"\n    }\n  }\n}\n' "$1"
}

package_json_depending_on() { # dependency spec
  printf '{\n  "name": "app",\n  "dependencies": {\n    "pkg": "%s"\n  }\n}\n' "$1"
}

# Fixture builders: each writes files into the current directory.
fixture_clean() {
  echo "$CLEAN_PACKAGE_JSON" > app/package.json
  lockfile_resolving "$NPMJS" > app/package-lock.json
}
fixture_mirror_url() {
  fixture_clean
  lockfile_resolving 'https://mirror.example.com/api/npm/npm/left-pad/-/left-pad-1.3.0.tgz' > app/package-lock.json
}
fixture_git_ssh() {
  fixture_clean
  lockfile_resolving 'git+ssh://git@github.com/example/private-pkg.git#abc123' > app/package-lock.json
}
fixture_file_resolved() {
  fixture_clean
  lockfile_resolving 'file:../vendor/pkg-1.0.0.tgz' > app/package-lock.json
}
fixture_shrinkwrap_mirror_url() {
  fixture_clean
  lockfile_resolving 'https://mirror.example.com/left-pad-1.3.0.tgz' > app/npm-shrinkwrap.json
}
fixture_yalc() {
  fixture_clean
  package_json_depending_on 'file:.yalc/@example/sdk' > app/package.json
}
fixture_file_dependency() {
  fixture_clean
  package_json_depending_on 'file:../local-pkg' > app/package.json
}
fixture_link_dependency() {
  fixture_clean
  package_json_depending_on 'link:../local-pkg' > app/package.json
}
fixture_github_dependency() {
  fixture_clean
  package_json_depending_on 'github:example/private-pkg' > app/package.json
}
fixture_github_shorthand_dependency() {
  fixture_clean
  package_json_depending_on 'example/private-pkg' > app/package.json
}
fixture_zero_lockfiles() {
  echo "$CLEAN_PACKAGE_JSON" > app/package.json
}
fixture_tracked_npmrc() {
  fixture_clean
  echo '//registry.example.com/:_authToken=not-a-real-token' > app/.npmrc
  git add -f app/.npmrc
}

# name | expected exit code | expected output substring
CASES=(
  "clean|0|OK: 1 lockfile(s)"
  "mirror_url|1|app/package-lock.json:5: Package not resolved"
  "git_ssh|1|app/package-lock.json:5: Package not resolved"
  "file_resolved|1|app/package-lock.json:5: Package not resolved"
  "shrinkwrap_mirror_url|1|app/npm-shrinkwrap.json:5: Package not resolved"
  "yalc|1|app/package.json:4: Local yalc build referenced"
  "file_dependency|1|app/package.json:4: Dependency doesn't install from the npm registry"
  "link_dependency|1|app/package.json:4: Dependency doesn't install from the npm registry"
  "github_dependency|1|app/package.json:4: Dependency doesn't install from the npm registry"
  "github_shorthand_dependency|1|app/package.json:4: Dependency doesn't install from the npm registry"
  "zero_lockfiles|1|No tracked package-lock.json or npm-shrinkwrap.json found"
  "tracked_npmrc|1|app/.npmrc:1: Committed .npmrc"
)

failures=0
for case in "${CASES[@]}"; do
  IFS='|' read -r name want_status want_output <<< "$case"
  repo="${WORK}/${name}"
  mkdir -p "${repo}/app"
  (
    cd "$repo"
    git init -q
    "fixture_${name}"
    git add -A
  )

  status=0
  # Unset GITHUB_ACTIONS so fixture failures don't become PR annotations.
  output="$(cd "$repo" && env -u GITHUB_ACTIONS bash "$CHECK" 2>&1)" || status=$?

  if [[ "$status" == "$want_status" && "$output" == *"$want_output"* ]]; then
    echo "ok   ${name}"
  else
    failures=$((failures + 1))
    echo "FAIL ${name}: want exit ${want_status} with \"${want_output}\", got exit ${status}:"
    sed 's/^/       /' <<< "$output"
  fi
done

if ((failures)); then
  echo "${failures} of ${#CASES[@]} case(s) failed." >&2
  exit 1
fi
echo "All ${#CASES[@]} cases passed."
