package org.apache.xml.serialize;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
public class Printer
{
    protected final OutputFormat _format;
    protected Writer             _writer;
    protected StringWriter       _dtdWriter;
    protected Writer          _docWriter;
    protected IOException     _exception;
    private static final int BufferSize = 4096;
    private final char[]  _buffer = new char[ BufferSize ];
    private int           _pos = 0;
    public Printer( Writer writer, OutputFormat format)
    {
        _writer = writer;
        _format = format;
        _exception = null;
        _dtdWriter = null;
        _docWriter = null;
        _pos = 0;
    }
    public IOException getException()
    {
        return _exception;
    }
    public void enterDTD()
        throws IOException
    {
        if ( _dtdWriter == null ) {
	    flushLine( false );
			_dtdWriter = new StringWriter();
            _docWriter = _writer;
            _writer = _dtdWriter;
        }
    }
    public String leaveDTD()
        throws IOException
    {
        if ( _writer == _dtdWriter ) {
            flushLine( false );
			_writer = _docWriter;
            return _dtdWriter.toString();
        }
        return null;
    }
    public void printText( String text )
        throws IOException
    {
        try {
            int length = text.length();
            for ( int i = 0 ; i < length ; ++i ) {
                if ( _pos == BufferSize ) {
                    _writer.write( _buffer );
                    _pos = 0;
                }
                _buffer[ _pos ] = text.charAt( i );
                ++_pos;
            }
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }
    public void printText( StringBuffer text )
        throws IOException
    {
        try {
            int length = text.length();
            for ( int i = 0 ; i < length ; ++i ) {
                if ( _pos == BufferSize ) {
                    _writer.write( _buffer );
                    _pos = 0;
                }
                _buffer[ _pos ] = text.charAt( i );
                ++_pos;
            }
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }
    public void printText( char[] chars, int start, int length )
        throws IOException
    {
        try {
            while ( length-- > 0 ) {
                if ( _pos == BufferSize ) {
                    _writer.write( _buffer );
                    _pos = 0;
                }
                _buffer[ _pos ] = chars[ start ];
                ++start;
                ++_pos;
            }
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }
    public void printText( char ch )
        throws IOException
    {
        try {
            if ( _pos == BufferSize ) {
                _writer.write( _buffer );
                _pos = 0;
            }
            _buffer[ _pos ] = ch;
            ++_pos;
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }
    public void printSpace()
        throws IOException
    {
        try {
            if ( _pos == BufferSize ) {
                _writer.write( _buffer );
                _pos = 0;
            }
            _buffer[ _pos ] = ' ';
            ++_pos;
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }
    public void breakLine()
        throws IOException
    {
        try {
            if ( _pos == BufferSize ) {
                _writer.write( _buffer );
                _pos = 0;
            }
            _buffer[ _pos ] = '\n';
            ++_pos;
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
    }
    public void breakLine( boolean preserveSpace )
        throws IOException
    {
        breakLine();
    }
    public void flushLine( boolean preserveSpace )
        throws IOException
    {
        try {
            _writer.write( _buffer, 0, _pos );
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
        }
        _pos = 0;
    }
    public void flush()
        throws IOException
    {
        try {
            _writer.write( _buffer, 0, _pos );
            _writer.flush();
        } catch ( IOException except ) {
            if ( _exception == null )
                _exception = except;
            throw except;
        }
        _pos = 0;
    }
    public void indent()
    {
    }
    public void unindent()
    {
    }
    public int getNextIndent()
    {
        return 0;
    }
    public void setNextIndent( int indent )
    {
    }
    public void setThisIndent( int indent )
    {
    }
}