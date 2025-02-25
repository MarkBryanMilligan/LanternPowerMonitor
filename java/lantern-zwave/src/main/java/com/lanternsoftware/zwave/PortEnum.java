package com.lanternsoftware.zwave;

import gnu.io.CommPortIdentifier;

import java.util.Enumeration;

public class PortEnum {
    public static void main(String[] args) {
        Enumeration<CommPortIdentifier> e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            CommPortIdentifier id = e.nextElement();
            if (id != null) {
                System.out.println(id.getName());
            }
        }
    }
}
