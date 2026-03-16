#!/usr/bin/env bash
set -euo pipefail

# REQUIREMENTS:
# - Install GitHub CLI: https://cli.github.com/
# - Authenticate: gh auth login
# - Run from repo root: bash project-ops/setup_github_project.sh "E‑Shop Lite Board"

BOARD_NAME=${1:-"E‑Shop Lite Board"}

# Create labels
echo "==> Seeding labels"
while read -r name; do
  color=$(jq -r ".[] | select(.name==\"$name\") | .color" project-ops/labels.json)
  desc=$(jq -r ".[] | select(.name==\"$name\") | .description" project-ops/labels.json)
  if gh label list | grep -q "^$name\s"; then
    gh label edit "$name" --color "$color" --description "$desc" || true
  else
    gh label create "$name" --color "$color" --description "$desc" || true
  fi
done < <(jq -r '.[].name' project-ops/labels.json)

# Create milestones
echo "==> Seeding milestones"
jq -c '.[]' project-ops/milestones.json | while read -r row; do
  title=$(echo "$row" | jq -r .title)
  desc=$(echo "$row" | jq -r .description)
  due=$(echo "$row" | jq -r .due_on)
  if gh api repos/:owner/:repo/milestones | jq -e ".[] | select(.title==\"$title\")" >/dev/null 2>&1; then
    echo "Milestone exists: $title"
  else
    gh api repos/:owner/:repo/milestones -f title="$title" -f description="$desc" -f due_on="$due" >/dev/null
    echo "Created milestone: $title"
  fi
done

# Create a classic project board
echo "==> Creating classic project board: $BOARD_NAME"
PROJECT_ID=$(gh api repos/:owner/:repo/projects -f name="$BOARD_NAME" --jq '.[0].id' 2>/dev/null || true)
if [ -z "$PROJECT_ID" ]; then
  PROJECT_ID=$(gh api repos/:owner/:repo/projects -f name="$BOARD_NAME" --jq '.id')
  echo "Created board: $PROJECT_ID"
else
  echo "Board already exists: $PROJECT_ID"
fi

# Add standard columns
add_column() {
  local name="$1"
  if ! gh api repos/:owner/:repo/projects/$PROJECT_ID/columns --jq '.[].name' | grep -q "^$name$"; then
    gh api repos/:owner/:repo/projects/$PROJECT_ID/columns -f name="$name" >/dev/null
    echo "Added column: $name"
  fi
}

for col in "Backlog" "Ready for Development" "In Progress" "In Review" "QA Ready" "QA In Progress" "Blocked" "Done"; do
  add_column "$col"
done

echo "==> Done. Open the board: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/projects"
