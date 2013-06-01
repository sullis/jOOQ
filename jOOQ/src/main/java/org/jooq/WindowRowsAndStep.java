/**
 * Copyright (c) 2009-2013, Lukas Eder, lukas.eder@gmail.com
 * All rights reserved.
 *
 * This software is licensed to you under the Apache License, Version 2.0
 * (the "License"); You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * . Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * . Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * . Neither the name "jOOQ" nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jooq;

import static org.jooq.SQLDialect.DB2;
import static org.jooq.SQLDialect.ORACLE;
import static org.jooq.SQLDialect.POSTGRES;
import static org.jooq.SQLDialect.SQLSERVER2012;
import static org.jooq.SQLDialect.SYBASE;

import org.jooq.api.annotation.State;
import org.jooq.api.annotation.Transition;

/**
 * This type is used for the window function DSL API.
 * <p>
 * Example: <code><pre>
 * field.firstValue()
 *      .ignoreNulls()
 *      .over()
 *      .partitionBy(AUTHOR_ID)
 *      .orderBy(PUBLISHED_IN.asc())
 *      .rowsBetweenUnboundedPreceding()
 *      .andUnboundedFollowing()
 * </pre></code>
 *
 * @param <T> The function return type
 * @author Lukas Eder
 */
@State
public interface WindowRowsAndStep<T> {

    /**
     * Add a <code>... AND UNBOUNDED PRECEDING</code> frame clause to the window
     * function.
     */
    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER2012, SYBASE })
    @Transition(
        name = "AND UNBOUNDED PRECEDING"
    )
    WindowFinalStep<T> andUnboundedPreceding();

    /**
     * Add a <code>... AND [number] PRECEDING</code> frame clause to the window
     * function.
     */
    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER2012, SYBASE })
    @Transition(
        name = "AND PRECEDING",
        args = "Integer"
    )
    WindowFinalStep<T> andPreceding(int number);

    /**
     * Add a <code>... AND CURRENT ROW</code> frame clause to the window
     * function.
     */
    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER2012, SYBASE })
    @Transition(
        name = "AND CURRENT ROW"
    )
    WindowFinalStep<T> andCurrentRow();

    /**
     * Add a <code>... AND UNBOUNDED FOLLOWING</code> frame clause to the window
     * function.
     */
    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER2012, SYBASE })
    @Transition(
        name = "AND UNBOUNDED FOLLOWING"
    )
    WindowFinalStep<T> andUnboundedFollowing();

    /**
     * Add a <code>... AND [number] FOLLOWING</code> frame clause to the window
     * function.
     */
    @Support({ DB2, POSTGRES, ORACLE, SQLSERVER2012, SYBASE })
    @Transition(
        name = "AND FOLLOWING",
        args = "Integer"
    )
    WindowFinalStep<T> andFollowing(int number);
}
