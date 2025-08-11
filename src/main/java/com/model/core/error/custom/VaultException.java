/*
 * SPDX-License-Identifier: BSD-3-Clause
 *
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * Licensed under the BSD 3-Clause License.
 * See LICENSE file in the project root for full license information.
 */
package com.model.core.error.custom;

import com.model.core.error.ErrorCode;
import com.model.core.error.base.AppException;

public class VaultException extends AppException {
    public VaultException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public VaultException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
