#!/bin/sh
#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-FileCopyrightText: 2026 marcmyworld <moviesminister@gmail.com>
# SPDX-License-Identifier: Apache-2.0
#

# Sourced by build/envsetup.sh (works in both bash and zsh)
_MIUICAM_SELF=""
if [ -n "${BASH_VERSION:-}" ]; then
    _MIUICAM_SELF="${BASH_SOURCE[0]:-}"
elif [ -n "${ZSH_VERSION:-}" ]; then
    # Use eval so bash does not fail parsing zsh parameter expansion flags
    eval '_MIUICAM_SELF="${(%):-%x}"'
fi

if [ -n "${_MIUICAM_SELF}" ]; then
    _MIUICAM_DIR="$(cd "$(dirname "${_MIUICAM_SELF}")" 2>/dev/null && pwd)"
elif [ -f "device/xiaomi/miuicamera-pudding/apply-source-patches.sh" ]; then
    _MIUICAM_DIR="$(pwd)/device/xiaomi/miuicamera-pudding"
elif [ -n "${T:-}" ] && [ -f "${T}/device/xiaomi/miuicamera-pudding/apply-source-patches.sh" ]; then
    _MIUICAM_DIR="${T}/device/xiaomi/miuicamera-pudding"
else
    _MIUICAM_DIR="$(pwd)"
fi

if [ -f "${_MIUICAM_DIR}/apply-source-patches.sh" ]; then
    bash "${_MIUICAM_DIR}/apply-source-patches.sh"
else
    echo "[!] WARNING: miuicamera-pudding: apply-source-patches.sh not found (resolved dir: ${_MIUICAM_DIR})" >&2
fi

unset _MIUICAM_DIR _MIUICAM_SELF
