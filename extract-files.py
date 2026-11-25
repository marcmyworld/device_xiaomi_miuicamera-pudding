#!/usr/bin/env -S PYTHONPATH=../../../tools/extract-utils python3
#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

import os

from extract_utils.fixups_blob import (
    blob_fixup,
    blob_fixups_user_type,
)
from extract_utils.main import (
    ExtractUtils,
    ExtractUtilsModule,
)


def _disable_git_quotepath() -> None:
    """Keep git path output unquoted so non-ASCII smali patches apply cleanly."""
    count = int(os.environ.get('GIT_CONFIG_COUNT', '0'))
    for i in range(count):
        if os.environ.get(f'GIT_CONFIG_KEY_{i}') == 'core.quotepath':
            os.environ[f'GIT_CONFIG_VALUE_{i}'] = 'false'
            return
    os.environ[f'GIT_CONFIG_KEY_{count}'] = 'core.quotepath'
    os.environ[f'GIT_CONFIG_VALUE_{count}'] = 'false'
    os.environ['GIT_CONFIG_COUNT'] = str(count + 1)


blob_fixups: blob_fixups_user_type = {
    'system/priv-app/MiuiCamera/MiuiCamera.apk': blob_fixup()
        .apktool_patch('patches'),
}

module = ExtractUtilsModule(
    'miuicamera-pudding',
    'xiaomi',
    blob_fixups=blob_fixups,
)

if __name__ == '__main__':
    _disable_git_quotepath()
    utils = ExtractUtils.device(module)
    utils.run()
