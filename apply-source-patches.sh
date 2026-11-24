#!/bin/bash
#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-FileCopyrightText: 2026 marcmyworld <moviesminister@gmail.com>
# SPDX-License-Identifier: Apache-2.0
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PATCHES_DIR="${SCRIPT_DIR}/patches-source"

MODE="apply"
QUIET=0

while [ $# -gt 0 ]; do
    case "$1" in
        -c|--check|--status)
            MODE="check"
            ;;
        -a|--apply)
            MODE="apply"
            ;;
        -q|--quiet)
            QUIET=1
            ;;
        -h|--help)
            echo "Usage: $(basename "$0") [--apply | --check] [--quiet]"
            exit 0
            ;;
        *)
            echo "[!] apply-source-patches.sh: Unknown option '$1'" >&2
            exit 2
            ;;
    esac
    shift
done

# Locate Android build top
if [ -z "${ANDROID_BUILD_TOP:-}" ]; then
    curr_dir="$SCRIPT_DIR"
    while [ "$curr_dir" != "/" ]; do
        if [ -f "$curr_dir/build/make/envsetup.sh" ]; then
            ANDROID_BUILD_TOP="$curr_dir"
            break
        fi
        curr_dir="$(dirname "$curr_dir")"
    done
fi

if [ -z "${ANDROID_BUILD_TOP:-}" ] || [ ! -d "$ANDROID_BUILD_TOP" ]; then
    echo "[!] WARNING: apply-source-patches.sh: Could not find ANDROID_BUILD_TOP" >&2
    exit 1
fi

if [ ! -d "$PATCHES_DIR" ]; then
    echo "[!] WARNING: apply-source-patches.sh: Patches directory missing at $PATCHES_DIR" >&2
    exit 1
fi

extract_patch_subject() {
    # Unfold RFC2822 multi-line Subject headers and strip [PATCH ...] prefix
    awk '
        /^Subject: / {
            sub(/^Subject: /, "")
            subj = $0
            in_subj = 1
            next
        }
        in_subj && /^[ \t]+/ {
            sub(/^[ \t]+/, " ")
            subj = subj $0
            next
        }
        in_subj { exit }
        END {
            sub(/^\[PATCH[^]]*\][ \t]*/, "", subj)
            print subj
        }
    ' "$1"
}

is_patch_applied() {
    local target_repo="$1"
    local patch="$2"
    local patch_subject

    patch_subject="$(extract_patch_subject "$patch")"

    # Check 1: Is commit already in git log?
    if [ -n "$patch_subject" ] && git -C "$target_repo" log -n 50 --format=%s 2>/dev/null | grep -Fqx "$patch_subject"; then
        return 0
    fi

    # Check 2: Are code changes already present in working tree or HEAD?
    if git -C "$target_repo" apply --check -R "$patch" 2>/dev/null; then
        return 0
    fi

    return 1
}

[ "$QUIET" -eq 0 ] && echo ">> Checking camera source patches for pudding (mode: ${MODE})..."

applied_count=0
unapplied_count=0
failed_count=0
total_count=0

while IFS= read -r patch; do
    [ -z "$patch" ] && continue
    total_count=$((total_count + 1))

    rel_path="${patch#$PATCHES_DIR/}"
    repo_rel="$(dirname "$rel_path")"
    target_repo="$ANDROID_BUILD_TOP/$repo_rel"
    patch_name="$(basename "$patch")"

    if [ ! -d "$target_repo/.git" ] && [ ! -f "$target_repo/.git" ]; then
        echo "    [!] WARNING: $repo_rel: Target repo does not exist or is not a git repo ($patch_name)" >&2
        failed_count=$((failed_count + 1))
        continue
    fi

    if is_patch_applied "$target_repo" "$patch"; then
        [ "$QUIET" -eq 0 ] && echo "    [OK] $repo_rel: Already applied ($patch_name)"
        applied_count=$((applied_count + 1))
        continue
    fi

    if [ "$MODE" = "check" ]; then
        echo "    [!] WARNING: $repo_rel: Source patch NOT applied ($patch_name)" >&2
        unapplied_count=$((unapplied_count + 1))
        continue
    fi

    # Check if patch can apply cleanly
    if git -C "$target_repo" apply --check "$patch" 2>/dev/null; then
        # Try git am -3 first to preserve author & commit info
        if git -C "$target_repo" am -3 "$patch" >/dev/null 2>&1; then
            [ "$QUIET" -eq 0 ] && echo "    [+] $repo_rel: Applied and committed $patch_name"
            applied_count=$((applied_count + 1))
        else
            # Abort any failed am state before falling back to git apply
            git -C "$target_repo" am --abort >/dev/null 2>&1 || true
            if git -C "$target_repo" apply "$patch" 2>/dev/null; then
                [ "$QUIET" -eq 0 ] && echo "    [+] $repo_rel: Applied changes to working tree for $patch_name"
                applied_count=$((applied_count + 1))
            else
                echo "    [!] WARNING: $repo_rel: Failed to apply $patch_name" >&2
                failed_count=$((failed_count + 1))
            fi
        fi
    else
        echo "    [!] WARNING: $repo_rel: Patch $patch_name does not apply cleanly (conflicts or upstream changed)" >&2
        failed_count=$((failed_count + 1))
    fi
done < <(find "$PATCHES_DIR" -type f -name "*.patch" | sort)

if [ "$total_count" -eq 0 ]; then
    echo "    [!] WARNING: No .patch files found in $PATCHES_DIR" >&2
    exit 1
fi

if [ "$MODE" = "check" ]; then
    if [ "$unapplied_count" -gt 0 ] || [ "$failed_count" -gt 0 ]; then
        echo ">> WARNING: Camera source patch check failed ($applied_count/$total_count applied, $unapplied_count unapplied, $failed_count error/missing repo)." >&2
        echo ">> Run: bash device/xiaomi/miuicamera-pudding/apply-source-patches.sh" >&2
        exit 1
    fi
    [ "$QUIET" -eq 0 ] && echo ">> Camera source patches OK ($applied_count/$total_count applied)."
    exit 0
fi

if [ "$failed_count" -gt 0 ]; then
    echo ">> WARNING: Camera source patching finished with errors ($applied_count/$total_count applied, $failed_count failed)." >&2
    exit 1
fi

[ "$QUIET" -eq 0 ] && echo ">> Camera source patches check completed ($applied_count/$total_count applied)."
exit 0
