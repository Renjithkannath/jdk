/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.jfr.internal.jfc.model;

import jdk.jfr.internal.tracing.Filter;

// Corresponds to <text>
final class XmlText extends XmlInput {

    @Override
    public String getOptionSyntax() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append("=<");
        sb.append(getContentType().orElse("text"));
        sb.append(">");
        sb.append("  (");
        String content = getContentOrEmptyQuote();
        if (isTimespan()) {
            // "20 ms" becomes "20ms"
            content = content.replaceAll("\\s", "");
        }
        sb.append(content);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void configure(String value) {
        if (isTimespan()) {
            value = Utilities.parseTimespan(value);
        }
        setContent(value);
        notifyListeners();
    }

    @Override
    public void configure(UserInterface ui) throws AbortException {
        ui.println();
        ui.println(getLabel() + ": " + getContentOrEmptyQuote() + "  (default)");
        while (!readInput(ui)) {
            ;
        }
    }

    @Override
    protected Result evaluate() {
        return Result.of(getContent());
    }

    private boolean readInput(UserInterface ui) throws AbortException {
        String line = ui.readLine();
        if (line.isBlank()) {
            ui.println("Using default: " + getContentOrEmptyQuote());
            return true;
        }
        if (isMethodFilter()) {
            if (!Filter.isValid(line)) {
                ui.println("""
                Not a valid method filter. A filter can be an annotation \
                (@jakarta.ws.rs.GET), a full qualified class name (com.example.Foo), \
                a fully qualified method reference (java.lang.HashMap::resize) or a \
                class initializer (::<clinit>). Use <init> for constructors. \
                Separate multiple filters with semicolon.\
                """);
                return false;
            }
        }
        if (isTimespan()) {
            try {
                line = Utilities.parseTimespan(line);
            } catch (IllegalArgumentException iae) {
                ui.println(iae.getMessage());
                return false;
            }
        }
        ui.println("Using: " + line);
        configure(line);
        return true;
    }

    private boolean isTimespan() {
        return getContentType().orElse("text").equals("timespan");
    }

    private boolean isMethodFilter() {
        return getContentType().orElse("text").equals("method-filter");
    }
}
