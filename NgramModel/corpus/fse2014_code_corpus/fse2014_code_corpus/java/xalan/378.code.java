package org.apache.xalan.xsltc.compiler;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUSH;
import org.apache.xalan.xsltc.compiler.util.ClassGenerator;
import org.apache.xalan.xsltc.compiler.util.MethodGenerator;
import org.apache.xalan.xsltc.compiler.util.Util;
final class Text extends Instruction {
    private String _text;
    private boolean _escaping = true;
    private boolean _ignore = false;
    private boolean _textElement = false;
    public Text() {
	_textElement = true;
    }
    public Text(String text) {
	_text = text;
    }
    protected String getText() {
	return _text;
    }
    protected void setText(String text) {
	if (_text == null)
	    _text = text;
	else
	    _text = _text + text;
    }
    public void display(int indent) {
	indent(indent);
	Util.println("Text");
	indent(indent + IndentIncrement);
	Util.println(_text);
    }
    public void parseContents(Parser parser) {
        final String str = getAttribute("disable-output-escaping");
	if ((str != null) && (str.equals("yes"))) _escaping = false;
	parseChildren(parser);
	if (_text == null) {
	    if (_textElement) {
		_text = EMPTYSTRING;
	    }
	    else {
		_ignore = true;
	    }
	}
	else if (_textElement) {
	    if (_text.length() == 0) _ignore = true;
	}
	else if (getParent() instanceof LiteralElement) {
	    LiteralElement element = (LiteralElement)getParent();
	    String space = element.getAttribute("xml:space");
	    if ((space == null) || (!space.equals("preserve")))
        {
            int i;
            final int textLength = _text.length();
            for (i = 0; i < textLength; i++) {
                char c = _text.charAt(i);
                if (!isWhitespace(c))
                    break;
            }
            if (i == textLength)
                _ignore = true;
        }
	}
	else {
        int i;
        final int textLength = _text.length();
        for (i = 0; i < textLength; i++) 
        {
            char c = _text.charAt(i);
            if (!isWhitespace(c))
                break;
        }
        if (i == textLength)
            _ignore = true;
	}
    }
    public void ignore() {
	_ignore = true;
    }
    public boolean isIgnore() {
    	return _ignore;
    }
    public boolean isTextElement() {
	return _textElement;
    }
    protected boolean contextDependent() {
	return false;
    }
    private static boolean isWhitespace(char c)
    {
    	return (c == 0x20 || c == 0x09 || c == 0x0A || c == 0x0D);
    }
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();
	if (!_ignore) {
	    final int esc = cpg.addInterfaceMethodref(OUTPUT_HANDLER,
						      "setEscaping", "(Z)Z");
	    if (!_escaping) {
		il.append(methodGen.loadHandler());
		il.append(new PUSH(cpg, false));
		il.append(new INVOKEINTERFACE(esc, 2));
	    }
            il.append(methodGen.loadHandler());
            if (!canLoadAsArrayOffsetLength()) {
                final int characters = cpg.addInterfaceMethodref(OUTPUT_HANDLER,
                                                           "characters",
                                                           "("+STRING_SIG+")V");
                il.append(new PUSH(cpg, _text));
                il.append(new INVOKEINTERFACE(characters, 2));
            } else {
                final int characters = cpg.addInterfaceMethodref(OUTPUT_HANDLER,
                                                                 "characters",
                                                                 "([CII)V");
                loadAsArrayOffsetLength(classGen, methodGen);
	        il.append(new INVOKEINTERFACE(characters, 4));
            }
	    if (!_escaping) {
		il.append(methodGen.loadHandler());
		il.append(SWAP);
		il.append(new INVOKEINTERFACE(esc, 2));
		il.append(POP);
	    }
	}
	translateContents(classGen, methodGen);
    }
    public boolean canLoadAsArrayOffsetLength() {
        return (_text.length() <= 21845);
    }
    public void loadAsArrayOffsetLength(ClassGenerator classGen,
                                        MethodGenerator methodGen) {
        final ConstantPoolGen cpg = classGen.getConstantPool();
        final InstructionList il = methodGen.getInstructionList();
        final XSLTC xsltc = classGen.getParser().getXSLTC();
        final int offset = xsltc.addCharacterData(_text);
        final int length = _text.length();
        String charDataFieldName =
            STATIC_CHAR_DATA_FIELD + (xsltc.getCharacterDataCount()-1);
        il.append(new GETSTATIC(cpg.addFieldref(xsltc.getClassName(),
                                       charDataFieldName,
                                       STATIC_CHAR_DATA_FIELD_SIG)));
        il.append(new PUSH(cpg, offset));
        il.append(new PUSH(cpg, _text.length()));
    }
}
