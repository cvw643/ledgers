/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.api.exception;

import de.adorsys.ledgers.um.api.domain.UserBO;

public class UserAlreadyExistsException extends Exception {

    public static final String ERROR_MESSAGE = "User with such login=%s or email=%s already exists";

    public UserAlreadyExistsException() {
    }

    public UserAlreadyExistsException(UserBO user) {
        this(String.format(ERROR_MESSAGE, user.getLogin(), user.getEmail()));
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

	public UserAlreadyExistsException(UserBO user, Throwable cause) {
		super(String.format(ERROR_MESSAGE, user.getLogin(), user.getEmail()), cause);
	}

	public UserAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
