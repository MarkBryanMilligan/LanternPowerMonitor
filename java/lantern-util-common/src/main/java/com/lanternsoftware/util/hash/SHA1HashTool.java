package com.lanternsoftware.util.hash;

public class SHA1HashTool extends AbstractHashTool {
    /**
     * Creates an SHA-1 hash tool with no static salt that performs only one hash iteration
     */
    public SHA1HashTool() {
        this(null, 1);
    }

    /**
     * Creates an SHA-1 hash tool with the specified static salt that performs only one hash iteration
     * @param _salt the salt to attach each time a hash is performed with this tool
     */
    public SHA1HashTool(String _salt) {
        this(_salt, 1);
    }

    /**
     * Creates an SHA-1 hash tool with the specified static salt that performs the specified number of iterations each time a hash is performed
     * @param _salt the salt to attach each time a hash is performed with this tool
     * @param _iterations the number of times to hash values
     */
    public SHA1HashTool(String _salt, int _iterations) {
        super("SHA-1", _salt, true, _iterations);
    }
}
