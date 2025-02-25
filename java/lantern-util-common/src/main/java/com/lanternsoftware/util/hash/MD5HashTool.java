package com.lanternsoftware.util.hash;

public class MD5HashTool extends AbstractHashTool {
	/**
	 * Creates an MD5 hash tool with no static salt that performs only one hash iteration
	 */
	public MD5HashTool() {
        this(null, 1);
    }

	/**
	 * Creates an MD5 hash tool with the specified static salt that performs only one hash iteration
	 * @param _salt the salt to attach each time a hash is performed with this tool
	 */
	public MD5HashTool(String _salt) {
        this(_salt, 1);
    }

	/**
	 * Creates an MD5 hash tool with the specified static salt that performs the specified number of iterations each time a hash is performed
	 * @param _salt the salt to attach each time a hash is performed with this tool
	 * @param _iterations the number of times to hash values
	 */
	public MD5HashTool(String _salt, int _iterations) {
        super("MD5", _salt, false, _iterations);
    }
}
