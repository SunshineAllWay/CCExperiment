package org.apache.xerces.util;
public final class ShadowedSymbolTable
extends SymbolTable {
    protected SymbolTable fSymbolTable;
    public ShadowedSymbolTable(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
    } 
    public String addSymbol(String symbol) {
        if (fSymbolTable.containsSymbol(symbol)) {
            return fSymbolTable.addSymbol(symbol);
        }
        return super.addSymbol(symbol);
    } 
    public String addSymbol(char[] buffer, int offset, int length) {
        if (fSymbolTable.containsSymbol(buffer, offset, length)) {
            return fSymbolTable.addSymbol(buffer, offset, length);
        }
        return super.addSymbol(buffer, offset, length);
    } 
    public int hash(String symbol) {
        return fSymbolTable.hash(symbol);
    } 
    public int hash(char[] buffer, int offset, int length) {
        return fSymbolTable.hash(buffer, offset, length);
    } 
} 
