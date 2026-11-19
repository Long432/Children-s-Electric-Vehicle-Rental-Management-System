#!/usr/bin/env bash
set -euo pipefail

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
target="$dir/application-local.properties"
gitignore="$dir/.gitignore"

touch "$gitignore"
if ! grep -qF "application-local.properties" "$gitignore"; then
    printf '\napplication-local.properties\n' >> "$gitignore"
fi

if [ -f "$target" ]; then
    echo "application-local.properties da ton tai, giu nguyen."
    exit 0
fi

read -r -s -p "Nhap mat khau PostgreSQL: " password
echo
if [ -z "$password" ]; then
    echo "Mat khau khong duoc de trong." >&2
    exit 1
fi

if command -v openssl >/dev/null 2>&1; then
    secret="$(openssl rand -hex 48)"
else
    secret="$(head -c 48 /dev/urandom | od -An -tx1 | tr -d ' \n')"
fi

escaped="${password//\\/\\\\}"

umask 077
printf 'spring.datasource.password=%s\njwt.secret=%s\n' "$escaped" "$secret" > "$target"

echo "Da tao application-local.properties"
