/*
 * Copyright 2017 Code Above Lab LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.scut.cs.hm.common.validate;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 */
public class ValidationUtils {

    /**
     * Throws exception when specified object is invalid.
     * @param validator validator
     * @param obj validating object
     * @param id string which can identify object
     * @param msg message whic pass to exception, mai be null
     * @param <T> type of object
     * @throws ValidityException
     */
    public static <T> void assertValid(Validator validator, T obj, String id, String msg) throws ValidityException {
        Set<ConstraintViolation<T>> res = validator.validate(obj);
        Validity validity = new JsrValidityImpl(id, res);
        if(!validity.isValid()) {
            if(msg == null) {
                msg = "Invalid: " + id;
            }
            throw new ValidityException(msg, validity);
        }
    }
}
