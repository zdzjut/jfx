/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.sun.glass.ui.monocle.x11;

import com.sun.glass.ui.monocle.AcceleratedScreen;
import com.sun.glass.ui.monocle.GLException;
import com.sun.glass.ui.monocle.NativePlatformFactory;
import com.sun.glass.ui.monocle.linux.LinuxSystem;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class X11AcceleratedScreen extends AcceleratedScreen {
    private X.XDisplay nativeDisplay;

    public X11AcceleratedScreen(int[] attributes) throws GLException {
        super(attributes);
    }

    @Override
    protected long platformGetNativeDisplay() {
        if (nativeDisplay == null) {
            boolean doMaliWorkaround =
                    AccessController.doPrivileged(
                            (PrivilegedAction<Boolean>) () ->
                                    Boolean.getBoolean(
                                            "monocle.maliSignedStruct"));
            X.XDisplay display = new X.XDisplay(X.XOpenDisplay(null));
            if (doMaliWorkaround) {
                long address = 0x7000000;
                nativeDisplay = new X.XDisplay(
                        ls.mmap(address, display.sizeof(),
                                LinuxSystem.PROT_READ | LinuxSystem.PROT_WRITE,
                                LinuxSystem.MAP_PRIVATE
                                        | LinuxSystem.MAP_ANONYMOUS,
                                -1, 0)
                );
                ls.memcpy(nativeDisplay.p, display.p, display.sizeof());
            } else {
                nativeDisplay = display;
            }
        }
        return nativeDisplay.p;
    }

    @Override
    protected long platformGetNativeWindow() {
        return NativePlatformFactory.getNativePlatform()
                .getScreen().getNativeHandle();
    }

}