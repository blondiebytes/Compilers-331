package symbolTable;

import errors.CompilerError;
import errors.SymbolTableError;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

public class SymbolTable {

	private Hashtable<String, SymbolTableEntry> symTable;

	// Init a symbol table
	public SymbolTable() { symTable = new Hashtable<>(); }

	public SymbolTable (int size)
	{
		symTable = new Hashtable<>(size);
	}

	// Lookup an entry in the symbol table via name (key)
	public SymbolTableEntry lookup (String key) { return symTable.get(key.toUpperCase()); }

	// Insert a new entry into the symbol table
	public void insert(SymbolTableEntry entry) throws SymbolTableError
	{
		entry.setName(entry.getName().toUpperCase());
		if (lookup(entry.getName()) == null) {
			symTable.put(entry.getName(), entry);
		} else {
			throw SymbolTableError.DuplicateEntry(entry.getName());
		}

	}

	// Get the size of the symbol table
	public int size() {
		return symTable.size();
	}

	// Dump the contents of the symbol table
	public void dumpTable () {
		System.out.println("Name : Entry");
		for (String k : symTable.keySet()) {
			System.out.println(k + " : " + lookup(k));
		}
	}

	// Installs the following reserved names in the symbol table that is passed to it as a parameter:
	// MAIN, READ, and WRITE as Procedure entries with 0 parameters
	// INPUT and OUTPUT as IODevice entries

	public static void installBuiltins(SymbolTable table) throws SymbolTableError{
		// Make the entries
		SymbolTableEntry main = new ProcedureEntry("MAIN", 0, new LinkedList());
		SymbolTableEntry read = new ProcedureEntry("READ", 0, new LinkedList());
		SymbolTableEntry write = new ProcedureEntry("WRITE", 0, new LinkedList());

		// Make them reserved
		main.setIsReserved(true);
		read.setIsReserved(true);
		write.setIsReserved(true);

		// Insert them
		table.insert(main);
		table.insert(read);
		table.insert(write);

	}

}
